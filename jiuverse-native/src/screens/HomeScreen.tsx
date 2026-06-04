import React, { useState, useEffect, useRef } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  TouchableOpacity, 
  ActivityIndicator, 
  Switch,
  ScrollView
} from 'react-native';
import { useAuthStore } from '../store/useAuthStore';
import { useGameStore } from '../store/useGameStore';
import { useSocket } from '../hooks/useSocket';

export default function HomeScreen() {
  const user = useAuthStore((state) => state.user);
  const token = useAuthStore((state) => state.token);
  const activePosition = useGameStore((state) => state.activeDojoPosition);

  // Connection management
  const [currentDojo, setCurrentDojo] = useState('tatame_central');
  const { isConnected, emitMovement } = useSocket(currentDojo, token);

  // Screen level states for engine control HUD
  const [debugMode, setDebugMode] = useState(true);
  const [proximityChatEnabled, setProximityChatEnabled] = useState(true);
  const [proximityThreshold, setProximityThreshold] = useState(150); // pixels
  const [privateRoomJoined, setPrivateRoomJoined] = useState(false);

  // Active rooms in the system
  const publicRooms = ['tatame_central', 'tatame_avancado', 'tatame_competicao'];
  const privateRooms = ['dojo_privado_gracie', 'dojo_privado_alliance'];

  // Inline Phaser 3 Isometric HTML Bundle loaded via conceptual WebView
  const getPhaserIsometricBundle = () => {
    return `
      <!DOCTYPE html>
      <html>
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <style>
          body, html { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background-color: #030712; font-family: monospace; }
          #game-container { width: 100%; height: 100%; }
          #diagnostic-hud {
            position: absolute; top: 10px; left: 10px; background: rgba(15, 23, 42, 0.9);
            border: 1px solid #06b6d4; padding: 8px; border-radius: 4px; color: #f8fafc;
            font-size: 10px; pointer-events: none; z-index: 100; max-width: 250px;
          }
        </style>
        <script src="https://cdn.jsdelivr.net/npm/phaser@3.60.0/dist/phaser.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/socket.io-client@4.7.5/dist/socket.io.min.js"></script>
      </head>
      <body>
        <div id="diagnostic-hud">
          <span style="color: #06b6d4; font-weight: bold;">JIUVERSE PHASER V3.60</span><br/>
          FPS: <span id="val-fps">60</span> | Ping: <span id="val-latency">12ms</span><br/>
          Room: <span style="color: #e11d48;" id="val-room">${currentDojo}</span><br/>
          Proximity Proximity Limit: <span id="val-proxl">${proximityThreshold}px</span><br/>
          Players sync: <span id="val-players">1</span> ONLINE
        </div>
        <div id="game-container"></div>

        <script>
          // Standard isometric coordinate translation formulas for 2.5D Social World
          const TILE_WIDTH = 64;
          const TILE_HEIGHT = 32;
          const MAP_OFFSET_X = 200;
          const MAP_OFFSET_Y = 120;
          const GRID_SIZE = 10; // 10x10 tatami mats

          function cartToIso(x, y) {
            return {
              x: (x - y) * (TILE_WIDTH / 2) + MAP_OFFSET_X,
              y: (x + y) * (TILE_HEIGHT / 2) + MAP_OFFSET_Y
            };
          }

          function isoToCart(x, y) {
            let offsetCachedX = x - MAP_OFFSET_X;
            let offsetCachedY = y - MAP_OFFSET_Y;
            return {
              x: Math.round((offsetCachedX / (TILE_WIDTH / 2) + offsetCachedY / (TILE_HEIGHT / 2)) / 2),
              y: Math.round((offsetCachedY / (TILE_HEIGHT / 2) - offsetCachedX / (TILE_WIDTH / 2)) / 2)
            };
          }

          class JiuVerseIsometricDojo extends Phaser.Scene {
            constructor() {
              super({ key: 'JiuVerseDojo' });
              this.localPlayer = null;
              this.otherPlayers = {};
              this.tatamiTiles = [];
              this.collisionGrid = [];
              this.cursors = null;
              this.proxRadius = ${proximityThreshold};
            }

            preload() {
              // Generation of procedural colored textures fallback in case of no server images
              let canvas = this.textures.createCanvas('tile_base', TILE_WIDTH, TILE_HEIGHT);
              let ctx = canvas.context;
              ctx.fillStyle = '#1e293b';
              ctx.lineWidth = 1;
              ctx.strokeStyle = '#06b6d4';
              // Draw isometric diamond tile path
              ctx.beginPath();
              ctx.moveTo(TILE_WIDTH / 2, 0);
              ctx.lineTo(TILE_WIDTH, TILE_HEIGHT / 2);
              ctx.lineTo(TILE_WIDTH / 2, TILE_HEIGHT);
              ctx.lineTo(0, TILE_HEIGHT / 2);
              ctx.closePath();
              ctx.fill();
              ctx.stroke();
              canvas.refresh();

              // Canvas for avatar
              let avatar = this.textures.createCanvas('char_front', 32, 64);
              let actx = avatar.context;
              // Body (Kimono)
              actx.fillStyle = '#3b82f6';
              actx.fillRect(4, 20, 24, 44);
              // Belt (Faixa Azul)
              actx.fillStyle = '#2563eb';
              actx.fillRect(2, 36, 28, 6);
              actx.fillStyle = '#ffffff';
              actx.fillRect(20, 36, 6, 6); // tip
              // Head
              actx.fillStyle = '#fbcfe8';
              actx.beginPath();
              actx.arc(16, 12, 10, 0, Math.PI * 2);
              actx.fill();
              avatar.refresh();
            }

            create() {
              this.cameras.main.setBackgroundColor('#090d16');
              this.cursors = this.input.keyboard.createCursorKeys();

              // 1. Build Isometric Grid
              for (let x = 0; x < GRID_SIZE; x++) {
                this.collisionGrid[x] = [];
                for (let y = 0; y < GRID_SIZE; y++) {
                  // Put colliders on border walls
                  const isBlocked = (x === 0 || y === 0 || x === GRID_SIZE - 1 || y === GRID_SIZE - 1);
                  this.collisionGrid[x][y] = isBlocked;

                  let pos = cartToIso(x, y);
                  let tile = this.add.image(pos.x, pos.y, 'tile_base');
                  
                  if (isBlocked) {
                    tile.setTint(0xf43f5e); // Boundary walls tinted Red
                  }
                  
                  this.tatamiTiles.push(tile);
                }
              }

              // 2. Spawn local player avatar
              let startCell = cartToIso(${activePosition.x}, ${activePosition.y});
              this.localPlayer = this.add.container(startCell.x, startCell.y);
              
              let playerSprite = this.add.image(0, -32, 'char_front');
              let tagText = this.add.text(0, -78, '${user?.nickname || "OssMaster"}', {
                fontSize: '11px',
                fontFamily: 'monospace',
                backgroundColor: 'rgba(15, 23, 42, 0.85)',
                padding: { x: 4, y: 2 },
                color: '#2dd4bf',
                stroke: '#0891b2',
                strokeThickness: 1
              }).setOrigin(0.5);

              let beltIndicator = this.add.text(0, -62, '[FAIXA ${user?.belt || "Azul"}]', {
                fontSize: '8px',
                fontFamily: 'monospace',
                color: '#38bdf8'
              }).setOrigin(0.5);

              this.localPlayer.add([playerSprite, tagText, beltIndicator]);
              this.localPlayer.gridX = ${activePosition.x};
              this.localPlayer.gridY = ${activePosition.y};

              this.cameras.main.startFollow(this.localPlayer, true, 0.1, 0.1);

              // 3. Connect Sockets mock integration to bridge React Native view
              this.initNetworkSync();
            }

            initNetworkSync() {
              // Real-time loop to simulate online users stepping onto tatame tiles
              this.mockMultiplayerSpawn();
            }

            mockMultiplayerSpawn() {
              const botNames = ['GuardaPassada90', 'FaixaPretaMestre', 'ArmLockDeLaRiva'];
              const botBelts = ['Roxa', 'Preta', 'Branca'];
              const colors = [0xa855f7, 0x1e293b, 0xf8fafc];

              botNames.forEach((bname, idx) => {
                let rx = Math.floor(Math.random() * (GRID_SIZE - 2)) + 1;
                let ry = Math.floor(Math.random() * (GRID_SIZE - 2)) + 1;
                let isoPos = cartToIso(rx, ry);

                let remoteC = this.add.container(isoPos.x, isoPos.y);
                let sprite = this.add.image(0, -32, 'char_front');
                sprite.setTint(colors[idx]); // Color-coded belt match

                let tag = this.add.text(0, -78, bname, {
                  fontSize: '10px',
                  fontFamily: 'monospace',
                  backgroundColor: 'rgba(15, 23, 42, 0.85)',
                  padding: { x: 4, y: 2 },
                  color: '#94a3b8'
                }).setOrigin(0.5);

                let beltText = this.add.text(0, -62, \`[FAIXA \${botBelts[idx]}]\`, {
                  fontSize: '8px',
                  fontFamily: 'monospace',
                  color: '#a8a29e'
                }).setOrigin(0.5);

                remoteC.add([sprite, tag, beltText]);
                remoteC.gridX = rx;
                remoteC.gridY = ry;

                this.otherPlayers[bname] = remoteC;
              });

              document.getElementById('val-players').innerText = Object.keys(this.otherPlayers).length + 1;
            }

            update(time, delta) {
              document.getElementById('val-fps').innerText = Math.round(1000 / delta);

              // Only move every 150ms to respect tickrate pacing
              if (!this.lastMoveTime) this.lastMoveTime = 0;
              if (time < this.lastMoveTime + 180) return;

              let dx = 0;
              let dy = 0;

              if (this.cursors.left.isDown) { dx = -1; }
              else if (this.cursors.right.isDown) { dx = 1; }
              else if (this.cursors.up.isDown) { dy = -1; }
              else if (this.cursors.down.isDown) { dy = 1; }

              if (dx !== 0 || dy !== 0) {
                let nextX = this.localPlayer.gridX + dx;
                let nextY = this.localPlayer.gridY + dy;

                // Collision system boundaries constraint & walls blocking logic
                if (nextX >= 0 && nextX < GRID_SIZE && nextY >= 0 && nextY < GRID_SIZE) {
                  if (!this.collisionGrid[nextX][nextY]) {
                    this.localPlayer.gridX = nextX;
                    this.localPlayer.gridY = nextY;
                    
                    let targetIso = cartToIso(nextX, nextY);
                    
                    // Simple Tween animation system for real isometric movement transition
                    this.tweens.add({
                      targets: this.localPlayer,
                      x: targetIso.x,
                      y: targetIso.y,
                      duration: 150,
                      ease: 'Power1'
                    });

                    this.lastMoveTime = time;

                    // Send position sync back to react native layer
                    if (window.ReactNativeWebView) {
                      window.ReactNativeWebView.postMessage(JSON.stringify({
                        type: 'PLAYER_MOVE',
                        x: nextX,
                        y: nextY
                      }));
                    }
                  } else {
                    // Flash red screen briefly during collisions
                    this.cameras.main.flash(80, 244, 63, 94, false);
                  }
                }
              }

              // Evaluate proximity updates of other fighters (proximity check)
              this.evaluateProximityThresholds();
            }

            evaluateProximityThresholds() {
              Object.keys(this.otherPlayers).forEach((id) => {
                let remoteC = this.otherPlayers[id];
                let dist = Phaser.Math.Distance.Between(this.localPlayer.x, this.localPlayer.y, remoteC.x, remoteC.y);

                let isClose = dist < this.proxRadius;
                
                // Show connection lines & proximity text bubbles
                if (isClose) {
                  remoteC.list[1].setColor('#38bdf8'); // Highlight tag
                  if (!remoteC.connLine) {
                    remoteC.connLine = this.add.graphics();
                    remoteC.bubble = this.add.text(remoteC.x, remoteC.y - 100, "ROLAR?", {
                      fontSize: '9px',
                      fontFamily: 'monospace',
                      backgroundColor: '#10b981',
                      padding: { x: 3, y: 1 },
                      color: '#000000',
                      fontWeight: 'bold'
                    }).setOrigin(0.5);
                  }
                  
                  remoteC.connLine.clear();
                  remoteC.connLine.lineStyle(1.5, 0x14b8a6, 0.4);
                  remoteC.connLine.lineBetween(this.localPlayer.x, this.localPlayer.y - 20, remoteC.x, remoteC.y - 20);
                  remoteC.bubble.setPosition(remoteC.x, remoteC.y - 100);
                  remoteC.bubble.setVisible(true);
                } else {
                  remoteC.list[1].setColor('#94a3b8');
                  if (remoteC.connLine) {
                    remoteC.connLine.clear();
                    remoteC.bubble.setVisible(false);
                  }
                }
              });
            }
          }

          const config = {
            type: Phaser.AUTO,
            width: window.innerWidth,
            height: window.innerHeight,
            parent: 'game-container',
            scene: JiuVerseIsometricDojo,
            physics: { default: 'arcade', arcade: { debug: ${debugMode} } }
          };

          const game = new Phaser.Game(config);

          window.addEventListener('resize', () => {
            game.scale.resize(window.innerWidth, window.innerHeight);
          });
        </script>
      </body>
      </html>
    `;
  };

  return (
    <View style={styles.container}>
      {/* Real-time Status HUD Header */}
      <View style={styles.headerBox}>
        <View>
          <Text style={styles.title}>JIUVERSE ESCALABILIDADE DE MAPA (2.5D)</Text>
          <Text style={styles.subtitle}>Instanciamento Síncrono de Sockets de Proximidade</Text>
        </View>
        <View style={[styles.badge, isConnected ? styles.bgGreen : styles.bgRed]}>
          <Text style={styles.badgeText}>{isConnected ? 'CONECTADO 10Hz' : 'OFFLINE'}</Text>
        </View>
      </View>

      {/* Visual Simulated Phaser Canvas Container */}
      <View style={styles.canvasContainer}>
        {/* We present a visually satisfying tactical isometric minimap with collision boundaries and bots */}
        <View style={styles.phaserSimulatorPlaceholder}>
          <Text style={styles.simText}>🎮 VISUALIZADOR 2.5D ISOMÉTRICO DO DOJO</Text>
          <Text style={styles.simSubtext}>Arraste no controle abaixo para movimentar ou clique na mesa</Text>
          
          <ScrollView horizontal contentContainerStyle={styles.canvasScroll} scrollEnabled={false}>
            <View style={styles.tatamiGrid}>
              {[...Array(6)].map((_, r) => (
                <View key={r} style={styles.tatamiRow}>
                  {[...Array(6)].map((_, c) => {
                    const isPlayer = activePosition.x === c && activePosition.y === r;
                    const isBot = (c === 2 && r === 4) || (c === 4 && r === 1);
                    const isNear = Math.abs(activePosition.x - c) <= 2 && Math.abs(activePosition.y - r) <= 2;
                    const isBlockCoord = c === 0 || r === 0 || c === 5 || r === 5;

                    return (
                      <View 
                        key={c} 
                        style={[
                          styles.tatamiTile,
                          isBlockCoord && styles.tileWall,
                          isPlayer && styles.tilePlayer,
                          isBot && styles.tileBot
                        ]}
                      >
                        {isPlayer && <Text style={styles.tileEmoji}>🥋</Text>}
                        {isBot && isNear && (
                          <View style={styles.proximityBubble}>
                            <Text style={styles.bubbleText}>Rolar?</Text>
                          </View>
                        )}
                        {!isPlayer && !isBot && isNear && proximityChatEnabled && (
                          <View style={styles.radarLine} />
                        )}
                      </View>
                    );
                  })}
                </View>
              ))}
            </View>
          </ScrollView>
        </View>
      </View>

      {/* Interactive Controller and Subsystems Config Panels */}
      <View style={styles.hudSettings}>
        <View style={styles.configHeader}>
          <Text style={styles.sectionTitle}>SALA DE CONEXÃO MULTIPLAYER</Text>
          <Text style={styles.sectionDesc}>Mudar canais para evitar sobrecarga de Sockets</Text>
        </View>

        {/* Public / Private instance pickers */}
        <View style={styles.row}>
          <Text style={styles.label}>Tatame Público:</Text>
          <View style={styles.tabsRow}>
            {publicRooms.map((room) => (
              <TouchableOpacity
                key={room}
                style={[styles.miniTabButton, currentDojo === room && styles.miniTabActive]}
                onPress={() => {
                  setCurrentDojo(room);
                  setPrivateRoomJoined(false);
                }}
              >
                <Text style={[styles.miniTabLabel, currentDojo === room && styles.miniTextActive]}>
                  {room === 'tatame_central' ? 'Geral' : room === 'tatame_avancado' ? 'Avançados' : 'ADCC'}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        <View style={styles.row}>
          <Text style={styles.label}>Sala Reservada:</Text>
          <View style={styles.tabsRow}>
            {privateRooms.map((room) => (
              <TouchableOpacity
                key={room}
                style={[styles.miniTabButton, currentDojo === room && styles.miniTabActive]}
                onPress={() => {
                  setCurrentDojo(room);
                  setPrivateRoomJoined(true);
                }}
              >
                <Text style={[styles.miniTabLabel, currentDojo === room && styles.miniTextActive]}>
                  {room.includes('gracie') ? 'Mestre Gracie' : 'Alliance'}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        <View style={styles.separator} />

        {/* Proximity chat toggle switches */}
        <View style={styles.switchRow}>
          <View>
            <Text style={styles.switchTitle}>Filtro de Proximidade (Spatial)</Text>
            <Text style={styles.switchDesc}>Limita renderização de jogadores vizinhos</Text>
          </View>
          <Switch 
            value={proximityChatEnabled} 
            onValueChange={setProximityChatEnabled}
            thumbColor="#14B8A6"
            trackColor={{ false: '#334155', true: '#0F766E' }}
          />
        </View>

        {/* Proximity dynamic threshold */}
        <View style={styles.proxSliderControl}>
          <Text style={styles.label}>Área de Visibilidade Proximidade: {proximityThreshold} metros</Text>
          <View style={styles.sliderMockBar}>
            <TouchableOpacity onPress={() => setProximityThreshold(100)} style={[styles.sliderTick, proximityThreshold === 100 && styles.sliderActiveTick]}>
              <Text style={styles.tickLabel}>Estrito</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => setProximityThreshold(150)} style={[styles.sliderTick, proximityThreshold === 150 && styles.sliderActiveTick]}>
              <Text style={styles.tickLabel}>Médio</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => setProximityThreshold(250)} style={[styles.sliderTick, proximityThreshold === 250 && styles.sliderActiveTick]}>
              <Text style={styles.tickLabel}>Amplo</Text>
            </TouchableOpacity>
          </View>
        </View>

        <View style={styles.manualControlsBox}>
          <Text style={styles.controlCenterText}>Direcional do Teclado Virtual (100ms lag síncrono):</Text>
          <View style={styles.keyPad}>
            <TouchableOpacity 
              style={[styles.padKey, activePosition.y <= 1 && styles.keyDisabled]}
              disabled={activePosition.y <= 1}
              onPress={() => {
                const nextX = activePosition.x;
                const nextY = Math.max(1, activePosition.y - 1);
                emitMovement(nextX, nextY);
              }}
            >
              <Text style={styles.padKeyText}>▲</Text>
            </TouchableOpacity>
            <View style={styles.keyPadMiddle}>
              <TouchableOpacity 
                style={[styles.padKey, activePosition.x <= 1 && styles.keyDisabled]}
                disabled={activePosition.x <= 1}
                onPress={() => {
                  const nextX = Math.max(1, activePosition.x - 1);
                  const nextY = activePosition.y;
                  emitMovement(nextX, nextY);
                }}
              >
                <Text style={styles.padKeyText}>◀</Text>
              </TouchableOpacity>
              <View style={styles.padKeyCenter}>
                <Text style={styles.centerDot}>🥋</Text>
              </View>
              <TouchableOpacity 
                style={[styles.padKey, activePosition.x >= 4 && styles.keyDisabled]}
                disabled={activePosition.x >= 4}
                onPress={() => {
                  const nextX = Math.min(4, activePosition.x + 1);
                  const nextY = activePosition.y;
                  emitMovement(nextX, nextY);
                }}
              >
                <Text style={styles.padKeyText}>▶</Text>
              </TouchableOpacity>
            </View>
            <TouchableOpacity 
              style={[styles.padKey, activePosition.y >= 4 && styles.keyDisabled]}
              disabled={activePosition.y >= 4}
              onPress={() => {
                const nextX = activePosition.x;
                const nextY = Math.min(4, activePosition.y + 1);
                emitMovement(nextX, nextY);
              }}
            >
              <Text style={styles.padKeyText}>▼</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#090D16',
    padding: 10,
  },
  headerBox: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#0F172A',
    borderColor: '#1E293B',
    borderWidth: 1,
    borderRadius: 8,
    padding: 10,
    marginBottom: 8,
  },
  title: {
    fontSize: 10,
    fontWeight: 'black',
    color: '#06B6D4',
  },
  subtitle: {
    fontSize: 8,
    color: '#94A3B8',
    marginTop: 2,
  },
  badge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 4,
  },
  bgGreen: {
    backgroundColor: '#14532D',
    borderColor: '#22C55E',
    borderWidth: 0.5,
  },
  bgRed: {
    backgroundColor: '#7F1D1D',
    borderColor: '#EF4444',
    borderWidth: 0.5,
  },
  badgeText: {
    fontSize: 8,
    fontWeight: 'bold',
    color: '#F8FAFC',
  },
  canvasContainer: {
    flex: 1.2,
    backgroundColor: '#020617',
    borderWidth: 1,
    borderColor: '#1E293B',
    borderRadius: 12,
    overflow: 'hidden',
    position: 'relative',
    minHeight: 220,
  },
  phaserSimulatorPlaceholder: {
    flex: 1,
    padding: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
  simText: {
    fontSize: 10,
    fontWeight: 'bold',
    color: '#14B8A6',
    letterSpacing: 0.5,
    marginBottom: 2,
  },
  simSubtext: {
    fontSize: 8,
    color: '#64748B',
    marginBottom: 10,
  },
  canvasScroll: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  tatamiGrid: {
    transform: [{ rotateX: '55deg' }, { rotateZ: '-45deg' }],
    width: 200,
    height: 200,
    justifyContent: 'center',
    alignItems: 'center',
  },
  tatamiRow: {
    flexDirection: 'row',
  },
  tatamiTile: {
    width: 30,
    height: 30,
    borderWidth: 0.5,
    borderColor: '#1E293B',
    backgroundColor: '#0F172A',
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
  },
  tileWall: {
    backgroundColor: '#7F1D1D',
    borderColor: '#B91C1C',
  },
  tilePlayer: {
    backgroundColor: '#1E40AF',
    borderColor: '#3B82F6',
  },
  tileBot: {
    backgroundColor: '#6B21A8',
    borderColor: '#8B5CF6',
  },
  tileEmoji: {
    fontSize: 12,
    transform: [{ rotateZ: '45deg' }, { rotateX: '-15deg' }],
  },
  proximityBubble: {
    position: 'absolute',
    top: -24,
    left: -10,
    backgroundColor: '#10B981',
    borderColor: '#059669',
    borderWidth: 0.5,
    paddingHorizontal: 4,
    paddingVertical: 1,
    borderRadius: 4,
    zIndex: 10,
    transform: [{ rotateZ: '45deg' }, { rotateX: '-15deg' }],
  },
  bubbleText: {
    fontSize: 7,
    fontWeight: 'bold',
    color: '#030712',
  },
  radarLine: {
    position: 'absolute',
    width: '100%',
    height: 1,
    backgroundColor: 'rgba(20, 184, 166, 0.4)',
  },
  hudSettings: {
    flex: 1.4,
    marginTop: 8,
    backgroundColor: '#0F172A',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#1E293B',
    padding: 10,
  },
  configHeader: {
    marginBottom: 8,
  },
  sectionTitle: {
    fontSize: 10,
    fontWeight: 'bold',
    color: '#E2E8F0',
  },
  sectionDesc: {
    fontSize: 8,
    color: '#64748B',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginVertical: 4,
  },
  label: {
    fontSize: 9,
    fontWeight: '600',
    color: '#94A3B8',
  },
  tabsRow: {
    flexDirection: 'row',
    backgroundColor: '#020617',
    padding: 2,
    borderRadius: 6,
  },
  miniTabButton: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 4,
    marginHorizontal: 1,
  },
  miniTabActive: {
    backgroundColor: '#1e293b',
    borderColor: '#14B8A6',
    borderWidth: 0.5,
  },
  miniTabLabel: {
    fontSize: 8,
    fontWeight: '500',
    color: '#475569',
  },
  miniTextActive: {
    color: '#14B8A6',
    fontWeight: 'bold',
  },
  separator: {
    height: 0.5,
    backgroundColor: '#1E293B',
    marginVertical: 6,
  },
  switchRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 4,
  },
  switchTitle: {
    fontSize: 9,
    fontWeight: 'bold',
    color: '#E2E8F0',
  },
  switchDesc: {
    fontSize: 7.5,
    color: '#94A3B8',
  },
  proxSliderControl: {
    backgroundColor: '#020617',
    padding: 6,
    borderRadius: 6,
    marginVertical: 4,
  },
  sliderMockBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 6,
    backgroundColor: '#0F172A',
    borderRadius: 4,
    padding: 2,
  },
  sliderTick: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: 4,
  },
  sliderActiveTick: {
    backgroundColor: '#14B8A6',
    borderRadius: 4,
  },
  tickLabel: {
    fontSize: 7.5,
    fontWeight: 'bold',
    color: '#E2E8F0',
  },
  manualControlsBox: {
    marginTop: 8,
    backgroundColor: '#020617',
    borderRadius: 8,
    padding: 6,
    alignItems: 'center',
  },
  controlCenterText: {
    fontSize: 8,
    fontWeight: 'bold',
    color: '#10B981',
    letterSpacing: 0.5,
    marginBottom: 4,
  },
  keyPad: {
    alignItems: 'center',
  },
  keyPadMiddle: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 2,
  },
  padKey: {
    width: 32,
    height: 32,
    backgroundColor: '#1E293B',
    borderColor: '#334155',
    borderWidth: 1,
    borderRadius: 6,
    justifyContent: 'center',
    alignItems: 'center',
  },
  keyDisabled: {
    opacity: 0.3,
  },
  padKeyText: {
    color: '#14B8A6',
    fontSize: 12,
    fontWeight: 'bold',
  },
  padKeyCenter: {
    width: 32,
    height: 32,
    justifyContent: 'center',
    alignItems: 'center',
  },
  centerDot: {
    fontSize: 13,
  },
});
