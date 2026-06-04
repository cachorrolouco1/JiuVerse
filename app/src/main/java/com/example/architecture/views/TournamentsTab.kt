package com.example.architecture.views

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.*

// Data structures for bracket simulations
data class TournamentParticipant(
    val name: String,
    val academy: String,
    val powerRank: Int,
    var status: String = "ATIVO" // "ATIVO", "ELIMINADO"
)

data class TournamentMatch(
    val id: String,
    val round: String, // "QUARTAS", "SEMI", "FINAL"
    val playerA: TournamentParticipant,
    val playerB: TournamentParticipant,
    var scoreA: Int = 0,
    var scoreB: Int = 0,
    var winner: String = "" // Player Name
)

data class RankedCompetitor(
    val rankingPos: Int,
    val teamName: String,
    val winRatio: String,
    val lpScore: Int, // League Points
    val tierIcon: String
)

@Composable
fun TournamentsTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // 1. Interactive States
    var selectedFormatClass by remember { mutableStateOf("1X1") } // "1X1", "2X2", "5X5", "ACADEMIAS"
    var bracketTypeSelected by remember { mutableStateOf("ELIMINACAO_SIMPLES") } // "ELIMINACAO_SIMPLES", "DUPLA_ELIMINACAO"
    var selectedSeasonFilter by remember { mutableStateOf("SEASON_I") } // "SEASON_I", "OFF_SEASON"

    // Bracket execution status simulator
    var currentMatchWinnerAnnouncement by remember { mutableStateOf("") }
    var matchesPlayedCounter by remember { mutableStateOf(0) }

    // Hardcoded pool representing participants
    val participants = remember {
        listOf(
            TournamentParticipant("Rickson_99", "Gracie Barra", 98),
            TournamentParticipant("Galvao_Fan", "Atos Dojo", 92),
            TournamentParticipant("Marcelo_Guardeiro", "Alliance", 95),
            TournamentParticipant("Leandro_Speed", "Cicero Costha", 94),
            TournamentParticipant("Cobrinha_Air", "Alliance Dojo", 93),
            TournamentParticipant("Buchecha_Heavy", "Checkmat Barra", 97),
            TournamentParticipant("Nicholas_Meregali", "DreamArt Estilo", 96),
            TournamentParticipant("Miyao_Bros", "Unity JiuJitsu", 91)
        )
    }

    // Bracket Tree structure simulation based on current configuration
    val bracketMatches = remember {
        mutableStateListOf(
            TournamentMatch("m1", "QUARTAS DE FINAL", participants[0], participants[1], 4, 2, "Rickson_99"),
            TournamentMatch("m2", "QUARTAS DE FINAL", participants[2], participants[3], 0, 3, "Leandro_Speed"),
            TournamentMatch("m3", "QUARTAS DE FINAL", participants[4], participants[5], 2, 8, "Buchecha_Heavy"),
            TournamentMatch("m4", "QUARTAS DE FINAL", participants[6], participants[7], 6, 0, "Nicholas_Meregali"),
            
            // Semifinals (Simulated initial)
            TournamentMatch("m5", "SEMIFINAIS", participants[0], participants[3], 0, 0, ""),
            TournamentMatch("m6", "SEMIFINAIS", participants[5], participants[6], 0, 0, ""),

            // Grand Final
            TournamentMatch("m7", "GRANDE FINAL", participants[0], participants[5], 0, 0, "") // Placeholder
        )
    }

    val handleAdvancementSimulation = { matchId: String, winnerName: String ->
        val targetMatch = bracketMatches.find { it.id == matchId }
        if (targetMatch != null && targetMatch.winner.isEmpty()) {
            targetMatch.winner = winnerName
            matchesPlayedCounter += 1
            
            if (winnerName == targetMatch.playerA.name) {
                targetMatch.scoreA = 9
                targetMatch.scoreB = 2
            } else {
                targetMatch.scoreA = 1
                targetMatch.scoreB = 8
            }

            // Advance automatically inside the reactive state tree
            when (matchId) {
                "m5" -> {
                    // Update final slot left side
                    val finalMatch = bracketMatches.find { it.id == "m7" }
                    if (finalMatch != null) {
                        // Create a modified match object
                        val index = bracketMatches.indexOf(finalMatch)
                        bracketMatches[index] = finalMatch.copy(
                            playerA = if (winnerName == targetMatch.playerA.name) targetMatch.playerA else targetMatch.playerB
                        )
                    }
                }
                "m6" -> {
                    // Update final slot right side
                    val finalMatch = bracketMatches.find { it.id == "m7" }
                    if (finalMatch != null) {
                        val index = bracketMatches.indexOf(finalMatch)
                        bracketMatches[index] = finalMatch.copy(
                            playerB = if (winnerName == targetMatch.playerA.name) targetMatch.playerA else targetMatch.playerB
                        )
                    }
                }
                "m7" -> {
                    currentMatchWinnerAnnouncement = "🏆 PARABÉNS! @$winnerName consagrou-se CAMPEÃO no GP eSports JiuVerse!"
                }
            }
        }
    }

    val handleResetSimulation = {
        bracketMatches[4] = TournamentMatch("m5", "SEMIFINAIS", participants[0], participants[3], 0, 0, "")
        bracketMatches[5] = TournamentMatch("m6", "SEMIFINAIS", participants[5], participants[6], 0, 0, "")
        bracketMatches[6] = TournamentMatch("m7", "GRANDE FINAL", participants[0], participants[5], 0, 0, "")
        currentMatchWinnerAnnouncement = ""
        matchesPlayedCounter = 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Arquitetura eSports & Torneios Global",
            subtitle = "Sistemas de Emparelhamento Dinâmico (Matchmaking), Chaves de Eliminação e Distribuição Concorrente"
        )

        // General Statement info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(0.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VISÃO GAME DESIGNER DE COMPETIÇÕES (EXPERT eSPORTS BUILD)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintTeal,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "No JiuVerse, os torneios dão vazão à rivalidade das academias sandbox. Chaves balanceadas por Power Rank de rede minimizam latência de chaveamento. Chaves de eliminação dupla oferecem repescagem justa a escopos competitivos globais.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Panel Column Layout: Brackets simulator and Config rules UI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            
            // Left Column: Tournament Configuration Controls & Live Simulator
            Column(modifier = Modifier.weight(1.1f)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CONFIGURAÇÃO INTEGRADA DO GP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                        }

                        // Categories selector (1x1, 2x2, 5x5, Academies)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Modo / Formato Competitivo:", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("1X1" to "Singles", "2X2" to "Duplas", "5X5" to "Equipes", "ACADEMIAS" to "Guildas").forEach { (key, label) ->
                                val active = selectedFormatClass == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (active) BlueprintTeal.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                        .border(0.5.dp, if (active) BlueprintTeal else BlueprintGridLine, RoundedCornerShape(4.dp))
                                        .clickable { selectedFormatClass = key }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = if (active) BlueprintTeal else BlueprintTextSecondary)
                                }
                            }
                        }

                        // Bracket Rules Selection (Single or Double Elimination)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Regra das Chaves (Brackets Setup):", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("ELIMINACAO_SIMPLES" to "Eliminação Simples", "DUPLA_ELIMINACAO" to "Dupla Eliminação").forEach { (key, label) ->
                                val active = bracketTypeSelected == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (active) BlueprintCyan.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                        .border(0.5.dp, if (active) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                                        .clickable { bracketTypeSelected = key }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (active) BlueprintCyan else BlueprintTextSecondary)
                                }
                            }
                        }

                        Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                        // Scalability parameters spec info
                        Text("DIRETRIZ DA ESCALABILIDADE DE SERVIDOR", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "• Barramento de Chaves: Filas agnósticas concorrentes com arquitetura WebSockets distribuídos gerenciando até 15.000 combatentes em sincronia de 12ms por nó de lobby.\n" +
                                   "• Algoritmo de Seed: Classificação de chave inicial automática usa variabilidade de ping regional e score de LP histórico consolidado.",
                            fontSize = 8.sp,
                            color = BlueprintTextSecondary,
                            lineHeight = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Tournament Runner console Simulator
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
                            Text("SIMULAÇÃO DE ADJUIZAÇÃO CHAVES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            if (matchesPlayedCounter > 0) {
                                Text("(Resetar)", fontSize = 7.5.sp, color = BlueprintOrange, modifier = Modifier.clickable { handleResetSimulation() })
                            }
                        }
                        Text("Resolva as lutas semifinais para gerar os adversários da final:", fontSize = 8.sp, color = BlueprintTextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Match 1 Simulation Row (Semis left side)
                        val matchSemis1 = bracketMatches[4]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("LUTA SEMIFINAL #A", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                Text("@${matchSemis1.playerA.name} CLÁSSICO vs @${matchSemis1.playerB.name}", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            }

                            if (matchSemis1.winner.isEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                            .clickable { handleAdvancementSimulation("m5", matchSemis1.playerA.name) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("A GANHA", fontSize = 7.5.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                            .clickable { handleAdvancementSimulation("m5", matchSemis1.playerB.name) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("B GANHA", fontSize = 7.5.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Text("VENCEDOR: @${matchSemis1.winner}", fontSize = 7.5.sp, fontWeight = FontWeight.Black, color = BlueprintTeal)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Match 2 Simulation Row (Semis right side)
                        val matchSemis2 = bracketMatches[5]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("LUTA SEMIFINAL #B", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                Text("@${matchSemis2.playerA.name} vs @${matchSemis2.playerB.name}", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            }

                            if (matchSemis2.winner.isEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                            .clickable { handleAdvancementSimulation("m6", matchSemis2.playerA.name) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("A GANHA", fontSize = 7.5.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                            .clickable { handleAdvancementSimulation("m6", matchSemis2.playerB.name) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("B GANHA", fontSize = 7.5.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Text("VENCEDOR: @${matchSemis2.winner}", fontSize = 7.5.sp, fontWeight = FontWeight.Black, color = BlueprintTeal)
                            }
                        }

                        // Final Simulated Showdown trigger
                        val matchFinal = bracketMatches[6]
                        if (matchSemis1.winner.isNotEmpty() && matchSemis2.winner.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("FINAIS GERADAS EM TEMPO REAL:", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BlueprintOrange.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintOrange, RoundedCornerShape(4.dp))
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("DISPUTA DO TÍTULO MUNDIAL JIUVERSE", fontSize = 7.5.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                                    Text("@${matchFinal.playerA.name} contra @${matchFinal.playerB.name}", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                }

                                if (matchFinal.winner.isEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .background(BlueprintOrange, RoundedCornerShape(2.dp))
                                                .clickable { handleAdvancementSimulation("m7", matchFinal.playerA.name) }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text("A VENCE", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Black)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(BlueprintOrange, RoundedCornerShape(2.dp))
                                                .clickable { handleAdvancementSimulation("m7", matchFinal.playerB.name) }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text("B VENCE", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Black)
                                        }
                                    }
                                } else {
                                    Text("🏆 CAMPEÃO: @${matchFinal.winner}", fontSize = 8.sp, fontWeight = FontWeight.Black, color = BlueprintOrange)
                                }
                            }
                        }
                    }
                }
            }

            // Right Column: Interactive Brackets Layout View
            Column(modifier = Modifier.weight(0.9f)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ÁRVORE DE CHAVES DO GP ISOMÉTRICO (COMPETITIVO)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Quarter Finals Bracket Rendering
                        Text("QUARTAS DE FINAL (BO1)", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            (0..3).forEach { index ->
                                val match = bracketMatches[index]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("@${match.playerA.name.take(10)} (${match.scoreA})", fontSize = 7.5.sp, color = if (match.winner == match.playerA.name) BlueprintTeal else BlueprintTextSecondary)
                                    Text("vs", fontSize = 7.5.sp, color = BlueprintGridLine)
                                    Text("(${match.scoreB}) @${match.playerB.name.take(10)}", fontSize = 7.5.sp, color = if (match.winner == match.playerB.name) BlueprintTeal else BlueprintTextSecondary)
                                }
                            }
                        }

                        // Semis rendering
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("SEMIFINAIS (BO3 ROTAÇÃO)", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            (4..5).forEach { index ->
                                val match = bracketMatches[index]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("@${match.playerA.name.take(10)}", fontSize = 7.5.sp, color = if (match.winner == match.playerA.name) BlueprintTeal else BlueprintTextPrimary)
                                    Text("vs", fontSize = 7.5.sp, color = BlueprintGridLine)
                                    Text("@${match.playerB.name.take(10)}", fontSize = 7.5.sp, color = if (match.winner == match.playerB.name) BlueprintTeal else BlueprintTextPrimary)
                                }
                            }
                        }

                        // Final block
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("DECISÃO DO GP NO TATAME ESTELAR", fontSize = 8.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                        val finalMatchObj = bracketMatches[6]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BlueprintOrange.copy(alpha = 0.05f))
                                .border(0.5.dp, BlueprintOrange, RoundedCornerShape(2.dp))
                                .padding(4.dp)
                        ) {
                            Text(
                                "@${finalMatchObj.playerA.name} x @${finalMatchObj.playerB.name}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintTextPrimary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Live winner notification banner block
        AnimatedVisibility(visible = currentMatchWinnerAnnouncement.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlueprintOrange.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .border(1.dp, BlueprintOrange, RoundedCornerShape(4.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = BlueprintOrange)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(currentMatchWinnerAnnouncement, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                }

                Text(
                    "SINC",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(BlueprintTeal, RoundedCornerShape(2.dp))
                        .clickable { currentMatchWinnerAnnouncement = "" }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Season rankings ledger
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2. PLACAR REGULAR E SISTEMA DE RANKING (LEAGUE RANKINGS)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Icon(imageVector = Icons.Default.FormatListNumbered, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
        }

        // Season Filter buttons row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "SEASON_I" to "Temporada Estelar I (Ativa)",
                "OFF_SEASON" to "Pré-temporada Gincana"
            ).forEach { (key, label) ->
                val isSelected = selectedSeasonFilter == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) BlueprintCyan.copy(alpha = 0.2f) else BlueprintCard, RoundedCornerShape(4.dp))
                        .border(0.5.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                        .clickable { selectedSeasonFilter = key }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) BlueprintCyan else BlueprintTextSecondary)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                
                // Table Header row
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.4f)).padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("POS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(0.4f))
                    Text("ACADEMIA / COMPETIDOR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1.8f))
                    Text("WIN RATIO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1f))
                    Text("DIRETRIZ LP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1f))
                }

                val rankingData = listOf(
                    RankedCompetitor(1, "Alliance Dojo Centro", "92.5% (120v-10d)", 4590, "🎗️"),
                    RankedCompetitor(2, "Gracie Barra Angra", "88.6% (102v-13d)", 4200, "🎗️"),
                    RankedCompetitor(3, "Atos San Diego", "85.2% (98v-17d)", 3980, "🎗️"),
                    RankedCompetitor(4, "Checkmat SP", "79.4% (85v-22d)", 3600, "🎗️"),
                    RankedCompetitor(5, "DreamArt MG", "75.0% (75v-25d)", 3210, "🎗️")
                )

                rankingData.forEach { rank ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pos circle
                        Box(
                            modifier = Modifier
                                .weight(0.4f)
                                .size(18.dp)
                                .background(if (rank.rankingPos <= 3) BlueprintOrange.copy(alpha = 0.15f) else Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${rank.rankingPos}", fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (rank.rankingPos <= 3) BlueprintOrange else BlueprintTextPrimary)
                        }

                        // Name
                        Column(modifier = Modifier.weight(1.8f)) {
                            Text(rank.teamName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            Text("Dívida/Taxa de Tatame: Em dia", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                        }

                        // Win ratio
                        Text(rank.winRatio, fontSize = 8.5.sp, color = BlueprintTextSecondary, modifier = Modifier.weight(1f))

                        // LP
                        Text("${rank.lpScore} LP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VÍNCULO ACADEMIA-MULTIPLIER: Membros de academias posicionadas no top 3 do placar acumulam bônus passivo de +5% Prestígio de dojo em qualquer sparring no sandbox.",
                        fontSize = 8.5.sp,
                        color = BlueprintTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Rewards & Prize matrix definitions
        Text(
            text = "3. MATRIZ DE PREMIAÇÕES POR TIER DE DESEMPENHO (PRIZES)",
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
                Text(
                    text = "A distribuição de prêmios ao final dos 60 dias de campeonato segue os limites rígidos de conservação econômica:",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val prizesList = listOf(
                    Triple("OURO (Campeão Mundial)", "🏆 Troféu Metálico de Sandbox + 25.000 JC + 1.200 JG", "Exclusividade única para o top de cada chave."),
                    Triple("PRATA (Finalistas)", "🥈 Medalha Prata Estelar + 10.000 JC + 500 JG", "Destinado a quem completa a grande final síncrona."),
                    Triple("BRONZE (Semi-Finalistas)", "🥉 Emote de Comemoração + 5.000 JC + 250 JG", "Distribuição simples para o top 4."),
                    Triple("PARTICIPAÇÃO", "🎗️ Faixa de Gincana Exclusiva + 1.000 JC", "Entregue a todos os participantes inscritos com luta ativa.")
                )

                prizesList.forEach { (tier, prizes, rules) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tier, fontSize = 9.sp, fontWeight = FontWeight.Black, color = BlueprintOrange)
                            Text(prizes, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            Text(rules, fontSize = 8.sp, color = BlueprintTextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
