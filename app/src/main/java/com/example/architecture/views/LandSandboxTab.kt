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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ViewModule
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

data class LandmarkItemSim(
    val id: String,
    val name: String,
    val category: String, // "ROOM", "TATAME", "TROPHY", "FURNITURE", "NPC", "SHOP"
    val prestigeReward: Int,
    val costJiCoin: Int,
    val costJiuGem: Int,
    val color: Color
)

@Composable
fun LandSandboxTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // 1. Current land configuration inputs
    var landName by remember { mutableStateOf("Alliance - QG Central") }
    var landSizeMeters by remember { mutableStateOf(15f) } // 10m to 40m
    var isRentalMode by remember { mutableStateOf(false) } // Buy vs Rental check
    var rentDaysLeft by remember { mutableStateOf(7) }

    // Currencies
    var walletJiCoins by remember { mutableStateOf(85000) }
    var walletJiuGems by remember { mutableStateOf(4200) }

    // 2. Deco catalog items definition
    val catalogItems = remember {
        listOf(
            LandmarkItemSim("d1", "Sala Principal de Treino", "ROOM", 300, 4000, 0, BlueprintCyan),
            LandmarkItemSim("d2", "Vestiário Clássico com Ducha", "ROOM", 150, 2000, 0, BlueprintCyan),
            LandmarkItemSim("d3", "Tatame Olímpico Premium Azul", "TATAME", 180, 2500, 0, BlueprintTeal),
            LandmarkItemSim("d4", "Tatame Gracie Tradicional Amarelo", "TATAME", 250, 0, 150, BlueprintTeal),
            LandmarkItemSim("d5", "Medalheiro de Ouro Gracie", "TROPHY", 500, 10000, 0, BlueprintOrange),
            LandmarkItemSim("d6", "Taça Mundial IBJJF", "TROPHY", 600, 12000, 0, BlueprintOrange),
            LandmarkItemSim("d7", "Armários de Madeira do Dojo", "FURNITURE", 40, 600, 0, Color(0xFF8B5A2B)),
            LandmarkItemSim("d8", "Banco de Convivência Pós-Roll", "FURNITURE", 30, 450, 0, Color(0xFF8B5A2B)),
            LandmarkItemSim("d9", "Mestre Welcoming NPC (Interações)", "NPC", 200, 3000, 0, Color(0xFF8B5CF6)),
            LandmarkItemSim("d10", "Faxineiro Dedicado (Gera Higiene +100%)", "NPC", 120, 1500, 0, Color(0xFF8B5CF6)),
            LandmarkItemSim("d11", "Quiosque de Açaí da Esquina", "SHOP", 450, 8000, 250, BlueprintOrange),
            LandmarkItemSim("d12", "Stand de Rashguards de Compressão", "SHOP", 350, 6500, 180, BlueprintOrange)
        )
    }

    // 3. User's active decorations placed on-grid
    val placedDecorations = remember {
        mutableStateListOf(
            catalogItems[0], // Sala Principal
            catalogItems[2], // Tatame Olímpico
            catalogItems[4], // Medalheiro Gracie
            catalogItems[9]  // Faxineiro dedicado NPC
        )
    }

    // Mathematical derivations
    val totalPrestigePoints = placedDecorations.sumOf { it.prestigeReward } + (landSizeMeters * 8).toInt()
    val dailyMaintenanceCost = (placedDecorations.size * 80) + (landSizeMeters * 20).toInt()
    val maxNPCAllowance = (landSizeMeters / 4).toInt()
    
    val placedNPCsCount = placedDecorations.count { it.category == "NPC" }
    val placedShopsCount = placedDecorations.count { it.category == "SHOP" }

    // Purchase calculations for land expansion
    val baseExpansionCost = (landSizeMeters * landSizeMeters * 100).toInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Sistema de Terrenos Sandbox & Construtor",
            subtitle = "Arquitetura Espacial de Academias, Aluguéis, Tatames, Lojas e NPCs Proprietários"
        )

        // Sandbox land general statement
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(0.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VISÃO ARQUITETO DE SANDBOX (LAND DESIGNER)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintTeal,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "No JiuVerse, cada jogador pode adquirir e gerenciar um lote territorial isométrico em servidores fragmentados, transformando propriedades brutas em polos mundiais de treinamento.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // HUD Wallet values
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlueprintCard, RoundedCornerShape(6.dp))
                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(6.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("RECURSOS URBANOS:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("🪙 ${String.format("%,d", walletJiCoins)} JC", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal, fontFamily = FontFamily.Monospace)
                Text("💎 ${String.format("%,d", walletJiuGems)} JG", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: Land Acquisition, Expansion & Rental
        Text(
            text = "1. REGISTRO TERRITORIAL & MÓDULO ALUGUÉL",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Card: Land Properties
            Card(
                modifier = Modifier.weight(1.1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Configuração de Lote", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                        Box(
                            modifier = Modifier
                                .background(if (isRentalMode) BlueprintOrange.copy(alpha = 0.2f) else BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isRentalMode) "ARRENDAMENTO ATIVO" else "PROPRIETÁRIO DIRETO",
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isRentalMode) BlueprintOrange else BlueprintTeal
                            )
                        }
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    Text("Nome da Academia / Terreno", fontSize = 9.sp, color = BlueprintTextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text(landName, fontSize = 10.sp, color = BlueprintTextPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Size slider with metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tamanho da Grid Quadrada", fontSize = 9.sp, color = BlueprintTextSecondary)
                        Text("${landSizeMeters.toInt()}m x ${landSizeMeters.toInt()}m", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                    }
                    Slider(
                        value = landSizeMeters,
                        onValueChange = { landSizeMeters = it },
                        valueRange = 10f..40f,
                        colors = SliderDefaults.colors(thumbColor = BlueprintCyan, activeTrackColor = BlueprintCyan, inactiveTrackColor = BlueprintGridLine),
                        modifier = Modifier.height(20.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Expand limits rule
                    Text(
                        text = "• Custos de Expansão: ${baseExpansionCost} JC para subir para o próximo nível de grid.\n• Limites: Máximo de 40m para evitar lag de ordenação em celulares.",
                        fontSize = 8.sp,
                        color = BlueprintTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rent toggle trigger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Modo Arrendamento / Aluguel", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            Text("Paga taxas diárias reduzidas", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(if (isRentalMode) BlueprintOrange else Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                .clickable { isRentalMode = !isRentalMode }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isRentalMode) "ALUGUEL LIGADO" else "ALTERAR P/ ALUGAR",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRentalMode) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            // Right Card: Dynamic metrics and limits
            Card(
                modifier = Modifier.weight(0.9f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C191E)),
                border = BorderStroke_workaround(BlueprintCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("ESTADIA E MÉTRICAS DE DOJO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Pontos de Prestígio", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Text(
                                text = "${totalPrestigePoints} Pts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintOrange,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column {
                            Text("Maintence Diária", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Text(
                                text = "${dailyMaintenanceCost} JC/dia",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Restrições Ativas de Grid:", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                    Text("• NPCs Máximos permitidos: ${maxNPCAllowance} (Instalados: ${placedNPCsCount})", fontSize = 8.sp, color = BlueprintTextPrimary)
                    Text("• Lojas Comerciais arrendadas: Max 3 (Instaladas: ${placedShopsCount})", fontSize = 8.sp, color = BlueprintTextPrimary)
                    Text("• Limite de Salas simultâneas: Max 4", fontSize = 8.sp, color = BlueprintTextPrimary)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated trigger of expanding size
                    Button(
                        onClick = {
                            if (walletJiCoins >= 10000 && landSizeMeters < 40f) {
                                walletJiCoins -= 10000
                                landSizeMeters = Math.min(40f, landSizeMeters + 5f)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().testTag("expand_land_size_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Expand, contentDescription = null, tint = Color.Black, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EXPANDIR TERRENO (+5m)", fontSize = 8.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Interactive Dojo Decoration Sandbox
        Text(
            text = "2. MONTAGEM INTERATIVA DO DOJO (DECORAÇÃO E CONSTRUÇÃO)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Placed itens in our Academy Grid visual layout
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Disposição da Grid Isometrica (Itens Ativos)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Minimal visual dojotatami layout representation
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("PORTAL DE ENTRADA DO TATAME", fontSize = 8.sp, color = BlueprintTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                        // Display active segments row by row
                        placedDecorations.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(item.color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, item.color, RoundedCornerShape(4.dp))
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when(item.category) {
                                            "ROOM" -> Icons.Default.Apartment
                                            "TATAME" -> Icons.Default.Layers
                                            "TROPHY" -> Icons.Default.EmojiEvents
                                            "NPC" -> Icons.Default.People
                                            "SHOP" -> Icons.Default.Storefront
                                            else -> Icons.Default.ViewModule
                                        },
                                        contentDescription = null,
                                        tint = item.color,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(item.name, fontSize = 9.sp, color = BlueprintTextPrimary, fontWeight = FontWeight.Bold)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("+${item.prestigeReward} Prestige", fontSize = 8.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remover",
                                        tint = BlueprintRed,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                placedDecorations.removeAt(index)
                                            }
                                    )
                                }
                            }
                        }

                        if (placedDecorations.isEmpty()) {
                            Text(
                                text = "Seu Tatame está plano e vazio! Compre itens do catálogo ao lado para inaugurar suas atividades síncronas.",
                                fontSize = 8.5.sp,
                                color = BlueprintTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Right column: catalog store to buy decorations
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Catálogo da Prefeitura (Comprar)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        modifier = Modifier.height(180.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        catalogItems.forEach { item ->
                            val canAffordJC = item.costJiCoin == 0 || walletJiCoins >= item.costJiCoin
                            val canAffordJG = item.costJiuGem == 0 || walletJiuGems >= item.costJiuGem

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable {
                                        if (canAffordJC && canAffordJG) {
                                            walletJiCoins -= item.costJiCoin
                                            walletJiuGems -= item.costJiuGem
                                            placedDecorations.add(item)
                                        }
                                    }
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                    Text(
                                        text = if(item.costJiuGem > 0) "💎 ${item.costJiuGem} Gems" else "🪙 ${item.costJiCoin} Coins",
                                        fontSize = 8.sp,
                                        color = if (item.costJiuGem > 0) BlueprintOrange else BlueprintTeal
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(if (canAffordJC && canAffordJG) BlueprintTeal.copy(alpha = 0.2f) else BlueprintRed.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "+${item.prestigeReward}P",
                                        fontSize = 7.5.sp,
                                        color = if (canAffordJC && canAffordJG) BlueprintTeal else BlueprintRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Tech Architecture, Data Models and Security Filters
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3. MODELAGEM PRISMA-SQL & HIERARQUIA DE PERMISSÃO",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Prisma DB schema
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                border = BorderStroke_workaround(BlueprintCyan.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("PRISMA SCHEMA DIAGRAM (POSTGRESQL)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "model Land {\n" +
                               "  id           String   @id @default(uuid())\n" +
                               "  ownerId      String\n" +
                               "  sizeMeters   Float    @default(15.0)\n" +
                               "  coordinateX  Float\n" +
                               "  coordinateY  Float\n" +
                               "  rentRateDaily Int     @default(50)\n" +
                               "  decorations  LandDecoration[]\n" +
                               "}\n\n" +
                               "model LandDecoration {\n" +
                               "  id        String @id @default(uuid())\n" +
                               "  landId    String\n" +
                               "  itemType  String\n" +
                               "  gridX     Int\n" +
                               "  gridY     Int\n" +
                               "  rotation  Int    @default(0)\n" +
                               "  land      Land   @relation(fields: [landId], references: [id])\n" +
                               "}",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BlueprintTextPrimary
                    )
                }
            }

            // Permission Scheme
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = BlueprintOrange, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("REGRAS DE CONCORRÊNCIA E CONTROLE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                    Text("Permissões de Terreno por Tag de Cargo:", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                    
                    Text("• DONO (Owner):\n  Total controle do layout, vendas e expulsão.", fontSize = 8.sp, color = BlueprintTextSecondary)
                    Text("• CO-DONO / INSTRUTOR:\n  Pode posicionar tapetes e ligar cronômetros.", fontSize = 8.sp, color = BlueprintTextSecondary)
                    Text("• ALUNO (Student):\n  Acesso livre, consome açaí sem precisar convites.", fontSize = 8.sp, color = BlueprintTextSecondary)
                    Text("• VISITANTE (Guest):\n  Assistir sparring em silêncio. Sem permissão de edição.", fontSize = 8.sp, color = BlueprintTextSecondary)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF3B1E28), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintRed, RoundedCornerShape(4.dp))
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BlueprintRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ANTI-DUPE: O motor bloqueia transações simultâneas de reuso de IDs únicos de Kimonos/Trofés colocados em salas.",
                            fontSize = 7.5.sp,
                            color = BlueprintTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// BorderStroke workaround
private fun BorderStroke_workaround(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(0.5.dp, color)
}
