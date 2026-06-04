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
