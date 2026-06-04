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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
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

data class MarketItemSim(
    val id: String,
    val name: String,
    val rarity: String, // "RARO", "LENDÁRIO", "COMUM"
    val rarityColor: Color,
    val seller: String,
    var price: Int, // in JiCoins / JiuGems
    val currency: String, // "🪙" or "💎"
    val isAuction: Boolean = false,
    var highestBid: Int = 0,
    var highestBidder: String = "",
    var auctionTimeLeftSeconds: Int = 3600,
    var status: String = "ACTIVE" // "ACTIVE", "SOLD", "EXPIRED"
)

data class PriceHistorySim(
    val period: String,
    val avgPriceJiCoin: Int,
    val tradingVolume: Int
)

data class AuditLogSim(
    val timestamp: String,
    val action: String,
    val details: String,
    val txHash: String,
    val status: String // "SECURE", "MUTEX_LOCKED", "FRAUD_BLOCKED"
)

@Composable
fun MarketplaceTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // 1. Initial State for Active Store Listings (P2P Sales)
    val listings = remember {
        mutableStateListOf(
            MarketItemSim("1", "Kimono de Ouro Gracie", "LENDÁRIO", BlueprintOrange, "MasterHelio", 12500, "🪙", isAuction = false),
            MarketItemSim("2", "Faixa Coral de Algodão Egípcio", "RARO", BlueprintCyan, "FaixaPretaRio", 3400, "🪙", isAuction = false),
            MarketItemSim("3", "Protetor Bucal de Safira", "LENDÁRIO", BlueprintOrange, "RicksonFan", 800, "💎", isAuction = true, highestBid = 850, highestBidder = "RenzoG_Jr", auctionTimeLeftSeconds = 1200),
            MarketItemSim("4", "Rashguard de Fibra de Carbono", "RARO", BlueprintCyan, "WrestlerPro", 1200, "🪙", isAuction = true, highestBid = 1250, highestBidder = "GuillotineKing", auctionTimeLeftSeconds = 2400),
            MarketItemSim("5", "Kimono Antigo Relíquia 1952", "LENDÁRIO", BlueprintOrange, "CarlosG_Legacy", 45000, "🪙", isAuction = false),
            MarketItemSim("6", "Garrafa de Água Hidratante Sagrada", "COMUM", BlueprintTeal, "NewbieGuard", 150, "🪙", isAuction = false)
        )
    }

    // 2. Price history data list
    val priceHistory = remember {
        listOf(
            PriceHistorySim("Semana 1", 8200, 48),
            PriceHistorySim("Semana 2", 9100, 62),
            PriceHistorySim("Semana 3", 11500, 85),
            PriceHistorySim("Semana 4 (Atual)", 12500, 110)
        )
    }

    // 3. Security logs simulating the engine's audits
    val auditLogs = remember {
        mutableStateListOf(
            AuditLogSim("15:44:02", "MUTEX_LOCK_GENERATE", "Verificação preventiva anti-duplicação iniciada para Kimono de Ouro Gracie.", "0x7a30cfde9f41b", "SECURE"),
            AuditLogSim("15:44:11", "RATE_LIMIT_CHECK", "Lutador 'SpeedyGuardPass' fez 1 requisição. Limite: 5 req/min. Aceito.", "0x39ba4e321fc4c", "SECURE"),
            AuditLogSim("15:44:24", "COMPRA_EFETIVADA_P2P", "Venda de Rashguard id #392 liquidada com sucesso.", "0x9ef239dfc823a", "SECURE")
        )
    }

    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "LENDÁRIO", "RARO", "AUCTION"
    var walletJiCoins by remember { mutableStateOf(25000) }
    var walletJiuGems by remember { mutableStateOf(1500) }

    // Interactivity fields for a custom Sell post
    var newItemName by remember { mutableStateOf("Faixa Preta Autografada") }
    var newItemRarity by remember { mutableStateOf("RARO") }
    var newItemPrice by remember { mutableStateOf(4500) }
    var newItemIsAuction by remember { mutableStateOf(false) }

    // Fraud trigger simulation state
    var showFraudNotice by remember { mutableStateOf(false) }
    var fraudTimestamp by remember { mutableStateOf("") }

    // Logic for posting custom sale
    val handlePublishItem = {
        val newId = (listings.size + 1).toString()
        val rarityColor = if (newItemRarity == "LENDÁRIO") BlueprintOrange else BlueprintCyan
        val marketObj = MarketItemSim(
            id = newId,
            name = newItemName,
            rarity = newItemRarity,
            rarityColor = rarityColor,
            seller = "SuaConta_You",
            price = newItemPrice,
            currency = if (newItemIsAuction) "💎" else "🪙",
            isAuction = newItemIsAuction,
            highestBid = if (newItemIsAuction) newItemPrice else 0,
            highestBidder = if (newItemIsAuction) "SuaConta_You" else "",
            auctionTimeLeftSeconds = 3600
        )
        listings.add(0, marketObj)

        auditLogs.add(0, AuditLogSim(
            timestamp = "15:46:12",
            action = "ITEM_MINT_POST_P2P",
            details = "Item '${newItemName}' publicado no Marketplace por SuaConta_You. Verificação de ID original correspondente segura.",
            txHash = "0x" + (349282..990238).random().toString(16),
            status = "SECURE"
        ))
    }

    // Purchase execution block
    val handleBuyItem = { item: MarketItemSim ->
        if (item.currency == "🪙" && walletJiCoins >= item.price) {
            walletJiCoins -= item.price
            item.status = "SOLD"
            
            // Add security audit trail
            auditLogs.add(0, AuditLogSim(
                timestamp = "15:46:42",
                action = "MUTEX_LOCK_ACQUIRED",
                details = "Bloqueio distribuído Redis gerado para item ID #${item.id}. Transação serializada com sucesso.",
                txHash = "0x" + (349282..990238).random().toString(16),
                status = "SECURE"
            ))

            auditLogs.add(0, AuditLogSim(
                timestamp = "15:46:43",
                action = "LIQUIDAÇÃO_AUDITADA",
                details = "Dono anterior ${item.seller} recebeu o montante líquido de ${item.price} JC (Imposto de 12% recolhido).",
                txHash = "0x" + (349282..990238).random().toString(16),
                status = "SECURE"
            ))
        } else if (item.currency == "💎" && walletJiuGems >= item.price) {
            walletJiuGems -= item.price
            item.status = "SOLD"
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
            title = "Marketplace Descentralizado (P2P)",
            subtitle = "Arquitetura de Compra, Venda, Leilões e Proteções Ativas Anti-Fraude e Duplicações de Itens"
        )

        // Wallet Balance HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(0.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SEU SALDO ATUAL:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format("%,d", walletJiCoins)} JC (Gratuito)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintTeal,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💎", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format("%,d", walletJiuGems)} JG (Premium)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintOrange,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main grid: Form for selling + Price progression graph
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Posting panel
            Card(
                modifier = Modifier.weight(1.1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PUBLICAR ANÚNCIO (VENDER ITEM)", fontSize = 10.sp, fontWeight = FontWeight.Black, color = BlueprintCyan)
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    Text("Nome do Item", fontSize = 9.sp, color = BlueprintTextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text(newItemName, fontSize = 10.sp, color = BlueprintTextPrimary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rarity checkboxes
                    Text("Raridade do Item", fontSize = 9.sp, color = BlueprintTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("RARO", "LENDÁRIO").forEach { rarity ->
                            val isSelected = newItemRarity == rarity
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) BlueprintCyan.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable { newItemRarity = rarity }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(rarity, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) BlueprintCyan else BlueprintTextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode: Instant Sale vs Auction
                    Text("Tipo de Negociação", fontSize = 9.sp, color = BlueprintTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(false, true).forEach { isAuction ->
                            val isSelected = newItemIsAuction == isAuction
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) BlueprintTeal.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, if (isSelected) BlueprintTeal else BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable { newItemIsAuction = isAuction }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isAuction) "LEILÃO (BIDS/💎)" else "VENDA DIRETA (🪙)",
                                    fontSize = 8.5.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = if (isSelected) BlueprintTeal else BlueprintTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Preço Mínimo / Inicial", fontSize = 9.sp, color = BlueprintTextSecondary)
                        Text("${newItemPrice} ${if(newItemIsAuction) "💎" else "🪙"}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                    }
                    Slider(
                        value = newItemPrice.toFloat(),
                        onValueChange = { newItemPrice = it.toInt() },
                        valueRange = 100f..50000f,
                        colors = SliderDefaults.colors(thumbColor = BlueprintOrange, activeTrackColor = BlueprintOrange),
                        modifier = Modifier.height(20.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Add Button
                    Button(
                        onClick = { handlePublishItem() },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().testTag("publish_new_listing")
                    ) {
                        Text("REGISTRAR NA FILA DE LEILÕES", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Price history graph card
            Card(
                modifier = Modifier.weight(0.9f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C191E)),
                border = BorderStroke_workaround(BlueprintCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PriceChange, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HISTÓRICO MENSAL DE PREÇOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    Text("Valores consolidados de itens de raridade LENDÁRIA (Kimono Ouro):", fontSize = 8.sp, color = BlueprintTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    priceHistory.forEach { history ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(history.period, fontSize = 9.sp, color = BlueprintTextPrimary)
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${history.avgPriceJiCoin} JC", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("(${history.tradingVolume} Trades)", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(BlueprintGridLine, RoundedCornerShape(2.dp))
                        ) {
                            val ratio = history.avgPriceJiCoin / 13000f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(ratio)
                                    .background(BlueprintTeal, RoundedCornerShape(2.dp))
                                    .fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("• Evasão Tributária: Todas as compras sofrem tributação de 12%. Tentativas de fraudar o valor líquido de listagem acarretam bloqueio temporário de negociações.", fontSize = 7.5.sp, color = BlueprintRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Store listings section
        Text(
            text = "2. MERCADORIAS ATIVAS E LEILÕES P2P",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Filters horizontal row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "ALL" to "Todas Lojas",
                "LENDÁRIO" to "Lendários",
                "RARO" to "Raros",
                "AUCTION" to "Leilões/Bids"
            ).forEach { (key, label) ->
                val isSelected = selectedCategoryFilter == key
                Box(
                    modifier = Modifier
                        .background(if (isSelected) BlueprintCyan.copy(alpha = 0.2f) else BlueprintCard, RoundedCornerShape(4.dp))
                        .border(0.5.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                        .clickable { selectedCategoryFilter = key }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) BlueprintCyan else BlueprintTextSecondary)
                }
            }
        }

        // Active Listings List
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val filteredListings = listings.filter { item ->
                    when (selectedCategoryFilter) {
                        "ALL" -> true
                        "LENDÁRIO" -> item.rarity == "LENDÁRIO"
                        "RARO" -> item.rarity == "RARO"
                        "AUCTION" -> item.isAuction
                        else -> true
                    }
                }

                if (filteredListings.isEmpty()) {
                    Text(
                        text = "Nenhum item ativo correspondente ao filtro.",
                        fontSize = 10.sp,
                        color = BlueprintTextSecondary,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }

                filteredListings.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(item.rarityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, item.rarityColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(item.rarity, fontSize = 7.sp, color = item.rarityColor, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            }
                            Text(
                                text = "Vendedor: ${item.seller} • ${if (item.isAuction) "Leilão de Lance" else "Compra Direta"}",
                                fontSize = 8.sp,
                                color = BlueprintTextSecondary
                            )
                        }

                        // Auction progress / detail
                        if (item.isAuction) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text("LANCE ATUAL", fontSize = 6.5.sp, color = BlueprintTextSecondary)
                                Text("${item.highestBid} ${item.currency}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange, fontFamily = FontFamily.Monospace)
                                Text("Por: ${item.highestBidder}", fontSize = 7.sp, color = BlueprintTextSecondary)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text("CUSTO", fontSize = 6.5.sp, color = BlueprintTextSecondary)
                                Text("${item.price} ${item.currency}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Buy/Bid triggers
                        if (item.status == "SOLD") {
                            Box(
                                modifier = Modifier
                                    .background(BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("ESGOTADO / VENDIDO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (item.isAuction) {
                                        // Place a custom bid + increment
                                        val increment = (item.highestBid * 0.1f).toInt()
                                        item.highestBid += if (increment > 0) increment else 50
                                        item.highestBidder = "SuaConta_You"
                                        
                                        auditLogs.add(0, AuditLogSim(
                                            timestamp = "15:47:01",
                                            action = "AUCTION_BID_SUBMITTED",
                                            details = "Lutador SuaConta_You efetuou um lance de ${item.highestBid} 💎 no item ID #${item.id}.",
                                            txHash = "0x" + (349282..990238).random().toString(16),
                                            status = "SECURE"
                                        ))
                                    } else {
                                        handleBuyItem(item)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (item.isAuction) BlueprintOrange else BlueprintTeal
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (item.isAuction) "DAR LANCE (+10%)" else "COMPRAR AGORA",
                                    fontSize = 8.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Third Section: Strict Fraud Prevention & Mutex Locks Simulation
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3. ENGENHARIA DE AUDITORIA E REGILISTROS DE SEGURANÇA",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = BlueprintRed, modifier = Modifier.size(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "A base de dados do JiuVerse executa isolamento SERIALIZABLE nas transações P2P. Isso impede abusos do tipo Race Conditions (tentativa de vender o mesmo item para duas pessoas simultaneamente para duplicar moedas).",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Administrative Fraud injection triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showFraudNotice = true
                            fraudTimestamp = "15:48:10"
                            auditLogs.add(0, AuditLogSim(
                                timestamp = fraudTimestamp,
                                action = "FRAUD_ATTEMPT_DETECTED",
                                details = "ALERTA: Usuário 'SpeedyGuardPass' disparou 2 transações em paralelo sob o mesmo ID de Item. Mutex ativo interceptou.",
                                txHash = "0xCE40DE2399BF",
                                status = "FRAUD_BLOCKED"
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintRed.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SIMULAR ATAQUE RACE-CONDITION (DUPLICAÇÃO)", fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showFraudNotice = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Text("FECHAR DEPURADOR", fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Conditionally Show Fraud Blocking alert
                AnimatedVisibility(visible = showFraudNotice) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF3B1E28), RoundedCornerShape(4.dp))
                            .border(1.dp, BlueprintRed, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = BlueprintRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "🚨 SISTEMA DE AUDITORIA INTERCEPTOR (DUPE_PROTECT_V2)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintRed,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "Tentativa de processar duas transferências concorrentes para o mesmo ID do kimono falhou. O ID correlação travou o Kimono em Lock State e alertou a administração.",
                                fontSize = 9.sp,
                                color = BlueprintTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time audit logs
                Text("HISTÓRICO DE AUDITORIA DE TRANSAÇÃO (LIVRO DA CAMADA DE SEGURANÇA):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                Spacer(modifier = Modifier.height(4.dp))

                auditLogs.forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(6.dp)
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("[${log.timestamp}]", fontSize = 8.sp, color = BlueprintTextSecondary, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log.action,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (log.status == "FRAUD_BLOCKED") BlueprintRed else if(log.status == "MUTEX_LOCKED") BlueprintOrange else BlueprintTeal
                                )
                            }
                            Text(log.details, fontSize = 8.sp, color = BlueprintTextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("TX_HASH", fontSize = 6.sp, color = BlueprintTextSecondary)
                            Text(log.txHash, fontSize = 8.sp, color = BlueprintCyan, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Workaround Border Stroke function
private fun BorderStroke_workaround(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(0.5.dp, color)
}
