package com.example.architecture.views

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.database.PlayerMemoryEntity
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenseiAiTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Initialize Database components for Sensei AI
    LaunchedEffect(Unit) {
        viewModel.initializeSensei(context)
    }

    // Observe StateFlows from ViewModel
    val chatMessages by viewModel.senseiChatHistory.collectAsState()
    val playerMemory by viewModel.playerMemory.collectAsState()
    val isThinking by viewModel.isSenseiThinking.collectAsState()

    // Local Speech & TTS State
    var isTtsReady by remember { mutableStateOf(false) }
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isMuted by remember { mutableStateOf(false) }
    
    // Voice/Mic Simulator states
    var isRecording by remember { mutableStateOf(false) }
    var voiceProgressSeconds by remember { mutableStateOf(0) }
    var micWavePhase by remember { mutableStateOf(0f) }
    var transcribedTextResult by remember { mutableStateOf("") }
    
    // Manual text input
    var textInput by remember { mutableStateOf("") }

    // TTS Setup beautifully managed
    DisposableEffect(context) {
        var speech: TextToSpeech? = null
        speech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = speech?.setLanguage(Locale("pt", "BR"))
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true
                }
            }
        }
        ttsInstance = speech
        onDispose {
            speech?.stop()
            speech?.shutdown()
        }
    }

    // Speak helper function
    val speakSpeechText = { text: String ->
        if (isTtsReady && !isMuted) {
            ttsInstance?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sensei_talk")
        }
    }

    // Mic wave oscillation timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            voiceProgressSeconds = 0
            while (isRecording) {
                delay(30)
                micWavePhase += 0.2f
            }
        }
    }

    // Voice simulation counter seconds
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                voiceProgressSeconds += 1
                if (voiceProgressSeconds >= 6) { // Auto-stop recording at 6s
                    isRecording = false
                    // Trigger fake speech evaluation based on template chosen
                    val query = transcribedTextResult.ifBlank { "Mestre, me sugira uma missão para iniciante" }
                    viewModel.askSensei(query, speakSpeechText)
                    transcribedTextResult = ""
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        // I. HEADER DO ASSISTENTE
        SectionHeader(
            title = "Sensei AI — Assistente de Voz & Consciência",
            subtitle = "Guia inteligente integrado de Jiu-Jitsu. Suporta conversação de voz Speech-to-Text e Text-to-Speech nativo sincronizado com persistência Room."
        )

        // II. MEMÓRIA DO JOGADOR CONTEXTUAL (PERSISTENT DATA CARD COVERS CAPABILITIES)
        Text(
            text = "I. COGNITION: MEMÓRIA PERSISTIDA DO JOGADOR (Room DB Tracker)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        playerMemory?.let { memory ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlueprintGridLine, RoundedCornerShape(10.dp))
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(BlueprintGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "PERFIL ATIVO DO JOGADOR NO BANCO DE DADOS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                viewModel.savePlayerMemory(PlayerMemoryEntity()) // Reset database profiles
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Resetar Perfil", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Editable Player Name
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Nome do Guerreiro:", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            OutlinedTextField(
                                value = memory.playerName,
                                onValueChange = { newName ->
                                    viewModel.savePlayerMemory(memory.copy(playerName = newName))
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black,
                                    focusedBorderColor = BlueprintCyan,
                                    unfocusedBorderColor = BlueprintGridLine
                                )
                            )
                        }

                        // Belt Level Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sinal de Graduação (Faixa):", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black)
                                    .border(1.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable {
                                        val belts = listOf("Faixa Branca", "Faixa Azul", "Faixa Roxa", "Faixa Marrom", "Faixa Preta", "Faixa Coral")
                                        val nextIndex = (belts.indexOf(memory.playerBelt) + 1) % belts.size
                                        viewModel.savePlayerMemory(memory.copy(playerBelt = belts[nextIndex]))
                                    }
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val beltColor = when (memory.playerBelt) {
                                        "Faixa Branca" -> Color.White
                                        "Faixa Azul" -> BlueprintCyan
                                        "Faixa Roxa" -> Color(0xFFC084FC)
                                        "Faixa Marrom" -> Color(0xFFB45309)
                                        "Faixa Preta" -> BlueprintRed
                                        "Faixa Coral" -> BlueprintOrange
                                        else -> Color.White
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(beltColor, RoundedCornerShape(1.dp)))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(memory.playerBelt, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Next Belt", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Level input
                        Column(modifier = Modifier.weight(0.6f)) {
                            Text("Nível:", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            OutlinedTextField(
                                value = memory.playerLevel.toString(),
                                onValueChange = { newValue ->
                                    val levelVal = newValue.toIntOrNull() ?: memory.playerLevel
                                    viewModel.savePlayerMemory(memory.copy(playerLevel = levelVal))
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black,
                                    focusedBorderColor = BlueprintCyan,
                                    unfocusedBorderColor = BlueprintGridLine
                                )
                            )
                        }

                        // XP input
                        Column(modifier = Modifier.weight(0.8f)) {
                            Text("XP Acumulado:", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            OutlinedTextField(
                                value = memory.playerXp.toString(),
                                onValueChange = { newValue ->
                                    val xpVal = newValue.toIntOrNull() ?: memory.playerXp
                                    viewModel.savePlayerMemory(memory.copy(playerXp = xpVal))
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black,
                                    focusedBorderColor = BlueprintCyan,
                                    unfocusedBorderColor = BlueprintGridLine
                                )
                            )
                        }

                        // Academy input
                        Column(modifier = Modifier.weight(1.4f)) {
                            Text("Academia Base Real/Virtual:", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            OutlinedTextField(
                                value = memory.academyName,
                                onValueChange = { newAcad ->
                                    viewModel.savePlayerMemory(memory.copy(academyName = newAcad))
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black,
                                    focusedBorderColor = BlueprintCyan,
                                    unfocusedBorderColor = BlueprintGridLine
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tactical stats & style selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Favorite Guard Style
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Estilo Predileto:", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val styles = listOf("Guarda Fechada", "Passador")
                                styles.forEach { style ->
                                    val isSelected = memory.favoriteStyle == style
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) BlueprintTeal.copy(alpha = 0.2f) else Color.Black)
                                            .border(1.dp, if (isSelected) BlueprintTeal else BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .clickable {
                                                viewModel.savePlayerMemory(memory.copy(favoriteStyle = style))
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(style, fontSize = 8.5.sp, color = if (isSelected) BlueprintTeal else Color.Gray, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Completed Quests & Rep Status Display
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Estatísticas dadas pelo Sensei:", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Missões: ${memory.completedQuestsCount}", fontSize = 9.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(BlueprintGridLine))
                                Text("Reputação: ${memory.masterReputation}", fontSize = 9.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Injected context memory
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = "Consciência", tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FATORES DE MEMÓRIA SENSÍVEL: \"${memory.customNotes}\" (Último local: ${memory.lastVisitedRegion})",
                                fontSize = 8.5.sp,
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // III. CONVERSA INTEGRAL: TEXT & SPEAKER SCREEN
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "II. CHAT TERMINAL: SENSEI'S RESPONSES (Room History)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Speak Toggle Icon button to mute TTS readouts
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Mudar áudio",
                        tint = if (isMuted) BlueprintRed else BlueprintCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMuted) "Speaker: Mutado" else "Speaker Outbox: Ativo",
                    fontSize = 9.sp,
                    color = if (isMuted) BlueprintRed else BlueprintCyan,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Central Terminal view box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            val listState = rememberLazyListState()
            LaunchedEffect(chatMessages.size) {
                if (chatMessages.isNotEmpty()) {
                    listState.animateScrollToItem(chatMessages.size - 1)
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                if (chatMessages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = "Sensei", tint = BlueprintGridLine, modifier = Modifier.size(34.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "O dojo está em silêncio.\nDiga algo ou faça um input de voz para invocar o Sensei.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isSensei = msg.sender == "sensei"
                            val bubbleColor = if (isSensei) Color(0xFF1E2E4E) else Color(0xFF1F2937)
                            val accentColor = if (isSensei) BlueprintCyan else BlueprintTeal
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isSensei) Arrangement.Start else Arrangement.End
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    colors = CardDefaults.cardColors(containerColor = bubbleColor),
                                    shape = RoundedCornerShape(if (isSensei) 0.dp else 8.dp)
                                        .copy(topStart = RoundedCornerShape(8.dp).topStart),
                                    border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isSensei) Icons.Outlined.KeyboardDoubleArrowRight else Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = accentColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isSensei) "SENSEI AI (Mestre Sábio)" else "VOCÊ ($msg.localizedTopic)",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accentColor,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                            
                                            if (isSensei) {
                                                IconButton(
                                                    onClick = { speakSpeechText(msg.content) },
                                                    modifier = Modifier.size(16.dp)
                                                ) {
                                                    Icon(Icons.Default.VolumeUp, contentDescription = "Falar Novamente", tint = BlueprintCyan, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = msg.content,
                                            fontSize = 11.5.sp,
                                            color = Color.White,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (isThinking) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                        border = BorderStroke(0.5.dp, BlueprintCyan.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(10.dp),
                                                strokeWidth = 1.5.dp,
                                                color = BlueprintCyan
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Sensei decifrando postura...",
                                                fontSize = 9.sp,
                                                color = BlueprintCyan,
                                                fontFamily = FontFamily.Monospace
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

        Spacer(modifier = Modifier.height(10.dp))

        // IV. INTERRUPÇÕES DE VOZ / SPEECH-TO-TEXT SIMULATOR BOARD
        Text(
            text = "III. SPEECH INGESTION: DISPATCHER DE VOZ (Speech-to-Text)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, if (isRecording) BlueprintOrange else BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = if (isRecording) Color(0xFF1E110A) else BlueprintCard)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "O Ingest de Voz traduz as ordens faladas ao microfone em instruções de texto estruturadas. Pressione para gravar ou selecione uma frase pré-programada de escuta.",
                    fontSize = 9.5.sp,
                    color = BlueprintTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Microphone sound wave rendering canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black)
                        .border(1.dp, BlueprintGridLine, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRecording) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val spacing = 8f
                            val midY = size.height / 2f
                            val itemsCount = (size.width / spacing).toInt()
                            
                            for (i in 0 until itemsCount) {
                                val distFromCenter = kotlin.math.abs(i - itemsCount / 2f) / (itemsCount / 2f)
                                val baseAmplitude = 25f * (1f - distFromCenter)
                                val waveFactor = sin(i * 0.15f + micWavePhase)
                                val height = baseAmplitude * waveFactor
                                
                                drawLine(
                                    color = BlueprintOrange,
                                    start = Offset(i * spacing, midY - height),
                                    end = Offset(i * spacing, midY + height),
                                    strokeWidth = 3f
                                )
                            }
                        }
                        
                        Text(
                            "GRAVANDO SINAL SÍNCRONO VIA WebRTC... 00:0${voiceProgressSeconds}s",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintOrange,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 4.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MicNone, contentDescription = "Mic Inativo", tint = Color.DarkGray, modifier = Modifier.size(18.dp))
                            Text(
                                "MODULAR DE VOZ: STANDBY INATIVO",
                                fontSize = 8.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Buttons / Controls Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isRecording = !isRecording
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) BlueprintRed else BlueprintOrange,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.StopCircle else Icons.Default.Mic,
                                contentDescription = "Gatilho de Voz",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRecording) "Parar e Enviar" else "Falar via Voz (Mic)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Direct Manual Clear
                    Button(
                        onClick = { viewModel.clearSenseiChatHistory() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BlueprintGridLine),
                        modifier = Modifier.weight(0.7f)
                    ) {
                        Text("Limpar", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preset Speech phrases triggers
                Text("Canais rápidos orientados (Fala simulada acelerada):", fontSize = 8.5.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                    val presets = listOf(
                        "Como herói da graduação, me ensine uma dica no tatame!" to "Mestre, me passa um guia de jiu jitsu para iniciantes?",
                        "Propor novas missões de combate no mapa dojo" to "Sensei, me mostre uma sugestão de missão heróica!",
                        "Explicar localizações táticas dadas no mapa" to "Como acho o dojo no mapa municipal?",
                        "Avisar sobre torneio e eventos futuros do JiuVerse" to "Onde vejo novidades sobre os torneios e eventos de peso absoluto?"
                    )

                    presets.forEach { (label, rawPrompt) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                .clickable {
                                    if (isRecording) {
                                        transcribedTextResult = rawPrompt
                                    } else {
                                        viewModel.askSensei(rawPrompt, speakSpeechText)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = "Fala", tint = BlueprintCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 9.5.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // V. MANUAL TEXT CONSOLE CONCRETE ENTRY
        Text(
            text = "IV. CONSERVA DIGITADA (Text Chat Input Console)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Digite sua mensagem para o Sensei...", fontSize = 10.sp, color = Color.Gray) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    focusedBorderColor = BlueprintCyan,
                    unfocusedBorderColor = BlueprintGridLine
                )
            )

            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.askSensei(textInput, speakSpeechText)
                        textInput = ""
                        focusManager.clearFocus()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan, contentColor = Color.Black),
                modifier = Modifier.height(44.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", modifier = Modifier.size(14.dp))
            }
        }
    }
}
