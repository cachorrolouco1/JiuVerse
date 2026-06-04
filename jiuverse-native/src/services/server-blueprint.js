/**
 * JIUVERSE GAME ROOM SERVER - SOCKET.IO BLUEPRINT
 * Scale-out architecture for handling thousands of concurrent fighters
 * in multiple public/private 2.5D Isometric Dojos.
 */

const http = require('http');
const { Server } = require('socket.io');

const PORT = process.argv[2] || 4000;
const server = http.createServer();
const io = new Server(server, {
  cors: { origin: '*', methods: ['GET', 'POST'] },
  pingTimeout: 10000,
  pingInterval: 5000
});

// Collision maps representing standard 10x10 isometric rooms (1 = Blocked, 0 = Walkable)
const COLLISION_MAPS = {
  tatame_central: [
    [1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 1, 1, 1, 1, 1, 1, 1, 1, 1]
  ],
  dojo_privado_gracie: [
    [1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 1, 1, 0, 0, 1, 1, 0, 1],
    [1, 0, 1, 1, 0, 0, 1, 1, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 1, 1, 1, 1, 1, 1, 1, 1, 1]
  ]
};

// Memory store tracking connected players
const players = {}; 
// Rooms tracking (roomID -> Map of players)
const rooms = {};

// Spatial Partitioning Bucket size (in grid tiles) to optimize proximity searches (O(N) vs O(N^2))
const BUCKET_SIZE = 2;

function getSpatialBucket(x, y) {
  const bx = Math.floor(x / BUCKET_SIZE);
  const by = Math.floor(y / BUCKET_SIZE);
  return `${bx}_${by}`;
}

io.on('connection', (socket) => {
  console.log(`[CONEXÃO] Lutador conectado: ${socket.id}`);

  // 1. Spawning / Joining a Game Dojo (supports public & private instances)
  socket.on('join_dojo', ({ nickname, belt, dojoId, isPrivate }) => {
    const activeDojo = dojoId || 'tatame_central';
    
    // Spawn placement strategy: Find a walkable spot
    let spawnX = 2;
    let spawnY = 2;
    const map = COLLISION_MAPS[activeDojo] || COLLISION_MAPS['tatame_central'];
    
    // Safety fallback
    if (map[spawnY] && map[spawnY][spawnX] === 1) {
      spawnX = 3;
      spawnY = 3;
    }

    // Register active session
    players[socket.id] = {
      id: socket.id,
      nickname,
      belt: belt || 'Azul',
      x: spawnX,
      y: spawnY,
      room: activeDojo,
      isPrivate: !!isPrivate
    };

    socket.join(activeDojo);

    if (!rooms[activeDojo]) {
      rooms[activeDojo] = {};
    }
    rooms[activeDojo][socket.id] = players[socket.id];

    console.log(`[JOIN] ${nickname} entrou na sala síncrona: ${activeDojo} em (${spawnX}, ${spawnY})`);

    // Notify local player of their spawn coordinates
    socket.emit('spawned_local', {
      self: players[socket.id],
      others: Object.values(rooms[activeDojo]).filter(p => p.id !== socket.id)
    });

    // Broadcast spawn to all other players in that specific room
    socket.to(activeDojo).emit('player_spawned', players[socket.id]);
  });

  // 2. Real-time Movement & Collision sync logic
  socket.on('move', ({ x, y }) => {
    const player = players[socket.id];
    if (!player) return;

    const map = COLLISION_MAPS[player.room] || COLLISION_MAPS['tatame_central'];

    // Server-side boundary & collision verification (anti-cheat limit)
    if (y >= 0 && y < map.length && x >= 0 && x < map[0].length) {
      if (map[y][x] === 0) {
        player.x = x;
        player.y = y;

        // Broadcast verified position update instantly
        socket.to(player.room).emit('player_moved', {
          id: socket.id,
          x: x,
          y: y
        });

        // Periodic proximity update check (Spatial Grid Bucket check)
        sendSpatialProximityUpdates(player.room);

      } else {
        // Enforce rewind position in case of collision cheat
        socket.emit('rewind_position', { x: player.x, y: player.y });
      }
    }
  });

  // 3. Proximity-based chat message propagation
  socket.on('say', ({ text }) => {
    const player = players[socket.id];
    if (!player) return;

    // Send only to players in the same room who are close (distance <= 3 tiles)
    const roomPlayers = rooms[player.room] || {};
    Object.values(roomPlayers).forEach((other) => {
      const distance = Math.hypot(player.x - other.x, player.y - other.y);
      if (distance <= 3.0) {
        io.to(other.id).emit('whisper', {
          sender: player.nickname,
          text: text,
          time: new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
        });
      }
    });
  });

  // 4. Cleanup on disconnect
  socket.on('disconnect', () => {
    const player = players[socket.id];
    if (player) {
      const room = player.room;
      if (rooms[room]) {
        delete rooms[room][socket.id];
      }
      delete players[socket.id];

      // Broadcast disappearance
      socket.to(room).emit('player_disconnected', { id: socket.id });
      console.log(`[DESCONECTADO] Lutador deslogou do JiuVerse: ${socket.id}`);
    }
  });
});

// Spatial proximity analysis
function sendSpatialProximityUpdates(room) {
  const roomPlayers = rooms[room] || {};
  const playersList = Object.values(roomPlayers);

  // Group players into spatial buckets
  const buckets = {};
  playersList.forEach((player) => {
    const bucketKey = getSpatialBucket(player.x, player.y);
    if (!buckets[bucketKey]) buckets[bucketKey] = [];
    buckets[bucketKey].push(player);
  });

  // Notify players of close neighbors only (great for thousands of concurrent users)
  playersList.forEach((player) => {
    const playerBucketKey = getSpatialBucket(player.x, player.y);
    const neighbors = [];

    // Check self bucket and adjacent 8 buckets
    const [bx, by] = playerBucketKey.split('_').map(Number);
    for (let dx = -1; dx <= 1; dx++) {
      for (let dy = -1; dy <= 1; dy++) {
        const neighborKey = `${bx + dx}_${by + dy}`;
        if (buckets[neighborKey]) {
          neighbors.push(...buckets[neighborKey]);
        }
      }
    }

    // Filter by strict radius and push update to specific connection
    const visibleFighters = neighbors.filter(other => {
      if (other.id === player.id) return false;
      const distance = Math.hypot(player.x - other.x, player.y - other.y);
      return distance <= 3.5; // meter visibility threshold
    });

    io.to(player.id).emit('proximity_visibilities_snapshot', {
      visiblePlayers: visibleFighters.map(f => ({ id: f.id, nickname: f.nickname, belt: f.belt, x: f.x, y: f.y }))
    });
  });
}

server.listen(PORT, () => {
  console.log(`===============================================`);
  console.log(`🥋 JIUVERSE SOCKET SERVER INICIADO NA PORTA ${PORT}`);
  console.log(`🚀 Escabilidade Ativa: Spatial grid bucket partitioning: HABILITADO`);
  console.log(`🔑 Rooms: tatame_central, tatame_avancado, dojo_privado_gracie`);
  console.log(`===============================================`);
});

module.exports = { server, io };
