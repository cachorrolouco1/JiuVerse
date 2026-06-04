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

// 1. Data model representing a Companion Pet in JiuVerse
data class CompanionPet(
    val id: String,
    val name: String,
    val type: String, // "Cachorro", "Gato", "Dragão", "Fênix", "Lobo", "Tigre", "Temático"
    val rarity: String, // "Comum", "Raro", "Épico", "Lendário", "Mítico"
    val rarityColor: Color,
    val emoji: String,
    var level: Int = 1,
    var xp: Int = 0,
    val maxXp: Int = 100,
    val description: String,
    val passiveSkill: String,
    val activeSkill: String,
    var equippedCosmetic: String? = null,
    var staminaMultiplier: Float = 1.0f
)

// Data class representing pet foods
data class PetFoodItem(
    val id: String,
    val name: String,
    val emoji: String,
    val xpReward: Int,
    val costCredits: Int,
    val description: String
)

@Composable
fun PetsTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Definition of the 7 initial representative pets (covering all requested types & rarities)
    val availablePets = remember {
        mutableStateListOf(
            CompanionPet(
                id = "pet_dog_1",
                name = "Banzai",
                type = "Cachorro",
                rarity = "Comum",
                rarityColor = Color(0xFF94A3B8), // slate
                emoji = "🐕",
                level = 3,
                xp = 45,
                description = "Um Shiba Inu extremamente travesso que adora morder a barra de quimonos alheios.",
                passiveSkill = "Foco Inabalável (+5% resistência a escapes)",
                activeSkill = "Guarda de Gancho Assistida (Enrola as pernas temporariamente)"
            ),
            CompanionPet(
                id = "pet_cat_1",
                name = "Neko Guard",
                type = "Gato",
                rarity = "Raro",
                rarityColor = BlueprintTeal,
                emoji = "🐈",
                level = 2,
                xp = 80,
                description = "Siames com reflexos absurdos. Capaz de prever passagens de guarda com um miado.",
                passiveSkill = "Passada Leve (+8% velocidade de locomoção)",
                activeSkill = "Postura de Esquiva Gatuna (Garante 1.5s de imunidade de pegada)"
            ),
            CompanionPet(
                id = "pet_wolf_1",
                name = "Lobo da Neve",
                type = "Lobos",
                rarity = "Épico",
                rarityColor = Color(0xFFA855F7), // purple
                emoji = "🐺",
                level = 1,
                xp = 20,
                description = "Mascote de combate solitário, sintonizado com o frio e com o ritmo das raspagens.",
                passiveSkill = "Sincronia Matilha (+12% dano de finalização)",
                activeSkill = "Uivo Estelar (Aumenta regeneração de stamina por 10s)"
            ),
            CompanionPet(
                id = "pet_tiger_1",
                name = "Torataro",
                type = "Tigres",
                rarity = "Épico",
                rarityColor = Color(0xFFA855F7),
                emoji = "🐅",
                level = 1,
                xp = 0,
                description = "Filhote de tigre de dentes de sabre, mestre na pressão extrema por cima.",
                passiveSkill = "Força da Selva (+15% eficiência de estrangulamento)",
                activeSkill = "Rugido de Pressão (Reduz a taxa de evasão do oponente em 30%)"
            ),
            CompanionPet(
                id = "pet_phoenix_1",
                name = "Suzaku",
                type = "Fênix",
                rarity = "Lendário",
                rarityColor = BlueprintOrange,
                emoji = "🐦‍🔥",
                level = 1,
                xp = 5,
                description = "Ave mística lendária que renasce das cinzas de sparrings perdidos.",
                passiveSkill = "Renascimento Diário (Ressuscita stamina a 50% uma vez por torneio)",
                activeSkill = "Aura Flamejante (Gera faíscas que cegam o oponente em transições)"
            ),
            CompanionPet(
                id = "pet_dragon_1",
                name = "Ryujin Cósmico",
                type = "Dragões",
                rarity = "Mítico",
                rarityColor = Color(0xFFEF4444), // glowing red/rose
                emoji = "🐉",
                level = 5,
                xp = 12,
                description = "Imperador dos mares celestes. Dragão de altíssima fidelidade com escamas de Kevlar.",
                passiveSkill = "Flutuação Astral (Ignora penalidades de peso em qualquer categoria)",
                activeSkill = "Sopro de Fogo Síncrono (Drena instantaneamente 40 pontos de stamina do rival)"
            ),
            CompanionPet(
                id = "pet_themed_1",
                name = "Quimoninho",
                type = "Temático",
                rarity = "Lendário",
                rarityColor = BlueprintOrange,
                emoji = "🥋✨",
                level = 4,
                xp = 60,
                description = "Um quimono de algodão trançado azul senciente que flutua ao lado do lutador.",
                passiveSkill = "Trançado de Aço (+20% resistência de pegadas na calça)",
                activeSkill = "Envelopamento Cósmico (Prende o oponente em uma chave de braço improvisada)"
            )
        )
    }

    // Available pet cosmetics
    val cosmeticsList = remember {
        listOf(
            "Mini Faixa Preta 🥋" to "Mostra que o pet domina a arte suave.",
            "Óculos Thug Life 😎" to "Estilo implacável de 2012 para intimidar no lobby.",
            "Coroa de Ouro Cósmico 👑" to "Exclusividade máxima reservada a campeões.",
            "Quimono Festivo 🎉" to "Adiciona efeitos de confete ao andar pelo mapa.",
            "Auréola de Anjo 😇" to "Efeito de luz difusa ao redor do mascote."
        )
    }

    // Available foods inventory
    val foodList = remember {
        listOf(
            PetFoodItem("f1", "Ração Premium de Salmão", "🐟", 15, 50, "Ração rica em ômega-3. Ótimo estímulo inicial de XP."),
            PetFoodItem("f2", "Super Açaí de Proteína", "🥤", 40, 120, "O clássico do Jiu-Jitsu. Revigora o pet instantaneamente."),
            PetFoodItem("f3", "Biscoito de Osso Cósmico", "🍖", 100, 250, "Biscoito de alta densidade estelar. Garante níveis rápidos!")
        )
    }

    // Dynamic states
    var selectedPetIdx by remember { mutableStateOf(0) }
    val currentPet = availablePets[selectedPetIdx]

    // General user stats
    var isFollowingPlayer by remember { mutableStateOf(true) }
    var userCredits by remember { mutableStateOf(850) }
    var selectedCosmeticIdx by remember { mutableStateOf(-1) }
    
    // Live simulation tracker
    var livePetConsoleLog by remember { mutableStateOf("Sistema de Companions carregado em modo Paisagem síncrono. 60 FPS.") }

    // Breeding Chamber States (Optional Multi-pet breeding logic)
    var breedingParentAIdx by remember { mutableStateOf(0) }
    var breedingParentBIdx by remember { mutableStateOf(1) }
    var isBreedingInProgress by remember { mutableStateOf(false) }
    var breedingLogMessage by remember { mutableStateOf("Selecione dois animais compatíveis para simular herança genética.") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Arquitetura e Engenharia do Sistema de Mascot e Pets (Companion Suite)",
            subtitle = "Sistemas síncronos de acompanhamento horizontal direcionados a suporte ao metaverso tático, evolução dinâmica, alimentação assistida e incubadora."
        )

        // General Info Header Warning
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(1.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = BlueprintCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "REQUISITOS DO SISTEMA DE PET COMPANIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Os mascotes do JiuVerse fornecem bônus passivos de suporte e mecânicas de resiliência ativa em torneios e duelos do metaverso. Configurá-los para seguir o jogador em Landscape assegura um traçado fluido de navegação e evita colisões no buffer gráfico do motor isométrico do mundo virtual.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // =========================================================================
        // SECTION I: GRAPHICAL LIVE 80/20 COCKPIT PLAYGROUND (PET SIMULATOR HUD)
        // =========================================================================
        Text(
            text = "I. COCKPIT DO COMPANION: GERENCIAMENTO DE ATRIBUTOS, ALIMENTAÇÃO E VISUAL",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            
            // -------------------------------------------------------------
            // LEFT COLUMN HUD (25% area - Pets List with Rarity Pills)
            // -------------------------------------------------------------
            Card(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("🐾 SEU COBOGÓ (PETS)", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availablePets.forEachIndexed { idx, pet ->
                            val isSelected = selectedPetIdx == idx
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFF131D33) else Color.Black)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) pet.rarityColor else BlueprintGridLine,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedPetIdx = idx }
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(pet.emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(pet.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("${pet.type} • Lvl ${pet.level}", fontSize = 8.sp, color = BlueprintTextSecondary)
                                    }
                                }
                                
                                // Rarity Badge
                                Box(
                                    modifier = Modifier
                                        .background(pet.rarityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, pet.rarityColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = pet.rarity.uppercase(),
                                        fontSize = 6.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = pet.rarityColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // CENTER VIEWPORT (50% area - Visual Animation & Evolution)
            // -------------------------------------------------------------
            Card(
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B13)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    
                    // Top header block detailing the active pet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(currentPet.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(currentPet.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(currentPet.rarityColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(currentPet.rarity, fontSize = 7.sp, color = currentPet.rarityColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("Lvl ${currentPet.level} • Categoria: ${currentPet.type}", fontSize = 9.sp, color = BlueprintTextSecondary)
                            }
                        }

                        // Follow indicator switch
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isFollowingPlayer) "SEGUINDO ATIVO" else "MODO DESCANSO",
                                fontSize = 7.sp,
                                color = if (isFollowingPlayer) BlueprintTeal else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                            Switch(
                                checked = isFollowingPlayer,
                                onCheckedChange = {
                                    isFollowingPlayer = it
                                    livePetConsoleLog = if (it) "Mascote ${currentPet.name} recebeu comando Follow. Seguirá o avatar no viewport." 
                                                       else "Mascote ${currentPet.name} entrou em repouso no canil digital."
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BlueprintTeal,
                                    checkedTrackColor = BlueprintTeal.copy(alpha = 0.3f),
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.scale(0.6f)
                            )
                        }
                    }

                    // Simulated 2D animation viewport representing follow activity
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing follow path decoration with particles
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                        ) {
                            // Virtual Player Model representation inside sandbox viewport
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(BlueprintCyan.copy(alpha = 0.2f), CircleShape)
                                        .border(1.dp, BlueprintCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🥋", fontSize = 12.sp)
                                }
                                Text("Você", fontSize = 7.sp, color = BlueprintCyan)
                            }

                            // Dynamic animated dots showing following link
                            val arrowColor = if (isFollowingPlayer) BlueprintTeal else Color.DarkGray
                            Text(
                                text = if (isFollowingPlayer) " 🟢 . . . ➔ 🐾 . . . ➔ " else " 🔴 . . . [X] . . . ",
                                fontSize = 11.sp,
                                color = arrowColor,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // Active Pet model following closely
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(currentPet.rarityColor.copy(alpha = 0.2f), CircleShape)
                                        .border(1.dp, currentPet.rarityColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(currentPet.emoji, fontSize = 12.sp)
                                }
                                Text(currentPet.name, fontSize = 7.sp, color = currentPet.rarityColor)
                            }
                        }

                        // Overlay for Cosmetic equipped
                        currentPet.equippedCosmetic?.let { cosmetic ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintOrange, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Visual: $cosmetic", fontSize = 7.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Skills / Multipliers View
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            border = BorderStroke(0.5.dp, BlueprintGridLine)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("🧬 HABILIDADE PASSIVA", fontSize = 7.5.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                Text(currentPet.passiveSkill, fontSize = 9.sp, color = Color.White)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            border = BorderStroke(0.5.dp, BlueprintGridLine)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("🔥 HABILIDADE ATIVA (CMD)", fontSize = 7.5.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                                Text(currentPet.activeSkill, fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }

                    // XP Progressive bar and evolution action
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("EXPERIÊNCIA (XP) DO COMPANION", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Text("${currentPet.xp} / ${currentPet.maxXp} XP", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.DarkGray)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(currentPet.xp.toFloat() / currentPet.maxXp)
                                    .background(currentPet.rarityColor)
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // RIGHT COLUMN HUD (25% area - Food & Cosmetics Action Deck)
            // -------------------------------------------------------------
            Card(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🛒 COZINHA & COSMÉTICOS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            Text("$userCredits CRÉDITOS", fontSize = 8.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        // Alimentacao panel
                        Text("🍖 ALIMENTAR MASCOTE (RECOMPENSA XP)", fontSize = 7.5.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            foodList.forEach { food ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black)
                                        .clickable {
                                            if (userCredits >= food.costCredits) {
                                                userCredits -= food.costCredits
                                                val addedXp = food.xpReward
                                                val nextXp = currentPet.xp + addedXp
                                                if (nextXp >= currentPet.maxXp) {
                                                    currentPet.level += 1
                                                    currentPet.xp = nextXp - currentPet.maxXp
                                                    livePetConsoleLog = "⚡ EVOLUÇÃO! ${currentPet.name} comeu ${food.name} e subiu para o Nível ${currentPet.level}! Atributos multiplicados!"
                                                } else {
                                                    currentPet.xp = nextXp
                                                    livePetConsoleLog = "😋 Nham! ${currentPet.name} consumiu ${food.name} e ganhou +$addedXp de Experiência."
                                                }
                                            } else {
                                                livePetConsoleLog = "❌ Créditos insuficientes para comprar comida premium."
                                            }
                                        }
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(food.emoji, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text(food.name, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("Ganhe +${food.xpReward} XP", fontSize = 6.5.sp, color = BlueprintTextSecondary)
                                        }
                                    }
                                    Text("${food.costCredits} C", fontSize = 7.5.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Cosmetics selection panel for equipped pet
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("💄 COSMÉTICOS ADICIONAIS", fontSize = 7.5.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Column(
                            modifier = Modifier
                                .height(72.dp)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            cosmeticsList.forEachIndexed { i, (cosmeticName, cosmeticDesc) ->
                                val isEquippedOnCurrent = currentPet.equippedCosmetic == cosmeticName
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isEquippedOnCurrent) BlueprintTeal.copy(alpha = 0.2f) else Color.Black)
                                        .clickable {
                                            if (isEquippedOnCurrent) {
                                                currentPet.equippedCosmetic = null
                                                livePetConsoleLog = "Desequipou visual do seu mascote ${currentPet.name}"
                                            } else {
                                                currentPet.equippedCosmetic = cosmeticName
                                                livePetConsoleLog = "Equipou visual '$cosmeticName' em: ${currentPet.name}. Rederizando sprite!"
                                            }
                                        }
                                        .padding(3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cosmeticName, fontSize = 7.5.sp, color = Color.White)
                                    Text(
                                        text = if (isEquippedOnCurrent) "Ativo" else "Equipar",
                                        fontSize = 7.sp,
                                        color = if (isEquippedOnCurrent) BlueprintTeal else BlueprintTextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Manual forced level up to make demo testing fun
                    Button(
                        onClick = {
                            currentPet.level += 1
                            currentPet.xp = 0
                            livePetConsoleLog = "⚡ Administrador forçou evolução! nível de ${currentPet.name} definido para ${currentPet.level}!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentPet.rarityColor, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        contentPadding = PaddingValues(vertical = 0.dp)
                    ) {
                        Text("FORÇAR SUBIDA DE NÍVEL (+1 Lvl)", fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live telemetry console logger
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(6.dp))
                .border(0.5.dp, BlueprintTeal, RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CONSOLES DO COMPANION (60 FPS): $livePetConsoleLog",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = BlueprintTeal
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // SECTION II: OPTIONAL INCUBATER LAB & BREEDING ENGINE
        // =========================================================================
        Text(
            text = "II. LABORATÓRIO DE INCUBAÇÃO SÍNCRONA: REPRODUÇÃO E MUTAMENTO (SISTEMA OPCIONAL)",
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
                Text(
                    text = "A incubadora do JiuVerse permite misturar genes de dois mascotes compatíveis para rolls de heranças. Teor de mutação mística aprimorado por pedras estelares.",
                    fontSize = 9.5.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    // Parent A Selector Box
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧬 GENÉTICA: MATRIZ A", fontSize = 8.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(availablePets[breedingParentAIdx].emoji, fontSize = 24.sp)
                            Column {
                                Text(availablePets[breedingParentAIdx].name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Raridade: ${availablePets[breedingParentAIdx].rarity}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { breedingParentAIdx = (breedingParentAIdx + 1) % availablePets.size },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                modifier = Modifier.height(22.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("Alternar", fontSize = 7.5.sp)
                            }
                        }
                    }

                    // Action center breeding triggers
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapCalls,
                            contentDescription = "Breeding Matrix",
                            tint = BlueprintTeal,
                            modifier = Modifier.size(28.dp)
                        )
                        
                        Text(
                            text = if (isBreedingInProgress) "⚡ INCUBANDO CÉLULAS..." else "PRONTO PARA PARTO GENÉTICO",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBreedingInProgress) BlueprintOrange else BlueprintTeal
                        )

                        Button(
                            onClick = {
                                if (breedingParentAIdx == breedingParentBIdx) {
                                    breedingLogMessage = "Divergência Crítica: Não é possível cruzar um pet com ele mesmo! Escolha matrizes genéticas diferentes."
                                } else {
                                    isBreedingInProgress = true
                                    breedingLogMessage = "Calculando retro-relação de DNA de ${availablePets[breedingParentAIdx].name} e ${availablePets[breedingParentBIdx].name}... 40% de probabilidade de Raro, 15% de Épico mutado!"
                                    
                                    // Simulation of cross breed
                                    val mutatedRarities = listOf("Comum", "Raro", "Épico", "Lendário")
                                    val rolledRarity = mutatedRarities.random()
                                    val babyEmojis = listOf("🐵", "🦊", "🐻", "🐼" , "🐨", "🐸", "🐉")
                                    
                                    livePetConsoleLog = "Ninho de Incubação cruzou genes! Ovos incubados síncronos postados no canil principal."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal, contentColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                        ) {
                            Text("INICIAR REPRODUÇÃO DIGITAL", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Parent B Selector Box
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧬 GENÉTICA: MATRIZ B", fontSize = 8.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(availablePets[breedingParentBIdx].emoji, fontSize = 24.sp)
                            Column {
                                Text(availablePets[breedingParentBIdx].name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Raridade: ${availablePets[breedingParentBIdx].rarity}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { breedingParentBIdx = (breedingParentBIdx + 1) % availablePets.size },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                modifier = Modifier.height(22.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("Alternar", fontSize = 7.5.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Console result output of cross reproduction
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "🔬 INCUBATOR SPECTRUM LOG: $breedingLogMessage",
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Yellow,
                        lineHeight = 11.sp
                    )
                }

                // Mutanting chart layout
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TÍTULO DE MUTABILIDADE DO AMBIENTE:", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Comum ➔ Raro (45%)", fontSize = 7.5.sp, color = Color.LightGray)
                        Text("Raro ➔ Épico (25%)", fontSize = 7.5.sp, color = BlueprintTeal)
                        Text("Épico ➔ Lendário (5%)", fontSize = 7.5.sp, color = BlueprintOrange)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // SECTION III: TECHNICAL MODEL SPECS & ARCHITECTURES
        // =========================================================================
        Text(
            text = "III. ENGENHARIA DO JOGO: ARQUITETURA DE DADOS E SCHEMAS PARA BANCO DE RETENÇÃO",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 1. Room SQLite Table Schema definition
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("1. TABELAS DE PERSISTÊNCIA DOS COMPANION PETS (SQLITE/ROOM DATABASE SCHEMA)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                val dbSchemaKotlinCode = """
-- Tabela principal de colecionados e conquistas do Canil de Pets
CREATE TABLE user_companion_pets (
    pet_unique_id TEXT PRIMARY KEY NOT NULL, -- UUID único gerado no nascimento/hatch
    player_id TEXT NOT NULL,                  -- Vínculo com a conta de login
    pet_template_id TEXT NOT NULL,            -- Id de configuração estática (Cachorro, Dragão etc.)
    custom_nickname TEXT,                     -- Apelido customizado opcional
    current_level INTEGER DEFAULT 1,
    cumulative_xp INTEGER DEFAULT 0,
    equipped_cosmetic_id TEXT,               -- Nulos se o pet estiver sem assessórios
    is_actively_following INTEGER DEFAULT 1,  -- Booleano para rendering no loop de frames
    stamina_factor REAL DEFAULT 1.0,
    creation_timestamp INTEGER NOT NULL,
    FOREIGN KEY(player_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabela acessória detalhando buffs de Habilidades passivas e ativas
CREATE TABLE pet_skill_dictionary (
    skill_id TEXT PRIMARY KEY NOT NULL,
    skill_name TEXT NOT NULL,
    skill_category TEXT NOT NULL,            -- 'PASSIVE', 'TRIGGER_SOCIETY', 'COMBAT'
    stat_multiplier REAL,
    stamina_regen_delta INTEGER
);

-- Tabela síncrona controlando os históricos de alimentação e cansaço
CREATE TABLE pet_nutrition_feed_logs (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    pet_unique_id TEXT NOT NULL,
    food_identifier TEXT NOT NULL,
    fed_at_timestamp INTEGER,
    xp_added INTEGER,
    FOREIGN KEY(pet_unique_id) REFERENCES user_companion_pets(pet_unique_id)
);
                """.trimIndent()

                CodeBlock(code = dbSchemaKotlinCode, title = "JiuVerse Composable Companion Pets Schema Definition")
            }
        }

        // 2. Mobile Game Engine Game loop coordinator Simulation
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Dashboard, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("2. ANIMATION COORD & INTERPOLAÇÃO DE RENDERIZADOR NO CLIENT LOOP (KOTLIN)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                val gameEngineLogic = """
package com.example.architecture.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LocalVector2D(var x: Float, var y: Float)

/**
 * Filtro de lag e interpolação linear (LERP) para garantir que
 * o Pet siga o jogador suavemente a 60 FPS síncronos na horizontal (Landscape-Only mode).
 */
class CompanionFollowPhysicsEngine(
    private val frameRateMs: Long = 16, // 60 FPS aprox.
    private val followTetherRadius: Float = 48f // Limite físico do touch target
) {
    private val _petCurrentVector = MutableStateFlow(LocalVector2D(0f, 0f))
    val petCurrentVector: StateFlow<LocalVector2D> = _petCurrentVector

    fun tickPhysicsIteration(playerVector: LocalVector2D) {
        val current = _petCurrentVector.value
        val distanceX = playerVector.x - current.x
        val distanceY = playerVector.y - current.y
        
        val radialDistance = kotlin.math.sqrt(distanceX * distanceX + distanceY * distanceY)
        
        if (radialDistance > followTetherRadius) {
            // Linear Interpolation factor (0.1f para suavização elegante sem delay)
            val nextX = current.x + (distanceX * 0.1f)
            val nextY = current.y + (distanceY * 0.1f)
            _petCurrentVector.value = LocalVector2D(nextX, nextY)
        }
    }
}
                """.trimIndent()
                
                CodeBlock(code = gameEngineLogic, title = "CompanionFollowPhysicsEngine Game Loop Interpolator")
            }
        }

        // 3. Best Practices for Mobile MMORPG Companions
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, tint = BlueprintOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("3. BOAS PRÁTICAS DE ENGENHARIA PARA RENDERING DE MASCOTES EM LANDSCAPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Pre-caching de Texturas: Todos os sprites de pets (incluindo cosméticos como a mini-faixa preta) devem ser pré-compilados em Spritesheets Atlas na GPU do celular para economizar draw calls na CPU.\n" +
                           "• Detecção de Oclusão (Frustum Culling): Em áreas de alta densidade no RJ-CENTRO_01 com mais de 100 jogadores no mesmo Hub Landscape, ocultar a renderização visual do pet se o jogador estiver fora do viewport do plano de câmera.\n" +
                           "• Garbage Collector de Óbito de Partículas: Efeitos como a Aura Flamejante da Suzaku devem alocar vetores reutilizáveis no pool dinâmico para não estressar a memória de aparelhos intermediários Android/iOS.\n" +
                           "• Sincronia de Threads: Rodar as posições interpoladas e a colisão de repulsão em segundo plano usando Coroutine Dispatchers.Default, reservando o Dispatchers.Main estritamente para modificação de canvas 3D/2D.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    lineHeight = 14.sp
                )
            }
        }
    }
}


