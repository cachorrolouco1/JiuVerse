package com.example.architecture.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.BlueprintCard
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintGridLine
import com.example.ui.theme.BlueprintOrange
import com.example.ui.theme.BlueprintRed
import com.example.ui.theme.BlueprintTeal
import com.example.ui.theme.BlueprintTextPrimary
import com.example.ui.theme.BlueprintTextSecondary

data class ProximityUserSim(
    val nickname: String,
    val role: String,
    var x: Float,
    var y: Float,
    var isLocalMuted: Boolean = false,
    var isBlocked: Boolean = false,
    var isReported: Boolean = false,
    var currentVolume: Float = 0f,
    var connectionStatus: String = "Disconnected"
)

@Composable
fun VoiceProximityTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // 1. Coordinates state of our Local User "You"
    var localX by remember { mutableStateOf(5.0f) }
    var localY by remember { mutableStateOf(5.0f) }
    var localMicMuted by remember { mutableStateOf(false) }

    // 2. Active Peers Simulated List
    val peers = remember {
        mutableStateListOf(
            ProximityUserSim("RenzoGracie88", "Faixa Preta", 6.5f, 5.0f),
            ProximityUserSim("SpeedyGuardPass", "Faixa Azul", 12.0f, 9.0f),
            ProximityUserSim("ToxicoChat99", "Faixa Roxa", 4.0f, 4.0f)
        )
    }

    // 3. Audio Decibel Peak Tracker for Auto Moderation
    var noiseBurstProgress by remember { mutableStateOf(65.0f) } // dB
    var autoModerationStatus by remember { mutableStateOf("SCANNING") } // "SCANNING", "PEAK_MUTED"
    var moderationCooldown by remember { mutableStateOf(0) }

    // Math calculation helper
    fun calculateVolume(dist: Float): Float {
        val maxDist = 15.0f
        val minDist = 2.0f
        if (dist <= minDist) return 1.0f
        if (dist >= maxDist) return 0.0f
        val alpha = (dist - minDist) / (maxDist - minDist)
        // Logarithmic volume decay
        val volume = 1.0f - Math.log10(1.0 + 9.0 * alpha).toFloat()
        return Math.max(0.0f, Math.min(1.0f, volume))
    }

    // Recalculate peer volumes relative to local coordinates
    peers.forEach { peer ->
        val dx = localX - peer.x
        val dy = localY - peer.y
        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        
        peer.currentVolume = if (peer.isBlocked || peer.isLocalMuted || localMicMuted) {
            0.0f
        } else {
            calculateVolume(dist)
        }

        peer.connectionStatus = when {
            peer.isBlocked -> "Bloqueado (RTC Recusado)"
            peer.isLocalMuted -> "Local Mutado"
            dist > 15.0f -> "Fora do Alcance (>15m)"
            peer.currentVolume > 0.8f -> "Canal Forte (${String.format("%.1fm", dist)})"
            else -> "Canal Suave (${String.format("%.1fm", dist)})"
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
            title = "Áudio Tridimensional por Proximidade",
            subtitle = "Sistemas WebRTC de Voz Espacial & Moderadores Automáticos de Frequência"
        )

        // Intro message
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(0.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationImportant,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VISÃO ENGENHARIA DE COMUNICAÇÕES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintTeal,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Este simulador processa as equações de decaimento acústico em tempo real. Movimente os lutadores para gerar conexões WebRTC via STUN/TURN sob demanda.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // First Section: Spatial Grid Coordinates Interface
        Text(
            text = "1. MATRIZ DE POSIÇÕES (COORDENADAS CO-PLANAR)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Local Coordinates Sliders Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Sua Posição Local (Microfone)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text("Eixo X: ${String.format("%.1f", localX)}m", fontSize = 9.sp, color = BlueprintTextSecondary)
                    Slider(
                        value = localX,
                        onValueChange = { localX = it },
                        valueRange = 0.0f..20.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = BlueprintCyan,
                            activeTrackColor = BlueprintCyan,
                            inactiveTrackColor = BlueprintGridLine
                        ),
                        modifier = Modifier.height(28.dp)
                    )

                    Text("Eixo Y: ${String.format("%.1f", localY)}m", fontSize = 9.sp, color = BlueprintTextSecondary)
                    Slider(
                        value = localY,
                        onValueChange = { localY = it },
                        valueRange = 0.0f..20.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = BlueprintCyan,
                            activeTrackColor = BlueprintCyan,
                            inactiveTrackColor = BlueprintGridLine
                        ),
                        modifier = Modifier.height(28.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Local Mic Control button
                    Button(
                        onClick = { localMicMuted = !localMicMuted },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (localMicMuted) BlueprintRed.copy(alpha = 0.3f) else Color(0xFF132D46)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().testTag("toggle_main_mic")
                    ) {
                        Icon(
                            imageVector = if (localMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (localMicMuted) BlueprintRed else BlueprintCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (localMicMuted) "MICROFONE MUTADO OSS" else "TRANSMITINDO LIVE AUDIO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (localMicMuted) BlueprintRed else BlueprintCyan
                        )
                    }
                }
            }

            // Architecture Schema / Config info Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C191E)),
                border = BorderStroke_workaround(BlueprintCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("WEBRTC STUN / TURN CONFIG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text("• STUN: stun.l.google.com:19302", fontSize = 9.sp, color = BlueprintTextPrimary, fontFamily = FontFamily.Monospace)
                    Text("• TURN: turn.jiuverse.com:3478", fontSize = 9.sp, color = BlueprintTextPrimary, fontFamily = FontFamily.Monospace)
                    Text("• Protocol: UDP Srflx/Relay Fallback", fontSize = 8.sp, color = BlueprintTextSecondary)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("EQUAÇÃO DE GANHO LOG:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                    Text(
                        text = "V = 1.0 - Log10(1 + 9 * ((d - 2) / 13))",
                        fontSize = 9.sp,
                        color = BlueprintOrange,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    )
                    Text("Corta áudio instantaneamente em d > 15m para otimizar overhead de rede e packets de UDP de canais ociosos.", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Second Section: Interactive Voice Room Peers List
        Text(
            text = "2. LUTADORES PRÓXIMOS E CONTROLADORES DE PARCEIROS (WEBRTC-PEERS)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                peers.forEach { peer ->
                    val isSilenced = peer.isBlocked || peer.isLocalMuted || localMicMuted
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (peer.currentVolume > 0f) Icons.Default.Hearing else Icons.Default.MicOff,
                                    contentDescription = null,
                                    tint = if (isSilenced) BlueprintRed else BlueprintTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(peer.nickname, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            }
                            Text(peer.connectionStatus, fontSize = 8.5.sp, color = BlueprintTextSecondary)
                        }

                        // Coordinates Control for this peer
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                            Text("Alterar X: ${String.format("%.1f", peer.x)}m", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Slider(
                                value = peer.x,
                                onValueChange = { newValue ->
                                    val idx = peers.indexOf(peer)
                                    if (idx != -1) peers[idx] = peer.copy(x = newValue)
                                },
                                valueRange = 0.0f..20.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = BlueprintTeal,
                                    activeTrackColor = BlueprintTeal
                                ),
                                modifier = Modifier.height(20.dp)
                            )
                        }

                        // Current attenuating volume read-out
                        Column(
                            modifier = Modifier.width(60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("VOLUME", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                            Text(
                                text = if (isSilenced) "0.00" else String.format("%.2f", peer.currentVolume),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (peer.currentVolume > 0.8f) BlueprintCyan else if (peer.currentVolume > 0f) BlueprintTeal else BlueprintRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Individual social controls (Mute, Block, Report)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mute Peer locally
                            Icon(
                                imageVector = if (peer.isLocalMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = "Mudar áudio",
                                tint = if (peer.isLocalMuted) BlueprintRed else BlueprintCyan,
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                    .clickable {
                                        val idx = peers.indexOf(peer)
                                        if (idx != -1) peers[idx] = peer.copy(isLocalMuted = !peer.isLocalMuted)
                                    }
                                    .padding(2.dp)
                            )

                            // Block peer from any WebRTC handshake requests
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = "Bloquear",
                                tint = if (peer.isBlocked) BlueprintRed else BlueprintTextSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                    .clickable {
                                        val idx = peers.indexOf(peer)
                                        if (idx != -1) peers[idx] = peer.copy(isBlocked = !peer.isBlocked)
                                    }
                                    .padding(2.dp)
                            )

                            // Report Abuse / Spam / Toxicity
                            Box(
                                modifier = Modifier
                                    .background(if (peer.isReported) BlueprintRed else Color(0xFF6B7280).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .clickable {
                                        val idx = peers.indexOf(peer)
                                        if (idx != -1) peers[idx] = peer.copy(isReported = true)
                                        // Activate automatic penalty mock for toxic sender
                                        if (peer.nickname == "ToxicoChat99") {
                                            autoModerationStatus = "PEAK_MUTED"
                                            noiseBurstProgress = 98.0f // Heavy frequency burst caught
                                        }
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (peer.isReported) "DENUNCIADO" else "REPORT",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (peer.isReported) Color.White else BlueprintTextPrimary
                                )
                            }
                        }
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Third Section: AI Automatic Moderation & Speech Thresholds
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3. FILTRO DE ATENUAÇÃO & AUTO-MODERAÇÃO",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = BlueprintRed, modifier = Modifier.size(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "O JiuVerse utiliza decodificadores Opus WebRTC combinados com o NestJS Gateway para prevenir abusos de áudio de forma passiva por detecção de pico decibel.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                // Realtime dynamic dB spectrum illustration
                Text(
                    text = "VOLUME DE ENTRADA DO AUDIO-STREAM: ${noiseBurstProgress.toInt()} dB",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (noiseBurstProgress > 85f) BlueprintRed else BlueprintTeal,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Signal progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(Color.Black, RoundedCornerShape(2.dp))
                        .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(2.dp))
                ) {
                    val ratio = Math.min(1.0f, noiseBurstProgress / 120f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .background(if (noiseBurstProgress > 85f) BlueprintRed else BlueprintTeal, RoundedCornerShape(2.dp))
                            .fillMaxSize()
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0 dB (Calmo)", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                    Text("85 dB (Limite Grito)", fontSize = 7.5.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                    Text("120 dB (Ruído)", fontSize = 7.5.sp, color = BlueprintRed)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons to simulate noise spike or report trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            noiseBurstProgress = 96.0f
                            autoModerationStatus = "PEAK_MUTED"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintRed.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SIMULAR RUÍDO EXTREMO (95+ dB)", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            noiseBurstProgress = 42.0f
                            autoModerationStatus = "SCANNING"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("RESETAR SINAL", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // State indicator based on auto-moderation status
                AnimatedVisibility(visible = autoModerationStatus == "PEAK_MUTED") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF3B1E28), RoundedCornerShape(4.dp))
                            .border(1.dp, BlueprintRed, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = BlueprintRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "🚨 INTERVENÇÃO AUTOMÁTICA DA REDE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintRed,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "Lutador 'ToxicoChat99' foi mutado temporariamente por 5 minutos devido a picos excedentes de 95dB ou reports ativos.",
                                fontSize = 9.sp,
                                color = BlueprintTextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Workaround Border Stroke function for clean compiling on older configurations
private fun BorderStroke_workaround(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(0.5.dp, color)
}
