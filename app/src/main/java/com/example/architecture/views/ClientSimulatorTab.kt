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
data class SimMsg(val sender: String, val content: String, val time: String, val isMe: Boolean)

@Composable
fun ClientSimulatorTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Simulator-level controls
    var simDarkMode by remember { mutableStateOf(true) }
    var simIsLoggedIn by remember { mutableStateOf(false) }
    var simScreen by remember { mutableStateOf("login") } // login, register, home, profile, inventory, ranking, friends, messages

    // User account states
    var userEmail by remember { mutableStateOf("mestre.oss@jiuverse.com") }
    var userNickname by remember { mutableStateOf("OssMaster") }
    var userBelt by remember { mutableStateOf("Azul") }
    var userXp by remember { mutableStateOf(320) }
    var userCoins by remember { mutableStateOf(850) }

    // Position Coordinates for simulated Dojo 2.5D Real-time grid
    var playerX by remember { mutableStateOf(3) }
    var playerY by remember { mutableStateOf(3) }

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
                                playerX = playerX,
                                playerY = playerY,
                                userNickname = userNickname,
                                userBelt = userBelt,
                                onMove = { dx, dy ->
                                    playerX = (playerX + dx).coerceIn(0, 6)
                                    playerY = (playerY + dy).coerceIn(0, 6)
                                },
                                onCoordsChange = { x, y ->
                                    playerX = x
                                    playerY = y
                                }
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
                                }
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
                                onSend = { text ->
                                    simMessages.add(SimMsg("OssMaster", text, "15:35", true))
                                    // Automatic system simulation response
                                    if (text.uppercase().contains("OSS") || text.uppercase().contains("OLÁ")) {
                                        simMessages.add(SimMsg("Mestre Cícero", "Foco no quadril e postura no tatame. Oss!", "15:36", false))
                                    }
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
    playerX: Int,
    playerY: Int,
    userNickname: String,
    userBelt: String,
    onMove: (Int, Int) -> Unit,
    onCoordsChange: (Int, Int) -> Unit
) {
    var selectedChannel by remember { mutableStateOf("Central") } // Central, Premium, Privado
    var proxFilter by remember { mutableStateOf(true) }
    var useWebPhaser by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
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
                Text("Dojo $selectedChannel • ($playerX, $playerY)", fontSize = 8.sp, color = phoneTextSecondary)
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

        Spacer(modifier = Modifier.height(6.dp))

        // Live Channels tabs selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Central", "ADCC", "Privado").forEach { channel ->
                val active = selectedChannel == channel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (active) Color(0xFF1E293B) else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { selectedChannel = channel }
                        .padding(vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color(0xFF14B8A6) else phoneTextSecondary
                    )
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
                                        if (type == "PLAYER_MOVE") {
                                            val x = json.optInt("x")
                                            val y = json.optInt("y")
                                            onCoordsChange(x, y)
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
                                            border: 1px solid #06b6d4; padding: 5px; border-radius: 4px; color: #f8fafc;
                                            font-size: 7.5px; pointer-events: none; z-index: 100; max-width: 160px;
                                            box-shadow: 0 0 5px rgba(6, 182, 212, 0.3);
                                        }
                                        .hud-line { margin-bottom: 2px; }
                                    </style>
                                    <script src="https://cdn.jsdelivr.net/npm/phaser@3.60.0/dist/phaser.min.js"></script>
                                </head>
                                <body>
                                    <div id="diagnostic-hud">
                                        <div class="hud-line" style="color: #2dd4bf; font-weight: bold;">PHASER ENGINE v3.60</div>
                                        <div class="hud-line">Status: <span style="color: #10b981; font-weight: bold;">ONLINE (SYNC)</span></div>
                                        <div class="hud-line">Coords: <span style="color: #38bdf8;" id="val-coords">($playerX, $playerY)</span></div>
                                        <div class="hud-line">FPS: <span id="val-fps">60</span></div>
                                    </div>
                                    <div id="game-container"></div>

                                    <script>
                                        const TILE_WIDTH = 44;
                                        const TILE_HEIGHT = 22;
                                        const MAP_OFFSET_X = 135;
                                        const MAP_OFFSET_Y = 48;
                                        const GRID_SIZE = 7;

                                        function cartToIso(x, y) {
                                            return {
                                                x: (x - y) * (TILE_WIDTH / 2) + MAP_OFFSET_X,
                                                y: (x + y) * (TILE_HEIGHT / 2) + MAP_OFFSET_Y
                                            };
                                        }

                                        class JiuVerseIsometricDojo extends Phaser.Scene {
                                            constructor() {
                                                super({ key: 'JiuVerseDojo' });
                                                this.localPlayer = null;
                                                this.otherPlayers = {};
                                                this.collisionGrid = [];
                                            }

                                            preload() {
                                                // Base ground tile
                                                let canvas = this.textures.createCanvas('tile_base', TILE_WIDTH, TILE_HEIGHT);
                                                let ctx = canvas.context;
                                                ctx.fillStyle = '#0f172a';
                                                ctx.lineWidth = 1;
                                                ctx.strokeStyle = '#334155';
                                                ctx.beginPath();
                                                ctx.moveTo(TILE_WIDTH / 2, 0);
                                                ctx.lineTo(TILE_WIDTH, TILE_HEIGHT / 2);
                                                ctx.lineTo(TILE_WIDTH / 2, TILE_HEIGHT);
                                                ctx.lineTo(0, TILE_HEIGHT / 2);
                                                ctx.closePath();
                                                ctx.fill();
                                                ctx.stroke();
                                                canvas.refresh();

                                                // Alternative ground color tile
                                                let activeCanvas = this.textures.createCanvas('tile_active', TILE_WIDTH, TILE_HEIGHT);
                                                let actx = activeCanvas.context;
                                                actx.fillStyle = '#1e293b';
                                                actx.lineWidth = 1;
                                                actx.strokeStyle = '#06b6d4';
                                                actx.beginPath();
                                                actx.moveTo(TILE_WIDTH / 2, 0);
                                                actx.lineTo(TILE_WIDTH, TILE_HEIGHT / 2);
                                                actx.lineTo(TILE_WIDTH / 2, TILE_HEIGHT);
                                                actx.lineTo(0, TILE_HEIGHT / 2);
                                                actx.closePath();
                                                actx.fill();
                                                actx.stroke();
                                                activeCanvas.refresh();

                                                // Procedural Character
                                                let avatar = this.textures.createCanvas('char_front', 32, 48);
                                                let avCtx = avatar.context;
                                                avCtx.fillStyle = '#1d4ed8'; // Kimono azul marinho
                                                avCtx.fillRect(6, 16, 20, 32);
                                                avCtx.fillStyle = '#000000'; // Faixa preta
                                                avCtx.fillRect(4, 28, 24, 4);
                                                avCtx.fillStyle = '#ffffff';
                                                avCtx.fillRect(18, 28, 4, 4);
                                                avCtx.fillStyle = '#fbcfe8'; // Rostinho
                                                avCtx.beginPath();
                                                avCtx.arc(16, 10, 8, 0, Math.PI * 2);
                                                avCtx.fill();
                                                avatar.refresh();
                                            }

                                            create() {
                                                this.cameras.main.setBackgroundColor('#020617');

                                                // Construct grid blocks
                                                for (let x = 0; x < GRID_SIZE; x++) {
                                                    this.collisionGrid[x] = [];
                                                    for (let y = 0; y < GRID_SIZE; y++) {
                                                        let isBlocked = x === 0 || y === 0 || x === GRID_SIZE - 1 || y === GRID_SIZE - 1;
                                                        this.collisionGrid[x][y] = isBlocked;

                                                        let pos = cartToIso(x, y);
                                                        let key = (x + y) % 2 === 0 ? 'tile_base' : 'tile_active';
                                                        let tile = this.add.image(pos.x, pos.y, key);
                                                        if (isBlocked) {
                                                            tile.setTint(0x7f1d1d);
                                                        }
                                                    }
                                                }

                                                // Load local fighter avatar
                                                let pPos = cartToIso($playerX, $playerY);
                                                this.localPlayer = this.add.container(pPos.x, pPos.y);
                                                let sprite = this.add.image(0, -20, 'char_front');

                                                let nickStr = "$userNickname" ? "$userNickname" : "Fighter";
                                                let tagText = this.add.text(0, -48, nickStr, {
                                                    fontSize: '8.5px',
                                                    fontFamily: 'monospace',
                                                    backgroundColor: 'rgba(9, 13, 22, 0.9)',
                                                    padding: { x: 3, y: 1 },
                                                    color: '#2dd4bf',
                                                    stroke: '#0891b2',
                                                    strokeThickness: 1
                                                }).setOrigin(0.5);

                                                let bStr = "$userBelt" ? "$userBelt" : "Preta";
                                                let beltText = this.add.text(0, -37, '[FAIXA ' + bStr.toUpperCase() + ']', {
                                                    fontSize: '6.5px',
                                                    fontFamily: 'monospace',
                                                    color: '#38bdf8'
                                                }).setOrigin(0.5);

                                                this.localPlayer.add([sprite, tagText, beltText]);
                                                this.localPlayer.gridX = $playerX;
                                                this.localPlayer.gridY = $playerY;

                                                // Static Dojo NPCs
                                                this.spawnDojoNPCs();
                                            }

                                            spawnDojoNPCs() {
                                                const npcs = [
                                                    { name: 'Mestre Cícero', belt: 'Preta', x: 2, y: 4, tint: 0x5b21b6 },
                                                    { name: 'Guerreiro Luta', belt: 'Coral', x: 4, y: 2, tint: 0x9a3412 }
                                                ];

                                                npcs.forEach(n => {
                                                    let pos = cartToIso(n.x, n.y);
                                                    let container = this.add.container(pos.x, pos.y);
                                                    let bSprite = this.add.image(0, -20, 'char_front');
                                                    bSprite.setTint(n.tint);

                                                    let bTag = this.add.text(0, -48, n.name, {
                                                        fontSize: '8px',
                                                        fontFamily: 'monospace',
                                                        color: '#e2e8f0',
                                                        backgroundColor: 'rgba(15, 23, 42, 0.85)',
                                                        padding: { x: 2, y: 1 }
                                                    }).setOrigin(0.5);

                                                    let bBelt = this.add.text(0, -37, '[FAIXA ' + n.belt.toUpperCase() + ']', {
                                                        fontSize: '6px',
                                                        fontFamily: 'monospace',
                                                        color: '#94a3b8'
                                                    }).setOrigin(0.5);

                                                    container.add([bSprite, bTag, bBelt]);
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
                                                            duration: 150,
                                                            ease: 'Power1'
                                                        });

                                                        document.getElementById('val-coords').innerText = '(' + nextX + ', ' + nextY + ')';

                                                        // Send coordinates back to Android JVM layer
                                                        if (window.AndroidWebView) {
                                                            window.AndroidWebView.postMessage(JSON.stringify({
                                                                type: 'PLAYER_MOVE',
                                                                x: nextX,
                                                                y: nextY
                                                            }));
                                                        }
                                                    } else {
                                                        this.cameras.main.flash(45, 185, 28, 28);
                                                    }
                                                }
                                            }

                                            update(time, delta) {
                                                document.getElementById('val-fps').innerText = Math.round(1000 / delta);
                                            }
                                        }

                                        const config = {
                                            type: Phaser.AUTO,
                                            width: window.innerWidth,
                                            height: window.innerHeight,
                                            parent: 'game-container',
                                            scene: JiuVerseIsometricDojo
                                        };

                                        const game = new Phaser.Game(config);

                                        // Bind API trigger function to window context
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
                                                    document.getElementById('val-coords').innerText = '(' + x + ', ' + y + ')';
                                                }
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
                        }
                    },
                    update = {
                        it.evaluateJavascript("if (window.updatePlayerCoords) { window.updatePlayerCoords($playerX, $playerY); }", null)
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

                    // Draw Tatami Tiles matching isometric projection
                    for (r in 0..4) {
                        for (c in 0..4) {
                            val isoX = originX + (c - r) * (tWidth / 2f)
                            val isoY = originY + (c + r) * (tHeight / 2f)

                            val isPlayer = playerX == c && playerY == r
                            val isBot = (c == 1 && r == 3) || (c == 3 && r == 1)
                            val isBlocked = c == 0 || r == 0 || c == 4 || r == 4

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
                                    if (dist <= 2 && proxFilter) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF10B981), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 3.dp, vertical = 1.dp)
                                                .offset(y = (-12).dp)
                                        ) {
                                            Text("OSS?", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
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
    onEquip: (SimItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Text("BOLSINHA DE ITENS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = phoneTextPrimary)
        Text("Dobre kimonos, acessórios ou decore seus tatames públicos.", fontSize = 9.sp, color = phoneTextSecondary)

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(phoneCard, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.rarity,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (item.rarity) {
                                    "LENDARIO" -> Color(0xFFF59E0B)
                                    "EPICO" -> Color(0xFFA855F7)
                                    "RARO" -> Color(0xFF3B82F6)
                                    else -> phoneTextSecondary
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(item.type, fontSize = 8.sp, color = phoneTextSecondary)
                        }
                        Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = phoneTextPrimary)
                        Text("$ ${item.price} JC", fontSize = 10.sp, color = Color(0xFFCA8A04))
                    }

                    Button(
                        onClick = { onEquip(item) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.equipped) Color(0xFF155E75) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (item.equipped) "DESEQUIPAR" else "EQUIPAR",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

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
    onSend: (String) -> Unit
) {
    var tmsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(msgs) { msg ->
                val alignMe = msg.isMe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (alignMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (alignMe) Color(0xFF0F766E) else phoneCard,
                                RoundedCornerShape(8.dp)
                            )
                            .border(0.5.dp, if (alignMe) Color(0xFF14B8A6) else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .widthIn(max = 200.dp)
                    ) {
                        Column {
                            Text(
                                msg.sender,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (alignMe) Color(0xFF2DD4BF) else Color(0xFF06B6D4)
                            )
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
                holder = "Digite oss no chat...",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF14B8A6), RoundedCornerShape(6.dp))
                    .clickable {
                        if (tmsg.isNotEmpty()) {
                            onSend(tmsg)
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
