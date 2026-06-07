package com.example.architecture.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.ConsoleMessage
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*

// Data structures for our Premium MMORPG Hub
data class SimFriend(val id: String, val name: String, val belt: String, val status: String)
data class SimItem(val id: String, val name: String, val type: String, val rarity: String, val price: Int, var equipped: Boolean)
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
    // Collect entities from the Room SQLite Database via ViewModel to remain fully synced!
    val playerMemoryState by viewModel.playerMemory.collectAsState()
    val playerMemory = playerMemoryState ?: com.example.architecture.database.PlayerMemoryEntity()
    val studentsState by viewModel.allStudentsState.collectAsState()

    // Interactive synchronized properties
    var userNickname by remember { mutableStateOf(playerMemory.playerName) }
    var userBelt by remember { mutableStateOf(playerMemory.playerBelt) }
    var userXp by remember { mutableStateOf(playerMemory.playerXp) }
    var userLevel by remember { mutableStateOf(playerMemory.playerLevel) }
    var userAcademy by remember { mutableStateOf(playerMemory.academyName) }
    var userCoins by remember { mutableStateOf(1650) } // Initial currency: JinCoins 🪙

    LaunchedEffect(playerMemory) {
        userNickname = playerMemory.playerName
        userBelt = playerMemory.playerBelt
        userXp = playerMemory.playerXp
        userLevel = playerMemory.playerLevel
        userAcademy = playerMemory.academyName
    }

    // Positions and environments
    var playerX by remember { mutableStateOf(6) }
    var playerY by remember { mutableStateOf(8) }
    var selectedEnvironment by remember { mutableStateOf("Academia Carlson Gracie") }
    var useWebPhaser by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    
    // Webview diagnostic states for loading screens and error fallbacks
    var webViewLoadingState by remember { mutableStateOf(true) }
    var webViewErrorState by remember { mutableStateOf<String?>(null) }
    val jsLogs = remember { mutableStateListOf<String>() }
    var showConsoleLogs by remember { mutableStateOf(false) }
    
    // Bottom navigation panel active tab descriptor: "INV", "SHOP", "QUESTS", "ACADEMY"
    var activeBottomTab by remember { mutableStateOf("INV") }

    // Dynamic Lists managing gameplay state
    val simFriends = remember {
        mutableStateListOf(
            SimFriend("1", "GuardaFechada99", "Roxa", "ONLINE"),
            SimFriend("2", "LeandroLoFan", "Preta", "TREINANDO"),
            SimFriend("3", "FaixaAzulMundial", "Azul", "SPARRING"),
            SimFriend("4", "RaspagemPorBaixo", "Marrom", "OFFLINE")
        )
    }

    val simInventory = remember {
        mutableStateListOf(
            SimItem("1", "Kimono Azul de Competição", "KIMONO", "EPICO", 450, true),
            SimItem("2", "Faixa Preta Master Linha Ouro", "FAIXA", "LENDARIO", 1200, false),
            SimItem("3", "Jaqueta Corta Vento BJJ 2026", "JAQUETA", "RARO", 350, false),
            SimItem("4", "Boné Oficial JiuVerse Aba Reta", "BONÉ", "COMUM", 120, false),
            SimItem("5", "Mochila Arena Roll-Top", "MOCHILA", "RARO", 280, true)
        )
    }

    val simShopItems = remember {
        listOf(
            SimItem("s1", "Kimono Campeão ADCC Holográfico", "KIMONO", "LENDARIO", 1000, false),
            SimItem("s2", "Medalha de Honra ao Mérito", "MEDALHA", "EPICO", 500, false),
            SimItem("s3", "Faixa Coral Lendária Mestre", "FAIXA", "LENDARIO", 1500, false),
            SimItem("s4", "Jaqueta Imperial Veludo", "JAQUETA", "EPICO", 650, false),
            SimItem("s5", "Touca Zen Terapia", "BONÉ", "COMUM", 100, false),
            SimItem("s6", "Saco Transversal de Treino", "MOCHILA", "COMUM", 150, false)
        )
    }

    val simMessages = remember {
        mutableStateListOf(
            SimMsg("Sensei Carlson", "OSS! Seja bem-vindo à nossa academia virtual no JiuVerse. Use as setas ou o joystick para caminhar.", "22:42", false, "LOCAL"),
            SimMsg("GuardaFechada99", "O treino hoje na PvP Arena vai ser sinistro! Quem vem?", "22:44", false, "GLOBAL")
        )
    }

    var chatInputField by remember { mutableStateOf("") }
    val mutedUsers = remember { mutableStateListOf<String>() }

    // Roster of students extracted directly from the SQLite database
    val databaseRoster = remember(studentsState) {
        studentsState.map { student ->
            SimFriend(
                id = student.studentId.toString(),
                name = student.virtualNickname.ifEmpty { student.name },
                belt = student.belt,
                status = if (student.studentId % 2 == 0) "ONLINE" else "OFFLINE"
            )
        }
    }

    // Custom helper functions
    fun postChat(msgText: String) {
        if (msgText.isBlank()) return
        
        // Append user chat message
        simMessages.add(
            SimMsg(
                sender = userNickname.ifEmpty { "Lutador" },
                content = msgText,
                time = "22:45",
                isMe = true,
                channel = "LOCAL"
            )
        )
        
        // Dynamic NPC response simulator to make the MMORPG highly responsive and alive
        if (msgText.uppercase().contains("OSS") || msgText.uppercase().contains("TREINO") || msgText.uppercase().contains("OLA")) {
            simMessages.add(
                SimMsg(
                    sender = "Mestre Carlson",
                    content = "Treino focado, mente forte! OSS, ${userNickname.ifEmpty { "Lutador" }}!",
                    time = "22:46",
                    isMe = false,
                    channel = "LOCAL"
                )
            )
        }
    }

    // Render columns in premium dark blueprint MMORPG aesthetics
    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .background(BlueprintBg)
            .padding(8.dp)
    ) {
        // --- HUD HEADER BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlueprintHeader, RoundedCornerShape(8.dp))
                .border(2.dp, BlueprintCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(BlueprintCyan, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "JIUVERSE RPG: PREMIUM MMO HUD",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = BlueprintTextPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF132D28), RoundedCornerShape(4.dp))
                        .border(0.5.dp, BlueprintTeal, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LATENCY: 14ms (STABLE)",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintTeal,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Engine Toggle & Day/Night indicators
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                        .border(1.dp, BlueprintGridLine, RoundedCornerShape(6.dp))
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(if (useWebPhaser) BlueprintCyan.copy(alpha = 0.25f) else Color.Transparent, RoundedCornerShape(4.dp))
                            .clickable { useWebPhaser = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🎮 PHASER 3", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (useWebPhaser) BlueprintCyan else BlueprintTextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .background(if (!useWebPhaser) BlueprintTeal.copy(alpha = 0.25f) else Color.Transparent, RoundedCornerShape(4.dp))
                            .clickable { useWebPhaser = false }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🕹️ SIM COMPOSE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (!useWebPhaser) BlueprintTeal else BlueprintTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // JinCoins Wallet Display
                Row(
                    modifier = Modifier
                        .background(Color(0xFF2D1E0F), RoundedCornerShape(6.dp))
                        .border(1.dp, BlueprintOrange, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🪙", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "J$ $userCoins",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = BlueprintOrange,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- CORE MMORPG SCREEN GRID (LEFT, CENTER, RIGHT) ---
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            
            // ==================== COLUMN 1: LEFT PANEL (PROFILE & MINI MAP) ====================
            Column(
                modifier = Modifier
                    .width(190.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Fighter Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                    border = BorderStroke(1.dp, BlueprintCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(getBeltColor(userBelt), CircleShape)
                                    .border(1.5.dp, BlueprintTextPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🥋", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = userNickname.ifEmpty { "Aspirante" },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueprintTextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Faixa $userBelt",
                                    fontSize = 8.sp,
                                    color = BlueprintCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dynamic XP levels gauge progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LVL $userLevel", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            Text("XP $userXp/1000", fontSize = 7.sp, color = BlueprintTextSecondary, fontFamily = FontFamily.Monospace)
                        }
                        
                        Spacer(modifier = Modifier.height(3.dp))
                        
                        LinearProgressIndicator(
                            progress = { (userXp % 1000) / 1000f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = BlueprintCyan,
                            trackColor = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick belt promotions triggers to test dynamic graphics modifications
                        Text("CLASSE DE FAIXA (BJJ):", fontSize = 7.sp, fontWeight = FontWeight.Black, color = BlueprintTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            listOf("Branca", "Azul", "Roxa", "Marrom", "Preta").forEach { belt ->
                                val isActive = userBelt == belt
                                val beltCol = getBeltColor(belt)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(14.dp)
                                        .background(
                                            if (isActive) beltCol else beltCol.copy(alpha = 0.4f),
                                            RoundedCornerShape(2.dp)
                                        )
                                        .border(
                                            0.5.dp,
                                            if (isActive) Color.White else Color.Transparent,
                                            RoundedCornerShape(2.dp)
                                        )
                                        .clickable { 
                                            userBelt = belt 
                                            viewModel.savePlayerMemory(playerMemory.copy(playerBelt = belt))
                                            // Trigger JS evaluation if WebView active
                                            webViewRef?.evaluateJavascript("javascript:if(window.changePlayerBelt){window.changePlayerBelt('$belt');}", null)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = belt.first().toString(),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Vector Mini Map Device
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                    border = BorderStroke(1.dp, BlueprintGridLine)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🌐 MINI MAPA VETORIAL",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintTextPrimary
                        )
                        Text(
                            text = "Coordenadas: ($playerX, $playerY) GPS",
                            fontSize = 7.sp,
                            color = BlueprintTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))

                        // Canvas drawing high-fidelity isometric radar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                                .border(1.dp, BlueprintGridLine, RoundedCornerShape(6.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw radial radar circles
                                drawCircle(
                                    color = BlueprintCyan.copy(alpha = 0.15f),
                                    radius = w * 0.4f,
                                    center = androidx.compose.ui.geometry.Offset(w/2, h/2)
                                )
                                drawCircle(
                                    color = BlueprintCyan.copy(alpha = 0.05f),
                                    radius = w * 0.2f,
                                    center = androidx.compose.ui.geometry.Offset(w/2, h/2)
                                )
                                // Crosslines
                                drawLine(
                                    color = BlueprintGridLine,
                                    start = androidx.compose.ui.geometry.Offset(0f, h/2),
                                    end = androidx.compose.ui.geometry.Offset(w, h/2),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = BlueprintGridLine,
                                    start = androidx.compose.ui.geometry.Offset(w/2, 0f),
                                    end = androidx.compose.ui.geometry.Offset(w/2, h),
                                    strokeWidth = 1f
                                )

                                // Current active environment map boundaries schematic lines
                                drawRect(
                                    color = BlueprintCyan.copy(alpha = 0.1f),
                                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.2f),
                                    size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.6f)
                                )

                                // Glowing active dot representing player coords
                                val pX = w * 0.2f + (playerX / 15f) * (w * 0.6f)
                                val pY = h * 0.2f + (playerY / 15f) * (h * 0.6f)
                                drawCircle(
                                    color = BlueprintOrange,
                                    radius = 5f,
                                    center = androidx.compose.ui.geometry.Offset(pX, pY)
                                )
                                // Compass indicator
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.5f,
                                    center = androidx.compose.ui.geometry.Offset(pX, pY)
                                )
                            }
                        }
                    }
                }
            }

            // ==================== COLUMN 2: CENTER VIEWPORT (MUNDO ISOMÉTRICO) ====================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Environment Selector Panel (Modular swapping of 10 environments requested!)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                    border = BorderStroke(1.dp, BlueprintGridLine)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📍 TELEPORTE / VIAGEM RÁPIDA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            Text("AMBIENTE SELECIONADO: ${selectedEnvironment.uppercase()}", fontSize = 7.1.sp, color = BlueprintOrange, fontWeight = FontWeight.Black)
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))

                        val topRooms = listOf("Praça Central", "Academia Carlson Gracie", "Arena PvP", "Loja Oficial", "Hall da Fama")
                        val bottomRooms = listOf("Vestiários", "Casas", "Apartamentos", "Escritórios", "Salas VIP")

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            // Row 1 of quick traveling
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                topRooms.forEach { room ->
                                    val isCurrent = selectedEnvironment == room
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isCurrent) Color(0xFF1E293B) else Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, if (isCurrent) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .clickable { 
                                                selectedEnvironment = room 
                                                webViewRef?.evaluateJavascript("javascript:if(window.changeEnvironment){window.changeEnvironment('$room');}", null)
                                            }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = room.replace("Academia ", "").replace("Oficial", "").uppercase(),
                                            fontSize = 6.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) BlueprintCyan else BlueprintTextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Row 2 of quick traveling
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                bottomRooms.forEach { room ->
                                    val isCurrent = selectedEnvironment == room
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isCurrent) Color(0xFF1E293B) else Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, if (isCurrent) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .clickable { 
                                                selectedEnvironment = room 
                                                webViewRef?.evaluateJavascript("javascript:if(window.changeEnvironment){window.changeEnvironment('$room');}", null)
                                            }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = room.uppercase(),
                                            fontSize = 6.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) BlueprintCyan else BlueprintTextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Isometric Canvas Box Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF020617), RoundedCornerShape(8.dp))
                        .border(1.5.dp, BlueprintCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (useWebPhaser) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Core Interactive Phaser 3 HTML5 Game Engine Simulator Webview!
                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                                super.onPageStarted(view, url, favicon)
                                                webViewLoadingState = true
                                                webViewErrorState = null
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                webViewLoadingState = false
                                            }

                                            override fun onReceivedError(
                                                view: WebView?,
                                                request: WebResourceRequest?,
                                                error: WebResourceError?
                                            ) {
                                                super.onReceivedError(view, request, error)
                                                if (request?.isForMainFrame == true) {
                                                    webViewErrorState = error?.description?.toString() ?: "Erro ao carregar o simulador"
                                                    webViewLoadingState = false
                                                }
                                            }
                                        }

                                        webChromeClient = object : WebChromeClient() {
                                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                                consoleMessage?.let {
                                                    val logMsg = "[${it.messageLevel()}] ${it.message()} (L${it.lineNumber()})"
                                                    jsLogs.add(logMsg)
                                                    android.util.Log.d("JiuVersePhaserConsole", logMsg)
                                                }
                                                return true
                                            }
                                        }

                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.useWideViewPort = true
                                        settings.loadWithOverviewMode = true
                                        settings.mixedContentMode = 0 // MIXED_CONTENT_ALWAYS_ALLOW

                                        addJavascriptInterface(object : Any() {
                                            @JavascriptInterface
                                            fun postMessage(message: String) {
                                                try {
                                                    val json = org.json.JSONObject(message)
                                                    val type = json.optString("type")
                                                    if (type == "PLAYER_MOVE") {
                                                        playerX = json.optInt("x")
                                                        playerY = json.optInt("y")
                                                    }
                                                    if (type == "ENV_CHANGE") {
                                                        selectedEnvironment = json.optString("env")
                                                        playerX = json.optInt("x")
                                                        playerY = json.optInt("y")
                                                    }
                                                    if (type == "CHAT_MESSAGE") {
                                                        val sender = json.optString("sender")
                                                        val content = json.optString("content")
                                                        simMessages.add(
                                                            SimMsg(sender, content, "22:45", false, "LOCAL")
                                                        )
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }, "AndroidWebView")

                                        // EMBEDDED PHASER 3 ISO ENGINE (SELF-CONTAINED VEKTOR TILESETS AND COLLISION)
                                    val phaserHtml = """
                                        <!DOCTYPE html>
                                        <html>
                                        <head>
                                            <meta charset="UTF-8">
                                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                                            <style>
                                                body { margin: 0; padding: 0; background-color: #020617; overflow: hidden; width: 100vw; height: 100vh; }
                                                canvas { display: block; width: 100%; height: 100%; }
                                                #canvas-fallback {
                                                    display: none;
                                                    position: absolute;
                                                    top: 0;
                                                    left: 0;
                                                    width: 100%;
                                                    height: 100%;
                                                    background-color: #020617;
                                                }
                                            </style>
                                            <script src="https://cdnjs.cloudflare.com/ajax/libs/phaser/3.60.0/phaser.min.js"></script>
                                        </head>
                                        <body>
                                            <canvas id="canvas-fallback"></canvas>

                                            <script>
                                                // 64x32 Dimetric Isometric Grid (2:1 projection) Config
                                                const T_WIDTH = 64;
                                                const T_HEIGHT = 32;
                                                const GRID_SIZE = 15;
                                                const Z_STEP = 16;

                                                let playerX = 6;
                                                let playerY = 8;
                                                let currentEnv = 'Academia Carlson Gracie';
                                                let playerBelt = 'Branca';
                                                let engineType = 'PHASER'; // 'PHASER' or 'CANVAS'

                                                const environmentsCycle = [
                                                    'Praça Central',
                                                    'Academia Carlson Gracie',
                                                    'Arena PvP',
                                                    'Loja Oficial',
                                                    'Hall da Fama',
                                                    'Casas Futuras'
                                                ];

                                                class JiuVerseIsoGame extends Phaser.Scene {
                                                    constructor() {
                                                        super('JiuVerseIsoGame');
                                                    }

                                                    preload() {
                                                         this.createProceduralTextures();
                                                     }

                                                     createProceduralTextures() {
                                                         const makeTile = (name, drawFn) => {
                                                             const canvas = document.createElement('canvas');
                                                             canvas.width = 64;
                                                             canvas.height = 32;
                                                             const ctx = canvas.getContext('2d');
                                                             
                                                             ctx.beginPath();
                                                             ctx.moveTo(32, 0);
                                                             ctx.lineTo(64, 16);
                                                             ctx.lineTo(32, 32);
                                                             ctx.lineTo(0, 16);
                                                             ctx.closePath();
                                                             ctx.clip();
                                                             
                                                             drawFn(ctx);
                                                             this.textures.addCanvas(name, canvas);
                                                         };

                                                         const makeBlock = (name, drawFn) => {
                                                             const canvas = document.createElement('canvas');
                                                             canvas.width = 64;
                                                             canvas.height = 80;
                                                             const ctx = canvas.getContext('2d');
                                                             drawFn(ctx);
                                                             this.textures.addCanvas(name, canvas);
                                                         };

                                                         // --- PISOS ---
                                                         makeTile('floor_stone_brick', (ctx) => {
                                                             ctx.fillStyle = '#475569';
                                                             ctx.fillRect(0, 0, 64, 32);
                                                             ctx.strokeStyle = '#334155';
                                                             ctx.lineWidth = 1;
                                                             for (let i = -32; i < 96; i += 8) {
                                                                 ctx.beginPath(); ctx.moveTo(i, -16); ctx.lineTo(i - 32, 48); ctx.stroke();
                                                                 ctx.beginPath(); ctx.moveTo(i, -16); ctx.lineTo(i + 32, 48); ctx.stroke();
                                                             }
                                                         });

                                                         makeTile('floor_wood_plank', (ctx) => {
                                                             ctx.fillStyle = '#78350f';
                                                             ctx.fillRect(0, 0, 64, 32);
                                                             ctx.strokeStyle = '#451a03';
                                                             ctx.lineWidth = 1.5;
                                                             for (let y = 0; y < 32; y += 4) {
                                                                 ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(64, y); ctx.stroke();
                                                             }
                                                             ctx.strokeStyle = '#1e0801';
                                                             for (let x = 8; x < 64; x += 16) {
                                                                 ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, 32); ctx.stroke();
                                                             }
                                                         });

                                                         makeTile('floor_checker_blue', (ctx) => {
                                                             ctx.fillStyle = '#1e3a8a';
                                                             ctx.fillRect(0, 0, 64, 32);
                                                             ctx.fillStyle = '#f8fafc';
                                                             ctx.beginPath();
                                                             ctx.moveTo(32, 0); ctx.lineTo(64, 16); ctx.lineTo(32, 16);
                                                             ctx.closePath(); ctx.fill();
                                                             ctx.beginPath();
                                                             ctx.moveTo(32, 16); ctx.lineTo(0, 16); ctx.lineTo(32, 32);
                                                             ctx.closePath(); ctx.fill();
                                                             ctx.strokeStyle = '#d97706';
                                                             ctx.lineWidth = 1.5;
                                                             ctx.beginPath();
                                                             ctx.moveTo(32, 0); ctx.lineTo(64, 16); ctx.lineTo(32, 32); ctx.lineTo(0, 16);
                                                             ctx.closePath(); ctx.stroke();
                                                         });

                                                         makeTile('floor_checker_grey', (ctx) => {
                                                             ctx.fillStyle = '#cbd5e1';
                                                             ctx.fillRect(0, 0, 64, 32);
                                                             ctx.fillStyle = '#64748b';
                                                             ctx.beginPath();
                                                             ctx.moveTo(32, 0); ctx.lineTo(48, 8); ctx.lineTo(32, 16); ctx.lineTo(16, 8);
                                                             ctx.closePath(); ctx.fill();
                                                             ctx.beginPath();
                                                             ctx.moveTo(32, 16); ctx.lineTo(48, 24); ctx.lineTo(32, 32); ctx.lineTo(16, 24);
                                                             ctx.closePath(); ctx.fill();
                                                         });

                                                         makeTile('floor_mud', (ctx) => {
                                                             ctx.fillStyle = '#451a03';
                                                             ctx.fillRect(0, 0, 64, 32);
                                                             ctx.fillStyle = '#78716c';
                                                             ctx.beginPath(); ctx.arc(15, 8, 3, 0, Math.PI*2); ctx.fill();
                                                             ctx.beginPath(); ctx.arc(48, 20, 4, 0, Math.PI*2); ctx.fill();
                                                         });

                                                         makeTile('nature_grass_clove', (ctx) => {
                                                             ctx.fillStyle = '#15803d';
                                                             ctx.fillRect(0, 0, 64, 32);
                                                             ctx.fillStyle = '#4ade80';
                                                             const drawClover = (cx, cy) => {
                                                                 ctx.beginPath();
                                                                 ctx.arc(cx - 2, cy, 2, 0, Math.PI*2);
                                                                 ctx.arc(cx + 2, cy, 2, 0, Math.PI*2);
                                                                 ctx.arc(cx, cy - 2, 2, 0, Math.PI*2);
                                                                 ctx.fill();
                                                             };
                                                             drawClover(12, 10);
                                                             drawClover(45, 22);
                                                             drawClover(25, 24);
                                                         });

                                                         makeTile('nature_water', (ctx) => {
                                                             ctx.fillStyle = '#0f766e';
                                                             ctx.fillRect(0, 0, 64, 32);
                                                             ctx.strokeStyle = '#2dd4bf';
                                                             ctx.lineWidth = 1;
                                                             ctx.beginPath();
                                                             ctx.arc(16, 8, 8, 0, Math.PI/3);
                                                             ctx.arc(48, 8, 12, 0, Math.PI/4);
                                                             ctx.stroke();
                                                         });

                                                         // --- EXTRUSÕES 3D (WALLS & ITEMS) ---
                                                         const drawFacade = (ctx, cl, cr, ct) => {
                                                             // Top cap
                                                             ctx.fillStyle = ct;
                                                             ctx.beginPath();
                                                             ctx.moveTo(32, 0); ctx.lineTo(64, 16); ctx.lineTo(32, 32); ctx.lineTo(0, 16);
                                                             ctx.closePath(); ctx.fill();
                                                             // Left Facade
                                                             ctx.fillStyle = cl;
                                                             ctx.beginPath();
                                                             ctx.moveTo(0, 16); ctx.lineTo(32, 32); ctx.lineTo(32, 80); ctx.lineTo(0, 64);
                                                             ctx.closePath(); ctx.fill();
                                                             // Right Facade
                                                             ctx.fillStyle = cr;
                                                             ctx.beginPath();
                                                             ctx.moveTo(32, 32); ctx.lineTo(64, 16); ctx.lineTo(64, 64); ctx.lineTo(32, 80);
                                                             ctx.closePath(); ctx.fill();
                                                         };

                                                         makeBlock('wall_cobble', (ctx) => {
                                                             drawFacade(ctx, '#1e293b', '#334155', '#475569');
                                                             ctx.strokeStyle = '#0f172a';
                                                             ctx.lineWidth = 1;
                                                             ctx.beginPath();
                                                             ctx.arc(12, 45, 5, 0, Math.PI*2);
                                                             ctx.arc(48, 48, 6, 0, Math.PI*2);
                                                             ctx.stroke();
                                                         });

                                                         makeBlock('wall_brick_red', (ctx) => {
                                                             drawFacade(ctx, '#7c2d12', '#9a3412', '#b45309');
                                                             ctx.strokeStyle = '#451a03';
                                                             ctx.lineWidth = 1;
                                                             for (let y = 24; y < 80; y += 8) {
                                                                 ctx.beginPath(); ctx.moveTo(0, y - 8); ctx.lineTo(32, y); ctx.stroke();
                                                                 ctx.beginPath(); ctx.moveTo(32, y); ctx.lineTo(64, y - 8); ctx.stroke();
                                                             }
                                                         });

                                                         makeBlock('wall_panel_wood', (ctx) => {
                                                             drawFacade(ctx, '#451a03', '#78350f', '#a16207');
                                                             ctx.strokeStyle = '#1c0a00';
                                                             ctx.lineWidth = 1.5;
                                                             for (let x = 8; x < 32; x += 8) {
                                                                 ctx.beginPath(); ctx.moveTo(x, 16 + x/2); ctx.lineTo(x, 64 + x/2); ctx.stroke();
                                                             }
                                                             for (let x = 40; x < 64; x += 8) {
                                                                 ctx.beginPath(); ctx.moveTo(x, 32 - (x-32)/2); ctx.lineTo(x, 80 - (x-32)/2); ctx.stroke();
                                                             }
                                                         });

                                                         makeBlock('door_wood_stud', (ctx) => {
                                                             drawFacade(ctx, '#27272a', '#3f1b07', '#18181b');
                                                             ctx.fillStyle = '#b45309';
                                                             ctx.beginPath();
                                                             ctx.moveTo(36, 34); ctx.lineTo(60, 22); ctx.lineTo(60, 68); ctx.lineTo(36, 80);
                                                             ctx.closePath(); ctx.fill();
                                                             ctx.strokeStyle = '#451a03';
                                                             ctx.stroke();
                                                         });

                                                         makeBlock('door_bars', (ctx) => {
                                                             drawFacade(ctx, '#0f172a', '#1e293b', '#334155');
                                                             ctx.fillStyle = '#020617';
                                                             ctx.beginPath();
                                                             ctx.moveTo(36, 34); ctx.lineTo(60, 22); ctx.lineTo(60, 62); ctx.lineTo(36, 74);
                                                             ctx.closePath(); ctx.fill();
                                                             ctx.strokeStyle = '#94a3b8';
                                                             ctx.lineWidth = 2;
                                                             ctx.beginPath();
                                                             ctx.moveTo(48, 23); ctx.lineTo(48, 68);
                                                             ctx.stroke();
                                                         });

                                                         makeBlock('door_arched', (ctx) => {
                                                             drawFacade(ctx, '#27272a', '#020617', '#18181b');
                                                             ctx.fillStyle = '#5c2b09';
                                                             ctx.beginPath();
                                                             ctx.moveTo(34, 76); ctx.lineTo(34, 38);
                                                             ctx.bezierCurveTo(34, 24, 62, 12, 62, 26);
                                                             ctx.lineTo(62, 64);
                                                             ctx.closePath(); ctx.fill();
                                                         });

                                                         makeBlock('window_arched_glass', (ctx) => {
                                                             drawFacade(ctx, '#3f1b07', '#5c2b09', '#1a0d00');
                                                             ctx.fillStyle = '#38bdf8';
                                                             ctx.beginPath();
                                                             ctx.moveTo(38, 54); ctx.lineTo(38, 38);
                                                             ctx.bezierCurveTo(38, 28, 58, 16, 58, 28);
                                                             ctx.lineTo(58, 44);
                                                             ctx.closePath(); ctx.fill();
                                                             ctx.strokeStyle = '#fbbf24';
                                                             ctx.stroke();
                                                         });

                                                         makeBlock('window_bar_jail', (ctx) => {
                                                             drawFacade(ctx, '#1e293b', '#334155', '#475569');
                                                             ctx.fillStyle = '#0f172a';
                                                             ctx.beginPath();
                                                             ctx.moveTo(38, 28); ctx.lineTo(58, 18); ctx.lineTo(58, 48); ctx.lineTo(38, 58);
                                                             ctx.closePath(); ctx.fill();
                                                         });

                                                         makeBlock('window_cozy_glow', (ctx) => {
                                                             drawFacade(ctx, '#7c2d12', '#9a3412', '#b45309');
                                                             ctx.fillStyle = '#fef08a';
                                                             ctx.beginPath();
                                                             ctx.moveTo(38, 28); ctx.lineTo(58, 18); ctx.lineTo(58, 48); ctx.lineTo(38, 58);
                                                             ctx.closePath(); ctx.fill();
                                                             ctx.strokeStyle = '#451a03';
                                                             ctx.stroke();
                                                         });

                                                         makeBlock('window_boarded', (ctx) => {
                                                             drawFacade(ctx, '#1e293b', '#334155', '#475569');
                                                             ctx.fillStyle = '#18181b';
                                                             ctx.beginPath();
                                                             ctx.moveTo(38, 28); ctx.lineTo(58, 18); ctx.lineTo(58, 48); ctx.lineTo(38, 58);
                                                             ctx.closePath(); ctx.fill();
                                                         });

                                                         makeBlock('roof_slate_blue', (ctx) => {
                                                             ctx.fillStyle = '#1e293b';
                                                             ctx.beginPath();
                                                             ctx.moveTo(32, 0); ctx.lineTo(64, 48); ctx.lineTo(32, 64); ctx.lineTo(0, 48);
                                                             ctx.closePath(); ctx.fill();
                                                             ctx.strokeStyle = '#334155';
                                                             for (let y = 16; y < 60; y += 8) {
                                                                 ctx.beginPath(); ctx.arc(32, y, 16, 0, Math.PI, false); ctx.stroke();
                                                             }
                                                         });

                                                         makeBlock('roof_thatch_gold', (ctx) => {
                                                             ctx.fillStyle = '#a16207';
                                                             ctx.beginPath();
                                                             ctx.moveTo(32, 0); ctx.lineTo(64, 48); ctx.lineTo(32, 64); ctx.lineTo(0, 48);
                                                             ctx.closePath(); ctx.fill();
                                                         });

                                                         makeBlock('roof_shingle_brown', (ctx) => {
                                                             ctx.fillStyle = '#7c2d12';
                                                             ctx.beginPath();
                                                             ctx.moveTo(32, 0); ctx.lineTo(64, 48); ctx.lineTo(32, 64); ctx.lineTo(0, 48);
                                                             ctx.closePath(); ctx.fill();
                                                         });
                                                     }

                                                    create() {
                                                        this.cameras.main.setBackgroundColor('#020617');
                                                        this.wHalf = T_WIDTH / 2;
                                                        this.hHalf = T_HEIGHT / 2;

                                                        this.originX = this.sys.game.config.width / 2;
                                                        this.originY = 80;

                                                        this.isoGroup = this.add.group();
                                                        this.tileGroup = this.add.group();

                                                        // Setup movement inputs listener
                                                        this.cursors = this.input.keyboard.createCursorKeys();
                                                        this.wasd = this.input.keyboard.addKeys('W,A,S,D');

                                                        // Re-draw map scene
                                                        this.renderIsometricMap();

                                                        // Add player visual shape
                                                        this.renderPlayerAvatar();

                                                        // Add companion NPCs representing nearby MMO players
                                                        this.renderNearbyNPCs();

                                                        this.isMoving = false;
                                                    }

                                                    // Colors and visual properties matching design blueprint definitions
                                                    getEnvironmentColors() {
                                                        switch(currentEnv) {
                                                            case 'Praça Central':
                                                                return { floor: 0x14532D, border: 0x166534, tag: 'SOCIAL' };
                                                            case 'Academia Carlson Gracie':
                                                                return { floor: 0x1e3a8a, border: 0xd97706, tag: 'BJJ_CLASSIC' };
                                                            case 'Arena PvP':
                                                                return { floor: 0x0f172a, border: 0x06b6d4, tag: 'STADIUM' };
                                                            case 'Loja Oficial':
                                                                return { floor: 0x78350f, border: 0xfab23c, tag: 'STORE' };
                                                            case 'Hall da Fama':
                                                                return { floor: 0xf8fafc, border: 0xca8a04, tag: 'MAUSOLEUM' };
                                                            case 'Vestiários':
                                                                return { floor: 0x475569, border: 0x38bdf8, tag: ' locker' };
                                                            case 'Casas':
                                                                return { floor: 0xa16207, border: 0xfef08a, tag: 'ZEN' };
                                                            case 'Apartamentos':
                                                                return { floor: 0x1a202c, border: 0xb45309, tag: 'LOFT' };
                                                            case 'Escritórios':
                                                                return { floor: 0x3f220f, border: 0x064e3b, tag: 'OFFICE' };
                                                            case 'Salas VIP':
                                                                return { floor: 0x991b1b, border: 0xeab308, tag: 'ROYAL' };
                                                            default:
                                                                return { floor: 0x1e293b, border: 0x22d3ee, tag: 'BLUEPRINT' };
                                                        }
                                                    }

                                                    getBeltColorHex() {
                                                        switch(playerBelt) {
                                                            case 'Branca': return 0xF8FAFC;
                                                            case 'Azul': return 0x2563EB;
                                                            case 'Roxa': return 0x7C3AED;
                                                            case 'Marrom': return 0x78350F;
                                                            case 'Preta': return 0x0F172A;
                                                            default: return 0xF8FAFC;
                                                        }
                                                    }

                                                     renderIsometricMap() {
                                                         // Destroy old tiles
                                                         this.tileGroup.clear(true, true);

                                                         // Draw 15x15 Isometric cells with retro textured tiles
                                                         for(let x=0; x<GRID_SIZE; x++) {
                                                             for(let y=0; y<GRID_SIZE; y++) {
                                                                 const posX = (x - y) * this.wHalf + this.originX;
                                                                 const posY = (x + y) * this.hHalf + this.originY;

                                                                 // Classification/Zoning mapping:
                                                                 let textureKey = 'floor_stone_brick';
                                                                 if (currentEnv === 'Praça Central') {
                                                                     textureKey = (x === 14 || y === 14) ? 'nature_water' : 'nature_grass_clove';
                                                                 } else if (currentEnv === 'Academia Carlson Gracie') {
                                                                     textureKey = 'floor_checker_blue';
                                                                 } else if (currentEnv === 'Arena PvP') {
                                                                     textureKey = 'floor_stone_brick';
                                                                 } else if (currentEnv === 'Loja Oficial') {
                                                                     textureKey = 'floor_wood_plank';
                                                                 } else if (currentEnv === 'Hall da Fama') {
                                                                     textureKey = 'floor_checker_grey';
                                                                 } else if (currentEnv === 'Casas Futuras') {
                                                                     textureKey = 'floor_mud';
                                                                 } else {
                                                                     textureKey = 'floor_mud';
                                                                 }

                                                                 // Draw the floor tile
                                                                 const tile = this.add.image(posX, posY, textureKey);
                                                                 tile.depth = (x + y) * 2;
                                                                 this.tileGroup.add(tile);

                                                                 // Draw boundary walls, windows, and doors along x === 0 and y === 0 edges
                                                                 if (x === 0 || y === 0) {
                                                                     let wallKey = 'wall_cobble';
                                                                     let doorKey = 'door_wood_stud';
                                                                     let windowKey = 'window_arched_glass';
                                                                     let roofKey = 'roof_slate_blue';

                                                                     if (currentEnv === 'Academia Carlson Gracie') {
                                                                         wallKey = 'wall_panel_wood';
                                                                         doorKey = 'door_wood_stud';
                                                                         windowKey = 'window_arched_glass';
                                                                         roofKey = 'roof_thatch_gold';
                                                                     } else if (currentEnv === 'Arena PvP') {
                                                                         wallKey = 'wall_cobble';
                                                                         doorKey = 'door_bars';
                                                                         windowKey = 'window_bar_jail';
                                                                         roofKey = 'roof_slate_blue';
                                                                     } else if (currentEnv === 'Praça Central') {
                                                                         wallKey = 'wall_cobble';
                                                                         doorKey = 'door_wood_stud';
                                                                         windowKey = 'window_boarded';
                                                                         roofKey = 'roof_thatch_gold';
                                                                     } else if (currentEnv === 'Loja Oficial') {
                                                                         wallKey = 'wall_brick_red';
                                                                         doorKey = 'door_wood_stud';
                                                                         windowKey = 'window_cozy_glow';
                                                                         roofKey = 'roof_shingle_brown';
                                                                     } else if (currentEnv === 'Hall da Fama') {
                                                                         wallKey = 'wall_cobble';
                                                                         doorKey = 'door_arched';
                                                                         windowKey = 'window_arched_glass';
                                                                         roofKey = 'roof_slate_blue';
                                                                     } else if (currentEnv === 'Casas Futuras') {
                                                                         wallKey = 'wall_brick_red';
                                                                         doorKey = 'door_wood_stud';
                                                                         windowKey = 'window_cozy_glow';
                                                                         roofKey = 'roof_shingle_brown';
                                                                     }

                                                                     // Setups portals/intersections
                                                                     if (x === 0 && y === 5) {
                                                                         const door = this.add.image(posX, posY - 24, doorKey);
                                                                         door.depth = (x + y) * 2 + 1;
                                                                         this.tileGroup.add(door);

                                                                         const portalText = this.add.text(posX, posY - 58, "PORTAL", {
                                                                             fontSize: '10px',
                                                                             fontFamily: 'monospace',
                                                                             color: '#f43f5e',
                                                                             backgroundColor: '#18181b',
                                                                             padding: { x: 4, y: 2 }
                                                                         }).setOrigin(0.5);
                                                                         portalText.depth = (x + y) * 2 + 10;
                                                                         this.tileGroup.add(portalText);
                                                                     }
                                                                     // High windows placement on boundary wall
                                                                     else if (x === 0 && (y === 2 || y === 8 || y === 12)) {
                                                                         const win = this.add.image(posX, posY - 24, windowKey);
                                                                         win.depth = (x + y) * 2 + 1;
                                                                         this.tileGroup.add(win);
                                                                     }
                                                                     // Block brick sections
                                                                     else {
                                                                         const wall = this.add.image(posX, posY - 24, wallKey);
                                                                         wall.depth = (x + y) * 2 + 1;
                                                                         this.tileGroup.add(wall);

                                                                         // Roof placement
                                                                         if (currentEnv !== 'Praça Central') {
                                                                             const roof = this.add.image(posX, posY - 48, roofKey);
                                                                             roof.depth = (x + y) * 2 + 2;
                                                                             this.tileGroup.add(roof);
                                                                         }
                                                                     }
                                                                 }
                                                             }
                                                         }
                                                     }

                                                     originalIsometricMap() {
                                                        // Destroy old tiles
                                                        this.tileGroup.clear(true, true);

                                                        const cfg = this.getEnvironmentColors();

                                                        // Draw 15x15 Isometric cells
                                                        for(let x=0; x<GRID_SIZE; x++) {
                                                            for(let y=0; y<GRID_SIZE; y++) {
                                                                const posX = (x - y) * this.wHalf + this.originX;
                                                                const posY = (x + y) * this.hHalf + this.originY;

                                                                const tile = this.add.graphics();
                                                                tile.fillStyle(cfg.floor, 1);
                                                                tile.lineStyle(1.5, cfg.border, 0.4);

                                                                // Draw Diamond Shape
                                                                tile.beginPath();
                                                                tile.moveTo(0, -this.hHalf);
                                                                tile.lineTo(this.wHalf, 0);
                                                                tile.lineTo(0, this.hHalf);
                                                                tile.lineTo(-this.wHalf, 0);
                                                                tile.closePath();
                                                                tile.fillPath();
                                                                tile.strokePath();

                                                                tile.x = posX;
                                                                tile.y = posY;
                                                                tile.depth = (x + y) * 2;

                                                                this.tileGroup.add(tile);

                                                                // Draw collision obstacles (immediate blocks for boundaries index)
                                                                if ((x === 0 || y === 0 || x === GRID_SIZE-1 || y === GRID_SIZE-1) && currentEnv === 'Arena PvP') {
                                                                    // Draw Neon bounding fence
                                                                    const wall = this.add.graphics();
                                                                    wall.fillStyle(0x06b6d4, 0.2);
                                                                    wall.lineStyle(2, 0x06b6d4, 0.9);
                                                                    wall.fillRect(-5, -30, 10, 30);
                                                                    wall.strokeRect(-5, -30, 10, 30);
                                                                    wall.x = posX;
                                                                    wall.y = posY;
                                                                    wall.depth = (x + y) * 2 + 1;
                                                                    this.tileGroup.add(wall);
                                                                }
                                                                
                                                                // Teletransport Portal Tile triggers inside room
                                                                if (x === 0 && y === 5) {
                                                                    const portal = this.add.graphics();
                                                                    portal.fillStyle(0xFB923C, 0.6);
                                                                    // Draw glowing portal
                                                                    portal.beginPath();
                                                                    portal.moveTo(0, -this.hHalf + 2);
                                                                    portal.lineTo(this.wHalf - 2, 0);
                                                                    portal.lineTo(0, this.hHalf - 2);
                                                                    portal.lineTo(-this.wHalf + 2, 0);
                                                                    portal.closePath();
                                                                    portal.fillPath();
                                                                    portal.x = posX;
                                                                    portal.y = posY;
                                                                    portal.depth = (x + y) * 2 + 1;
                                                                    this.tileGroup.add(portal);
                                                                }
                                                            }
                                                        }
                                                    }

                                                    renderPlayerAvatar() {
                                                        if (this.playerVisual) this.playerVisual.destroy();
                                                        
                                                        this.playerVisual = this.add.container(0, 0);

                                                        // Avatar base shadow ellipse
                                                        const shadow = this.add.graphics();
                                                        shadow.fillStyle(0x000000, 0.35);
                                                        shadow.fillEllipse(0, 0, 24, 12);
                                                        
                                                        // Fighter body drawing vector style
                                                        const body = this.add.graphics();
                                                        // Skin tone
                                                        body.fillStyle(0xFDBA74, 1);
                                                        body.fillCircle(0, -42, 10); // head
                                                        
                                                        // Kimono robe vestwear
                                                        body.fillStyle(0x1e3a8a, 1); // Blue default Kimono jacket
                                                        body.lineStyle(1.5, 0x000000, 1);
                                                        body.beginPath();
                                                        body.moveTo(-12, -32);
                                                        body.lineTo(12, -32);
                                                        body.lineTo(10, 0);
                                                        body.lineTo(-10, 0);
                                                        body.closePath();
                                                        body.fillPath();
                                                        body.strokePath();

                                                        // Dynamic Belt (Faixa BJJ!)
                                                        const beltCol = this.getBeltColorHex();
                                                        body.fillStyle(beltCol, 1);
                                                        body.fillRect(-11, -12, 22, 5); // main belt
                                                        // Knot details
                                                        body.fillStyle(0xEF4444, 1); // red bar of Black Belt
                                                        body.fillRect(2, -12, 5, 5);

                                                        this.playerVisual.add([shadow, body]);
                                                        const pX = (playerX - playerY) * this.wHalf + this.originX;
                                                        const pY = (playerX + playerY) * this.hHalf + this.originY;
                                                        this.playerVisual.setPosition(pX, pY);
                                                        this.playerVisual.depth = (playerX + playerY) * 2 + 3;
                                                    }

                                                    renderNearbyNPCs() {
                                                        if (this.npc1) this.npc1.destroy();
                                                        if (this.npc2) this.npc2.destroy();

                                                        // Draw 2 sparring buddies
                                                        this.npc1 = this.add.container((1 - 4) * this.wHalf + this.originX, (1 + 4) * this.hHalf + this.originY);
                                                        const shadow1 = this.add.graphics().fillStyle(0x000000, 0.3).fillEllipse(0, 0, 20, 10);
                                                        const body1 = this.add.graphics().fillStyle(0xef4444, 1).fillRect(-8, -28, 16, 28);
                                                        body1.fillStyle(0xFDBA74).fillCircle(0, -34, 8);
                                                        this.npc1.add([shadow1, body1]);
                                                        this.npc1.depth = (1 + 4) * 2 + 3;

                                                        this.npc2 = this.add.container((8 - 3) * this.wHalf + this.originX, (8 + 3) * this.hHalf + this.originY);
                                                        const shadow2 = this.add.graphics().fillStyle(0x000000, 0.3).fillEllipse(0, 0, 20, 10);
                                                        const body2 = this.add.graphics().fillStyle(0x10b981, 1).fillRect(-8, -28, 16, 28);
                                                        body2.fillStyle(0xFDBA74).fillCircle(0, -34, 8);
                                                        this.npc2.add([shadow2, body2]);
                                                        this.npc2.depth = (8 + 3) * 2 + 3;
                                                    }

                                                    update() {
                                                        if (this.isMoving) return;

                                                        let dx = 0;
                                                        let dy = 0;

                                                        if (this.cursors.left.isDown || this.wasd.A.isDown) dx = -1;
                                                        else if (this.cursors.right.isDown || this.wasd.D.isDown) dx = 1;
                                                        else if (this.cursors.up.isDown || this.wasd.W.isDown) dy = -1;
                                                        else if (this.cursors.down.isDown || this.wasd.S.isDown) dy = 1;

                                                        if (dx !== 0 || dy !== 0) {
                                                            this.movePlayerTo(playerX + dx, playerY + dy);
                                                        }
                                                    }

                                                    // Corner Sliding Vector alignment on collision
                                                    movePlayerTo(targetX, targetY) {
                                                         // Collision limits boundaries checking
                                                         if (targetX < 0 || targetX >= GRID_SIZE || targetY < 0 || targetY >= GRID_SIZE) {
                                                             this.cameras.main.shake(80, 0.003);
                                                             return;
                                                         }

                                                         // Solid elements blocks collision (3D physical walls & windows along x===0 or y===0, except portal door at x===0, y===5)
                                                         if ((targetX === 0 || targetY === 0) && !(targetX === 0 && targetY === 5)) {
                                                             this.cameras.main.shake(80, 0.003);
                                                             return; // Collision with textured Kenney tileset wall
                                                         }

                                                         // Obstacle at (2,2) in Academia Carlson Gracie
                                                         if (targetX === 2 && targetY === 2 && currentEnv === 'Academia Carlson Gracie') {
                                                             this.cameras.main.shake(80, 0.003);
                                                             return; // Locked obstacle
                                                         }

                                                         this.isMoving = true;
                                                         playerX = targetX;
                                                         playerY = targetY;

                                                         const pxX = (playerX - playerY) * this.wHalf + this.originX;
                                                         const pxY = (playerX + playerY) * this.hHalf + this.originY;

                                                         this.tweens.add({
                                                             targets: this.playerVisual,
                                                             x: pxX,
                                                             y: pxY,
                                                             duration: 200,
                                                             onComplete: () => {
                                                                 this.isMoving = false;
                                                                 this.playerVisual.depth = (playerX + playerY) * 2 + 3;

                                                                 // Notify Android layer
                                                                 if(window.AndroidWebView) {
                                                                     window.AndroidWebView.postMessage(JSON.stringify({
                                                                         type: 'PLAYER_MOVE',
                                                                         x: playerX,
                                                                         y: playerY
                                                                     }));
                                                                 }

                                                                 // Trigger portal teletransports triggers!
                                                                 if (playerX === 0 && playerY === 5) {
                                                                     this.triggerTeleportPortal();
                                                                 }
                                                             }
                                                         });
                                                     }

                                                     triggerTeleportPortal() {
                                                         this.cameras.main.fadeOut(300, 0, 0, 0);
                                                         this.cameras.main.once('camerafadeoutcomplete', () => {
                                                             let index = environmentsCycle.indexOf(currentEnv);
                                                             if (index === -1) index = 0;
                                                             const nextRoom = environmentsCycle[(index + 1) % environmentsCycle.length];
                                                             currentEnv = nextRoom;
                                                             playerX = 7;
                                                             playerY = 7;

                                                             this.renderIsometricMap();
                                                             this.renderPlayerAvatar();
                                                             this.cameras.main.fadeIn(300);

                                                             if (window.AndroidWebView) {
                                                                 window.AndroidWebView.postMessage(JSON.stringify({
                                                                     type: 'ENV_CHANGE',
                                                                     env: nextRoom,
                                                                     x: 7,
                                                                     y: 7
                                                                 }));
                                                             }
                                                         });
                                                     }



                                                    forceEnvironmentChange(env) {
                                                        currentEnv = env;
                                                        this.renderIsometricMap();
                                                        this.renderPlayerAvatar();
                                                    }

                                                    forceBeltChange(belt) {
                                                        playerBelt = belt;
                                                        this.renderPlayerAvatar();
                                                    }

                                                    forcePlayerCoords(x, y) {
                                                        playerX = x;
                                                        playerY = y;
                                                        const pxX = (playerX - playerY) * this.wHalf + this.originX;
                                                        const pxY = (playerX + playerY) * this.hHalf + this.originY;
                                                        if (this.playerVisual) {
                                                            this.playerVisual.setPosition(pxX, pxY);
                                                            this.playerVisual.depth = (playerX + playerY) * 2 + 3;
                                                        }
                                                    }
                                                }

                                                class CanvasGame {
                                                    constructor() {
                                                        this.canvas = document.getElementById('canvas-fallback');
                                                        if (this.canvas) {
                                                            this.canvas.style.display = 'block';
                                                            this.ctx = this.canvas.getContext('2d');
                                                            this.resize();
                                                        }
                                                        window.addEventListener('resize', () => this.resize());
                                                        
                                                        this.keys = {};
                                                        window.addEventListener('keydown', (e) => {
                                                            this.keys[e.key.toUpperCase()] = true;
                                                            this.keys[e.key] = true;
                                                            this.handleInput();
                                                        });
                                                        window.addEventListener('keyup', (e) => {
                                                            this.keys[e.key.toUpperCase()] = false;
                                                            this.keys[e.key] = false;
                                                        });

                                                        this.isMoving = false;
                                                        this.wHalf = T_WIDTH / 2;
                                                        this.hHalf = T_HEIGHT / 2;
                                                        
                                                        this.draw();
                                                    }

                                                    resize() {
                                                        if (!this.canvas) return;
                                                        this.canvas.width = window.innerWidth;
                                                        this.canvas.height = window.innerHeight;
                                                        this.originX = this.canvas.width / 2;
                                                        this.originY = 80;
                                                        this.draw();
                                                    }

                                                    getEnvironmentColors() {
                                                        switch(currentEnv) {
                                                            case 'Praça Central': return { floor: '#14532D', border: '#166534' };
                                                            case 'Academia Carlson Gracie': return { floor: '#1e3a8a', border: '#d97706' };
                                                            case 'Arena PvP': return { floor: '#0f172a', border: '#06b6d4' };
                                                            case 'Loja Oficial': return { floor: '#78350f', border: '#fab23c' };
                                                            case 'Hall da Fama': return { floor: '#f8fafc', border: '#ca8a04' };
                                                            case 'Vestiários': return { floor: '#475569', border: '#38bdf8' };
                                                            case 'Casas': return { floor: '#a16207', border: '#fef08a' };
                                                            case 'Apartamentos': return { floor: '#1a202c', border: '#b45309' };
                                                            case 'Escritórios': return { floor: '#3f220f', border: '#064e3b' };
                                                            case 'Salas VIP': return { floor: '#991b1b', border: '#eab308' };
                                                            default: return { floor: '#1e293b', border: '#22d3ee' };
                                                        }
                                                    }

                                                    getBeltColorHex() {
                                                        switch(playerBelt) {
                                                            case 'Branca': return '#F8FAFC';
                                                            case 'Azul': return '#2563EB';
                                                            case 'Roxa': return '#7C3AED';
                                                            case 'Marrom': return '#78350F';
                                                            case 'Preta': return '#0F172A';
                                                            default: return '#F8FAFC';
                                                        }
                                                    }

                                                    draw() {
                                                        const ctx = this.ctx;
                                                        if (!ctx) return;
                                                        ctx.fillStyle = '#020617';
                                                        ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

                                                        const cfg = this.getEnvironmentColors();

                                                        for (let x = 0; x < GRID_SIZE; x++) {
                                                            for (let y = 0; y < GRID_SIZE; y++) {
                                                                const posX = (x - y) * this.wHalf + this.originX;
                                                                const posY = (x + y) * this.hHalf + this.originY;

                                                                ctx.beginPath();
                                                                ctx.moveTo(posX, posY - this.hHalf);
                                                                ctx.lineTo(posX + this.wHalf, posY);
                                                                ctx.lineTo(posX, posY + this.hHalf);
                                                                ctx.lineTo(posX - this.wHalf, posY);
                                                                ctx.closePath();
                                                                ctx.fillStyle = cfg.floor;
                                                                ctx.fill();
                                                                ctx.strokeStyle = cfg.border;
                                                                ctx.lineWidth = 1;
                                                                ctx.stroke();

                                                                if (x === 0 && y === 5) {
                                                                    ctx.beginPath();
                                                                    ctx.moveTo(posX, posY - this.hHalf + 2);
                                                                    ctx.lineTo(posX + this.wHalf - 2, posY);
                                                                    ctx.lineTo(posX, posY + this.hHalf - 2);
                                                                    ctx.lineTo(posX - this.wHalf + 2, posY);
                                                                    ctx.closePath();
                                                                    ctx.fillStyle = 'rgba(251, 146, 60, 0.6)';
                                                                    ctx.fill();
                                                                }

                                                                if (x === playerX && y === playerY) {
                                                                    this.drawPlayer(posX, posY);
                                                                }
                                                            }
                                                        }

                                                        const npc1X = (1 - 4) * this.wHalf + this.originX;
                                                        const npc1Y = (1 + 4) * this.hHalf + this.originY;
                                                        this.drawNPC(npc1X, npc1Y, '#ef4444');

                                                        const npc2X = (8 - 3) * this.wHalf + this.originX;
                                                        const npc2Y = (8 + 3) * this.hHalf + this.originY;
                                                        this.drawNPC(npc2X, npc2Y, '#10b981');
                                                    }

                                                    drawPlayer(pxX, pxY) {
                                                        const ctx = this.ctx;
                                                        ctx.fillStyle = 'rgba(0, 0, 0, 0.35)';
                                                        ctx.beginPath();
                                                        if (ctx.ellipse) {
                                                            ctx.ellipse(pxX, pxY, 12, 6, 0, 0, 2 * Math.PI);
                                                        } else {
                                                            ctx.arc(pxX, pxY, 8, 0, 2 * Math.PI);
                                                        }
                                                        ctx.fill();

                                                        ctx.fillStyle = '#FDBA74';
                                                        ctx.beginPath();
                                                        ctx.arc(pxX, pxY - 42, 10, 0, 2 * Math.PI);
                                                        ctx.fill();

                                                        ctx.fillStyle = '#1e3a8a';
                                                        ctx.beginPath();
                                                        ctx.moveTo(pxX - 12, pxY - 32);
                                                        ctx.lineTo(pxX + 12, pxY - 32);
                                                        ctx.lineTo(pxX + 10, pxY);
                                                        ctx.lineTo(pxX - 10, pxY);
                                                        ctx.closePath();
                                                        ctx.fill();
                                                        ctx.strokeStyle = '#000000';
                                                        ctx.lineWidth = 1;
                                                        ctx.stroke();

                                                        ctx.fillStyle = this.getBeltColorHex();
                                                        ctx.fillRect(pxX - 11, pxY - 12, 22, 5);
                                                        ctx.fillStyle = '#EF4444';
                                                        ctx.fillRect(pxX + 2, pxY - 12, 5, 5);
                                                    }

                                                    drawNPC(pxX, pxY, color) {
                                                        const ctx = this.ctx;
                                                        ctx.fillStyle = 'rgba(0, 0, 0, 0.3)';
                                                        ctx.beginPath();
                                                        if (ctx.ellipse) {
                                                            ctx.ellipse(pxX, pxY, 10, 5, 0, 0, 2 * Math.PI);
                                                        } else {
                                                            ctx.arc(pxX, pxY, 6, 0, 2 * Math.PI);
                                                        }
                                                        ctx.fill();

                                                        ctx.fillStyle = color;
                                                        ctx.fillRect(pxX - 8, pxY - 28, 16, 28);

                                                        ctx.fillStyle = '#FDBA74';
                                                        ctx.beginPath();
                                                        ctx.arc(pxX, pxY - 34, 8, 0, 2 * Math.PI);
                                                        ctx.fill();
                                                    }

                                                    handleInput() {
                                                        if (this.isMoving) return;

                                                        let dx = 0;
                                                        let dy = 0;

                                                        if (this.keys['ArrowLeft'] || this.keys['A']) dx = -1;
                                                        else if (this.keys['ArrowRight'] || this.keys['D']) dx = 1;
                                                        else if (this.keys['ArrowUp'] || this.keys['W']) dy = -1;
                                                        else if (this.keys['ArrowDown'] || this.keys['S']) dy = 1;

                                                        if (dx !== 0 || dy !== 0) {
                                                            this.movePlayerTo(playerX + dx, playerY + dy);
                                                        }
                                                    }

                                                    movePlayerTo(targetX, targetY) {
                                                        if (targetX < 0 || targetX >= GRID_SIZE || targetY < 0 || targetY >= GRID_SIZE) {
                                                            return;
                                                        }

                                                        if (targetX === 2 && targetY === 2 && currentEnv === 'Academia Carlson Gracie') {
                                                            return;
                                                        }

                                                        this.isMoving = true;
                                                        playerX = targetX;
                                                        playerY = targetY;

                                                        setTimeout(() => {
                                                            this.isMoving = false;
                                                            this.draw();

                                                            if (window.AndroidWebView) {
                                                                window.AndroidWebView.postMessage(JSON.stringify({
                                                                    type: 'PLAYER_MOVE',
                                                                    x: playerX,
                                                                    y: playerY
                                                                }));
                                                            }

                                                            if (playerX === 0 && playerY === 5) {
                                                                this.triggerTeleportPortal();
                                                            }
                                                        }, 150);
                                                    }

                                                    triggerTeleportPortal() {
                                                        const nextRoom = (currentEnv === 'Academia Carlson Gracie') ? 'Arena PvP' : 'Academia Carlson Gracie';
                                                        currentEnv = nextRoom;
                                                        playerX = 7;
                                                        playerY = 7;
                                                        this.draw();

                                                        if (window.AndroidWebView) {
                                                            window.AndroidWebView.postMessage(JSON.stringify({
                                                                type: 'ENV_CHANGE',
                                                                env: nextRoom,
                                                                x: 7,
                                                                y: 7
                                                            }));
                                                        }
                                                    }

                                                    forceEnvironmentChange(env) {
                                                        currentEnv = env;
                                                        this.draw();
                                                    }

                                                    forceBeltChange(belt) {
                                                        playerBelt = belt;
                                                        this.draw();
                                                    }

                                                    forcePlayerCoords(x, y) {
                                                        playerX = x;
                                                        playerY = y;
                                                        this.draw();
                                                    }
                                                }

                                                let game;
                                                let canvasEngine;

                                                if (typeof Phaser !== 'undefined') {
                                                    engineType = 'PHASER';
                                                    const config = {
                                                        type: Phaser.AUTO,
                                                        width: window.innerWidth,
                                                        height: window.innerHeight,
                                                        parent: document.body,
                                                        scene: JiuVerseIsoGame,
                                                        audio: { noAudio: true }
                                                    };
                                                    game = new Phaser.Game(config);
                                                } else {
                                                    engineType = 'CANVAS';
                                                    canvasEngine = new CanvasGame();
                                                }

                                                window.updatePlayerCoords = function(x, y) {
                                                    if (engineType === 'PHASER' && game) {
                                                        const scene = game.scene.keys['JiuVerseIsoGame'];
                                                        if (scene) scene.forcePlayerCoords(x, y);
                                                    } else if (engineType === 'CANVAS' && canvasEngine) {
                                                        canvasEngine.forcePlayerCoords(x, y);
                                                    }
                                                }

                                                window.changeEnvironment = function(env) {
                                                    if (engineType === 'PHASER' && game) {
                                                        const scene = game.scene.keys['JiuVerseIsoGame'];
                                                        if (scene) scene.forceEnvironmentChange(env);
                                                     } else if (engineType === 'CANVAS' && canvasEngine) {
                                                        canvasEngine.forceEnvironmentChange(env);
                                                    }
                                                }

                                                window.changePlayerBelt = function(belt) {
                                                    if (engineType === 'PHASER' && game) {
                                                        const scene = game.scene.keys['JiuVerseIsoGame'];
                                                        if (scene) scene.forceBeltChange(belt);
                                                    } else if (engineType === 'CANVAS' && canvasEngine) {
                                                        canvasEngine.forceBeltChange(belt);
                                                    }
                                                }

                                                window.addEventListener('resize', () => {
                                                    if (engineType === 'PHASER' && game) {
                                                        game.scale.resize(window.innerWidth, window.innerHeight);
                                                    }
                                                });
                                            </script>
                                        </body>
                                        </html>
                                    """.trimIndent()

                                    loadDataWithBaseURL("https://cdnjs.cloudflare.com", phaserHtml, "text/html", "UTF-8", null)
                                    webViewRef = this
                                }
                            },
                            update = { webView ->
                                webView.evaluateJavascript("if (window.updatePlayerCoords) { window.updatePlayerCoords($playerX, $playerY); }", null)
                                webView.evaluateJavascript("if (window.changeEnvironment) { window.changeEnvironment('$selectedEnvironment'); }", null)
                                webView.evaluateJavascript("if (window.changePlayerBelt) { window.changePlayerBelt('$userBelt'); }", null)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // 1. Loading Screen (Tela de carregamento)
                        if (webViewLoadingState) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF020617))
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = BlueprintCyan,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "CARREGANDO DOJO ISOMÉTRICO (PHASER 3)...",
                                    color = BlueprintCyan,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Iniciando mapeamento de colisões, texturas Kenney Retro e materiais Phaser...",
                                    color = BlueprintTextSecondary,
                                    fontSize = 7.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // 2. Global Fallback Screen for WebView rendering/connection errors
                        webViewErrorState?.let { errMsg ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF090D16))
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Erro de inicialização",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "FALHA NA RENDERIZAÇÃO DO CLIENTE",
                                    color = Color(0xFFEF4444),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = errMsg,
                                    color = BlueprintTextSecondary,
                                    fontSize = 8.5.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            useWebPhaser = false
                                            webViewLoadingState = true
                                            webViewErrorState = null
                                            jsLogs.clear()
                                            useWebPhaser = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Recarregar", fontSize = 9.sp, color = Color.White)
                                    }
                                    Button(
                                        onClick = {
                                            useWebPhaser = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintCyan),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Fallback Canvas", fontSize = 9.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    } // Ends the Box containing WebView + overlays
                    } else {
                        // Native Compose 60FPS Simulated Isometric view fallback
                        Box(modifier = Modifier.fillMaxSize()) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val originX = w / 2f
                                val originY = h / 4f
                                val cellW = 44f
                                val cellH = 22f

                                // Draw simulated ground tiles
                                val col = getBeltColor(userBelt)
                                for (x in -4..4) {
                                    for (y in -4..4) {
                                        val isoX = (x - y) * cellW + originX
                                        val isoY = (x + y) * cellH + originY

                                        val path = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(isoX, isoY - cellH)
                                            lineTo(isoX + cellW, isoY)
                                            lineTo(isoX, isoY + cellH)
                                            lineTo(isoX - cellW, isoY)
                                            close()
                                        }

                                        drawPath(
                                            path = path,
                                            color = if (x == 0 && y == 0) col.copy(alpha = 0.5f) else BlueprintCard,
                                            style = androidx.compose.ui.graphics.drawscope.Fill
                                        )
                                        drawPath(
                                            path = path,
                                            color = BlueprintGridLine.copy(alpha = 0.5f),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(1f)
                                        )
                                    }
                                }

                                // Player avatar dot on simulated grid
                                drawCircle(
                                    color = BlueprintCyan,
                                    radius = 8f,
                                    center = androidx.compose.ui.geometry.Offset(originX, originY + 10f)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3f,
                                    center = androidx.compose.ui.geometry.Offset(originX, originY + 10f)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("VISTA SIMULADA ATIVA • COORDINATES SYNCED WITH BLUEPRINT", fontSize = 7.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // --- ON-SCREEN GLASSMORPHIC JOYPAD DIRECTION CONTROLLER ---
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .size(90.dp)
                            .background(Color(0xFF0F172A).copy(alpha = 0.85f), CircleShape)
                            .border(1.5.dp, BlueprintCyan.copy(alpha = 0.6f), CircleShape)
                    ) {
                        // Direction buttons layout
                        IconButton(
                            onClick = { 
                                playerY = (playerY - 1).coerceAtLeast(0)
                                webViewRef?.evaluateJavascript("javascript:if(window.updatePlayerCoords){window.updatePlayerCoords($playerX, $playerY);}", null)
                            },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(28.dp)
                        ) {
                            Text("▲", color = BlueprintCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { 
                                playerX = (playerX - 1).coerceAtLeast(0) 
                                webViewRef?.evaluateJavascript("javascript:if(window.updatePlayerCoords){window.updatePlayerCoords($playerX, $playerY);}", null)
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(28.dp)
                        ) {
                            Text("◀", color = BlueprintCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { 
                                playerX = (playerX + 1).coerceAtMost(14)
                                webViewRef?.evaluateJavascript("javascript:if(window.updatePlayerCoords){window.updatePlayerCoords($playerX, $playerY);}", null)
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(28.dp)
                        ) {
                            Text("▶", color = BlueprintCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { 
                                playerY = (playerY + 1).coerceAtMost(14)
                                webViewRef?.evaluateJavascript("javascript:if(window.updatePlayerCoords){window.updatePlayerCoords($playerX, $playerY);}", null)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(28.dp)
                        ) {
                            Text("▼", color = BlueprintCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        // Center dot logo
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(16.dp)
                                .background(BlueprintOrange, CircleShape)
                        )
                    }
                }

                // Collapsible JS Console Log Viewer (Developer / Director Auditing tool)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showConsoleLogs = !showConsoleLogs }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (showConsoleLogs) "▼" else "▲",
                                color = BlueprintCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CONSOLE LOG JS INTERNO DO DOJO (${jsLogs.size} LOGS)",
                                color = BlueprintCyan,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (jsLogs.isNotEmpty()) {
                            Text(
                                text = "LIMPAR",
                                color = Color(0xFFEF4444),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { jsLogs.clear() }
                            )
                        }
                    }
                    
                    if (showConsoleLogs) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color(0xFF020617))
                                .border(1.dp, BlueprintCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        ) {
                            if (jsLogs.isEmpty()) {
                                Text(
                                    text = "Nenhum log no console. WebView inicializado limpo.",
                                    color = BlueprintTextSecondary,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(jsLogs) { log ->
                                        Text(
                                            text = log,
                                            color = if (log.contains("ERROR")) Color(0xFFEF4444) else if (log.contains("WARN")) Color(0xFFFBBF24) else Color(0xFFCBD5E1),
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==================== COLUMN 3: RIGHT PANEL (CHAT & AMIGOS) ====================
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Unified Social Chat Panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.3f),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                    border = BorderStroke(1.dp, BlueprintGridLine)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("💬 MULTI-CHAN LIVE CHAT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                        
                        // Scrollable chat feed
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFF070B13), RoundedCornerShape(6.dp))
                                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(6.dp))
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(simMessages) { msg ->
                                val senderCol = if (msg.isMe) BlueprintCyan else BlueprintOrange
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = msg.sender,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = senderCol
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "[${msg.channel}] ${msg.time}",
                                            fontSize = 6.sp,
                                            color = BlueprintTextSecondary
                                        )
                                    }
                                    Text(
                                        text = msg.content,
                                        fontSize = 8.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Custom Chat input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            BasicTextField(
                                value = chatInputField,
                                onValueChange = { chatInputField = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 8.sp),
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .padding(6.dp),
                                cursorBrush = SolidColor(BlueprintCyan)
                            )
                            IconButton(
                                onClick = {
                                    if (chatInputField.isNotBlank()) {
                                        postChat(chatInputField)
                                        chatInputField = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(BlueprintCyan, RoundedCornerShape(4.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.Black,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }

                // Friends & Online Rivals List
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                    border = BorderStroke(1.dp, BlueprintGridLine)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("👥 LUTADORES ONLINE (${simFriends.size})", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(simFriends) { friend ->
                                val isOnline = friend.status != "OFFLINE"
                                val indicatorColor = if (friend.status == "SPARRING") BlueprintOrange else if (isOnline) BlueprintGreen else BlueprintTextSecondary
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(indicatorColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(friend.name, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("[F. ${friend.belt}]", fontSize = 6.5.sp, color = BlueprintCyan)
                                    }
                                    
                                    // Interactive challenge trigger action
                                    Box(
                                        modifier = Modifier
                                            .background(if (isOnline) Color(0xFF065F46) else Color(0xFF1E293B), RoundedCornerShape(2.dp))
                                            .clickable(enabled = isOnline) {
                                                simMessages.add(
                                                    SimMsg("SISTEMA", "Desafio de Sparring enviado para ${friend.name}! Aguardando aceite...", "22:45", false, "PVP")
                                                )
                                            }
                                            .padding(horizontal = 4.dp, vertical = 1.5.dp)
                                    ) {
                                        Text(
                                            text = if (isOnline) "DESAFIAR" else "INDISPONÍVEL",
                                            fontSize = 5.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ==================== BOTTOM SHELF PANEL (INVENTORY, SHOP, QUESTS, ROSTER) ====================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .border(2.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header of panel switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val bottomToggles = listOf(
                        "INV" to "🎒 MEU INVENTÁRIO",
                        "SHOP" to "🛒 LOJA OFICIAL J$",
                        "QUESTS" to "🏆 MISSÕES DE TREINO",
                        "ROSTER" to "🏫 ACADEMIA ROSTER DB"
                    )

                    bottomToggles.forEach { (tag, name) ->
                        val active = activeBottomTab == tag
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (active) BlueprintCyan.copy(alpha = 0.2f) else Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                .border(1.dp, if (active) BlueprintCyan else Color.Transparent, RoundedCornerShape(4.dp))
                                .clickable { activeBottomTab = tag }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) BlueprintCyan else BlueprintTextSecondary
                            )
                        }
                    }
                }

                // Dynamic Screen layout switcher inside bottom shelf
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF0A0F1D))
                        .padding(6.dp)
                ) {
                    when (activeBottomTab) {
                        "INV" -> {
                            // Inventory listing with Click-to-Equip functionality!
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(simInventory) { item ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF131A2D), RoundedCornerShape(6.dp))
                                            .border(1.dp, if (item.equipped) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(6.dp))
                                            .clickable {
                                                // Equip toggle binder
                                                val index = simInventory.indexOf(item)
                                                if (index != -1) {
                                                    simInventory[index] = item.copy(equipped = !item.equipped)
                                                }
                                            }
                                            .padding(6.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.rarity,
                                                    fontSize = 5.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when(item.rarity) {
                                                        "LENDARIO" -> BlueprintOrange
                                                        "EPICO" -> Color(0xFFA855F7)
                                                        "RARO" -> BlueprintCyan
                                                        else -> BlueprintTextSecondary
                                                    }
                                                )
                                                if (item.equipped) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .background(BlueprintTeal, CircleShape)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.name,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (item.equipped) "EQUIPADO" else "CLIQUE P/ EQUIPAR",
                                                fontSize = 6.2.sp,
                                                color = if (item.equipped) BlueprintTeal else BlueprintTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "SHOP" -> {
                            // Official exclusive merchandise shop, deducting money upon purchase!
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(6),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(simShopItems) { item ->
                                    val isAffordable = userCoins >= item.price
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF221111).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .border(0.5.dp, BlueprintOrange.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .clickable(enabled = isAffordable) {
                                                userCoins -= item.price
                                                simInventory.add(
                                                    SimItem(
                                                        id = item.id + "_p",
                                                        name = item.name,
                                                        type = item.type,
                                                        rarity = item.rarity,
                                                        price = item.price,
                                                        equipped = false
                                                    )
                                                )
                                            }
                                            .padding(4.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(item.name, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(item.rarity, fontSize = 5.sp, color = BlueprintOrange, fontWeight = FontWeight.Black)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🪙", fontSize = 6.sp)
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "${item.price}",
                                                    fontSize = 7.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAffordable) BlueprintOrange else Color.Red,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "QUESTS" -> {
                            // Active target lists and daily contracts milestones
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val quests = listOf(
                                    "Aperfeiçoar o triângulo do dojo" to "FINALIZAR 3 COMBATES",
                                    "Comprar Kimono Lendário na Loja" to "COMPRAR 1 ITEM",
                                    "Mudar a graduação no painel lateral" to "ALTERAR COR DE FAIXA BJJ",
                                    "Visitar as colunas da Arena PvP" to "VISITAR 3 AMBIENTES"
                                )

                                quests.forEachIndexed { i, q ->
                                    val checked = i % 2 == 0
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(Color(0xFF131A26), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .padding(6.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("FILO CONTRATO #${104 + i}", fontSize = 6.sp, color = BlueprintCyan)
                                                Text(if (checked) "✓ FEITO" else "○ PROGRESSO", fontSize = 6.sp, color = if (checked) BlueprintTeal else BlueprintOrange, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(q.first, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(q.second, fontSize = 6.sp, color = BlueprintTextSecondary)
                                        }
                                    }
                                }
                            }
                        }

                        "ROSTER" -> {
                            // Registered Students from the room database rendering to keeping connected
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(databaseRoster) { member ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF131A26), RoundedCornerShape(4.dp))
                                            .padding(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(getBeltColor(member.belt), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(member.name, fontSize = 7.5.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text("Faixa ${member.belt}", fontSize = 6.sp, color = BlueprintCyan)
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
}

// Utility mapper matching custom hexes colors of BJJ belts class
fun getBeltColor(name: String): Color {
    return when (name) {
        "Branca" -> Color(0xFFF1F5F9)
        "Azul" -> Color(0xFF2563EB)
        "Roxa" -> Color(0xFF7C3AED)
        "Marrom" -> Color(0xFF78350F)
        "Preta" -> Color(0xFF0F172A)
        else -> Color(0xFF94A3B8)
    }
}
