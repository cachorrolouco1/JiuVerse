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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.model.BlueprintData
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

@Composable
fun PlaybookTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val runningIncidentId = viewModel.runningIncidentId.collectAsState()
    val logs = viewModel.currentLogs.collectAsState()

    // 0: Seguranca, 1: Anti-Cheat, 2: Anti-Spam, 3: Moderação
    val activeSubTab = remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Playbooks de Defesa",
            subtitle = "Protocolos de segurança, moderação e regras anti-fraude integradas"
        )

        Text(
            text = "Para um jogo persistente e social de alta acessibilidade com dezenas de milhares de usuários online ativos, as estratégias de controle defensivo devem ser integradas em todas as camadas da infra. Escolha uma área para ler os detalhes técnicos do blueprint:",
            fontSize = 12.sp,
            color = BlueprintTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Subtabs for security articles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            val subTabTitles = listOf("Segurança", "Anti-Cheat", "Anti-Spam", "Moderação")
            subTabTitles.forEachIndexed { index, title ->
                val isSelected = activeSubTab.value == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) BlueprintCard else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { activeSubTab.value = index }
                        .padding(vertical = 8.dp)
                        .testTag("playbook_subtab_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) BlueprintCyan else BlueprintTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Document display depending on playbook tab
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = BorderStrokeCustom(1.dp, BlueprintGridLine)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when (activeSubTab.value) {
                    0 -> PlaybookArticle(
                        title = "Estratégia de Segurança & Integridade",
                        icon = Icons.Default.Security,
                        color = BlueprintCyan,
                        bullets = listOf(
                            "**Autenticação JWT de Duas Partes**: Os tokens de login JWT curtos possuem expiração de 15 minutos, enquanto os refresh tokens são armazenados com restrição de IP em tabelas PostgreSQL seguras.",
                            "**Cookies HTTP-Only e SameSite**: Protege tokens contra vazamento ou ataques de XSS (Cross-Site Scripting) na Web ou injeção de WebView.",
                            "**CORS Estrito & Helmet**: Segurança agregada no NestJS rejeitando requisições de domínios maliciosos e escondendo headers que identificam o runtime de Node.js.",
                            "**Borda Cloudflare WAF**: Oferece filtros de blacklist, certificados SSL de borda criptografados e bloqueio de SQL Injection em tráfego de entrada."
                        )
                    )
                    1 -> PlaybookArticle(
                        title = "Estratégia Anti-Cheat Real-Time",
                        icon = Icons.Default.Gavel,
                        color = BlueprintOrange,
                        bullets = listOf(
                            "**Controle Autoritativo de Movimento**: O jogo se recusa a aceitar 'salto' de teletransporte direto ou cálculo de física feito exclusivamente pelo cliente. O servidor calcula colisões, atritos e velocidades permitidas por frames.",
                            "**Modulação de Delta de Velocidade**: Se uma mensagem 'move' vier com distância percorrida entre frames que desafia a velocidade máxima permitida da faixa de Jiu-Jitsu do avatar, a movimentação é rejeitada instantaneamente.",
                            "**Controle de Latência Unificado**: Servidores reconciliam pacotes de rede usando interpoladores (Lerp) de forma a neutralizar pequenas perdas de pacotes sem causar rubberbanding agressivo para avatares inocentes."
                        )
                    )
                    2 -> PlaybookArticle(
                        title = "Estratégia Anti-Spam de Chat",
                        icon = Icons.Default.SignalWifi4Bar,
                        color = BlueprintTeal,
                        bullets = listOf(
                            "**Token Bucket via Redis**: Cada personagem tem um balde virtual com capacidade para 5 mensagens de chat. Cada envio consome 1 token, que regenera continuamente (1 token a cada 1.5s). Se for esvaziado, o jogador é temporariamente banido de se comunicar no dojo.",
                            "**Fuzzy String Distance comparative (Levenshtein)**: Seta um hashing em cache das últimas 5 conversas para que se um robô tentar mandar strings muito similares com pequenas variações ortográficas, elas sejam consolidadas e bloqueadas por redundância excessiva.",
                            "**Filtro Blacklist & Regex**: Bloqueia palavras invasivas, links de golpes fraudulentos e tentativas de difamação de forma nativa."
                        )
                    )
                    3 -> PlaybookArticle(
                        title = "Estratégia de Moderação Social Ativa",
                        icon = Icons.Default.CheckCircle,
                        color = BlueprintGreen,
                        bullets = listOf(
                            "**Buffer de Voz Local**: Quando um usuário denuncia outro por abuso de chat de voz por proximidade, o app local grava os últimos 8 segundos de buffer circular do microfone dele de WebRTC e envia ao servidor como prova criptografada para auditoria.",
                            "**Mute/Ban Dinâmicos em Tempo Real**: Um gateway centralizado se encarrega de divulgar aos canais de áudio de WebRTC as ordens de mutes ou punições recebidas do painel de moderação instantaneamente.",
                            "**Shadowban Silencioso**: Contas suspeitas de spammer continuam operando normalmente nos seus telefones, porém suas mensagens não são empurradas na fila global no Redis, preservando a paz dos outros jogadores de forma silenciosa."
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Sandbox Incident Simulator ---
        SectionHeader(
            title = "Sandbox do Gateway (Simulador Ativo)",
            subtitle = "Visualize os algoritmos mitigando incidentes na vida real"
        )
        
        Text(
            text = "Selecione um cenário hipotético abaixo para disparar o sistema reativo de moderação automática do JiuVerse e ler o fluxo de logs do servidor:",
            fontSize = 12.sp,
            color = BlueprintTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Grid selection of incidents
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BlueprintData.incidents.forEach { incident ->
                val isTargetRunning = runningIncidentId.value == incident.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = incident.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BlueprintTextPrimary
                                )
                                Text(
                                    text = "Categoria: ${incident.attackType} | Mitigação: ${incident.mitigationTech}",
                                    fontSize = 11.sp,
                                    color = BlueprintCyan,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }

                            Button(
                                onClick = { viewModel.runIncidentSimulation(incident) },
                                enabled = runningIncidentId.value == null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BlueprintTeal,
                                    disabledContainerColor = BlueprintGridLine
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("run_incident_${incident.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isTargetRunning) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 1.5.dp,
                                            color = BlueprintTextPrimary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = BlueprintTextPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isTargetRunning) "SIMULANDO" else "SIMULAR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BlueprintTextPrimary
                                    )
                                }
                            }
                        }

                        Text(
                            text = incident.description,
                            fontSize = 12.sp,
                            color = BlueprintTextSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Log output console screen
        Text(
            text = "CONSOLE DE LOGS SENSORIZADO:",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF060B12)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .height(220.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = BlueprintCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (runningIncidentId.value != null) "LOGS ATIVOS: SIMULADOR DETRANSMISSÃO" else "PRONTO PARA SIMULACAO (CONSOLE IDLE)",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (logs.value.isEmpty()) {
                        Text(
                            text = "[IDLE] Pressione 'SIMULAR' em algum cenário defensivo acima para inspecionar os loops de código de mitigação contra DDoS, Speed hacks de posicionamento ou floods do JiuVerse.",
                            color = BlueprintTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        logs.value.forEach { log ->
                            Text(
                                text = log,
                                color = if (log.contains("BLOCK") || log.contains("ALERT") || log.contains("cheat") || log.contains("CHEATER") || log.contains("rejeitada") || log.contains("FRAUDE")) BlueprintRed 
                                else if (log.contains("CLOUDFLARE") || log.contains("NGINX") || log.contains("REACTION") || log.contains("MITIGADO")) BlueprintCyan 
                                else if (log.contains("BOTNET") || log.contains("SPAM") || log.contains("abusivo") || log.contains("Denunciar")) BlueprintOrange 
                                else BlueprintTextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaybookArticle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    bullets: List<String>
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintTextPrimary
            )
        }

        bullets.forEach { bullet ->
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "• ",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                // Split for bold key prefix
                if (bullet.contains("**:")) {
                    val split = bullet.split("**:")
                    val boldPart = split[0].replace("**", "")
                    val restPart = split[1]
                    Column {
                        Text(
                            text = boldPart + ":",
                            fontWeight = FontWeight.Bold,
                            color = BlueprintTextPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = restPart.trim(),
                            color = BlueprintTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                } else {
                    Text(
                        text = bullet,
                        color = BlueprintTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
