package com.example.architecture.views

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 1. Core Data Models for Property & Economy Sandbox
data class PropertyItem(
    val id: String,
    val type: String, // "Apartamento", "Casa", "Cobertura", "Mansão", "Academia Residencial"
    val title: String,
    val description: String,
    val basePrice: Int,
    val monthlyRent: Int,
    val emoji: String,
    val totalSlots: Int,
    var ownershipState: String, // "À Venda", "Disponível Aluguel", "Comprada", "Alugada"
    var lastPriceChangedTrend: Double = 1.0 // market trend multiplier
)

data class DecorativeElement(
    val id: String,
    val itemName: String,
    val category: String, // "Sofá", "TV", "Troféu", "Quadro", "Computador", "Animal"
    val emoji: String,
    val priceCredits: Int,
    val customBuff: String,
    val requiredSpace: Int = 1
)

data class PlacedItem(
    val id: String,
    val element: DecorativeElement,
    var xPosition: Int, // grid coordinates (0 to 5)
    var yPosition: Int  // grid coordinates (0 to 5)
)

data class VisitorLog(
    val visitorName: String,
    val action: String,
    val timeAgo: String,
    val profileEmoji: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HousingTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- Wallet & Assets Simulation State (Economy Engine) ---
    var walletCredits by remember { mutableStateOf(350000) }
    var safeBalanceCredits by remember { mutableStateOf(50000) }
    var marketEconomyTrend by remember { mutableStateOf("Estável (+2% a.m.)") }
    var economyTaxRate by remember { mutableStateOf(1.5f) } // real estate tax %
    var feedbackMessageLog by remember { mutableStateOf("Hub do Sistema de Moradias & Sandbox Conectado.") }

    // --- State: Database of Properties ---
    val propertiesList = remember {
        mutableStateListOf(
            PropertyItem("p_1", "Apartamento", "Studio de Angra dos Reis", "Apartamento compacto, ideal para recém-chegados organizarem quimonos de reserva.", 45000, 450, "🏢", 12, "À Venda"),
            PropertyItem("p_2", "Casa", "Dojo Cabana da Restinga", "Construção de madeira de lei com arejamento ideal para manter o tatame livre de fungos.", 95000, 900, "🏡", 18, "À Venda"),
            PropertyItem("p_3", "Cobertura", "Duplex na Orla Celestial", "Cobertura de luxo com vista para o polo náutico, piscina de açaí hidromassageadora síncrona.", 210000, 1800, "🏙️", 24, "À Venda"),
            PropertyItem("p_4", "Mansão", "Solar Lendário dos Campeões", "Arquitetura monumental para clãs. Portais com sensor biométrico e vestiário olímpico.", 480000, 3900, "🏰", 36, "À Venda"),
            PropertyItem("p_5", "Academia Residencial", "Centro de Treinamento Privado", "Equipado com sacos de pancadas de alta densidade, espelhos multidimensionais e tatame oficial.", 160000, 1400, "🥋", 30, "À Venda"),
            PropertyItem("p_6", "Apartamento", "Suíte Flat Arena VIP", "Compacta, luxuosa, localizada acima do estádio central de eSports do JiuVerse.", 75000, 700, "🏨", 16, "Comprada", 1.15) // Owned initially as default demonstration
        )
    }

    // --- State: Decor Store Inventory ---
    val decorationCatalog = remember {
        listOf(
            DecorativeElement("dec_1", "Sofá de Veludo Nobre", "Sofá", "🛋️", 1200, "Regeneração de fôlego +10%", 2),
            DecorativeElement("dec_2", "TV Gamer Curvada 86\"", "TV", "📺", 3400, "Dobra canais de estudos táticos", 3),
            DecorativeElement("dec_3", "Cálice Absoluto Ouro", "Troféu", "🏆", 8000, "Prestígio Geral +45 pontos", 1),
            DecorativeElement("dec_4", "Monumento Cinturão Cósmico", "Troféu", "🥇", 15000, "Respeito do Dojo Alavanca +100", 1),
            DecorativeElement("dec_5", "Quadro Grande Grande Mestre Helio", "Quadro", "🖼️", 3000, "Aura de Defesa +8% em casa", 2),
            DecorativeElement("dec_6", "Quadro Estilo Graffiti JiuVerse", "Quadro", "🎨", 800, "Estética Jovem e Atitude", 1),
            DecorativeElement("dec_7", "Setup Quantum Core", "Computador", "💻", 6200, "Simulação eSports +12% velocidade", 2),
            DecorativeElement("dec_8", "Bulldog Mascote Protetor", "Animal", "🐶", 5000, "Bloqueia penetrações suspeitas", 1),
            DecorativeElement("dec_9", "Panda da Sorte Meditando", "Animal", "🐼", 9000, "Meditação gera passivamente créditos", 1),
            DecorativeElement("dec_10", "Sofá Puff Confort Tatame", "Sofá", "🛋️", 500, "Relaxamento tático rápido", 1)
        )
    }

    // --- State: Currently active customizable house ---
    var selectedPropertyForDecorationIndex by remember { mutableStateOf(5) } // matches p_6, which is owned
    val selectedProperty = propertiesList.getOrNull(selectedPropertyForDecorationIndex) ?: propertiesList[0]

    // Wall Painting Colors
    val colorsLibrary = listOf(
        Pair("Slate Blueprint", Color(0xFF0F172A)),
        Pair("Royal Tatame Wood", Color(0xFF1E1E1E)),
        Pair("Dojo Imperial", Color(0xFF3B0712)), // Crimson/Burgundy
        Pair("Poison Ivy Moss", Color(0xFF064E3B)), // Forest Green
        Pair("Ocean Pacific", Color(0xFF0C4A6E)), // Blue
        Pair("Fita Amarela Glow", Color(0xFF78350F))  // Amber
    )
    var selectedWallColorIndex by remember { mutableStateOf(0) }

    // Placed items in the selected property
    val placedItemsMap = remember {
        mutableStateMapOf<String, PlacedItem>().apply {
            // Seed a physical layout with default ornaments
            put("p_6_item1", PlacedItem("p_6_item1", decorationCatalog[0], 2, 1)) // Velvet Sofa at (2,1)
            put("p_6_item2", PlacedItem("p_6_item2", decorationCatalog[2], 0, 0)) // Golden Trophy at (0,0)
            put("p_6_item3", PlacedItem("p_6_item3", decorationCatalog[8], 4, 3)) // Lucky Panda at (4,3)
        }
    }

    // --- State: Access Management Policy ---
    var propertyAccessLevel by remember { mutableStateOf("Apenas Amigos") } // "Público", "Apenas Amigos", "Privado"
    val visitorLogList = remember {
        mutableStateListOf(
            VisitorLog("Gabriel_Guarda", "Visitou o Dojo e deixou curtida de prestígio.", "2 min atrás", "🥋"),
            VisitorLog("Kru_Somchai", "Entrou e depositou 800 créditos de aluguel pendente.", "14 min atrás", "🥊"),
            VisitorLog("Dona_Estela", "Olhou os seus troféus e deitou no sofá.", "1 hora atrás", "👵")
        )
    }

    var inviteInputName by remember { mutableStateOf("") }
    var createdInvitationsHistory = remember { mutableStateListOf<String>() }

    // --- State: Interactive Deposit/Withdraw safe simulation ---
    var digitsCodeSafeState by remember { mutableStateOf("") }
    var safeSuccessAttempt by remember { mutableStateOf<Boolean?>(null) } // true=unlocked, false=wrong code, null=idle
    var isSafeOpen by remember { mutableStateOf(false) }

    // Automatically trigger real estate market simulation cycle every 10 seconds to update trend prices
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            // Simula oscilações econômicas da imobiliária
            val isBullish = kotlin.random.Random.nextInt(0, 11) > 4
            val changePercent = kotlin.random.Random.nextInt(1, 7) / 100.0
            if (isBullish) {
                marketEconomyTrend = "Em Alta (+${String.format("%.1f", changePercent * 100)}% ao ciclo)"
                economyTaxRate = kotlin.random.Random.nextDouble(1.2, 1.9).toFloat()
                // increase prices slightly
                propertiesList.forEachIndexed { idx, item ->
                    if (item.ownershipState == "À Venda" || item.ownershipState == "Disponível Aluguel") {
                        propertiesList[idx] = item.copy(lastPriceChangedTrend = item.lastPriceChangedTrend + (changePercent * 0.5))
                    }
                }
            } else {
                marketEconomyTrend = "Correção Inflacionária (-${String.format("%.1f", changePercent * 100)}% ao ciclo)"
                economyTaxRate = kotlin.random.Random.nextDouble(1.0, 1.4).toFloat()
                propertiesList.forEachIndexed { idx, item ->
                    if (item.ownershipState == "À Venda" || item.ownershipState == "Disponível Aluguel") {
                        propertiesList[idx] = item.copy(lastPriceChangedTrend = (item.lastPriceChangedTrend - (changePercent * 0.3)).coerceAtLeast(0.8))
                    }
                }
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
            title = "JiuVerse Real Estate Sandbox & Moradias Customizáveis",
            subtitle = "Motor Econômico Imobiliário e Editor de Interiores Avançado. Adquira moradias residenciais, alugue CTs para faturar passivamente ou monte seu dojo ideal."
        )

        // Sandbox Game Master Stats Dashboard Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C1424), RoundedCornerShape(8.dp))
                .border(1.dp, BlueprintCyan, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "STATUS DO SISTEMA DE URBANIZAÇÃO E FINANÇAS SÍNCRONAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "As moradias do JiuVerse proveem bônus de descanso, buff de atributos em sparrins domésticos e armários de inventário estendidos que aumentam conforme a categoria do imóvel.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Seus Fundos Liq.", fontSize = 9.sp, color = BlueprintTextSecondary)
                Text(
                    text = "$walletCredits Credits",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintOrange,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // =========================================================================
        // SECTION 1: PROPERTIES EXCHAGE & REAL ESTATE APPRAISEMENT (COMPRA, ALUGUEL, VENDA)
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "I. PAINEL DE TRANSAÇÕES E COTAÇÕES DO MERCADO IMOBILIÁRIO",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Box(
                modifier = Modifier
                    .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .border(0.5.dp, BlueprintTeal, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("Cenário Econômico: $marketEconomyTrend", fontSize = 8.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Selecione um lote / infraestrutura na listagem abaixo para Comprar, Alugar, Vender de volta ao mercado pela cotação do ciclo ou definir como endereço ativo de customização.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // List of Properties
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    propertiesList.forEachIndexed { index, prop ->
                        val finalBuyPrice = (prop.basePrice * prop.lastPriceChangedTrend).toInt()
                        val finalRentPrice = (prop.monthlyRent * prop.lastPriceChangedTrend).toInt()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedPropertyForDecorationIndex == index) Color(0xFF1B2A4A) else Color.Black)
                                .border(1.dp, if (selectedPropertyForDecorationIndex == index) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1.2f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(BlueprintHeader, CircleShape)
                                        .border(1.dp, BlueprintCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(prop.emoji, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(prop.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color.Black)
                                                .border(0.5.dp, BlueprintTeal, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(prop.type, fontSize = 7.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(prop.description, fontSize = 9.sp, color = BlueprintTextSecondary)
                                    Text(
                                        text = "Max Slots decoração: ${prop.totalSlots} | Taxas do Templo: ${economyTaxRate}% ao mês",
                                        fontSize = 8.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            // Dynamic state action buttons representing the actual economic operation
                            Column(
                                modifier = Modifier.weight(0.8f),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center
                            ) {
                                when (prop.ownershipState) {
                                    "À Venda" -> {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Button(
                                                onClick = {
                                                    if (walletCredits >= finalBuyPrice) {
                                                        walletCredits -= finalBuyPrice
                                                        prop.ownershipState = "Comprada"
                                                        propertiesList[index] = prop.copy() // trigger recompose
                                                        selectedPropertyForDecorationIndex = index
                                                        feedbackMessageLog = "Felicitações! Você comprou o imóvel '${prop.title}' por $finalBuyPrice créditos!"
                                                    } else {
                                                        feedbackMessageLog = "Erro Imobiliária: Créditos insuficientes na carteira de Angra para comprar este lote de ${finalBuyPrice} C."
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintOrange, contentColor = Color.Black),
                                                modifier = Modifier.height(30.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp)
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("COMPRAR", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    Text("$finalBuyPrice C", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    if (walletCredits >= finalRentPrice * 2) {
                                                        walletCredits -= finalRentPrice * 2 // deposit fee
                                                        prop.ownershipState = "Alugada"
                                                        propertiesList[index] = prop.copy()
                                                        selectedPropertyForDecorationIndex = index
                                                        feedbackMessageLog = "Aluguel garantido para '${prop.title}'! Pago calção inicial de 2 meses (${finalRentPrice * 2} C)."
                                                    } else {
                                                        feedbackMessageLog = "Erro Imobiliária: Créditos insuficientes para calção inicial de aluguel (${finalRentPrice * 2} C)."
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan, contentColor = Color.Black),
                                                modifier = Modifier.height(30.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp)
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("ALUGAR", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    Text("$finalRentPrice C/mês", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                    "Comprada", "Alugada" -> {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .border(0.5.dp, BlueprintTeal, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(if (prop.ownershipState == "Comprada") "ADQUIRIDO" else "LOCADO", fontSize = 8.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                            }

                                            // Sell Property Button
                                            Button(
                                                onClick = {
                                                    val resellValue = (finalBuyPrice * 0.85).toInt() // 15% depreciation property fee
                                                    walletCredits += resellValue
                                                    prop.ownershipState = "À Venda"
                                                    propertiesList[index] = prop.copy()
                                                    feedbackMessageLog = "Venda confirmada! O imóvel '${prop.title}' foi devolvido ao mercado de Angra por $resellValue créditos."
                                                    // adjust fallback index to index 5
                                                    selectedPropertyForDecorationIndex = 5
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B), contentColor = Color.White),
                                                modifier = Modifier.height(26.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp)
                                            ) {
                                                Text("VENDER (85%)", fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Decorative active indicator check
                                            IconButton(
                                                onClick = {
                                                    selectedPropertyForDecorationIndex = index
                                                    feedbackMessageLog = "Imóvel de trabalho ativo alternado para: ${prop.title}"
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (selectedPropertyForDecorationIndex == index) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                    contentDescription = "Selecionar para decorar",
                                                    tint = if (selectedPropertyForDecorationIndex == index) BlueprintCyan else Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // SECTION 2: INTERACTIVE DECOR PORTAL AND SPATIAL LAYOUT GRID
        // =========================================================================
        Text(
            text = "II. DESENHO DE LAYOUT E DECORAÇÃO SÍNCRONA (GRID 6x6)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🛠️ DOJO DESIGN EDITOR: ${selectedProperty.title.uppercase()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintOrange
                        )
                        Text(
                            text = "Clique em um slot do tatame abaixo para re-posicionar itens ou remova-os tocando neles.",
                            fontSize = 8.sp,
                            color = BlueprintTextSecondary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Badge(containerColor = BlueprintCyan.copy(alpha = 0.2f), contentColor = BlueprintCyan) {
                            Text("Wall: ${colorsLibrary[selectedWallColorIndex].first}", fontSize = 8.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // LEFT COLUMN: GRID VISUAL REPRESENTATION (60% width)
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // The 6x6 grid mapping
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colorsLibrary[selectedWallColorIndex].second)
                                .border(1.5.dp, BlueprintCyan, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (row in 0..5) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        for (col in 0..5) {
                                            // Check if there is an item placed in (col, row)
                                            val itemIdAtSlot = placedItemsMap.values.find {
                                                it.xPosition == col && it.yPosition == row
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (itemIdAtSlot != null) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.05f))
                                                    .border(
                                                        0.5.dp,
                                                        if (itemIdAtSlot != null) BlueprintOrange else BlueprintGridLine.copy(alpha = 0.4f),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .clickable {
                                                        if (itemIdAtSlot != null) {
                                                            // Remove or manipulate placed item
                                                            placedItemsMap.remove(itemIdAtSlot.id)
                                                            feedbackMessageLog = "Item '${itemIdAtSlot.element.itemName}' removido do tatame."
                                                        } else {
                                                            feedbackMessageLog = "Para adicionar um item novo, escolha um ornamento no catálogo ao lado e posicione!"
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (itemIdAtSlot != null) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(itemIdAtSlot.element.emoji, fontSize = 16.sp)
                                                        Text(
                                                            text = itemIdAtSlot.element.itemName.take(4) + "..",
                                                            fontSize = 6.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                } else {
                                                    Text(
                                                        text = "[$col,$row]",
                                                        fontSize = 5.sp,
                                                        color = Color.LightGray.copy(alpha = 0.4f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Colors palette controller for wall painting customization
                        Column {
                            Text("Pintar Paredes Externas e Forro do Dojo:", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                colorsLibrary.forEachIndexed { i, scheme ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(scheme.second)
                                            .border(1.dp, if (selectedWallColorIndex == i) Color.White else Color.Transparent, RoundedCornerShape(4.dp))
                                            .clickable {
                                                selectedWallColorIndex = i
                                                feedbackMessageLog = "Pintura atualizada no server síncrono para ${scheme.first}."
                                            }
                                    )
                                }
                            }
                        }
                    }

                    // RIGHT COLUMN: DECOR STORE & INVENTORY LIST (40% width)
                    Card(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        border = BorderStroke(1.dp, BlueprintGridLine)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = "🛒 LOJA DE DECORAÇÃO",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                decorationCatalog.forEach { spec ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF0C1424))
                                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .clickable {
                                                // Find an empty random space to place this decorative item on the grid
                                                var freeCellFound = false
                                                for (r in 0..5) {
                                                    for (c in 0..5) {
                                                        val taken = placedItemsMap.values.any { it.xPosition == c && it.yPosition == r }
                                                        if (!taken) {
                                                            if (walletCredits >= spec.priceCredits) {
                                                                walletCredits -= spec.priceCredits
                                                                val newId = "p_item_${System.currentTimeMillis()}"
                                                                placedItemsMap.put(newId, PlacedItem(newId, spec, c, r))
                                                                feedbackMessageLog = "Adquiriu '${spec.itemName}' e instalou na coordenada ($c, $r)!"
                                                            } else {
                                                                feedbackMessageLog = "Créditos insuficientes de Angra para comprar '${spec.itemName}' (${spec.priceCredits} C)."
                                                            }
                                                            freeCellFound = true
                                                            break
                                                        }
                                                    }
                                                    if (freeCellFound) break
                                                }
                                                if (!freeCellFound) {
                                                    feedbackMessageLog = "Tatame Completo! Remova algum ornamento antigo para liberar espaço."
                                                }
                                            }
                                            .padding(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(spec.emoji, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column {
                                                Text(spec.itemName, fontSize = 7.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                Text(spec.customBuff, fontSize = 6.sp, color = BlueprintTeal)
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${spec.priceCredits} C", fontSize = 7.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                                            Text("Comprar", fontSize = 5.5.sp, color = BlueprintTextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // SECTION 3: ACCESS CONTROL, SECURE CRYPTO SAFE & VISITOR INVITES
        // =========================================================================
        Text(
            text = "III. SEGURANÇA IMOBILIÁRIA (ACESSO, COFRES E LISTA DE VISITANTES)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // LEFT COLUMN: ACCESS PRIVILEGES & INVITATION TERMINAL
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(
                            text = "🔐 GERENCIAMENTO DE ACESSO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintCyan
                        )
                        Text(
                            text = "Regule quem pode entrar na sua propriedade privada.",
                            fontSize = 7.5.sp,
                            color = BlueprintTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Switch access level radio simulation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Público", "Apenas Amigos", "Privado").forEach { level ->
                                val active = propertyAccessLevel == level
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (active) BlueprintCyan.copy(alpha = 0.25f) else Color.Black)
                                        .border(1.dp, if (active) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                                        .clickable {
                                            propertyAccessLevel = level
                                            feedbackMessageLog = "Nível de acesso alterado pelo proprietário para: $level."
                                        }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(level, fontSize = 7.5.sp, color = if (active) BlueprintCyan else Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Invites code custom dispatching tool
                        Text("Gerador de Chaves de Convite:", fontSize = 8.sp, color = Color.White)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = inviteInputName,
                                onValueChange = { inviteInputName = it },
                                placeholder = { Text("Nome do Visitante", fontSize = 8.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Black,
                                    unfocusedContainerColor = Color.Black
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(34.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.sp)
                            )

                            Button(
                                onClick = {
                                    if (inviteInputName.isNotBlank()) {
                                        val generatedKey = "INVITE-${inviteInputName.trim().uppercase()}-${(1000..9999).random()}"
                                        createdInvitationsHistory.add(generatedKey)
                                        feedbackMessageLog = "Chave gerada com sucesso para ${inviteInputName}!"
                                        inviteInputName = ""
                                    }
                                },
                                enabled = inviteInputName.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintOrange, contentColor = Color.Black),
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(34.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("GERAR PASS", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Code History Log list
                        if (createdInvitationsHistory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                createdInvitationsHistory.forEach { code ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black)
                                            .border(0.5.dp, BlueprintGridLine)
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(code, fontSize = 7.5.sp, color = BlueprintOrange, fontFamily = FontFamily.Monospace)
                                        Text("Ativo", fontSize = 6.sp, color = BlueprintTeal)
                                    }
                                }
                            }
                        }
                    }

                    // Security telemetry warning
                    Text(
                        text = "Aviso: A falsificação de tokens biométricos de acesso é banida automaticamente via Warden Anti-Cheat.",
                        fontSize = 7.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // RIGHT COLUMN: COFRE-FORTE COGNITIVO COM SIMULAÇÃO PIN KEYPAD (Cofres)
            Card(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = BlueprintHeader),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(
                            text = "🏦 COFRE DE ITENS E CRÉDITOS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintTeal
                        )
                        Text(
                            text = "Deposite ou decole itens ultra-raros no cofre-forte blindado para escapar de saques.",
                            fontSize = 7.sp,
                            color = BlueprintTextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Vault state display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSafeOpen) Color(0xFF064E3B) else Color(0xFF450A0A))
                                .border(1.dp, if (isSafeOpen) BlueprintTeal else Color.Red, RoundedCornerShape(6.dp))
                                .padding(6.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isSafeOpen) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Green, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cofre Desbloqueado", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                                    }
                                    Text("Depósito Seguro: $safeBalanceCredits Credits", fontSize = 8.sp, color = Color.White)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cofre Blindado Trancado", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Text("Senha Digitada: $digitsCodeSafeState (Dica: '1337')", fontSize = 8.sp, color = BlueprintTextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Grid for pin code entry digits (1-9, 0, clear, open)
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                listOf("1", "3", "7").forEach { digit ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black)
                                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .clickable {
                                                if (digitsCodeSafeState.length < 4) {
                                                    digitsCodeSafeState += digit
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(digit, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Button(
                                    onClick = { digitsCodeSafeState = "" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(24.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("CLEAR", fontSize = 7.sp)
                                }

                                Button(
                                    onClick = {
                                        if (digitsCodeSafeState == "1337") {
                                            isSafeOpen = true
                                            safeSuccessAttempt = true
                                            feedbackMessageLog = "Cofre Aberto! Acesso concedido ao inventário de itens blindados."
                                        } else {
                                            isSafeOpen = false
                                            safeSuccessAttempt = false
                                            feedbackMessageLog = "Alerta: Senha incorreta no cofre. Tentativa suspeita registrada!"
                                            digitsCodeSafeState = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal, contentColor = Color.Black),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(24.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("OPEN", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Quick operation buttons when safe is unlocked
                    if (isSafeOpen) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = {
                                    if (walletCredits >= 5000) {
                                        walletCredits -= 5000
                                        safeBalanceCredits += 5000
                                        feedbackMessageLog = "Guardou 5000 créditos com segurança no cofre principal."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan, contentColor = Color.Black),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(22.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("DEPOSIT 5k", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (safeBalanceCredits >= 5000) {
                                        safeBalanceCredits -= 5000
                                        walletCredits += 5000
                                        feedbackMessageLog = "Retirou 5000 créditos do cofre de volta para a carteira de Angra."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintOrange, contentColor = Color.Black),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(22.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("WITHDRAW 5k", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Safe locked lock button description
                        Text("Trancado por biometria e cifragem quântica.", fontSize = 7.sp, color = BlueprintTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visitor Log Summary Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "📋 HISTÓRICO DE VISITANTES & SINCRONIZAÇÃO SENSORIAL EM TEMPO REAL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    visitorLogList.forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(BlueprintHeader, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(log.profileEmoji, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(log.visitorName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(log.action, fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                }
                            }
                            Text(log.timeAgo, fontSize = 7.sp, color = BlueprintTeal, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Action Telemetry Logger Area below Sandbox
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(6.dp))
                .border(0.5.dp, BlueprintTeal, RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LOG REAL ESTATE SYSTEM: $feedbackMessageLog",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = BlueprintTeal
            )
        }
    }
}
