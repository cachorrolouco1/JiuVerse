package com.example.architecture.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlueprintCard
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintGridLine
import com.example.ui.theme.BlueprintOrange
import com.example.ui.theme.BlueprintTeal
import com.example.ui.theme.BlueprintTextPrimary
import com.example.ui.theme.BlueprintTextSecondary

data class ArchitectureNode(
    val id: String,
    val title: String,
    val layer: String,
    val tech: String,
    val icon: ImageVector,
    val activeColor: Color,
    val details: String,
    val productionKey: String
)

@Composable
fun OverviewTab(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    
    val nodes = remember {
        listOf(
            ArchitectureNode(
                id = "client",
                title = "1. Expo Client (Android/iOS/Web)",
                layer = "Frontend Layer",
                tech = "React Native + Expo Router + Socket.IO Client + WebRTC API",
                icon = Icons.Default.Navigation,
                activeColor = BlueprintCyan,
                productionKey = "Porta: 80 / 443 (HTTP/S & WS)",
                details = "O aplicativo final dos jogadores de JiuVerse roda unificado via Expo. Ele renderiza a interface do dojo em matriz bidimensional isométrica (DojoGrid), rastreia toques para caminhar, controla áudio do microfone via WebRTC Mesh, e atualiza posições autoritativas a 10Hz via WebSockets."
            ),
            ArchitectureNode(
                id = "cloudflare",
                title = "2. Cloudflare Edge CDN",
                layer = "Security & Network Delivery",
                tech = "Cloudflare WAF / DDoS Mitigation + DNS Anycast",
                icon = Icons.Default.SettingsInputComponent,
                activeColor = BlueprintOrange,
                productionKey = "SSL / Proxy Borda Habilitado",
                details = "Primeira linha de defesa global para mitigar ataques volumétricos de DDoS. Filtra pacotes na borda usando regras WAF e reverte proxies WebSocket mantendo conexões persistentes de alto desempenho. Fornece criptografia SSL/TLS e reduz latência de assets estáticos."
            ),
            ArchitectureNode(
                id = "nginx",
                title = "3. Nginx Reverse Proxy (Sticky Session)",
                layer = "Gateway & Routing Engine",
                tech = "Nginx Proxy Cache + CertBot SSL (HTTP/2)",
                icon = Icons.Default.Info,
                activeColor = BlueprintTeal,
                productionKey = "Sticky Session (ip_hash) habilitado",
                details = "Distribui conexões real-time. Como o Socket.IO usa multiplexação de protocolo HTTP Polling secundário antes do WebSocket se elevar, é obrigatório Nginx usar balanceamento 'ip_hash' para ancorar o cliente na mesma réplica de servidor NestJS onde ele iniciou seu handshake."
            ),
            ArchitectureNode(
                id = "nestjs",
                title = "4. NestJS Server Cluster (Gateway WebSocket)",
                layer = "Backend Application Core",
                tech = "Node.js (NestJS) + @nestjs/websockets + Socket.IO server",
                icon = Icons.Default.SettingsInputComponent,
                activeColor = BlueprintCyan,
                productionKey = "Escalonado via PM2 Cluster / Docker",
                details = "Coração lógico do JiuVerse MMORPG. Sobe endpoints de REST API de login/cadastro de personagens e instâncias de Gateways de Socket.IO autocontidos. Valida movimentos de speed-hack no gateway e envia atualizações com buffers LERP interpolados no cliente."
            ),
            ArchitectureNode(
                id = "coturn",
                title = "5. COTURN VoIP Server (STUN/TURN)",
                layer = "Real-Time Comms Voice Gateway",
                tech = "Coturn Service + WebRTC PeerConnections",
                icon = Icons.Default.Mic,
                activeColor = BlueprintOrange,
                productionKey = "UDP / TCP Ports: 3478, 5349",
                details = "Para o chat por voz por proximidade WebRTC. Garante que se dois avatares estiverem atrás de NATs simétricos corporativos ou de redes móveis (difícil conexão direta), o áudio trafegue de forma criptografada usando canalizador de TURN Server dedicado de alta vazão de banda UDP."
            ),
            ArchitectureNode(
                id = "redis",
                title = "6. Redis Pub/Sub Cluster (State Adapter)",
                layer = "Memory Data & Sync Layer",
                tech = "Redis Memory Cache + Sockets Redis Adapter",
                icon = Icons.Default.Storage,
                activeColor = BlueprintTeal,
                productionKey = "Redis adaptador in-memory caching",
                details = "Resolve o gargalo de sincronia: se 100.000 jogadores movimentarem-se, o Redis sincroniza canais de dojos entre múltiplas réplicas de NestJS sem precisar encostar no Postgres. Salva as posições temporárias X,Y instantâneas com expiração rápida."
            ),
            ArchitectureNode(
                id = "postgres",
                title = "7. PostgreSQL Database (Prisma ORM)",
                layer = "Relational Database Storage",
                tech = "PostgreSQL DB + Prisma Client Engine",
                icon = Icons.Default.Storage,
                activeColor = BlueprintCyan,
                productionKey = "Persistência transacional com isolamento SERIALIZABLE",
                details = "Banco persistente final. Gerencia o inventário, academias decoradas, transações P2P do marketplace, pontuações de faixa de jiu-jitsu (XP/Level) e conquistas. Protegido por restrições ACID e isolamentos pessimistas para travar duplicações de exploits de móveis."
            )
        )
    }

    val selectedNodeId = remember { mutableStateOf("client") }
    val activeNode = remember(selectedNodeId.value) {
        nodes.firstOrNull { it.id == selectedNodeId.value } ?: nodes.first()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top
    ) {
        SectionHeader(
            title = "Arquitetura JiuVerse",
            subtitle = "Sinfonia completa de alto nível com fluxo de controle em tempo real"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Text instruction
        Text(
            text = "Toque em qualquer nó do diagrama técnico para explorar seu papel e especificações físicas:",
            fontSize = 12.sp,
            color = BlueprintTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Visual Interlocking Network Flow
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            nodes.forEachIndexed { index, node ->
                val isSelected = selectedNodeId.value == node.id
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) node.activeColor else BlueprintGridLine,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedNodeId.value = node.id }
                        .testTag("architecture_node_${node.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF14243B) else BlueprintCard
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = if (isSelected) node.activeColor.copy(alpha = 0.2f) else BlueprintGridLine,
                                        shape = RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = node.icon,
                                    contentDescription = node.title,
                                    tint = if (isSelected) node.activeColor else BlueprintTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = node.title,
                                    color = BlueprintTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = node.layer,
                                    color = if (isSelected) node.activeColor else BlueprintTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Down arrow indicating real-time connections flow
                if (index < nodes.size - 1) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Conecta com",
                        tint = BlueprintGridLine,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic inspected details drawer card
        SectionHeader(
            title = "Detalhes de Produção",
            subtitle = "Especificações técnicas do nó do ecossistema selecionado"
        )

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeNode.activeColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(activeNode.activeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = activeNode.icon,
                                contentDescription = null,
                                tint = activeNode.activeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = activeNode.title,
                                color = BlueprintTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeNode.tech,
                                color = activeNode.activeColor,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = activeNode.details,
                        fontSize = 13.sp,
                        color = BlueprintTextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C1420), RoundedCornerShape(4.dp))
                            .border(1.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "MÉTRICA DE VALIDAÇÃO / PRODUÇÃO:",
                                color = activeNode.activeColor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeNode.productionKey,
                                color = BlueprintTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
