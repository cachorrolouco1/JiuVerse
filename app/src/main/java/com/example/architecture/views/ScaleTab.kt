package com.example.architecture.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.ui.theme.BlueprintGreen
import com.example.ui.theme.BlueprintGridLine
import com.example.ui.theme.BlueprintOrange
import com.example.ui.theme.BlueprintRed
import com.example.ui.theme.BlueprintTeal
import com.example.ui.theme.BlueprintTextPrimary
import com.example.ui.theme.BlueprintTextSecondary
import java.util.Locale

@Composable
fun ScaleTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val ccu = viewModel.ccuInput.collectAsState()
    val tickRate = viewModel.tickRateInput.collectAsState()
    val voipPercent = viewModel.voipActivePercent.collectAsState()

    // --- Dynamic Calculations based on architecture parameters ---
    // Sockets payload coordinates ~ 128 bytes
    val coordinatesPayloadBytes = 128

    // Active sockets msgs/sec
    val socketMsgsSec = ccu.value.toLong() * tickRate.value

    // Positions Traffic: CCU * TickRate * 128 bytes/sec
    val positioningBps = socketMsgsSec * coordinatesPayloadBytes
    val positioningMbps = (positioningBps * 8) / (1000 * 1000.0) // bits to Megabits/s

    // VoIP Bandwidth: 25% of CCU are talking, Opus codec is ~32kbps
    // If on average they hear 3 other people in proximity: 32kbps * 3 = 96kbps per listener.
    val activeTalkers = (ccu.value * (voipPercent.value / 100.0)).toInt()
    val voipKbpsPerTalker = 32
    val voipBps = activeTalkers.toLong() * voipKbpsPerTalker * 1000
    val voipMbps = voipBps / (1000 * 1000.0)

    val totalBandwidthGbps = (positioningMbps + voipMbps) / 1000.0

    // Number of NestJS instances needed (1 instance handles max 10k connections stabilized)
    val podsNeeded = Math.ceil(ccu.value / 10000.0).toInt()

    // Redis PUB/SUB bandwidth overhead
    val redisPubSubOps = socketMsgsSec

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Simulador de Alta Escala",
            subtitle = "Calcule largura de banda, concorrência e pods de container para 100k usuários"
        )

        Text(
            text = "Escale o ecossistema do jogo JiuVerse simulando cargas extremas de concorrência ativa (CCU). Arraste os controles técnicos abaixo e observe o impacto matemático em servidores Kubernetes, Redis e canais de WebRTC:",
            fontSize = 12.sp,
            color = BlueprintTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // --- Interactive Controls Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CONTROLES DE ENGENHARIA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                // CCU Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Usuários Simultâneos (CCU):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintTextPrimary
                    )
                    Text(
                        text = String.format("%,d", ccu.value),
                        fontSize = 14.sp,
                        color = BlueprintCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = ccu.value.toFloat(),
                    onValueChange = { viewModel.updateCCU(it.toInt()) },
                    valueRange = 1000f..150000f,
                    modifier = Modifier.testTag("ccu_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tick Rate Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Frequência de Rede (Dojo Tickrate):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintTextPrimary
                    )
                    Text(
                        text = "${tickRate.value} Hz (Pacotes/seg)",
                        fontSize = 14.sp,
                        color = BlueprintCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = tickRate.value.toFloat(),
                    onValueChange = { viewModel.updateTickRate(it.toInt()) },
                    valueRange = 5f..30f,
                    modifier = Modifier.testTag("tickrate_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Voip active talkers Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jogadores no canal de Voz ativo:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintTextPrimary
                    )
                    Text(
                        text = "${voipPercent.value}% do CCU",
                        fontSize = 14.sp,
                        color = BlueprintCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = voipPercent.value.toFloat(),
                    onValueChange = { viewModel.updateVoipPercent(it.toInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.testTag("voip_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Math Results Dashboard ---
        SectionHeader(
            title = "Cálculo de Infraestrutura Recomendada",
            subtitle = "Demandas computacionais estimadas em tempo de execução"
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Outbound traffic
            MetricCard(
                title = "Largura de Banda de Saída Global (Outbound)",
                value = String.format(Locale.getDefault(), "%.2f Gbps", totalBandwidthGbps),
                desc = "Posicionamento coordinates (~${String.format(Locale.getDefault(), "%.1f", positioningMbps)} Mbps) + VoIP por proximidade (~${String.format(Locale.getDefault(), "%.1f", voipMbps)} Mbps)",
                color = if (totalBandwidthGbps > 4.0) BlueprintRed else if (totalBandwidthGbps > 2.0) BlueprintOrange else BlueprintGreen
            )

            // Redis Pub/Sub stress of state
            MetricCard(
                title = "Operações por Segundo no Redis Cluster (Pub/Sub)",
                value = String.format(Locale.getDefault(), "%,d Ops/s", redisPubSubOps),
                desc = "Cada movimento publicado na sala Socket.IO é propagado no Redis Adapter para servidores NestJS redundantes.",
                color = if (redisPubSubOps > 1000000) BlueprintRed else if (redisPubSubOps > 300000) BlueprintOrange else BlueprintGreen
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Docker containers needed
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        title = "Pods NestJS Mínimos",
                        value = "$podsNeeded réplicas",
                        desc = "Carga estável recomendada de 10.000 sockets concorrentes por vCPU.",
                        color = BlueprintCyan
                    )
                }
                
                // Active Voice Streams
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        title = "Canais WebRTC Ativos",
                        value = String.format(Locale.getDefault(), "%,d conexões", activeTalkers),
                        desc = "Fluxos de áudio Opus transportados localmente e roteados se houver TURN ativo.",
                        color = BlueprintTeal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Architecture Advice System ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStrokeCustom() // Custom border for highlighting warning if scale is critical
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = if (ccu.value >= 100000) Icons.Default.Warning else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (ccu.value >= 100000) BlueprintOrange else BlueprintCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "RECOMENDAÇÃO TÉCNICA DE INFRAESTRUTURA:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (ccu.value >= 100000) BlueprintOrange else BlueprintCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getScaleAdviceText(ccu.value, tickRate.value, totalBandwidthGbps, podsNeeded),
                        fontSize = 12.sp,
                        color = BlueprintTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    desc: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlueprintCard),
        border = BorderStrokeCustom(1.dp, BlueprintGridLine)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color, RoundedCornerShape(3.dp))
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                desc,
                fontSize = 11.sp,
                color = BlueprintTextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun BorderStrokeCustom(width: androidx.compose.ui.unit.Dp = 1.dp, color: Color = BlueprintGridLine): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(width, color)
}

fun getScaleAdviceText(ccu: Int, tickRate: Int, bandwidth: Double, pods: Int): String {
    return when {
        ccu >= 100000 -> {
            "ESTADO CRÍTICO DE ESCALABILIDADE (100k+ CCU): A essa vazão, o PostgreSQL sofrerá exaustão de conexões se gravar a cada tick de rede. Recomendamos salvar coordenadas X,Y em buffer Redis in-memory e descarregar no Postgres final (Batch synchronization) somente a cada 5 minutos, ou quando o avatar mudar de dojo. Configurar Kubernetes HPA com pods limites de NestJS limitados por utilização de rede."
        }
        bandwidth > 2.5 -> {
            "ALTA CONSUMO DE BANDA (>2.5 Gbps): Os fluxos de áudio Opus WebRTC começam a entupir o tráfego se mantidos em topologia Mesh pura de proximidade. Forçar o corte automático de conexões de áudio para quem estiver com distância maior que 40 tiles. Habilitar compactação GZip ou binária (Protobuf/MessagePack) para mensagens de sincronia Socket.IO."
        }
        pods > 6 -> {
            "ESCALONAMENTO DE CONTAINERS: Para lidar estável com $pods réplicas de NestJS, recomendamos ancorar as sessões WebSockets no proxy por meio de persistência Nginx Sticky Cookie. Use múltiplos servidores Redis Cluster com adaptadores separados para evitar gargalo de processamento em single-process do Redis."
        }
        else -> {
            "ESCALABILIDADE SAUDÁVEL E ESTÁVEL: Seus servidores NestJS em ambiente Docker básico dão conta de sustentar o jogo tranquilamente. Use PM2 Cluster local em modo 'max_instances' para elevar ao máximo as vCPUs disponíveis."
        }
    }
}
