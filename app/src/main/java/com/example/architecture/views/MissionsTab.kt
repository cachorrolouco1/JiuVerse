package com.example.architecture.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
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

data class QuestSim(
    val id: String,
    val title: String,
    val type: String, // "DIARIA", "SEMANAL", "MENSAL", "TEMPORADA", "EVENTO"
    val description: String,
    var currentProgress: Int,
    val targetProgress: Int,
    val xpReward: Int,
    val coinReward: Int,
    val gemReward: Int,
    val itemReward: String = "",
    var isClaimed: Boolean = false
)

data class AchievementSim(
    val id: String,
    val name: String,
    val requirement: String,
    val prestigeBonus: Int,
    val uniqueBadge: String,
    var unlocked: Boolean = false
)

@Composable
fun MissionsTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Currencies and levels simulation
    var playerLevel by remember { mutableStateOf(4) }
    var playerXP by remember { mutableStateOf(650) }
    val xpNeededForNextLevel = 1000
    var walletJiCoins by remember { mutableStateOf(12800) }
    var walletJiuGems by remember { mutableStateOf(400) }

    // Visual notification triggers
    var lastClaimedReward by remember { mutableStateOf("") }
    var levelUpTriggered by remember { mutableStateOf(false) }

    // Quests collection
    val questsList = remember {
        mutableStateListOf(
            // Dailies
            QuestSim("q1", "Conversar com 5 jogadores", "DIARIA", "Utilize a voz de proximidade espacial ou chat de guilda no Tatame do QG.", 2, 5, 120, 150, 0),
            QuestSim("q2", "Entrar em uma academia", "DIARIA", "Visite um terreno de academia física licenciado na vizinhança sandbox.", 0, 1, 100, 100, 0),
            QuestSim("q3", "Praticar 15min de Solo Drills", "DIARIA", "Realize drilamento de reposição de guarda no seu dojo.", 15, 15, 80, 120, 2),
            
            // Weeklies
            QuestSim("q4", "Ganhar 3 desafios síncronos", "SEMANAL", "Vença sparrings amigáveis com matchmaking competitivo ativo.", 1, 3, 450, 500, 10),
            QuestSim("q5", "Limpar o Tatame Principal", "SEMANAL", "Interaja com os NPCs auxiliares de higiene da sua academia sandbox.", 0, 1, 300, 400, 0),
            QuestSim("q6", "Investir na Bolsa de JiCoins", "SEMANAL", "Altere as taxas de custódia da sua Guilda ou compre cotas do Dojo.", 0, 1, 350, 450, 5),

            // Monthlies
            QuestSim("q7", "Completar 10 aulas virtuais", "MENSAL", "Conclua aulas guiadas interativas na Academy do JiuVerse.", 6, 10, 1200, 1500, 30, "Faixa Azul Clássica"),
            QuestSim("q8", "Participar de um torneio", "MENSAL", "Increva-se ou lute no Grand Prix Estelar Mensal do JiuVerse.", 0, 1, 1500, 2000, 50, "Cinto de Outono"),

            // Seasons
            QuestSim("q9", "Mapear 3 Terrenos Expandidos", "TEMPORADA", "Compre ou arrende novos lotes isométricos para a sua guilda.", 1, 3, 2500, 3500, 100, "Troféu Samurai Placa"),
            QuestSim("q10", "Atingir rank Faixa-Marrom", "TEMPORADA", "Suba de nível de prestígio de sparring até graduação profissional.", 0, 1, 3000, 4000, 150, "Kimono Gold Edition"),

            // Events
            QuestSim("q11", "Encontrar Mestre Helio Secreto", "EVENTO", "Descubra a localização do NPC lendário do Tatame Escondido de Angra.", 0, 1, 1800, 2000, 80, "Título: Guardião Legado"),
            QuestSim("q12", "Derrotar o Desafiador do Deserto", "EVENTO", "Sparring com o boss NPC de wrestling de Chicago na gincana.", 1, 1, 1200, 1000, 25, "Medalha de Chicago")
        )
    }

    // Achievements collection
    val achievements = remember {
        mutableStateListOf(
            AchievementSim("a1", "Duque do Desafio", "Vença 10 sparrings com o mesmo oponente sem sofrer passagem de guarda.", 150, "🎗️ Duque", unlocked = true),
            AchievementSim("a2", "Colecionador de Faixas Marrons", "Adquira 4 kimonos marrons de marcas licenciadas no mercado P2P.", 300, "🎖️ Colecionador"),
            AchievementSim("a3", "Magnata do Tatame", "Mantenha saldo acima de 50.000 JC sem impostos atrasados.", 500, "👑 Imperial")
        )
    }

    // Interactive claim triggers
    val handleClaimQuest = { quest: QuestSim ->
        if (quest.currentProgress >= quest.targetProgress && !quest.isClaimed) {
            quest.isClaimed = true
            walletJiCoins += quest.coinReward
            walletJiuGems += quest.gemReward
            
            // Gain XP logic
            playerXP += quest.xpReward
            lastClaimedReward = "Resgatado: +${quest.xpReward} XP, +${quest.coinReward} JC, +${quest.gemReward} JG!"
            
            if (quest.itemReward.isNotEmpty()) {
                lastClaimedReward += " [Item: ${quest.itemReward}]"
            }

            // Level Up logic
            if (playerXP >= xpNeededForNextLevel) {
                playerXP -= xpNeededForNextLevel
                playerLevel += 1
                levelUpTriggered = true
            }
        }
    }

    var selectedTypeFilter by remember { mutableStateOf("DIARIA") } // "DIARIA", "SEMANAL", "MENSAL", "TEMPORADA", "EVENTO"

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Arquitetura de Engajamento, Missões & Retenção",
            subtitle = "Sistemas de Desafios Regulares, Calendário de Atividades, XP Dinâmico e Recompensas P2P"
        )

        // Simulated Balance sheet / game multiplier metrics HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(0.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VISÃO ESPECIALISTA EM RETENÇÃO & ECONOMIA (ENGAGEMENT ENGINE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintTeal,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "A retenção é sustentada em ciclos diários (hábitos básicos), semanais (socialização e torneios) e mensais (prestígio sandbox). Este balanço de recompensas foi calibrado para evitar a deflação de JiCoins e manter o preço de itens raros alto.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Level / XP Progression Top Dashboard Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(BlueprintCyan.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, BlueprintCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$playerLevel",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BlueprintCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("SEU NÍVEL ATUAL:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                                Text(
                                    text = if(playerLevel >= 5) "Graduado Especialista" else "Aspirante a Tatame",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueprintTextPrimary
                                )
                            }
                        }
                    }

                    // Currencies inside the card
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("MOEDAS TIPO JC", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Text("🪙 ${String.format("%,d", walletJiCoins)}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = BlueprintTeal, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("GEMS PREMIUM JG", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Text("💎 ${String.format("%,d", walletJiuGems)}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = BlueprintOrange, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // XP Progress bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Experiência Geral (XP)", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                    Text("$playerXP / $xpNeededForNextLevel XP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange, fontFamily = FontFamily.Monospace)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                ) {
                    val ratio = playerXP.toFloat() / xpNeededForNextLevel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .background(BlueprintOrange, RoundedCornerShape(4.dp))
                            .fillMaxSize()
                    )
                }

                // Smooth Claim feedback overlays
                AnimatedVisibility(visible = lastClaimedReward.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BlueprintTeal.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintTeal, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(lastClaimedReward, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                    }
                }

                AnimatedVisibility(visible = levelUpTriggered) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BlueprintOrange.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .border(1.dp, BlueprintOrange, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = BlueprintOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("🎉 PARABÉNS! VOCÊ SUBIU DE GRAU!", fontSize = 11.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                            Text("A sua faixa do JiuVerse brilha mais forte no feed global e desbloqueia novos desafios sandbox de temporada.", fontSize = 9.sp, color = BlueprintTextPrimary)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "FECHAR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintTextSecondary,
                            modifier = Modifier.clickable { levelUpTriggered = false }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Quests System and Selector Grid
        Text(
            text = "1. QUADRO DE MISSÕES (QUEST BOARD REGULAR)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Tab selection for categories
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "DIARIA" to "Diárias",
                "SEMANAL" to "Semanais",
                "MENSAL" to "Mensais",
                "TEMPORADA" to "Temporada",
                "EVENTO" to "Eventos Especiais"
            ).forEach { (key, label) ->
                val isSelected = selectedTypeFilter == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) BlueprintCyan.copy(alpha = 0.2f) else BlueprintCard, RoundedCornerShape(4.dp))
                        .border(0.5.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                        .clickable { selectedTypeFilter = key }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) BlueprintCyan else BlueprintTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Quests List
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val filteredQuests = questsList.filter { it.type == selectedTypeFilter }

                filteredQuests.forEach { quest ->
                    val isCompleted = quest.currentProgress >= quest.targetProgress

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (quest.isClaimed) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(14.dp))
                                } else if (isCompleted) {
                                    Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = BlueprintOrange, modifier = Modifier.size(14.dp))
                                } else {
                                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = BlueprintTextSecondary, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(quest.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (quest.isClaimed) BlueprintTextSecondary else BlueprintTextPrimary)
                            }
                            Text(quest.description, fontSize = 8.5.sp, color = BlueprintTextSecondary)

                            // Linear progress info
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Ação: ${quest.currentProgress}/${quest.targetProgress}", fontSize = 8.sp, color = BlueprintCyan, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recompensa: +${quest.xpReward} XP / +${quest.coinReward} JC" + (if (quest.gemReward > 0) " / +${quest.gemReward} JG" else "") + (if (quest.itemReward.isNotEmpty()) " / Item: ${quest.itemReward}" else ""),
                                    fontSize = 8.sp,
                                    color = BlueprintTeal
                                )
                            }
                        }

                        // Action complete/claim button
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.padding(start = 10.dp)) {
                            if (quest.isClaimed) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("RESGATADO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                                }
                            } else if (isCompleted) {
                                Button(
                                    onClick = { handleClaimQuest(quest) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintOrange),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("CLAIM RECOMPENSA", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Simulate single complete click
                                Button(
                                    onClick = {
                                        quest.currentProgress = Math.min(quest.targetProgress, quest.currentProgress + 1)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("+1 SIMULAR", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cabinet of Achievements
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2. PAINEL DE CONQUISTAS E MEDALHARIA (TROPHIES)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = BlueprintOrange, modifier = Modifier.size(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                achievements.forEach { achievement ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                if (achievement.unlocked) BlueprintOrange.copy(alpha = 0.05f) else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                0.5.dp,
                                if (achievement.unlocked) BlueprintOrange.copy(alpha = 0.4f) else BlueprintGridLine,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(achievement.uniqueBadge, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(achievement.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(if (achievement.unlocked) BlueprintTeal.copy(alpha = 0.2f) else BlueprintRed.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (achievement.unlocked) "DESBLOQUEADO" else "BLOQUEADO",
                                            fontSize = 6.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (achievement.unlocked) BlueprintTeal else BlueprintRed
                                        )
                                    }
                                }
                                Text(achievement.requirement, fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("PRESTÍGIO BONUS", fontSize = 6.5.sp, color = BlueprintTextSecondary)
                            Text("+${achievement.prestigeBonus} Pts", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance & Progression Economy Sheet (Strict game-theoretic definitions)
        Text(
            text = "3. BALANCEAMENTO DA PROGRESSÃO DA MOEDA JICOIN NO JIUVERSE",
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TABELA MATRICIAL DE CONTROLE DE INFLAÇÃO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                }
                Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                Text(
                    text = "A moeda do JiuVerse segue um teto de emissão semanal por jogador para evitar depreciação de preço no Marketplace:",
                    fontSize = 9.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Cost / Effort Grid
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3f)).padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TIPO DE MISSÃO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1f))
                    Text("CUSTO TEMP/EFFORT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1.3f))
                    Text("LIMITE SEMANAL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1f))
                    Text("RECOMPENSA MÁXIMA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1f))
                }

                listOf(
                    Quadruple("Missões Diárias", "5 min (Solo/Conversa)", "21 Missões", "3.150 JC / 10 JG"),
                    Quadruple("Missões Semanais", "45 min (Desafios/Dojos)", "6 Missões", "2.700 JC / 30 JG"),
                    Quadruple("Missão Mensal GP", "3 horas (GP Mundial Estelar)", "2 Inscrições", "4.000 JC / 100 JG"),
                    Quadruple("Milestone Temporada", "Varia (Expansão Territorial)", "Por Season", "7.500 JC / 350 JG")
                ).forEach { metric ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(metric.first, fontSize = 8.sp, color = BlueprintTextPrimary, modifier = Modifier.weight(1f))
                        Text(metric.second, fontSize = 8.sp, color = BlueprintTextSecondary, modifier = Modifier.weight(1.3f))
                        Text(metric.third, fontSize = 8.sp, color = BlueprintTextSecondary, modifier = Modifier.weight(1f))
                        Text(metric.fourth, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3B1E28), RoundedCornerShape(4.dp))
                        .border(0.5.dp, BlueprintRed, RoundedCornerShape(4.dp))
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = null, tint = BlueprintRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VÍNCULO RETENÇÃO-IMPOSTO: Jogadores inativos por mais de 14 dias perdem taxas de aluguel territorial reduzidas em sandbox, incentivando logins de recorrência.",
                        fontSize = 8.sp,
                        color = BlueprintTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Workaround data structure for metric rows
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
