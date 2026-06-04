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

// 1. Core Data Models for Social & Marriage Systems
data class JiuVerseFriend(
    val id: String,
    val username: String,
    val beltColor: String, // "Branca", "Azul", "Roxa", "Marrom", "Preta"
    val rankScore: Int,
    var relationType: String, // "Amigo", "Melhor Amigo", "Seguidor", "Parceiro"
    val avatarEmoji: String,
    val associationDojo: String,
    var isMuted: Boolean = false,
    var isBlocked: Boolean = false,
    var mutualStatus: Boolean = true // True means we follow each other
)

data class SpecialTitle(
    val titleId: String,
    val name: String,
    val bonusDescription: String,
    val rarity: String
)

data class VirtualRing(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val priceCredits: Int,
    val statBonus: String
)

data class SharedHouse(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val priceCredits: Int,
    val xpBonusPercent: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- State: Database / Friends List ---
    val friendsList = remember {
        mutableStateListOf(
            JiuVerseFriend("f_1", "Gabriel_Guarda", "Azul", 1850, "Amigo", "🥋", "Gracie Barra Angra"),
            JiuVerseFriend("f_2", "Ana_Passadora", "Preta", 2950, "Melhor Amigo", "🥷", "Alliance RJ"),
            JiuVerseFriend("f_3", "Bruninho_Berimbolo", "Roxa", 2100, "Amigo", "🥋", "Kodan Shugyo"),
            JiuVerseFriend("f_4", "Kru_Somchai", "Preta", 3120, "Parceiro", "🥊", "Tiger Muay Thai"),
            JiuVerseFriend("f_5", "Dona_Estela", "Branca", 350, "Seguidor", "👵", "Dojo Municipal de Angra"),
            JiuVerseFriend("f_6", "Carlão_Submission", "Marrom", 2400, "Melhor Amigo", "🐻", "Checkmat Central")
        )
    }

    // --- State: Static Lists for Wedding Setup ---
    val ringsList = remember {
        listOf(
            VirtualRing("r_1", "Aço Galvanizado Fosco", "Feita de ligas leves táticas de baixo custo.", "💍", 100, "+5% Ganho de Stamina"),
            VirtualRing("r_2", "Fibra de Carbono Trançada", "Combinação perfeita com quimonos pretos de luxo.", "🖤", 500, "+12% Velocidade de Escapes"),
            VirtualRing("r_3", "Ouro Branco Cósmico", "Aliança sagrada polida com poeira estelar de Angra.", "✨", 1500, "+20% Resistência a Golpes"),
            VirtualRing("r_4", "Esmeralda Infinita do Tatame", "Mítica. Apenas os de espírito inabalável sabem fabricar.", "💚", 4500, "+30% Velocidade de Envergadura")
        )
    }

    val housesList = remember {
        listOf(
            SharedHouse("h_1", "Flat na Praia do Arpoador", "Excelente vista para treinos de NoGi na beira do mar.", "🏖️", 800, 5),
            SharedHouse("h_2", "Loft Integrado Gracie", "Situado no segundo andar do ginásio. Dormir no tatame!", "🏬", 2200, 15),
            SharedHouse("h_3", "Palácio do Templo Cósmico", "Fliperamas, jacuzzi de açaí e tatame gravitacional.", "🏛️", 8000, 35)
        )
    }

    val titlesList = remember {
        listOf(
            SpecialTitle("t_1", "Dupla Dinâmica de Angra", "Bônus: +10% de XP compartilhado se jogarem juntos.", "Raro"),
            SpecialTitle("t_2", "Cônjuges da Alavanca Eterna", "Bônus: Regeneração de mana duplicada se na mesma sala.", "Épico"),
            SpecialTitle("t_3", "Reis do Absoluto Conjugal", "Bônus: +20% no dano de projeções em dupla (eSports).", "Lendário")
        )
    }

    // --- State: Configured Wedding Options ---
    var selectedRingIndex by remember { mutableStateOf(1) }
    var selectedHouseIndex by remember { mutableStateOf(0) }
    var selectedTitleIndex by remember { mutableStateOf(0) }
    var weddingPartnerName by remember { mutableStateOf("Ana_Passadora") }
    val weddingGuestsEnabled = remember { mutableStateMapOf<String, Boolean>() }
    
    // Default setup guests
    val initialGuests = remember { listOf("Grande Mestre Robson", "Clara (Recepção)", "Kru Somchai", "Sensei Yamato", "Kauã (Guia)") }
    
    // --- State: Live Active Marriage Simulation Process ---
    var ceremonyStage by remember { mutableStateOf(0) } // 0: Idle, 1: Procession, 2: Vows, 3: Ring Exchange, 4: Confessions, 5: Married!
    val isCeremonyRunning = ceremonyStage in 1..4
    var coupleTitleMessage by remember { mutableStateOf("Solteiro") }
    var showMarriedBadge by remember { mutableStateOf(false) }

    // --- State: Active Viewport Category Log ---
    val listCategories = listOf("Todos", "Amigos", "Melhores Amigos", "Seguidores", "Parceiros", "Muted/Blocked")
    var selectedCategoryTab by remember { mutableStateOf(0) }
    var textSearchUser by remember { mutableStateOf("") }
    var liveTelemetrySocialLog by remember { mutableStateOf("Central Social síncrona JiuVerse carregada e monitorando abusos.") }

    // --- State: Anti-Spam & Moderation Controls ---
    var antiSpamThresholdMs by remember { mutableStateOf(800f) } // slider for speed threshold
    var isProfanityFilterEnabled by remember { mutableStateOf(true) }
    var isCaptchaVerified by remember { mutableStateOf(false) }
    var messagesSentCount by remember { mutableStateOf(0) }
    var moderationSystemStatus by remember { mutableStateOf("Ativo & Saudável") }
    
    // Anti-Abuse Test Sandbox State
    var textMessagePayloadInput by remember { mutableStateOf("Quero lutar com aquele perdedor nojento!") }

    // Initial configuration of guests
    LaunchedEffect(Unit) {
        initialGuests.forEach { guest ->
            weddingGuestsEnabled.put(guest, true)
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
            title = "Arquitetura do Ecosystem Social e Altar de Consorciação (Casamento Virtual)",
            subtitle = "Sistemas cognitivos de agrupamento, conexões familiares, títulos de glória conjunta, combate a fraudes de canais, e segurança de dados do JiuVerse."
        )

        // General AI Info Card with quick telemetry data representation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(1.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = BlueprintCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VISÃO DE GAME SOCIAL DESIGNER (JIUVERSE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "A rede social do JiuVerse promove a retenção interativa via dinâmicas síncronas. O Casamento Virtual estabelece vínculos permanentes que geram canais de chat privados criptografados, bônus de XP de treino conjugal em casas compartilhadas e compartilhamento de itens sem taxas de intermediação.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // =========================================================================
        // SECTION 1: NETWORK MANAGER & CONTACT DIRECTORY (FRIENDS, FOLLOWERS, ETC)
        // =========================================================================
        Text(
            text = "I. PAINEL DE CONEXÕES SOCIAIS E DIRECTORY DE ATLETAS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Half: Add friend & interactive filter columns (60% width)
            Card(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Title index
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👥 SEGMENTOS DA REDE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintCyan
                        )
                        Badge(containerColor = BlueprintTeal.copy(alpha = 0.2f), contentColor = BlueprintTeal) {
                            Text("${friendsList.size} Atletas Conectados", fontSize = 7.5.sp)
                        }
                    }

                    // Horizontal subtab filters inside social network
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Todos", "Favoritos", "Parceiros", "Seguidores").forEachIndexed { idx, label ->
                            val isSelected = selectedCategoryTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) Color(0xFF1B3B4B) else Color.Black)
                                    .border(1.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable { selectedCategoryTab = idx }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) BlueprintCyan else Color.White
                                )
                            }
                        }
                    }

                    // Search field simulation
                    OutlinedTextField(
                        value = textSearchUser,
                        onValueChange = { textSearchUser = it },
                        placeholder = { Text("Buscar atleta pelo apelido no JiuVerse...", fontSize = 8.sp, color = BlueprintTextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Black,
                            unfocusedContainerColor = Color.Black,
                            focusedBorderColor = BlueprintCyan,
                            unfocusedBorderColor = BlueprintGridLine,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = BlueprintTextSecondary,
                            unfocusedPlaceholderColor = BlueprintTextSecondary
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.sp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Friends listing view with interactive state updates
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val filteredFriends = friendsList.filter { f ->
                            val matchSearch = f.username.lowercase().contains(textSearchUser.lowercase())
                            val matchesTab = when (selectedCategoryTab) {
                                1 -> f.relationType == "Melhor Amigo"
                                2 -> f.relationType == "Parceiro"
                                3 -> f.relationType == "Seguidor"
                                else -> true
                            }
                            matchSearch && matchesTab
                        }

                        if (filteredFriends.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhum atleta mapeado neste tier.", fontSize = 9.sp, color = BlueprintTextSecondary)
                            }
                        } else {
                            filteredFriends.forEach { friend ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (friend.isBlocked) Color(30, 10, 10) else Color.Black)
                                        .border(1.dp, if (friend.relationType == "Melhor Amigo") BlueprintOrange.copy(alpha = 0.6f) else BlueprintGridLine, RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .background(BlueprintCard, CircleShape)
                                                .border(1.dp, if (friend.isMuted) Color.Gray else BlueprintTeal, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(friend.avatarEmoji, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = friend.username,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (friend.isBlocked) Color.Red else Color.White
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color.Black)
                                                        .border(0.5.dp, BlueprintCyan, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 2.dp)
                                                ) {
                                                    Text(friend.beltColor, fontSize = 6.5.sp, color = BlueprintCyan)
                                                }
                                            }
                                            Text(
                                                text = "${friend.relationType} • ${friend.associationDojo}",
                                                fontSize = 8.sp,
                                                color = BlueprintTextSecondary
                                            )
                                        }
                                    }

                                    // Interactive controls (Toggle best-friend, mute, block)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        // Relation toggling icon
                                        IconButton(
                                            onClick = {
                                                friend.relationType = if (friend.relationType == "Melhor Amigo") {
                                                    liveTelemetrySocialLog = "Atleta ${friend.username} removido dos Melhores Amigos."
                                                    "Amigo"
                                                } else {
                                                    liveTelemetrySocialLog = "Atleta ${friend.username} promovido a MELHOR AMIGO (+15% XP de Treino)."
                                                    "Melhor Amigo"
                                                }
                                                // trigger state rewrite
                                                val i = friendsList.indexOf(friend)
                                                if (i != -1) {
                                                    friendsList[i] = friend.copy(relationType = friend.relationType)
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (friend.relationType == "Melhor Amigo") Icons.Default.Star else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favoritar",
                                                tint = if (friend.relationType == "Melhor Amigo") BlueprintOrange else BlueprintTextSecondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }

                                        // Mute Toggle Icon
                                        IconButton(
                                            onClick = {
                                                friend.isMuted = !friend.isMuted
                                                liveTelemetrySocialLog = if (friend.isMuted) "Silenciou canal de conversa direta com ${friend.username}." else "Restabeleceu som das comunicações de ${friend.username}."
                                                val i = friendsList.indexOf(friend)
                                                if (i != -1) {
                                                    friendsList[i] = friend.copy(isMuted = friend.isMuted)
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (friend.isMuted) Icons.Default.VolumeOff else Icons.Default.Chat,
                                                contentDescription = "Mudar silêncio",
                                                tint = if (friend.isMuted) Color.Red else BlueprintTeal,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }

                                        // Block Toggle Button
                                        Button(
                                            onClick = {
                                                friend.isBlocked = !friend.isBlocked
                                                liveTelemetrySocialLog = if (friend.isBlocked) "⚠️ BLOQUEIO: ${friend.username} foi bloqueado preventivamente." else "Liberou ${friend.username} do block list."
                                                val i = friendsList.indexOf(friend)
                                                if (i != -1) {
                                                    friendsList[i] = friend.copy(isBlocked = friend.isBlocked)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (friend.isBlocked) Color.Red else Color.DarkGray,
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                            modifier = Modifier.height(18.dp)
                                        ) {
                                            Text(if (friend.isBlocked) "DESBLOQUEAR" else "BLOQUEAR", fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Right Half: Add User (Registration simulation with strict rate-limiting / anti-spam demonstration)
            Card(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = BlueprintHeader),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(
                            text = "➕ REGISTRO DE AMIGO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintOrange
                        )
                        Text(
                            text = "Simule a entrada de oponentes.",
                            fontSize = 7.sp,
                            color = BlueprintTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        
                        var newFriendName by remember { mutableStateOf("") }
                        var newFriendBelt by remember { mutableStateOf("Azul") }
                        
                        Text("Apelido da Conta:", fontSize = 8.sp, color = Color.White)
                        OutlinedTextField(
                            value = newFriendName,
                            onValueChange = { newFriendName = it },
                            placeholder = { Text("Ex: Roger_Gracie", fontSize = 8.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.sp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Grau/Faixa de Combate:", fontSize = 8.sp, color = Color.White)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Azul", "Roxa", "Preta").forEach { belt ->
                                val sel = newFriendBelt == belt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (sel) BlueprintOrange.copy(alpha = 0.25f) else Color.Black)
                                        .border(1.dp, if (sel) BlueprintOrange else BlueprintGridLine, RoundedCornerShape(4.dp))
                                        .clickable { newFriendBelt = belt }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(belt, fontSize = 7.5.sp, color = if (sel) BlueprintOrange else Color.White)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (newFriendName.isNotBlank()) {
                                    // Anti spam check
                                    if (newFriendName.length > 20 || newFriendName.contains(" ") || newFriendName.contains("@")) {
                                        liveTelemetrySocialLog = "❌ Erro Anti-Spam: Caracteres inválidos nas contas parceiras."
                                    } else {
                                        friendsList.add(
                                            JiuVerseFriend(
                                                id = "f_gen_${System.currentTimeMillis()}",
                                                username = newFriendName.trim(),
                                                beltColor = newFriendBelt,
                                                rankScore = (1000..3000).random(),
                                                relationType = "Amigo",
                                                avatarEmoji = listOf("🥋", "🥷", "🏄‍♂️", "🦾", "🦊").random(),
                                                associationDojo = "Gracie Barra Central"
                                            )
                                        )
                                        liveTelemetrySocialLog = "✓ Sucesso: Convite enviado e aceito de volta para ${newFriendName}. Conexão síncrona iniciada!"
                                        newFriendName = ""
                                    }
                                }
                            },
                            enabled = newFriendName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueprintOrange, contentColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("ENVIAR SOLICITAÇÃO", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Social Benefits Block Quick View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(6.dp))
                            .border(0.5.dp, BlueprintTeal.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    ) {
                        Column {
                            Text("👑 BENEFÍCIOS ATIVOS DO CLÃ:", fontSize = 7.5.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                            Text("• Best Friends: +15% de XP em Sparrings síncronos.", fontSize = 7.sp, color = BlueprintTextSecondary)
                            Text("• Título Conjugal: Velocidade de regeneração do gás.", fontSize = 7.sp, color = BlueprintTextSecondary)
                            Text("• Casas: Armário de inventário síncrono descentralizado.", fontSize = 7.sp, color = BlueprintTextSecondary)
                        }
                    }
                }
            }
        }

        // Action Telemetry Logger Area below Directory
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
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "STATUS DE SEGURANÇA E FEEDBACK SOCIAL: $liveTelemetrySocialLog",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = BlueprintTeal
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // SECTION 2: THE MARRIAGE SANCTUARY & COGNITIVE ALTAR (VIRTUAL WEDDING HUD)
        // =========================================================================
        Text(
            text = "II. ALTAR SAGRADO DE CONDUÇÃO DE CASAMENTO VIRTUAL JIUVERSE",
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
                    text = "Monte casamentos virtuais no metaverso obtendo alianças táticas que aprimoram parâmetros de luta, comprem casas compartilhadas que impulsionam experiência de fita e escolham o título do casal.",
                    fontSize = 9.5.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // SETUP COLUMN (Configurador de Casamento) - Left 55%
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚙️ PARÂMETROS DO COMPROMISSO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintCyan
                        )

                        // Choose Partner Dropdown Selection representation
                        Column {
                            Text("Selecione o Cônjuge (Melhores Amigos & Amigos):", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable {
                                        // Pick a different partner for simulation
                                        val candidates = friendsList.filter { it.relationType == "Melhor Amigo" || it.relationType == "Amigo" }
                                        if (candidates.isNotEmpty()) {
                                            val currentIdx = candidates.indexOfFirst { it.username == weddingPartnerName }
                                            val nextIdx = (currentIdx + 1) % candidates.size
                                            weddingPartnerName = candidates[nextIdx].username
                                        }
                                    }
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💍 $weddingPartnerName", fontSize = 10.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                                Text("Alternar", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                            }
                        }

                        // Rings Grid
                        Column {
                            Text("1. Selecione a Aliança Virtual (Upgrade de Buffs):", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                ringsList.forEachIndexed { idx, ring ->
                                    val isSelected = selectedRingIndex == idx
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) BlueprintCyan.copy(alpha = 0.15f) else Color.Black)
                                            .border(0.5.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .clickable { selectedRingIndex = idx }
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(ring.emoji, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column {
                                                Text(ring.name, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text(ring.statBonus, fontSize = 6.5.sp, color = BlueprintTeal)
                                            }
                                        }
                                        Text("${ring.priceCredits} C", fontSize = 7.5.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Shared Houses Selectors
                        Column {
                            Text("2. Escolha sua Casa Compartilhada Conjugal:", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                housesList.forEachIndexed { idx, house ->
                                    val isSelected = selectedHouseIndex == idx
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) BlueprintTeal.copy(alpha = 0.15f) else Color.Black)
                                            .border(1.dp, if (isSelected) BlueprintTeal else BlueprintGridLine, RoundedCornerShape(6.dp))
                                            .clickable { selectedHouseIndex = idx }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(house.emoji, fontSize = 16.sp)
                                            Text(house.name.split(" ").last(), fontSize = 7.5.sp, color = Color.White, textAlign = TextAlign.Center)
                                            Text("+${house.xpBonusPercent}% XP Co-op", fontSize = 6.5.sp, color = BlueprintTeal)
                                        }
                                    }
                                }
                            }
                        }

                        // Special Couple Title selection
                        Column {
                            Text("3. Escolha o Título Conjugal de Honra:", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                titlesList.forEachIndexed { idx, t ->
                                    val isSelected = selectedTitleIndex == idx
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) BlueprintOrange.copy(alpha = 0.15f) else Color.Black)
                                            .border(1.dp, if (isSelected) BlueprintOrange else BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .clickable { selectedTitleIndex = idx }
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(t.name, fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                            Text(t.rarity, fontSize = 6.sp, color = BlueprintOrange)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // CEREMONY ENGAGING ACTION & ANIMATED VIEWPORT - Right 45%
                    Column(
                        modifier = Modifier.weight(0.8f),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⛪ CEREMÔNIA DE CONSORCIAÇÃO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintOrange
                        )

                        // Live Stage Visualizer Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(Color.Black, RoundedCornerShape(8.dp))
                                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Live tracker labels based on ceremony state
                                val statusLabel = when (ceremonyStage) {
                                    1 -> "🚶 Entrada dos Noivos no Altar..."
                                    2 -> "📜 Mestre Robson recitando as Leis do Jiu-Jitsu..."
                                    3 -> "💍 Troca de Alianças Táticas..."
                                    4 -> "🎉 Casados! Jogando pétalas de flores!"
                                    5 -> "💖 Matrimônio síncrono registrado!"
                                    else -> "⛪ Altar Geral no Templo Vazio"
                                }

                                val detailsLabel = when (ceremonyStage) {
                                    1 -> "Você e $weddingPartnerName caminham sob aplausos de Angra."
                                    2 -> "'O primeiro dever do casamento é respeitar a guarda alheia.'"
                                    3 -> "Aliança '${ringsList[selectedRingIndex].name}' equipada!"
                                    4 -> "Bônus de +XP da '${housesList[selectedHouseIndex].name}' ativado!"
                                    5 -> "TÍTULO ATIVO: ${titlesList[selectedTitleIndex].name}"
                                    else -> "Pronto para iniciar."
                                }

                                // Visual Render Simulation of Stage
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .background(Color(0xFF0F172A), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCeremonyRunning || ceremonyStage == 5) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text("🥋", fontSize = 28.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = when (ceremonyStage) {
                                                    1 -> " ➔ ❤️ 🥷"
                                                    2 -> " 📿 ❤️ 🥷"
                                                    3 -> " 💍 ❤️ ⛓️"
                                                    4 -> " 💒 ✨ 🍾"
                                                    5 -> " ✨ 🍾 🏆"
                                                    else -> ""
                                                },
                                                fontSize = 20.sp,
                                                color = BlueprintCyan
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("🥷", fontSize = 28.sp)
                                        }
                                    } else {
                                        Text("⛪ Altar de Angra Virtual", fontSize = 11.sp, color = BlueprintTextSecondary, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(statusLabel, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                                Text(detailsLabel, fontSize = 7.sp, color = BlueprintTextSecondary, textAlign = TextAlign.Center)
                            }
                        }

                        // Invite Guest toggles box
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            border = BorderStroke(0.5.dp, BlueprintGridLine)
                        ) {
                            Column(modifier = Modifier.padding(4.dp)) {
                                Text("Enviar Convite aos Mestres NPCs:", fontSize = 7.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                                
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    maxItemsInEachRow = 3,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    initialGuests.forEach { guest ->
                                        val invited = weddingGuestsEnabled.getOrDefault(guest, false)
                                        Box(
                                            modifier = Modifier
                                                .padding(vertical = 2.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(if (invited) BlueprintTeal.copy(alpha = 0.2f) else Color.DarkGray)
                                                .border(0.5.dp, if (invited) BlueprintTeal else Color.LightGray, RoundedCornerShape(3.dp))
                                                .clickable { weddingGuestsEnabled.put(guest, !invited) }
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (invited) "✓ $guest" else "✉ $guest",
                                                fontSize = 6.5.sp,
                                                color = if (invited) BlueprintTeal else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Trigger Ceremony action button with sequential delays síncronos
                        Button(
                            onClick = {
                                scope.launch {
                                    ceremonyStage = 1
                                    liveTelemetrySocialLog = "🔔 Cerimônia iniciada: Noivos marchando rumo ao altar de Angra..."
                                    delay(2000)
                                    
                                    ceremonyStage = 2
                                    liveTelemetrySocialLog = "📜 Mestre Robson recita as filosofias da alavanca conjugal e do respeito mútuo."
                                    delay(2050)
                                    
                                    ceremonyStage = 3
                                    liveTelemetrySocialLog = "💍 Troca de alianças táticas realizada! Anel de casamento acoplado."
                                    delay(2000)
                                    
                                    ceremonyStage = 4
                                    liveTelemetrySocialLog = "🎉 Parabéns! Casados oficializados sob a benção dos Dojos síncronos da Baía."
                                    delay(1800)
                                    
                                    ceremonyStage = 5
                                    coupleTitleMessage = "Casado com $weddingPartnerName"
                                    showMarriedBadge = true
                                    liveTelemetrySocialLog = "🏆 Matrimônio síncrono registrado com sucesso no banco relacional JiuVerse!"
                                }
                            },
                            enabled = !isCeremonyRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal, contentColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Text(
                                text = if (isCeremonyRunning) "CEREMÔNIA EM ANDAMENTO" else "INICIAR CASAMENTO VIRTUAL",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // SECTION 3: ANTI-ABUSE & SPAM MITIGATION RADAR (ANTI-FRAUD SETTINGS)
        // =========================================================================
        Text(
            text = "III. RADAR E MECANISMOS DE ANTISPAM, FILTRAGEM E ANTIABUSO (SOCIAL HEALTH)",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛡️ PAINEL DO MODERADOR TÁTICO JIUVERSE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintOrange
                    )
                    Badge(containerColor = if (messagesSentCount > 5) Color.Red else BlueprintTeal, contentColor = Color.White) {
                        Text("Taxa de Conversação: $messagesSentCount m/s", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "Ajuste os filtros de mensagens e regras heurísticas síncronas para demover hackers, bots de propaganda criminosa, ou comportamento ofensivo de lutadores no lobby principal do chat.",
                    fontSize = 9.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Controls Column: Slider and Toggles
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Rate limiter limit
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Limite de Anti-Flood:", fontSize = 8.sp, color = Color.White)
                                Text("${antiSpamThresholdMs.toInt()} ms", fontSize = 8.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = antiSpamThresholdMs,
                                onValueChange = { antiSpamThresholdMs = it },
                                valueRange = 100f..2000f,
                                colors = SliderDefaults.colors(
                                    thumbColor = BlueprintCyan,
                                    activeTrackColor = BlueprintCyan,
                                    inactiveTrackColor = Color.DarkGray
                                )
                            )
                            Text("Bloqueia reenvio sob esta velocidade.", fontSize = 7.sp, color = BlueprintTextSecondary)
                        }

                        // Profanity filter switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Filtro Ativo contra Ofensas", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Mascarar xingamentos e palavras proibidas", fontSize = 7.sp, color = BlueprintTextSecondary)
                            }
                            Switch(
                                checked = isProfanityFilterEnabled,
                                onCheckedChange = { isProfanityFilterEnabled = it },
                                modifier = Modifier.scale(0.7f),
                                colors = SwitchDefaults.colors(checkedThumbColor = BlueprintCyan)
                            )
                        }

                        // Captcha verification flag
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Simular Verificação CAPTCHA", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Exigir a validação para envios sequenciais", fontSize = 7.sp, color = BlueprintTextSecondary)
                            }
                            Switch(
                                checked = isCaptchaVerified,
                                onCheckedChange = { isCaptchaVerified = it },
                                modifier = Modifier.scale(0.7f),
                                colors = SwitchDefaults.colors(checkedThumbColor = BlueprintOrange)
                            )
                        }
                    }

                    // Test Sandbox Column (Interactive messaging to evaluate filters)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "💬 TESTAR FILTRO DE ASSÉDIO / FLOOD SÍNCRONO",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintCyan
                        )
                        
                        OutlinedTextField(
                            value = textMessagePayloadInput,
                            onValueChange = { textMessagePayloadInput = it },
                            placeholder = { Text("Digite conteúdo para testar no chat...", fontSize = 8.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.sp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Run analysis triggers
                            Button(
                                onClick = {
                                    val cleaned = if (isProfanityFilterEnabled) {
                                        var text = textMessagePayloadInput
                                        val swears = listOf("perdedor", "nojento", "merda", "trapaceiro")
                                        swears.forEach { s ->
                                            text = text.replace(s, "*****", ignoreCase = true)
                                        }
                                        text
                                    } else {
                                        textMessagePayloadInput
                                    }
                                    
                                    messagesSentCount++
                                    
                                    liveTelemetrySocialLog = if (messagesSentCount > 3 && antiSpamThresholdMs > 1000f) {
                                        "🛑 SPAM DETECTADO! Conexão de chat silenciada temporariamente por flood."
                                    } else {
                                        "MENSAGEM ENVIADA: \"$cleaned\" (Passe Heurístico Síncrono ✓)"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan, contentColor = Color.Black),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Simular Post", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }

                            // Manual Reset Block rules
                            Button(
                                onClick = {
                                    messagesSentCount = 0
                                    textMessagePayloadInput = "Olá, vamos agendar um treino de lapela limpo hoje?"
                                    liveTelemetrySocialLog = "Contadores de flood reiniciados pelo moderador."
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Limpar Radar", fontSize = 8.sp)
                            }
                        }

                        // Code implementation pattern info
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "💡 Heurística: se mensagens enviadas forem menores que o Delay de Flood (${antiSpamThresholdMs.toInt()}ms), marcamos o pacote de chat como spam preventivo.",
                                fontSize = 7.5.sp,
                                color = Color.Yellow,
                                lineHeight = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // SECTION 4: SENIOR ARCHITECT: DATABASE PERSISTENCE CODE SPECS
        // =========================================================================
        Text(
            text = "IV. BLUEPRINTS SÊNIOR: ESPECIFICAÇÕES DE SCHEMAS ROOM DATABASE PARA TABELAS SOCIAIS",
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
                    Text("1. MODELAGEM DE BANCO DE DADOS DE TABELAS SOCIAIS E CASAMENTOS (SQLITE/ROOM SCHEMA)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                val socialDbSchemaCode = """
-- Tabela de relacionamento de amizade, melhores amigos e parceiros
CREATE TABLE jiuverse_friendships (
    player_id TEXT NOT NULL,
    friend_id TEXT NOT NULL,
    relationship_type TEXT DEFAULT 'FRIEND', -- 'FRIEND', 'BEST_FRIEND', 'PARTNER', 'FOLLOWER'
    mutual_following INTEGER DEFAULT 1,     -- 1: Amas as partes seguem, 0: Apenas um segue
    is_silenced INTEGER DEFAULT 0,          -- Flag de mute preventivo
    is_blacklisted INTEGER DEFAULT 0,       -- Flag de blocklist completo
    established_at INTEGER NOT NULL,        -- Timestamp de criação para ordenação
    conversations_count INTEGER DEFAULT 0,
    PRIMARY KEY (player_id, friend_id),
    FOREIGN KEY(player_id) REFERENCES players(id) ON DELETE CASCADE
);

-- Tabela de Casamentos Virtuais e Unificação de Contas Conjugais
CREATE TABLE jiuverse_virtual_marriages (
    marriage_id TEXT PRIMARY KEY NOT NULL,  -- UUID próprio da união
    spouse_a_id TEXT NOT NULL,              -- Conta do Jogador A
    spouse_b_id TEXT NOT NULL,              -- Conta do Jogador B
    tactical_ring_id TEXT NOT NULL,         -- ID da Aliança Virtual equipada (Stamina/Evasion Up)
    shared_house_id TEXT,                    -- ID da Casa compartilhada comprada
    dedicated_title_id TEXT,                 -- ID do título de honra conjugal activo
    ceremony_timestamp INTEGER NOT NULL,     -- Dia da unificação relacional
    coop_xp_multiplier REAL DEFAULT 1.15,   -- Multiplicador herdeiro síncrono
    is_active INTEGER DEFAULT 1,            -- Flag de divórcio / dissolução legal
    FOREIGN KEY(spouse_a_id) REFERENCES players(id),
    FOREIGN KEY(spouse_b_id) REFERENCES players(id)
);

-- Tabela de Registro de Convites de Casamento Pendentes
CREATE TABLE marriage_ceremony_invitations (
    invitation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    marriage_id TEXT NOT NULL,
    guest_id TEXT NOT NULL,                 -- Jogador convidado ou ID de mestre NPC
    rsvp_status TEXT DEFAULT 'PENDING',     -- 'PENDING', 'ACCEPTED', 'DECLINED'
    gifts_sent_credits INTEGER DEFAULT 0,
    FOREIGN KEY(marriage_id) REFERENCES jiuverse_virtual_marriages(marriage_id) ON DELETE CASCADE
);
                """.trimIndent()

                CodeBlock(code = socialDbSchemaCode, title = "JiuVerse Composable Social & Marriage Database Schema Definition")
            }
        }

        // 2. Anti-Spam room logger tables
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("2. MODELAGEM ANTISPAM E LOGS DE MODERAÇÃO DE ATLETAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                val antiSpamDbSchema = """
-- Tabela de controle de taxa de reenvio de chat por IP/Conta (Rate Limiter)
CREATE TABLE player_rate_limiter_stats (
    player_id TEXT PRIMARY KEY NOT NULL,
    last_message_at_timestamp INTEGER,       -- Último momento síncrono de chat
    violations_count INTEGER DEFAULT 0,       -- Número de floods detectados sequencialmente
    is_currently_muted INTEGER DEFAULT 0,     -- Flag booleana de punição activa
    mute_expiration_timestamp INTEGER,       -- Tempo para liberação da punição
    shadow_banned INTEGER DEFAULT 0          -- Flag booleana para shadowban de assediadores
);

-- Tabela de filtragem de ofensas e palavras proibidas editável pelo Admin
CREATE TABLE dojo_banned_words_dictionary (
    word_id INTEGER PRIMARY KEY AUTOINCREMENT,
    forbidden_word TEXT NOT NULL UNIQUE,     -- Ex: 'perdedor', 'merda', 'trapaceiro'
    severity_rank INTEGER DEFAULT 1,          -- Nível de punição associado
    category TEXT DEFAULT 'HARASSMENT'       -- 'HARASSMENT', 'BOT_SPAM', 'SCAM_LINK'
);
                """.trimIndent()

                CodeBlock(code = antiSpamDbSchema, title = "JiuVerse Anti-Abuse Rate Limiter & Swear Words Database")
            }
        }
    }
}


