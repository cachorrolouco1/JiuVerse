package com.example.architecture.views

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs

// Representation of static or interactive landmarks on our isometric mesh
data class IsometricLandmark(
    val name: String,
    val gridX: Int,
    val gridY: Int,
    val iconEmoji: String,
    val description: String,
    val targetTabIdx: Int,
    val accentColor: Color
)

// Characters/NPCs living inside the virtual plaza
data class VirtualMmoCharacter(
    val name: String,
    val handle: String,
    val belt: String,
    val beltColor: Color,
    val defaultGridX: Float,
    val defaultGridY: Float,
    var currentGridX: Float,
    var currentGridY: Float,
    var baseSpeechBubble: String,
    val isNpc: Boolean,
    val level: Int = 1,
    var isOnline: Boolean = true,
    var isFriend: Boolean = false,
    var statusText: String = "Online"
)

enum class MovementState {
    IDLE, WALK, RUN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Initialize state
    LaunchedEffect(Unit) {
        viewModel.initializeSensei(context)
    }

    val playerMemoryState = viewModel.playerMemory.collectAsState()
    val memory = playerMemoryState.value ?: com.example.architecture.database.PlayerMemoryEntity()

    // --- REPRESENTATIVE MAP CANVAS MEASUREMENT SYSTEM ---
    var canvasWidth by remember { mutableStateOf(1000f) }
    var canvasHeight by remember { mutableStateOf(800f) }

    val localDensity = LocalDensity.current

    // Compute coordinate translation variables reactively
    val scale = Math.min(canvasWidth, canvasHeight) / 10f
    val tileHalfW = 1.6f * scale
    val tileHalfH = 0.8f * scale

    val originX = canvasWidth / 2f
    val originY = canvasHeight / 2f + tileHalfH

    // Fast coordinate-to-DP projection mapper
    val getScreenOffsetDp: (Float, Float) -> Pair<androidx.compose.ui.unit.Dp, androidx.compose.ui.unit.Dp> = { gx, gy ->
        val relativeX = gx - 5f
        val relativeY = gy - 5f
        val screenX = originX + (relativeX - relativeY) * tileHalfW
        val screenY = originY + (relativeX + relativeY) * tileHalfH
        val xDp = with(localDensity) { screenX.toDp() }
        val yDp = with(localDensity) { screenY.toDp() }
        Pair(xDp, yDp)
    }

    // --- GAME WORLD DYNAMIC STATE & MULTIPLAYER SIMULATION ---
    var playerGridX by remember { mutableStateOf(5.0f) }
    var playerGridY by remember { mutableStateOf(7.0f) }
    var targetGridX by remember { mutableStateOf(5.0f) }
    var targetGridY by remember { mutableStateOf(7.0f) }

    var movementState by remember { mutableStateOf(MovementState.IDLE) }
    var isRunningModeEnabled by remember { mutableStateOf(true) } // Speed toggle for clicks
    var currentPath by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }

    var bobFrame by remember { mutableStateOf(0f) }
    val bobOffsetPx = (kotlin.math.abs(kotlin.math.sin(bobFrame.toDouble())).toFloat() * (if (movementState == MovementState.RUN) 9f else 5.5f))

    // Helper functions for collisions and obstacle detection
    val isCellWalkable: (Int, Int) -> Boolean = { cx, cy ->
        val landmarksLocations = listOf(
            Pair(5, 1), // Event Tower
            Pair(2, 2), // Portal Academias
            Pair(2, 8), // Arena PvP
            Pair(8, 2), // Dojo Shop / Loja
            Pair(8, 8), // Ranking Hall
            Pair(5, 5)  // Center portal beam
        )
        Pair(cx, cy) !in landmarksLocations
    }

    val isPositionWalkable: (Float, Float) -> Boolean = { px, py ->
        val roundedX = kotlin.math.round(px).toInt().coerceIn(0, 10)
        val roundedY = kotlin.math.round(py).toInt().coerceIn(0, 10)
        isCellWalkable(roundedX, roundedY)
    }

    // BFS Pathfinding Algorithm with obstacle avoidance
    val findPath: (Int, Int, Int, Int) -> List<Pair<Int, Int>> = { startX, startY, endX, endY ->
        var finalEndX = endX
        var finalEndY = endY
        if (!isCellWalkable(finalEndX, finalEndY)) {
            // Re-target to a nearby walkable adjacent cell
            val directions = listOf(
                Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0),
                Pair(1, 1), Pair(-1, 1), Pair(1, -1), Pair(-1, -1)
            )
            for (dir in directions) {
                val nx = finalEndX + dir.first
                val ny = finalEndY + dir.second
                if (nx in 0..10 && ny in 0..10 && isCellWalkable(nx, ny)) {
                    finalEndX = nx
                    finalEndY = ny
                    break
                }
            }
        }

        val q = java.util.ArrayDeque<List<Pair<Int, Int>>>()
        q.add(listOf(Pair(startX, startY)))
        val visited = mutableSetOf<Pair<Int, Int>>()
        visited.add(Pair(startX, startY))

        val stepDirs = listOf(
            Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1),
            Pair(1, 1), Pair(-1, 1), Pair(1, -1), Pair(-1, -1)
        )

        var foundPath: List<Pair<Int, Int>> = emptyList()
        while (q.isNotEmpty()) {
            val path = q.poll() ?: break
            val curr = path.last()
            if (curr.first == finalEndX && curr.second == finalEndY) {
                foundPath = path
                break
            }
            for (dir in stepDirs) {
                val nextX = curr.first + dir.first
                val nextY = curr.second + dir.second
                val nextNode = Pair(nextX, nextY)
                if (nextX in 0..10 && nextY in 0..10 && nextNode !in visited && isCellWalkable(nextX, nextY)) {
                    visited.add(nextNode)
                    q.add(path + nextNode)
                }
            }
        }
        foundPath
    }

    // Glowing animation for the central portal beam
    val beamPulseAnim = rememberInfiniteTransition(label = "portal")
    val beamScale by beamPulseAnim.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beam_scale"
    )

    // Particles/Combat Emote animations
    var activeEmoteParticleCoord by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var activeEmoteParticleText by remember { mutableStateOf<String?>(null) }
    var emoteAlpha by remember { mutableStateOf(0f) }

    // Floating bubble text for Mestre Flavio
    var activeBubbleText by remember { mutableStateOf<String?>("Oss! Bem-vindo ao JiuVerse!") }

    // Background music or FX status flags
    var isVoipEnabled by remember { mutableStateOf(false) }
    var micOutputVolume by remember { mutableStateOf(0.12f) }

    // Dynamic telemetry log list matching the bottom or sidebar console
    val gameEvents = remember {
        mutableStateListOf(
            "📍 [SISTEMA] Entrando na Praça Central (Servidor Central #1 - Latência: 12ms)",
            "💬 [SENSEI AI] OSS! Busque a sabedoria dos tatames nos portais de integração.",
            "🎮 [GAMEPLAY] Caminhe tocando livremente qualquer área do tatame geométrico."
        )
    }

    // Interactive NPCs
    val npcs = remember {
        mutableStateListOf(
            VirtualMmoCharacter(
                name = "Laura",
                handle = "@LauraRoxa",
                belt = "Roxa",
                beltColor = Color(0xFF6B21A8),
                defaultGridX = 3f,
                defaultGridY = 4f,
                currentGridX = 3f,
                currentGridY = 4f,
                baseSpeechBubble = "Oss! Treino das 19h no tatame real tá de pé?",
                isNpc = true,
                level = 18,
                isOnline = true,
                isFriend = true,
                statusText = "Online"
            ),
            VirtualMmoCharacter(
                name = "Guilherme",
                handle = "@GuiPassador",
                belt = "Azul",
                beltColor = Color(0xFF1D4ED8),
                defaultGridX = 8f,
                defaultGridY = 5f,
                currentGridX = 8f,
                currentGridY = 5f,
                baseSpeechBubble = "Foco na raspagem de meia-guarda!",
                isNpc = true,
                level = 12,
                isOnline = true,
                isFriend = false,
                statusText = "Disponível"
            ),
            VirtualMmoCharacter(
                name = "Dragão Negro",
                handle = "@DragoBlack",
                belt = "Preta",
                beltColor = Color.Black,
                defaultGridX = 4f,
                defaultGridY = 2f,
                currentGridX = 4f,
                currentGridY = 2f,
                baseSpeechBubble = "Quem ousa me desafiar na arena PvP?",
                isNpc = true,
                level = 99,
                isOnline = true,
                isFriend = false,
                statusText = "No Fight ⚔️"
            )
        )
    }

    // High Density Landmarks Setup
    val landmarks = remember {
        listOf(
            IsometricLandmark(
                name = "Torre de Eventos",
                gridX = 5,
                gridY = 1,
                iconEmoji = "🏆",
                description = "Área de Eventos & Torneios Locais Autenticados",
                targetTabIdx = 17, // TournamentsTab
                accentColor = BlueprintOrange
            ),
            IsometricLandmark(
                name = "Portal Academias",
                gridX = 2,
                gridY = 2,
                iconEmoji = "⛩️",
                description = "Acesso ao Hub de Academias Reais Integradas",
                targetTabIdx = 8, // AcademyTab
                accentColor = BlueprintCyan
            ),
            IsometricLandmark(
                name = "Arena PvP",
                gridX = 2,
                gridY = 8,
                iconEmoji = "🥋",
                description = "Combates Virtuais Arena de Guildas",
                targetTabIdx = 24, // GuildWarsTab
                accentColor = BlueprintRed
            ),
            IsometricLandmark(
                name = "Dojo Shop / Loja",
                gridX = 8,
                gridY = 2,
                iconEmoji = "🏪",
                description = "Mercado P2P, Equipamentos e Planos de Treino",
                targetTabIdx = 11, // MarketplaceTab
                accentColor = BlueprintTeal
            ),
            IsometricLandmark(
                name = "Ranking Hall",
                gridX = 8,
                gridY = 8,
                iconEmoji = "🎖️",
                description = "Hall da Fama dos Campeões da Core-Ledger",
                targetTabIdx = 17, // TournamentsTab
                accentColor = BlueprintCyan
            )
        )
    }

    // Telepathy chat text state
    var textMessageInput by remember { mutableStateOf("") }

    // --- EXPERT CHAT SYSTEM STATES ---
    var chatChannelSelected by remember { mutableStateOf("GLOBAL") } // "GLOBAL", "LOCAL", "ACADEMIA", "PRIVADO"
    var whisperRecipientByName by remember { mutableStateOf("Laura") } // Which player we are messaging in PRIVADO
    val mutedPlayersList = remember { mutableStateListOf<String>() } // Names of muted players
    val blockedPlayersList = remember { mutableStateListOf<String>() } // Names of blocked players
    var lastChatMessageSentTimestamp by remember { mutableStateOf(0L) } // For anti-spam/flood
    var isModerationPanelDialogVisible by remember { mutableStateOf(false) } // Open safety settings panel
    var chatFilterTextKeyword by remember { mutableStateOf("") }
    
    // --- SOCKET.IO REALTIME PRESENCE & SIDEBAR ACTIONS ---
    var expandedPlayerName by remember { mutableStateOf<String?>(null) }
    var socketConnectionStatus by remember { mutableStateOf("Conectado") } // Conectado, Re-conectando, Latência oscilando
    var socketConnectionPing by remember { mutableStateOf(12) }
    var isPresenceSimulationActive by remember { mutableStateOf(true) } // Randomly toggles presence states in loop // To filter logs

    // --- JOYSTICK TOUCH STATE ---
    var joystickActive by remember { mutableStateOf(false) }
    var joystickDragOffset by remember { mutableStateOf(Offset.Zero) }

    // Unified 25Hz Core Game Loop (Joystick, Pathfinding, Collision slide, Sockets/Simulated MMORPG)
    LaunchedEffect(Unit) {
        while (true) {
            // 1. ANIMATION TAPE (Dynamic bobbing speed matching MovementState)
            when (movementState) {
                MovementState.RUN -> {
                    bobFrame += 0.55f
                }
                MovementState.WALK -> {
                    bobFrame += 0.28f
                }
                MovementState.IDLE -> {
                    if (bobFrame % (2f * kotlin.math.PI.toFloat()) > 0.1f) {
                        bobFrame += 0.2f // complete wave smoothly to rest
                    } else {
                        bobFrame = 0f
                    }
                }
            }

            // 2. JOYSTICK CONTROL (Continuous movement, collision slide)
            if (joystickActive) {
                val maxRadX = with(localDensity) { 45.dp.toPx() }
                val dist = kotlin.math.sqrt(joystickDragOffset.x * joystickDragOffset.x + joystickDragOffset.y * joystickDragOffset.y)
                val fraction = if (maxRadX > 0) (dist / maxRadX).coerceIn(0f, 1f) else 0f
                
                if (fraction > 0.15f) {
                    val isRunning = fraction > 0.65f
                    movementState = if (isRunning) MovementState.RUN else MovementState.WALK
                    val stepMultiplier = if (isRunning) 0.18f else 0.08f
                    
                    // Direct joystick screen direction to visual isometric grid mapping:
                    // Up decreases x and y index
                    // Down increases x and y index
                    // Right increases x and decreases y index
                    // Left decreases x and increases y index
                    val normX = joystickDragOffset.x / dist
                    val normY = joystickDragOffset.y / dist
                    
                    val gridDX = (normY + normX) * stepMultiplier * fraction
                    val gridDY = (normY - normX) * stepMultiplier * fraction
                    
                    val potentialX = (playerGridX + gridDX).coerceIn(0f, 10f)
                    val potentialY = (playerGridY + gridDY).coerceIn(0f, 10f)
                    
                    // Collision avoidance: Check walkthrough, if blocked try single axis sliding (Standard game UX)
                    if (isPositionWalkable(potentialX, potentialY)) {
                        playerGridX = potentialX
                        playerGridY = potentialY
                    } else {
                        if (isPositionWalkable(potentialX, playerGridY)) {
                            playerGridX = potentialX
                        } else if (isPositionWalkable(playerGridX, potentialY)) {
                            playerGridY = potentialY
                        }
                    }
                    targetGridX = playerGridX
                    targetGridY = playerGridY
                } else {
                    movementState = MovementState.IDLE
                }
            } else if (currentPath.isNotEmpty()) {
                // 3. DESKTOP CLICK-TO-WALK A* PATHFINDING QUEUE
                val isRunning = isRunningModeEnabled
                movementState = if (isRunning) MovementState.RUN else MovementState.WALK
                val stepSize = if (isRunning) 0.16f else 0.07f
                
                val nextNode = currentPath.first()
                val targetNodeX = nextNode.first.toFloat()
                val targetNodeY = nextNode.second.toFloat()
                
                val dx = targetNodeX - playerGridX
                val dy = targetNodeY - playerGridY
                val distToNode = kotlin.math.sqrt(dx * dx + dy * dy)
                
                if (distToNode <= stepSize) {
                    // Node reached!
                    playerGridX = targetNodeX
                    playerGridY = targetNodeY
                    currentPath = currentPath.drop(1)
                    
                    if (currentPath.isEmpty()) {
                        movementState = MovementState.IDLE
                        gameEvents.add(0, "📡 [WEB-SOCKET] Coordenadas (X=${String.format("%.1f", playerGridX)}, Y=${String.format("%.1f", playerGridY)}) sincronizadas no Redis PubSub em tempo real.")
                    }
                } else {
                    playerGridX += (dx / distToNode) * stepSize
                    playerGridY += (dy / distToNode) * stepSize
                }
            } else {
                movementState = MovementState.IDLE
            }

            // 4. CHECK PROXIMITY TRIGGERS WITH LANDMARKS
            landmarks.forEach { landmark ->
                if (abs(playerGridX - landmark.gridX) < 0.6f && abs(playerGridY - landmark.gridY) < 0.6f) {
                    val formattedMsg = "🔮 [TATAME INTERATIVO] Aproximou-se de ${landmark.name}. Toque para acessar!"
                    if (gameEvents.firstOrNull() != formattedMsg) {
                        gameEvents.add(0, formattedMsg)
                    }
                }
            }

            // 5. BOOMING MULTIPLAYER SOCKETS EMULATOR (Handles hundreds of players interactions)
            npcs.forEachIndexed { idx, npc ->
                val isBlocked = blockedPlayersList.contains(npc.name)
                val isMuted = mutedPlayersList.contains(npc.name)

                if (!isBlocked) {
                    // Move other players around randomly
                    if (Math.random() < 0.015) {
                        val nx = (1..9).random()
                        val ny = (1..9).random()
                        if (isCellWalkable(nx, ny)) {
                            npc.currentGridX = nx.toFloat()
                            npc.currentGridY = ny.toFloat()
                            
                            val socketTelemetry = listOf(
                                "📡 [SOCKET] Cliente #${1000 + idx} (${npc.handle}) se moveu para (X=$nx, Y=$ny)",
                                "📨 [WS_EMIT] Broadcast para 250 clientes na sala \"praça_central\"",
                                "⚡ [SOCKET_SYNC] Sincronização UDP concluída. Conexões ativas: 14.250"
                            ).random()
                            gameEvents.add(0, socketTelemetry)
                        }
                    }
                    
                    // Simulate socket.io real-time status/presence changes
                    if (isPresenceSimulationActive && Math.random() < 0.003) {
                        val randomStatus = listOf("Online", "Treinando", "Meditação", "Ausente", "Em Combate ⚔️", "Desconectado").random()
                        if (randomStatus == "Desconectado") {
                            if (npc.isOnline) {
                                npc.isOnline = false
                                gameEvents.add(0, "📶 [SOCKET.IO] Presença: ${npc.handle} se desconectou do servidor.")
                            }
                        } else {
                            if (!npc.isOnline) {
                                npc.isOnline = true
                                npc.statusText = "Online"
                                gameEvents.add(0, "📶 [SOCKET.IO] Presença: ${npc.handle} conectou via Socket.IO real-time.")
                            } else {
                                npc.statusText = randomStatus
                                gameEvents.add(0, "📶 [SOCKET.IO] Presença: ${npc.handle} atualizou status para '$randomStatus'")
                            }
                        }
                    }

                    // Random comments/emotes from simulated sockets
                    if (!isMuted && Math.random() < 0.003) {
                        val channel = listOf("GLOBAL", "LOCAL", "ACADEMIA").random()
                        val randomLines = listOf(
                            "Foco na raspagem!",
                            "Hoje tem treino às 19h no tatame Alliance!",
                            "Onde resgata a faixa do Battle Pass?",
                            "OSS! Mestre Flávio!",
                            "Arena PvP tá pegando fogo!"
                        )
                        val messageText = randomLines.random()
                        
                        // Update speech bubbles for local simulation
                        if (npc.isOnline) {
                            npc.baseSpeechBubble = messageText
                            
                            val formattedMsg = when (channel) {
                                "LOCAL" -> "🔊 [LOCAL] ${npc.name}: \"$messageText\""
                                "ACADEMIA" -> "🥋 [ACADEMIA] ${npc.name}: \"$messageText\""
                                else -> "🌍 [GLOBAL] ${npc.name}: \"$messageText\""
                            }
                            gameEvents.add(0, formattedMsg)
                        }
                    }
                }
            }
            if (Math.random() < 0.03) {
                socketConnectionPing = (9..19).random()
                if (Math.random() < 0.1) {
                    socketConnectionStatus = listOf("Conectado", "Sincronizando", "Latência boa").random()
                }
            }

            if (gameEvents.size > 80) {
                gameEvents.removeRange(60, gameEvents.size)
            }

            delay(40) // 25Hz Core Game Loop Execution Rate
        }
    }

    // Dynamic timer to fade Mestre Flavio's speech bubble
    LaunchedEffect(activeBubbleText) {
        if (activeBubbleText != null) {
            delay(5000)
            activeBubbleText = null
        }
    }

    // Dynamic particle anim decay
    LaunchedEffect(activeEmoteParticleCoord) {
        if (activeEmoteParticleCoord != null) {
            emoteAlpha = 1f
            val decaySteps = 10
            for (i in 1..decaySteps) {
                delay(80)
                emoteAlpha -= 0.1f
            }
            activeEmoteParticleCoord = null
            activeEmoteParticleText = null
            emoteAlpha = 0f
        }
    }

    // --- HORIZONTAL MMORPG ORIENTED WORLD ROW ---
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(BlueprintBg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // ================= LEFT PORTION: ISOMETRIC FULL CANVAS AND CONTROLS (70% WIDTH) =================
        Column(
            modifier = Modifier
                .weight(2.3f)
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // HEADER: Telemetry status bars
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = BoxBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile/Level panel
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val headerBeltColor = when (memory.playerBelt) {
                            "Faixa Branca" -> Color.White
                            "Faixa Azul" -> BlueprintCyan
                            "Faixa Roxa" -> Color(0xFFC084FC)
                            "Faixa Marrom" -> Color(0xFFB45309)
                            "Faixa Preta" -> BlueprintRed
                            "Faixa Coral" -> BlueprintOrange
                            else -> Color.White
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(headerBeltColor.copy(alpha = 0.3f))
                                .border(1.5.dp, headerBeltColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🥋", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = memory.playerName.uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueprintTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(BlueprintCyan.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("NÍVEL ${memory.playerLevel}", fontSize = 7.5.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                                }
                            }
                            // XP Mini-bar (Dynamic based on memory.playerXp)
                            val xpPercent = ((memory.playerXp % 1000).toFloat() / 1000f).coerceIn(0.05f, 1f)
                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(4.dp)
                                    .background(Color.DarkGray, RoundedCornerShape(2.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(xpPercent)
                                        .background(BlueprintCyan, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }

                    // Middle Info: Title
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "JIUVERSE WORLD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BlueprintCyan,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(BlueprintGreen, CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PRAÇA CENTRAL", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Wealth displays
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SimpleResourceBadge(emoji = "🪙", amount = "12.450", label = "JiuCoins", tint = BlueprintTeal)
                        SimpleResourceBadge(emoji = "💎", amount = "350", label = "Gemas", tint = BlueprintCyan)
                    }
                }
            }

            // CENTRAL ISOMETRIC CANVAS GAMEBOARD
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .testTag("isometric_map_container")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size ->
                            canvasWidth = size.width.toFloat()
                            canvasHeight = size.height.toFloat()
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                // Inverse isometric transformation formulas
                                val tileHalfWVal = 1.6f * (Math.min(size.width, size.height) / 10f)
                                val tileHalfHVal = 0.8f * (Math.min(size.width, size.height) / 10f)

                                val centerX = size.width / 2f
                                val centerY = size.height / 2f + tileHalfHVal

                                val localX = offset.x - centerX
                                val localY = offset.y - centerY

                                // Inverse equations:
                                val rawGridX = 0.5f * (localY / tileHalfHVal + localX / tileHalfWVal) + 5f
                                val rawGridY = 0.5f * (localY / tileHalfHVal - localX / tileHalfWVal) + 5f

                                // Clamping to grid boundaries (0 to 10)
                                val finalGridX = rawGridX.coerceIn(0f, 10f)
                                val finalGridY = rawGridY.coerceIn(0f, 10f)

                                val startCX = kotlin.math.round(playerGridX).toInt().coerceIn(0, 10)
                                val startCY = kotlin.math.round(playerGridY).toInt().coerceIn(0, 10)
                                val endCX = kotlin.math.round(finalGridX).toInt().coerceIn(0, 10)
                                val endCY = kotlin.math.round(finalGridY).toInt().coerceIn(0, 10)

                                val calculatedPath = findPath(startCX, startCY, endCX, endCY)
                                if (calculatedPath.isNotEmpty()) {
                                    currentPath = calculatedPath
                                    targetGridX = finalGridX
                                    targetGridY = finalGridY
                                    gameEvents.add(
                                        0,
                                        "⚡ [PATH-FINDING] Rota traçada desviando de obstáculos! ${calculatedPath.size} nós computados no grid."
                                    )
                                } else {
                                    // Fallback to direct linear movement if identical coordinates
                                    targetGridX = finalGridX
                                    targetGridY = finalGridY
                                    currentPath = listOf(Pair(endCX, endCY))
                                    gameEvents.add(
                                        0,
                                        "⚡ [LOCALIZADOR] Destino direto atualizado: (X=${String.format("%.1f", finalGridX)}, Y=${String.format("%.1f", finalGridY)})"
                                    )
                                }
                            }
                        }
                ) {
                    val canvasW = size.width
                    val canvasH = size.height

                    // Dynamic scale computation to maintain full fit
                    val baseScale = Math.min(canvasW, canvasH) / 10f
                    val halfTileW = 1.6f * baseScale
                    val halfTileH = 0.8f * baseScale

                    val currentOriginX = canvasW / 2f
                    val currentOriginY = canvasH / 2f + halfTileH

                    // Helper projection lambda inside draw scope
                    val toIso: (Float, Float) -> Offset = { gx, gy ->
                        val relativeX = gx - 5f
                        val relativeY = gy - 5f
                        val screenX = currentOriginX + (relativeX - relativeY) * halfTileW
                        val screenY = currentOriginY + (relativeX + relativeY) * halfTileH
                        Offset(screenX, screenY)
                    }

                     // DRAW GROUND GRID (TATAMI SQUARES)
                     for (gx in 0..10) {
                         for (gy in 0..10) {
                             val isEven = (gx + gy) % 2 == 0
                             val tileColor = if (isEven) Color(0xFF131F35) else Color(0xFF0F1A2D)

                             val pTop = toIso(gx.toFloat(), gy.toFloat())
                             val pRight = toIso(gx + 1f, gy.toFloat())
                             val pBottom = toIso(gx + 1f, gy + 1f)
                             val pLeft = toIso(gx.toFloat(), gy + 1f)

                             val path = Path().apply {
                                 moveTo(pTop.x, pTop.y)
                                 lineTo(pRight.x, pRight.y)
                                 lineTo(pBottom.x, pBottom.y)
                                 lineTo(pLeft.x, pLeft.y)
                                 close()
                             }
                             drawPath(path = path, color = tileColor)
                             
                             // Draw subtle collision warning indicator for blocked tiles
                             if (!isCellWalkable(gx, gy)) {
                                 drawPath(path = path, color = BlueprintRed.copy(alpha = 0.15f))
                                 drawPath(path = path, color = BlueprintRed.copy(alpha = 0.35f), style = Stroke(width = 0.8f))
                             } else {
                                 drawPath(path = path, color = BlueprintGridLine.copy(alpha = 0.3f), style = Stroke(width = 0.5f))
                             }
                         }
                     }

                    // DRAW MAP BOUNDARIES / RETRO MATRIX BOARDERS
                    val gridBoundary = Path().apply {
                        val originWorld = toIso(0f, 0f)
                        val maxRegX = toIso(11f, 0f)
                        val maxRegBoth = toIso(11f, 11f)
                        val maxRegY = toIso(0f, 11f)
                        moveTo(originWorld.x, originWorld.y)
                        lineTo(maxRegX.x, maxRegX.y)
                        lineTo(maxRegBoth.x, maxRegBoth.y)
                        lineTo(maxRegY.x, maxRegY.y)
                        close()
                    }
                    drawPath(gridBoundary, color = BlueprintCyan.copy(alpha = 0.25f), style = Stroke(width = 2f))

                    // DRAW FLOATING PORTAL IN THE CENTER (X=5, Y=5)
                    val portalCenter = toIso(5.5f, 5.5f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(BlueprintCyan, Color.Transparent),
                            center = portalCenter,
                            radius = halfTileW * 1.5f
                        ),
                        center = portalCenter,
                        radius = halfTileW * 1.5f
                    )

                    // Electric column energy beam simulation
                    val beamWidth = halfTileW * 0.7f * beamScale
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, BlueprintCyan.copy(alpha = 0.4f), BlueprintCyan.copy(alpha = 0.8f), BlueprintCyan.copy(alpha = 0.4f), Color.Transparent),
                        ),
                        topLeft = Offset(portalCenter.x - beamWidth / 2f, portalCenter.y - halfTileH * 12),
                        size = Size(beamWidth, halfTileH * 12)
                    )

                    // Draw concentric rings on the portal deck
                    for (i in 1..3) {
                        drawArc(
                            color = BlueprintCyan.copy(alpha = 0.35f * (4 - i) / 3f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(portalCenter.x - halfTileW * 0.5f * i, portalCenter.y - halfTileH * 0.5f * i),
                            size = Size(halfTileW * i, halfTileH * i),
                            style = Stroke(width = 2f)
                        )
                    }

                    // DRAW STATIC LANDMARKS GRAPHICS
                    landmarks.forEach { landmark ->
                        val landCoord = toIso(landmark.gridX.toFloat() + 0.5f, landmark.gridY.toFloat() + 0.5f)

                        // Outer glowing zone
                        drawCircle(
                            color = landmark.accentColor.copy(alpha = 0.2f),
                            center = landCoord,
                            radius = halfTileW * 0.8f
                        )

                        // Accent borders
                        val landmarkHex = Path().apply {
                            val topP = landCoord - Offset(0f, halfTileH * 0.8f)
                            val rightP = landCoord + Offset(halfTileW * 0.8f, 0f)
                            val bottomP = landCoord + Offset(0f, halfTileH * 0.8f)
                            val leftP = landCoord - Offset(halfTileW * 0.8f, 0f)
                            moveTo(topP.x, topP.y)
                            lineTo(rightP.x, rightP.y)
                            lineTo(bottomP.x, bottomP.y)
                            lineTo(leftP.x, leftP.y)
                            close()
                        }
                        drawPath(landmarkHex, color = landmark.accentColor, style = Stroke(width = 1.5f))

                        // Miniature pillars
                        drawLine(
                            color = landmark.accentColor,
                            start = landCoord,
                            end = landCoord - Offset(0f, halfTileH * 1.5f),
                            strokeWidth = 2.5f
                        )
                    }

                    // DRAW LIVE NPCS (Laura, Guilherme, Dragão)
                    npcs.forEach { npc ->
                        val npcCoord = toIso(npc.currentGridX, npc.currentGridY)
                        
                        // Subtle base shadow
                        drawOval(
                            color = Color.Black.copy(alpha = 0.4f),
                            topLeft = npcCoord - Offset(halfTileW * 0.3f, halfTileH * 0.2f),
                            size = Size(halfTileW * 0.6f, halfTileH * 0.4f)
                        )

                        // NPC Circle Avatar torso
                        drawCircle(
                            color = Color(0xFF1E293B),
                            center = npcCoord - Offset(0f, 14f),
                            radius = 11f
                        )
                        // Belt band decoration wrap
                        drawCircle(
                            color = npc.beltColor,
                            center = npcCoord - Offset(0f, 14f),
                            radius = 12f,
                            style = Stroke(width = 2.5f)
                        )

                        // Face circle representation
                        drawCircle(
                            color = Color(0xFFFCD34D),
                            center = npcCoord - Offset(0f, 32f),
                            radius = 8.5f
                        )
                    }

                    // DRAW USER CONTROLLED PLAYER (Dynamic stats & customizable belts)
                    val playerCoord = toIso(playerGridX, playerGridY)

                    // Base shadow
                    drawOval(
                        color = Color.Black.copy(alpha = 0.5f),
                        topLeft = playerCoord - Offset(halfTileW * 0.4f, halfTileH * 0.24f),
                        size = Size(halfTileW * 0.8f, halfTileH * 0.48f)
                    )

                    // Draw dynamic torso based on selected playstyle (Kimono Color)
                    val torsoCenterY = playerCoord.y - 18f - bobOffsetPx
                    val torsoColor = when (memory.favoriteStyle) {
                        "Guarda Fechada" -> Color(0xFF1E3A8A) // Blue Kimono
                        "Passador" -> Color(0xFF15803D) // Green Kimono
                        else -> Color(0xFFFFB03A) // Yellow Kimono
                    }
                    drawCircle(
                        color = torsoColor,
                        center = Offset(playerCoord.x, torsoCenterY),
                        radius = 14f
                    )

                    // Dynamic belt color drawn based on graduation rank
                    val drawBeltColor = when (memory.playerBelt) {
                        "Faixa Branca" -> Color.White
                        "Faixa Azul" -> BlueprintCyan
                        "Faixa Roxa" -> Color(0xFFC084FC)
                        "Faixa Marrom" -> Color(0xFFB45309)
                        "Faixa Preta" -> Color.Black
                        "Faixa Coral" -> BlueprintOrange
                        else -> Color.White
                    }
                    drawCircle(
                        color = drawBeltColor,
                        center = Offset(playerCoord.x, torsoCenterY),
                        radius = 15.5f,
                        style = Stroke(width = 3.5f)
                    )

                    // Head/Face
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        center = Offset(playerCoord.x, playerCoord.y - 40f - bobOffsetPx),
                        radius = 10f
                    )

                    // Active strike visual shockwave particles
                    activeEmoteParticleCoord?.let { coord ->
                        val sparkCoord = toIso(coord.first, coord.second)
                        drawCircle(
                            color = BlueprintCyan.copy(alpha = emoteAlpha),
                            center = sparkCoord,
                            radius = halfTileW * 1.8f * (1f - emoteAlpha),
                            style = Stroke(width = 3f)
                        )
                    }
                }

                // --- CUSTOM ON-SCREEN VIRTUAL JOYSTICK (For Mobile / Touch controls) ---
                val joystickRadiusPx = with(localDensity) { 45.dp.toPx() }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .size(105.dp)
                        .clip(CircleShape)
                        .background(Color(0xE60B132B))
                        .border(1.5.dp, BlueprintCyan.copy(alpha = 0.85f), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    joystickActive = true
                                    currentPath = emptyList() // Joystick cancels target clicks
                                },
                                onDragEnd = {
                                    joystickActive = false
                                    joystickDragOffset = Offset.Zero
                                    movementState = MovementState.IDLE
                                },
                                onDragCancel = {
                                    joystickActive = false
                                    joystickDragOffset = Offset.Zero
                                    movementState = MovementState.IDLE
                                },
                                onDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: Offset ->
                                    change.consume()
                                    val currentDrag = joystickDragOffset + dragAmount
                                    val dragDist = kotlin.math.sqrt(currentDrag.x * currentDrag.x + currentDrag.y * currentDrag.y)
                                    joystickDragOffset = if (dragDist <= joystickRadiusPx) {
                                        currentDrag
                                    } else if (dragDist > 0) {
                                        Offset(
                                            (currentDrag.x / dragDist) * joystickRadiusPx,
                                            (currentDrag.y / dragDist) * joystickRadiusPx
                                        )
                                    } else {
                                        Offset.Zero
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val handleOffsetDpX = with(localDensity) { joystickDragOffset.x.toDp() }
                    val handleOffsetDpY = with(localDensity) { joystickDragOffset.y.toDp() }
                    
                    Box(
                        modifier = Modifier
                            .offset(x = handleOffsetDpX, y = handleOffsetDpY)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(BlueprintCyan, Color(0xFF0369A1))
                                )
                            )
                            .border(1.dp, Color.White, CircleShape)
                            .testTag("joystick_knob")
                    )
                }

                // --- DOUBLE CLICK / CLICK MODE SPEED CONTROL (WALK VS RUN TOGGLE CAPSHIELD) ---
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 130.dp, bottom = 16.dp)
                        .background(Color(0xCC090D16), RoundedCornerShape(20.dp))
                        .border(1.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { isRunningModeEnabled = !isRunningModeEnabled }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("run_mode_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isRunningModeEnabled) "🏃 MODO: CORRER" else "🚶 MODO: CAMINHAR",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunningModeEnabled) BlueprintCyan else BlueprintTextSecondary
                        )
                    }
                }

                // --- CANVAS LANDMARK INTERACTIVE LABELS & OVERLAYS ---
                landmarks.forEach { landmark ->
                    val (xDp, yDp) = getScreenOffsetDp(landmark.gridX.toFloat() + 0.5f, landmark.gridY.toFloat() + 0.5f)

                    Box(
                        modifier = Modifier
                            .offset(x = xDp - 50.dp, y = yDp - 62.dp)
                            .background(Color(0xE60F172A), RoundedCornerShape(4.dp))
                            .border(0.5.dp, landmark.accentColor, RoundedCornerShape(4.dp))
                            .clickable {
                                gameEvents.add(
                                    0,
                                    "⚡ [MIGRAÇÃO] Carregando módulo federado síncrono para ${landmark.name} (Tab index ${landmark.targetTabIdx})"
                                )
                                viewModel.selectTab(landmark.targetTabIdx)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(landmark.iconEmoji, fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = landmark.name,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueprintTextPrimary
                                )
                                Text(
                                    text = "REGISTRAR",
                                    fontSize = 6.sp,
                                    fontWeight = FontWeight.Black,
                                    color = landmark.accentColor
                                )
                            }
                        }
                    }
                }

                // --- NPC FLOATING NAMEPLATES & DIALOG BUBBLES ---
                npcs.forEach { npc ->
                    val isBlocked = blockedPlayersList.contains(npc.name)
                    val isMuted = mutedPlayersList.contains(npc.name)
                    
                    if (!isBlocked) {
                        val (xDp, yDp) = getScreenOffsetDp(npc.currentGridX, npc.currentGridY)
    
                        // Floating nameplate (Always visible)
                        val npcBeltTextColor = if (npc.beltColor == Color.White) Color.Black else Color.White
                        Column(
                            modifier = Modifier
                                .offset(x = xDp - 50.dp, y = yDp - 46.dp)
                                .background(Color(0xD90F172A), RoundedCornerShape(4.dp))
                                .border(0.5.dp, npc.beltColor.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                .width(100.dp)
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isMuted) {
                                        Text("🔇", fontSize = 6.sp)
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = npc.name.uppercase(),
                                        fontSize = 6.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMuted) BlueprintTextSecondary else BlueprintTextPrimary,
                                        maxLines = 1
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(npc.beltColor, RoundedCornerShape(2.dp))
                                        .border(0.5.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                        .padding(horizontal = 2.dp, vertical = 0.5.dp)
                                ) {
                                    Text(
                                        text = npc.belt.uppercase(),
                                        fontSize = 4.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = npcBeltTextColor
                                    )
                                }
                            }
                        }
    
                        // Speech bubble (if npc is talking / has text and not muted)
                        if (npc.baseSpeechBubble.isNotEmpty() && !isMuted) {
                            Box(
                                modifier = Modifier
                                    .offset(x = xDp - 55.dp, y = yDp - 88.dp)
                                    .background(Color.White, RoundedCornerShape(6.dp))
                                    .border(1.dp, npc.beltColor, RoundedCornerShape(6.dp))
                                    .widthIn(max = 110.dp)
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(4.dp).background(npc.beltColor, CircleShape))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(npc.name, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    Text(npc.baseSpeechBubble, fontSize = 7.sp, color = Color(0xFF1E293B), lineHeight = 9.sp)
                                }
                            }
                        }
                    }
                }

                // PLAYER CHAT FLOATING DIALOG (Dynamic bubble)
                val bobOffsetPxDp = with(localDensity) { bobOffsetPx.toDp() }
                activeBubbleText?.let { text ->
                    val (xDp, yDp) = getScreenOffsetDp(playerGridX, playerGridY)

                    Box(
                        modifier = Modifier
                            .offset(x = xDp - 60.dp, y = yDp - 104.dp - bobOffsetPxDp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .border(1.dp, BlueprintCyan, RoundedCornerShape(8.dp))
                            .widthIn(max = 130.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "@${memory.playerName.uppercase().replace(" ", "_")}",
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Black,
                                color = BlueprintCyan
                            )
                            Text(
                                text = text,
                                fontSize = 8.5.sp,
                                color = BlueprintTextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }

                // --- FLOATING NAMEPLATE OVERLAY (NAME, LEVEL, BELT, ACADEMY INDICATORS) ---
                val (nameplateXDp, nameplateYDp) = getScreenOffsetDp(playerGridX, playerGridY)
                val nameplateBeltColor = when (memory.playerBelt) {
                    "Faixa Branca" -> Color.White
                    "Faixa Azul" -> BlueprintCyan
                    "Faixa Roxa" -> Color(0xFFC084FC)
                    "Faixa Marrom" -> Color(0xFFB45309)
                    "Faixa Preta" -> Color.Black
                    "Faixa Coral" -> BlueprintOrange
                    else -> Color.White
                }
                val nameplateBeltTextColor = if (nameplateBeltColor == Color.White) Color.Black else Color.White

                Column(
                    modifier = Modifier
                        .offset(x = nameplateXDp - 60.dp, y = nameplateYDp - 48.dp - bobOffsetPxDp)
                        .background(Color(0xE60B132B), RoundedCornerShape(4.dp))
                        .border(1.dp, nameplateBeltColor, RoundedCornerShape(4.dp))
                        .width(120.dp)
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = memory.playerName.uppercase(),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintTextPrimary,
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .background(BlueprintCyan.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                .padding(horizontal = 3.dp, vertical = 0.5.dp)
                        ) {
                            Text(
                                "Lvl ${memory.playerLevel}",
                                fontSize = 6.sp,
                                color = BlueprintCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(1.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(nameplateBeltColor, RoundedCornerShape(2.dp))
                                .border(0.5.dp, Color.Gray, RoundedCornerShape(2.dp))
                                .padding(horizontal = 3.dp, vertical = 0.5.dp)
                        ) {
                            Text(
                                text = memory.playerBelt.uppercase(),
                                fontSize = 5.sp,
                                fontWeight = FontWeight.Black,
                                color = nameplateBeltTextColor
                            )
                        }
                        
                        Text(
                            text = memory.academyName,
                            fontSize = 6.sp,
                            color = BlueprintTextSecondary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }

                // Dynamic UI Emote effect float text
                activeEmoteParticleText?.let { text ->
                    val coord = activeEmoteParticleCoord ?: Pair(5f, 7f)
                    val (xDp, yDp) = getScreenOffsetDp(coord.first, coord.second)
                    val slideOffset = (18f / (1f - emoteAlpha))
                    val slideOffsetDp = with(localDensity) { slideOffset.toDp() }

                    Text(
                        text = text,
                        color = BlueprintCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .offset(x = xDp - 25.dp, y = yDp - 50.dp - slideOffsetDp)
                            .background(Color.Black.copy(alpha = emoteAlpha), RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // TOP-RIGHT CORNER: Teleport Portal Controller & Map Quick Guide
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(Color(0xCC0B0F19), RoundedCornerShape(6.dp))
                        .border(1.dp, BlueprintGridLine, RoundedCornerShape(6.dp))
                        .padding(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SISTEMA DE CÂMERAS", fontSize = 7.5.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                        Text("Ponto de Fuga: Central", fontSize = 6.5.sp, color = BlueprintTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(BlueprintCard, RoundedCornerShape(3.dp))
                                    .clickable {
                                        // Teleport center
                                        targetGridX = 5f
                                        targetGridY = 7f
                                        gameEvents.add(0, "🔮 [TELETRANSPORTE] Retornando ao spawn central")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Home, null, tint = BlueprintTextPrimary, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // BOTTOM GAME BAR CONTROLS (EMOTES, TELEPORT, MANUAL D-PAD)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                    .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Joystick simulator D-pad
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("MANUAL:", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(BlueprintCard, RoundedCornerShape(4.dp))
                            .clickable {
                                targetGridY = (targetGridY - 1f).coerceIn(0f, 10f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "North", tint = BlueprintCyan, modifier = Modifier.size(14.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(BlueprintCard, RoundedCornerShape(4.dp))
                            .clickable {
                                targetGridX = (targetGridX + 1f).coerceIn(0f, 10f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "East", tint = BlueprintCyan, modifier = Modifier.size(14.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(BlueprintCard, RoundedCornerShape(4.dp))
                            .clickable {
                                targetGridX = (targetGridX - 1f).coerceIn(0f, 10f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "West", tint = BlueprintCyan, modifier = Modifier.size(14.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(BlueprintCard, RoundedCornerShape(4.dp))
                            .clickable {
                                targetGridY = (targetGridY + 1f).coerceIn(0f, 10f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "South", tint = BlueprintCyan, modifier = Modifier.size(14.dp))
                    }
                }

                // Action / Emote buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bubble Chat quick presets
                    PresetSpeechButton("Surgir Aura!", onTrigger = {
                        activeBubbleText = "💥 EXPANÇÃO DE ENERGIA INDÔMITA!"
                        activeEmoteParticleCoord = Pair(playerGridX, playerGridY)
                        activeEmoteParticleText = "⚡ +250 BATTLE-XP! 🥋"
                        gameEvents.add(0, "💪 [EMOTE] ${memory.playerName} soltou sua aura dourada de JiuCoins.")
                    })
                    PresetSpeechButton("Surgir (Spawn)", onTrigger = {
                        val spawnPoints = listOf(
                            Pair(2f, 2f) to "Portal para Academias",
                            Pair(2f, 8f) to "Arena PvP",
                            Pair(8f, 2f) to "Loja Oficial",
                            Pair(8f, 8f) to "Ranking Hall",
                            Pair(5f, 1f) to "Área de Eventos"
                        )
                        val randomPoint = spawnPoints.random()
                        targetGridX = randomPoint.first.first
                        targetGridY = randomPoint.first.second
                        playerGridX = randomPoint.first.first
                        playerGridY = randomPoint.first.second
                        activeBubbleText = "📍 Teleportando para o spawn!"
                        activeEmoteParticleCoord = randomPoint.first
                        activeEmoteParticleText = "🔮 SPARSED! 💥"
                        gameEvents.add(0, "📍 [SPAWN] Sincronizado com sucesso em: ${randomPoint.second} (${randomPoint.first.first}, ${randomPoint.first.second})")
                    })
                    PresetSpeechButton("Ranger (Respawn)", onTrigger = {
                        targetGridX = 5f
                        targetGridY = 7f
                        playerGridX = 5f
                        playerGridY = 7f
                        activeBubbleText = "🏥 RETORNANDO AO CENTRO PLAZA!"
                        activeEmoteParticleCoord = Pair(5f, 7f)
                        activeEmoteParticleText = "✨ RE-CONSTITUÍDO! ✨"
                        gameEvents.add(0, "✨ [RESPAWN] Seu guerreiro foi curado, limpo e re-posicionado no spawn central da cidade.")
                    })
                }
            }
        }

        // ================= RIGHT PORTION: SIDEBAR Telemetry + Minimap + Chat (30% WIDTH) =================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(BlueprintCard)
                .border(startWidth(), BlueprintGridLine)
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Telepathy mini panel
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, null, tint = BlueprintCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CENTRO TELEMETRIC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BlueprintTextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(BlueprintCyan.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "X: ${String.format("%.1f", playerGridX)}  Y: ${String.format("%.1f", playerGridY)}",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Simulated Interactive Proximity VoIP Control Widget
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BoxBorder()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isVoipEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = null,
                                    tint = if (isVoipEnabled) BlueprintGreen else BlueprintRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Voz por Proximidade",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueprintTextPrimary
                                )
                            }
                            Switch(
                                checked = isVoipEnabled,
                                onCheckedChange = {
                                    isVoipEnabled = it
                                    gameEvents.add(
                                        0,
                                        if (it) "🎙️ [VOIP] Fluxo de áudio WebRTC Peer unificado com os avatares da praça!"
                                        else "🎙️ [VOIP] Microfone local mutado."
                                    )
                                },
                                modifier = Modifier.scaleScale(),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BlueprintGreen,
                                    checkedTrackColor = BlueprintGreen.copy(alpha = 0.3f)
                                )
                            )
                        }
                        if (isVoipEnabled) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Volume do Grid:", fontSize = 8.sp, color = BlueprintTextSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Slider(
                                    value = micOutputVolume,
                                    onValueChange = { micOutputVolume = it },
                                    modifier = Modifier.weight(1f).height(12.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = BlueprintCyan,
                                        activeTrackColor = BlueprintCyan
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // TELEPATHY LOGS CONSOLE (LIVE AREA CHAT BOX)
            Card(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BoxBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Chat Control Row (Channel Selection + Moderation Switch)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SISTEMA DE CHAT JIUVERSE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = BlueprintCyan,
                                letterSpacing = 0.5.sp
                            )
                            
                            // Safety Control Button
                            Button(
                                onClick = { isModerationPanelDialogVisible = !isModerationPanelDialogVisible },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isModerationPanelDialogVisible) BlueprintOrange else Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(2.dp),
                                modifier = Modifier.height(13.dp)
                            ) {
                                Text(
                                    text = "🛡️ " + (if (isModerationPanelDialogVisible) "FECHAR MOD." else "SEGURANÇA"),
                                    fontSize = 6.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        // Channel Toggles Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            listOf(
                                "GLOBAL" to "🌍 GLOBAL", 
                                "LOCAL" to "🔊 LOCAL", 
                                "ACADEMIA" to "🥋 ACAD.", 
                                "PRIVADO" to "🔒 PRIV."
                            ).forEach { (chId, chLabel) ->
                                val isActive = chatChannelSelected == chId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isActive) BlueprintCyan.copy(alpha = 0.25f) else Color(0xFF1E293B),
                                            RoundedCornerShape(3.dp)
                                        )
                                        .border(
                                            0.5.dp, 
                                            if (isActive) BlueprintCyan else Color.Transparent, 
                                            RoundedCornerShape(3.dp)
                                        )
                                        .clickable { chatChannelSelected = chId }
                                        .padding(vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = chLabel,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) BlueprintCyan else BlueprintTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Conditional Safety / Moderation Panel (Silenciar & Bloquear Jogador)
                    if (isModerationPanelDialogVisible) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(0.5.dp, BlueprintOrange.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(4.dp)) {
                                Text(
                                    text = "PAINEL DE SEGURANÇA E MODERAÇÃO (ANTI-FLOOD ATIVO)",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BlueprintOrange
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                // Listed users for rapid toggle Actions
                                val listableUsers = listOf("Laura", "Guilherme", "Dragão Negro")
                                listableUsers.forEach { targetUser ->
                                    val isMuted = mutedPlayersList.contains(targetUser)
                                    val isBlocked = blockedPlayersList.contains(targetUser)
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 1.6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "@$targetUser",
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BlueprintTextPrimary
                                        )
                                        
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // Silenciar / Mute Toggle
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (isMuted) BlueprintOrange.copy(alpha = 0.3f) else Color.DarkGray,
                                                        RoundedCornerShape(2.dp)
                                                    )
                                                    .clickable {
                                                        if (isMuted) {
                                                            mutedPlayersList.remove(targetUser)
                                                            gameEvents.add(0, "🔊 [MODERAÇÃO] @$targetUser foi desmutado.")
                                                        } else {
                                                            mutedPlayersList.add(targetUser)
                                                            gameEvents.add(0, "🔇 [MODERAÇÃO] @$targetUser silenciado com sucesso. Mensagens ocultas!")
                                                        }
                                                    }
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isMuted) "🔇 SILENCIADO" else "🔈 SILENCIAR",
                                                    fontSize = 6.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isMuted) BlueprintOrange else Color.LightGray
                                                )
                                            }
                                            
                                            // Bloquear / Block Toggle
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (isBlocked) Color.Red.copy(alpha = 0.3f) else Color.DarkGray,
                                                        RoundedCornerShape(2.dp)
                                                    )
                                                    .clickable {
                                                        if (isBlocked) {
                                                            blockedPlayersList.remove(targetUser)
                                                            gameEvents.add(0, "🔔 [MODERAÇÃO] @$targetUser foi desbloqueado.")
                                                        } else {
                                                            blockedPlayersList.add(targetUser)
                                                            gameEvents.add(0, "🚫 [MODERAÇÃO] @$targetUser BLOQUEADO! Avatar ocultado da praça.")
                                                        }
                                                    }
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isBlocked) "🚫 BLOQUEADO" else "⚔️ BLOQUEAR",
                                                    fontSize = 6.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isBlocked) Color.Red else Color.LightGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // WHISPER RECIPIENT SELECTOR
                    if (chatChannelSelected == "PRIVADO") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(3.dp))
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Para:", fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            listOf("Laura", "Guilherme", "Dragão Negro").forEach { target ->
                                val isSelected = whisperRecipientByName == target
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) Color(0xFFEC4899).copy(alpha = 0.25f) else Color.Transparent,
                                            RoundedCornerShape(2.dp)
                                        )
                                        .border(
                                            0.5.dp,
                                            if (isSelected) Color(0xFFEC4899) else Color.Gray.copy(alpha = 0.4f),
                                            RoundedCornerShape(2.dp)
                                        )
                                        .clickable { whisperRecipientByName = target }
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "@$target",
                                        fontSize = 6.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFEC4899) else Color.LightGray
                                    )
                                }
                            }
                        }
                    }

                    // Dynamic search filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔍 Filtrar:",
                            fontSize = 7.sp,
                            color = BlueprintTextSecondary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        BasicTextField(
                            value = chatFilterTextKeyword,
                            onValueChange = { chatFilterTextKeyword = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1E293B), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                        if (chatFilterTextKeyword.isNotEmpty()) {
                            Text(
                                text = "Limpar",
                                fontSize = 7.sp,
                                color = BlueprintCyan,
                                modifier = Modifier
                                    .padding(start = 3.dp)
                                    .clickable { chatFilterTextKeyword = "" }
                            )
                        }
                    }

                    // Text Scroller / Log Stream
                    Box(modifier = Modifier.weight(1f).padding(top = 4.dp)) {
                        val filteredEvents = gameEvents.filter {
                            chatFilterTextKeyword.isEmpty() || it.contains(chatFilterTextKeyword, ignoreCase = true)
                        }
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredEvents) { msg ->
                                // Determine the appropriate accent color index for readability
                                val textColor = when {
                                    msg.startsWith("🔊 [LOCAL]") -> BlueprintTeal
                                    msg.startsWith("🌍 [GLOBAL]") -> BlueprintCyan
                                    msg.startsWith("🥋 [ACADEMIA]") -> Color(0xFFC084FC)
                                    msg.startsWith("🔒 [PRIVADO]") || msg.contains("Sussurrando") || msg.contains("sussurra") || msg.contains("responde") || msg.contains("sussuro") -> Color(0xFFEC4899)
                                    msg.startsWith("⚠️ [MODERAÇÃO]") || msg.startsWith("🔇 [MODERAÇÃO]") || msg.startsWith("🚫 [MODERAÇÃO]") -> BlueprintOrange
                                    msg.contains("SISTEMA") || msg.contains("SENSEI") -> BlueprintTeal
                                    else -> BlueprintTextSecondary
                                }
                                
                                Text(
                                    text = msg,
                                    fontSize = 8.5.sp,
                                    color = textColor,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }

                    // Send local speech input field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .background(BlueprintCard, RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = textMessageInput,
                            onValueChange = { textMessageInput = it },
                            textStyle = TextStyle(
                                color = BlueprintTextPrimary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    val trimmedMsg = textMessageInput.trim()
                                    if (trimmedMsg.isNotEmpty()) {
                                        val now = System.currentTimeMillis()
                                        // Anti-spam warning (rate limiter 1.5s)
                                        if (now - lastChatMessageSentTimestamp < 1500L) {
                                            gameEvents.add(0, "⚠️ [MODERAÇÃO] Anti-Flood: Aguarde 1.5s antes de enviar outra mensagem!")
                                        } else {
                                            lastChatMessageSentTimestamp = now
                                            
                                            // Process based on selected channel
                                            when (chatChannelSelected) {
                                                "LOCAL" -> {
                                                    activeBubbleText = trimmedMsg
                                                    val finalMsg = "🔊 [LOCAL] @${memory.playerName}: \"$trimmedMsg\""
                                                    gameEvents.add(0, finalMsg)
                                                    
                                                    // Trigger Proximity Chat replies from nearby characters
                                                    npcs.forEach { npc ->
                                                        val isBlocked = blockedPlayersList.contains(npc.name)
                                                        val isMuted = mutedPlayersList.contains(npc.name)
                                                        if (!isBlocked && !isMuted) {
                                                            val xDiff = playerGridX - npc.currentGridX
                                                            val yDiff = playerGridY - npc.currentGridY
                                                            val distance = kotlin.math.sqrt(xDiff * xDiff + yDiff * yDiff)
                                                            if (distance <= 3.5f) {
                                                                // Dynamic nearby reaction
                                                                val reactions = listOf(
                                                                    "Tô por aqui conversando!",
                                                                    "Oss! Ouvi seu papo aqui por perto.",
                                                                    "Boa @${memory.playerName}! Vamos treinar no tatame?",
                                                                    "Entendi tudo perfeitamente!"
                                                                )
                                                                npc.baseSpeechBubble = reactions.random()
                                                                gameEvents.add(
                                                                    0,
                                                                    "🔊 [LOCAL] @${npc.name}: \"${npc.baseSpeechBubble}\" (A ${String.format("%.1f", distance)} unidades de distância)"
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                "GLOBAL" -> {
                                                    activeBubbleText = trimmedMsg
                                                    gameEvents.add(0, "🌍 [GLOBAL] @${memory.playerName}: \"$trimmedMsg\"")
                                                }
                                                "ACADEMIA" -> {
                                                    activeBubbleText = trimmedMsg
                                                    gameEvents.add(0, "🥋 [ACADEMIA] @${memory.playerName}: \"$trimmedMsg\"")
                                                }
                                                "PRIVADO" -> {
                                                    // Sussurro / Private Message
                                                    gameEvents.add(0, "🔒 [PRIVADO] Sussurrando para @$whisperRecipientByName: \"$trimmedMsg\"")
                                                    
                                                    // Simulated recipient response to private whisper
                                                    val isBlocked = blockedPlayersList.contains(whisperRecipientByName)
                                                    val isMuted = mutedPlayersList.contains(whisperRecipientByName)
                                                    if (!isBlocked && !isMuted) {
                                                        gameEvents.add(
                                                            0,
                                                            "🔒 [PRIVADO] @$whisperRecipientByName sussurra de volta: \"Foco no foco! Oss! Recebi seu sussurro privado!\""
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        textMessageInput = ""
                                    }
                                }
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send text",
                            tint = BlueprintCyan,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    val trimmedMsg = textMessageInput.trim()
                                    if (trimmedMsg.isNotEmpty()) {
                                        val now = System.currentTimeMillis()
                                        // Anti-spam warning (rate limiter 1.5s)
                                        if (now - lastChatMessageSentTimestamp < 1500L) {
                                            gameEvents.add(0, "⚠️ [MODERAÇÃO] Anti-Flood: Aguarde 1.5s antes de enviar outra mensagem!")
                                        } else {
                                            lastChatMessageSentTimestamp = now
                                            
                                            // Process based on selected channel
                                            when (chatChannelSelected) {
                                                "LOCAL" -> {
                                                    activeBubbleText = trimmedMsg
                                                    val finalMsg = "🔊 [LOCAL] @${memory.playerName}: \"$trimmedMsg\""
                                                    gameEvents.add(0, finalMsg)
                                                    
                                                    // Trigger Proximity Chat replies from nearby characters
                                                    npcs.forEach { npc ->
                                                        val isBlocked = blockedPlayersList.contains(npc.name)
                                                        val isMuted = mutedPlayersList.contains(npc.name)
                                                        if (!isBlocked && !isMuted) {
                                                            val xDiff = playerGridX - npc.currentGridX
                                                            val yDiff = playerGridY - npc.currentGridY
                                                            val distance = kotlin.math.sqrt(xDiff * xDiff + yDiff * yDiff)
                                                            if (distance <= 3.5f) {
                                                                // Dynamic nearby reaction
                                                                val reactions = listOf(
                                                                    "Tô por aqui conversando!",
                                                                    "Oss! Ouvi seu papo aqui por perto.",
                                                                    "Boa @${memory.playerName}! Vamos treinar no tatame?",
                                                                    "Entendi tudo perfeitamente!"
                                                                )
                                                                npc.baseSpeechBubble = reactions.random()
                                                                gameEvents.add(
                                                                    0,
                                                                    "🔊 [LOCAL] @${npc.name}: \"${npc.baseSpeechBubble}\" (A ${String.format("%.1f", distance)} unidades de distância)"
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                "GLOBAL" -> {
                                                    activeBubbleText = trimmedMsg
                                                    gameEvents.add(0, "🌍 [GLOBAL] @${memory.playerName}: \"$trimmedMsg\"")
                                                }
                                                "ACADEMIA" -> {
                                                    activeBubbleText = trimmedMsg
                                                    gameEvents.add(0, "🥋 [ACADEMIA] @${memory.playerName}: \"$trimmedMsg\"")
                                                }
                                                "PRIVADO" -> {
                                                    // Sussurro / Private Message
                                                    gameEvents.add(0, "🔒 [PRIVADO] Sussurrando para @$whisperRecipientByName: \"$trimmedMsg\"")
                                                    
                                                    // Simulated recipient response to private whisper
                                                    val isBlocked = blockedPlayersList.contains(whisperRecipientByName)
                                                    val isMuted = mutedPlayersList.contains(whisperRecipientByName)
                                                    if (!isBlocked && !isMuted) {
                                                        gameEvents.add(
                                                            0,
                                                            "🔒 [PRIVADO] @$whisperRecipientByName sussurra de volta: \"Foco no foco! Oss! Recebi seu sussurro privado!\""
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        textMessageInput = ""
                                    }
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ================= PRESENÇA ONLINE EM TEMPO REAL (Socket.IO) =================
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BoxBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                ) {
                    // Socket.IO Status Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "JOGADORES ONLINE (Socket.IO)",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = BlueprintTextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(if (socketConnectionStatus == "Conectado") BlueprintGreen else BlueprintOrange, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Socket.IO • ${socketConnectionPing}ms (${socketConnectionStatus.uppercase()})",
                                    fontSize = 6.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (socketConnectionStatus == "Conectado") BlueprintGreen else BlueprintOrange
                                )
                            }
                        }
                        
                        // Toggle Simulation Button
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isPresenceSimulationActive) BlueprintTeal.copy(alpha = 0.2f) else Color.DarkGray,
                                    RoundedCornerShape(2.dp)
                                )
                                .border(0.5.dp, if (isPresenceSimulationActive) BlueprintTeal else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                .clickable {
                                    isPresenceSimulationActive = !isPresenceSimulationActive
                                    gameEvents.add(
                                        0,
                                        if (isPresenceSimulationActive) "📶 [SOCKET.IO] Rede ativa: Iniciando escuta de presença em tempo real (Port 9091)..."
                                        else "📶 [SOCKET.IO] Rede pausada: Clientes congelados sob escuta offline."
                                    )
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isPresenceSimulationActive) "📶 SIM ATIVO" else "⏹️ PARADO",
                                fontSize = 6.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isPresenceSimulationActive) BlueprintTeal else Color.LightGray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(npcs) { buddy ->
                            val isExpanded = expandedPlayerName == buddy.name
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BlueprintCard, RoundedCornerShape(4.dp))
                                    .border(
                                        0.5.dp, 
                                        if (isExpanded) BlueprintCyan.copy(alpha = 0.6f) else Color.Transparent, 
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        expandedPlayerName = if (isExpanded) null else buddy.name
                                    }
                                    .padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Stylized Belt Circular Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(buddy.beltColor, CircleShape)
                                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (buddy.name.isNotEmpty()) buddy.name.take(1).uppercase() else "🥋",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (buddy.beltColor == Color.Black) Color.White else Color.Black
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = buddy.name,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BlueprintTextPrimary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFF1E293B), RoundedCornerShape(2.dp))
                                                        .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                                ) {
                                                    Text(
                                                        text = "NV ${buddy.level}",
                                                        fontSize = 6.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BlueprintCyan
                                                    )
                                                }
                                                if (buddy.isFriend) {
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("⭐", fontSize = 6.sp)
                                                }
                                            }
                                            Text(
                                                text = buddy.handle,
                                                fontSize = 7.sp,
                                                color = BlueprintTextSecondary
                                            )
                                        }
                                    }

                                    // Online Presence Indicators using Socket.IO Status
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(if (buddy.isOnline) BlueprintGreen else Color.Gray, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (buddy.isOnline) buddy.statusText.uppercase() else "OFFLINE",
                                            fontSize = 6.5.sp,
                                            color = if (buddy.isOnline) BlueprintGreen else Color.LightGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Interactive Actions Deck
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                            .background(Color(0xFF0F172A).copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                            .padding(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            // 1. ADICIONAR AMIGO
                                            Button(
                                                onClick = {
                                                    val index = npcs.indexOf(buddy)
                                                    if (index != -1) {
                                                        npcs[index] = buddy.copy(isFriend = !buddy.isFriend)
                                                        gameEvents.add(
                                                            0,
                                                            if (!buddy.isFriend) "🤝 [SOCKET.IO] Evento 'friend_request' emitido: { target: \"${buddy.handle}\", isFriend: true } — Vínculo de amizade ativado!"
                                                            else "🤝 [SOCKET.IO] Amizade desfeita com ${buddy.name}."
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).height(18.dp),
                                                contentPadding = PaddingValues(horizontal = 2.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (buddy.isFriend) BlueprintOrange.copy(alpha = 0.4f) else Color(0xFF1E293B)
                                                ),
                                                shape = RoundedCornerShape(2.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person, 
                                                        contentDescription = null, 
                                                        tint = if (buddy.isFriend) BlueprintOrange else Color.LightGray,
                                                        modifier = Modifier.size(8.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = if (buddy.isFriend) "AMIGO ✓" else "ADD AMIGO",
                                                        fontSize = 6.5.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // 2. ENVIAR MENSAGEM
                                            Button(
                                                onClick = {
                                                    chatChannelSelected = "PRIVADO"
                                                    whisperRecipientByName = buddy.name
                                                    gameEvents.add(
                                                        0,
                                                        "💬 [SOCKET.IO] Canal privado Socket sincronizado com ${buddy.handle}. Digite sua mensagem em sussurro!"
                                                    )
                                                },
                                                modifier = Modifier.weight(1f).height(18.dp),
                                                contentPadding = PaddingValues(horizontal = 2.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                                shape = RoundedCornerShape(2.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Send, 
                                                        contentDescription = null, 
                                                        tint = BlueprintCyan,
                                                        modifier = Modifier.size(8.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "COCHICHAR",
                                                        fontSize = 6.5.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            // 3. VISITAR ACADEMIA
                                            Button(
                                                onClick = {
                                                    if (buddy.isOnline) {
                                                        // Teleport near the buddy
                                                        targetGridX = buddy.currentGridX + 0.5f
                                                        targetGridY = buddy.currentGridY - 0.5f
                                                        activeEmoteParticleCoord = Pair(buddy.currentGridX, buddy.currentGridY)
                                                        activeEmoteParticleText = "⚡ VIAJANDO..."
                                                        
                                                        gameEvents.add(
                                                            0,
                                                            "🥋 [SOCKET.IO] Teleportando ao Dojo filiado de ${buddy.handle}... Coordenadas de tatame sincronizadas!"
                                                        )
                                                    } else {
                                                        gameEvents.add(0, "⚠️ [SOCKET.IO] Jogador offline: Não é possível visitar a academia agora.")
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).height(18.dp),
                                                contentPadding = PaddingValues(horizontal = 2.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                                shape = RoundedCornerShape(2.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Home, 
                                                        contentDescription = null, 
                                                        tint = BlueprintTeal,
                                                        modifier = Modifier.size(8.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "VISITAR COCH",
                                                        fontSize = 6.5.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // 4. DESAFIAR JOGADOR
                                            Button(
                                                onClick = {
                                                    if (buddy.isOnline) {
                                                        gameEvents.add(0, "⚔️ [SOCKET.IO] Desafio em tempo real emitido: socket.emit(\"pvp_challenge\", { opponent: \"${buddy.name}\" })")
                                                        val index = npcs.indexOf(buddy)
                                                        if (index != -1) {
                                                            npcs[index] = buddy.copy(statusText = "🥊 Desafiado")
                                                        }
                                                        
                                                        // Auto trigger aceitação response from server in 1.2s
                                                        gameEvents.add(0, "🥊 [SOCKET.IO] ${buddy.name} está analisando os termos de combate virtual...")
                                                    } else {
                                                        gameEvents.add(0, "⚠️ [SOCKET.IO] Jogador offline: Impossível desafiar para combate!")
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).height(18.dp),
                                                contentPadding = PaddingValues(horizontal = 2.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                                shape = RoundedCornerShape(2.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow, 
                                                        contentDescription = null, 
                                                        tint = BlueprintRed,
                                                        modifier = Modifier.size(8.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "DESAFIAR",
                                                        fontSize = 6.5.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleResourceBadge(emoji: String, amount: String, label: String, tint: Color) {
    Box(
        modifier = Modifier
            .background(BlueprintCard, RoundedCornerShape(4.dp))
            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 11.sp)
            Column {
                Text(amount, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tint, fontFamily = FontFamily.Monospace)
                Text(label, fontSize = 6.sp, color = BlueprintTextSecondary)
            }
        }
    }
}

@Composable
fun PresetSpeechButton(label: String, onTrigger: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
            .border(0.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable { onTrigger() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = BlueprintCyan, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
    }
}

// Helpers for visual safety & consistency
@Composable
fun BoxBorder() = androidx.compose.foundation.BorderStroke(0.5.dp, BlueprintGridLine)

@Composable
fun startWidth() = 1.dp

@Composable
fun Modifier.scaleScale(): Modifier = this.size(width = 24.dp, height = 14.dp)
