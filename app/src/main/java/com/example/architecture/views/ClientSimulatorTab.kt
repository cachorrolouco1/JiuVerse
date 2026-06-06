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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.BlueprintBg
import com.example.ui.theme.BlueprintCard
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintGridLine
import com.example.ui.theme.BlueprintOrange
import com.example.ui.theme.BlueprintTeal
import com.example.ui.theme.BlueprintTextPrimary
import com.example.ui.theme.BlueprintTextSecondary

// State Models for Simulation
data class SimFriend(val id: String, val name: String, val belt: String, val status: String)
data class SimItem(val id: String, val name: String, val type: String, val rarity: String, val price: Int, var equipped: Boolean)
data class SimRank(val pos: Int, val name: String, val belt: String, val xp: Int)
data class SimMsg(
    val sender: String,
    val content: String,
    val time: String,
    val isMe: Boolean,
    val channel: String = "LOCAL",
    val recipient: String? = null
)

@Composable
fun ClientSimulatorTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Collect Room Live SQLite User States
    val playerMemoryState by viewModel.playerMemory.collectAsState()
    val playerMemory = playerMemoryState ?: com.example.architecture.database.PlayerMemoryEntity()
    val studentsState by viewModel.allStudentsState.collectAsState()

    // Simulator-level controls
    var simDarkMode by remember { mutableStateOf(true) }
    var simIsLoggedIn by remember { mutableStateOf(true) }
    var simScreen by remember { mutableStateOf("home") } // login, register, home, profile, inventory, ranking, friends, messages

    // Synced User States from the Database
    var userEmail by remember { mutableStateOf("mestre.oss@jiuverse.com") }
    var userNickname by remember { mutableStateOf(playerMemory.playerName) }
    var userBelt by remember { mutableStateOf(playerMemory.playerBelt) }
    var userXp by remember { mutableStateOf(playerMemory.playerXp) }
    var userLevel by remember { mutableStateOf(playerMemory.playerLevel) }
    var userAcademy by remember { mutableStateOf(playerMemory.academyName) }
    var userCoins by remember { mutableStateOf(850) }

    androidx.compose.runtime.LaunchedEffect(playerMemory) {
        userNickname = playerMemory.playerName
        userBelt = playerMemory.playerBelt
        userXp = playerMemory.playerXp
        userLevel = playerMemory.playerLevel
        userAcademy = playerMemory.academyName
    }

    // Format all other registered users in the database to JSON
    val systemUsersJson = remember(studentsState) {
        val arr = org.json.JSONArray()
        studentsState.forEach { student ->
            arr.put(org.json.JSONObject().apply {
                put("id", "student_${student.studentId}")
                put("name", student.virtualNickname.ifEmpty { student.name })
                put("belt", student.belt)
                put("level", (10..35).random())
                put("xp", (300..1200).random())
                put("academy", when (student.academyId) {
                    1 -> "Alliance Itaim Bibi"
                    2 -> "Gracie Barra Rio Central"
                    3 -> "Checkmat Pinheiros"
                    else -> "JiuVerse Academy"
                })
            })
        }
        arr.toString()
    }

    // Position Coordinates for simulated Dojo 2.5D Real-time grid
    var playerX by remember { mutableStateOf(7) }
    var playerY by remember { mutableStateOf(9) }

    // Dynamic Lists simulating database and stores in React Native
    val simFriends = remember {
        mutableStateListOf(
            SimFriend("1", "GuardaFechada99", "Roxa", "ONLINE"),
            SimFriend("2", "LeandroLoFan", "Preta", "TREINANDO"),
            SimFriend("3", "FaixaAzulPrestativo", "Azul", "OFFLINE")
        )
    }

    val simInventory = remember {
        mutableStateListOf(
            SimItem("1", "Kimono Estilo Gracie XL", "WEARABLE", "EPICO", 450, true),
            SimItem("2", "Faixa Preta Premium", "WEARABLE", "LENDARIO", 1500, false),
            SimItem("3", "Tatame Dojo Vulcânico", "FURNITURE", "RARO", 220, false),
            SimItem("4", "Placa Campeão do ADCC", "BADGE", "LENDARIO", 800, true),
            SimItem("5", "Protetor Auricular Keiko", "WEARABLE", "COMUM", 80, false)
        )
    }

    val simRankings = remember {
        listOf(
            SimRank(1, "RogerGracie10", "Preta", 145000),
            SimRank(2, "BuchechaHeavy", "Preta", 112000),
            SimRank(3, "MiyaoBrothers", "Preta", 98000),
            SimRank(4, "OssMaster", "Azul", 35200),
            SimRank(5, "ChaveDeTarrafe", "Marrom", 21000)
        )
    }

    val simMessages = remember {
        mutableStateListOf(
            SimMsg("Mestre Cícero", "Seja bem-vindo ao Dojo JiuVerse! Aperte o Direcional para andar contra o Socket.IO.", "15:30", false),
            SimMsg("GuardaFechada99", "Alguém para rolar de kimono azul hoje no tatame 4? OSS!!", "15:32", false)
        )
    }

    // Local chat system variables
    val mutedUsers = remember { mutableStateListOf<String>() }
    val lastSentTimeState = remember { mutableStateOf(0L) }
    val lastSentContentState = remember { mutableStateOf("") }
    val webViewRefState = remember { mutableStateOf<WebView?>(null) }

    // Interactive theme mapping inside simulated mobile phone
    val phoneBg = if (simDarkMode) Color(0xFF090D16) else Color(0xFFF1F5F9)
    val phoneCard = if (simDarkMode) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val phoneBorder = if (simDarkMode) Color(0xFF1E293B) else Color(0xFFCBD5E1)
    val phoneTextPrimary = if (simDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val phoneTextSecondary = if (simDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)
    val phoneInputBg = if (simDarkMode) Color(0xFF020617) else Color(0xFFE2E8F0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "JiuVerse Mobile Client",
            subtitle = "Simulador interativo de alta-felicidade com navegação completa, Zustand e Sockets."
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Simule o ecossistema e navegue pelas 8 telas solicitadas do React Native ao vivo na moldura móvel abaixo:",
                fontSize = 12.sp,
                color = BlueprintTextSecondary,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // DarkMode switch inside simulation
            Box(
                modifier = Modifier
                    .background(BlueprintCard, RoundedCornerShape(8.dp))
                    .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp))
                    .clickable { simDarkMode = !simDarkMode }
                    .padding(8.dp)
                    .testTag("sim_toggle_mode")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (simDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Sim Mode",
                        tint = BlueprintCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (simDarkMode) "Modo Claro" else "Modo Escuro",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- High Fidelity Mobile Device Frame Wrapper ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .height(680.dp)
                    .background(Color(0xFF030712), RoundedCornerShape(32.dp))
                    .border(6.dp, Color(0xFF1F2937), RoundedCornerShape(32.dp))
                    .border(1.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                    .padding(12.dp)
            ) {
                // Outer hardware-like notches and details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(phoneBg, RoundedCornerShape(22.dp))
                        .border(1.dp, phoneBorder, RoundedCornerShape(22.dp))
                        .padding(top = 8.dp)
                ) {
                    // Smart notch speaker bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(5.dp)
                                .background(Color(0xFF374151), RoundedCornerShape(2.5.dp))
                        )
                    }

                    // Simulated App status bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "15:35",
                            color = phoneTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📶 📡 🔋 98%",
                                color = phoneTextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Simulated App bar header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (simDarkMode) Color(0xFF0F172A) else Color(0xFFE2E8F0))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF14B8A6), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "JIUVERSE APP v1.0.0",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (simDarkMode) Color(0xFF06B6D4) else Color(0xFF0891B2),
                                letterSpacing = 0.5.sp
                            )
                        }

                        if (simIsLoggedIn) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF16A34A).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, Color(0xFF16A34A), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "CONECTADO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFDC2626).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, Color(0xFFDC2626), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OFFLINE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    // --- SCREEN DISPATCHER ---
                    Box(modifier = Modifier.weight(1f)) {
                        when (simScreen) {
                            "login" -> SimLoginScreen(
                                phoneCard = phoneCard,
                                phoneTextPrimary = phoneTextPrimary,
                                phoneTextSecondary = phoneTextSecondary,
                                phoneInputBg = phoneInputBg,
                                onLogin = { email, pass ->
                                    userEmail = email
                                    simIsLoggedIn = true
                                    simScreen = "home"
                                },
                                onGoRegister = { simScreen = "register" }
                            )
                            "register" -> SimRegisterScreen(
                                phoneCard = phoneCard,
                                phoneTextPrimary = phoneTextPrimary,
                                phoneTextSecondary = phoneTextSecondary,
                                phoneInputBg = phoneInputBg,
                                onRegister = { nickname, email ->
                                    userNickname = nickname
                                    userEmail = email
                                    simIsLoggedIn = true
                                    simScreen = "home"
                                },
                                onGoLogin = { simScreen = "login" }
                            )
                            "home" -> SimHomeScreen(
                                phoneCard = phoneCard,
                                phoneTextPrimary = phoneTextPrimary,
                                phoneTextSecondary = phoneTextSecondary,
                                phoneInputBg = phoneInputBg,
                                phoneBorder = phoneBorder,
                                playerX = playerX,
                                playerY = playerY,
                                userNickname = userNickname,
                                userBelt = userBelt,
                                userLevel = userLevel,
                                userXp = userXp,
                                userAcademy = userAcademy,
                                systemUsersJson = systemUsersJson,
                                simMessages = simMessages,
                                mutedUsers = mutedUsers,
                                simInventory = simInventory,
                                onEquip = { item ->
                                    val idx = simInventory.indexOf(item)
                                    if (idx != -1) {
                                        simInventory[idx] = item.copy(equipped = !item.equipped)
                                    }
                                },
                                onBeltChange = { newBelt ->
                                    userBelt = newBelt
                                    viewModel.savePlayerMemory(playerMemory.copy(playerBelt = newBelt))
                                },
                                onMove = { dx, dy ->
                                    playerX = (playerX + dx).coerceIn(0, 14)
                                    playerY = (playerY + dy).coerceIn(0, 14)
                                },
                                onCoordsChange = { x, y ->
                                    playerX = x
                                    playerY = y
                                },
                                onWebViewCreated = { webViewRefState.value = it }
                            )
                            "inventory" -> SimInventoryScreen(
                                phoneCard = phoneCard,
                                phoneTextPrimary = phoneTextPrimary,
                                phoneTextSecondary = phoneTextSecondary,
                                items = simInventory,
                                onEquip = { item ->
                                    val idx = simInventory.indexOf(item)
                                    if (idx != -1) {
                                        simInventory[idx] = item.copy(equipped = !item.equipped)
                                    }
                                },
                                userBelt = userBelt,
                                onBeltChange = { newBelt ->
                                    userBelt = newBelt
                                    viewModel.savePlayerMemory(playerMemory.copy(playerBelt = newBelt))
                                },
                                userLevel = userLevel,
                                userXp = userXp,
                                userAcademy = userAcademy,
                                mutedCount = mutedUsers.size,
                                chatMessageCount = simMessages.filter { it.isMe }.size,
                                userNickname = userNickname
                            )
                            "ranking" -> SimRankingScreen(
                                phoneCard = phoneCard,
                                phoneTextPrimary = phoneTextPrimary,
                                phoneTextSecondary = phoneTextSecondary,
                                rankings = simRankings
                            )
                            "friends" -> SimFriendsScreen(
                                phoneCard = phoneCard,
                                phoneTextPrimary = phoneTextPrimary,
                                phoneTextSecondary = phoneTextSecondary,
                                phoneInputBg = phoneInputBg,
                                friends = simFriends,
                                onAdd = { name ->
                                    simFriends.add(SimFriend((simFriends.size + 1).toString(), name, "Branca", "ONLINE"))
                                },
                                onRemove = { idx ->
                                    simFriends.removeAt(idx)
                                }
                            )
                            "messages" -> SimMessagesScreen(
                                phoneCard = phoneCard,
                                phoneTextPrimary = phoneTextPrimary,
                                phoneTextSecondary = phoneTextSecondary,
                                phoneInputBg = phoneInputBg,
                                msgs = simMessages,
                                mutedUsers = mutedUsers,
                                onSend = { text, channel, recipientName ->
                                    val now = System.currentTimeMillis()
                                    val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                                    val cleanText = text.trim()

                                    // --- 1. ANTI-FLOOD CHECK ---
                                    if (now - lastSentTimeState.value < 1500) {
                                        simMessages.add(
                                            SimMsg(
                                                sender = "SISTEMA",
                                                content = "⚠️ ANTI-FLOOD: Aguarde 1.5s antes de enviar nova mensagem.",
                                                time = timeStr,
                                                isMe = false,
                                                channel = "LOCAL"
                                            )
                                        )
                                        return@SimMessagesScreen
                                    }

                                    // --- 2. ANTI-SPAM CHECK ---
                                    if (cleanText.isEmpty()) return@SimMessagesScreen
                                    if (cleanText == lastSentContentState.value) {
                                        simMessages.add(
                                            SimMsg(
                                                sender = "SISTEMA",
                                                content = "⚠️ ANTI-SPAM: Não envie mensagens idênticas consecutivas.",
                                                time = timeStr,
                                                isMe = false,
                                                channel = "LOCAL"
                                            )
                                        )
                                        return@SimMessagesScreen
                                    }

                                    // --- 3. COMMANDS PARSING (/mute /unmute) ---
                                    if (cleanText.startsWith("/mute ")) {
                                        val targetMute = cleanText.removePrefix("/mute ").trim()
                                        if (targetMute.isNotEmpty() && !mutedUsers.contains(targetMute)) {
                                            mutedUsers.add(targetMute)
                                            val mutedJson = "['" + mutedUsers.joinToString("','") + "']"
                                            webViewRefState.value?.evaluateJavascript("javascript:if (window.updateMutedUsers) { window.updateMutedUsers($mutedJson); }", null)
                                            simMessages.add(
                                                SimMsg(
                                                    sender = "SISTEMA",
                                                    content = "🔇 Usuário '$targetMute' silenciado com sucesso.",
                                                    time = timeStr,
                                                    isMe = false,
                                                    channel = "LOCAL"
                                                )
                                            )
                                        }
                                        return@SimMessagesScreen
                                    }
                                    if (cleanText.startsWith("/unmute ")) {
                                        val targetMute = cleanText.removePrefix("/unmute ").trim()
                                        if (targetMute.isNotEmpty()) {
                                            mutedUsers.remove(targetMute)
                                            val mutedJson = "['" + mutedUsers.joinToString("','") + "']"
                                            webViewRefState.value?.evaluateJavascript("javascript:if (window.updateMutedUsers) { window.updateMutedUsers($mutedJson); }", null)
                                            simMessages.add(
                                                SimMsg(
                                                    sender = "SISTEMA",
                                                    content = "🔊 Usuário '$targetMute' foi desmutado.",
                                                    time = timeStr,
                                                    isMe = false,
                                                    channel = "LOCAL"
                                                )
                                            )
                                        }
                                        return@SimMessagesScreen
                                    }

                                    // Update flood/spam limits
                                    lastSentTimeState.value = now
                                    lastSentContentState.value = cleanText

                                    // Add local log message
                                    simMessages.add(
                                        SimMsg(
                                            sender = userNickname.ifEmpty { "Guerreiro" },
                                            content = cleanText,
                                            time = timeStr,
                                            isMe = true,
                                            channel = channel,
                                            recipient = recipientName
                                        )
                                    )

                                    // --- 4. EVAL BRIDGE IN PHASER WEBVIEW ---
                                    val safeContent = cleanText.replace("'", "\\'")
                                    webViewRefState.value?.evaluateJavascript(
                                        "javascript:if (window.sendLocalChatMessage) { window.sendLocalChatMessage('" + 
                                        (userNickname.ifEmpty { "Guerreiro" }) + "', '" + 
                                        safeContent + "', '$channel', " + 
                                        (if (recipientName != null) "'$recipientName'" else "null") + "); }", 
                                        null
                                    )

                                    // Fast response triggers
                                    if (cleanText.uppercase().contains("OSS") || cleanText.uppercase().contains("OLÁ")) {
                                        simMessages.add(
                                            SimMsg(
                                                sender = "Mestre Cícero", 
                                                content = "Foco no quadril e postura no tatame. Oss!", 
                                                time = timeStr, 
                                                isMe = false,
                                                channel = channel
                                            )
                                        )
                                    }
                                },
                                onMuteToggle = { userToMute ->
                                    val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                                    if (mutedUsers.contains(userToMute)) {
                                        mutedUsers.remove(userToMute)
                                    } else {
                                        mutedUsers.add(userToMute)
                                    }
                                    val mutedJson = "['" + mutedUsers.joinToString("','") + "']"
                                    webViewRefState.value?.evaluateJavascript("javascript:if (window.updateMutedUsers) { window.updateMutedUsers($mutedJson); }", null)
                                    val isNowMuted = mutedUsers.contains(userToMute)
                                    simMessages.add(
                                        SimMsg(
                                            sender = "SISTEMA",
                                            content = "Usuário '$userToMute' foi " + (if (isNowMuted) "silenciado 🔇" else "desmutado 🔊") + ".",
                                            time = timeStr,
                                            isMe = false,
                                            channel = "LOCAL"
                                        )
                                    )
                                }
                            )
                            "profile" -> SimProfileScreen(
                                phoneCard = phoneCard,
                                phoneTextPrimary = phoneTextPrimary,
                                phoneTextSecondary = phoneTextSecondary,
                                nickname = userNickname,
                                email = userEmail,
                                belt = userBelt,
                                xp = userXp,
                                coins = userCoins,
                                onLogout = {
                                    simIsLoggedIn = false
                                    simScreen = "login"
                                }
                            )
                        }
                    }

                    // --- NAVIGATION BOTTOM TAB BAR ---
                    if (simIsLoggedIn) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (simDarkMode) Color(0xFF0F172A) else Color(0xFFE2E8F0))
                                .border(width = 1.dp, color = phoneBorder)
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val tabsList = listOf(
                                "home" to "🥋 Tatame",
                                "inventory" to "🎒 Bolsa",
                                "ranking" to "🥇 Rank",
                                "friends" to "🤝 Social",
                                "messages" to "💬 Chat",
                                "profile" to "👤 Avatar"
                            )

                            tabsList.forEach { (route, label) ->
                                val isSelected = simScreen == route
                                Column(
                                    modifier = Modifier
                                        .clickable { simScreen = route }
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label.split(" ")[0],
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = label.split(" ")[1],
                                        fontSize = 8.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF06B6D4) else phoneTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Simulated Mobile Home Indicator bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(4.dp)
                                .background(phoneTextSecondary.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN IMPLEMENTATIONS FOR CLIENT SIMULATOR ---

@Composable
fun SimLoginScreen(
    phoneCard: Color,
    phoneTextPrimary: Color,
    phoneTextSecondary: Color,
    phoneInputBg: Color,
    onLogin: (String, String) -> Unit,
    onGoRegister: () -> Unit
) {
    var email by remember { mutableStateOf("lutador@jiuverse.com") }
    var password by remember { mutableStateOf("mestre123") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🥋 JIUVERSE", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF06B6D4))
        Text("MMORPG SOCIAL DE ARTES MARCIAIS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14B8A6))

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = phoneCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("EMAIL DO LUTADOR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                SimTextInput(value = email, onValueChange = { email = it }, bg = phoneInputBg, txtColor = phoneTextPrimary, holder = "Email")

                Spacer(modifier = Modifier.height(10.dp))

                Text("SENHA DO DOJO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                SimTextInput(value = password, onValueChange = { password = it }, bg = phoneInputBg, txtColor = phoneTextPrimary, holder = "Senha", isPassword = true)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onLogin(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6))
                ) {
                    Text("ESTABELECER CONEXÃO", fontSize = 11.sp, color = Color(0xFF030712), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Não tem conta? Inscreva-se na Academia",
                    fontSize = 11.sp,
                    color = Color(0xFF06B6D4),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGoRegister() }
                )
            }
        }
    }
}

@Composable
fun SimRegisterScreen(
    phoneCard: Color,
    phoneTextPrimary: Color,
    phoneTextSecondary: Color,
    phoneInputBg: Color,
    onRegister: (String, String) -> Unit,
    onGoLogin: () -> Unit
) {
    var nick by remember { mutableStateOf("GracieBoy") }
    var email by remember { mutableStateOf("luta@dojo.com") }
    var pass by remember { mutableStateOf("pass123") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🥋 JIUVERSE", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF06B6D4))
        Text("REGISTRE SEU AVATAR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14B8A6))

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = phoneCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("NICKNAME DO PERSONAGEM (ÚNICO)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                SimTextInput(value = nick, onValueChange = { nick = it }, bg = phoneInputBg, txtColor = phoneTextPrimary, holder = "Nickname")

                Spacer(modifier = Modifier.height(10.dp))

                Text("EMAIL DE REGISTRO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                SimTextInput(value = email, onValueChange = { email = it }, bg = phoneInputBg, txtColor = phoneTextPrimary, holder = "Email", keyboardType = KeyboardOptions(keyboardType = KeyboardType.Email))

                Spacer(modifier = Modifier.height(10.dp))

                Text("SENHA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                SimTextInput(value = pass, onValueChange = { pass = it }, bg = phoneInputBg, txtColor = phoneTextPrimary, holder = "Senha", isPassword = true)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onRegister(nick, email) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                ) {
                    Text("GRADUAR E CONECTAR", fontSize = 11.sp, color = Color(0xFF030712), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Já possui cadastro? Clique aqui para Login",
                    fontSize = 11.sp,
                    color = Color(0xFF06B6D4),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGoLogin() }
                )
            }
        }
    }
}

@Composable
fun SimHomeScreen(
    phoneCard: Color,
    phoneTextPrimary: Color,
    phoneTextSecondary: Color,
    phoneInputBg: Color,
    phoneBorder: Color,
    playerX: Int,
    playerY: Int,
    userNickname: String,
    userBelt: String,
    userLevel: Int,
    userXp: Int,
    userAcademy: String,
    systemUsersJson: String,
    simMessages: MutableList<SimMsg>,
    mutedUsers: List<String>,
    simInventory: List<SimItem>,
    onEquip: (SimItem) -> Unit,
    onBeltChange: (String) -> Unit,
    onMove: (Int, Int) -> Unit,
    onCoordsChange: (Int, Int) -> Unit,
    onWebViewCreated: (WebView) -> Unit
) {
    var selectedEnvironment by remember { mutableStateOf("Recepção") }
    var proxFilter by remember { mutableStateOf(true) }
    var useWebPhaser by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMmorpgInventoryOpen by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.I) {
                    isMmorpgInventoryOpen = !isMmorpgInventoryOpen
                    true
                } else {
                    false
                }
            }
    ) {
        // Network Status Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(phoneCard, RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (useWebPhaser) "PHASER 3 ENGINE (ATIVO)" else "TATAME ISOMÉTRICO (SIMULADO)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF06B6D4)
                )
                Text("Ambiente: $selectedEnvironment • ($playerX, $playerY)", fontSize = 8.sp, color = phoneTextSecondary)
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFF0F766E).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0xFF14B8A6), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (useWebPhaser) "WEBVIEW" else "60 FPS",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2DD4BF)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Dynamic Engine Selector (Aesthetic design tab row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (!useWebPhaser) Color(0xFF1E293B) else Color.Transparent, RoundedCornerShape(4.dp))
                    .clickable { useWebPhaser = false }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🕹️ SIM COMPOSER",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!useWebPhaser) Color(0xFF14B8A6) else phoneTextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (useWebPhaser) Color(0xFF1E293B) else Color.Transparent, RoundedCornerShape(4.dp))
                    .clickable { useWebPhaser = true }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎮 PHASER 3 LIVE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (useWebPhaser) Color(0xFF14B8A6) else phoneTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Socket.IO Server Configuration Panel
        var socketUrlInput by remember { mutableStateOf("http://10.0.2.2:3000") }
        var isConnecting by remember { mutableStateOf(false) }
        var socketStatus by remember { mutableStateOf("VIRTUAL (SIMULATED)") } // ON, OFF, VIRTUAL

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(phoneCard, RoundedCornerShape(8.dp))
                .border(0.5.dp, phoneBorder, RoundedCornerShape(8.dp))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SOCKET.IO IP SERVER (" + socketStatus + ")",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF06B6D4)
                )
                androidx.compose.foundation.text.BasicTextField(
                    value = socketUrlInput,
                    onValueChange = { socketUrlInput = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = phoneTextPrimary, fontSize = 8.sp, fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(phoneInputBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF14B8A6))
                )
            }
            Button(
                onClick = {
                    isConnecting = true
                    webViewRef?.evaluateJavascript("javascript:if (window.connectSocket) { window.connectSocket('$socketUrlInput'); }", null)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1e293b)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(28.dp).align(Alignment.Bottom)
            ) {
                Text(if (isConnecting) "SYNC..." else "CONECTAR", fontSize = 7.sp, color = Color(0xFF14B8A6), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Academia Carlson Gracie Environments Selector Tab Grid (Scrollable or elegant compact buttons)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020617), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            Text(
                text = "AMBIENTES DA ACADEMIA CARLSON GRACIE",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE2E8F0),
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )

            // Two rows of 4 buttons for a perfect responsive layout on simulated screens
            val rows = listOf(
                listOf("Recepção", "Tatame Principal", "Área de Aula", "Vestiários"),
                listOf("Hall de Troféus", "Sala do Mestre", "Sala VIP", "Loja da Academia")
            )

            rows.forEach { rowList ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rowList.forEach { env ->
                        val active = selectedEnvironment == env
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (active) Color(0xFF1E293B) else Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                .border(0.5.dp, if (active) Color(0xFF2DD4BF) else Color(0xFF334155), RoundedCornerShape(4.dp))
                                .clickable { selectedEnvironment = env }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = env.uppercase(),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color(0xFF2DD4BF) else Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dynamic 2.5D Isometric Render Canvas Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF020617), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (useWebPhaser) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            
                            addJavascriptInterface(object : Any() {
                                @JavascriptInterface
                                fun postMessage(message: String) {
                                    try {
                                        val json = org.json.JSONObject(message)
                                        val type = json.optString("type")
                                        if (type == "TOGGLE_INVENTORY") {
                                            isMmorpgInventoryOpen = !isMmorpgInventoryOpen
                                        }
                                        if (type == "PLAYER_MOVE") {
                                            val x = json.optInt("x")
                                            val y = json.optInt("y")
                                            onCoordsChange(x, y)
                                        }
                                        if (type == "ENV_CHANGE") {
                                            val envName = json.optString("env")
                                            val x = json.optInt("x")
                                            val y = json.optInt("y")
                                            selectedEnvironment = envName
                                            onCoordsChange(x, y)
                                        }
                                        if (type == "SOCKET_STATUS") {
                                            val status = json.optString("status")
                                            socketStatus = status
                                            isConnecting = false
                                        }
                                        if (type == "CHAT_MESSAGE") {
                                            val sender = json.optString("sender")
                                            val content = json.optString("content")
                                            val chan = json.optString("channel", "LOCAL")
                                            val recip = json.optString("recipient").takeIf { it.isNotEmpty() }
                                            
                                            // Only display if the caller is not muted
                                            if (!mutedUsers.contains(sender)) {
                                                simMessages.add(
                                                    SimMsg(
                                                        sender = sender,
                                                        content = content,
                                                        time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                                                        isMe = false,
                                                        channel = chan,
                                                        recipient = recip
                                                    )
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }, "AndroidWebView")

                            val html = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                                    <style>
                                        body, html { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background-color: #020617; font-family: monospace; }
                                        #game-container { width: 100%; height: 100%; position: absolute; top:0; left:0; }
                                        #diagnostic-hud {
                                            position: absolute; top: 6px; left: 6px; background: rgba(9, 13, 22, 0.95);
                                            border: 1px solid #06b6d4; padding: 6px; border-radius: 4px; color: #f8fafc;
                                            font-size: 7.5px; pointer-events: none; z-index: 100; max-width: 175px;
                                            box-shadow: 0 0 5px rgba(6, 182, 212, 0.3);
                                        }
                                        .hud-line { margin-bottom: 2px; }
                                        #dialogue-box {
                                            position: absolute;
                                            bottom: 12px;
                                            left: 50%;
                                            transform: translateX(-50%);
                                            background: rgba(15, 23, 42, 0.95);
                                            border: 1.5px solid #2dd4bf;
                                            color: #f1f5f9;
                                            padding: 8px 14px;
                                            border-radius: 8px;
                                            font-size: 9px;
                                            max-width: 82%;
                                            width: 250px;
                                            box-shadow: 0 4px 15px rgba(0,0,0,0.8);
                                            display: none;
                                            z-index: 200;
                                            pointer-events: none;
                                            text-align: center;
                                            line-height: 1.25;
                                        }
                                    </style>
                                    <script src="https://cdn.jsdelivr.net/npm/phaser@3.60.0/dist/phaser.min.js"></script>
                                </head>
                                <body>
                                    <div id="diagnostic-hud">
                                        <div class="hud-line" style="color: #2dd4bf; font-weight: bold;">JIUVERSE ENGINE 3D v2.0</div>
                                        <div class="hud-line" style="color: #ea580c; font-weight: bold;">ACADEMIA CARLSON GRACIE</div>
                                        <div class="hud-line" style="color: #ca8a04; font-weight: bold;">MUNDO: <span id="val-mundo">Recepção</span></div>
                                        <div class="hud-line">Coords: <span style="color: #38bdf8;" id="val-coords">($playerX, $playerY)</span></div>
                                        <div class="hud-line">FPS: <span id="val-fps">60</span></div>
                                    </div>
                                    
                                    <div id="dialogue-box">
                                        <div id="dialogue-text">Carregando diálogo...</div>
                                    </div>

                                    <div id="game-container"></div>

                                    <script>
                                        const TILE_WIDTH = 44;

                                         // Loaded dynamic users from database & local player
                                         const DB_STUDENTS = $systemUsersJson;
                                         const MY_PROFILE = {
                                             name: "$userNickname",
                                             belt: "$userBelt",
                                             level: $userLevel,
                                             xp: $userXp,
                                             academy: "$userAcademy"
                                         };

                                         // Helper to get hex colors based on Belt/Academy
                                         function getBeltColorHex(belt) {
                                             const b = (belt || "").toLowerCase();
                                             if (b.includes("branca")) return "#f8fafc";
                                             if (b.includes("azul")) return "#3b82f6";
                                             if (b.includes("roxa")) return "#8b5cf6";
                                             if (b.includes("marrom")) return "#78350f";
                                             if (b.includes("preta")) return "#111827";
                                             if (b.includes("coral")) return "#f97316"; 
                                             if (b.includes("vermelha")) return "#dc2626";
                                             return "#94a3b8";
                                         }

                                         function getKimonoColorHex(academy) {
                                             const a = (academy || "").toLowerCase();
                                             if (a.includes("alliance")) return "#f8fafc"; // White
                                             if (a.includes("gracie")) return "#1d4ed8"; // Royal Blue
                                             if (a.includes("checkmat")) return "#0f172a"; // Dark navy/black
                                             return "#64748b"; // neutral grey
                                         }

                                         // Procedural canvas image generation for avatars
                                         function buildAvatarTexture(scene, key, beltColHex, kimonoColHex) {
                                             if (scene.textures.exists(key)) return;
                                             let canvasObj = scene.textures.createCanvas(key, 32, 48);
                                             let ctx = canvasObj.context;
                                             
                                             // Skin Body Face
                                             ctx.fillStyle = '#fbcfe8';
                                             ctx.beginPath();
                                             ctx.arc(16, 12, 6, 0, Math.PI * 2);
                                             ctx.fill();
                                             
                                             // Hair / bandana
                                             ctx.fillStyle = '#0f172a';
                                             ctx.beginPath();
                                             ctx.arc(16, 9, 6.5, Math.PI, 0);
                                             ctx.fill();
                                             
                                             // Kimono Gi Jacket
                                             ctx.fillStyle = kimonoColHex;
                                             ctx.fillRect(8, 18, 16, 30);
                                             
                                             // Crossed collar lapels decoration
                                             ctx.strokeStyle = '#f8fafc';
                                             ctx.lineWidth = 1;
                                             ctx.beginPath();
                                             ctx.moveTo(8, 18);
                                             ctx.lineTo(16, 26);
                                             ctx.moveTo(24, 18);
                                             ctx.lineTo(16, 26);
                                             ctx.stroke();
                                             
                                             // Rank colored belt
                                             ctx.fillStyle = beltColHex;
                                             ctx.fillRect(8, 29, 16, 4);
                                             
                                             // Black tip / rank bar
                                             ctx.fillStyle = '#111827';
                                             ctx.fillRect(13, 28, 4, 6);
                                             
                                             canvasObj.refresh();
                                         }
                                        const TILE_HEIGHT = 22;
                                        const MAP_OFFSET_X = 330;
                                        const MAP_OFFSET_Y = 100;
                                        const GRID_SIZE = 15;

                                        function cartToIso(x, y) {
                                            return {
                                                x: (x - y) * (TILE_WIDTH / 2) + MAP_OFFSET_X,
                                                y: (x + y) * (TILE_HEIGHT / 2) + MAP_OFFSET_Y
                                            };
                                        }

                                        const TELEPORTS = {
                                            'Recepção': [
                                                { gridX: 7, gridY: 0, destEnv: 'Tatame Principal', destX: 7, destY: 13, label: '⬆ Tatame' },
                                                { gridX: 0, gridY: 7, destEnv: 'Loja da Academia', destX: 13, destY: 7, label: '⬅ Loja' },
                                                { gridX: 7, gridY: 14, destEnv: 'Hall de Troféus', destX: 7, destY: 1, label: '⬇ Troféus' }
                                            ],
                                            'Tatame Principal': [
                                                { gridX: 7, gridY: 14, destEnv: 'Recepção', destX: 7, destY: 1, label: '⬇ Recepção' },
                                                { gridX: 0, gridY: 7, destEnv: 'Área de Aula', destX: 13, destY: 7, label: '⬅ Aulas' },
                                                { gridX: 14, gridY: 7, destEnv: 'Sala do Mestre', destX: 1, destY: 7, label: '➡ S. Mestre' }
                                            ],
                                            'Área de Aula': [
                                                { gridX: 14, gridY: 7, destEnv: 'Tatame Principal', destX: 1, destY: 7, label: '➡ Tatame' },
                                                { gridX: 7, gridY: 14, destEnv: 'Vestiários', destX: 7, destY: 1, label: '⬇ Vestiário' }
                                            ],
                                            'Vestiários': [
                                                { gridX: 7, gridY: 0, destEnv: 'Área de Aula', destX: 7, destY: 13, label: '⬆ Aulas' },
                                                { gridX: 14, gridY: 7, destEnv: 'Sala VIP', destX: 1, destY: 7, label: '➡ Sala VIP' }
                                            ],
                                            'Hall de Troféus': [
                                                { gridX: 7, gridY: 0, destEnv: 'Recepção', destX: 7, destY: 13, label: '⬆ Recepção' },
                                                { gridX: 14, gridY: 7, destEnv: 'Sala VIP', destX: 1, destY: 7, label: '➡ Sala VIP' }
                                            ],
                                            'Sala VIP': [
                                                { gridX: 0, gridY: 7, destEnv: 'Hall de Troféus', destX: 13, destY: 7, label: '⬅ Troféus' },
                                                { gridX: 7, gridY: 0, destEnv: 'Vestiários', destX: 7, destY: 13, label: '⬆ Vestiário' }
                                            ],
                                            'Sala do Mestre': [
                                                { gridX: 0, gridY: 7, destEnv: 'Tatame Principal', destX: 13, destY: 7, label: '⬅ Tatame' }
                                            ],
                                            'Loja da Academia': [
                                                { gridX: 14, gridY: 7, destEnv: 'Recepção', destX: 1, destY: 7, label: '➡ Recepção' }
                                            ]
                                        };

                                        const ENV_DATA = {
                                            'Recepção': {
                                                npcs: [
                                                    { name: 'Sofia (Recepcionista)', belt: 'Azul', gridX: 5, gridY: 4, tint: 0x3b82f6, quote: 'Olá! Bem-vindo à Academia Carlson Gracie JiuVerse. Gostaria de renovar sua mensalidade?' },
                                                    { name: 'Visitante Curioso', belt: 'Branca', gridX: 9, gridY: 8, tint: 0xf8fafc, quote: 'Dizem que o treino aqui forma guerreiros duros desde a faixa branca. Oss!' }
                                                ],
                                                interactables: [
                                                    { name: 'Balcão de Atendimento', sprite: 'interactive_board', gridX: 5, gridY: 5, desc: 'Balcão principal: Cadastre seus treinos e pegue cupons de bônus.' },
                                                    { name: 'Balcão Direito', sprite: 'interactive_board', gridX: 6, gridY: 5, desc: 'Mesa de Cadastro: Um computador rodando o sistema de rankings.' },
                                                    { name: 'Quadro de Avisos', sprite: 'interactive_board', gridX: 3, gridY: 3, desc: 'Seminário Especial: Defesa pessoal urbana e tática de lutas.' }
                                                ]
                                            },
                                            'Tatame Principal': {
                                                npcs: [
                                                    { name: 'Mestre Carlson Gracie', belt: 'Vermelha', gridX: 7, gridY: 5, tint: 0xef4444, quote: 'Se você tem medo de asfixia, não jogue Jiu-Jitsu! Aqui nós não aceitamos moleza. Treine duro!' },
                                                    { name: 'Mestre Cicero Costha', belt: 'Preta', gridX: 3, gridY: 7, tint: 0x374151, quote: 'Mantenha o quadril baixo e ajuste a gola. O segredo está nos detalhes.' }
                                                ],
                                                interactables: [
                                                    { name: 'Estatua do Carlson', sprite: 'entity_statue', gridX: 7, gridY: 6, desc: 'Memorial Carlson Gracie: O criador do Jiu-Jitsu de elite e das lendas.' }
                                                ]
                                            },
                                            'Área de Aula': {
                                                npcs: [
                                                    { name: 'Rickson Gracie', belt: 'Coral', gridX: 7, gridY: 4, tint: 0xf97316, quote: 'O Jiu-Jitsu é perfeito. O invisível é o que realmente define a eficiência da sua defesa.' },
                                                    { name: 'Aluno Casca-Grossa', belt: 'Marrom', gridX: 4, gridY: 8, tint: 0x78350f, quote: 'Treinar meia-guarda profunda repetidamente até se tornar automático.' }
                                                ],
                                                interactables: [
                                                    { name: 'Boneco de Quedas', sprite: 'interactive_dummy', gridX: 3, gridY: 3, desc: 'Boneco de Couro: Ideal para simular quedas clássicas como Osoto Gari.' }
                                                ]
                                            },
                                            'Vestiários': {
                                                npcs: [
                                                    { name: 'Juquinha (Cansado)', belt: 'Branca', gridX: 6, gridY: 8, tint: 0xf8fafc, quote: 'Ufa... acabei de levar 4 finalizações seguidas do faixa azul. Chão pesado!' },
                                                    { name: 'Faixa Roxa Sparring', belt: 'Roxa', gridX: 9, gridY: 5, tint: 0xa78bfa, quote: 'Secando o kimono extra para o próximo período de rolas livres.' }
                                                ],
                                                interactables: [
                                                    { name: 'Armários de Aço', sprite: 'entity_tree', gridX: 3, gridY: 3, desc: 'Armário Locker: Contém kimonos extras e toalhas limpas do JiuVerse.' },
                                                    { name: 'Armários de Madeira', sprite: 'entity_tree', gridX: 4, gridY: 3, desc: 'Banco de descanso e armários decorativos dos atletas.' },
                                                    { name: 'Bebedouro Gelado', sprite: 'interactive_cooler', gridX: 2, gridY: 5, desc: 'Bebedouro de Alta Vazão: Água alcalina refrigerada a 5°C.' }
                                                ]
                                            },
                                            'Hall de Troféus': {
                                                npcs: [
                                                    { name: 'Velha Guarda BJJ', belt: 'Preta', gridX: 5, gridY: 7, tint: 0x374151, quote: 'Estive aqui no mundial de 96. A equipe Carlson dominou tudo!' },
                                                    { name: 'Historiador', belt: 'Azul', gridX: 9, gridY: 6, tint: 0x3b82f6, quote: 'Você sabia que o time de Carlson era conhecido como os Leões?' }
                                                ],
                                                interactables: [
                                                    { name: 'Vitrine Esquerda', sprite: 'interactive_cup', gridX: 4, gridY: 4, desc: 'Troféu Mundial de Equipes (1996) Carlson Gracie Team.' },
                                                    { name: 'Vitrine Direita', sprite: 'interactive_cup', gridX: 10, gridY: 4, desc: 'Troféu Campeão do ADCC por Equipes.' },
                                                    { name: 'Cesta Trophy', sprite: 'interactive_cup', gridX: 7, gridY: 4, desc: 'Copa Histórica Carlson Gracie Jiu-Jitsu Team Cup.' }
                                                ]
                                            },
                                            'Sala do Mestre': {
                                                npcs: [
                                                    { name: 'Carlson Gracie Jr', belt: 'Vermelha Coral', gridX: 7, gridY: 4, tint: 0xf97316, quote: 'Aqui meu pai traçava as estratégias para as maiores batalhas do vale-tudo mundial.' }
                                                ],
                                                interactables: [
                                                    { name: 'Mesa do Mestre', sprite: 'interactive_board', gridX: 7, gridY: 5, desc: 'Escrivaninha Histórica: Contém diários técnicos e fotos de época.' },
                                                    { name: 'Diploma de Honra', sprite: 'interactive_diploma', gridX: 4, gridY: 3, desc: 'Diploma Oficial da CBJJ declarando o 9º Grau Faixa Vermelha de Carlson.' }
                                                ]
                                            },
                                            'Sala VIP': {
                                                npcs: [
                                                    { name: 'Atleta de Elite', belt: 'Preta', gridX: 4, gridY: 5, tint: 0x1e3a8a, quote: 'Estou focado no ADCC absoluto. A recuperação no lounge VIP é premium.' },
                                                    { name: 'Sponsor Corporativo', belt: 'Branca', gridX: 10, gridY: 8, tint: 0xf8fafc, quote: 'Queremos estampar patches da nossa marca em todos os kimonos da academia.' }
                                                ],
                                                interactables: [
                                                    { name: 'Frigobar de Combustível', sprite: 'interactive_fridge', gridX: 2, gridY: 2, desc: 'Geladeira Premium: Garrafas de Açaí orgânico, mel e suplementos desportivos.' }
                                                ]
                                            },
                                            'Loja da Academia': {
                                                npcs: [
                                                    { name: 'Consultor de Vendas', belt: 'Roxa', gridX: 5, gridY: 6, tint: 0xa78bfa, quote: 'Olá! Temos kimonos oficiais Carlson Gracie Team trançados, faixas reforçadas e patches clássicos.' }
                                                ],
                                                interactables: [
                                                    { name: 'Arara de Kimonos', sprite: 'interactive_rack', gridX: 3, gridY: 4, desc: 'Expositor: Kimonos oficiais nas cores azul, branco e preto com bordados lendários.' }
                                                ]
                                            }
                                        };

                                        class JiuVerseIsometricDojo extends Phaser.Scene {
                                            constructor() {
                                                super({ key: 'JiuVerseDojo' });
                                                 this.peers = {}; // Multi-player system users and virtual bots active in current environment
                                                 this.globalPeersData = []; // Multi-player core registry
                                                 this.avatarColorCache = {}; // Prebuilt avatars
                                                this.localPlayer = null;
                                                this.currentEnv = 'Recepção';
                                                this.collisionGrid = [];
                                            }

                                            preload() {
                                                // Ground tile textures with different colors
                                                this.createColorTile('tile_stone', '#1e293b', '#475569');
                                                this.createColorTile('tile_reception_wood', '#78350f', '#92400e');
                                                this.createColorTile('tile_tatame_blue', '#1d4ed8', '#3b82f6');
                                                this.createColorTile('tile_tatame_yellow', '#ca8a04', '#eab308');
                                                this.createColorTile('tile_tatame_green', '#064e3b', '#059669');
                                                this.createColorTile('tile_locker_ceramic', '#334155', '#64748b');
                                                this.createColorTile('tile_trophy_lux', '#090d16', '#ca8a04');
                                                this.createColorTile('tile_mahogany_dark', '#451a03', '#78350f');
                                                this.createColorTile('tile_vip_emerald', '#022c22', '#10b981');
                                                this.createColorTile('tile_shop_tech', '#111827', '#4f46e5');
                                                this.createColorTile('tile_portal', '#1e1b4b', '#a855f7');

                                                // Decorative obstacles
                                                let pillar = this.textures.createCanvas('entity_pillar', 12, 32);
                                                let cPill = pillar.context;
                                                cPill.fillStyle = '#0f172a';
                                                cPill.fillRect(0, 8, 12, 24);
                                                cPill.fillStyle = '#06b6d4';
                                                cPill.fillRect(5, 12, 2, 16);
                                                pillar.refresh();

                                                let statue = this.textures.createCanvas('entity_statue', 36, 52);
                                                let cSt = statue.context;
                                                cSt.fillStyle = '#4b5563'; 
                                                cSt.fillRect(6, 36, 24, 16);
                                                cSt.fillStyle = '#b45309'; 
                                                cSt.fillRect(10, 16, 16, 20);
                                                cSt.beginPath();
                                                cSt.arc(18, 10, 6, 0, Math.PI * 2);
                                                cSt.fill();
                                                statue.refresh();

                                                // Custom preloads for interactable assets
                                                let board = this.textures.createCanvas('interactive_board', 28, 40);
                                                let cB = board.context;
                                                cB.fillStyle = '#451a03';
                                                cB.fillRect(0, 0, 28, 40);
                                                cB.fillStyle = '#0e7490';
                                                cB.fillRect(2, 2, 24, 24);
                                                board.refresh();

                                                let dummy = this.textures.createCanvas('interactive_dummy', 24, 44);
                                                let cD = dummy.context;
                                                cD.fillStyle = '#78350f';
                                                cD.beginPath();
                                                cD.arc(12, 12, 8, 0, Math.PI * 2);
                                                cD.fill();
                                                cD.fillRect(6, 18, 12, 26);
                                                dummy.refresh();

                                                let cooler = this.textures.createCanvas('interactive_cooler', 20, 36);
                                                let cCol = cooler.context;
                                                cCol.fillStyle = '#475569';
                                                cCol.fillRect(0, 12, 20, 24);
                                                cCol.fillStyle = '#38bdf8';
                                                cCol.fillRect(3, 2, 14, 10);
                                                cooler.refresh();

                                                let cup = this.textures.createCanvas('interactive_cup', 30, 42);
                                                let cCp = cup.context;
                                                cCp.fillStyle = '#eab308';
                                                cCp.beginPath();
                                                cCp.arc(15, 15, 10, 0, Math.PI);
                                                cCp.fill();
                                                cCp.fillRect(12, 15, 6, 15);
                                                cCp.fillRect(6, 30, 18, 8);
                                                cup.refresh();

                                                let diploma = this.textures.createCanvas('interactive_diploma', 34, 42);
                                                let cDp = diploma.context;
                                                cDp.fillStyle = '#b45309';
                                                cDp.fillRect(0, 0, 34, 42);
                                                cDp.fillStyle = '#fef08a';
                                                cDp.fillRect(3, 3, 28, 36);
                                                cDp.fillStyle = '#eab308';
                                                cDp.beginPath();
                                                cDp.arc(17, 21, 6, 0, Math.PI * 2);
                                                cDp.fill();
                                                diploma.refresh();

                                                let fridge = this.textures.createCanvas('interactive_fridge', 22, 38);
                                                let cFr = fridge.context;
                                                cFr.fillStyle = '#0f172a';
                                                cFr.fillRect(0, 0, 22, 38);
                                                cFr.fillStyle = '#10b981';
                                                cFr.fillRect(3, 4, 16, 14);
                                                cFr.fillStyle = '#475569';
                                                cFr.fillRect(3, 22, 16, 12);
                                                fridge.refresh();

                                                let rack = this.textures.createCanvas('interactive_rack', 32, 44);
                                                let cRk = rack.context;
                                                cRk.fillStyle = '#1e293b';
                                                cRk.fillRect(14, 0, 4, 44);
                                                cRk.fillRect(0, 40, 32, 4);
                                                cRk.fillStyle = '#c084fc';
                                                cRk.fillRect(4, 8, 24, 14);
                                                rack.refresh();

                                                let avatar = this.textures.createCanvas('char_default', 32, 48);
                                                let avCtx = avatar.context;
                                                avCtx.fillStyle = '#fbcfe8'; 
                                                avCtx.beginPath();
                                                avCtx.arc(16, 10, 8, 0, Math.PI * 2);
                                                avCtx.fill();
                                                avCtx.fillStyle = '#1e3a8a'; 
                                                avCtx.fillRect(6, 16, 20, 32);
                                                avCtx.fillStyle = '#ffffff'; 
                                                avCtx.fillRect(5, 29, 22, 3);
                                                avCtx.fillStyle = '#000000'; 
                                                avCtx.fillRect(13, 28, 6, 5);
                                                avatar.refresh();
                                            }

                                            createColorTile(key, fill, stroke) {
                                                let tile = this.textures.createCanvas(key, TILE_WIDTH, TILE_HEIGHT);
                                                let ctx = tile.context;
                                                ctx.fillStyle = fill;
                                                ctx.lineWidth = 1;
                                                ctx.strokeStyle = stroke;
                                                ctx.beginPath();
                                                ctx.moveTo(TILE_WIDTH / 2, 0);
                                                ctx.lineTo(TILE_WIDTH, TILE_HEIGHT / 2);
                                                ctx.lineTo(TILE_WIDTH / 2, TILE_HEIGHT);
                                                ctx.lineTo(0, TILE_HEIGHT / 2);
                                                ctx.closePath();
                                                ctx.fill();
                                                ctx.stroke();
                                                tile.refresh();
                                             }

                                             // Initializes a massive MMO dynamic environment populate (300+ connected users)
                                             initMMOUniverse() {
                                                 this.globalPeersData = [];
                                                 
                                                 // Add dynamic SQLite system students
                                                 const dbRaw = $systemUsersJson;
                                                 if (dbRaw && dbRaw.length > 0) {
                                                     dbRaw.forEach((student, index) => {
                                                         const rEnv = Object.keys(TELEPORTS)[index % 8];
                                                         this.globalPeersData.push({
                                                             id: student.id || student.nome || "sys_" + index,
                                                             name: student.nome || student.name || "Guerreiro",
                                                             belt: student.faixa || student.belt || "Branca",
                                                             level: student.level || student.nivel || 1,
                                                             xp: student.xp || student.XP || 100,
                                                             academy: student.academia || student.academy || "Carlson Gracie",
                                                             env: rEnv,
                                                             gridX: 2 + Math.floor(Math.random() * 11),
                                                             gridY: 2 + Math.floor(Math.random() * 11),
                                                             isBot: false,
                                                             actionTimer: 0
                                                         });
                                                     });
                                                 }

                                                 // Spawn procedural bots to reach 300+ connected players!
                                                 const botBelts = ["Branca", "Azul", "Roxa", "Marrom", "Preta"];
                                                 const botAcademies = ["Gracie Barra", "Alliance Itaim", "Checkmat Pinheiros"];
                                                 const botNames = [
                                                     "GraciePassador", "GuardaDeMola", "BuchechaJr", "KimonoAzul", "TatameLover", 
                                                     "BerimboloPro", "RaspadorDeIa", "TrianguloJusto", "MestreDoOss", "LutadorCopa",
                                                     "ChaveDePe", "ArmlockCerteiro", "MeiaGuarda", "GuerreiroLuz", "LoboDosDojos",
                                                     "CarlsonLeao", "OsotoGariKing", "PegadaFirme", "Estrangulador", "EspiritoLivre",
                                                     "PosturaFirme", "ArrozComFeijao", "KimonoTrancado", "FocoMestre", "TreinoDuro"
                                                 ];

                                                 const rooms = Object.keys(TELEPORTS);
                                                 const botsCount = 300 - this.globalPeersData.length;

                                                 for (let i = 0; i < botsCount; i++) {
                                                     const name = botNames[i % botNames.length] + "_" + (100 + i);
                                                     const belt = botBelts[i % botBelts.length];
                                                     const academy = botAcademies[i % botAcademies.length];
                                                     const level = 5 + (i % 45);
                                                     const room = rooms[i % rooms.length];

                                                     this.globalPeersData.push({
                                                         id: "procedural_bot_" + i,
                                                         name: name,
                                                         belt: belt,
                                                         level: level,
                                                         xp: level * 100 + (i * 7) % 100,
                                                         academy: academy,
                                                         env: room,
                                                         gridX: 2 + Math.floor(Math.random() * 11),
                                                         gridY: 2 + Math.floor(Math.random() * 11),
                                                         isBot: true,
                                                         actionTimer: Math.random() * 2000
                                                     });
                                                 }
                                             }

                                             // Spawns and manages MMO avatars in current loaded Dojo screen room
                                             spawnRoomPeers() {
                                                 this.peerGroup.clear(true, true);
                                                 this.peers = {};

                                                 // Filter players in current loaded environment screen
                                                 const currentRoomPeers = this.globalPeersData.filter(p => p.env === this.currentEnv);
                                                 
                                                 currentRoomPeers.forEach(p => {
                                                     const textureKey = "avatar_" + p.id;
                                                     const bCol = getBeltColorHex(p.belt);
                                                     const kCol = getKimonoColorHex(p.academy);
                                                     
                                                     // Procedural sprite building
                                                     buildAvatarTexture(this, textureKey, bCol, kCol);

                                                     const isoPos = cartToIso(p.gridX, p.gridY);
                                                     const container = this.add.container(isoPos.x, isoPos.y);
                                                     container.setDepth(isoPos.y + 10);

                                                     const sprite = this.add.image(0, -20, textureKey);
                                                     
                                                     // Name text tag above head
                                                     const tag = this.add.text(0, -48, p.name, {
                                                         fontSize: '7.5px',
                                                         fontFamily: 'monospace',
                                                         backgroundColor: 'rgba(15, 23, 42, 0.9)',
                                                         padding: { x: 2, y: 1 },
                                                         color: p.isBot ? '#cbd5e1' : '#f59e0b', // Gold name for database system students
                                                         stroke: p.isBot ? '#475569' : '#b45309',
                                                         strokeThickness: 0.5
                                                     }).setOrigin(0.5);

                                                     // Info label
                                                     const info = this.add.text(0, -38, '[NVL ' + p.level + ' ' + (p.academy.split(" ")[0]).toUpperCase() + ']', {
                                                         fontSize: '5.5px',
                                                         fontFamily: 'monospace',
                                                         color: '#e2e8f0'
                                                     }).setOrigin(0.5);

                                                     container.add([sprite, tag, info]);
                                                     container.sprite = sprite;
                                                     container.gridX = p.gridX;
                                                     container.gridY = p.gridY;
                                                     container.isBot = p.isBot;
                                                     container.chatBubble = null;

                                                     this.peers[p.id] = container;
                                                     this.peerGroup.add(container);
                                                 });
                                             }

                                             // Hybrid sync model (Simulated event packets or Real Socket.IO client WS)
                                             setupSocketIOBridge() {
                                                 window.connectSocket = (url) => {
                                                     console.log("Iniciando conexão Socket.IO com: " + url);
                                                     this.socketStatus = "CONNECTING";
                                                     this.reportSocketStatus("CONNECTING");

                                                     setTimeout(() => {
                                                         // Simulate network handshakes and online sync
                                                         this.socketStatus = "ONLINE";
                                                         this.reportSocketStatus(url);
                                                         
                                                         // Notify of initial welcome sync
                                                         this.triggerWorldNotification("Você entrou no servidor Socket.io!");
                                                     }, 1800);
                                                 };
                                             }

                                             reportSocketStatus(status) {
                                                 if (window.AndroidWebView) {
                                                     window.AndroidWebView.postMessage(JSON.stringify({
                                                         type: 'SOCKET_STATUS',
                                                         status: status
                                                     }));
                                                 }
                                             }

                                             broadcastMovement(x, y) {
                                                 console.log("Socket.IO Broadcast: player_movement {" + x + ", " + y + "}");
                                             }

                                             triggerWorldNotification(msg) {
                                                 let dBox = document.getElementById('dialogue-box');
                                                 let dText = document.getElementById('dialogue-text');
                                                 dText.innerHTML = '<strong style="color:#eab308;font-size:10px;">📣 ALERTA DO SERVIDOR</strong><br/><span style="color:#f1f5f9;font-size:8px;">' + msg + '</span>';
                                                 dBox.style.display = 'block';
                                                 setTimeout(() => { dBox.style.display = 'none'; }, 2200);
                                             }

                                             // Show comic text bubble above avatar's head in Dojo
                                             showSpeechBubble(peerContainer, text, channel) {
                                                 if (peerContainer.chatBubble) {
                                                     peerContainer.chatBubble.destroy();
                                                 }

                                                 // Setup small Speech bubble bg and text
                                                 const bubble = this.add.container(0, -68);
                                                 const txt = this.add.text(0, 0, text, {
                                                     fontSize: '6.5px',
                                                     fontFamily: 'monospace',
                                                     color: '#0f172a',
                                                     backgroundColor: (channel === 'ACADEMIA' ? '#93c5fd' : (channel === 'PRIVADA' ? '#fbcfe8' : '#fef08a')),
                                                     padding: { x: 4, y: 2 }
                                                 }).setOrigin(0.5);

                                                 bubble.add(txt);
                                                 peerContainer.add(bubble);
                                                 peerContainer.chatBubble = bubble;

                                                 // Vanish bubble after 2 seconds
                                                 this.time.delayedCall(3200, () => {
                                                     if (bubble && bubble.active) bubble.destroy();
                                                 });
                                             }
                                            }

                                            create() {
                                                this.cameras.main.setBackgroundColor('#020617');
                                                 this.peerGroup = this.add.group();

                                                // Visual groups
                                                this.mapGroup = this.add.group();
                                                this.npcGroup = this.add.group();
                                                this.teleportGroup = this.add.group();
                                                this.interactiveGroup = this.add.group();

                                                // Setup local player
                                                let pPos = cartToIso($playerX, $playerY);
                                                this.localPlayer = this.add.container(pPos.x, pPos.y);
                                                this.localPlayer.setDepth(pPos.y + 15);

                                                const bCol = getBeltColorHex(MY_PROFILE.belt);
                                                 const kCol = getKimonoColorHex(MY_PROFILE.academy);
                                                 buildAvatarTexture(this, 'local_avatar', bCol, kCol);
                                                 let pSprite = this.add.image(0, -20, 'local_avatar');
                                                 this.localPlayer.sprite = pSprite;
                                                 let tagText = this.add.text(0, -48, MY_PROFILE.name, {
                                                    fontSize: '8.5px',
                                                    fontFamily: 'monospace',
                                                    backgroundColor: 'rgba(9, 13, 22, 0.95)',
                                                    padding: { x: 3, y: 1.5 },
                                                    color: '#2dd4bf',
                                                    stroke: '#0891b2',
                                                    strokeThickness: 1
                                                }).setOrigin(0.5);

                                                 let beltText = this.add.text(0, -38, '[FAIXA ' + MY_PROFILE.belt.toUpperCase() + ' NVL ' + MY_PROFILE.level + ']', {
                                                    fontSize: '6.5px',
                                                    fontFamily: 'monospace',
                                                    color: '#38bdf8'
                                                }).setOrigin(0.5);

                                                this.localPlayer.add([pSprite, tagText, beltText]);
                                                this.localPlayer.gridX = $playerX;
                                                this.localPlayer.gridY = $playerY;

                                                // Configure keyboard
                                                this.cursors = this.input.keyboard.createCursorKeys();
                                                this.keysWASD = this.input.keyboard.addKeys({
                                                    up: Phaser.Input.Keyboard.KeyCodes.W,
                                                    down: Phaser.Input.Keyboard.KeyCodes.S,
                                                    left: Phaser.Input.Keyboard.KeyCodes.A,
                                                    right: Phaser.Input.Keyboard.KeyCodes.D
                                                });
                                                this.nextMoveTime = 0;

                                                // Load initial area
                                                // Build global peers dataset (SQLite system users + procedural bots = 300+ players)
                                                 this.initMMOUniverse();

                                                 // Map window interface bridges
                                                 window.movePlayer = (dx, dy) => {
                                                     this.moveLocalPlayer(dx, dy);
                                                 };
                                                 window.changeEnvironment = (envName) => {
                                                     if (envName && envName !== this.currentEnv) {
                                                         this.transitionEnvironment(envName, 7, 9);
                                                     }
                                                 };
                                                 
                                                 window.mutedUsersList = [];
                                                 window.updateMutedUsers = (mutedList) => {
                                                     window.mutedUsersList = mutedList || [];
                                                 };

                                                 window.isSenderMuted = (senderName) => {
                                                     return (window.mutedUsersList || []).includes(senderName);
                                                 };

                                                 window.sendLocalChatMessage = (sender, text, channel, target) => {
                                                     // Show speech bubble over localPlayer!
                                                     this.showSpeechBubble(this.localPlayer, text, channel);
                                                     
                                                     // If the message is typed in Local/Academy/Private, let a random nearby peer respond!
                                                     if (channel === 'LOCAL') {
                                                         setTimeout(() => {
                                                             const activePeers = Object.values(this.peers);
                                                             if (activePeers.length > 0) {
                                                                 const randomPeer = activePeers[Math.floor(Math.random() * activePeers.length)];
                                                                 
                                                                 // Don't respond if they are muted
                                                                 if (window.isSenderMuted && window.isSenderMuted(randomPeer.name)) {
                                                                     return;
                                                                 }

                                                                 const possibleReplies = [
                                                                     "Oss! Treino de pressão total.", "Boa rapaz!", "Oss! Vamos treinar mestre.", 
                                                                     "Eu foco na guarda.", "Defesa pessoal sempre!", "A passagem tá justa!"
                                                                 ];
                                                                 const replyText = possibleReplies[Math.floor(Math.random() * possibleReplies.length)];
                                                                 
                                                                 this.showSpeechBubble(randomPeer, replyText, 'LOCAL');
                                                                 
                                                                 // Report back to Compose
                                                                 if (window.AndroidWebView) {
                                                                     window.AndroidWebView.postMessage(JSON.stringify({
                                                                         type: 'CHAT_MESSAGE',
                                                                         sender: randomPeer.name,
                                                                         content: replyText,
                                                                         channel: 'LOCAL'
                                                                     }));
                                                                 }
                                                             }
                                                         }, 1500);
                                                     } else if (channel === 'ACADEMIA') {
                                                         setTimeout(() => {
                                                             const possibleReplies = [
                                                                 "Vamos levar o escudo da equipe!", "Gracie Barra na veia, oss!", "Carlson Gracie Team forte!", 
                                                                 "No tatame somos gigantes.", "Forte abraço equipe!"
                                                             ];
                                                             const replyText = possibleReplies[Math.floor(Math.random() * possibleReplies.length)];
                                                             
                                                             // Show bubble on one of peers if there's someone in room
                                                             const activePeers = Object.values(this.peers);
                                                             if (activePeers.length > 0) {
                                                                 const peer = activePeers[Math.floor(Math.random() * activePeers.length)];
                                                                 if (!window.isSenderMuted || !window.isSenderMuted(peer.name)) {
                                                                     this.showSpeechBubble(peer, replyText, 'ACADEMIA');
                                                                 }
                                                             }

                                                             if (window.AndroidWebView) {
                                                                 window.AndroidWebView.postMessage(JSON.stringify({
                                                                     type: 'CHAT_MESSAGE',
                                                                     sender: "Parceiro de Equipe",
                                                                     content: replyText,
                                                                     channel: 'ACADEMIA'
                                                                 }));
                                                             }
                                                         }, 1500);
                                                     } else if (channel === 'PRIVADA' && target) {
                                                         setTimeout(() => {
                                                             const replyText = "Entendido, mestre! Treino privado marcado. Oss!";
                                                             
                                                             // If target is in current room, make them show bubble
                                                             const activePeers = Object.values(this.peers);
                                                             const peer = activePeers.find(p => p.name === target);
                                                             if (peer && (!window.isSenderMuted || !window.isSenderMuted(peer.name))) {
                                                                 this.showSpeechBubble(peer, replyText, 'PRIVADA');
                                                             }

                                                             if (window.AndroidWebView) {
                                                                 window.AndroidWebView.postMessage(JSON.stringify({
                                                                     type: 'CHAT_MESSAGE',
                                                                     sender: target,
                                                                     content: replyText,
                                                                     channel: 'PRIVADA',
                                                                     recipient: sender
                                                                 }));
                                                             }
                                                         }, 1500);
                                                     }
                                                 };

                                                 // WebSockets socket simulation / live link
                                                 this.socketStatus = "SIMULATED";
                                                 this.setupSocketIOBridge();

                                                 this.loadArea(this.currentEnv, $playerX, $playerY);

                                                // Configure Camera following
                                                this.cameras.main.startFollow(this.localPlayer, true, 0.08, 0.08);
                                                this.cameras.main.setZoom(1.4);
                                                this.cameras.main.setBounds(-200, -200, 1200, 900);
                                            }

                                            loadArea(envName, startX, startY) {
                                                this.currentEnv = envName;
                                                document.getElementById('val-mundo').innerText = envName;

                                                // Empty previous elements
                                                this.mapGroup.clear(true, true);
                                                this.npcGroup.clear(true, true);
                                                this.teleportGroup.clear(true, true);
                                                this.interactiveGroup.clear(true, true);

                                                // Build collisions grid dynamically
                                                this.collisionGrid = [];
                                                let currentTeleports = TELEPORTS[envName] || [];
                                                let envDef = ENV_DATA[envName] || { npcs: [], interactables: [] };

                                                for (let x = 0; x < GRID_SIZE; x++) {
                                                    this.collisionGrid[x] = [];
                                                    for (let y = 0; y < GRID_SIZE; y++) {
                                                        let isBoundary = (x === 0 || y === 0 || x === GRID_SIZE - 1 || y === GRID_SIZE - 1);
                                                        
                                                        let isTeleport = false;
                                                        for (let t of currentTeleports) {
                                                            if (t.gridX === x && t.gridY === y) {
                                                                isTeleport = true;
                                                                break;
                                                            }
                                                        }

                                                        let isBlocked = isBoundary && !isTeleport;

                                                        // Check NPC collision
                                                        for (let npc of envDef.npcs) {
                                                            if (npc.gridX === x && npc.gridY === y) {
                                                                isBlocked = true;
                                                            }
                                                        }

                                                        // Check Interactable collision
                                                        for (let item of envDef.interactables) {
                                                            if (item.gridX === x && item.gridY === y) {
                                                                isBlocked = true;
                                                            }
                                                        }

                                                        this.collisionGrid[x][y] = isBlocked;

                                                        // Identify tile graphic asset
                                                        let tileType = 'stone';
                                                        if (isTeleport) {
                                                            tileType = 'portal';
                                                        } else {
                                                            switch (envName) {
                                                                case 'Recepção':
                                                                    tileType = 'reception_wood';
                                                                    break;
                                                                case 'Tatame Principal':
                                                                    if (x >= 3 && x <= 11 && y >= 3 && y <= 11) {
                                                                        tileType = (x + y) % 2 === 0 ? 'tatame_blue' : 'tatame_yellow';
                                                                    } else {
                                                                        tileType = 'stone';
                                                                    }
                                                                    break;
                                                                case 'Área de Aula':
                                                                    tileType = 'tatame_green';
                                                                    break;
                                                                case 'Vestiários':
                                                                    tileType = 'locker_ceramic';
                                                                    break;
                                                                case 'Hall de Troféus':
                                                                    tileType = 'trophy_lux';
                                                                    break;
                                                                case 'Sala do Mestre':
                                                                    tileType = 'mahogany_dark';
                                                                    break;
                                                                case 'Sala VIP':
                                                                    tileType = 'vip_emerald';
                                                                    break;
                                                                case 'Loja da Academia':
                                                                    tileType = 'shop_tech';
                                                                    break;
                                                                default:
                                                                    tileType = 'stone';
                                                            }
                                                        }

                                                        let pos = cartToIso(x, y);
                                                        let tileImg = this.add.image(pos.x, pos.y, 'tile_' + tileType);
                                                        tileImg.setDepth(pos.y);
                                                        this.mapGroup.add(tileImg);

                                                        // Draw obstacles
                                                        if (isBoundary && !isTeleport) {
                                                            let obstacleImg = this.add.image(pos.x, pos.y - 10, 'entity_pillar');
                                                            obstacleImg.setDepth(pos.y + 12);
                                                            this.mapGroup.add(obstacleImg);
                                                        }
                                                    }
                                                }

                                                // Render Portals metadata
                                                currentTeleports.forEach(t => {
                                                    let tPos = cartToIso(t.gridX, t.gridY);
                                                    let labelText = this.add.text(tPos.x, tPos.y - 12, t.label, {
                                                        fontSize: '6px',
                                                        fontFamily: 'monospace',
                                                        color: '#f472b6',
                                                        backgroundColor: 'rgba(15, 23, 42, 0.95)',
                                                        padding: { x: 2, y: 1 }
                                                    }).setOrigin(0.5).setDepth(tPos.y + 12);
                                                    this.teleportGroup.add(labelText);
                                                });

                                                // Render NPCs
                                                envDef.npcs.forEach(n => {
                                                    let nPos = cartToIso(n.gridX, n.gridY);
                                                    let container = this.add.container(nPos.x, nPos.y);
                                                    container.setDepth(nPos.y + 10);

                                                    let nSprite = this.add.image(0, -20, 'char_front');
                                                    if (n.tint) nSprite.setTint(n.tint);

                                                    let nTag = this.add.text(0, -48, n.name, {
                                                        fontSize: '8px',
                                                        fontFamily: 'monospace',
                                                        color: '#f1f5f9',
                                                        backgroundColor: 'rgba(15, 23, 42, 0.9)',
                                                        padding: { x: 2, y: 1 }
                                                    }).setOrigin(0.5);

                                                    let nBelt = this.add.text(0, -37, '[FAIXA ' + n.belt.toUpperCase() + ']', {
                                                        fontSize: '6px',
                                                        fontFamily: 'monospace',
                                                        color: n.belt === 'Vermelha' ? '#ef4444' : n.belt.includes('Coral') ? '#f97316' : '#38bdf8'
                                                    }).setOrigin(0.5);

                                                    container.add([nSprite, nTag, nBelt]);
                                                    this.npcGroup.add(container);
                                                });

                                                // Render Interactables
                                                envDef.interactables.forEach(item => {
                                                    let itemPos = cartToIso(item.gridX, item.gridY);
                                                    let itemImg = this.add.image(itemPos.x, itemPos.y - 12, item.sprite);
                                                    itemImg.setDepth(itemPos.y + 11);
                                                    this.interactiveGroup.add(itemImg); this.spawnRoomPeers();
                                                });
                                            }

                                            moveLocalPlayer(dx, dy) {
                                                let nextX = this.localPlayer.gridX + dx;
                                                let nextY = this.localPlayer.gridY + dy;

                                                if (nextX >= 0 && nextX < GRID_SIZE && nextY >= 0 && nextY < GRID_SIZE) {
                                                    if (!this.collisionGrid[nextX][nextY]) {
                                                        this.localPlayer.gridX = nextX;
                                                        this.localPlayer.gridY = nextY;

                                                        let targetIso = cartToIso(nextX, nextY);
                                                        this.tweens.add({
                                                            targets: this.localPlayer,
                                                            x: targetIso.x,
                                                            y: targetIso.y,
                                                            duration: 160,
                                                            ease: 'Linear',
                                                            onComplete: () => {
                                                                if (this.localPlayer) {
                                                                    this.localPlayer.setDepth(targetIso.y + 15);
                                                                    this.checkTeleports();
                                                                }
                                                            }
                                                        });

                                                        document.getElementById('val-coords').innerText = '(' + nextX + ', ' + nextY + ')';

                                                        if (window.AndroidWebView) {
                                                            window.AndroidWebView.postMessage(JSON.stringify({
                                                                type: 'PLAYER_MOVE',
                                                                x: nextX,
                                                                y: nextY
                                                            }));
                                                        }
                                                    } else {
                                                        this.cameras.main.flash(35, 120, 10, 10);
                                                    }
                                                }
                                            }

                                            checkTeleports() {
                                                let currentTeleports = TELEPORTS[this.currentEnv] || [];
                                                for (let t of currentTeleports) {
                                                    if (this.localPlayer.gridX === t.gridX && this.localPlayer.gridY === t.gridY) {
                                                        this.transitionEnvironment(t.destEnv, t.destX, t.destY);
                                                        break;
                                                    }
                                                }
                                            }

                                            transitionEnvironment(envName, startX, startY) {
                                                this.cameras.main.flash(180, 168, 85, 247);

                                                this.localPlayer.gridX = startX;
                                                this.localPlayer.gridY = startY;
                                                let startIso = cartToIso(startX, startY);
                                                this.localPlayer.x = startIso.x;
                                                this.localPlayer.y = startIso.y;
                                                this.localPlayer.setDepth(startIso.y + 15);

                                                this.cameras.main.scrollX = startIso.x - 330;
                                                this.cameras.main.scrollY = startIso.y - 200;

                                                this.loadArea(envName, startX, startY);

                                                if (window.AndroidWebView) {
                                                    window.AndroidWebView.postMessage(JSON.stringify({
                                                        type: 'ENV_CHANGE',
                                                        env: envName,
                                                        x: startX,
                                                        y: startY
                                                    }));
                                                }
                                            }

                                            update(time, delta) {
                                                document.getElementById('val-fps').innerText = Math.round(1000 / delta);

                                                if (this.localPlayer) {
                                                    let dialogueText = null;

                                                    // Proximity to NPCs
                                                    let envDef = ENV_DATA[this.currentEnv] || { npcs: [], interactables: [] };
                                                    for (let i = 0; i < envDef.npcs.length; i++) {
                                                        let n = envDef.npcs[i];
                                                        let dist = Math.abs(this.localPlayer.gridX - n.gridX) + Math.abs(this.localPlayer.gridY - n.gridY);
                                                        if (dist <= 1) {
                                                            dialogueText = '<strong style="color:#2dd4bf;font-size:10px;">' + n.name + '</strong> <span style="font-size:6.3px;color:#cbd5e1;border:0.5px solid #64748b;padding:0px 2px;border-radius:2px;">FAIXA ' + n.belt.toUpperCase() + '</span><br/><span style="color:#f1f5f9;font-style:italic;">"' + n.quote + '"</span>';
                                                            break;
                                                        }
                                                    }

                                                    // Proximity to Interactable Items
                                                    if (!dialogueText) {
                                                        for (let i = 0; i < envDef.interactables.length; i++) {
                                                            let item = envDef.interactables[i];
                                                            let dist = Math.abs(this.localPlayer.gridX - item.gridX) + Math.abs(this.localPlayer.gridY - item.gridY);
                                                            if (dist <= 1) {
                                                                dialogueText = '<strong style="color:#ca8a04;font-size:10px;">🔍 EXAMINAR: ' + item.name + '</strong><br/><span style="color:#f1f5f9;font-size:7.5px;">' + item.desc + '</span>';
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    let dBox = document.getElementById('dialogue-box');
                                                    let dText = document.getElementById('dialogue-text');
                                                    if (dialogueText) {
                                                        dText.innerHTML = dialogueText;
                                                        dBox.style.display = 'block';
                                                    } else {
                                                        dBox.style.display = 'none';
                                                    }
                                                }

                                                if (this.localPlayer && time > this.nextMoveTime) {
                                                     // Update procedural bots/peers AI state & paths
                                                     this.globalPeersData.forEach(p => {
                                                         if (p.isBot) {
                                                             p.actionTimer -= delta;
                                                             if (p.actionTimer <= 0) {
                                                                 p.actionTimer = 4000 + Math.random() * 5000;
                                                                 
                                                                 // Random walk inside the Dojo
                                                                 if (Math.random() < 0.4) {
                                                                     const dirs = [{x:1, y:0}, {x:-1, y:0}, {x:0, y:1}, {x:0, y:-1}];
                                                                     const dir = dirs[Math.floor(Math.random() * dirs.length)];
                                                                     const newX = p.gridX + dir.x;
                                                                     const newY = p.gridY + dir.y;
                                                                     
                                                                     // Boundary & screen room bounds check
                                                                     if (newX >= 1 && newX < GRID_SIZE - 1 && newY >= 1 && newY < GRID_SIZE - 1) {
                                                                         p.gridX = newX;
                                                                         p.gridY = newY;
                                                                         
                                                                         // If the bot is active in player's current screen room, tween them beautifully!
                                                                         const container = this.peers[p.id];
                                                                         if (container) {
                                                                             const targetIso = cartToIso(newX, newY);
                                                                             this.tweens.add({
                                                                                 targets: container,
                                                                                 x: targetIso.x,
                                                                                 y: targetIso.y,
                                                                                 duration: 600,
                                                                                 ease: 'Power1',
                                                                                 onComplete: () => {
                                                                                     if (container && container.active) {
                                                                                         container.setDepth(targetIso.y + 10);
                                                                                     }
                                                                                 }
                                                                             });
                                                                         }
                                                                     }
                                                                 }

                                                                 // Random chatter & bubble phrases based on Carlson Gracie martial art style
                                                                 if (Math.random() < 0.25) {
                                                                     const container = this.peers[p.id];
                                                                     if (container) {
                                                                         const phrases = [
                                                                             "Oss! Treino duro hoje!", "Passagem de guarda encaixada!",
                                                                             "Mestre Carlson Gracie é lendário!", "Carlson Gracie Team!",
                                                                             "O tatame é meu lar.", "Defesa pessoal em dia.",
                                                                             "Cuidado com esse armlock!", "Faixa " + p.belt + " focando na evolução.",
                                                                             "Para frente sempre!", "Sem pressa, mas sem pausa.",
                                                                             "Berimbolo aqui não se cria!", "Amassando e passando!",
                                                                             "Esse kimono clássico Carlson Gracie é muito estiloso.",
                                                                             "Foco total na pressão!", "Oss! Vamos rolar?",
                                                                             "Respeito ao tatame sempre.", "A evolução é constante.",
                                                                             "Pronto para a proxima graduação!"
                                                                         ];
                                                                         const phrase = phrases[Math.floor(Math.random() * phrases.length)];
                                                                         // Check if the bot is silenced
                                                                          const isMuted = window.isSenderMuted ? window.isSenderMuted(p.name) : false;
                                                                          if (!isMuted) {
                                                                              this.showSpeechBubble(container, phrase, 'LOCAL');
                                                                              
                                                                              // Stream to Android local chat log
                                                                              if (window.AndroidWebView) {
                                                                                  window.AndroidWebView.postMessage(JSON.stringify({
                                                                                      type: 'CHAT_MESSAGE',
                                                                                      sender: p.name,
                                                                                      content: phrase,
                                                                                      channel: 'LOCAL'
                                                                                  }));
                                                                              }
                                                                          }
                                                                     }
                                                                 }
                                                             }
                                                         }
                                                     });
                                                    let dx = 0;
                                                    let dy = 0;
                                                    if (this.cursors.left.isDown || this.keysWASD.left.isDown) {
                                                        dx = -1;
                                                    } else if (this.cursors.right.isDown || this.keysWASD.right.isDown) {
                                                        dx = 1;
                                                    } else if (this.cursors.up.isDown || this.keysWASD.up.isDown) {
                                                        dy = -1;
                                                    } else if (this.cursors.down.isDown || this.keysWASD.down.isDown) {
                                                        dy = 1;
                                                    }
                                                    if (dx !== 0 || dy !== 0) {
                                                        this.moveLocalPlayer(dx, dy);
                                                        this.nextMoveTime = time + 200;
                                                    }
                                                }
                                            }
                                        }

                                        const config = {
                                            type: Phaser.AUTO,
                                            scale: {
                                                mode: Phaser.Scale.FIT,
                                                autoCenter: Phaser.Scale.CENTER_BOTH,
                                                width: 660,
                                                height: 400
                                            },
                                            parent: 'game-container',
                                            scene: JiuVerseIsometricDojo
                                        };

                                        const game = new Phaser.Game(config);

                                        window.movePlayer = function(dx, dy) {
                                            let scene = game.scene.keys['JiuVerseDojo'];
                                            if (scene) {
                                                scene.moveLocalPlayer(dx, dy);
                                            }
                                        };

                                        window.updatePlayerCoords = function(x, y) {
                                            let scene = game.scene.keys['JiuVerseDojo'];
                                            if (scene && scene.localPlayer) {
                                                if (scene.localPlayer.gridX !== x || scene.localPlayer.gridY !== y) {
                                                    scene.localPlayer.gridX = x;
                                                    scene.localPlayer.gridY = y;
                                                    let targetIso = cartToIso(x, y);
                                                    scene.localPlayer.x = targetIso.x;
                                                    scene.localPlayer.y = targetIso.y;
                                                    scene.localPlayer.setDepth(targetIso.y + 15);
                                                    document.getElementById('val-coords').innerText = '(' + x + ', ' + y + ')';
                                                }
                                            }
                                        };

                                        window.changeEnvironment = function(envName) {
                                            let scene = game.scene.keys['JiuVerseDojo'];
                                            if (scene && scene.currentEnv !== envName) {
                                                let startX = 7, startY = 7;
                                                if (envName === 'Tatame Principal') { startX = 7; startY = 9; }
                                                else if (envName === 'Recepção') { startX = 7; startY = 7; }
                                                else if (envName === 'Área de Aula') { startX = 5; startY = 5; }
                                                else if (envName === 'Vestiários') { startX = 6; startY = 6; }
                                                else if (envName === 'Hall de Troféus') { startX = 6; startY = 6; }
                                                else if (envName === 'Sala do Mestre') { startX = 7; startY = 6; }
                                                else if (envName === 'Sala VIP') { startX = 6; startY = 6; }
                                                else if (envName === 'Loja da Academia') { startX = 6; startY = 6; }
                                                
                                                scene.transitionEnvironment(envName, startX, startY);
                                            }
                                        };

                                        window.addEventListener('resize', () => {
                                            game.scale.resize(window.innerWidth, window.innerHeight);
                                        });
                                    </script>
                                </body>
                                </html>
                            """.trimIndent()

                            loadDataWithBaseURL("https://phaser.io", html, "text/html", "UTF-8", null)
                            webViewRef = this
                             onWebViewCreated(this)
                        }
                    },
                    update = {
                        it.evaluateJavascript("if (window.updatePlayerCoords) { window.updatePlayerCoords($playerX, $playerY); }", null)
                        it.evaluateJavascript("if (window.changeEnvironment) { window.changeEnvironment('$selectedEnvironment'); }", null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Isometric perspective simulated layout fallback
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(180.dp)
                ) {
                    val tWidth = 26f
                    val tHeight = 13f
                    val originX = 75f // offset centering
                    val originY = 20f

                    // Draw Sliding 5x5 grid viewport centered on player (mini-map fallback of 15x15 world)
                    for (offsetR in -2..2) {
                        for (offsetC in -2..2) {
                            val r = playerY + offsetR
                            val c = playerX + offsetC

                            val drawR = offsetR + 2
                            val drawC = offsetC + 2
                            val isoX = originX + (drawC - drawR) * (tWidth / 2f)
                            val isoY = originY + (drawC + drawR) * (tHeight / 2f)

                            val isWithinBounds = r in 0..14 && c in 0..14
                            val isPlayer = playerX == c && playerY == r
                            // Match NPC/Interactable locations based on environment in fallback
                            val isBot = isWithinBounds && (
                                when (selectedEnvironment) {
                                    "Recepção" -> (c == 5 && r == 4) || (c == 9 && r == 8)
                                    "Tatame Principal" -> (c == 7 && r == 5) || (c == 3 && r == 7)
                                    "Área de Aula" -> (c == 7 && r == 4) || (c == 4 && r == 8)
                                    "Vestiários" -> (c == 6 && r == 8) || (c == 9 && r == 5)
                                    "Hall de Troféus" -> (c == 5 && r == 7) || (c == 9 && r == 6)
                                    "Sala do Mestre" -> (c == 7 && r == 4)
                                    "Sala VIP" -> (c == 4 && r == 5) || (c == 10 && r == 8)
                                    "Loja da Academia" -> (c == 5 && r == 6)
                                    else -> false
                                }
                            )
                            val isBlocked = !isWithinBounds || (
                                r == 0 || c == 0 || r == 14 || c == 14 ||
                                when (selectedEnvironment) {
                                    "Recepção" -> (c == 5 && r == 5) || (c == 6 && r == 5) || (c == 3 && r == 3)
                                    "Tatame Principal" -> (c == 7 && r == 6)
                                    "Área de Aula" -> (c == 3 && r == 3)
                                    "Vestiários" -> (c in 3..5 && r == 3) || (c == 7 && r == 7) || (c == 2 && r == 5)
                                    "Hall de Troféus" -> (c == 4 && r == 4) || (c == 10 && r == 4) || (c == 7 && r == 4)
                                    "Sala do Mestre" -> (c == 7 && r == 5) || (c == 4 && r == 3)
                                    "Sala VIP" -> (c == 3 && r == 6) || (c == 11 && r == 6) || (c == 7 && r == 7) || (c == 2 && r == 2)
                                    "Loja da Academia" -> (c == 3 && r == 5) || (c == 11 && r == 5) || (c == 7 && r == 7) || (c == 3 && r == 4)
                                    else -> false
                                }
                            )

                            if (isWithinBounds) {
                                // Render each tile offset dynamically
                                Box(
                                    modifier = Modifier
                                        .size(width = tWidth.dp, height = (tHeight + 2).dp)
                                        .offset(x = isoX.dp, y = isoY.dp)
                                        .background(
                                            color = if (isBlocked) Color(0xFF7F1D1D)
                                                    else if (isPlayer) Color(0xFF1D4ED8)
                                                    else if (isBot) Color(0xFF5B21B6)
                                                    else if ((r + c) % 2 == 0) Color(0xFF0F172A)
                                                    else Color(0xFF1E293B),
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                        .border(0.5.dp, if (isBlocked) Color(0xFFB91C1C) else Color(0xFF334155), RoundedCornerShape(2.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPlayer) {
                                        Text(
                                            "🥋", 
                                            fontSize = 11.sp,
                                            modifier = Modifier.offset(y = (-6).dp)
                                        )
                                    }
                                    if (isBot) {
                                        Text(
                                            "👤", 
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            modifier = Modifier.offset(y = (-4).dp)
                                        )
                                        
                                        // Speech/Proximity bubble simulation for nearby players
                                        val dist = Math.abs(playerX - c) + Math.abs(playerY - r)
                                        if (dist <= 1 && proxFilter) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF10B981), RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                                                    .offset(y = (-12).dp)
                                            ) {
                                                Text("OSS", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FLOATING ACTION BUTTON - INVENTÁRIO MMORPG
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color(0xFF0F172A).copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFD97706), RoundedCornerShape(6.dp))
                    .clickable { isMmorpgInventoryOpen = !isMmorpgInventoryOpen }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎒", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("INV (I)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEAB308))
                }
            }

            // MODAL OVERLAY: FULLSCREEN WINDOW ESTILO MMORPG INVENTÓRIO
            if (isMmorpgInventoryOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .clickable { /* prevent clicks bubbling up */ }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MmorpgInventoryView(
                        phoneCard = phoneCard,
                        phoneTextPrimary = phoneTextPrimary,
                        phoneTextSecondary = phoneTextSecondary,
                        items = simInventory,
                        onEquip = onEquip,
                        userBelt = userBelt,
                        onBeltChange = onBeltChange,
                        userLevel = userLevel,
                        userXp = userXp,
                        userAcademy = userAcademy,
                        mutedCount = mutedUsers.size,
                        chatMessageCount = simMessages.filter { it.isMe }.size,
                        onClose = { isMmorpgInventoryOpen = false },
                        userNickname = userNickname
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Multi-directional crossbar control pad with limit boundaries alerts
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(phoneCard, RoundedCornerShape(10.dp))
                .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FILTRO PROXIMIDADE (SPATIAL)", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = phoneTextSecondary)
                Box(
                    modifier = Modifier
                        .background(if (proxFilter) Color(0xFF065F46) else Color(0xFF374151), RoundedCornerShape(12.dp))
                        .clickable { proxFilter = !proxFilter }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(if (proxFilter) "ATIVADO" else "DESATIVADO", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .clickable { 
                            if (useWebPhaser) {
                                webViewRef?.evaluateJavascript("javascript:if (window.movePlayer) { window.movePlayer(0, -1); }", null)
                            } else {
                                onMove(0, -1) 
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("▲", color = Color(0xFF14B8A6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .clickable { 
                                if (useWebPhaser) {
                                    webViewRef?.evaluateJavascript("javascript:if (window.movePlayer) { window.movePlayer(-1, 0); }", null)
                                } else {
                                    onMove(-1, 0)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("◀", color = Color(0xFF14B8A6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .clickable { 
                                if (useWebPhaser) {
                                    webViewRef?.evaluateJavascript("javascript:if (window.movePlayer) { window.movePlayer(1, 0); }", null)
                                } else {
                                    onMove(1, 0)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", color = Color(0xFF14B8A6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .clickable { 
                            if (useWebPhaser) {
                                webViewRef?.evaluateJavascript("javascript:if (window.movePlayer) { window.movePlayer(0, 1); }", null)
                            } else {
                                onMove(0, 1)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("▼", color = Color(0xFF14B8A6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SimInventoryScreen(
    phoneCard: Color,
    phoneTextPrimary: Color,
    phoneTextSecondary: Color,
    items: List<SimItem>,
    onEquip: (SimItem) -> Unit,
    userBelt: String,
    onBeltChange: (String) -> Unit,
    userLevel: Int,
    userXp: Int,
    userAcademy: String,
    mutedCount: Int,
    chatMessageCount: Int,
    userNickname: String
) {
    MmorpgInventoryView(
        phoneCard = phoneCard,
        phoneTextPrimary = phoneTextPrimary,
        phoneTextSecondary = phoneTextSecondary,
        items = items,
        onEquip = onEquip,
        userBelt = userBelt,
        onBeltChange = onBeltChange,
        userLevel = userLevel,
        userXp = userXp,
        userAcademy = userAcademy,
        mutedCount = mutedCount,
        chatMessageCount = chatMessageCount,
        onClose = null,
        userNickname = userNickname
    )
}

@Composable
fun BeltVisualIndicator(name: String, modifier: Modifier = Modifier) {
    val baseColor = when {
        name.lowercase().contains("branca") -> Color(0xFFF1F5F9)
        name.lowercase().contains("azul") -> Color(0xFF1E40AF)
        name.lowercase().contains("roxa") -> Color(0xFF6B21A8)
        name.lowercase().contains("marrom") -> Color(0xFF78350F)
        name.lowercase().contains("preta") -> Color(0xFF0F172A)
        name.lowercase().contains("coral") -> Color(0xFFC2410C)
        name.lowercase().contains("vermelha") -> Color(0xFF991B1B)
        else -> Color(0xFF64748B)
    }

    Row(
        modifier = modifier
            .height(22.dp)
            .background(baseColor, RoundedCornerShape(4.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight()
                .background(if (name.lowercase().contains("preta")) Color(0xFFDC2626) else Color(0xFF0F172A))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.7f).background(Color.White))
                Spacer(modifier = Modifier.width(2.dp))
                Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.7f).background(Color.White))
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name.uppercase(),
            fontSize = 9.sp,
            color = if (name.lowercase().contains("branca")) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun MmorpgInventoryView(
    phoneCard: Color,
    phoneTextPrimary: Color,
    phoneTextSecondary: Color,
    items: List<SimItem>,
    onEquip: (SimItem) -> Unit,
    userBelt: String,
    onBeltChange: (String) -> Unit,
    userLevel: Int,
    userXp: Int,
    userAcademy: String,
    mutedCount: Int,
    chatMessageCount: Int,
    onClose: (() -> Unit)? = null,
    userNickname: String
) {
    var activeSubTab by remember { mutableStateOf("items") }
    var selectedItem by remember { mutableStateOf<SimItem?>(null) }

    fun getRarityColor(rarity: String): Color {
        return when (rarity.uppercase()) {
            "LENDARIO" -> Color(0xFFFFB300)
            "EPICO" -> Color(0xFFA855F7)
            "RARO" -> Color(0xFF2563EB)
            else -> Color(0xFF64748B)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .border(width = 2.dp, color = Color(0xFFD97706), shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E30), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFFD97706), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚔️", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "INVENTÁRIO JIUVERSE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEAB308),
                    fontFamily = FontFamily.Monospace
                )
            }
            if (onClose != null) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF991B1B), RoundedCornerShape(3.dp))
                        .clickable { onClose() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("X", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val subTabs = listOf(
                "items" to "🎒 ITENS",
                "belts" to "🥋 FAIXAS",
                "status" to "👤 ATLETA",
                "quests" to "🏆 QUESTS"
            )
            subTabs.forEach { (key, label) ->
                val active = activeSubTab == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (active) Color(0xFF1E293B) else Color(0xFF0F172A), RoundedCornerShape(4.dp))
                        .border(1.dp, if (active) Color(0xFFEAB308) else Color(0xFF334155), RoundedCornerShape(4.dp))
                        .clickable { activeSubTab = key }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color(0xFFEAB308) else Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                "status" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF131A26), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text("DADOS DO JOGADOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEAB308))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🥋", fontSize = 28.sp)
                            }

                            Column {
                                Text(text = "NOME: $userNickname", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "ACADEMIA: $userAcademy", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                Text(text = "COGNOME: Aspirante Imortal", fontSize = 8.sp, color = Color(0xFF14B8A6))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val nextXpMax = 2000
                        val progressRatio = (userXp.toFloat() / nextXpMax).coerceIn(0f, 1f)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text("NÍVEL DO TATAME: $userLevel", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            Text("$userXp / $nextXpMax XP", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFFFB300))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(3.dp))
                                .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressRatio)
                                    .fillMaxHeight()
                                    .background(Color(0xFFD97706), RoundedCornerShape(3.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("EQUIPAMENTO ATIVO DA GRADUAÇÃO:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        BeltVisualIndicator(name = userBelt)
                    }
                }

                "belts" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("GRADUAÇÕES JIU-JITSU DISPONÍVEIS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEAB308))
                        Text("Escolha qual faixa sincronizar direto com seu avatar físico e banco de dados:", fontSize = 8.sp, color = Color(0xFF94A3B8))

                        Spacer(modifier = Modifier.height(6.dp))

                        val beltsList = listOf(
                            "Faixa Branca", "Faixa Azul", "Faixa Roxa", "Faixa Marrom", 
                            "Faixa Preta", "Faixa Coral", "Faixa Vermelha"
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(beltsList) { beltName ->
                                val active = userBelt.lowercase() == beltName.lowercase()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF131A26), RoundedCornerShape(6.dp))
                                        .border(1.dp, if (active) Color(0xFFEAB308) else Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                        .clickable { onBeltChange(beltName) }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BeltVisualIndicator(name = beltName)
                                    Box(
                                        modifier = Modifier
                                            .background(if (active) Color(0xFF155E75) else Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, if (active) Color(0xFF2DD4BF) else Color(0xFF334155), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (active) "EQUIPADO" else "EQUIPAR",
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (active) Color(0xFF2DD4BF) else Color(0xFFE2E8F0)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "items" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("COSMÉTICOS & MOBÍLIAS EM POSSE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEAB308))
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val chunkedItems = items.chunked(2)
                            items(chunkedItems) { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    row.forEach { item ->
                                        val isEquipped = item.equipped
                                        val activeSelected = selectedItem?.id == item.id
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Color(0xFF131A26), RoundedCornerShape(6.dp))
                                                .border(1.dp, if (activeSelected) Color(0xFFEAB308) else getRarityColor(item.rarity), RoundedCornerShape(6.dp))
                                                .clickable { selectedItem = item }
                                                .padding(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = item.rarity,
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = getRarityColor(item.rarity)
                                                    )
                                                    if (isEquipped) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("ATIVO", fontSize = 6.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Black)
                                                    }
                                                }
                                                Text(item.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                                                Text("$ ${item.price} JC", fontSize = 7.5.sp, color = Color(0xFFCA8A04))
                                            }
                                        }
                                    }
                                    if (row.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .padding(6.dp)
                        ) {
                            val activeItem = selectedItem ?: items.firstOrNull()
                            if (activeItem != null) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = activeItem.name.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = getRarityColor(activeItem.rarity))
                                        Text(
                                            text = when (activeItem.type) {
                                                "WEARABLE" -> "Acessório equipável no seu personagem para exibição pública"
                                                "FURNITURE" -> "Item de mobília para decorar seu tatame privativo de treino"
                                                "BADGE" -> "Brasão honorífico exibido ao lado do nick do lutador"
                                                else -> "Utilitário consumível do ecossistema RPG JiuVerse"
                                            },
                                            fontSize = 8.sp,
                                            color = Color(0xFF94A3B8),
                                            lineHeight = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = { onEquip(activeItem) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (activeItem.equipped) Color(0xFF991B1B) else Color(0xFF155E75)
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(28.dp).padding(0.dp)
                                    ) {
                                        Text(
                                            text = if (activeItem.equipped) "REMOVER" else "EQUIPAR",
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            } else {
                                Text("Selecione um item no catálogo para inspecionar raridade e uso imediato.", fontSize = 8.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }

                "quests" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("CONQUISTAS DO DOJO (REAL-TIME)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEAB308))
                        Text("Suas conquistas proativas calculadas a partir das ações do simulador:", fontSize = 8.sp, color = Color(0xFF94A3B8))

                        Spacer(modifier = Modifier.height(6.dp))

                        val listQuests = listOf(
                            SimAchievement(
                                title = "Caminho do Suave",
                                description = "Graduou-se além da faixa branca inicial",
                                isCompleted = userBelt.lowercase() != "faixa branca" && userBelt.lowercase() != "branca",
                                progress = if (userBelt.lowercase() != "faixa branca" && userBelt.lowercase() != "branca") 1f else 0f,
                                progressText = if (userBelt.lowercase() != "faixa branca" && userBelt.lowercase() != "branca") "Pronto" else "0/1",
                                rewardXp = 150,
                                icon = "🥋"
                            ),
                            SimAchievement(
                                title = "Empreendedor Sênior",
                                description = "Adquiriu múltiplos cosméticos protetores",
                                isCompleted = items.size >= 4,
                                progress = (items.size.toFloat() / 4f).coerceIn(0f, 1f),
                                progressText = "${items.size}/4 Itens",
                                rewardXp = 300,
                                icon = "🎒"
                            ),
                            SimAchievement(
                                title = "Mestre da Resenha",
                                description = "Postou mensagens de chat de treino",
                                isCompleted = chatMessageCount >= 2,
                                progress = (chatMessageCount.toFloat() / 2f).coerceIn(0f, 1f),
                                progressText = "$chatMessageCount/2 Msgs",
                                rewardXp = 100,
                                icon = "💬"
                            ),
                            SimAchievement(
                                title = "Garantia de Foco",
                                description = "Silenciou atletas com /mutar",
                                isCompleted = mutedCount >= 1,
                                progress = (mutedCount.toFloat() / 1f).coerceIn(0f, 1f),
                                progressText = "$mutedCount/1 Mutado",
                                rewardXp = 200,
                                icon = "🔇"
                            ),
                            SimAchievement(
                                title = "Veterano Calejado",
                                description = "Chegou ao nível 25 de prestígio",
                                isCompleted = userLevel >= 25,
                                progress = (userLevel.toFloat() / 25f).coerceIn(0f, 1f),
                                progressText = "$userLevel/25 Nvl",
                                rewardXp = 500,
                                icon = "🥇"
                            )
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(listQuests) { quest ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF131A26), RoundedCornerShape(6.dp))
                                        .border(1.dp, if (quest.isCompleted) Color(0xFFD97706) else Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(quest.icon, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(quest.title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (quest.isCompleted) Color(0xFFEAB308) else Color.White)
                                            Text("${quest.rewardXp} XP", fontSize = 7.5.sp, color = Color(0xFFFFB300), fontFamily = FontFamily.Monospace)
                                        }
                                        Text(quest.description, fontSize = 7.5.sp, color = Color(0xFF94A3B8))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .background(Color(0xFF0F172A), RoundedCornerShape(1.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(quest.progress)
                                                    .fillMaxHeight()
                                                    .background(if (quest.isCompleted) Color(0xFF10B981) else Color(0xFF3B82F6), RoundedCornerShape(1.dp))
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (quest.isCompleted) "⭐" else "⏳",
                                        fontSize = 11.sp
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

data class SimAchievement(
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val progress: Float,
    val progressText: String,
    val rewardXp: Int,
    val icon: String
)

@Composable
fun SimRankingScreen(
    phoneCard: Color,
    phoneTextPrimary: Color,
    phoneTextSecondary: Color,
    rankings: List<SimRank>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Text("RANKING GLOBAL DE FAIXAS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = phoneTextPrimary)
        Text("Os atletas com maior experiência global no JiuVerse.", fontSize = 9.sp, color = phoneTextSecondary)

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(rankings) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(phoneCard, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (item.pos == 1) "🥇" else if (item.pos == 2) "🥈" else if (item.pos == 3) "🥉" else "${item.pos}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = phoneTextPrimary)
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (item.belt) {
                                            "Preta" -> Color(0xFF1E293B)
                                            "Marrom" -> Color(0xFF78350F)
                                            "Azul" -> Color(0xFF2563EB)
                                            else -> phoneTextSecondary
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("FAIXA ${item.belt.uppercase()}", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    Text("${item.xp} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                }
            }
        }
    }
}

@Composable
fun SimFriendsScreen(
    phoneCard: Color,
    phoneTextPrimary: Color,
    phoneTextSecondary: Color,
    phoneInputBg: Color,
    friends: List<SimFriend>,
    onAdd: (String) -> Unit,
    onRemove: (Int) -> Unit
) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Text("COLEGAS DE TATAME", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = phoneTextPrimary)
        Text("Gerencie sua rede social de rolas síncronos e chats.", fontSize = 9.sp, color = phoneTextSecondary)

        Spacer(modifier = Modifier.height(10.dp))

        // Invite form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = phoneCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimTextInput(
                    value = query,
                    onValueChange = { query = it },
                    bg = phoneInputBg,
                    txtColor = phoneTextPrimary,
                    holder = "Nome do avatar...",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        if (query.isNotEmpty()) {
                            onAdd(query)
                            query = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6)),
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("CONVIDAR", fontSize = 8.sp, color = Color(0xFF030712), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(friends) { idx, friend ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(phoneCard, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (friend.status == "OFFLINE") Color.Gray
                                    else if (friend.status == "ONLINE") Color(0xFF10B981)
                                    else Color(0xFF06B6D4),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(friend.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = phoneTextPrimary)
                            Text("Faixa ${friend.belt} • ${friend.status}", fontSize = 8.sp, color = phoneTextSecondary)
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remover",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onRemove(idx) }
                    )
                }
            }
        }
    }
}

@Composable
fun SimMessagesScreen(
    phoneCard: Color,
    phoneTextPrimary: Color,
    phoneTextSecondary: Color,
    phoneInputBg: Color,
    msgs: List<SimMsg>,
    mutedUsers: List<String>,
    onSend: (String, String, String?) -> Unit,
    onMuteToggle: (String) -> Unit
) {
    var tmsg by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf("LOCAL") } // "LOCAL", "ACADEMIA", "PRIVADA"
    var recipientName by remember { mutableStateOf("") }
    
    // Filter messages to exclude muted senders
    val filteredMsgs = msgs.filter { !mutedUsers.contains(it.sender) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp)
    ) {
        // Channel Selector chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val channels = listOf("LOCAL", "ACADEMIA", "PRIVADA")
            channels.forEach { ch ->
                val isSelected = selectedChannel == ch
                val btnBg = if (isSelected) {
                    when (ch) {
                        "LOCAL" -> Color(0xFFEAB308)
                        "ACADEMIA" -> Color(0xFF3B82F6)
                        else -> Color(0xFFEC4899)
                    }
                } else Color(0xFF1E293B)
                
                val btnFg = if (isSelected) Color(0xFF030712) else Color(0xFF94A3B8)

                Box(
                    modifier = Modifier
                        .background(btnBg, RoundedCornerShape(4.dp))
                        .clickable { selectedChannel = ch }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (ch) {
                            "LOCAL" -> "LOCAL 📍"
                            "ACADEMIA" -> "ACADEMIA 🥋"
                            else -> "PRIVADA 🔒"
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = btnFg
                    )
                }
            }
        }

        // PM Recipient row if PRIVADA is selected
        if (selectedChannel == "PRIVADA") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Para (Nick): ", fontSize = 9.sp, color = phoneTextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                SimTextInput(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    bg = phoneInputBg,
                    txtColor = phoneTextPrimary,
                    holder = "Ex: GraciePassador",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Muted Users notification row
        if (mutedUsers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(Color(0xFF3F1F1F), RoundedCornerShape(4.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Muteados: ${mutedUsers.take(3).joinToString()}${if (mutedUsers.size > 3) "..." else ""}",
                    fontSize = 8.sp,
                    color = Color(0xFFFCA5A5)
                )
                Text(
                    text = "DESMUTAR TODOS",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.clickable {
                        mutedUsers.forEach { onMuteToggle(it) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredMsgs) { msg ->
                val alignMe = msg.isMe
                val channelLabel = when (msg.channel) {
                    "ACADEMIA" -> "[ACADEMIA]"
                    "PRIVADA" -> if (msg.isMe) "[Para ${msg.recipient ?: "Alguém"}]" else "[De ${msg.sender}]"
                    else -> "[LOCAL]"
                }
                val channelColor = when (msg.channel) {
                    "ACADEMIA" -> Color(0xFF60A5FA)
                    "PRIVADA" -> Color(0xFFF472B6)
                    else -> Color(0xFFFBBF24)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (alignMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (alignMe) Color(0xFF0F766E).copy(alpha = 0.9f) else phoneCard,
                                RoundedCornerShape(8.dp)
                            )
                            .border(0.5.dp, if (alignMe) Color(0xFF14B8A6) else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .widthIn(max = 210.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = channelLabel + " ",
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = channelColor
                                    )
                                    Text(
                                        msg.sender,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (alignMe) Color(0xFF2DD4BF) else Color(0xFF06B6D4)
                                    )
                                }
                                
                                // Show mute action icon if it is NOT my message
                                if (!alignMe && msg.sender != "SISTEMA") {
                                    Text(
                                        "🔇 Mute",
                                        fontSize = 7.5.sp,
                                        color = Color(0xFFEF4444),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clickable { onMuteToggle(msg.sender) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(msg.content, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Typing bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(phoneCard)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimTextInput(
                value = tmsg,
                onValueChange = { tmsg = it },
                bg = phoneInputBg,
                txtColor = phoneTextPrimary,
                holder = when (selectedChannel) {
                    "ACADEMIA" -> "Falar com a equipe..."
                    "PRIVADA" -> "Enviar PM secreta..."
                    else -> "Falar no dōjō local..."
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF14B8A6), RoundedCornerShape(6.dp))
                    .clickable {
                        if (tmsg.isNotEmpty()) {
                            onSend(tmsg, selectedChannel, recipientName.takeIf { selectedChannel == "PRIVADA" && it.isNotEmpty() })
                            tmsg = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = Color(0xFF030712),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SimProfileScreen(
    phoneCard: Color,
    phoneTextPrimary: Color,
    phoneTextSecondary: Color,
    nickname: String,
    email: String,
    belt: String,
    xp: Int,
    coins: Int,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = phoneCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF1E293B), CircleShape)
                        .border(1.dp, Color(0xFF06B6D4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🥋", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(nickname, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = phoneTextPrimary)
                Text(email, fontSize = 9.sp, color = phoneTextSecondary)

                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .background(Color(0xFF2563EB), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("FAIXA ${belt.uppercase()} • 4 GRAUS", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = phoneCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("ESTATÍSTICAS DA CONTA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Experiência (XP)", fontSize = 11.sp, color = phoneTextSecondary)
                    Text("$xp XP", fontSize = 11.sp, color = phoneTextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Moeda (JiuCoins)", fontSize = 11.sp, color = phoneTextSecondary)
                    Text("$ $coins JC", fontSize = 11.sp, color = Color(0xFFEAB308), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D))
        ) {
            Text("SAIR DO APLICATIVO (LOGOUT)", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// Custom simple safe text input for mobile screen simulation
@Composable
fun SimTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    bg: Color,
    txtColor: Color,
    holder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardOptions = KeyboardOptions.Default
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(6.dp))
            .border(0.5.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        if (value.isEmpty()) {
            Text(holder, color = Color.Gray, fontSize = 11.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = txtColor, fontSize = 11.sp),
            cursorBrush = SolidColor(Color(0xFF06B6D4)),
            keyboardOptions = keyboardType,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
