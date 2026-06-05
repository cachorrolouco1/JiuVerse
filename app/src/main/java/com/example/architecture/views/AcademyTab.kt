package com.example.architecture.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AcademyTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Initialize DB Data
    LaunchedEffect(Unit) {
        viewModel.initializeSensei(context)
    }

    // Reactive states collected from the Room database / ViewModel
    val realAcademies by viewModel.realAcademies.collectAsState()
    val allTournaments by viewModel.realTournaments.collectAsState()
    val selectedAcademyId by viewModel.selectedAcademyId.collectAsState()
    val selectedAcademyStudents by viewModel.selectedAcademyStudents.collectAsState()

    // Find currently selected academy
    val activeAcademy = realAcademies.find { it.id == selectedAcademyId } ?: realAcademies.firstOrNull()

    // Filtered tournaments for selected academy
    val filteredTournaments = allTournaments.filter { it.academyId == (activeAcademy?.id ?: -1) }

    // Internal navigation tabs
    var currentSubTab by remember { mutableStateOf(0) } // 0: Perfis, 1: Alunos, 2: Eventos, 3: Estatísticas & Monetização

    // Local state for API request sub-console logs
    val apiLogs = remember {
        mutableStateListOf<String>(
            "[GET] /api/v1/academies -> 200 OK (3 Academias carregadas da persistência local)",
            "[GET] /api/v1/academies/1/students -> 200 OK (Alunos coletados do banco de dados)"
        )
    }
    
    fun addApiLog(method: String, route: String, status: String, details: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        apiLogs.add(0, "[$timestamp] $method $route -> $status ($details)")
        if (apiLogs.size > 15) {
            apiLogs.removeLast()
        }
    }

    // Register gym form states
    var gymNameInput by remember { mutableStateOf("") }
    var gymCnpjInput by remember { mutableStateOf("") }
    var gymMasterInput by remember { mutableStateOf("") }
    var gymRegionInput by remember { mutableStateOf("") }
    var gymPhoneInput by remember { mutableStateOf("") }
    var gymPlanInput by remember { mutableStateOf("Premium Dojo") }
    val gymPlans = listOf("Plano Inicial", "Premium Dojo", "Franquia Master")
    var isRegisterFormOpen by remember { mutableStateOf(false) }

    // Register student form states
    var studentNameInput by remember { mutableStateOf("") }
    var studentBeltInput by remember { mutableStateOf("Azul") }
    var studentNicknameInput by remember { mutableStateOf("") }
    val beltOptions = listOf("Branca", "Azul", "Roxa", "Marrom", "Preta")

    // Create tournament form states
    var eventTitleInput by remember { mutableStateOf("") }
    var eventTypeInput by remember { mutableStateOf("Campeonato Real") }
    val eventTypes = listOf("Campeonato Real", "Seminário", "Desafio Integrado")
    var eventFeeInput by remember { mutableStateOf("80") }
    var eventBonusInput by remember { mutableStateOf("400") }
    var eventDateInput by remember { mutableStateOf("25/06/2026") }

    // Interactive physical representation space states
    var activeDojoSpace by remember { mutableStateOf(0) } // 0: Fachada, 1: Recepção, 2: Tatame, 3: Hall de Troféus
    var visitorMessage by remember { mutableStateOf("") }
    val guestMessages = remember { mutableStateListOf<String>("Oss! Ótima estrutura!", "Foco total na guarda fechada.", "Grande Mestre presente!") }
    var visitLogCount by remember { mutableStateOf(0) }
    var selectedMatCell by remember { mutableStateOf<Int?>(null) }
    var isRollingActive by remember { mutableStateOf(false) }
    var selectedTrophyId by remember { mutableStateOf<String?>("t1") }
    var entranceLightOn by remember { mutableStateOf(true) }

    // Carlson Gracie Retro Isometric Simulation States (Habbo Style)
    var showIsometricDojoMode by remember { mutableStateOf(true) }
    var selectedIsometricRoom by remember { mutableStateOf("RECEPÇÃO") }
    var playerAvatarStyle by remember { mutableStateOf("Branco 🥋") }
    var playerPositionRoom by remember { mutableStateOf("RECEPÇÃO") }
    var travelStatus by remember { mutableStateOf("") }
    var isTraveling by remember { mutableStateOf(false) }
    var isCarlsonMember by remember { mutableStateOf(false) }
    var carlsonMembersCount by remember { mutableStateOf(28) }
    var carlsonPrestige by remember { mutableStateOf(2450) }
    var carlsonLevel by remember { mutableStateOf(5) }
    var currentNpcDialogue by remember { mutableStateOf("Diana: Olá, seja bem-vindo ao templo Carlson Gracie! Deseja inscrever-se ou ver convites?") }
    var activeNpcName by remember { mutableStateOf("Recepcionista Diana") }
    var activeNpcEmoji by remember { mutableStateOf("👩‍💼") }
    var isNoticeBoardOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(
            title = "Integração de Academias Reais",
            subtitle = "Arquitetura Federada de Perfis Físicos, Alunos, Eventos e Rankings Síncronos"
        )

        // Intro message about the Architectural Vision
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(0.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(10.dp),
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
                    text = "VISÃO DO ARQUITETO ESPORTIVO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Conecte tatames reais ao ecossistema persistente do JiuVerse. Valide registros com verificação oficial, aprove graduações de alunos e sincronize premiações de campeonatos locais via APIs REST.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal navigation tabs for subcategories
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                Pair("🏢 Academias", 0),
                Pair("🥋 Alunos & Vínculos", 1),
                Pair("🏆 Eventos Locais", 2),
                Pair("📊 Estatísticas & Planos", 3)
            )
            items(tabs) { (title, idx) ->
                val isSelected = currentSubTab == idx
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) BlueprintCyan else BlueprintCard,
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            0.5.dp,
                            if (isSelected) BlueprintCyan else BlueprintGridLine,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { currentSubTab = idx }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else BlueprintTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ACADEMY SELECTOR DRAWER (Shown on top if multiple exist)
        if (realAcademies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selecionar Unidade:",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(realAcademies) { academy ->
                        val isSelected = academy.id == activeAcademy?.id
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) BlueprintTeal.copy(alpha = 0.2f) else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    0.5.dp,
                                    if (isSelected) BlueprintTeal else BlueprintGridLine,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    viewModel.selectAcademy(academy.id)
                                    addApiLog("GET", "/api/v1/academies/${academy.id}", "200 OK", "Filiados carregados")
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = academy.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) BlueprintTeal else BlueprintTextSecondary
                                )
                                if (academy.isVerified) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = BlueprintCyan,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // CONTENT BY TAB
        when (currentSubTab) {
            0 -> { // 🏢 TAB: ACADEMIES PORTAL
                // Toggle between Real-world generic layout and Isometric 2.5D Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(BlueprintCard, RoundedCornerShape(4.dp))
                        .border(1.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔌 SIMULADOR DE MUNDO VIRTUAL:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { 
                                showIsometricDojoMode = false 
                                addApiLog("GET", "/api/v1/mode/classic", "200 OK", "Alternado para Painel Clássico")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!showIsometricDojoMode) BlueprintTeal else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(3.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("DASHBOARD REAL", fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { 
                                showIsometricDojoMode = true 
                                addApiLog("GET", "/api/v1/mode/carlson-gracie", "200 OK", "Ativado Modo Imersivo Carlson Gracie, Projetado estilo retro 2.5D")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showIsometricDojoMode) Color(0xFFC62828) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(3.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("IMERSIVO CARLSON 🥋", fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (showIsometricDojoMode) {
                    CarlsonGracieIsometricWorld(
                        selectedRoom = selectedIsometricRoom,
                        onRoomSelected = { selectedIsometricRoom = it },
                        playerAvatarStyle = playerAvatarStyle,
                        onAvatarStyleChanged = { playerAvatarStyle = it },
                        playerPositionRoom = playerPositionRoom,
                        onPlayerRoomChanged = { playerPositionRoom = it },
                        travelStatus = travelStatus,
                        onTravelStatusChanged = { travelStatus = it },
                        isTraveling = isTraveling,
                        onTravelingChanged = { isTraveling = it },
                        isCarlsonMember = isCarlsonMember,
                        onCarlsonMemberChanged = { isCarlsonMember = it },
                        carlsonMembersCount = carlsonMembersCount,
                        onCarlsonMembersCountChanged = { carlsonMembersCount = it },
                        carlsonPrestige = carlsonPrestige,
                        onCarlsonPrestigeChanged = { carlsonPrestige = it },
                        carlsonLevel = carlsonLevel,
                        onCarlsonLevelChanged = { carlsonLevel = it },
                        currentNpcDialogue = currentNpcDialogue,
                        onDialogueChanged = { currentNpcDialogue = it },
                        activeNpcName = activeNpcName,
                        onNpcNameChanged = { activeNpcName = it },
                        activeNpcEmoji = activeNpcEmoji,
                        onNpcEmojiChanged = { activeNpcEmoji = it },
                        isNoticeBoardOpen = isNoticeBoardOpen,
                        onNoticeBoardOpenChanged = { isNoticeBoardOpen = it },
                        addApiLog = { method, route, status, details -> addApiLog(method, route, status, details) },
                        activeAcademyId = activeAcademy?.id ?: -1,
                        viewModel = viewModel
                    )
                } else {
                    Text(
                        text = "1. REPRESENTAÇÃO OFICIAL DO TATAME (MUNDO REAL)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintCyan,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                // Render Selected Academy Card Details
                if (activeAcademy != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                        border = BorderStroke(1.dp, BlueprintGridLine)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏢", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = activeAcademy.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BlueprintTextPrimary
                                        )
                                        Text(
                                            text = "CNPJ: ${activeAcademy.cnpj}",
                                            fontSize = 9.sp,
                                            color = BlueprintTextSecondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                // Verified Badge / System Action
                                if (activeAcademy.isVerified) {
                                    Box(
                                        modifier = Modifier
                                            .background(BlueprintCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, BlueprintCyan, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("VERIFICADA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(BlueprintOrange.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, BlueprintOrange, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("PENDENTE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Details Grid
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Responsável Técnico", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                                    Text(activeAcademy.responsibleMaster, fontSize = 11.sp, color = BlueprintTextPrimary, fontWeight = FontWeight.Bold)
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Text("Região / Estado", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                                    Text(activeAcademy.region, fontSize = 11.sp, color = BlueprintTextPrimary)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Contato de Suporte", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                                    Text(activeAcademy.phone, fontSize = 11.sp, color = BlueprintTextPrimary, fontFamily = FontFamily.Monospace)
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Text("Guilda Síncrona Virtual", fontSize = 8.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                                    Text(activeAcademy.virtualGuildSynced, fontSize = 11.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                }
                            }

                            Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                            // Stats Inline Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("INTEGRANTES", fontSize = 8.sp, color = BlueprintTextSecondary)
                                    Text("${activeAcademy.memberCount} Atletas", fontSize = 11.sp, color = BlueprintTextPrimary, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("SALDO COFRE", fontSize = 8.sp, color = BlueprintTextSecondary)
                                    Text("${activeAcademy.jiuCoinsBalance} JiuCoins", fontSize = 11.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Column {
                                    Text("PLANO CONTRATADO", fontSize = 8.sp, color = BlueprintTextSecondary)
                                    Text(activeAcademy.monetizationPlan, fontSize = 11.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Admin Actions (Verification / Document Check Simulation)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val nextVerifiedState = !activeAcademy.isVerified
                                        viewModel.verifyAcademy(activeAcademy.id, nextVerifiedState)
                                        val statusLabel = if (nextVerifiedState) "VERIFICADO" else "REVOGADO"
                                        addApiLog("POST", "/api/v1/academies/${activeAcademy.id}/verify?status=$nextVerifiedState", "200 OK", "CNPJ autenticado e selo $statusLabel")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeAcademy.isVerified) BlueprintRed.copy(alpha = 0.2f) else BlueprintCyan,
                                        contentColor = if (activeAcademy.isVerified) BlueprintRed else Color.Black
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    val icon = if (activeAcademy.isVerified) Icons.Default.Cancel else Icons.Default.Verified
                                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (activeAcademy.isVerified) "REVOGAR VERIFICAÇÃO" else "APROVAR VERIFICAÇÃO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        addApiLog("GET", "/api/v1/academies/${activeAcademy.id}/synced-status", "200 OK", "Forçado Sync de Rankings e Moedas")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintHeader),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.weight(0.8f)
                                ) {
                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = BlueprintTextPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("FORÇAR SYNC", fontSize = 9.sp, color = BlueprintTextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- PHYSICAL INFRASTRUCTURE SIMULATION OF THE UNIFIED DOJO ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                        border = BorderStroke(1.dp, BlueprintGridLine.copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "🏛️ ESTRUTURA FÍSICA DO TEMPLO (TATAME REAL)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BlueprintCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Modelagem virtual e ambientes físicos da unidade: ${activeAcademy.name}",
                                        fontSize = 9.sp,
                                        color = BlueprintTextSecondary
                                     )
                                }

                                // Small badge showing status of local power/lights
                                Box(
                                    modifier = Modifier
                                        .background(if (entranceLightOn) Color(0xFF065F46) else Color(0xFF7F1D1D), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, if (entranceLightOn) Color(0xFF059669) else Color(0xFFDC2626), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .clickable { entranceLightOn = !entranceLightOn }
                                ) {
                                    Text(
                                        text = if (entranceLightOn) "💡 ENERGIA: ATIVA" else "🌑 APAGADO",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Tab selection for spaces
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val spaces = listOf(
                                    Triple("🚪 FACHADA & LOGO", 0, Color(0xFF06B6D4)),
                                    Triple("🛎️ RECEPÇÃO", 1, Color(0xFF14B8A6)),
                                    Triple("🥋 TATAME", 2, Color(0xFF3B82F6)),
                                    Triple("🏆 TROFÉUS", 3, Color(0xFFF59E0B))
                                )
                                spaces.forEach { (title, id, accent) ->
                                    val isSelected = activeDojoSpace == id
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isSelected) accent.copy(alpha = 0.25f) else Color.Transparent, RoundedCornerShape(4.dp))
                                            .border(0.5.dp, if (isSelected) accent else Color.Transparent, RoundedCornerShape(4.dp))
                                            .clickable { activeDojoSpace = id }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) accent else BlueprintTextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // RENDER ACTIVE SPACE SCENERY
                            when (activeDojoSpace) {
                                0 -> { // FACHADA & LOGO
                                    val logoPreset = when {
                                        activeAcademy.name.contains("Alliance", ignoreCase = true) -> Pair("🦅", "ALLIANCE EAGLE CREST")
                                        activeAcademy.name.contains("Gracie", ignoreCase = true) -> Pair("🛡️", "GRACIE RED SHIELD TRIANGLE")
                                        activeAcademy.name.contains("Checkmat", ignoreCase = true) -> Pair("♞", "CHECKMAT CHESS KNIGHT")
                                        else -> Pair("⛩️", "TRADITIONAL DOJO SIGN")
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                                            .border(1.dp, BlueprintGridLine)
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        // Left Column: Visual Emblem drawing
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                                .border(0.5.dp, BlueprintCyan.copy(alpha = 0.3f))
                                                .padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                             Text(
                                                 text = "EMBLEMA / LOGO OFICIAL",
                                                 fontSize = 8.sp,
                                                 fontWeight = FontWeight.Bold,
                                                 color = BlueprintTextSecondary,
                                                 modifier = Modifier.padding(bottom = 8.dp)
                                             )
                                             Box(
                                                 modifier = Modifier
                                                     .size(56.dp)
                                                     .background(Color.Black, RoundedCornerShape(28.dp))
                                                     .border(2.dp, if (activeAcademy.isVerified) BlueprintCyan else BlueprintOrange, RoundedCornerShape(28.dp)),
                                                 contentAlignment = Alignment.Center
                                             ) {
                                                 Text(logoPreset.first, fontSize = 28.sp)
                                             }
                                             Spacer(modifier = Modifier.height(6.dp))
                                             Text(
                                                 text = logoPreset.second,
                                                 fontSize = 8.sp,
                                                 fontWeight = FontWeight.Bold,
                                                 color = if (activeAcademy.isVerified) BlueprintCyan else BlueprintOrange,
                                                 textAlign = TextAlign.Center
                                             )
                                         }

                                         // Right Column: Information facade details
                                         Column(
                                             modifier = Modifier
                                                 .weight(1.8f)
                                                 .padding(vertical = 4.dp)
                                         ) {
                                             Text(
                                                 text = activeAcademy.name.uppercase(),
                                                 fontSize = 13.sp,
                                                 fontWeight = FontWeight.ExtraBold,
                                                 color = BlueprintTextPrimary
                                             )
                                             Text(
                                                 text = "RESPONSÁVEL: ${activeAcademy.responsibleMaster.uppercase()}",
                                                 fontSize = 9.sp,
                                                 color = BlueprintCyan,
                                                 fontWeight = FontWeight.Bold
                                             )
                                             Spacer(modifier = Modifier.height(8.dp))

                                             // Building facade visual description (Entrance status)
                                             Box(
                                                 modifier = Modifier
                                                     .fillMaxWidth()
                                                     .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                                     .padding(8.dp)
                                             ) {
                                                 Column {
                                                     Row(verticalAlignment = Alignment.CenterVertically) {
                                                         Box(modifier = Modifier.size(6.dp).background(if (entranceLightOn) Color.Green else Color.Gray, RoundedCornerShape(3.dp)))
                                                         Spacer(modifier = Modifier.width(6.dp))
                                                         Text(
                                                             text = "PORTA DE CORRER INTEGRADA: " + if (entranceLightOn) "TRANCADA POR RECONHECIMENTO FACIAL" else "MODO MANUAL DE EMERGÊNCIA",
                                                             fontSize = 7.5.sp,
                                                             fontWeight = FontWeight.Bold,
                                                             color = BlueprintTextPrimary
                                                         )
                                                     }
                                                     Spacer(modifier = Modifier.height(4.dp))
                                                     Text(
                                                         text = "Fachada clássica revestida de painéis acústicos e proteção térmica de tatames. Equipado com controle de presença por NFC federada.",
                                                         fontSize = 8.5.sp,
                                                         color = BlueprintTextSecondary
                                                     )
                                                 }
                                             }

                                             Spacer(modifier = Modifier.height(6.dp))
                                             
                                             // Count of students listed
                                             Text(
                                                 text = "👥 Censo Esportivo Interno: ${selectedAcademyStudents.size} Atletas Cadastrados (Dono: ${activeAcademy.responsibleMaster})",
                                                 fontSize = 8.5.sp,
                                                 fontWeight = FontWeight.Bold,
                                                 color = BlueprintTeal
                                             )
                                         }
                                     }
                                 }

                                 1 -> { // RECEPÇÃO
                                     Row(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                                             .border(1.dp, BlueprintGridLine)
                                             .padding(12.dp),
                                         horizontalArrangement = Arrangement.spacedBy(12.dp)
                                     ) {
                                         Column(modifier = Modifier.weight(1.2f)) {
                                             Text(
                                                 text = "カウンター RECEPÇÃO DIGITAL",
                                                 fontSize = 11.sp,
                                                 fontWeight = FontWeight.ExtraBold,
                                                 color = BlueprintCyan,
                                                 fontFamily = FontFamily.Monospace
                                             )
                                             Text(
                                                 text = "Central de check-in de atletas federados e atendimento. Assine o livro para registrar sua passagem no local!",
                                                 fontSize = 9.sp,
                                                 color = BlueprintTextSecondary,
                                                 modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                                             )

                                             Row(
                                                 modifier = Modifier.fillMaxWidth(),
                                                 horizontalArrangement = Arrangement.spacedBy(6.dp)
                                             ) {
                                                 OutlinedTextField(
                                                     value = visitorMessage,
                                                     onValueChange = { visitorMessage = it },
                                                     placeholder = { Text("Insira sua mensagem...", fontSize = 9.sp, color = Color.Gray) },
                                                     textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 10.sp),
                                                     modifier = Modifier.weight(1.5f),
                                                     singleLine = true,
                                                     shape = RoundedCornerShape(4.dp)
                                                 )
                                                 Button(
                                                     onClick = {
                                                         if (visitorMessage.isNotEmpty()) {
                                                             guestMessages.add(0, "@Visitante: $visitorMessage")
                                                             visitorMessage = ""
                                                             visitLogCount++
                                                             addApiLog("POST", "/api/v1/academies/${activeAcademy.id}/visit-book", "200 OK", "Registrada passagem na recepção")
                                                         }
                                                     },
                                                     colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal, contentColor = Color.Black),
                                                     modifier = Modifier.weight(1f),
                                                     shape = RoundedCornerShape(4.dp)
                                                 ) {
                                                     Text("ASSINAR", fontSize = 8.sp, fontWeight = FontWeight.Black)
                                                 }
                                             }
                                         }

                                         // Log of guest book signings
                                         Column(
                                             modifier = Modifier
                                                 .weight(1f)
                                                 .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                                 .border(0.5.dp, BlueprintGridLine)
                                                 .padding(8.dp)
                                         ) {
                                             Text(
                                                 text = "📔 LIVRO DE VISITAS (${visitLogCount + guestMessages.size})",
                                                 fontSize = 8.sp,
                                                 fontWeight = FontWeight.Bold,
                                                 color = BlueprintTeal
                                             )
                                             Spacer(modifier = Modifier.height(4.dp))
                                             LazyColumn(
                                                 modifier = Modifier.height(68.dp),
                                                 verticalArrangement = Arrangement.spacedBy(4.dp)
                                             ) {
                                                 items(guestMessages) { msg ->
                                                     Text(
                                                         text = msg,
                                                         fontSize = 8.sp,
                                                         color = BlueprintTextPrimary,
                                                         fontFamily = FontFamily.Monospace
                                                     )
                                                 }
                                             }
                                         }
                                     }
                                 }

                                 2 -> { // TATAME (TATAMI)
                                     Column(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                                             .border(1.dp, BlueprintGridLine)
                                             .padding(12.dp)
                                     ) {
                                         Row(
                                             modifier = Modifier.fillMaxWidth(),
                                             horizontalArrangement = Arrangement.SpaceBetween,
                                             verticalAlignment = Alignment.CenterVertically
                                         ) {
                                             Column {
                                                 Text(
                                                     text = "🥋 ÁREA DE ROLLS (CONCENTRAÇÃO DO TATAME)",
                                                     fontSize = 10.sp,
                                                     fontWeight = FontWeight.Bold,
                                                     color = BlueprintCyan
                                                 )
                                                 Text(
                                                     text = "Clique em uma célula do tatame para iniciar um treino",
                                                     fontSize = 8.5.sp,
                                                     color = BlueprintTextSecondary
                                                 )
                                             }

                                             // Timer logic
                                             Button(
                                                 onClick = { isRollingActive = !isRollingActive },
                                                 colors = ButtonDefaults.buttonColors(containerColor = if (isRollingActive) BlueprintRed else BlueprintTeal, contentColor = Color.Black),
                                                 shape = RoundedCornerShape(4.dp),
                                                 contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                 modifier = Modifier.height(24.dp)
                                             ) {
                                                 Text(
                                                     text = if (isRollingActive) "⏱️ PARAR ROLL" else "⏱️ INICIAR ROLAR COMUNITÁRIO (5 MIN)",
                                                     fontSize = 8.sp,
                                                     fontWeight = FontWeight.Bold
                                                 )
                                             }
                                         }

                                         Spacer(modifier = Modifier.height(8.dp))

                                         if (isRollingActive) {
                                             Box(
                                                 modifier = Modifier
                                                     .fillMaxWidth()
                                                     .background(Color(0xFF1E1B4B), RoundedCornerShape(4.dp))
                                                     .border(0.5.dp, BlueprintCyan, RoundedCornerShape(4.dp))
                                                     .padding(vertical = 4.dp),
                                                 contentAlignment = Alignment.Center
                                             ) {
                                                 Text("⏳ O CRONÔMETRO ESTÁ ROLANDO! Alunos simulando cansaço de guardeiro...", fontSize = 8.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                                             }
                                             Spacer(modifier = Modifier.height(8.dp))
                                         }

                                         // Grid layout of Tatami Mats
                                         Row(
                                             modifier = Modifier.fillMaxWidth(),
                                             horizontalArrangement = Arrangement.spacedBy(8.dp),
                                             verticalAlignment = Alignment.CenterVertically
                                         ) {
                                             Column(
                                                 modifier = Modifier.weight(1.3f),
                                                 verticalArrangement = Arrangement.spacedBy(4.dp)
                                             ) {
                                                 for (row in 0 until 3) {
                                                     Row(
                                                         modifier = Modifier.fillMaxWidth(),
                                                         horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                     ) {
                                                         for (col in 0 until 5) {
                                                             val idx = row * 5 + col
                                                             val isSelected = selectedMatCell == idx
                                                             
                                                             val studentAtCell = if (idx < selectedAcademyStudents.size) selectedAcademyStudents[idx] else null
                                                             val iconAtCell = when {
                                                                 studentAtCell != null -> "🥋"
                                                                 idx == 7 -> "👴"
                                                                 else -> ""
                                                             }

                                                             Box(
                                                                 modifier = Modifier
                                                                     .weight(1f)
                                                                     .height(26.dp)
                                                                     .background(
                                                                         if (isSelected) Color(0xFF1E40AF)
                                                                         else if (iconAtCell.isNotEmpty()) Color(0xFF1E293B)
                                                                         else Color(0xFF0F172A),
                                                                         RoundedCornerShape(3.dp)
                                                                     )
                                                                     .border(
                                                                         0.5.dp,
                                                                         if (isSelected) BlueprintCyan else Color(0xFF334155),
                                                                         RoundedCornerShape(3.dp)
                                                                     )
                                                                     .clickable {
                                                                         selectedMatCell = idx
                                                                         if (studentAtCell != null) {
                                                                             addApiLog("POST", "/api/v1/students/${studentAtCell.studentId}/call-roll", "200 OK", "Chamou ${studentAtCell.name} para o combate!")
                                                                         } else {
                                                                             addApiLog("POST", "/api/v1/academies/tatami-click/$idx", "200 OK", "Ajustou tatame na posição $idx")
                                                                         }
                                                                     },
                                                                 contentAlignment = Alignment.Center
                                                             ) {
                                                                 if (iconAtCell.isNotEmpty()) {
                                                                     Text(iconAtCell, fontSize = 11.sp)
                                                                 } else {
                                                                     Text("", fontSize = 8.sp)
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 }
                                             }

                                             // Info panel on right side of tatame
                                             Column(
                                                 modifier = Modifier
                                                     .weight(1.5f)
                                                     .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                                     .border(0.5.dp, BlueprintGridLine)
                                                     .padding(8.dp)
                                             ) {
                                                 Text(
                                                     text = "📜 CÚPULA DE REGRAS DO CLÃ",
                                                     fontSize = 8.sp,
                                                     fontWeight = FontWeight.Bold,
                                                     color = BlueprintOrange
                                                 )
                                                 
                                                 val rules = listOf(
                                                     "1. Respeite as graduações",
                                                     "2. Unhas cortadas sempre",
                                                     "3. Kimono limpo obrigatório",
                                                     "4. Bateu, parou! (Tap ou Oss)"
                                                 )
                                                 rules.forEach { rule ->
                                                     Text(
                                                         text = rule,
                                                         fontSize = 8.sp,
                                                         color = BlueprintTextSecondary,
                                                         modifier = Modifier.padding(top = 2.dp)
                                                     )
                                                 }

                                                 if (selectedMatCell != null) {
                                                     val clickedIdx = selectedMatCell!!
                                                     val studentAtCell = if (clickedIdx < selectedAcademyStudents.size) selectedAcademyStudents[clickedIdx] else null
                                                     Spacer(modifier = Modifier.height(4.dp))
                                                     Text(
                                                         text = if (studentAtCell != null) "👉 Aluno: @${studentAtCell.virtualNickname} (${studentAtCell.belt})" else "👉 Tatame livre #$clickedIdx",
                                                         fontSize = 8.sp,
                                                         color = BlueprintCyan,
                                                         fontWeight = FontWeight.Bold,
                                                         fontFamily = FontFamily.Monospace
                                                     )
                                                 }
                                             }
                                         }
                                     }
                                 }

                                 3 -> { // Hall de Troféus
                                     Row(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                                             .border(1.dp, BlueprintGridLine)
                                             .padding(12.dp),
                                         horizontalArrangement = Arrangement.spacedBy(12.dp)
                                     ) {
                                         Column(
                                             modifier = Modifier.weight(1.3f)
                                         ) {
                                             Text(
                                                 text = "🏆 ARMÁRIO DE HONRA DE ${activeAcademy.name.uppercase()}",
                                                 fontSize = 11.sp,
                                                 fontWeight = FontWeight.Bold,
                                                 color = BlueprintCyan
                                             )
                                             Text(
                                                 text = "Histórico de conquistas atestadas em conformidade federada para fins de reputação e bônus.",
                                                 fontSize = 9.sp,
                                                 color = BlueprintTextSecondary,
                                                 modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                                             )

                                             Row(
                                                 horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                 modifier = Modifier.fillMaxWidth()
                                             ) {
                                                 val trophies = listOf(
                                                     Pair("🏆", "t1"),
                                                     Pair("🥇", "t2"),
                                                     Pair("🎖️", "t3"),
                                                     Pair("🏅", "t4")
                                                 )
                                                 trophies.forEach { (emoji, tid) ->
                                                     val isSelected = selectedTrophyId == tid
                                                     Box(
                                                         modifier = Modifier
                                                             .size(36.dp)
                                                             .background(if (isSelected) Color(0xFF3F2D0B) else Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                                             .border(1.dp, if (isSelected) Color(0xFFF59E0B) else Color(0xFF334155), RoundedCornerShape(6.dp))
                                                             .clickable { selectedTrophyId = tid },
                                                         contentAlignment = Alignment.Center
                                                     ) {
                                                         Text(emoji, fontSize = 20.sp)
                                                     }
                                                 }
                                             }
                                         }

                                         // Trophy Description Panel
                                         Column(
                                             modifier = Modifier
                                                 .weight(1f)
                                                 .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                                 .border(0.5.dp, BlueprintGridLine)
                                                 .padding(8.dp)
                                         ) {
                                             val details = when (selectedTrophyId) {
                                                 "t1" -> Triple("Taça Real Alliance / Gracie", "Vencida nas seletivas abertas locais. Recompensa: +1500 JiuCoins federados.", Color(0xFFF59E0B))
                                                 "t2" -> Triple("Medalha do Fundador", "Concedida em reconhecimento de disseminação de jiu-jitsu de forma idônea nacional.", Color(0xFF10B981))
                                                 "t3" -> Triple("Selo de Dojo Premium", "Atribuído a academias com plano ativo e mais de 100 membros no ambiente virtual.", Color(0xFF06B6D4))
                                                 "t4" -> Triple("Laureia Anti-Cheat", "Trophy conferido a dancings e transações cripto-esportivas sem incidentes.", Color(0xFF8B5CF6))
                                                 else -> Triple("Selecione um Troféu", "Clique em um troféu para inspecionar sua lenda histórica de conquistas.", Color(0xFF94A3B8))
                                             }

                                             Text(
                                                 text = details.first.uppercase(),
                                                 fontSize = 8.sp,
                                                 fontWeight = FontWeight.Black,
                                                 color = details.third
                                             )
                                             Spacer(modifier = Modifier.height(2.dp))
                                             Text(
                                                 text = details.second,
                                                 fontSize = 8.sp,
                                                 color = BlueprintTextSecondary
                                             )
                                             
                                             Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                                             
                                             Text(
                                                 text = "Rendimento Real: ${activeAcademy.realRankPoints} pts",
                                                 fontSize = 8.sp,
                                                 fontWeight = FontWeight.Bold,
                                                 color = BlueprintTextPrimary,
                                                 fontFamily = FontFamily.Monospace
                                             )
                                             Text(
                                                 text = "Rendimento Virtual: ${activeAcademy.virtualRankPoints} pts",
                                                 fontSize = 8.sp,
                                                 fontWeight = FontWeight.Bold,
                                                 color = BlueprintCyan,
                                                 fontFamily = FontFamily.Monospace
                                             )
                                         }
                                     }
                                 }
                             }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // BUTTON to trigger expansion of "Register New Academy"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. QUER VINCULAR SEU TATAME REAL?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintTextSecondary
                    )
                    Button(
                        onClick = { isRegisterFormOpen = !isRegisterFormOpen },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRegisterFormOpen) Color.DarkGray else Color(0xFF1E293B)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(if (isRegisterFormOpen) "FECHAR FORM" else "CADASTRAR CADEMIA", fontSize = 9.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                    }
                }

                AnimatedVisibility(visible = isRegisterFormOpen) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                        border = BorderStroke(1.dp, BlueprintGridLine)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Registro Federado de Nova Unidade Síncrona", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Nome Oficial da Academia", fontSize = 8.sp, color = BlueprintTextSecondary)
                            OutlinedTextField(
                                value = gymNameInput,
                                onValueChange = { gymNameInput = it },
                                textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                singleLine = true,
                                placeholder = { Text("Ex: Gracie Barra Copacabana", fontSize = 11.sp, color = Color.Gray) }
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("CNPJ", fontSize = 8.sp, color = BlueprintTextSecondary)
                                    OutlinedTextField(
                                        value = gymCnpjInput,
                                        onValueChange = { gymCnpjInput = it },
                                        textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(4.dp),
                                        singleLine = true,
                                        placeholder = { Text("00.000.000/0001-00", fontSize = 11.sp, color = Color.Gray) }
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Mestre Responsável (Selo Preto)", fontSize = 8.sp, color = BlueprintTextSecondary)
                                    OutlinedTextField(
                                        value = gymMasterInput,
                                        onValueChange = { gymMasterInput = it },
                                        textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(4.dp),
                                        singleLine = true,
                                        placeholder = { Text("Ex: Roger Gracie", fontSize = 11.sp, color = Color.Gray) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Cidade / Estado", fontSize = 8.sp, color = BlueprintTextSecondary)
                                    OutlinedTextField(
                                        value = gymRegionInput,
                                        onValueChange = { gymRegionInput = it },
                                        textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(4.dp),
                                        singleLine = true,
                                        placeholder = { Text("Ex: Curitiba, PR", fontSize = 11.sp, color = Color.Gray) }
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Telefone Comercial", fontSize = 8.sp, color = BlueprintTextSecondary)
                                    OutlinedTextField(
                                        value = gymPhoneInput,
                                        onValueChange = { gymPhoneInput = it },
                                        textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(4.dp),
                                        singleLine = true,
                                        placeholder = { Text("(41) 99999-8888", fontSize = 11.sp, color = Color.Gray) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Selecione o Modelo de Licenciamento", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                gymPlans.forEach { plan ->
                                    val isSelected = gymPlanInput == plan
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) BlueprintOrange.copy(alpha = 0.15f) else Color.Transparent,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                0.5.dp,
                                                if (isSelected) BlueprintOrange else BlueprintGridLine,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable { gymPlanInput = plan }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = plan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) BlueprintOrange else BlueprintTextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Submit register button
                            Button(
                                onClick = {
                                    if (gymNameInput.isNotEmpty() && gymCnpjInput.isNotEmpty()) {
                                        val planPrice = when(gymPlanInput) {
                                            "Plano Inicial" -> 99
                                            "Premium Dojo" -> 199
                                            else -> 350
                                        }
                                        viewModel.registerAcademy(
                                            name = gymNameInput,
                                            cnpj = gymCnpjInput,
                                            master = if (gymMasterInput.isEmpty()) "Mestre Filiado" else gymMasterInput,
                                            region = if (gymRegionInput.isEmpty()) "Brasil" else gymRegionInput,
                                            phone = if (gymPhoneInput.isEmpty()) "999-9999" else gymPhoneInput,
                                            plan = gymPlanInput,
                                            price = planPrice
                                        )
                                        addApiLog("POST", "/api/v1/academies/register", "201 Created", "Academia '$gymNameInput' federada pendente de validação")
                                        
                                        // Reset fields
                                        gymNameInput = ""
                                        gymCnpjInput = ""
                                        gymMasterInput = ""
                                        gymRegionInput = ""
                                        gymPhoneInput = ""
                                        isRegisterFormOpen = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal, contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                enabled = gymNameInput.isNotEmpty() && gymCnpjInput.isNotEmpty()
                            ) {
                                Text("SOLICITAR REPRESENTAÇÃO OFICIAL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            }

            1 -> { // 🥋 TAB: STUDENT MANAGEMENT
                Text(
                    text = "GESTÃO DE ATLETAS E VÍNCULOS DE FAIXA (REAL-TO-VIRTUAL)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (activeAcademy == null) {
                    Text("Nenhuma academia no banco de dados.", color = BlueprintTextSecondary, fontSize = 11.sp)
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                        border = BorderStroke(1.dp, BlueprintGridLine)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "No JiuVerse, donos de academia aprovam vínculos de alunos para verificação oficial de suas faixas de forma idônea, ganhando proteção federada contra falsas graduações.",
                                fontSize = 10.sp,
                                color = BlueprintTextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Table of students
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A))
                                    .padding(6.dp)
                            ) {
                                Text("Lutador", modifier = Modifier.weight(1.2f), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                                Text("Faixa", modifier = Modifier.weight(0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                                Text("Identidade Virtual", modifier = Modifier.weight(1.1f), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                                Text("Ações", modifier = Modifier.weight(1f), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary, textAlign = TextAlign.Center)
                            }

                            if (selectedAcademyStudents.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Nenhum atleta federado nesta unidade ainda.", fontSize = 10.sp, color = BlueprintTextSecondary)
                                }
                            } else {
                                selectedAcademyStudents.forEach { student ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.2f)) {
                                            Text(student.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                            Text(
                                                text = if (student.registrationApproved) "Selo Federado" else "Pendente",
                                                fontSize = 8.sp,
                                                color = if (student.registrationApproved) BlueprintTeal else BlueprintOrange,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Belt indicator badge with visual colors
                                        Box(modifier = Modifier.weight(0.7f)) {
                                            val beltColor = when (student.belt) {
                                                "Preta" -> Color.Black
                                                "Marrom" -> Color(0xFF78350F)
                                                "Roxa" -> Color(0xFF6B21A8)
                                                "Azul" -> Color(0xFF1D4ED8)
                                                else -> Color.White
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(beltColor, RoundedCornerShape(3.dp))
                                                    .border(0.5.dp, Color.Gray, RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = student.belt.uppercase(),
                                                    fontSize = 7.sp,
                                                    color = if (student.belt == "Branca") Color.Black else Color.White,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }

                                        Text(
                                            text = "@${student.virtualNickname}",
                                            modifier = Modifier.weight(1.1f),
                                            fontSize = 10.sp,
                                            color = BlueprintCyan,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )

                                        // Actions Row
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (!student.registrationApproved) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                                                        .clickable {
                                                            viewModel.approveStudent(student.studentId, true)
                                                            addApiLog("POST", "/api/v1/students/${student.studentId}/verify?approved=true", "200 OK", "Faixa graduada autenticada via hash")
                                                        }
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("APROVAR", fontSize = 7.3.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Expel",
                                                tint = BlueprintRed,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        viewModel.expelStudent(student.studentId)
                                                        addApiLog("DELETE", "/api/v1/students/${student.studentId}", "200 OK", "Filiado removido do censo esportivo")
                                                    }
                                            )
                                        }
                                    }
                                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Inscribe Student Form
                            Text("Matricular Novo Aluno Físico", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = studentNameInput,
                                    onValueChange = { studentNameInput = it },
                                    label = { Text("Nome Completo", fontSize = 9.sp) },
                                    textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(4.dp)
                                )

                                OutlinedTextField(
                                    value = studentNicknameInput,
                                    onValueChange = { studentNicknameInput = it },
                                    label = { Text("ID Virtual (@)", fontSize = 9.sp) },
                                    textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Faixa:", fontSize = 9.sp, color = BlueprintTextSecondary)
                                    beltOptions.forEach { belt ->
                                        val isSelected = studentBeltInput == belt
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) BlueprintTeal.copy(alpha = 0.2f) else Color.Transparent,
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .border(
                                                    0.5.dp,
                                                    if (isSelected) BlueprintTeal else BlueprintGridLine,
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .clickable { studentBeltInput = belt }
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(belt, fontSize = 7.5.sp, color = if (isSelected) BlueprintTeal else BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (studentNameInput.isNotEmpty() && studentNicknameInput.isNotEmpty()) {
                                            viewModel.enrollStudentToAcademy(
                                                academyId = activeAcademy.id,
                                                name = studentNameInput,
                                                belt = studentBeltInput,
                                                nickname = studentNicknameInput.replace("@", ""),
                                                approved = false
                                            )
                                            addApiLog("POST", "/api/v1/students/enroll", "201 Created", "Aluno '$studentNameInput' cadastrado como faixa $studentBeltInput")
                                            
                                            // Reset inputs
                                            studentNameInput = ""
                                            studentNicknameInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal, contentColor = Color.Black),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(34.dp),
                                    enabled = studentNameInput.isNotEmpty() && studentNicknameInput.isNotEmpty()
                                ) {
                                    Text("SALVAR", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            2 -> { // 🏆 TAB: DISCLOSE TOURNAMENTS
                Text(
                    text = "DIVULGAÇÃO E CO-ORGANIZAÇÃO DE CAMPEONATOS OFICIAIS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (activeAcademy == null) {
                    Text("Nenhuma unidade de suporte carregada.", color = BlueprintTextSecondary, fontSize = 11.sp)
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                        border = BorderStroke(1.dp, BlueprintGridLine)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Promova competições locais no calendário central. O ingresso em Real (BRL) recolhe taxas automatizadas para o pool de liquidez e recompensa os lutadores sob a forma de Airdrops em JiuCoins.",
                                fontSize = 10.sp,
                                color = BlueprintTextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // List tournaments
                            if (filteredTournaments.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Nenhum evento registrado no momento por esta unidade.", fontSize = 10.sp, color = BlueprintTextSecondary)
                                }
                            } else {
                                filteredTournaments.forEach { event ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.5f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            if (event.eventType.contains("Camp")) BlueprintOrange.copy(alpha = 0.15f) else BlueprintTeal.copy(alpha = 0.15f),
                                                            RoundedCornerShape(3.dp)
                                                        )
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(event.eventType.uppercase(), fontSize = 6.5.sp, color = if (event.eventType.contains("Camp")) BlueprintOrange else BlueprintTeal, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(event.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                            }
                                            Text("Data: ${event.eventDate} • Status: ${event.status}", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                                        }

                                        Row(
                                            modifier = Modifier.weight(1.1f),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Matrícula: R$ ${event.entryFeeBrl}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                                Text("Bônus: +${event.virtualSyncBonus} JC", fontSize = 8.5.sp, color = BlueprintTeal, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Cancel",
                                                tint = BlueprintRed.copy(alpha = 0.8f),
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        viewModel.deleteTournamentFromAcademy(event.eventId)
                                                        addApiLog("DELETE", "/api/v1/events/${event.eventId}", "200 OK", "Campeonato cancelado e ingressos estornados")
                                                    }
                                            )
                                        }
                                    }
                                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action to schedule a tournament
                            Text("Divulgar Novo Evento do Dojo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = eventTitleInput,
                                onValueChange = { eventTitleInput = it },
                                label = { Text("Nome do Campeonato ou Seminário", fontSize = 9.sp) },
                                textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(4.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = eventFeeInput,
                                    onValueChange = { eventFeeInput = it },
                                    label = { Text("Inscrição (R$)", fontSize = 9.sp) },
                                    textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(4.dp)
                                )

                                OutlinedTextField(
                                    value = eventBonusInput,
                                    onValueChange = { eventBonusInput = it },
                                    label = { Text("Bônus JiuCoins", fontSize = 9.sp) },
                                    textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                                    modifier = Modifier.weight(1.1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(4.dp)
                                )

                                OutlinedTextField(
                                    value = eventDateInput,
                                    onValueChange = { eventDateInput = it },
                                    label = { Text("Data Prevista", fontSize = 9.sp) },
                                    textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Tipo:", fontSize = 9.sp, color = BlueprintTextSecondary)
                                    eventTypes.forEach { type ->
                                        val isSelected = eventTypeInput == type
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) BlueprintOrange.copy(alpha = 0.2f) else Color.Transparent,
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .border(
                                                    0.5.dp,
                                                    if (isSelected) BlueprintOrange else BlueprintGridLine,
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .clickable { eventTypeInput = type }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(type, fontSize = 7.5.sp, color = if (isSelected) BlueprintOrange else BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (eventTitleInput.isNotEmpty()) {
                                            val fee = eventFeeInput.toIntOrNull() ?: 50
                                            val bonus = eventBonusInput.toIntOrNull() ?: 200
                                            viewModel.addTournamentToAcademy(
                                                academyId = activeAcademy.id,
                                                title = eventTitleInput,
                                                type = eventTypeInput,
                                                fee = fee,
                                                bonus = bonus,
                                                date = eventDateInput
                                            )
                                            addApiLog("POST", "/api/v1/events/create", "201 Created", "Torneio '$eventTitleInput' injetado no calendário global")
                                            
                                            // Reset
                                            eventTitleInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueprintOrange, contentColor = Color.Black),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(34.dp),
                                    enabled = eventTitleInput.isNotEmpty()
                                ) {
                                    Text("AGENDAR", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            3 -> { // 📊 TAB: STATS & MONETIZATION PLANS
                Text(
                    text = "ESTATÍSTICAS CO-AUTORITATIVAS E MODELO DE MONETIZAÇÃO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Real vs Virtual Ranking Comparison Card
                    Card(
                        modifier = Modifier.weight(1.1f),
                        colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                        border = BorderStroke(1.dp, BlueprintGridLine)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Comparação de Rankings", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Ranking Real acumulado nos tatames físicos vs Ranking Virtual acumulado em ligas e disputas online.", fontSize = 8.sp, color = BlueprintTextSecondary)
                            
                            Spacer(modifier = Modifier.height(10.dp))

                            realAcademies.forEachIndexed { index, gym ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("#${index + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), color = BlueprintOrange, fontFamily = FontFamily.Monospace)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(gym.name, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("Físico: ${gym.realRankPoints} pts", fontSize = 8.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                            Text("Virtual: ${gym.virtualRankPoints} pts", fontSize = 8.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                            }
                        }
                    }

                    // Monetization breakdown diagram Card
                    Card(
                        modifier = Modifier.weight(0.9f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1D24)),
                        border = BorderStroke(1.dp, BlueprintTeal.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CurrencyExchange, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Split de Receitas", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Unidades de alto calibre usufruem do sistema de franquia e split inteligente:", fontSize = 8.sp, color = BlueprintTextSecondary)

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp)).padding(6.dp)) {
                                    Column {
                                        Text("RESERVA DO JIUVERSE: 10%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                                        Text("Revertido em infra de servidores", fontSize = 7.sp, color = BlueprintTextSecondary)
                                    }
                                }
                                Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp)).padding(6.dp)) {
                                    Column {
                                        Text("AIRDROPS DE ATLETAS: 5%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                                        Text("Convertido em bolsas JiuCoins", fontSize = 7.sp, color = BlueprintTextSecondary)
                                    }
                                }
                                Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp)).padding(6.dp)) {
                                    Column {
                                        Text("REPASSE DO MESTRE: 85%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                                        Text("Depositado em BRL ou JiuTokens", fontSize = 7.sp, color = BlueprintTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // REST API CONSOLE (REAL-TIME CALL REVEAL)
        Text(
            text = "📡 TERMINAL INTEGRADO DE REQUISÍÇÕES DE APIs (JiuVerse Sync)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = BorderStroke(1.dp, BlueprintGridLine)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONSOLE TRANSMISSÃO DE DADOS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "STATUS: ONLINE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintTeal,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    apiLogs.forEach { log ->
                        Text(
                            text = log,
                            fontSize = 8.3.sp,
                            color = if (log.contains("201")) BlueprintOrange else if (log.contains("DELETE")) BlueprintRed else Color.LightGray,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Helper data class for isometric map config
data class RoomTileInfo(
    val name: String,
    val pctX: Float,
    val pctY: Float,
    val emoji: String,
    val label: String
)

@Composable
fun CarlsonGracieIsometricWorld(
    selectedRoom: String,
    onRoomSelected: (String) -> Unit,
    playerAvatarStyle: String,
    onAvatarStyleChanged: (String) -> Unit,
    playerPositionRoom: String,
    onPlayerRoomChanged: (String) -> Unit,
    travelStatus: String,
    onTravelStatusChanged: (String) -> Unit,
    isTraveling: Boolean,
    onTravelingChanged: (Boolean) -> Unit,
    isCarlsonMember: Boolean,
    onCarlsonMemberChanged: (Boolean) -> Unit,
    carlsonMembersCount: Int,
    onCarlsonMembersCountChanged: (Int) -> Unit,
    carlsonPrestige: Int,
    onCarlsonPrestigeChanged: (Int) -> Unit,
    carlsonLevel: Int,
    onCarlsonLevelChanged: (Int) -> Unit,
    currentNpcDialogue: String,
    onDialogueChanged: (String) -> Unit,
    activeNpcName: String,
    onNpcNameChanged: (String) -> Unit,
    activeNpcEmoji: String,
    onNpcEmojiChanged: (String) -> Unit,
    isNoticeBoardOpen: Boolean,
    onNoticeBoardOpenChanged: (Boolean) -> Unit,
    addApiLog: (String, String, String, String) -> Unit,
    activeAcademyId: Int,
    viewModel: ArchitectureViewModel
) {
    val travelScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0F10)) // Hex listed in prompt: #0F0F10 (Black)
            .border(1.dp, Color(0xFFC62828), RoundedCornerShape(4.dp)) // Chrome/Red frame
            .padding(12.dp)
    ) {
        // --- Carlson Gracie Top Banner info ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF232323), RoundedCornerShape(4.dp)) // #232323 (Dark Grey)
                .border(0.5.dp, Color(0xFFFFD700), RoundedCornerShape(4.dp)) // Gold line
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🥋", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ACADEMIA CARLSON GRACIE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFC62828), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("NÍVEL $carlsonLevel", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Text(
                        text = "Dono Oficial: Mestre Admin • Unidade Virtual Síncrona Registrada",
                        fontSize = 9.sp,
                        color = Color(0xFF8B8B8B) // Silver
                    )
                }
            }

            // Prestige Indicators & Notice Toggle
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("FILIADOS", fontSize = 8.sp, color = Color(0xFF8B8B8B))
                    Text("$carlsonMembersCount / 50 Amigos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("PRESTÍGIO CLÃ", fontSize = 8.sp, color = Color(0xFF8B8B8B))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 9.sp)
                        Text("$carlsonPrestige pts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700)) // Gold
                    }
                }
                Button(
                    onClick = { onNoticeBoardOpenChanged(!isNoticeBoardOpen) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF323232)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("📋 MURAL", fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Side-by-Side: Map on Left, Control Dialogue actions on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // LEFT COLUMN (WEIGHT 1.2): Isometric map layout of the 8 Rooms
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .height(290.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F10)),
                border = BorderStroke(1.dp, Color(0xFF232323))
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val width = maxWidth
                    val height = maxHeight
                    val density = LocalDensity.current
                    val step = with(density) { 45.dp.toPx() }

                    // Isometric tile projection background grid lines
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        for (i in -10..20) {
                            val lineOffset = i * step
                            drawLine(
                                color = Color(0xFF1E1E1F),
                                start = androidx.compose.ui.geometry.Offset(lineOffset, 0f),
                                end = androidx.compose.ui.geometry.Offset(lineOffset + this.size.height * 1.5f, this.size.height),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color(0xFF1E1E1F),
                                start = androidx.compose.ui.geometry.Offset(lineOffset, this.size.height),
                                end = androidx.compose.ui.geometry.Offset(lineOffset + this.size.height * 1.5f, 0f),
                                strokeWidth = 1f
                            )
                        }
                    }

                    // Labels & Floor logo
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CARLSON GRACIE",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFC62828).copy(alpha = 0.15f),
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 2.sp
                        )
                    }

                    // Defined isometric room items
                    val roomsConfig = listOf(
                        RoomTileInfo("RECEPÇÃO", 8f, 62f, "🛎️", "1. RECEPÇÃO"),
                        RoomTileInfo("TATAME PRINCIPAL", 36f, 38f, "🥋", "2. TATAME CENTRAL"),
                        RoomTileInfo("ÁREA DE AULA", 68f, 55f, "🎒", "3. ÁREA DE AULA"),
                        RoomTileInfo("VESTIÁRIOS", 8f, 16f, "👕", "4. VESTIÁRIOS"),
                        RoomTileInfo("HALL DE TROFÉUS", 36f, 10f, "🏆", "5. HISTÓRICO"),
                        RoomTileInfo("SALA DO MESTRE", 68f, 14f, "👴", "6. SALA DO MESTRE"),
                        RoomTileInfo("SALA VIP", 72f, 36f, "🛋️", "7. SALA VIP"),
                        RoomTileInfo("LOJA DA ACADEMIA", 42f, 72f, "🏪", "8. LOJA CLÃ")
                    )

                    roomsConfig.forEach { room ->
                        val isSelected = selectedRoom == room.name
                        val isPlayerHere = playerPositionRoom == room.name

                        val roomBg = when (room.name) {
                            "TATAME PRINCIPAL" -> Color(0xFF18181A)
                            "SALA VIP" -> Color(0xFF1A1625)
                            "LOJA DA ACADEMIA" -> Color(0xFF1F1D1D)
                            "HALL DE TROFÉUS" -> Color(0xFF1C1B17)
                            else -> Color(0xFF232323)
                        }

                        val roomBorderColor = when {
                            isSelected -> Color(0xFFC62828) // Red selection
                            isPlayerHere -> Color(0xFFFFD700) // Gold for walking avatar present
                            else -> Color(0xFF3A3A3A)
                        }

                        val roomBorderWidth = if (isSelected || isPlayerHere) 2.dp else 1.dp

                        // Offset positioned box
                        val xOffset = width * (room.pctX / 100f)
                        val yOffset = height * (room.pctY / 100f)

                        Box(
                            modifier = Modifier
                                .absoluteOffset(x = xOffset, y = yOffset)
                                .size(width = 115.dp, height = 52.dp)
                                .background(roomBg, RoundedCornerShape(3.dp))
                                .border(roomBorderWidth, roomBorderColor, RoundedCornerShape(3.dp))
                                .clickable {
                                    if (!isTraveling && playerPositionRoom != room.name) {
                                        onTravelingChanged(true)
                                        onTravelStatusChanged("Caminhando até ${room.name}... 🏃‍♂️")
                                        onRoomSelected(room.name)
                                        travelScope.launch {
                                            // Simulated delay for habbo walk state
                                            kotlinx.coroutines.delay(1000)
                                            onPlayerRoomChanged(room.name)
                                            onTravelingChanged(false)
                                            onTravelStatusChanged("")

                                            // Trigger speech dialog & state based on room
                                            when (room.name) {
                                                "RECEPÇÃO" -> {
                                                    onNpcNameChanged("Recepcionista Diana")
                                                    onNpcEmojiChanged("Diana 👩‍💼")
                                                    onDialogueChanged("Diana: Olá, amigão! Quer conferir estatísticas do clã Carlson Gracie, filiar-se ou gerenciar convites?")
                                                    addApiLog("GET", "/api/v1/carlson/reception", "200 OK", "Navegação síncrona para Recepção")
                                                }
                                                "TATAME PRINCIPAL" -> {
                                                    onNpcNameChanged("Combate e Rola")
                                                    onNpcEmojiChanged("Tatame 🥋")
                                                    onDialogueChanged("Tatame Central: Bem-vindo à lona sagrada Carlson Gracie! Mantenha a postura. Pronto para suar o kimono?")
                                                    addApiLog("GET", "/api/v1/carlson/tatame", "200 OK", "Passos síncronos no Tatame Central")
                                                }
                                                "ÁREA DE AULA" -> {
                                                    onNpcNameChanged("Instrutor Henrique")
                                                    onNpcEmojiChanged("Henrique 🥋")
                                                    onDialogueChanged("Henrique: Oss! Preparado para ajustar as raspagens e finalizar nos ataques de braço? Use nossos sacos ou lute!")
                                                    addApiLog("GET", "/api/v1/carlson/classroom", "200 OK", "Lutador ocupou a Área de Aula")
                                                }
                                                "VESTIÁRIOS" -> {
                                                    onNpcNameChanged("Camareiro Cabine")
                                                    onNpcEmojiChanged("Cabines 👕")
                                                    onDialogueChanged("Vestiário: Equipamentos reforçados. Troque seu kimono ou mude seu estilo de combate no MMORPG.")
                                                    addApiLog("GET", "/api/v1/carlson/vestiary", "200 OK", "Lutador entrou no vestiário")
                                                }
                                                "HALL DE TROFÉUS" -> {
                                                    onNpcNameChanged("Legado Carlson Gracie")
                                                    onNpcEmojiChanged("Troféus 🏆")
                                                    onDialogueChanged("Hall Histórico: Veja as relíquias de conquistas imortais que pavimentaram a história do vale-tudo e jiu-jitsu.")
                                                    addApiLog("GET", "/api/v1/carlson/trophys", "200 OK", "Consultou galeria de prestígio")
                                                }
                                                "SALA DO MESTRE" -> {
                                                    onNpcNameChanged("Mestre Carlson Gracie")
                                                    onNpcEmojiChanged("Carlson 👴")
                                                    onDialogueChanged("Mestre Carlson: Se você tem medo de cara feia, não entra no Tatame! A nossa sala de aula é sagrada. Quem é filiado tem passe livre aqui.")
                                                    addApiLog("GET", "/api/v1/carlson/master-desk", "200 OK", "Lutador se apresentou na sala do mestre")
                                                }
                                                "SALA VIP" -> {
                                                    onNpcNameChanged("Lounge de Elite")
                                                    onNpcEmojiChanged("VIP 🛋️")
                                                    onDialogueChanged("Sala VIP: Descanso, café e poltronas ergonômicas para comissão técnica e lutadores de elite!")
                                                    addApiLog("GET", "/api/v1/carlson/vip-lounge", "200 OK", "Verificou portão de acesso VIP")
                                                }
                                                "LOJA DA ACADEMIA" -> {
                                                    onNpcNameChanged("Vendedor Seu Wu")
                                                    onNpcEmojiChanged("Seu Wu 🧙‍♂️")
                                                    onDialogueChanged("Seu Wu: Prefere faixas de combate reforçadas ou patches Carlson originais bordados com linha importada?")
                                                    addApiLog("GET", "/api/v1/carlson/shop", "200 OK", "Carregada vitrine da loja clã")
                                                }
                                            }
                                        }
                                    } else if (!isTraveling) {
                                        onRoomSelected(room.name)
                                    }
                                }
                                .padding(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = room.label,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFC62828) else if (isPlayerHere) Color(0xFFFFD700) else Color.White
                                    )
                                    Text(room.emoji, fontSize = 10.sp)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = when (room.name) {
                                            "RECEPÇÃO" -> "Diana 👩‍💼"
                                            "ÁREA DE AULA" -> "Henrique 🥋"
                                            "LOJA DA ACADEMIA" -> "Seu Wu 🧙‍♂️"
                                            "SALA DO MESTRE" -> "Carlson 👴"
                                            else -> "Vazio"
                                        },
                                        fontSize = 7.sp,
                                        color = Color(0xFF8B8B8B)
                                    )

                                    if (isPlayerHere) {
                                        Text(
                                            text = "🏃‍♂️ VOCÊ ($playerAvatarStyle)",
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFFFD700),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Loading overlay when walking
                    if (isTraveling) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF232323)),
                                border = BorderStroke(1.dp, Color(0xFFC62828))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color(0xFFC62828),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = travelStatus,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // RIGHT COLUMN (WEIGHT 0.8): Dialogue bubble, Active Area info & Interactive system actions
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .height(290.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Info Box of Selected Room
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF232323), RoundedCornerShape(4.dp))
                        .border(0.5.dp, Color(0xFF3A3A3A), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "📌 AMBIENTE ATIVO: $selectedRoom",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = when (selectedRoom) {
                                "RECEPÇÃO" -> "Interaja com Diana para as formalidades de censo do clã."
                                "TATAME PRINCIPAL" -> "Espaço de competição, lutas livres e duelos contra sparrings."
                                "ÁREA DE AULA" -> "Faça missões dadas pelo Instrutor Henrique para forjar seu nível esportivo."
                                "VESTIÁRIOS" -> "Guarda-roupa virtual oficial para equipar e obter trajes especiais."
                                "HALL DE TROFÉUS" -> "Inspire-se contemplando grandes medalhas heráldicas."
                                "SALA DO MESTRE" -> "Gabinete tático do Mestre Carlson para controle administrative."
                                "SALA VIP" -> "Área privativa premium para dar repouso aos campeões."
                                "LOJA DA ACADEMIA" -> "Adquira patches oficiais e kimonos exclusivos."
                                else -> ""
                            },
                            fontSize = 8.5.sp,
                            color = Color.LightGray
                        )
                    }
                }

                // Interactive Speech Bubble
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF0F0F10), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFC62828), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "💬 DIÁLOGO: $activeNpcEmoji",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentNpcDialogue,
                            fontSize = 9.2.sp,
                            color = Color.White,
                            lineHeight = 12.sp
                        )
                    }
                }

                // Interactive Actions Board
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF232323), RoundedCornerShape(4.dp))
                        .border(0.5.dp, Color(0xFF333333))
                        .padding(8.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "⚒️ AÇÕES REAIS DISPONÍVEIS:",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B8B8B)
                        )

                        when (selectedRoom) {
                            "RECEPÇÃO" -> {
                                Button(
                                    onClick = {
                                        if (!isCarlsonMember) {
                                            onCarlsonMemberChanged(true)
                                            onCarlsonMembersCountChanged(carlsonMembersCount + 1)
                                            // Real database linkage (Room database call)
                                            viewModel.enrollStudentToAcademy(
                                                academyId = activeAcademyId,
                                                name = "Membro Carlson Gracie",
                                                belt = "Preta",
                                                nickname = "carlson_membro",
                                                approved = true
                                            )
                                            onDialogueChanged("Diana: Magnífico! Cadastro sincronizado perfeitamente no banco local SQLite! Vínculo aprovado.")
                                            addApiLog("POST", "/api/v1/students/enroll", "201 Created", "Cadastro Carlson Gracie sincronizado na persistência local")
                                        } else {
                                            onDialogueChanged("Diana: Seu selo de filiação Carlson Gracie já está ativo e regular!")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isCarlsonMember) "✓ CONEXÃO ATIVA NO ROOM" else "FILIAR-SE GRATUITAMENTE (SQLITE SYNC)",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Button(
                                    onClick = {
                                        onDialogueChanged("Diana: Atualmente, temos R$ 4.560 em prêmios no cofre e $carlsonMembersCount membros inscritos.")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E3E40)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("CONSULTAR STATUS DO COFRE", fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            "TATAME PRINCIPAL" -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = {
                                            onCarlsonPrestigeChanged(carlsonPrestige + 15)
                                            // Simulated player leveling mechanics
                                            onDialogueChanged("Você colocou o kimono, pisou firme no tatame e treinou focado na raspagem de gancho! +15 de prestígio!")
                                            addApiLog("POST", "/api/v1/carlson/train", "200 OK", "Lutador realizou treino clássico síncrono")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("PISAR NO TATAME", fontSize = 8.5.sp, color = Color.White)
                                    }

                                    Button(
                                        onClick = {
                                            val myVal = (1..6).random()
                                            val partnerVal = (1..6).random()
                                            if (myVal >= partnerVal) {
                                                onCarlsonPrestigeChanged(carlsonPrestige + 40)
                                                onDialogueChanged("OSS! Você ajustou um estrangulamento Ezequiel perfeito! Vitória no rola! (Seu Dado: $myVal vs Parceiro: $partnerVal).")
                                                addApiLog("POST", "/api/v1/carlson/duel?match=won", "200 OK", "Duelo síncrono finalizado com vitória")
                                            } else {
                                                onDialogueChanged("Parceiro te pegou em uma chave de calcanhar rápida! Batida confirmada! (Seu Dado: $myVal vs Parceiro: $partnerVal).")
                                                addApiLog("POST", "/api/v1/carlson/duel?match=tap", "200 OK", "Duelo síncrono finalizado por submissão")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("DESAFIAR SPARRING", fontSize = 8.5.sp, color = Color.White)
                                    }
                                }
                            }

                            "ÁREA DE AULA" -> {
                                Button(
                                    onClick = {
                                        onCarlsonPrestigeChanged(carlsonPrestige + 30)
                                        onDialogueChanged("Henrique: Excelente aula sobre passagem de guarda emborcando! Bônus de treino e +30 prestígio creditado.")
                                        addApiLog("POST", "/api/v1/carlson/class/attend", "200 OK", "Atleta participou das aulas técnicas do dia")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("ASSISTIR PALESTRA TÁTICA (+30 PRESTÍGIO)", fontSize = 8.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        onDialogueChanged("Henrique: Sua missão diária hoje é fazer 3 drolles de passagem ou comprar 1 patch Carlson na loja!")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("VER MISSÃO DIÁRIA DO TEMPLO", fontSize = 8.5.sp, color = Color.White)
                                }
                            }

                            "VESTIÁRIOS" -> {
                                Text("SELECIONAR COR DO KIMONO:", fontSize = 8.sp, color = Color.Gray)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf("Branco 🥋", "Azul 🥋🔵", "Preto 🥋⚫").forEach { kimono ->
                                        val isEquipped = playerAvatarStyle == kimono
                                        Button(
                                            onClick = {
                                                onAvatarStyleChanged(kimono)
                                                onDialogueChanged("Kimono ajustado! Equipou: $kimono para exibições e rolas síncronos.")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isEquipped) Color(0xFFC62828) else Color(0xFF333333)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(vertical = 2.dp)
                                        ) {
                                            Text(kimono, fontSize = 7.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            "HALL DE TROFÉUS" -> {
                                Button(
                                    onClick = {
                                        onDialogueChanged("Carlson Gracie pavimentou a era de ouro do jiu-jitsu, ensinando que a garra vence o talento teórico! Pioneiro de dezenas de faixas-pretas mundiais.")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("LER HISTÓRIA DOS MESTRES", fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        onDialogueChanged("Líderes de Medalha Carlson Gracie:\n1. @MestreAdmin (2.450 pts)\n2. @carlson_membro (1.200 pts)\n3. @SparringMaster (850 pts)")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("CONSULTAR RANKING OFICIAL", fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            "SALA DO MESTRE" -> {
                                if (isCarlsonMember) {
                                    Button(
                                        onClick = {
                                            onDialogueChanged("Master Desk: Painel de configurações aberto! Configurado modelo federado síncrono. Taxa tributária de split: 10%.")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("GERENCIAR GRUPOS & CONFIGS DU CARANHA", fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text("🔒 ENTRADA BLOQUEADA\nApenas membros oficiais filiados no censo de Room DB!", fontSize = 8.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                }
                            }

                            "SALA VIP" -> {
                                if (isCarlsonMember || carlsonPrestige >= 2450) {
                                    Button(
                                        onClick = {
                                            onDialogueChanged("Lounge VIP: Você sentou, tomou café expresso gourmet síncrono e assistiu às fitas gravadas dos campeonatos!")
                                            addApiLog("POST", "/api/v1/carlson/vip/coffee", "200 OK", "Lutador consumiu café VIP do clube")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B4B)),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("TOMAR CAFÉ ESPRESSO SÍNCRONO ☕", fontSize = 8.5.sp, color = Color.White)
                                    }
                                } else {
                                    Text("🔒 PORTA TRANCADA\nApenas convidados ou membros ativos!", fontSize = 8.5.sp, color = Color(0xFF8B8B8B))
                                }
                            }

                            "LOJA DA ACADEMIA" -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = {
                                            onDialogueChanged("Seu Wu: Descontando 500 moedas virtuais... Comprou Kimono Linha Vermelha Carlson Gracie! Equipado.")
                                            onAvatarStyleChanged("Preto 🥋⚫")
                                            addApiLog("POST", "/api/v1/carlson/shop/buy?item=kimono", "200 OK", "Kimono Carlson Gracie Oficial adquirido na loja")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        modifier = Modifier.weight(1.1f),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("KIMONO CARLSON (500🪙)", fontSize = 7.5.sp, color = Color.White)
                                    }

                                    Button(
                                        onClick = {
                                            onDialogueChanged("Seu Wu: Comprou Patch Red Shield Carlson! Adicionado ao inventário de cosméticos.")
                                            addApiLog("POST", "/api/v1/carlson/shop/buy?item=patch", "200 OK", "Patch Red Shield oficial adquirido")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        modifier = Modifier.weight(0.9f),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("PATCH CLÃ (200🪙)", fontSize = 7.5.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Simulated Corkwood Notice Bulletin Board Dialog (Prompt 7) ---
        AnimatedVisibility(visible = isNoticeBoardOpen) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF8D6E63), RoundedCornerShape(6.dp)), // Woody outline
                colors = CardDefaults.cardColors(containerColor = Color(0xFF5D4037)) // Corkwood light brown back
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📌 QUADRO DE RECADOS OFICIAIS DO TEMPLO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar Mural",
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onNoticeBoardOpenChanged(false) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Note 1
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)) // Yellow post-it
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("🥋 VESTIMENTA", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                Text("No tatame Carlson Gracie, apenas quimonos limpos e com patches oficiais são permitidos. Respeite!", fontSize = 7.5.sp, color = Color.DarkGray)
                            }
                        }

                        // Note 2
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)) // Green post-it
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("📆 PRÓXIMO TORNEIO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                Text("Campeonato Memorial Carlson Gracie agendado no calendário global. Ingressos via API federada.", fontSize = 7.5.sp, color = Color.DarkGray)
                            }
                        }

                        // Note 3
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)) // Orange post-it
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("🦁 REVOLUÇÃO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                Text("Treine o pescoço e a guarda ativa. Nossos mestres lutam para vencer. Oss!", fontSize = 7.5.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
