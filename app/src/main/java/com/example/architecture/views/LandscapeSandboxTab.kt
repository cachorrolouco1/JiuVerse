package com.example.architecture.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2
import kotlin.math.sqrt

// 1. Data model for virtual world objects
data class VirtualMmorpgPlayer(
    val id: String,
    val name: String,
    var x: Float, // grid percentage 0..100
    var y: Float, // grid percentage 0..100
    val color: Color,
    val ringColor: Color,
    val spriteIcon: String,
    val level: Int,
    var activeSpeech: String = "",
    var isNpc: Boolean = false,
    val description: String = ""
)

data class InventoryShortcutSlot(
    val id: String,
    val name: String,
    val icon: String,
    val quantity: Int
)

data class MmorpgQuest(
    val id: String,
    val description: String,
    var progress: String,
    var isDone: Boolean = false
)

@Composable
fun LandscapeSandboxTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Interactive States for Virtual World Simulation
    var playerX by remember { mutableStateOf(45f) }
    var playerY by remember { mutableStateOf(50f) }
    var activeSpeechLabel by remember { mutableStateOf("OSS! Preparado para o dojo síncrono!") }
    var showSpeechTimer by remember { mutableStateOf(8) }

    // Proximity voice state
    var voiceChatActive by remember { mutableStateOf(false) }
    // Interactive action feed
    var recentDojoConsoleLog by remember { mutableStateOf("Mundo virtual carregou. 60 FPS síncronos assegurados.") }
    
    // Inventory pool
    val inventoryItems = remember {
        mutableStateListOf(
            InventoryShortcutSlot("i1", "Faixa Preta Holográfica", "🥋", 1),
            InventoryShortcutSlot("i2", "Quimono Pixélado Lendário", "🥋", 1),
            InventoryShortcutSlot("i3", "Suco de Açaí Energético", "🥤", 12),
            InventoryShortcutSlot("i4", "Chave do Baú Cósmico", "🔑", 3),
            InventoryShortcutSlot("i5", "Pergaminho de Raspagem", "📜", 1)
        )
    }

    // Active quests
    val activeQuests = remember {
        mutableStateListOf(
            MmorpgQuest("q1", "Ganhe 3 Sparrings Síncronos", "1/3", false),
            MmorpgQuest("q2", "Aprimore o Doble-under na Academia", "0/1", false),
            MmorpgQuest("q3", "Interaja com Mestre Helio", "Pronto para falar", false)
        )
    }

    // Interactive speech bubble clearing simulator
    LaunchedEffect(activeSpeechLabel) {
        if (activeSpeechLabel.isNotEmpty()) {
            kotlinx.coroutines.delay(4000)
            activeSpeechLabel = ""
        }
    }

    // Simulated network of online players / NPCs
    val virtualEntities = remember {
        mutableStateListOf(
            VirtualMmorpgPlayer("npc1", "Mestre Helio Gracie", 30f, 25f, BlueprintOrange, Color.Yellow, "👴", 99, "Busque a alavanca perfeita.", true, "Grão-Mestre Fundador"),
            VirtualMmorpgPlayer("npc2", "Juiz de Chaves", 70f, 30f, BlueprintCyan, Color.White, "👔", 50, "Mantenham a postura limpa!", true, "Árbitros do Tatame"),
            VirtualMmorpgPlayer("p1", "NoGi_King_XP", 25f, 65f, BlueprintTeal, Color.Green, "🥋", 15, "Estou comprando áreas do sandbox!"),
            VirtualMmorpgPlayer("p2", "Guard_Preta_9", 80f, 75f, Color(0xFFC084FC), Color.Magenta, "🧑‍🎤", 32, "Alguém do Atos Dojo online pro 1v1?"),
            VirtualMmorpgPlayer("p3", "Kimura_Master", 55f, 78f, Color(0xFFF472B6), Color.Red, "🥷", 24, "A chave do torneio fecha em 5min!")
        )
    }

    // Virtual Joystick dragging physics emulator
    var joystickOffsetX by remember { mutableStateOf(0f) }
    var joystickOffsetY by remember { mutableStateOf(0f) }
    val maxJoystickDragRadius = 45f

    // Background infinite pulse for radar/sound effect simulation
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseProgression by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse"
    )

    // Joystick execution handler to shift player coord
    LaunchedEffect(joystickOffsetX, joystickOffsetY) {
        while (true) {
            if (joystickOffsetX != 0f || joystickOffsetY != 0f) {
                // Calculate move delta
                val deltaX = (joystickOffsetX / maxJoystickDragRadius) * 1.8f
                val deltaY = (joystickOffsetY / maxJoystickDragRadius) * 1.8f
                
                playerX = (playerX + deltaX).coerceIn(5f, 92f)
                playerY = (playerY + deltaY).coerceIn(5f, 92f)
            }
            kotlinx.coroutines.delay(30) // game loop speed approx 30ms ticks
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Arquitetura & Engenharia do Core de Jogo Horizontal (Landscape MMORPG)",
            subtitle = "Lockout total de rotação portrait, simulação de HUD 80/20 síncrona inspirada em clássicos isométricos e controles móveis estáveis."
        )

        // General warning / mandate statement block
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(1.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ScreenRotation,
                contentDescription = null,
                tint = BlueprintCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "DIRETRIZ DE ARQUITETURA MOBILE LANDSCAPE-ONLY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "A jogabilidade de MMORPG com grid social de alta densidade exige escaneamento visual horizontal amplo. Ao eliminar o fluxo de rotação e ignorar Portraits, o pipeline de renderização foca em resoluções com aspecto 16:9, diminuindo a sobrecarga física das Views e evitando falhas estruturais em insets de teclado do chat síncrono.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // =========================================================================
        // SECTION A: THE HORIZONTAL MMORPG PLAYGROUND COCKPIT CABINET (VIRTUAL SIMULATOR)
        // =========================================================================
        Text(
            text = "I. COCKPIT INTERATIVO: MUNDO VIRTUAL JIUVERSE HORIZONTAL (SIMULATOR)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan
        )
        Text(
            text = "Arraste o Joystick virtual no canto esquerdo ou acione as teclas rápidas e emojis abaixo para ver a reatividade do HUD 80/20.",
            fontSize = 10.sp,
            color = BlueprintTextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // The viewport Card simulating the phone / tablet horizontal screen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(Color.Black)
                .border(2.dp, BlueprintCyan, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                
                // -------------------------------------------------------------
                // LEFT COLUMN HUD (20% of area - Mini map & Profile - Habbo/Ragnarok vibe)
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier
                        .weight(0.2f)
                        .fillMaxHeight()
                        .background(Color(0xFF0D1527))
                        .border(1.dp, BlueprintGridLine)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mini Map Box
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📍 MINI MAPA", fontSize = 8.sp, fontWeight = FontWeight.Black, color = BlueprintCyan)
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color.Black)
                                .border(1.dp, BlueprintTeal, RoundedCornerShape(4.dp))
                        ) {
                            // Draw dots simulating entities on mini map
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw player dot
                                drawCircle(
                                    color = BlueprintCyan,
                                    radius = 3.dp.toPx(),
                                    center = Offset(
                                        x = size.width * (playerX / 100f),
                                        y = size.height * (playerY / 100f)
                                    )
                                )
                                // Draw other players/NPC radar dots
                                drawCircle(
                                    color = BlueprintOrange,
                                    radius = 2.dp.toPx(),
                                    center = Offset(size.width * 0.3f, size.height * 0.25f)
                                )
                                drawCircle(
                                    color = Color.Green,
                                    radius = 2.dp.toPx(),
                                    center = Offset(size.width * 0.25f, size.height * 0.65f)
                                )
                                drawCircle(
                                    color = Color.Magenta,
                                    radius = 2.dp.toPx(),
                                    center = Offset(size.width * 0.8f, size.height * 0.75f)
                                )
                            }
                        }
                    }

                    // Player level / HP bars (Ragnarok style)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("ATLETA BRASIL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                        Text("Rank Elite • Lvl 42", fontSize = 7.5.sp, color = BlueprintTextSecondary)

                        // HP Bar
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("HP", fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                Text("850/850", fontSize = 6.5.sp, color = BlueprintTextPrimary)
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.DarkGray).clip(RoundedCornerShape(1f))) {
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(1f).background(Color.Red))
                            }
                        }

                        // Stamina Bar
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("STM", fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                                Text("220/300", fontSize = 6.5.sp, color = BlueprintTextPrimary)
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.DarkGray).clip(RoundedCornerShape(1f))) {
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.73f).background(BlueprintTeal))
                            }
                        }
                    }

                    // Latency / Battery Status info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("60 FPS", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(4.dp).background(BlueprintTeal, CircleShape))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("18ms", fontSize = 7.sp, color = BlueprintTextSecondary)
                        }
                    }
                }

                // -------------------------------------------------------------
                // CENTER VOLUME (80% world game view, with absolute joystick overlay, virtual avatars)
                // -------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .background(Color(0xFF0F172A))
                        .border(1.dp, BlueprintGridLine)
                ) {
                    
                    // Decorative Canvas drawing virtual floor tiling or grid
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridGap = 28.dp.toPx()
                        // Horizontal grid lines
                        for (i in 0..(size.height / gridGap).toInt()) {
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(0f, i * gridGap),
                                end = Offset(size.width, i * gridGap),
                                strokeWidth = 1f
                            )
                        }
                        // Vertical grid lines
                        for (i in 0..(size.width / gridGap).toInt()) {
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(i * gridGap, 0f),
                                end = Offset(i * gridGap, size.height),
                                strokeWidth = 1f
                            )
                        }
                    }

                    // Virtual World Title Watermark
                    Text(
                        text = "JIUVERSE SANDBOX CANOPY MAP NODE: RJ-CENTRO_01",
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
                    )

                    // Draw our Player on the grid
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = ((playerX / 100f) * 230).coerceIn(10f, 220f).dp.roundToPx(),
                                    y = ((playerY / 100f) * 210).coerceIn(10f, 200f).dp.roundToPx()
                                )
                            }
                            .size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Floating speech bubble if active
                            if (activeSpeechLabel.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .border(0.5.dp, BlueprintCyan, RoundedCornerShape(4.dp))
                                ) {
                                    Text(
                                        text = activeSpeechLabel,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        lineHeight = 8.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            // Microphone active indicator
                            if (voiceChatActive) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = BlueprintTeal,
                                    modifier = Modifier.size(10.dp)
                                )
                            }

                            // Avatar icon & title
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(BlueprintCyan.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, BlueprintCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🥋", fontSize = 11.sp)
                            }
                            Text("VOCÊ (Lvl 42)", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                        }
                    }

                    // Draw other players and NPCs in Sandbox
                    virtualEntities.forEach { entity ->
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = ((entity.x / 100f) * 230).dp.roundToPx(),
                                        y = ((entity.y / 100f) * 210).dp.roundToPx()
                                    )
                                }
                                .size(34.dp)
                                .clickable {
                                    recentDojoConsoleLog = "Focou em @${entity.name}. Atleta do dojo federado!"
                                    activeSpeechLabel = "E aí, @${entity.name}! Quer fechar um sparring síncrono?"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Speech labels for others
                                Box(
                                    modifier = Modifier
                                        .background(if (entity.isNpc) BlueprintOrange.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        entity.activeSpeech,
                                        fontSize = 6.5.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))

                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(entity.color.copy(alpha = 0.2f), CircleShape)
                                        .border(1.dp, entity.ringColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(entity.spriteIcon, fontSize = 9.sp)
                                }
                                Text("@${entity.name}", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // OVERLAY CONTROLS: VIRTUAL ANALOG JOYSTICK (BOTTOM LEFT OVERLAY)
                    // ---------------------------------------------------------
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .size(90.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, BlueprintCyan.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(joystickOffsetX.toInt(), joystickOffsetY.toInt())
                                }
                                .size(36.dp)
                                .background(BlueprintCyan, CircleShape)
                                .border(1.5.dp, Color.White, CircleShape)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            joystickOffsetX = 0f
                                            joystickOffsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            // Handle drag coordinates inside boundary radius
                                            val nextX = joystickOffsetX + dragAmount.x
                                            val nextY = joystickOffsetY + dragAmount.y
                                            val dist = sqrt((nextX * nextX) + (nextY * nextY))
                                            if (dist <= maxJoystickDragRadius) {
                                                joystickOffsetX = nextX
                                                joystickOffsetY = nextY
                                            } else {
                                                val angle = atan2(nextY, nextX)
                                                joystickOffsetX = cos(angle) * maxJoystickDragRadius
                                                joystickOffsetY = sin(angle) * maxJoystickDragRadius
                                            }
                                        }
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp).align(Alignment.Center)
                            )
                        }
                    }

                    // ---------------------------------------------------------
                    // OVERLAY CONTROLS: ACTION BUTTONS DECK (BOTTOM RIGHT OVERLAY)
                    // ---------------------------------------------------------
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // VOICE CONTROLLER BUTTON
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (voiceChatActive) BlueprintTeal else Color.Black.copy(alpha = 0.7f), CircleShape)
                                    .border(1.dp, if (voiceChatActive) Color.White else BlueprintTeal, CircleShape)
                                    .clickable {
                                        voiceChatActive = !voiceChatActive
                                        recentDojoConsoleLog = "Voz de proximidade síncrona: ${if (voiceChatActive) "ATIVADO (Transmissor Aberto)" else "MUTADO"}"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (voiceChatActive) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = "Voice",
                                    tint = if (voiceChatActive) Color.Black else BlueprintTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // DOJO INTERACTION INTERACT KEY
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                    .border(1.dp, BlueprintCyan, CircleShape)
                                    .clickable {
                                        // Find nearest entity
                                        recentDojoConsoleLog = "Interagindo com Mestre Helio Gracie: 'Busque a alavanca de cotovelo ideal no tatame estelar.'"
                                        activeSpeechLabel = "OSS! Obrigado pelos conselhos, Grão-Mestre!"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "Interact",
                                    tint = BlueprintCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // CHALLENGE DUELS KEY
                            Box(
                                modifier = Modifier
                                    .background(BlueprintOrange, RoundedCornerShape(8.dp))
                                    .clickable {
                                        recentDojoConsoleLog = "Desafio instantâneo disparado nas proximidades! Quem aceita?"
                                        activeSpeechLabel = "🥊 DESAFIO ABERTO NO MEIO DO GRID!"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("DESAFIO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }

                            // ACADEMY BUTTON
                            Box(
                                modifier = Modifier
                                    .background(BlueprintTeal, RoundedCornerShape(8.dp))
                                    .clickable {
                                        recentDojoConsoleLog = "Interface de Guilda/Dojo carregada no HUD."
                                        activeSpeechLabel = "🥋 Orgulho da Gracie Barra Angra!"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("ACADEMIA", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // RIGHT COLUMN HUD (20% of area - Social Chat, Friends, Active Quests)
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier
                        .weight(0.2f)
                        .fillMaxHeight()
                        .background(Color(0xFF0D1527))
                        .border(1.dp, BlueprintGridLine)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Chat Box Feed
                    Column {
                        Text("💬 SOCIAL CHAT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                        Spacer(modifier = Modifier.height(2.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                .padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("<NoGi_King>: Alguém pra duelar?", fontSize = 7.sp, color = BlueprintTextSecondary)
                            Text("<GuardPreta>: OSS!", fontSize = 7.sp, color = BlueprintTeal)
                            Text("<Você>: Ativando voz síncrona", fontSize = 7.sp, color = BlueprintCyan)
                            Text("<Helio_Gracie>: Estudem a base.", fontSize = 7.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Friends list
                    Column {
                        Text("👥 AMIGOS DO DOJO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                        Spacer(modifier = Modifier.height(2.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf(
                                "Miyao_Bros" to "Online Hub 2",
                                "Marcelo_Guardeiro" to "Em Luta GP"
                            ).forEach { (fName, fStatus) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(fName, fontSize = 7.sp, color = BlueprintTextPrimary)
                                    Text(fStatus, fontSize = 6.sp, color = BlueprintTeal)
                                }
                            }
                        }
                    }

                    // Active Quests Tracker
                    Column {
                        Text("📜 MISSÕES ATIVAS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                        Spacer(modifier = Modifier.height(2.dp))
                        activeQuests.forEach { quest ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(3.dp).background(BlueprintOrange, CircleShape))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${quest.description.take(18)}... (${quest.progress})",
                                    fontSize = 6.5.sp,
                                    color = BlueprintTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Dashboard section: Row of Shortcuts & Emotes control blocks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Emote action shortcuts card row (Left side Bottom)
            Card(
                modifier = Modifier.weight(1.1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("ATALHOS EMBALADOS DE INVENTÁRIO (HOT BAR SLOTS)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        inventoryItems.forEach { slot ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable {
                                        recentDojoConsoleLog = "Consumiu item de inventário '${slot.name}'! Multiplicador ativo."
                                        activeSpeechLabel = "Usou ${slot.icon}!"
                                    }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(slot.icon, fontSize = 14.sp)
                                    Text("x${slot.quantity}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Quick expression Emote triggers (Right side Bottom)
            Card(
                modifier = Modifier.weight(0.9f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("EMOTES E BALÕES DE FEEDBACK RÁPIDO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf(
                            Triple("🥋", "OSS!", "OSS!! Curvando-se em respeito"),
                            Triple("🔥", "FOGO!", "Adicionou aura flamejante temporária"),
                            Triple("🧘", "MEDITAR", "Iniciou postura de recuperação mística"),
                            Triple("🥤", "AÇAÍ", "Recarregou HP com suco rápido"),
                            Triple("🏆", "CROWN", "Sinalizou vitória com troféu brilhante")
                        ).forEach { (icon, reactionText, systemOutput) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintOrange.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .clickable {
                                        activeSpeechLabel = "$icon $reactionText"
                                        recentDojoConsoleLog = systemOutput
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(icon, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Game Console Log status ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(6.dp))
                .border(0.5.dp, BlueprintTeal, RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LOG DO COCKPIT DE DESENVOLVEDOR: $recentDojoConsoleLog",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = BlueprintTeal
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // SECTION B: DETAILED LANDSCAPE ARCHITECTURE AND TECHNICAL MANUALS (GERAR 1 AO 7)
        // =========================================================================
        Text(
            text = "II. LIVRO DE ENGENHARIA: ARQUITETURA MULTI-TELA LANDSCAPE EXCLUSIVO",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 1. Arquitetura de Telas Landscape
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Schema, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("1. ARQUITETURA DE TELAS LANDSCAPE SÍNCRONA (ROUTING STREAM)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A navegação do JiuVerse é projetada de forma horizontal de ponta a ponta sem qualquer redundância Portrait:\n" +
                           "  • Splash View: Bloqueia a orientação em Landscape Left/Right imediatamente na inicialização nativa.\n" +
                           "  • Auth (OAuth2 Code Flow): Abre uma Webview injetada em modo fullscreen horizontal para login do Google ou carteiras Sandbox.\n" +
                           "  • Lobby / Dojo Hub: Interface unificada de navegação modular usando canais paralelos de dados síncronos.\n" +
                           "  • World Interactive Map (80/20 UI Partition): Onde o canvas de renderização interage diretamente com o barramento do broker.\n" +
                           "  • Navigation Model: Configurado sem empilhamento destrutivo do estado do jogo (Stack Navigation preserva o Renderer Socket ativo).",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    lineHeight = 14.sp
                )
            }
        }

        // 2. Wireframe Schema ASCII (Exact HUD Split 80/20)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Grid4x4, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("2. WIREFRAME RETRO DO HUD DA INTERFACE DO JOGO (80% MUNDO / 20% CONTROLES)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                val wireframeAscii = """
+-----------------------------------------------------------------------------------------+
| [ LADO ESQUERDO: HUD 20% ]  | [ CENTRO CANVASES MUNDO: HUD 80% ] | [ LADO DIREITO: HUD 20% ] |
|-----------------------------+------------------------------------+--------------------------|
| [O] Mini-Mapa Relâmpago SP  |                                    | 💬 CHAT DE SESSÃO SOCIAL |
|                             |   🥋 NoGi_King_XP                  |   <Miyao>: "Oss!"        |
|  Atleta Brasil (Lvl 42)     |       (Avatar Ativo)               |   <Helio>: "Base..."     |
|  HP  [==========] 850v      |                                    |                          |
|  STM [=======---] 220s      |   Mestre Helio Gracie              | 👥 CONTATOS DO DOJO      |
|                             |    "Busque a alavanca..."          |   Miyao_Bros    - Hub 2  |
| 60 FPS                      |                                    |   Marcelo       - GP     |
| Lag: 18ms                   |                                    |                          |
|-----------------------------+                                    | 📜 MISSÕES DO GP         |
| [JOYSTICK VIRTUAL RETRO]    |   [🎙️ VOZ (Microfone)]              |  - Ganhar 3 Sparrings    |
|                             |   [🤝 INTERAGIR COM DOJO RESIDENTE] |  - Mover Mestre Helio    |
|   (O) <- Arrastar para mover|   [🥊 ENVIAR DESAFIO SPARRING]     |                          |
+-----------------------------------------------------------------------------------------+
| [PARTE INFERIOR HUD SLOTS: BARRA DE ATALHOS RAPIDOS]                                    |
| [ 🥋 Faixa Pr. ] [ 🥋 Quimono Pix ] [ 🥤 Suco Açaí ] | Emojis Rápidos: [🥋] [🔥] [🧘] [🥤] [🏆] |
+-----------------------------------------------------------------------------------------+
                """.trimIndent()

                CodeBlock(code = wireframeAscii, title = "MMORPG Social Layout Partition Wireframe Specification")
            }
        }

        // 3. Estrutura de Componentes
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Widgets, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("3. ESTRUTURA METÓDICA DE COMPONENTES DE JOGO (HIERARCHY DEEP DIVE)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A árvore de componentes do JiuVerse MMORPG é segmentada para preloading reativo no loop de frames:\n" +
                           "  1. GameWorldContainer (Componivel Raiz - Aloca tamanhos absolutos e previne recomposições síncronas estéreis)\n" +
                           "    ├── CanvasRenderSurface (Desenha células isométricas de terrenos, colisões e rotas espaciais)\n" +
                           "    │     └── SpriteManager (Injeta avatars e NPCs a partir de atlas bitmap otimizados)\n" +
                           "    ├── UserHudOverlay (Controla renderizações HUD flutuantes na lateral esquerda, direita e rodapé)\n" +
                           "    │     ├── MiniMapWidget (Reativo ao fluxo de coordenadas do Socket)\n" +
                           "    │     └── SocialChatComponent (Fila assíncrona tolerante a concorrência com insets estáveis)\n" +
                           "    └── InputDeckController (Responsável pela captura física síncrona)\n" +
                           "          ├── VirtualJoystick (Coleta vetores cartesianos de translação e repassa ao motor de locomoção)\n" +
                           "          └── ActionButtonHub (Gatilhos rápidos para macros de habilidades, emotes, voz e desafios)",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    lineHeight = 14.sp
                )
            }
        }

        // 4. Código de Configuração Android (AndroidManifest.xml configs)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SettingsCell, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("4. CÓDIGO DE CONFIGURAÇÃO NATIVA ANDROID (SECURITY LOCK)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No Android nativo, garantimos que qualquer Activity do sistema seja forçada a ignorar retratos através do manifesto. Também configuramos configChanges para evitar a reinicialização e recriação do WebSocket durante rotações de 180 graus (sensorLandscape):",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val androidXmlSnippet = """
<!-- /app/src/main/AndroidManifest.xml -->
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:label="@string/app_name"
    android:screenOrientation="sensorLandscape" 
    android:configChanges="orientation|screenSize|keyboardHidden|screenLayout|smallestScreenSize"
    android:theme="@style/Theme.MyApplication">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
                """.trimIndent()
                CodeBlock(code = androidXmlSnippet, title = "AndroidManifest.xml Lock configuration")
            }
        }

        // 5. Código de Configuração iOS
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PhoneIphone, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("5. CÓDIGO DE CONFIGURAÇÃO NATIVA iOS (plist EXCLUSIVE DIRETRIZ)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Para travar a rotação no ambiente iOS nativo (Objective-C/Swift do Xcode), é obrigatório podar as chaves de orientação em 'Info.plist', extirpando as chaves que permitem Portrait (Vertical):",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val iosPlistSnippet = """
<!-- ios/JiuVerse/Info.plist -->
<key>UISupportedInterfaceOrientations</key>
<array>
    <string>UIInterfaceOrientationLandscapeLeft</string>
    <string>UIInterfaceOrientationLandscapeRight</string>
</array>
<key>UISupportedInterfaceOrientations~ipad</key>
<array>
    <string>UIInterfaceOrientationLandscapeLeft</string>
    <string>UIInterfaceOrientationLandscapeRight</string>
</array>
<key>UIRequiresFullScreen</key>
<true/>
                """.trimIndent()
                CodeBlock(code = iosPlistSnippet, title = "Info.plist orientation lockout payload")
            }
        }

        // 6. Configuração Expo
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.IntegrationInstructions, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("6. ARQUIVO DE CONFIGURAÇÃO EXPO JSON (CROSS-PLATFORM INTEGRATION)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Se a stack móvel utilizar Expo (React Native), a configuração é feita diretamente em 'app.json' ou 'app.config.js'. O motor do Expo converte essa chave em regras automatizadas para Android e iOS durante a compilação nativa (Prebuild):",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val expoJson = """
{
  "expo": {
    "name": "JiuVerse Sandbox eSports MMO",
    "slug": "jiuverse-mmo",
    "version": "1.0.0",
    "orientation": "landscape",
    "ios": {
      "supportsTablet": true,
      "requireFullScreen": true,
      "infoPlist": {
        "UISupportedInterfaceOrientations": [
          "UIInterfaceOrientationLandscapeLeft",
          "UIInterfaceOrientationLandscapeRight"
        ]
      }
    },
    "android": {
      "screenOrientation": "landscape",
      "permissions": [
        "RECORD_AUDIO",
        "INTERNET"
      ]
    }
  }
}
                """.trimIndent()
                CodeBlock(code = expoJson, title = "app.json - React Native Expo configuration")
            }
        }

        // 7. Boas práticas para MMORPG mobile horizontal (60 FPS & network sync)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Recommend, contentDescription = null, tint = BlueprintOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("7. BOAS PRÁTICAS PARA MMORPG MOVEL HORIZONTAL TOTAL (60 FPS STABLE)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "  • Multi-Threading & Looping: Processe toda a simulação de sockets de dados em threads secundárias do sistema (Dispatchers.Default). Jamais trave a Main Thread (UI Thread) com mensagens síncronas de jogadores.\n" +
                           "  • Otimização de Assets: Utilize formatos modernos de textura (e.g. ASTC ou WebP compacto) com spritesheets indexados. Isso reduz a taxa de leitura do disco e poupa RAM em dispositivos intermediários de 4GB.\n" +
                           "  • Rede PubSub de Baixa Latência: Utilize agrupadores de mensagens (Frame Aggregation) no WebSocket de proximidade. Ao invés de trafegar coordenadas individuais instantaneamente para centenas de avatares, dispare snapshots mundiais comprimidos a cada 100ms e use técnicas de Interpolação Linear (LERP) no cliente para desenhar caminhos suaves.\n" +
                           "  • Spatial Partitioning: Divida o mapa do sandbox em quadrantes de proximidade lógica (K-D Trees ou Quadtrees). O cliente só recebe os pacotes de áudio de voz e coordenadas de personagens que estiverem situados em até um quadrante de distância física do seu avatar, permitindo o suporte síncrono para milhares de participantes concorrentes.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
