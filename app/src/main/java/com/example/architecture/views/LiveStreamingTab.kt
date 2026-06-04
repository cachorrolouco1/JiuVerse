package com.example.architecture.views

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 1. Data Models for the Live Streaming Architecture
data class StreamFeed(
    val id: String,
    val title: String,
    val type: String, // "Torneio", "Aula ao Vivo", "Evento", "Palestra"
    val iconEmoji: String,
    val baseViewersRef: Int,
    var viewersCount: Int,
    val masterCoach: String,
    val description: String,
    val activeTechString: String,
    val defaultResolution: String,
    val streamingSourceUrl: String
)

data class LiveChatMessage(
    val id: String,
    val senderName: String,
    val senderAvatar: String,
    val messageText: String,
    val timeLabel: String,
    val role: String, // "Mestre", "Moderador", "Espectador"
    val badgeColor: Color,
    var isPinned: Boolean = false
)

data class ReactionBubble(
    val emoji: String,
    var count: Int,
    val highlightColor: Color
)

data class HighlightClip(
    val id: String,
    val timestampLabel: String,
    val description: String,
    val resolutionLabel: String,
    val technologyUsed: String,
    val segmentUuid: String
)

data class CdnNode(
    val id: String,
    val name: String,
    val region: String,
    var currentLoadPercentage: Int,
    var responseLatencyMs: Int,
    var activeConnections: Int,
    val ipAddress: String,
    var isShielded: Boolean
)

@Composable
fun LiveStreamingTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- STATE: Available Streams ---
    val streamFeeds = remember {
        mutableStateListOf(
            StreamFeed(
                id = "stream_1",
                title = "Grande Copa Angra Síncrona",
                type = "Torneio",
                iconEmoji = "🏆",
                baseViewersRef = 45000,
                viewersCount = 45220,
                masterCoach = "Federação JiuVerse",
                description = "Finais Absolutas do Cinturão de Titânio. Câmera principal tática focada na raspagem helicóptero.",
                activeTechString = "HLS + CDN Edge Delivery",
                defaultResolution = "1080p (60fps)",
                streamingSourceUrl = "https://cdn.jiuverse.com/live/angra-cup/master.m3u8"
            ),
            StreamFeed(
                id = "stream_2",
                title = "Aula Elite: Passagem Invisível",
                type = "Aula ao Vivo",
                iconEmoji = "🥋",
                baseViewersRef = 2400,
                viewersCount = 2415,
                masterCoach = "Mestre Helio Angra",
                description = "Masterclass ao vivo com perguntas interativas. Domínio tático de lapela e desequilíbrios.",
                activeTechString = "WebRTC Low-Latency Peer",
                defaultResolution = "720p (30fps)",
                streamingSourceUrl = "webrtc://webrtc-ingest.jiuverse.com/live/helio-pass"
            ),
            StreamFeed(
                id = "stream_3",
                title = "JiuVerse Global Keynote 2026",
                type = "Evento",
                iconEmoji = "🌐",
                baseViewersRef = 85000,
                viewersCount = 85600,
                masterCoach = "CTO Copacabana",
                description = "Revelação das novas mecânicas de Sandbox 3D e contratos inteligentes de apostas virtuais.",
                activeTechString = "HLS Multicast Adaptive",
                defaultResolution = "1080p (Source)",
                streamingSourceUrl = "https://cdn.jiuverse.com/live/keynote-2026/index.m3u8"
            ),
            StreamFeed(
                id = "stream_4",
                title = "Legado e Resiliência Psicológica",
                type = "Palestra",
                iconEmoji = "🎤",
                baseViewersRef = 9800,
                viewersCount = 9840,
                masterCoach = "Grande Mestre Rickson",
                description = "Conversa e perguntas e respostas sobre o controle de respiração em momentos de retenção extrema.",
                activeTechString = "WebRTC Interactive Web-Bridge",
                defaultResolution = "720p (30fps)",
                streamingSourceUrl = "webrtc://webrtc-ingest.jiuverse.com/talks/rickson-mindset"
            )
        )
    }

    var selectedStreamIndex by remember { mutableStateOf(0) }
    val currentStream = streamFeeds[selectedStreamIndex]

    // --- STATE: Chat Room ---
    val chatMessages = remember {
        mutableStateListOf(
            LiveChatMessage("c_1", "Mestre Rickson", "🥋", "Sejam bem-vindos à transmissão oficial! Oss.", "21:00", "Mestre", BlueprintOrange, true),
            LiveChatMessage("c_2", "Mod_Carlos", "🛡️", "Lembrem-se de manter o respeito no chat com o regulamento do dojo.", "21:01", "Moderador", BlueprintTeal),
            LiveChatMessage("c_3", "Renzo_Copacabana", "🦈", "Essa raspagem de gancho foi cirúrgica demais!", "21:03", "Espectador", Color.White),
            LiveChatMessage("c_4", "Gabi_Guarderia", "⚡", "Estou testando as táticas na TV e aqui no celular síncrono.", "21:04", "Espectador", Color.White),
            LiveChatMessage("c_5", "KimonosGlow", "🛒", "Tem cupom ativo para a loja oficial?", "21:04", "Espectador", Color.White)
        )
    }

    // --- STATE: Reactions Overlay Tracker ---
    val activeReactions = remember {
        mutableStateListOf(
            ReactionBubble("🥋", 4220, BlueprintOrange),
            ReactionBubble("🔥", 8932, BlueprintRed),
            ReactionBubble("❤️", 3102, BlueprintRed),
            ReactionBubble("😮", 1545, BlueprintCyan),
            ReactionBubble("👍", 5430, BlueprintTeal)
        )
    }

    // --- STATE: Highlight Clips ---
    val highlightClips = remember {
        mutableStateListOf(
            HighlightClip("clip_1", "00:14:22", "Raspagem Helicóptero Sensacional", "1080p", "HLS Segment #412", "hl-seg-74bd2e-0142"),
            HighlightClip("clip_2", "00:32:05", "Chave de Braço voadora - Final de Luta", "1080p", "HLS Segment #1094", "hl-seg-38f32a-0320")
        )
    }

    // --- STATE: CDN Cloud Nodes Dashboard ---
    val cdnNodes = remember {
        mutableStateListOf(
            CdnNode("cdn_1", "São Paulo Edge Active", "América do Sul (Principal)", 72, 14, 25000, "172.64.120.4", true),
            CdnNode("cdn_2", "Angra Local Edge Hub", "Dojo Local / Resort", 35, 3, 3400, "10.15.2.22", true),
            CdnNode("cdn_3", "Rio de Janeiro Main POP", "Sudeste", 89, 8, 48000, "186.202.1.99", true),
            CdnNode("cdn_4", "Miami Edge Booster", "América do Norte", 22, 115, 9200, "192.16.8.10", false)
        )
    }

    // --- STATE: Custom Input fields and toggles ---
    var customChatMessageText by remember { mutableStateOf("") }
    var chatFilterStatusMsg by remember { mutableStateOf("Filtro Ativo: Moderado Seguro") }
    var streamResolutionSelection by remember { mutableStateOf("1080p (Automatico)") }
    var isLowLatencyForced by remember { mutableStateOf(false) }
    var isModeratorViewActive by remember { mutableStateOf(false) }

    // --- STATE: Settings for simulation ---
    var activeLiveBitrate by remember { mutableStateOf(4850) } // kbps
    var activeFpsCounter by remember { mutableStateOf(60) }
    var streamLatencyms by remember { mutableStateOf(2400f) } // millisecs
    var activeIngestCodec by remember { mutableStateOf("AV1 Hybrid / Opus") }

    // --- STATE: Massive 100,000 Spectators Simulator Engine ---
    var isSimulatingSpike by remember { mutableStateOf(false) }
    var loadSpikeProgress by remember { mutableStateOf(0f) }
    var currentSimulatedThroughputGbps by remember { mutableStateOf(12.4f) }
    var simulatedDropRateCount by remember { mutableStateOf(0.04f) }

    // --- CONCORDANT AUDIO POWER SIMULATION METER INDICATORS ---
    var audioLevelPeak by remember { mutableStateOf(4) }

    // Coroutine loops to update variables over time for active feeling
    LaunchedEffect(key1 = currentStream) {
        while (true) {
            delay(1500)
            // Gently fluctuate viewers count to give life to stats
            streamFeeds.forEachIndexed { idx, value ->
                val variation = kotlin.random.Random.nextInt(-15, 20)
                streamFeeds[idx] = value.copy(viewersCount = (value.baseViewersRef + variation).coerceAtLeast(10))
            }
            audioLevelPeak = kotlin.random.Random.nextInt(1, 8)
            // Slightly fluctuate latency and bitrate
            activeLiveBitrate = 4500 + kotlin.random.Random.nextInt(-350, 400)
            activeFpsCounter = if (currentStream.defaultResolution.contains("60fps")) 60 + kotlin.random.Random.nextInt(-1, 1) else 30
        }
    }

    // Action execution helpers
    fun injectChatMessage(textToInject: String, customRole: String = "Espectador", badgeCol: Color = Color.White) {
        if (textToInject.isBlank()) return
        
        // 1. Toxicity filter scanning
        val isVulgar = textToInject.contains("bosta", ignoreCase = true) || 
                       textToInject.contains("merda", ignoreCase = true) || 
                       textToInject.contains("otario", ignoreCase = true) ||
                       textToInject.contains("canalha", ignoreCase = true)
        
        if (isVulgar) {
            chatFilterStatusMsg = "⚠️ BLOQUEADO: Mensagem ofensiva interceptada por Auto-Mod AI."
            return
        }

        // Add to active chat queue
        val currentMinutes = "21:${kotlin.random.Random.nextInt(10, 59)}"
        chatMessages.add(
            LiveChatMessage(
                id = "chat_inject_${kotlin.random.Random.nextInt()}",
                senderName = if (customRole == "Espectador") "Você_Fã_JiuVerse" else "Mod_Automático",
                senderAvatar = "🥋",
                messageText = textToInject,
                timeLabel = currentMinutes,
                role = customRole,
                badgeColor = badgeCol
            )
        )
        chatFilterStatusMsg = "Mensagem transmitida via WebRTC Ingestion"
    }

    fun triggerMassiveCdnLoadSimulation() {
        if (isSimulatingSpike) return
        scope.launch {
            isSimulatingSpike = true
            loadSpikeProgress = 0f
            chatFilterStatusMsg = "Configurando replicadores CDN multicontinentais..."

            while (loadSpikeProgress < 1.0f) {
                delay(150)
                loadSpikeProgress += 0.05f
                currentSimulatedThroughputGbps = 12.4f + (loadSpikeProgress * 420.5f)
                simulatedDropRateCount = 0.04f + (loadSpikeProgress * 0.02f)
                
                // Stress test load distribution
                cdnNodes.forEachIndexed { i, node ->
                    cdnNodes[i] = node.copy(
                        currentLoadPercentage = (30 + (loadSpikeProgress * 65) + kotlin.random.Random.nextInt(-5, 5)).toInt().coerceIn(0, 100),
                        responseLatencyMs = (node.responseLatencyMs + (loadSpikeProgress * 15)).toInt().coerceAtLeast(1)
                    )
                }
            }

            loadSpikeProgress = 1.0f
            isSimulatingSpike = false
            chatFilterStatusMsg = "Sucesso: CDN blindou tráfego de 100k usuários com 0.02% de drop rates síncronos!"
            injectChatMessage("CDN Stress Test concluído com sucesso. 100k conexões blindadas no Edge!", "Moderador", BlueprintTeal)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        // ACTING SECTION HEADER
        SectionHeader(
            title = "JiuVerse Live Streaming & Video Pipeline Engine",
            subtitle = "Estúdio de Engenharia com Ingestão de Sinal de Baixa Latência (WebRTC) e Distribuição Global HLS/CDN para milhões de espectadores síncronos."
        )

        // I. ARQUITETO BADGE & CURRENT STATS CONSOLE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(1.dp, BlueprintCyan, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.3f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(BlueprintGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PERFIL: ARQUITETO DE STREAMING DO JIUVERSE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = BlueprintGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Consola de Telemetria e Transmissão",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Codecs Ativos: HEVC Main10 | VP9 Fallback | Opus Audio Multiplex",
                    fontSize = 9.sp,
                    color = BlueprintTextSecondary
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(0.7f)
            ) {
                Text(
                    text = "Throughput: ${(currentStream.viewersCount * 0.0031f).format(2)} Gbps",
                    fontSize = 10.sp,
                    color = BlueprintCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total Viewers: ${currentStream.viewersCount}",
                    fontSize = 9.5.sp,
                    color = BlueprintTextPrimary,
                    fontWeight = FontWeight.Black
                )
                Box(
                    modifier = Modifier
                        .background(BlueprintOrange.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("LATENCY PROFILE: ${if (isLowLatencyForced) "WebRTC 120ms" else "HLS Adaptive 3s"}", fontSize = 7.5.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // II. SELETOR DE CANAIS DE VÍDEO (Tournament, Class, Event, Talks)
        Text(
            text = "I. SELEÇÃO DE TRANSMISSÕES DA PLATAFORMA (Ingestão de Sinais)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(streamFeeds.size) { index ->
                val fed = streamFeeds[index]
                val isSelected = selectedStreamIndex == index

                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .border(
                            1.dp,
                            if (isSelected) BlueprintCyan else BlueprintGridLine,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedStreamIndex = index },
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF1E2E4E) else BlueprintCard)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (fed.type) {
                                            "Torneio" -> BlueprintRed.copy(alpha = 0.2f)
                                            "Aula ao Vivo" -> BlueprintTeal.copy(alpha = 0.2f)
                                            "Evento" -> BlueprintOrange.copy(alpha = 0.2f)
                                            else -> BlueprintCyan.copy(alpha = 0.2f)
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = fed.type,
                                    fontSize = 7.5.sp,
                                    color = when (fed.type) {
                                        "Torneio" -> BlueprintRed
                                        "Aula ao Vivo" -> BlueprintTeal
                                        "Evento" -> BlueprintOrange
                                        else -> BlueprintCyan
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Glow Red dot representing live signal
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(5.dp).background(BlueprintRed, CircleShape))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("LIVE", fontSize = 7.sp, color = BlueprintRed, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(fed.iconEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = fed.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Por: ${fed.masterCoach}",
                            fontSize = 8.5.sp,
                            color = BlueprintTextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${fed.viewersCount} 👥",
                                fontSize = 8.5.sp,
                                color = BlueprintCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = fed.defaultResolution,
                                fontSize = 8.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        // III. DETALHE DO FEED ATIVO & VIDEO PLAYER SIMULATOR
        Text(
            text = "II. MONITOR DE INGESTÃO E VIDEO PLAYER DE ESTABILIDADE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BlueprintGridLine, RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Video Screen Mockup with glowing aesthetics
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF020617))
                        .border(1.dp, BlueprintCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Grid background simulating camera overlay
                    Box(modifier = Modifier.fillMaxSize().drawBlueprintGrid())

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Fluctuate glowing indicator of simulated sparring stream
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Active Camera Feed",
                                tint = BlueprintCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STREAM INGEST CONDUIT ACTIVE",
                                fontSize = 11.sp,
                                color = BlueprintCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "[ ${currentStream.title} ]",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "URL Fonte: ${currentStream.streamingSourceUrl}",
                            fontSize = 8.sp,
                            color = BlueprintTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Audio dynamic spectrum fluctuating
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.height(18.dp)
                        ) {
                            for (i in 1..10) {
                                val currentHeight = remember(audioLevelPeak) {
                                    kotlin.random.Random.nextInt(4, 18)
                                }
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(currentHeight.dp)
                                        .background(
                                            if (currentHeight > 14) BlueprintOrange else BlueprintTeal,
                                            CircleShape
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("SIMULATED AUDIO LEVEL (OPUS INGEST -48dB)", fontSize = 6.5.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    }

                    // Floating overlays (Latency, Resolution, Tech)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TEC: ${currentStream.activeTechString}",
                            fontSize = 7.5.sp,
                            color = BlueprintOrange,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${activeFpsCounter} FPS | ${activeLiveBitrate} kbps",
                            fontSize = 8.sp,
                            color = BlueprintGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Bottom Player controls overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("00:45:12", fontSize = 8.sp, color = Color.White)
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("AUTO-BITRATE: ON", fontSize = 7.sp, color = BlueprintGreen, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .background(BlueprintCyan.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("HD 1080p", fontSize = 7.sp, color = BlueprintCyan, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stream diagnostic details block
                Text(
                    text = currentStream.description,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Technical controls (Resolution, Force Latency Option)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Configuração de Resolução:", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
                            listOf("1080p", "720p", "480p (P2P)").forEach { res ->
                                val isSelected = streamResolutionSelection.contains(res)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) BlueprintCyan else Color.Black)
                                        .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                        .clickable { 
                                            streamResolutionSelection = res 
                                            activeLiveBitrate = if (res.contains("1080p")) 4800 else if (res.contains("720p")) 2400 else 900
                                        }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(res, fontSize = 8.sp, color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Atalhos Tecnológicos:", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isLowLatencyForced) Color(0xFF132D28) else Color.Black)
                                .border(0.5.dp, if (isLowLatencyForced) BlueprintTeal else BlueprintGridLine, RoundedCornerShape(4.dp))
                                .clickable { 
                                    isLowLatencyForced = !isLowLatencyForced 
                                    if (isLowLatencyForced) {
                                        streamLatencyms = 120f
                                        activeIngestCodec = "WebRTC Opus Unified"
                                    } else {
                                        streamLatencyms = 2800f
                                        activeIngestCodec = "HLS AV1 Segmented"
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isLowLatencyForced) Icons.Default.Bolt else Icons.Default.SettingsEthernet,
                                contentDescription = "Low Latency Toggle",
                                tint = if (isLowLatencyForced) BlueprintTeal else Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLowLatencyForced) "WebRTC Low Delay: ON" else "Force WebRTC Latency",
                                fontSize = 8.sp,
                                color = if (isLowLatencyForced) BlueprintTeal else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // IV. CHAT EM TEMPO REAL & REAÇÕES INTERATIVAS
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "III. CHAT SÍNCRONO & ENGAJAMENTO (Multi-User Stream)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Box(
                modifier = Modifier
                    .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = "Chat WebSockets Hub", fontSize = 8.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Interactive Pinned Message Capsule (If any message is pinned)
                val pinnedMessage = chatMessages.find { it.isPinned }
                if (pinnedMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .background(BlueprintCyan.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .border(0.5.dp, BlueprintCyan, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PushPin, contentDescription = "Pinned Message", tint = BlueprintCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                "MENSAGEM FIXADA POR ${pinnedMessage.senderName.uppercase()} (${pinnedMessage.role.uppercase()}):",
                                fontSize = 7.5.sp,
                                color = BlueprintCyan,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(pinnedMessage.messageText, fontSize = 10.sp, color = Color.White)
                        }
                    }
                }

                // Chat Log display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color.Black)
                        .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        chatMessages.forEach { msg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (msg.isPinned) BlueprintCyan.copy(alpha = 0.05f) else Color.Transparent)
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(0xFF1E293B), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(msg.senderAvatar, fontSize = 9.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = msg.senderName, 
                                            fontSize = 9.5.sp, 
                                            fontWeight = FontWeight.Bold, 
                                            color = if (msg.role == "Mestre") BlueprintOrange else if (msg.role == "Moderador") BlueprintTeal else Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(msg.badgeColor.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                                .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                        ) {
                                            Text(msg.role, fontSize = 6.sp, color = msg.badgeColor, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(msg.timeLabel, fontSize = 7.5.sp, color = Color.Gray)
                                    }
                                    Text(msg.messageText, fontSize = 10.sp, color = Color.LightGray)
                                }
                                
                                // Direct Moderator actions side button
                                if (isModeratorViewActive) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Deletar Mensagem",
                                        tint = BlueprintRed,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { chatMessages.remove(msg) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Interactions toolbar (OSS, Raspagem, Incrível, etc.)
                Text("Interações Rápidas de Torcida:", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val presets = listOf("OSS! 🥋", "Que raspagem linda! 🔥", "Arquiteto sem lag! ⚡", "Mestre Rickson é lenda! 🙌")
                    presets.forEach { text ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF101B2E), RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                .clickable { injectChatMessage(text) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(text, fontSize = 8.5.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Custom Chat Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = customChatMessageText,
                        onValueChange = { customChatMessageText = it },
                        placeholder = { Text("Fale no chat dojo com respeito...", fontSize = 9.sp, color = Color.Gray) },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Black,
                            unfocusedContainerColor = Color.Black
                        )
                    )

                    Button(
                        onClick = {
                            if (customChatMessageText.isNotBlank()) {
                                injectChatMessage(customChatMessageText)
                                customChatMessageText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan, contentColor = Color.Black),
                        modifier = Modifier.height(42.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", modifier = Modifier.size(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Moderado status text visual feedback
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "🔬 AUTO-MOD & DISPATCH: $chatFilterStatusMsg",
                        fontSize = 8.5.sp,
                        color = BlueprintCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SPECTATOR REACTION TICKER OVERLAYS
                Text("Reações Coletivas da Tropa (Toque para disparar):", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activeReactions.forEachIndexed { idx, react ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(react.highlightColor.copy(alpha = 0.15f))
                                .border(0.5.dp, react.highlightColor, RoundedCornerShape(6.dp))
                                .clickable {
                                    activeReactions[idx] = react.copy(count = react.count + 1)
                                    chatFilterStatusMsg = "Reação '${react.emoji}' propagada no CDN Multiplex!"
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(react.emoji, fontSize = 16.sp)
                                Text(
                                    text = "${react.count}",
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // V. DESTAQUES & CLIPIADORA SÍNCRONA (Highlights Clip Generator)
        Text(
            text = "IV. MARCADOR DE DESTAQUES & GERAÇÃO DE CLIPS (Timeline Highlighting)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "No JiuVerse, o Arquiteto de Streaming permite catalogar instantaneamente trechos marcantes do torneio para gerar replay síncrono. O clipe captura metadados, tempo de ingestão e id do bloco CDN.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val rSegment = "hl-seg-${kotlin.random.Random.nextInt(100000, 999999)}"
                            val randSecs = kotlin.random.Random.nextInt(10, 59)
                            val randMins = kotlin.random.Random.nextInt(10, 55)
                            val timestamp = "00:$randMins:$randSecs"
                            
                            val descPreset = listOf(
                                "Fuga de quadril inteligente de atleta",
                                "Tentativa de estrangulamento por trás",
                                "Queda perfeita de double leg no tatame",
                                "Raspagem tática do co-host de Angra"
                            ).random()

                            highlightClips.add(
                                0,
                                HighlightClip(
                                    id = "clip_${highlightClips.size + 1}",
                                    timestampLabel = timestamp,
                                    description = descPreset,
                                    resolutionLabel = "1080p (HQ Source)",
                                    technologyUsed = if (isLowLatencyForced) "WebRTC Frame Ingest" else "HLS Segment Node",
                                    segmentUuid = rSegment
                                )
                            )
                            chatFilterStatusMsg = "Clipe gerado no timestamp síncrono $timestamp!"
                            injectChatMessage("⚠️ Clipe criado por Mod: '$descPreset' em $timestamp", "Moderador", BlueprintTeal)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintOrange, contentColor = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = "Gerar Clipe", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GERAR CLIPE DE DESTAQUE 📹", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            highlightClips.clear()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                        modifier = Modifier.height(38.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("LIMPAR", fontSize = 9.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Clips list view
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (highlightClips.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Color.Black)
                                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhum destaque gerado nesta sessão.", fontSize = 9.sp, color = Color.Gray)
                        }
                    } else {
                        highlightClips.forEach { clip ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black)
                                    .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayCircleFilled, contentDescription = "Play Replay", tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(clip.description, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        Row {
                                            Text("Timestamp: ", fontSize = 8.sp, color = BlueprintTextSecondary)
                                            Text(clip.timestampLabel, fontSize = 8.sp, color = BlueprintOrange, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                            Text(" | ID de Bloco: ${clip.segmentUuid}", fontSize = 8.sp, color = Color.LightGray)
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(clip.technologyUsed, fontSize = 7.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // VI. CDN EDGE BLUEPRINT & LIVE HARDWARE STATUS MAP
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "V. ESTRUTURA GLOBAL DE CDN & REPLICADORES EDGE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Box(
                modifier = Modifier
                    .background(BlueprintOrange.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .border(0.5.dp, BlueprintOrange, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = "Edge Shielding Level: 99.8%", fontSize = 8.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "A distribuição adaptativa descarrega 90% do faturamento de tráfego do banco relacional de Angra, utilizando POPs (Ponto de Presença) Edge em memcached e replicadores de buffers WebSockets.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Layout of edge nodes
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cdnNodes.forEach { node ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black)
                                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(if (node.currentLoadPercentage < 80) BlueprintGreen else BlueprintOrange, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(node.name, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Text("Região: ${node.region} | IP: ${node.ipAddress}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                            }

                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Load progress bar inside node card
                                Column {
                                    Text("Carga: ${node.currentLoadPercentage}%", fontSize = 7.sp, color = Color.White)
                                    Box(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(4.dp)
                                            .background(Color.DarkGray, RoundedCornerShape(2.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(node.currentLoadPercentage / 100f)
                                                .background(if (node.currentLoadPercentage < 80) BlueprintTeal else BlueprintOrange, RoundedCornerShape(2.dp))
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${node.responseLatencyMs}ms Ping", fontSize = 8.5.sp, color = BlueprintCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    Text("${node.activeConnections} active", fontSize = 7.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // MASSIVE EVENT LOAD TESTING TOOL
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, BlueprintCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "STRESS BENCHMARK SÍNCRONO: 100K SPECTATORS SPIKE",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintCyan,
                                fontFamily = FontFamily.Monospace
                            )
                            if (isSimulatingSpike) {
                                Text("ESTRESSANDO...", fontSize = 8.5.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gere picos simultâneos no cluster e teste se o chaveamento de bitrate adaptativo reage para evitar buffering global. Simula tráfego concorrente de 432 Gbps de pico.",
                            fontSize = 9.sp,
                            color = BlueprintTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mock Ingesting load:", fontSize = 8.sp, color = Color.White)
                            Text(
                                text = "${currentSimulatedThroughputGbps.format(1)} Gbps",
                                fontSize = 11.sp,
                                color = BlueprintOrange,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        LinearProgressIndicator(
                            progress = loadSpikeProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = BlueprintCyan,
                            trackColor = Color.DarkGray
                        )

                        Button(
                            onClick = { triggerMassiveCdnLoadSimulation() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSimulatingSpike) Color.DarkGray else BlueprintCyan, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSimulatingSpike,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("⚡ DISPARAR TESTE DE CARGA CDN (100.000 SPECTATOR CONDUIT)", fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Decimal formatting extension for precise telemetry values
fun Float.format(digits: Int) = "%.${digits}f".format(this)
