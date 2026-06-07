package com.example.architecture.views

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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

// --- Core Data Models ---
data class AcademyElement(
    val id: String,
    val name: String,
    val category: String, // "ROOM", "TATAME", "FURNITURE", "TROPHY", "RECEPTION", "STORE"
    val prestige: Int,
    val costCoins: Int,
    val costGems: Int,
    val color: Color,
    val icon: ImageVector,
    val description: String = ""
)

data class PlacedGridItem(
    val id: String,
    val element: AcademyElement,
    val x: Int,
    val y: Int,
    var rotation: Int = 0 // 0, 90, 180, 270
)

data class TerrainPreset(
    val id: String,
    val name: String,
    val themeColorHex: Long,
    val baseRentRate: Int,
    val visualDesc: String,
    val terrainType: String // "BEACH", "MOUNTAIN", "TEMPLE", "METROPOLIS"
)

data class PermissionSet(
    val roleName: String,
    val hasEditGrid: Boolean,
    val hasWithdrawStore: Boolean,
    val hasInviteGuests: Boolean,
    val hasChangeTheme: Boolean
)

@Composable
fun LandSandboxTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 1. Terrain Presets
    val terrains = remember {
        listOf(
            TerrainPreset("t_beach", "Costa de Angra dos Reis", 0xFFEAB308, 50, "Areia dourada e coqueiros costeiros.", "BEACH"),
            TerrainPreset("t_mountain", "Pico Celestial Petrópolis", 0xFF6366F1, 120, "Atmosfera fria nas altitudes com névoa.", "MOUNTAIN"),
            TerrainPreset("t_temple", "Dojô Imperial Tradicional", 0xFFEF4444, 200, "Cerejeiras e portais Shinto ancestrais.", "TEMPLE"),
            TerrainPreset("t_metropolis", "Selva de Pedra - Rio Centro", 0xFF64748B, 80, "Estilo industrial, asfalto urbano e grafites.", "METROPOLIS")
        )
    }

    var selectedTerrainState by remember { mutableStateOf(terrains[2]) }
    var landName by remember { mutableStateOf("Dojô Gracie - Imperial Kyoto") }
    var landSizeMeters by remember { mutableStateOf(15f) } 
    var isRentalMode by remember { mutableStateOf(true) }
    var rentDaysLeft by remember { mutableStateOf(14) }

    // Currencies
    var walletJiCoins by remember { mutableStateOf(92500) }
    var walletJiuGems by remember { mutableStateOf(5800) }

    // 2. All Buyable Catalog Items
    val allCatalogElements = remember {
        listOf(
            // Modular Rooms
            AcademyElement("r1", "Sala Central Carlson Gracie", "ROOM", 500, 5000, 0, BlueprintCyan, Icons.Default.Apartment, "Sala principal ampla otimizada para rolls síncronos."),
            AcademyElement("r2", "Vestiário Conectado com Ducha", "ROOM", 150, 2200, 0, BlueprintCyan, Icons.Default.CleaningServices, "Vestiário higienizado com regeneração de stamina."),
            AcademyElement("r3", "Área de Spa & Crioterapia UGC", "ROOM", 300, 3500, 150, BlueprintCyan, Icons.Default.LocalActivity, "Recuperação de fadiga rápida pós-combate."),

            // Tatames (Mats)
            AcademyElement("m1", "Tatame Gracie Amarelo Clássico", "TATAME", 120, 1500, 0, BlueprintTeal, Icons.Default.Layers, "Lona vinílica clássica de alta aderência tradicional."),
            AcademyElement("m2", "Tatame Alliance Azul Marinho", "TATAME", 185, 2000, 0, BlueprintTeal, Icons.Default.Layers, "Material importado olímpico antiderrapante."),
            AcademyElement("m3", "Tatame Black Belt Grafite", "TATAME", 250, 2500, 100, BlueprintTeal, Icons.Default.Layers, "Tatame isolante premium de absorção tátil de impacto."),

            // Decor & Furniture
            AcademyElement("f1", "Banco de Convivência Maciço", "FURNITURE", 40, 400, 0, Color(0xFFEAB308), Icons.Default.Home, "Banco rústico para atletas aguardando o cronômetro."),
            AcademyElement("f2", "Saco de Pancadas Vintage Couro", "FURNITURE", 80, 800, 0, Color(0xFFEAB308), Icons.Default.SportsMartialArts, "Treino solo de raspadas ou simulações musculares."),
            AcademyElement("f3", "Bebedouro Gelado Inox 50 Litros", "FURNITURE", 60, 650, 0, Color(0xFFEAB308), Icons.Default.Home, "Água filtrada gelada livre para recuperar o fôlego."),

            // Trophies
            AcademyElement("t1", "Cinturão Mundial Absoluto IBJJF", "TROPHY", 1200, 10000, 350, BlueprintOrange, Icons.Default.EmojiEvents, "O ápice do orgulho competitivo do clã no tatame."),
            AcademyElement("t2", "Medalheiro Gracie de Ouro", "TROPHY", 500, 6000, 0, BlueprintOrange, Icons.Default.Shield, "Fixação de metal com conquistas históricas do servidor."),

            // Receptions
            AcademyElement("rp1", "Balcão Administrativo Principal", "RECEPTION", 200, 1800, 0, Color(0xFF8B5CF6), Icons.Default.Business, "Recepção para o check-in e faturamento de matrículas."),
            AcademyElement("rp2", "Catraca Biométrica Anticheat NFC", "RECEPTION", 150, 1200, 0, Color(0xFF8B5CF6), Icons.Default.Security, "Garante que apenas alunos adentrem o duto de treino."),

            // Own Store (Loja Própria)
            AcademyElement("s1", "Boutique de Kimonos JiuVerse", "STORE", 450, 4500, 150, Color(0xFFEC4899), Icons.Default.Storefront, "Stand de vendas de Kimonos e Faixas raros com comissões."),
            AcademyElement("s2", "Quiosque de Açaí Turbinado", "STORE", 350, 3000, 0, Color(0xFFEC4899), Icons.Default.ShoppingBag, "Fornecedor vital de carboidrato e suco pós-treino.")
        )
    }

    // Unplaced Furniture Inventory (bought but not placed on grid yet)
    val inventoryBalances = remember {
        mutableStateMapOf<String, Int>().apply {
            put("r1", 1) // Starts with 1 Room
            put("m1", 3) // Starts with 3 yellow mats
            put("t2", 1) // Starts with 1 Medal Display
            put("rp1", 1) // Starts with 1 Reception
            put("s2", 1) // Starts with 1 açaí stand
        }
    }

    // Active placed coordinates mapping (10x10 Grid System)
    val placedItems = remember {
        mutableStateListOf<PlacedGridItem>().apply {
            // Initial layout pre-placed for premium visual look
            add(PlacedGridItem("p_init_1", allCatalogElements[0], 2, 2, 0)) // Carlson Gracie Room
            add(PlacedGridItem("p_init_2", allCatalogElements[3], 3, 2, 0)) // Classical Mat
            add(PlacedGridItem("p_init_3", allCatalogElements[3], 3, 3, 0)) // Classical Mat
            add(PlacedGridItem("p_init_4", allCatalogElements[10], 1, 1, 90)) // Reception Desk
            add(PlacedGridItem("p_init_5", allCatalogElements[13], 2, 5, 180)) // Açaí stall
        }
    }

    // Selected Brush (The item the user selected in inventory to paint)
    var activeBrushId by remember { mutableStateOf<String?>("m1") } // Yellow Mat selected initially
    var editorMode by remember { mutableStateOf("BRUSH") } // "BRUSH" or "ERASER"

    // Focused Cell Inspector State
    var focusedCellX by remember { mutableStateOf(2) }
    var focusedCellY by remember { mutableStateOf(2) }

    // Multi-tab section selection on the Right Panel
    var rightTabSelected by remember { mutableStateOf("SHOP") } // "SHOP", "INVENTORY", "PERMISSIONS", "SQL_DB", "PHASER"

    // SQL database live logging state
    val sqlLogLines = remember {
        mutableStateListOf(
            "[SQL-INIT] Banco SQLite (via Room API) montado com sucesso.",
            "[INFO] SQLite carregou 5 instâncias de mobílias ativas do lote Alliance."
        )
    }

    // Role Permissions custom settings
    val rolesPermissions = remember {
        mutableStateListOf(
            PermissionSet("DONO (Owner)", true, true, true, true),
            PermissionSet("CO-DONO (Co-Owner)", true, false, true, true),
            PermissionSet("COACH / INSTRUTOR", true, false, true, false),
            PermissionSet("ALUNO (Student)", false, false, false, false),
            PermissionSet("VISITANTE", false, false, false, false)
        )
    }

    // Mathematical calculus
    val activePrestige = remember(placedItems.size, landSizeMeters) {
        val base = (landSizeMeters * 6).toInt()
        val itemsPoints = placedItems.sumOf { it.element.prestige }
        base + itemsPoints
    }

    val dailyMaintenance = remember(placedItems.size, landSizeMeters, isRentalMode) {
        val baseLandFee = (landSizeMeters * 15).toInt()
        val itemsFee = placedItems.size * 65
        val terrainMod = selectedTerrainState.baseRentRate
        val multiplier = if (isRentalMode) 0.7f else 1.2f // Rental mode has discount on maintenance but pays rent
        ((baseLandFee + itemsFee + terrainMod) * multiplier).toInt()
    }

    val totalPlacedRooms = placedItems.count { it.element.category == "ROOM" }
    val totalPlacedMats = placedItems.count { it.element.category == "TATAME" }
    val totalPlacedTrophies = placedItems.count { it.element.category == "TROPHY" }
    val totalPlacedReceptions = placedItems.count { it.element.category == "RECEPTION" }
    val totalPlacedStores = placedItems.count { it.element.category == "STORE" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(12.dp)
            .verticalScroll(scrollState)
    ) {
        // High fidelity blueprint title section
        SectionHeader(
            title = "Dojo Builder UGC & Terrenos Sandbox",
            subtitle = "Arquitetura e Planejamento Isométrico de Lotes Territoriais Autorais de Servidor"
        )

        // Row 1: HUD Stats Block
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlueprintCard, RoundedCornerShape(8.dp))
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ViewModule,
                    contentDescription = null,
                    tint = BlueprintCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PAINEL DE ENGENHARIA DE LOTES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = landName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintTextPrimary
                    )
                }
            }

            // Wallet counters
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("SALDO COINS", fontSize = 8.sp, color = BlueprintTextSecondary)
                    Text("🪙 ${String.format("%,d", walletJiCoins)} JC", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("GEMA SÍNDICO", fontSize = 8.sp, color = BlueprintTextSecondary)
                    Text("💎 ${String.format("%,d", walletJiuGems)} JG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Land Configuration (Terrain Preset, Rental Configuration, Lot Size Slider)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Card: Terrain Preset selector
            Card(
                modifier = Modifier.weight(1.1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("1. SELECIONAR PRESET DO TERRENO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                    Text("Invalida ou readequa a iluminação de shader e ambientação no Phaser 3.", fontSize = 9.sp, color = BlueprintTextSecondary, modifier = Modifier.padding(bottom = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        terrains.forEach { t ->
                            val isSelected = selectedTerrainState.id == t.id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) Color(t.themeColorHex).copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.4f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(t.themeColorHex) else BlueprintGridLine,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { selectedTerrainState = t }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = t.name.split(" ").last(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(t.themeColorHex) else BlueprintTextPrimary
                                    )
                                    Text(
                                        text = "${t.baseRentRate} JC",
                                        fontSize = 8.sp,
                                        color = BlueprintTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Foco Visual: ${selectedTerrainState.visualDesc}",
                        fontSize = 9.sp,
                        color = BlueprintTextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    )
                }
            }

            // Right Card: Rental settings
            Card(
                modifier = Modifier.weight(0.9f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SISTEMA DE OCUPAÇÃO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                        Box(
                            modifier = Modifier
                                .background(if (isRentalMode) BlueprintOrange.copy(alpha = 0.2f) else BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isRentalMode) "ARRENDADO (ALUGUEL)" else "PROPRIEDADE DIRETA",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isRentalMode) BlueprintOrange else BlueprintTeal
                            )
                        }
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isRentalMode) "Restam $rentDaysLeft dias de aluguel" else "Taxa de Imposto Territorial Pago",
                                fontSize = 9.sp,
                                color = BlueprintTextPrimary
                            )
                            Text(
                                text = if (isRentalMode) "Custo do Lote: ${selectedTerrainState.baseRentRate} JC/dia" else "Isento de Arrendamento",
                                fontSize = 8.sp,
                                color = BlueprintTextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                if (isRentalMode) {
                                    if (walletJiCoins >= 500) {
                                        walletJiCoins -= 500
                                        rentDaysLeft += 7
                                        Toast.makeText(context, "Aluguel renovado por +7 dias!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    isRentalMode = true
                                    rentDaysLeft = 7
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isRentalMode) BlueprintOrange else BlueprintTeal),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = if (isRentalMode) "RENOVAR" else "MIGRAR",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Área Expandida: ${landSizeMeters.toInt()}m x ${landSizeMeters.toInt()}m", fontSize = 9.sp, color = BlueprintTextSecondary)
                        Text("Max: 40m", fontSize = 8.sp, color = BlueprintCyan)
                    }

                    Slider(
                        value = landSizeMeters,
                        onValueChange = { landSizeMeters = it },
                        valueRange = 10f..40f,
                        colors = SliderDefaults.colors(
                            thumbColor = BlueprintCyan,
                            activeTrackColor = BlueprintCyan,
                            inactiveTrackColor = BlueprintGridLine
                        ),
                        modifier = Modifier.height(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: Live Visual Editor Sandbox + Grid (The main feature requested!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left block (Width 1.1x): Visual Canvas Inspector and Editor grid
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
                        Column {
                            Text("2. MAPA SÍNCRONO & EDITOR DE ACADEMIAS UGC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            Text("Clique nos blocos para pintar o item ou inspecionar o relevo", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                        }

                        // Brush / Eraser tool toggles
                        Row(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(if (editorMode == "BRUSH") BlueprintTeal.copy(alpha = 0.25f) else Color.Transparent, RoundedCornerShape(3.dp))
                                    .clickable { editorMode = "BRUSH" }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Brush, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Pincel", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(if (editorMode == "ERASER") BlueprintRed.copy(alpha = 0.25f) else Color.Transparent, RoundedCornerShape(3.dp))
                                    .clickable { editorMode = "ERASER" }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = BlueprintRed, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Borracha", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintRed)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 10x10 Cellular grid rendering representing Phaser 3 coordinates logic
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070F13), RoundedCornerShape(6.dp))
                            .border(1.dp, BlueprintGridLine, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (y in 0 until 10) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (x in 0 until 10) {
                                    val itemAtCell = placedItems.firstOrNull { it.x == x && it.y == y }
                                    val isFocused = focusedCellX == x && focusedCellY == y
                                    
                                    val cellTint = when {
                                        itemAtCell != null -> itemAtCell.element.color.copy(alpha = 0.8f)
                                        isFocused -> BlueprintCyan.copy(alpha = 0.2f)
                                        (x + y) % 2 == 0 -> Color(0xFF0F1C24)
                                        else -> Color(0xFF0C161D)
                                    }

                                    val borderStrokeColor = when {
                                        isFocused -> BlueprintCyan
                                        itemAtCell != null -> itemAtCell.element.color
                                        else -> BlueprintGridLine.copy(alpha = 0.2f)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(24.dp)
                                            .background(cellTint, RoundedCornerShape(2.dp))
                                            .border(0.5.dp, borderStrokeColor, RoundedCornerShape(2.dp))
                                            .clickable {
                                                focusedCellX = x
                                                focusedCellY = y

                                                if (editorMode == "ERASER") {
                                                    if (itemAtCell != null) {
                                                        sqlLogLines.add("[SQL-ADMIN] DELETE FROM dojo_items WHERE x = $x AND y = $y;")
                                                        // Return block to inventory
                                                        val elId = itemAtCell.element.id
                                                        inventoryBalances[elId] = (inventoryBalances[elId] ?: 0) + 1
                                                        placedItems.remove(itemAtCell)
                                                        Toast.makeText(context, "${itemAtCell.element.name} reciclado de $x,$y para o Inventário!", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    // Painting brush mode
                                                    if (itemAtCell == null) {
                                                        val activeBrush = activeBrushId
                                                        if (activeBrush != null) {
                                                            val invQty = inventoryBalances[activeBrush] ?: 0
                                                            if (invQty > 0) {
                                                                // Deduct from inventory
                                                                inventoryBalances[activeBrush] = invQty - 1
                                                                // Place on layout
                                                                val element = allCatalogElements.first { it.id == activeBrush }
                                                                val newItem = PlacedGridItem("p_${System.currentTimeMillis()}", element, x, y, 0)
                                                                placedItems.add(newItem)

                                                                sqlLogLines.add("[SQL-CREATE] INSERT INTO dojo_items (land_id, element_id, x, y) VALUES ('Alliance-HQ', '${element.id}', $x, $y);")
                                                                Toast.makeText(context, "${element.name} colocado com sucesso em X:$x, Y:$y!", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, "Nenhum material sobrando no inventário! Compre na Aba Loja.", Toast.LENGTH_LONG).show()
                                                            }
                                                        } else {
                                                            Toast.makeText(context, "Selecione primeiro um material atômico abaixo!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        Toast.makeText(context, "Cesta ocupada! Remova este item antes usando a Borracha.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (itemAtCell != null) {
                                            Icon(
                                                imageVector = itemAtCell.element.icon,
                                                contentDescription = itemAtCell.element.name,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "$x,$y",
                                                fontSize = 6.sp,
                                                color = BlueprintTextSecondary.copy(alpha = 0.35f),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Map legend indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LegendBadge("Sala", BlueprintCyan)
                            LegendBadge("Tatame", BlueprintTeal)
                            LegendBadge("Modulares", Color(0xFFD97706))
                            LegendBadge("Recepção", Color(0xFF8B5CF6))
                            LegendBadge("Loja", Color(0xFFEC4899))
                        }

                        Button(
                            onClick = {
                                placedItems.forEach { item ->
                                    val elId = item.element.id
                                    inventoryBalances[elId] = (inventoryBalances[elId] ?: 0) + 1
                                }
                                placedItems.clear()
                                sqlLogLines.add("[SQL-TRUNCATE] DELETE FROM dojo_items WHERE land_id='Alliance-HQ';")
                                Toast.makeText(context, "Tatame limpo! Todo mobiliário voltou ao Inventário.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueprintRed.copy(alpha = 0.15f), contentColor = BlueprintRed),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpar Grid", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cell detailed inspection card below grid
                    val inspectedGridItem = placedItems.firstOrNull { it.x == focusedCellX && it.y == focusedCellY }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        if (inspectedGridItem != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = inspectedGridItem.element.icon,
                                        contentDescription = null,
                                        tint = inspectedGridItem.element.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = inspectedGridItem.element.name,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BlueprintTextPrimary
                                        )
                                        Text(
                                            text = "Coord: X: ${inspectedGridItem.x}, Y: ${inspectedGridItem.y} — Prestígio: +${inspectedGridItem.element.prestige} pts",
                                            fontSize = 8.5.sp,
                                            color = BlueprintTextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Rotation changer
                                    Text(
                                        text = "${inspectedGridItem.rotation}°",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BlueprintCyan,
                                        modifier = Modifier
                                            .background(BlueprintCyan.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                            .clickable {
                                                val nextRot = (inspectedGridItem.rotation + 90) % 360
                                                inspectedGridItem.rotation = nextRot
                                                // Trigger list update force recompose
                                                val listShadow = placedItems.toList()
                                                placedItems.clear()
                                                placedItems.addAll(listShadow)
                                                Toast.makeText(context, "Item rotacionado para $nextRot°!", Toast.LENGTH_SHORT).show()
                                                sqlLogLines.add("[SQL-UPDATE] UPDATE dojo_items SET rotation = $nextRot WHERE x=${inspectedGridItem.x} AND y=${inspectedGridItem.y};")
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    )

                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = BlueprintRed,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                val elId = inspectedGridItem.element.id
                                                inventoryBalances[elId] = (inventoryBalances[elId] ?: 0) + 1
                                                placedItems.remove(inspectedGridItem)
                                                sqlLogLines.add("[SQL-ADMIN] DELETE FROM dojo_items WHERE x = ${focusedCellX} AND y = ${focusedCellY};")
                                            }
                                    )
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Célula vazia em X: $focusedCellX, Y: $focusedCellY. Selecione um material no inventário ao lado e clique aqui para construir.",
                                    fontSize = 9.sp,
                                    color = BlueprintTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Right block (Width 0.9x): Sub-Tabs module workspace (Shop, Inventário, Permissoes, SQL Prisma, Phaser Exporter)
            Card(
                modifier = Modifier.weight(0.9f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Capsules navigation row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        SubTabNavigatorCapsule("LOJA", rightTabSelected == "SHOP") { rightTabSelected = "SHOP" }
                        SubTabNavigatorCapsule("INV", rightTabSelected == "INVENTORY") { rightTabSelected = "INVENTORY" }
                        SubTabNavigatorCapsule("CARGOS", rightTabSelected == "PERMISSIONS") { rightTabSelected = "PERMISSIONS" }
                        SubTabNavigatorCapsule("SQL", rightTabSelected == "SQL_DB") { rightTabSelected = "SQL_DB" }
                        SubTabNavigatorCapsule("PHASER", rightTabSelected == "PHASER") { rightTabSelected = "PHASER" }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (rightTabSelected) {
                        "SHOP" -> {
                            Text("CATÁLOGO DE CONSTRUÇÃO DO MUNICÍPIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                            Text("Compre materiais modulares de alto padrão para seu dojo. As aquisições geram pontos de prestígio imediato e vão direto para seu Inventário local.", fontSize = 8.5.sp, color = BlueprintTextSecondary, modifier = Modifier.padding(bottom = 8.dp))

                            Column(
                                modifier = Modifier.height(290.dp).verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                allCatalogElements.forEach { item ->
                                    val canAffordCoins = walletJiCoins >= item.costCoins
                                    val canAffordGems = walletJiuGems >= item.costGems

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(item.name, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                            }
                                            Text(item.description, fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                            
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 2.dp)) {
                                                if (item.costCoins > 0) {
                                                    Text("🪙 ${String.format("%,d", item.costCoins)} JC", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                                                }
                                                if (item.costGems > 0) {
                                                    Text("💎 ${item.costGems} Gems", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                                                }
                                                Text("+${item.prestige} Prestígio", fontSize = 8.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (canAffordCoins && canAffordGems) {
                                                    walletJiCoins -= item.costCoins
                                                    walletJiuGems -= item.costGems
                                                    inventoryBalances[item.id] = (inventoryBalances[item.id] ?: 0) + 1
                                                    Toast.makeText(context, "${item.name} adicionado ao Inventário!", Toast.LENGTH_SHORT).show()
                                                    sqlLogLines.add("[SQL-BUY] INSERT INTO user_inventory (element_id, status) VALUES ('${item.id}', 'RESERVED');")
                                                } else {
                                                    Toast.makeText(context, "Recursos insuficientes na carteira urbana!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (canAffordCoins && canAffordGems) BlueprintTeal else BlueprintRed.copy(alpha = 0.2f),
                                                contentColor = if (canAffordCoins && canAffordGems) Color.Black else BlueprintRed
                                            ),
                                            shape = RoundedCornerShape(2.dp),
                                            modifier = Modifier.height(24.dp).testTag("buy_item_${item.id}")
                                        ) {
                                            Text("COMPRAR", fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        "INVENTORY" -> {
                            Text("INVENTÁRIO LOCAL DE MÓVEIS UGC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            Text("Mostra itens comprados disponíveis para posicionamento no tatame quadrado. Clique em um item para carregar seu pincel e colocá-lo clicando no grid.", fontSize = 8.5.sp, color = BlueprintTextSecondary, modifier = Modifier.padding(bottom = 8.dp))

                            val inventoryItemsList = allCatalogElements.filter { (inventoryBalances[it.id] ?: 0) > 0 }

                            if (inventoryItemsList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Nenhum mobiliário sobrando. Visite a Aba LOJA para comprar novos tatames, salas ou lojas!",
                                        fontSize = 9.sp,
                                        color = BlueprintTextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.height(270.dp).verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    inventoryItemsList.forEach { element ->
                                        val qty = inventoryBalances[element.id] ?: 0
                                        val isBrushActive = activeBrushId == element.id

                                        val cardBg = if (isBrushActive) Color(0xFF142F3F) else Color.Black.copy(alpha = 0.3f)
                                        val cardBorder = if (isBrushActive) BlueprintCyan else BlueprintGridLine

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(cardBg, RoundedCornerShape(4.dp))
                                                .border(0.5.dp, cardBorder, RoundedCornerShape(4.dp))
                                                .clickable {
                                                    activeBrushId = element.id
                                                    editorMode = "BRUSH"
                                                    Toast.makeText(context, "Pincel alterado para: ${element.name}!", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.1f)) {
                                                Icon(
                                                    imageVector = element.icon,
                                                    contentDescription = null,
                                                    tint = element.color,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text(element.name, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                                    Text("Prestige: +${element.prestige} p — Tipo: ${element.category}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = "Qtd: $qty",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BlueprintTeal,
                                                    modifier = Modifier
                                                        .background(BlueprintTeal.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                )

                                                if (isBrushActive) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(BlueprintCyan.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("ATIVADO", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "PERMISSIONS" -> {
                            Text("REGRAS DE CONCORRÊNCIA E PERMISSÕES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                            Text("Assinale privilégios para cargos hierárquicos ao entrar nas academias. O motor assegura que apenas quem tem permissão atue no layout.", fontSize = 8.5.sp, color = BlueprintTextSecondary, modifier = Modifier.padding(bottom = 8.dp))

                            Column(
                                modifier = Modifier.height(270.dp).verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rolesPermissions.forEachIndexed { index, role ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(role.roleName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 2.dp)) {
                                                PermBadge("Editar Layout", role.hasEditGrid)
                                                PermBadge("Sacar Vendas", role.hasWithdrawStore)
                                                PermBadge("Convites", role.hasInviteGuests)
                                                PermBadge("Muda Tema", role.hasChangeTheme)
                                            }
                                        }

                                        // Simulated Interactive toggle switches
                                        Box(
                                            modifier = Modifier
                                                .background(BlueprintOrange.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .clickable {
                                                    // Toggle first property for demonstration
                                                    val updated = role.copy(hasEditGrid = !role.hasEditGrid)
                                                    rolesPermissions[index] = updated
                                                    Toast.makeText(context, "Permissões de ${role.roleName} salvas no cache de sessão!", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(horizontal = 6.dp, vertical = 5.dp)
                                        ) {
                                            Text("CONFIGURAR", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                                        }
                                    }
                                }
                            }
                        }

                        "SQL_DB" -> {
                            Text("COMPILAÇÃO DO PRISMA SCHEMA & SQL MONITOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            Text("Drives relacionais baseados na modelagem Prisma Schema estruturada do banco local. Mantenha controle de transações síncronas de gravação e anti-dupagem.", fontSize = 8.5.sp, color = BlueprintTextSecondary)

                            // Show live terminal simulator
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                                border = BorderStroke_workaround(BlueprintCyan.copy(alpha = 0.3f)),
                                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp).fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("CMD SQL TERMINAL LOGS (ACADEMY_SANDBOX_DB)", fontSize = 7.5.sp, color = BlueprintTeal, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                                    
                                    Column(
                                        modifier = Modifier.height(130.dp).verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        sqlLogLines.forEach { line ->
                                            Text(line, fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                                        }
                                    }
                                }
                            }

                            // Interactive database persistence save call
                            Button(
                                onClick = {
                                    sqlLogLines.add("[SQL-SYNC] BEGIN TRANSACTION;")
                                    sqlLogLines.add("[SQL-SYNC] DELETE FROM dojo_config WHERE land_id='Alliance-HQ';")
                                    sqlLogLines.add("[SQL-SYNC] INSERT INTO dojo_config VALUES ('Alliance-HQ', '${selectedTerrainState.id}', ${landSizeMeters.toInt()});")
                                    placedItems.forEach { item ->
                                        sqlLogLines.add("[SQL-SYNC] INSERT INTO dojo_items VALUES ('${item.id}', 'Alliance-HQ', '${item.element.id}', ${item.x}, ${item.y}, ${item.rotation});")
                                    }
                                    sqlLogLines.add("[SQL-SYNC] COMMIT; -- Gravação finalizada! ${placedItems.size + 1} queries afetadas nos lotes.")
                                    Toast.makeText(context, "Estrutura do tatame persistida com sucesso no SQLite do celular!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.fillMaxWidth().testTag("save_sandbox_sqlite_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SALVAR NO BANCO (ROOM SQLite)", fontSize = 8.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        "PHASER" -> {
                            Text("PHASER 3 CONCENTRIC BRIDGE CONFIG EXPORTER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                            Text("Mapeamento espacial perfeitamente alinhado com Phaser 3. Clique em copiar para transferir o script JSON gerado, integrando com o motor gráfico Web síncrono.", fontSize = 8.5.sp, color = BlueprintTextSecondary)

                            // Javascript Code Generator dynamic calculation
                            val itemsPayloadJs = placedItems.joinToString(",\n    ") { 
                                "{ id: \"${it.id}\", elementId: \"${it.element.id}\", x: ${it.x}, y: ${it.y}, depth: ${(it.x + it.y) * 2}, rotation: ${it.rotation}, key: \"${it.element.category.lowercase()}_sprite\" }"
                            }

                            val phaserBridgeCode = """
// JiuVerse Custom Dojo UGC Phaser 3 Layout Generator
const UGC_DOJO_CONFIG = {
  terrainPreset: "${selectedTerrainState.name}",
  terrainType: "${selectedTerrainState.terrainType}",
  gridSize: 10,
  tileSize: { width: 64, height: 32 },
  placedSlots: [
    $itemsPayloadJs
  ]
};

// Coordinate mapping in the Phaser Game engine:
class CustomizedDojoScene extends Phaser.Scene {
    preload() {
        this.load.image('room_sprite', 'assets/iso_room.png');
        this.load.image('tatame_sprite', 'assets/iso_mats.png');
        this.load.image('furniture_sprite', 'assets/iso_bench.png');
        this.load.image('trophy_sprite', 'assets/iso_trophy.png');
        this.load.image('reception_sprite', 'assets/iso_reception.png');
        this.load.image('store_sprite', 'assets/iso_açaiseller.png');
    }

    create() {
        const wHalf = UGC_DOJO_CONFIG.tileSize.width / 2;
        const hHalf = UGC_DOJO_CONFIG.tileSize.height / 2;
        const originX = this.sys.game.config.width / 2;
        const originY = 80;

        UGC_DOJO_CONFIG.placedSlots.forEach(item => {
            const pxX = (item.x - item.y) * wHalf + originX;
            const pxY = (item.x + item.y) * hHalf + originY;

            let sprite = this.add.sprite(pxX, pxY, item.key);
            sprite.setDepth(item.depth + 1);
            sprite.setAngle(item.rotation);
            sprite.setInteractive();

            sprite.on('pointerdown', () => {
                console.log("Inspecionando item UGC " + item.id + " em " + item.x + "," + item.y);
            });
        });
    }
}
""".trimIndent()

                            Spacer(modifier = Modifier.height(4.dp))

                            CodeBlock(
                                code = phaserBridgeCode,
                                title = "UgcDojoPhaserBridge.js"
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 4: Dojo Simulator Stats (Prestige, maintenance, active shops)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("MÉTRICAS ATIVAS DE DOJO & RENDIMENTO FINANCEIRO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Prestígio da Equipe", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Text("$activePrestige Pts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Manutenção Diária", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Text("$dailyMaintenance JC/dia", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BlueprintRed, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Módulos Ativos", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Text("${placedItems.size} Peças", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Salas: $totalPlacedRooms/4 | • Tatames: $totalPlacedMats mat-tiles | • Decorações: $totalPlacedTrophies displays | • Recepções: $totalPlacedReceptions turnstiles | • Lojas: $totalPlacedStores/3",
                        fontSize = 8.5.sp,
                        color = BlueprintTextPrimary,
                        modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// --- Auxiliary Jetpack Compose Styling helpers ---

@Composable
fun LegendBadge(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(text = label, fontSize = 8.sp, color = BlueprintTextSecondary)
    }
}

@Composable
fun PermBadge(label: String, allowed: Boolean) {
    Box(
        modifier = Modifier
            .background(if (allowed) BlueprintTeal.copy(alpha = 0.15f) else BlueprintRed.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
            .border(0.5.dp, if (allowed) BlueprintTeal else BlueprintRed, RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = "${if (allowed) "✓" else "✗"} $label",
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            color = if (allowed) BlueprintTeal else BlueprintRed
        )
    }
}

@Composable
fun RowWithCell(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 8.sp, color = BlueprintTextSecondary)
        Text(value, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun SubTabNavigatorCapsule(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(if (isSelected) BlueprintCyan.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
            .border(
                width = 0.5.dp,
                color = if (isSelected) BlueprintCyan else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 6.dp)
            .wrapContentWidth()
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) BlueprintCyan else BlueprintTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// Workaround for Compose BorderStroke parameter layout bugs
private fun BorderStroke_workaround(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(0.5.dp, color)
}
