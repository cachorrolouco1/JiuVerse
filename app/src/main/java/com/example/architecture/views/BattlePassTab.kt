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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.TextStyle
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

data class PassReward(
    val level: Int,
    val isPremium: Boolean,
    val type: String, // "JICOIN", "JIUGEMS", "COSMETICO", "EMOTE", "PET", "TITULO"
    val rewardName: String,
    val rewardIcon: String,
    val valueStr: String = "",
    var isClaimed: Boolean = false
)

@Composable
fun BattlePassTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // 1. Core Battle Pass simulation states
    var premiumUnlocked by remember { mutableStateOf(false) }
    var currentPassLevel by remember { mutableStateOf(1) }
    var currentPassXP by remember { mutableStateOf(150) }
    val xpPerLevel = 500

    var walletJiCoins by remember { mutableStateOf(5300) }
    var walletJiuGems by remember { mutableStateOf(1250) } // Can purchase premium for 950 JiuGems

    var activeNotification by remember { mutableStateOf("") }
    var buyPremiumSuccessDialog by remember { mutableStateOf(false) }

    // 2. Battle pass levels matrix definitions (Auto generated 1 to 100)
    val passRewards = remember {
        val list = mutableListOf<PassReward>()
        for (i in 1..100) {
            // Free Track
            val freeType: String
            val freeName: String
            val freeIcon: String

            // Premium Track
            val premType: String
            val premName: String
            val premIcon: String

            when {
                i == 1 -> {
                    freeType = "TITULO" ; freeName = "Iniciado do Tatame" ; freeIcon = "🏷️"
                    premType = "TITULO" ; premName = "Elite JiuVerse" ; premIcon = "🏅"
                }
                i == 5 -> {
                    freeType = "JICOIN" ; freeName = "Bonus Moedas" ; freeIcon = "🪙"
                    premType = "COSMETICO" ; premName = "Kimono de Safira Claro" ; premIcon = "🥋"
                }
                i == 10 -> {
                    freeType = "EMOTE" ; freeName = "Oss Respeitoso" ; freeIcon = "🤝"
                    premType = "EMOTE" ; premName = "Vem Pro Guard" ; premIcon = "🔥"
                }
                i == 20 -> {
                    freeType = "JIUGEMS" ; freeName = "Gemas Iniciais" ; freeIcon = "💎"
                    premType = "PET" ; premName = "Pitbull Filhote" ; premIcon = "🐕"
                }
                i == 35 -> {
                    freeType = "COSMETICO" ; freeName = "Faixa Azul Sincronizada" ; freeIcon = "🎗️"
                    premType = "COSMETICO" ; premName = "Rashguard Cromada Neon" ; premIcon = "👕"
                }
                i == 50 -> {
                    freeType = "TITULO" ; freeName = "Veterano Regresso" ; freeIcon = "🎖️"
                    premType = "COSMETICO" ; premName = "Kimono Armadura Lendária" ; premIcon = "👑"
                }
                i == 75 -> {
                    freeType = "JIUGEMS" ; freeName = "Super Pack Gemas" ; freeIcon = "💎"
                    premType = "TITULO" ; premName = "Lenda Invencível Estelar" ; premIcon = "🌌"
                }
                i == 100 -> {
                    freeType = "COSMETICO" ; freeName = "Kimono do Grande Mestre" ; freeIcon = "🥋"
                    premType = "PET" ; premName = "Capivara Suprema Faixa Coral" ; premIcon = "🦫"
                }
                i % 10 == 0 -> {
                    freeType = "JIUGEMS" ; freeName = "Gemas de Nível Redondo" ; freeIcon = "💎"
                    premType = "COSMETICO" ; premName = "Faixa Especial Tier $i" ; premIcon = "🎗️"
                }
                i % 5 == 0 -> {
                    freeType = "COSMETICO" ; freeName = "Bandagem Rara $i" ; freeIcon = "🩹"
                    premType = "EMOTE" ; premName = "Gesto Combo Tatame $i" ; premIcon = "✨"
                }
                i % 2 == 0 -> {
                    freeType = "JICOIN" ; freeName = "JiCoins Rebolo" ; freeIcon = "🪙"
                    premType = "JIUGEMS" ; premName = "JiuGems Premium" ; premIcon = "💎"
                }
                else -> {
                    freeType = "JICOIN" ; freeName = "Pack de Apoio" ; freeIcon = "🪙"
                    premType = "JICOIN" ; premName = "Super Tesouro Dojo" ; premIcon = "🪙"
                }
            }

            // Values
            val freeVal = when(freeType) {
                "JICOIN" -> "${(100 + i * 5)} JC"
                "JIUGEMS" -> "${(2 + i / 10)} JG"
                else -> ""
            }
            val premVal = when(premType) {
                "JICOIN" -> "${(300 + i * 15)} JC"
                "JIUGEMS" -> "${(10 + i / 5)} JG"
                else -> ""
            }

            list.add(PassReward(i, isPremium = false, freeType, freeName, freeIcon, freeVal))
            list.add(PassReward(i, isPremium = true, premType, premName, premIcon, premVal))
        }
        mutableStateListOf(*list.toTypedArray())
    }

    // Level search query
    var searchQueryLevel by remember { mutableStateOf("1") }
    var currentViewRange by remember { mutableStateOf("TIERS_1_10") } // "TIERS_1_10", "TIERS_MILESTONES", "TIERS_COMPARTILHADO"

    // Currency values updates when claiming
    val handleClaimReward = { reward: PassReward ->
        if (reward.level <= currentPassLevel && !reward.isClaimed) {
            if (reward.isPremium && !premiumUnlocked) {
                activeNotification = "Alerta: Trilha Premium bloqueada! Adquira o Passe de Elite primeiro."
            } else {
                reward.isClaimed = true
                if (reward.type == "JICOIN") {
                    val amount = reward.valueStr.replace(" JC", "").toIntOrNull() ?: 100
                    walletJiCoins += amount
                } else if (reward.type == "JIUGEMS") {
                    val amount = reward.valueStr.replace(" JG", "").toIntOrNull() ?: 10
                    walletJiuGems += amount
                }
                activeNotification = "Sucesso: Resgatado '${reward.rewardName}' (Nível ${reward.level}) com sucesso!"
            }
        }
    }

    val handleBuyPremium = {
        if (walletJiuGems >= 950) {
            walletJiuGems -= 950
            premiumUnlocked = true
            buyPremiumSuccessDialog = true
            activeNotification = "Parabéns! Passe Premium Elite desbloqueado. Reivindique os bônus acumulados."
        } else {
            activeNotification = "Alerta: Gemas insuficientes! Insira mais fundos na carteira para comprar o Passe."
        }
    }

    val handleSimulateXP = {
        currentPassXP += 150
        if (currentPassXP >= xpPerLevel) {
            currentPassXP -= xpPerLevel
            if (currentPassLevel < 100) {
                currentPassLevel += 1
                activeNotification = "Progresso: Você subiu para o Nível $currentPassLevel do Passe!"
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
        SectionHeader(
            title = "Desenho Técnico do Passe de Batalha (Battle Pass)",
            subtitle = "Motor de Monetização Linear, Balanço de Retenção Síncrona e Engenharia de Exclusividade"
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
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VISÃO ANALISTA PRINCIPAL DE MONETIZAÇÃO (MONETIZATION ARCHITECT)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintTeal,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "O Passe de Batalha de 60 dias do JiuVerse converte tempo de jogo em prestígio de dojo. Oferece 100 níveis balanceados para recompensas diárias. A trilha Premium age como gatilho psicológico de perda acumuladora, gerando conversão constante.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Top Panel: Dashboard info
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
                                    .size(38.dp)
                                    .background(
                                        if (premiumUnlocked) BlueprintOrange.copy(alpha = 0.15f) else Color.Black,
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (premiumUnlocked) BlueprintOrange else BlueprintGridLine,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$currentPassLevel",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (premiumUnlocked) BlueprintOrange else BlueprintTextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("PASSE DO JIUVERSE - TEMPORADA I", fontSize = 9.sp, color = BlueprintTextSecondary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (premiumUnlocked) BlueprintOrange.copy(alpha = 0.15f) else Color(0xFF1E293B),
                                                RoundedCornerShape(2.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (premiumUnlocked) "PREMIUM ELITE" else "TRILHA GRATUITA",
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (premiumUnlocked) BlueprintOrange else BlueprintTextSecondary
                                        )
                                    }
                                }
                                Text("A Força do Kimono Ancestral", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            }
                        }
                    }

                    // Remaining timer & status
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = BlueprintRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DURAÇÃO: 60 DIAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintRed)
                        }
                        Text("Termina em: 59d 23h 48m", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // XP Progress tracker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Progresso de XP da Temporada:", fontSize = 9.sp, color = BlueprintTextSecondary)
                    Text("$currentPassXP / $xpPerLevel XP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.Black, RoundedCornerShape(5.dp))
                        .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(5.dp))
                ) {
                    val ratio = currentPassXP.toFloat() / xpPerLevel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .background(BlueprintCyan, RoundedCornerShape(5.dp))
                            .fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action simulation buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { handleSimulateXP() },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("+150 XP SIMULAR", fontSize = 8.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { 
                                if (currentPassLevel < 100) {
                                    currentPassLevel += 1 
                                    currentPassXP = 0
                                    activeNotification = "Progresso: Comprou +1 nível do passe!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("PULAR NÍVEL (150 jc)", fontSize = 8.5.sp, color = Color.White)
                        }
                    }

                    // Purchase Premium Pass Elite Trigger
                    if (!premiumUnlocked) {
                        Button(
                            onClick = { handleBuyPremium() },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueprintOrange),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CardMembership, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PASSE PREMIUM ELITE (950 JG)", fontSize = 8.5.sp, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(BlueprintOrange.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlueprintOrange, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("BÊNÇÃO ELITE ATIVA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                        }
                    }
                }

                // Balance info display
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Seus Fundos Activos: 🪙 $walletJiCoins JiCoins | 💎 $walletJiuGems JiuGems", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                    if (walletJiuGems < 950 && !premiumUnlocked) {
                        Text("Adicione 💎 para liberar o Premium", fontSize = 8.5.sp, color = BlueprintRed, modifier = Modifier.clickable { walletJiuGems += 500 })
                    }
                }

                // Notifications Banner
                AnimatedVisibility(visible = activeNotification.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BlueprintCyan.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintCyan, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(activeNotification, fontSize = 8.5.sp, color = BlueprintTextPrimary)
                        Text(
                            "OK",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintCyan,
                            modifier = Modifier.clickable { activeNotification = "" }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Interactive levels display Grid (100 Levels)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "1. PROGRESSÃO COMPLETA DOS 100 NÍVEIS (TIER PROGRESSION LOCKER)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Icon(imageVector = Icons.Default.Style, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
        }

        // Search & Filter controls row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drop selectors
            listOf(
                "TIERS_1_10" to "Níveis 1 - 15",
                "TIERS_MILESTONES" to "Grandes Marcos",
                "TIERS_COMPARTILHADO" to "Conquistados"
            ).forEach { (range, label) ->
                val isSelected = currentViewRange == range
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) BlueprintCyan.copy(alpha = 0.15f) else BlueprintCard, RoundedCornerShape(4.dp))
                        .border(0.5.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                        .clickable { currentViewRange = range }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = if (isSelected) BlueprintCyan else BlueprintTextSecondary)
                }
            }

            // Quick Direct Level Search input
            OutlinedTextField(
                value = searchQueryLevel,
                onValueChange = { 
                    if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 1..100)) {
                        searchQueryLevel = it
                    }
                },
                label = { Text("Nível (1-100)", fontSize = 7.5.sp) },
                textStyle = TextStyle(fontSize = 9.sp, color = Color.White),
                modifier = Modifier.width(80.dp),
                maxLines = 1,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = BlueprintOrange,
                        modifier = Modifier.size(12.dp)
                    )
                }
            )
        }

        // Render levels table
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                
                // Table header
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3f)).padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NÍVEL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(0.5f))
                    Text("🥋 TRILHA GRATUITA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal, modifier = Modifier.weight(1.3f))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("👑 TRILHA PREMIUM ELITE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange, modifier = Modifier.weight(1.3f))
                }

                // Filtering matrix list based on selector
                val targetLevels = remember(currentViewRange, searchQueryLevel) {
                    val baseList = (1..100).toList()
                    val filtered = when(currentViewRange) {
                        "TIERS_1_10" -> baseList.filter { it <= 15 }
                        "TIERS_MILESTONES" -> baseList.filter { it == 1 || it == 5 || it == 10 || it == 20 || it == 35 || it == 50 || it == 75 || it == 100 }
                        "TIERS_COMPARTILHADO" -> baseList.filter { it <= currentPassLevel }
                        else -> baseList
                    }

                    if (searchQueryLevel.isNotEmpty()) {
                        val num = searchQueryLevel.toIntOrNull()
                        if (num != null && num in 1..100) {
                            listOf(num)
                        } else filtered
                    } else filtered
                }

                Column(
                    modifier = Modifier.height(240.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    targetLevels.forEach { lvl ->
                        // Gather free & premium reward instances
                        val freeReward = passRewards.first { it.level == lvl && !it.isPremium }
                        val premReward = passRewards.first { it.level == lvl && it.isPremium }

                        val isLvlUnlocked = lvl <= currentPassLevel

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isLvlUnlocked) BlueprintCyan.copy(alpha = 0.03f) else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    0.5.dp,
                                    if (lvl == currentPassLevel) BlueprintCyan else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Level column circle block
                            Box(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (isLvlUnlocked) BlueprintCyan.copy(alpha = 0.15f) else Color.Black,
                                            CircleShape
                                        )
                                        .border(
                                            0.5.dp,
                                            if (isLvlUnlocked) BlueprintCyan else BlueprintGridLine,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$lvl",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isLvlUnlocked) BlueprintCyan else BlueprintTextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Free Track Cell
                            Row(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, if (freeReward.isClaimed) BlueprintTeal.copy(alpha = 0.3f) else BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable { handleClaimReward(freeReward) }
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(freeReward.rewardIcon, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(freeReward.rewardName, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                        if (freeReward.valueStr.isNotEmpty()) {
                                            Text(freeReward.valueStr, fontSize = 7.sp, color = BlueprintTeal)
                                        } else {
                                            Text(freeReward.type, fontSize = 6.5.sp, color = BlueprintTextSecondary)
                                        }
                                    }
                                }

                                if (freeReward.isClaimed) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(11.dp))
                                } else if (isLvlUnlocked) {
                                    Text("CLAIM", fontSize = 7.5.sp, color = BlueprintTeal, fontWeight = FontWeight.Black)
                                } else {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BlueprintTextSecondary, modifier = Modifier.size(10.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Premium Track Cell
                            Row(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .background(
                                        if (premiumUnlocked) BlueprintOrange.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.4f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        0.5.dp, 
                                        if (premReward.isClaimed) BlueprintTeal.copy(alpha = 0.3f) 
                                        else if (premiumUnlocked) BlueprintOrange.copy(alpha = 0.4f) 
                                        else BlueprintGridLine, 
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { handleClaimReward(premReward) }
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(premReward.rewardIcon, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(premReward.rewardName, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (premiumUnlocked) BlueprintTextPrimary else BlueprintTextSecondary)
                                        if (premReward.valueStr.isNotEmpty()) {
                                            Text(premReward.valueStr, fontSize = 7.sp, color = BlueprintOrange)
                                        } else {
                                            Text(premReward.type, fontSize = 6.5.sp, color = BlueprintTextSecondary)
                                        }
                                    }
                                }

                                if (premReward.isClaimed) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(11.dp))
                                } else if (isLvlUnlocked && premiumUnlocked) {
                                    Text("CLAIM", fontSize = 7.5.sp, color = BlueprintOrange, fontWeight = FontWeight.Black)
                                } else {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BlueprintOrange, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Retention and Balance metrics
        Text(
            text = "2. MÉTRICAS SÍNCRONAS DE BALANCEAMENTO E RETENÇÃO DA TEMPORADA",
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
                    Text("GRAU DE ESFORÇO DO JOGADOR (COMPREENSÃO DO DESIGNER)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A curva de progressão do Passe segue o modelo de escassez controlada e reforço positivo para garantir a integridade da economia:",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progression stats grid
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3f)).padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("MÉTRICA CHAVE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1f))
                    Text("VALOR ALVO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(1f))
                    Text("GATILHO PSICOLÓGICO/BENEFÍCIO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, modifier = Modifier.weight(2f))
                }

                val economicsData = listOf(
                    Triple("Tempo de Completitude", "50 a 60 Horas líquidas", "Exige login recorrente de 40-50 min diários sem fadiga."),
                    Triple("Taxa de Conversão Premium", "Preço alvo: 950 Gemas", "Corresponde ao valor psicológico de transação de micro faturamento."),
                    Triple("Retenção D7 Esperada", "+12% acréscimo", "Exclusividade de re-skins de kimono para guildas que progridem juntas."),
                    Triple("Exclusividade de Título (Lvl 100)", "Mítico (Capivara Coral)", "Incentiva status visual extremo compartilhável no chat espacial.")
                )

                economicsData.forEach { (metric, target, trigger) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(metric, fontSize = 8.sp, color = BlueprintTextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(target, fontSize = 8.sp, color = BlueprintTeal, modifier = Modifier.weight(1f))
                        Text(trigger, fontSize = 8.sp, color = BlueprintTextSecondary, modifier = Modifier.weight(2f))
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3B1E1E), RoundedCornerShape(4.dp))
                        .border(0.5.dp, BlueprintRed, RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = BlueprintRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PREVENÇÃO DE INFLAÇÃO DE MERCADO: Peças obtidas no Passe Premium só podem ser vendidas no Marketplace P2P após o término da temporada (60 dias), impedindo crash do preço imediato por excesso de oferta.",
                        fontSize = 8.sp,
                        color = BlueprintTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
