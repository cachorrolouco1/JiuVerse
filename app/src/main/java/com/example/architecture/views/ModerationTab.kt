package com.example.architecture.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.*

// Data Models for moderation simulation
data class ModerationLog(
    val timestamp: String,
    val source: String,
    val category: String, // "SPAM", "BOT", "TOXIC_WORD", "MALICIOUS_LINK", "REPORT_ESCALATION"
    val severity: String, // "INFO", "WARNING", "CRITICAL", "ESCALATED"
    val message: String
)

data class EvidenceFile(
    val reporter: String,
    val suspect: String,
    val reason: String,
    val weight: Int, // Toxicity scale score
    val location: String, // JiuVerse Coordinates
    val timestamp: String,
    val dialogHistory: List<String>,
    val botHeuristics: String = "Normal"
)

data class PenalizedUser(
    val username: String,
    val penaltyType: String, // "SILENCIADO", "BANIDO"
    val remainingTime: String,
    val reason: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModerationTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Trust & Safety Settings State
    var textFilterActive by remember { mutableStateOf(true) }
    var antiSpamActive by remember { mutableStateOf(true) }
    var botSensorSensitivity by remember { mutableStateOf("MÉDIA Heurística") } // "DESATIVADA", "MÉDIA Heurística", "AGRESSIVA CAPTCHA"
    var linkBlockerActive by remember { mutableStateOf(true) }
    var autoEscalationLevel by remember { mutableStateOf(2) } // Escalation threshold out of 5

    // Word Filters DB
    val bannedWords = listOf("muleque", "imbecil", "trapaceiro", "hacker", "lixo", "noob")
    var testUserText by remember { mutableStateOf("") }
    var testUserResultText by remember { mutableStateOf("") }
    var spamCountSim by remember { mutableStateOf(0) }
    var customAlertMessage by remember { mutableStateOf("") }

    // Evidence & Reporting DB
    val reportsList = remember {
        mutableStateListOf(
            EvidenceFile(
                reporter = "FaixaPreta99",
                suspect = "LigeirinhoBot",
                reason = "Comportamento automatizado e click-macro para farmar JiCoins síncronas",
                weight = 80,
                location = "Dojo Central Isometrico [X: 1420, Y: 890]",
                timestamp = "16:01:10",
                dialogHistory = listOf("[SISTEMA]: Jogador clicou no NPC 150 vezes por segundo"),
                botHeuristics = "Cliques repetitivos idênticos a 0.001ms de dispersão (Anomalia Heurística detectada)"
            ),
            EvidenceFile(
                reporter = "Guardeiro_BR",
                suspect = "FreeGems_Lover",
                reason = "Divulgação de link malicioso de hack e gemas infinitas falsas",
                weight = 95,
                location = "Praça do QG Principal [X: 450, Y: 320]",
                timestamp = "16:02:40",
                dialogHistory = listOf(
                    "FreeGems_Lover: Acessem jiuverse-hack-freegems.com e dupliquem suas moedas agora, total grátis!",
                    "Guardeiro_BR: Sai daqui bot sem vergonha"
                ),
                botHeuristics = "URL suspeita suspeita sem certificado SSL, domínio listado na blacklist cloud"
            ),
            EvidenceFile(
                reporter = "Mestre_Luta",
                suspect = "ToxicBooster",
                reason = "Ofensas verbais repetitivas pós luta de Grand Prix",
                weight = 65,
                location = "Sala de Sparring Arena B [X: 2310, Y: 110]",
                timestamp = "16:04:15",
                dialogHistory = listOf(
                    "ToxicBooster: Voce e um lixo de lutador, um tremendo imbecil trapaceiro!",
                    "Mestre_Luta: Respeite os princípios do tatame do esporte."
                )
            )
        )
    }

    var selectedEvidenceIndex by remember { mutableStateOf(0) }

    // Active Penalized Users
    val penalizedUsers = remember {
        mutableStateListOf(
            PenalizedUser("SpamMasterX", "SILENCIADO", "45 min", "Inundar chat do tatame com spam síncrono"),
            PenalizedUser("PirataDojo", "BANIDO", "Permanente", "Ataque de bots automatizados de bypass"),
            PenalizedUser("FuraPapo", "SILENCIADO", "2 horas", "Tenta burlar sistema de voz de proximidade")
        )
    }

    // Complete audit logs stream
    val systemLogs = remember {
        mutableStateListOf(
            ModerationLog("16:00:10", "Filtro de Palavras", "TOXIC_WORD", "INFO", "Sessão iniciada na versão de sandbox estável."),
            ModerationLog("16:01:10", "Anti-Bot Heuristics", "BOT", "WARNING", "Suspeita de macro na conta 'LigeirinhoBot' - Gatilho 1"),
            ModerationLog("16:02:40", "Anti-Malicious Links", "MALICIOUS_LINK", "CRITICAL", "Link bloqueado enviado por 'FreeGems_Lover': jiuverse-hack-freegems.com"),
            ModerationLog("16:03:00", "Moderador Automático", "REPORT_ESCALATION", "ESCALATED", "Incidente 'FreeGems_Lover' escalado automaticamente para TIER 3 (Risco de Furto de Conta)"),
            ModerationLog("16:04:15", "NLP Chat Classifier", "TOXIC_WORD", "WARNING", "Palavras ofensivas ('lixo', 'imbecil') detectadas em 'ToxicBooster' - Ofensa registrada no log de evidências.")
        )
    }

    // Handler to process/resolve action on report
    val handleResolveReport = { outcome: String ->
        if (reportsList.isNotEmpty() && selectedEvidenceIndex in reportsList.indices) {
            val resolvedItem = reportsList[selectedEvidenceIndex]
            
            // Log resolving
            systemLogs.add(
                ModerationLog(
                    timestamp = "16:05:00",
                    source = "Painel Moderador",
                    category = "REPORT_ESCALATION",
                    severity = "INFO",
                    message = "Caso suspeito '${resolvedItem.suspect}' resolvido com decisão: [${outcome.uppercase()}] por Moderador Humano."
                )
            )

            // Add penalization if applied
            if (outcome == "silenciar") {
                penalizedUsers.add(PenalizedUser(resolvedItem.suspect, "SILENCIADO", "11 Horas", resolvedItem.reason))
            } else if (outcome == "banir") {
                penalizedUsers.add(PenalizedUser(resolvedItem.suspect, "BANIDO", "Permanente / IP", resolvedItem.reason))
            }

            // Remove reported user from queue
            reportsList.removeAt(selectedEvidenceIndex)
            
            // Adjust pointer index
            selectedEvidenceIndex = if (reportsList.isNotEmpty()) 0 else -1
            customAlertMessage = "Alerta: Sanção de [${outcome.uppercase()}] aplicada imediatamente e sincronizada com barramento de segurança!"
        }
    }

    // Interactive simulator trigger logic
    val handleSimulatorSubmit = {
        var cleanText = testUserText
        var flagged = false
        var logCategory = "TOXIC_WORD"
        var logSeverity = "INFO"
        var logMessage = "Texto limpo enviado com sucesso."

        // 1. Anti Spam / Flood Check
        if (antiSpamActive && testUserText.trim().isNotEmpty()) {
            spamCountSim += 1
            if (spamCountSim >= 3) {
                flagged = true
                cleanText = "[BLOQUEADO POR SPAM: Aguarde 5 segundos antes de digitar de novo]"
                logCategory = "SPAM"
                logSeverity = "WARNING"
                logMessage = "Bloqueio automático por flooding síncrono ativado no canal."
            }
        }

        // 2. Link Blocker Check
        if (linkBlockerActive && !flagged) {
            val urlsKeywords = listOf(".com", ".net", ".xyz", "http:", "https:", "www.")
            if (urlsKeywords.any { testUserText.contains(it, ignoreCase = true) }) {
                flagged = true
                cleanText = "[LINK BLOQUEADO PARA SEGURANÇA DETECTADO PELO JIUVERSE]"
                logCategory = "MALICIOUS_LINK"
                logSeverity = "CRITICAL"
                logMessage = "Domínio não confiável detectado no input do chat: $testUserText"
            }
        }

        // 3. Word Filter Logic
        if (textFilterActive && !flagged) {
            var wordsFound = false
            bannedWords.forEach { word ->
                if (cleanText.contains(word, ignoreCase = true)) {
                    val stars = "*".repeat(word.length)
                    cleanText = cleanText.replace(word, stars, ignoreCase = true)
                    wordsFound = true
                }
            }
            if (wordsFound) {
                logCategory = "TOXIC_WORD"
                logSeverity = "WARNING"
                logMessage = "Gatilho de palavras proibidas atenuado automaticamente."
            }
        }

        testUserResultText = cleanText
        // Add log
        val now = "16:05:${(10..59).random()}"
        systemLogs.add(ModerationLog(now, "Jogador Simulador", logCategory, logSeverity, logMessage))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Arquitetura Trust & Safety de JiuVerse",
            subtitle = "Sistemas Integrados de Moderação, Filtro de Ofensas, Anti-Spam e Auditoria Heurística contra Bots"
        )

        // General Statement on Roblox-style dynamic moderation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(0.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VISÃO DIRETOR DE CONFIANÇA & CONFORMIDADE (TRUST & SAFETY)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintTeal,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "No JiuVerse, o ambiente seguro incentiva interações saudáveis nos dojós digitais. Empregamos um sistema híbrido: moderação algorítmica reativa na GPU, auditoria de telemetria espacial e escalonamento automatizado de evidências para o board.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large 2-column layout. Column Left: Active settings and live simulator, Column Right: Evidence locker & report resolve
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            
            // Left Half: Settings controls & interactive tests
            Column(modifier = Modifier.weight(1f)) {
                
                // Card 1: Moderator General Cockpit (Parametrização)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CONFIGURAÇÃO DOS SENSORES ATIVOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Toggle row 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Filtro de Palavras Proibidas", fontSize = 9.sp, color = BlueprintTextPrimary)
                            Switch(
                                checked = textFilterActive,
                                onCheckedChange = { textFilterActive = it },
                                modifier = Modifier.scale_workaround(0.7f)
                            )
                        }

                        // Toggle row 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Prevenção de Spam & Flood", fontSize = 9.sp, color = BlueprintTextPrimary)
                            Switch(
                                checked = antiSpamActive,
                                onCheckedChange = { 
                                    antiSpamActive = it
                                    if (!it) spamCountSim = 0
                                },
                                modifier = Modifier.scale_workaround(0.7f)
                            )
                        }

                        // Toggle row 3
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Filtro de Links Maliciosos", fontSize = 9.sp, color = BlueprintTextPrimary)
                            Switch(
                                checked = linkBlockerActive,
                                onCheckedChange = { linkBlockerActive = it },
                                modifier = Modifier.scale_workaround(0.7f)
                            )
                        }

                        Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                        // Trigger rate levels
                        Text("Sensibilidade Anti-Bot:", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("NENHUM", "MÉDIA Heurística", "CAPTCHA AGRESSIVA").forEach { sens ->
                                val active = botSensorSensitivity == sens
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (active) BlueprintTeal.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                        .border(0.5.dp, if (active) BlueprintTeal else BlueprintGridLine, RoundedCornerShape(4.dp))
                                        .clickable { botSensorSensitivity = sens }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(sens, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = if (active) BlueprintTeal else BlueprintTextSecondary)
                                }
                            }
                        }

                        // Escalation setting slider simulation
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Limite de Escalonamento Automático:", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            Text("TIER $autoEscalationLevel / 5", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (1..5).forEach { tier ->
                                val active = autoEscalationLevel == tier
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (active) BlueprintOrange.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                        .border(0.5.dp, if (active) BlueprintOrange else BlueprintGridLine, RoundedCornerShape(4.dp))
                                        .clickable { autoEscalationLevel = tier }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("T$tier", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (active) BlueprintOrange else BlueprintTextSecondary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Card 2: Interactive Sandbox chat & spam simulator to check features LIVE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("SIMULADOR DE PAYLOAD & FLOOD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                        Text("Dígite payloads para testar a contenção algorítmica:", fontSize = 8.sp, color = BlueprintTextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Sim fields
                        OutlinedTextField(
                            value = testUserText,
                            onValueChange = { testUserText = it },
                            placeholder = { Text("Ex: Meus imbecis joguem www.gratisgems.xyz!", fontSize = 9.sp, color = BlueprintTextSecondary) },
                            textStyle = TextStyle(fontSize = 10.sp, color = BlueprintTextPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick trigger buttons to speed up demo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { 
                                    testUserText = "Você é um imbecil trapaceiro" 
                                    handleSimulatorSubmit()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Gatilho Toxic", fontSize = 7.5.sp, color = Color.White)
                            }

                            Button(
                                onClick = { 
                                    testUserText = "Visitem jiuverse-gratis-moedas.net"
                                    handleSimulatorSubmit()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1.1f)
                            ) {
                                Text("Gatilho Link", fontSize = 7.5.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Simulator action trigger
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Simula Flood: ", fontSize = 8.sp, color = BlueprintTextSecondary)
                                Text("$spamCountSim/3", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (spamCountSim >= 3) BlueprintRed else BlueprintTeal, fontFamily = FontFamily.Monospace)
                                if (spamCountSim > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("(Limpar)", fontSize = 7.sp, color = BlueprintOrange, modifier = Modifier.clickable { spamCountSim = 0 })
                                }
                            }

                            Button(
                                onClick = { handleSimulatorSubmit() },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("ENVIAR NO CONTEXTO", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Result
                        if (testUserResultText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("PROCESSO DE SAÍDA DO CHAT:", fontSize = 7.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                                    Text(testUserResultText, fontSize = 9.sp, color = BlueprintTeal, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }

            // Right Half: Reports & Active Evidence locker review
            Column(modifier = Modifier.weight(1f)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("FILA DE DENÚNCIAS SUSPEITAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            Box(
                                modifier = Modifier
                                    .background(BlueprintRed.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("${reportsList.size} CASOS", fontSize = 7.5.sp, color = BlueprintRed, fontWeight = FontWeight.Black)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        if (reportsList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(290.dp)
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(38.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("TATAMES 100% LIMPOS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                                    Text("Nenhuma denúncia pendente.", fontSize = 9.sp, color = BlueprintTextSecondary)
                                }
                            }
                        } else {
                            // Render queue
                            reportsList.forEachIndexed { idx, rep ->
                                val isSelected = selectedEvidenceIndex == idx
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(if (isSelected) BlueprintCyan.copy(alpha = 0.15f) else Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                                        .clickable { selectedEvidenceIndex = idx }
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("@${rep.suspect}", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                        Text("Motivo: ${rep.reason.take(30)}...", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (rep.weight >= 80) BlueprintRed.copy(alpha = 0.15f) else BlueprintOrange.copy(alpha = 0.15f),
                                                    RoundedCornerShape(2.dp)
                                                )
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("SCORE ${rep.weight}%", fontSize = 7.sp, color = if (rep.weight >= 80) BlueprintRed else BlueprintOrange, fontWeight = FontWeight.Black)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = BlueprintTextSecondary, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            // Show Evidence locker details of currently selected
                            if (selectedEvidenceIndex in reportsList.indices) {
                                val activeReport = reportsList[selectedEvidenceIndex]
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("GABINETE DE EVIDÊNCIAS & TELEMETRIA", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black, RoundedCornerShape(4.dp))
                                        .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text("REPORTER: @${activeReport.reporter} | UTC: ${activeReport.timestamp}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                        Text("POSIÇÃO: ${activeReport.location}", fontSize = 7.5.sp, color = BlueprintCyan, fontFamily = FontFamily.Monospace)
                                        Text("COMPLEMENTO HEURÍSTICO: ${activeReport.botHeuristics}", fontSize = 7.5.sp, color = BlueprintOrange)
                                        
                                        Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                                        Text("TELEMETRIA DIÁLOGOS NO CHAT:", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                        activeReport.dialogHistory.forEach { sentence ->
                                            Text(sentence, fontSize = 8.sp, color = BlueprintTextSecondary, lineHeight = 10.sp)
                                        }
                                    }
                                }

                                // Interactive sanction applying buttons
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Ação do Moderador de JiuVerse:", fontSize = 8.sp, color = BlueprintTextSecondary)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = { handleResolveReport("advertir") },
                                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text("ADVERTIR", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { handleResolveReport("silenciar") },
                                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintOrange),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text("SILENCIAR", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { handleResolveReport("banir") },
                                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintRed),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text("BANIR!", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Feedback Banner
        AnimatedVisibility(visible = customAlertMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlueprintRed.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .border(0.5.dp, BlueprintOrange, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(customAlertMessage, fontSize = 9.sp, color = BlueprintTextPrimary)
                Text(
                    "CONCLUÍDO",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintOrange,
                    modifier = Modifier.clickable { customAlertMessage = "" }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System 2: Active Punitive Records on IP/Network
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2. HISTÓRICO DE BANIMENTOS E SILENCIAMENTO ATIVOS (PUNITIVE SYSTEM)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = BlueprintRed, modifier = Modifier.size(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                penalizedUsers.forEach { badUser ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        if (badUser.penaltyType == "BANIDO") BlueprintRed.copy(alpha = 0.15f) else BlueprintOrange.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (badUser.penaltyType == "BANIDO") Icons.Default.Lock else Icons.Default.HeadsetMic,
                                    contentDescription = null,
                                    tint = if (badUser.penaltyType == "BANIDO") BlueprintRed else BlueprintOrange,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("@${badUser.username}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (badUser.penaltyType == "BANIDO") BlueprintRed.copy(alpha = 0.1f) else BlueprintOrange.copy(alpha = 0.1f),
                                                RoundedCornerShape(2.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(badUser.penaltyType, fontSize = 7.sp, fontWeight = FontWeight.Black, color = if (badUser.penaltyType == "BANIDO") BlueprintRed else BlueprintOrange)
                                    }
                                }
                                Text("Motivo: ${badUser.reason}", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("DURAÇÃO", fontSize = 6.5.sp, color = BlueprintTextSecondary)
                            Text(badUser.remainingTime, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System 3: Realtime SysLogs Audit complete panel
        Text(
            text = "3. REGISTROS DE SEGURANÇA E AUDITORIA COMPLETA (SYSLOGS AUDIT)",
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
                // Column Header
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.2f)).padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TIMESTAMP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1f))
                    Text("CANAL / PROCESSO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1.5f))
                    Text("CATEGORIA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1.2f))
                    Text("MENSAGEM REGISTRADA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(4f))
                }

                Column(
                    modifier = Modifier.height(180.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Reverse list to show newest on top
                    systemLogs.reversed().forEach { log ->
                        val textSeverityColor = when(log.severity) {
                            "CRITICAL" -> BlueprintRed
                            "WARNING" -> BlueprintOrange
                            "ESCALATED" -> Color(0xFFA855F7)
                            else -> BlueprintTeal
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(log.timestamp, fontSize = 8.sp, color = BlueprintTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Text(log.source, fontSize = 8.sp, color = BlueprintTextPrimary, modifier = Modifier.weight(1.5f))
                            
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .background(textSeverityColor.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = log.category,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textSeverityColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Text(
                                text = log.message,
                                fontSize = 8.sp,
                                color = BlueprintTextSecondary,
                                modifier = Modifier.weight(4f).padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// scale modifier workaround for switches
private fun Modifier.scale_workaround(scale: Float): Modifier {
    return this.size((48 * scale).dp, (24 * scale).dp)
}
