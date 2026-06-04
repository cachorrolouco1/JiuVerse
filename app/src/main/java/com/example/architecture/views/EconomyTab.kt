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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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

data class EconomyRowState(
    val activityName: String,
    val type: String, // "FAUCET" (Source) or "SINK" (Imposto/Escoamento)
    val currency: String, // "JiCoin" or "JiuGem"
    val baselineReward: Int,
    val currentCapDaily: String,
    val antiExploitRule: String
)

@Composable
fun EconomyTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Interactive simulator inputs
    var activePlayersCount by remember { mutableStateOf(10000f) } // 1K - 100K users
    var dailyQuestReward by remember { mutableStateOf(500f) } // JiCoins
    var tournamentRewardPool by remember { mutableStateOf(2500f) } // JiCoins
    var academyCheckInReward by remember { mutableStateOf(150f) } // JiCoins

    // Sinks and taxes
    var marketplaceTaxPercent by remember { mutableStateOf(12f) } // 5% - 25%
    var academyTaxPercent by remember { mutableStateOf(10f) } // 0% - 50%
    var upgradeFailureDeflationCost by remember { mutableStateOf(1200f) } // JiCoins sink
    var premiumBattlePassPrice by remember { mutableStateOf(350f) } // JiuGems

    // Default Spreadsheet Model database
    val spreadsheetLines = remember {
        mutableStateListOf(
            EconomyRowState("Missões Comuns Diárias", "FAUCET", "JiCoin", 500, "1 tarefa/dia", "Check de Assinatura IP único"),
            EconomyRowState("Aulas Clínicas e Sparring", "FAUCET", "JiCoin", 150, "3 aulas/dia", "Limite de HWID de dispositivo"),
            EconomyRowState("Copa Semanal do Dojo", "FAUCET", "JiCoin", 2500, "1 ingresso/semana", "Mínimo Nível de Combate 15"),
            EconomyRowState("Conquista: Faixa Corajosa", "FAUCET", "JiCoin", 1000, "Única vez", "ID de Conta verificado por Celular"),
            EconomyRowState("Eventos Especiais Sazonais", "FAUCET", "JiCoin", 3000, "Varia / Mês", "Cooldown de Resgate em Batch"),
            EconomyRowState("Passe de Batalha Premium", "FAUCET", "JiuGem", 400, "1 vez / Temporada", "Moeda Premium rastreada por Google Play"),
            EconomyRowState("Loja de Acessórios (Kimonos)", "FAUCET", "JiuGem", 100, "Sem Limite", "Transação Segura Gateway de Pagamento"),
            EconomyRowState("Marketplace Inter-Membros", "SINK", "JiCoin", 0, "Taxa Proporcional", "Evasão Tributária: Bloqueio progressivo de trade"),
            EconomyRowState("Tesouraria de Guilda", "SINK", "JiCoin", 0, "Retenção Local", "Auditoria de Transferência anti-mula"),
            EconomyRowState("Forja de Equipamentos", "SINK", "JiCoin", 1200, "Sem Limite", "Sorteador Pseudo-randômico (Anti-seed hack)"),
            EconomyRowState("Inscrições de Torneios Master", "SINK", "JiCoin", 400, "2 por semana", "Recusa de reembolso pós-pareamento")
        )
    }

    // Mathematical projection of dynamic Minting and Deflation Rate (Economic Balance)
    val totalEstimatedMintedDaily = activePlayersCount * (dailyQuestReward + (academyCheckInReward * 2) + (tournamentRewardPool * 0.15f))
    val estimatedDeflatedDailyFromSinks = activePlayersCount * (upgradeFailureDeflationCost * 0.1f) + 
            (activePlayersCount * 800f * (marketplaceTaxPercent / 100f))

    val economicNetBalance = totalEstimatedMintedDaily - estimatedDeflatedDailyFromSinks
    val inflationIndex = totalEstimatedMintedDaily / if (estimatedDeflatedDailyFromSinks > 0) estimatedDeflatedDailyFromSinks else 1f

    // Calculate dynamic severity level of inflation
    val (statusLabel, statusColor, statusDetails) = when {
        inflationIndex > 2.2f -> Triple("HIPER-INFLAÇÃO CRÍTICA (ALERTA VERMELHO)", BlueprintRed, "Vazões (Faucets) extremamente dominantes. JiCoins perderão valor e criarão colapso no mercado virtual em menos de 20 dias.")
        inflationIndex in 1.4f..2.2f -> Triple("INFLACIONÁRIA MODERADA (RISCO MÉDIO)", BlueprintOrange, "Crescimento sustentado a curto prazo, mas exige eventos de queima de recursos decorrentes de forjarias para evitar desvalorização.")
        inflationIndex in 0.8f..1.4f -> Triple("ESTÁVEL E SAUDÁVEL (PERFEITO BALANCEAMENTO)", BlueprintTeal, "A taxa de retenção por marketplace e quebras de itens balanceia perfeitamente a geração de missões. Perfil MMO ideal.")
        else -> Triple("REPRESSÃO DE LIQUIDEZ (RISCO DE ESTAGNAÇÃO)", BlueprintCyan, "Moedas escassas demais. Os novos jogadores terão dificuldade para pagar upgrades de cinturões e abandonarão o jogo.")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Desenhos de Economia e Balanceamento",
            subtitle = "Sopro de Moedas (Faucets), Escoamentos (Sinks) e Controles de Hiperinflação"
        )

        // Game Economy Overview Board
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(0.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = BlueprintOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VISÃO ECONOMISTA CHEFE (MMO GAMEPLAN)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintOrange,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "No JiuVerse a coexistência da JiCoin (obtenção em jogo) e JiuGem (adquirida via passes e micro-pagamentos) formam um ecossistema complexo regulado por impostos de circulação e auditorias.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Currency types comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // JiCoin Card (Free)
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        ) {
                            Text("🪙", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("JiCoin", fontSize = 12.sp, fontWeight = FontWeight.Black, color = BlueprintTextPrimary)
                            Text("Moeda Gratuita / Soft Currency", fontSize = 8.sp, color = BlueprintTextSecondary)
                        }
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    Text("• Fontes: Tarefas diárias, lutas, eventos síncronos e checkpoints de dojo.", fontSize = 9.sp, color = BlueprintTextSecondary)
                    Text("• Destinação: Ingressos de lutas, taxas administrativas locais da guilda e upgrades de faixas.", fontSize = 9.sp, color = BlueprintTextSecondary)
                }
            }

            // JiuGem Card (Premium)
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E152A)),
                border = BorderStroke_workaround(BlueprintCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(BlueprintOrange.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        ) {
                            Text("💎", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("JiuGem", fontSize = 12.sp, fontWeight = FontWeight.Black, color = BlueprintOrange)
                            Text("Moeda Premium / Hard Currency", fontSize = 8.sp, color = BlueprintTextSecondary)
                        }
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    Text("• Fontes: Compras in-app, renovação de passes de batalha e conquistas extraordinárias.", fontSize = 9.sp, color = BlueprintTextSecondary)
                    Text("• Destinação: Cosméticos premium, alteração de brasões personalizados de academias.", fontSize = 9.sp, color = BlueprintTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Economic Sandbox Tool
        Text(
            text = "1. SIMULADOR ECONÔMICO DINÂMICO (SANDBOX)",
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
                
                // Active CCU / Players Config
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estimativa de Usuários Ativos por Dia", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                    Text("${activePlayersCount.toInt()} Fighters/dia", fontSize = 11.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = activePlayersCount,
                    onValueChange = { activePlayersCount = it },
                    valueRange = 1000f..100000f,
                    colors = SliderDefaults.colors(thumbColor = BlueprintCyan, activeTrackColor = BlueprintCyan, inactiveTrackColor = BlueprintGridLine),
                    modifier = Modifier.height(24.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Faucets and Sinks configs grouped
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Column: Faucets (Geração de JiCoins)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("GERADORES (MINTING FAUCETS)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Daily Quest reward
                        Text("Quest Diária: ${dailyQuestReward.toInt()} 🪙", fontSize = 9.sp, color = BlueprintTextSecondary)
                        Slider(
                            value = dailyQuestReward,
                            onValueChange = { dailyQuestReward = it },
                            valueRange = 100f..2000f,
                            colors = SliderDefaults.colors(thumbColor = BlueprintTeal, activeTrackColor = BlueprintTeal),
                            modifier = Modifier.height(20.dp)
                        )

                        // Check-in reward
                        Text("Check-in Academia: ${academyCheckInReward.toInt()} 🪙", fontSize = 9.sp, color = BlueprintTextSecondary)
                        Slider(
                            value = academyCheckInReward,
                            onValueChange = { academyCheckInReward = it },
                            valueRange = 50f..1000f,
                            colors = SliderDefaults.colors(thumbColor = BlueprintTeal, activeTrackColor = BlueprintTeal),
                            modifier = Modifier.height(20.dp)
                        )

                        // Tournaments pool
                        Text("Copa Semanal: ${tournamentRewardPool.toInt()} 🪙", fontSize = 9.sp, color = BlueprintTextSecondary)
                        Slider(
                            value = tournamentRewardPool,
                            onValueChange = { tournamentRewardPool = it },
                            valueRange = 500f..10000f,
                            colors = SliderDefaults.colors(thumbColor = BlueprintTeal, activeTrackColor = BlueprintTeal),
                            modifier = Modifier.height(20.dp)
                        )
                    }

                    // Right Column: Sinks & Taxes (Escoamento / Queima)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("RETORES (DEFLATIONARY SINKS)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintRed)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Marketplace Tax %
                        Text("Imposto do Mercado: ${marketplaceTaxPercent.toInt()}%", fontSize = 9.sp, color = BlueprintTextSecondary)
                        Slider(
                            value = marketplaceTaxPercent,
                            onValueChange = { marketplaceTaxPercent = it },
                            valueRange = 5f..35f,
                            colors = SliderDefaults.colors(thumbColor = BlueprintRed, activeTrackColor = BlueprintRed),
                            modifier = Modifier.height(20.dp)
                        )

                        // Item Destruction Upgrade Sink Cost
                        Text("Custo de Forja: ${upgradeFailureDeflationCost.toInt()} 🪙", fontSize = 9.sp, color = BlueprintTextSecondary)
                        Slider(
                            value = upgradeFailureDeflationCost,
                            onValueChange = { upgradeFailureDeflationCost = it },
                            valueRange = 200f..5000f,
                            colors = SliderDefaults.colors(thumbColor = BlueprintRed, activeTrackColor = BlueprintRed),
                            modifier = Modifier.height(20.dp)
                        )

                        // Battle Pass Price
                        Text("Passe de Batalha: ${premiumBattlePassPrice.toInt()} 💎", fontSize = 9.sp, color = BlueprintTextSecondary)
                        Slider(
                            value = premiumBattlePassPrice,
                            onValueChange = { premiumBattlePassPrice = it },
                            valueRange = 100f..1000f,
                            colors = SliderDefaults.colors(thumbColor = BlueprintRed, activeTrackColor = BlueprintRed),
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Math results & Diagnostic Box
                Text("PROJEÇÃO DIÁRIA DA MIGRATORIA COM AS CONFIGURAÇÕES ACIMA:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Geração (JiCoins Criadas / Dia)", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                        Text(
                            text = "+${String.format("%,d", totalEstimatedMintedDaily.toLong())} JC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintTeal,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column {
                        Text("Queima (JiCoins Retidas / Dia)", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                        Text(
                            text = "-${String.format("%,d", estimatedDeflatedDailyFromSinks.toLong())} JC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintRed,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column {
                        Text("Balanço Líquido (Remanescente)", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                        Text(
                            text = "${if(economicNetBalance > 0) "+" else ""}${String.format("%,d", economicNetBalance.toLong())} JC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (economicNetBalance > 0) BlueprintOrange else BlueprintTeal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Diagnostic Result container box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, statusColor, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = statusDetails,
                        fontSize = 9.sp,
                        color = BlueprintTextPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Second Section: Interactive Balancing Spreadsheet (Tabela Completa de Balanceamento)
        Text(
            text = "2. PLANILHA DE ECONOMIA E ANTI-EXPLORAÇÃO REGRAS",
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
                // Spreadsheet table headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF020617))
                        .padding(6.dp)
                ) {
                    Text("Atividade/Fluxo", modifier = Modifier.weight(1.3f), fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                    Text("Tipo", modifier = Modifier.weight(0.6f), fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                    Text("Mod", modifier = Modifier.weight(0.6f), fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                    Text("Montante (Base)", modifier = Modifier.weight(0.8f), fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                    Text("Limite Diário", modifier = Modifier.weight(0.9f), fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                    Text("Segurança / Anti-Bot Rule", modifier = Modifier.weight(1.5f), fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                }

                spreadsheetLines.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(row.activityName, modifier = Modifier.weight(1.3f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                        
                        // Type Badge (Faucet vs Sink)
                        Box(modifier = Modifier.weight(0.6f)) {
                            val badgeColor = if (row.type == "FAUCET") BlueprintTeal else BlueprintRed
                            Box(
                                modifier = Modifier
                                    .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(row.type, fontSize = 7.sp, color = badgeColor, fontWeight = FontWeight.Black)
                            }
                        }

                        // Currency Symbol
                        Text(
                            text = if (row.currency == "JiCoin") "🪙" else "💎",
                            modifier = Modifier.weight(0.6f),
                            fontSize = 11.sp
                        )

                        // Reward base
                        Text(
                            text = if(row.baselineReward > 0) "${row.baselineReward}" else "Local Tax",
                            modifier = Modifier.weight(0.8f),
                            fontSize = 10.sp,
                            color = BlueprintTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        // Cap details
                        Text(row.currentCapDaily, modifier = Modifier.weight(0.9f), fontSize = 9.sp, color = BlueprintTextPrimary)

                        // Anti exploit column
                        Text(
                            text = row.antiExploitRule,
                            modifier = Modifier.weight(1.5f),
                            fontSize = 8.5.sp,
                            color = BlueprintCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Download/Export mock trigger action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Formato Ativo de Arquivo: JSON CONFIG", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = BlueprintTextSecondary)
                    
                    Button(
                        onClick = {
                            // Simulator baseline config reload
                            dailyQuestReward = 500f
                            tournamentRewardPool = 2500f
                            academyCheckInReward = 150f
                            marketplaceTaxPercent = 12f
                            upgradeFailureDeflationCost = 1200f
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.testTag("reset_balance_sheet")
                    ) {
                        Text("RESTAURAR PADRÕES MMORPG", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Third Section: Strict Economical Policies and Anti-bot Defenses
        Text(
            text = "3. POLÍTICA DE AUDITORIA E DEFESAS SANITARISTAS",
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
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CONTRAS-MEDIDAS ATIVAS CONTRA FAZENDAS DE BOTS (GOLD FARMING)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "• Verificações de HWID e MacAddress: Impede que um único computador instale emuladores em paralelo para minerar JiCoins por meio de clicks fantasmas em lutas públicas do tatame síncrono.\n" +
                           "• Sandbox Anti-Transações Mula: Itens trocados têm valor sugerido de trading travado por margens de (+/- 20%) de flutuação de mercado das últimas 24 horas. Impede comércio ilegal externo com dinheiro real.\n" +
                           "• Cooldown de Forja Pseudo-randômico: Evita o salvamento em memória de timers de sementes RNG na geração de dados para acertos críticos de forjas e passagens de faixas.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Workaround function for BorderStroke
private fun BorderStroke_workaround(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(0.5.dp, color)
}
