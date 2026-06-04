package com.example.architecture.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.*

// Mock structures to represent SaaS admin entities
data class ManagedUser(
    val id: String,
    val username: String,
    val academy: String,
    val email: String,
    var role: String, // "ATLETA", "INSTRUTOR", "MODERADOR", "AUDITOR", "ADMIN"
    var status: String // "ATIVO", "SUSPENSO", "PENDENTE_VERIFICACAO"
)

data class ManagedAcademy(
    val id: String,
    val name: String,
    val masterCoach: String,
    var tier: String, // "MEGA_DOJO", "REGULAR_DOJO", "FILIAL"
    var isVerified: Boolean,
    var score: Int
)

data class ManagedEvent(
    val id: String,
    val title: String,
    val category: String, // "1X1", "2X2", "5X5", "ACADEMIA_WAR"
    var status: String, // "AGENDADO", "EM_ANDAMENTO", "CONCLUIDO"
    var participantLimit: Int,
    var basePrizePool: Int
)

data class ManagedMarketplaceAsset(
    val id: String,
    val name: String,
    var priceJC: Int, // JiCoins
    var priceJG: Int, // JiGems
    var isFeatured: Boolean,
    var stockRemaining: Int
)

data class ToxicityReport(
    val id: String,
    val reportedPlayer: String,
    val reporter: String,
    val category: String, // "PROXIMITY_CHAT_ABUSE", "SANDBAGGING", "GLITCH_EXPLOIT"
    val evidenceContent: String,
    val severity: String, // "CRITICA", "ALTA", "BAIXA"
    var status: String // "PENDENTE", "BANIDO", "DESCARTADO"
)

data class SystemAuditLog(
    val timestamp: String,
    val actor: String,
    val action: String,
    val severityIcon: String
)

data class LiveServerMetric(
    val title: String,
    val value: String,
    val statusColor: Color
)

@Composable
fun AdminTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // 1. Nested Active Admin View Section Slider
    var activeSubPanel by remember { mutableStateOf("TELEMETRIA") } // "TELEMETRIA", "SOCIOS", "COMPETITIVO", "LEGAL"

    // 2. Interactive Role-Based Access Control (RBAC) Switcher State
    var activeRbacRole by remember { mutableStateOf("CTO_CHIEF_ARCHITECT") } 
    // Roles: "CTO_CHIEF_ARCHITECT", "FINANCIAL_AUDITOR", "COMMUNITY_MODERATOR", "SUPPORT_TIER_1"

    // Help Helper permissions validation predicate
    val checkPerm = { requiredPerm: String ->
        when (activeRbacRole) {
            "CTO_CHIEF_ARCHITECT" -> true
            "FINANCIAL_AUDITOR" -> requiredPerm in listOf("FINANCE_READ", "FINANCE_WRITE", "MARKET_EDIT")
            "COMMUNITY_MODERATOR" -> requiredPerm in listOf("USER_READ", "MODERATION_WRITE", "EVENTS_READ")
            "SUPPORT_TIER_1" -> requiredPerm in listOf("USER_READ", "EVENTS_READ")
            else -> false
        }
    }

    // 3. Mutable State Datastore pools for UI edits
    val managedUsersList = remember {
        mutableStateListOf(
            ManagedUser("u1", "Rickson_99", "Gracie Barra Angra", "rickson@gracie.io", "INSTRUTOR", "ATIVO"),
            ManagedUser("u2", "Miyao_Bros", "Alliance Dojo Centro", "miyao@alliance.br", "ATLETA", "ATIVO"),
            ManagedUser("u3", "Galvao_Fan", "Atos San Diego", "fanatic@atos.com", "MODERADOR", "ATIVO"),
            ManagedUser("u4", "Buchecha_Rulez", "Checkmat SP", "b@checkmat.org", "ATLETA", "ATIVO"),
            ManagedUser("u5", "Anonymous_Guard", "Doubtful Cave", "troll@anonymous.net", "ATLETA", "SUSPENSO")
        )
    }

    val managedAcademiesList = remember {
        mutableStateListOf(
            ManagedAcademy("a1", "Alliance Dojo Centro", "Fabio Gurgel", "MEGA_DOJO", true, 98),
            ManagedAcademy("a2", "Gracie Barra Angra", "Jefferson Silva", "REGULAR_DOJO", true, 92),
            ManagedAcademy("a3", "Atos San Diego", "Andre Galvao", "MEGA_DOJO", false, 95)
        )
    }

    val managedEventsList = remember {
        mutableStateListOf(
            ManagedEvent("e1", "GP Estelar - Pro Div - 1x1", "1X1", "EM_ANDAMENTO", 8000, 25000),
            ManagedEvent("e2", "Copa Sulamericana de Duplas", "2X2", "AGENDADO", 2048, 15000),
            ManagedEvent("e3", "Guerra de Guildas de Academias", "ACADEMIA_WAR", "AGENDADO", 4096, 50000)
        )
    }

    val managedMarketplaceAssets = remember {
        mutableStateListOf(
            ManagedMarketplaceAsset("m1", "Skin Quimono Pixelado de Elite", 8500, 150, true, 420),
            ManagedMarketplaceAsset("m2", "Faixa Preta Holográfica Cósmica", 15000, 450, true, 12),
            ManagedMarketplaceAsset("m3", "Gesto do Punho de Aço Dojo", 2500, 25, false, 9999)
        )
    }

    val managedToxicityReports = remember {
        mutableStateListOf(
            ToxicityReport("tr1", "Sandbagger_Jiu", "Rickson_99", "SANDBAGGING", "Smurf com 500 wins na faixa azul destruindo torneio de iniciantes", "ALTA", "PENDENTE"),
            ToxicityReport("tr2", "Exploiter_Mestre", "Miyao_Bros", "GLITCH_EXPLOIT", "Executou lag-switch durante raspagem em quarto de final síncrono", "CRITICA", "PENDENTE"),
            ToxicityReport("tr3", "Flamer_Dojo", "Galvao_Fan", "PROXIMITY_CHAT_ABUSE", "Ofensas verbais graves no áudio de proximidade da arquibancada", "BAIXA", "PENDENTE")
        )
    }

    val mockAuditLogs = remember {
        mutableStateListOf(
            SystemAuditLog("16:10:02", "sys_engine", "Auto-rebalancing Spanner database shards", "⚙️"),
            SystemAuditLog("16:08:45", "admin_cto", "Updated price on 'Faixa Preta Holográfica'", "💰"),
            SystemAuditLog("16:05:12", "mod_marcelo", "Approved suspension for 'Anonymous_Guard' (toxic chat)", "🛡️"),
            SystemAuditLog("15:58:30", "sys_webhook", "Authorized payout cycle to GB Angra (R$ 4.250)", "🏦")
        )
    }

    // Server Workload simulated controls
    var mockCpuPercentage by remember { mutableStateOf(44.2f) }
    var mockActiveWebsockets by remember { mutableStateOf(14250) }
    var containerReplicaState by remember { mutableStateOf("ACTIVE_5_NODES") } // UNHEALTHY, SCALE_UP_REPLICA, ACTIVE_5_NODES
    var liveMicrotransactionsLog = remember {
        mutableStateListOf(
            "COMPRA: Atleta @Miyao_Bros comprou 'Skin Quimono Pixelado' (150 gems)",
            "LOG: Tenant 'Alliance Centro' retirou R$ 1.250,00 via Payout Split",
            "SINC: WebSocket regional SA-East registrou pico de 4.200 msgn/segundo",
            "MERCADO: Venda P2P concluída entre @Rickson_99 e @Buchecha_Rulez",
            "PAGAMENTO: Stripe Webhook confirmou recarga de 5.000 JG do usuário @Galvao_Fan"
        )
    }

    // Action Helpers
    val triggerLogAndAudit = { logText: String, auditAction: String, severity: String ->
        liveMicrotransactionsLog.add(0, "SISTEMA: $logText")
        mockAuditLogs.add(0, SystemAuditLog("16:15:33", "user_active_session", auditAction, severity))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        
        // Header
        SectionHeader(
            title = "Painel Administrativo & COCKPIT CTO",
            subtitle = "SaaS Console Global: Gestão de Tenants, Controles RBAC, Telemetria Multi-região e Infraestrutura"
        )

        // VISUAL CTU/COCKPIT BRANDING BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF070B13), RoundedCornerShape(8.dp))
                .border(1.dp, BlueprintCyan, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(BlueprintCyan, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "STATUS DO SISTEMA: COMPILADO E TOTALMENTE CONECTADO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Central de controle tático do Arquiteto SaaS. Use os switches de RBAC no bloco abaixo para simular as restrições hierárquicas e as diretivas de auditoria em tempo real.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 1. INTERACTIVE RBAC CONTROL DECK (CRITICAL REQUIREMENT)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = BlueprintOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CONTROLES DE AUTORIZAÇÃO (RBAC SESSION SIMULATOR)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Altere sua função atual do sistema para ver como as restrições de permissões SaaS agem na interface administrativa do JiuVerse instantaneamente:",
                    fontSize = 10.5.sp,
                    color = BlueprintTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Role buttons selectors row
                val rolesData = listOf(
                    "CTO_CHIEF_ARCHITECT" to "CTO / Arquiteto",
                    "FINANCIAL_AUDITOR" to "Auditor Fiscal",
                    "COMMUNITY_MODERATOR" to "Moderador Geral",
                    "SUPPORT_TIER_1" to "Atendimento N1"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rolesData.forEach { (roleKey, roleLabel) ->
                        val isSelected = activeRbacRole == roleKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) BlueprintOrange.copy(alpha = 0.2f) else Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, if (isSelected) BlueprintOrange else BlueprintGridLine, RoundedCornerShape(4.dp))
                                .clickable {
                                    activeRbacRole = roleKey
                                    triggerLogAndAudit(
                                        "Nível de autorização RBAC trocado para $roleKey",
                                        "CHG_RBAC_HIERARCHY -> $roleKey",
                                        "🔐"
                                    )
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = roleLabel,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BlueprintOrange else BlueprintTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(11.dp))

                // Permit Matrix checklist visually showing allowed permissions
                Text("Cadeia Funcional Permitida (Permissões de Escopo Atuais):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val permissionsMatrix = listOf(
                        "USER_READ" to "Consultar Contas",
                        "MODERATION_WRITE" to "Aplicar Ban/Sanções",
                        "MARKET_EDIT" to "Alterar Preços",
                        "FINANCE_WRITE" to "Gerenciar Cofres",
                        "EVENTS_WRITE" to "Iniciar Torneio"
                    )

                    permissionsMatrix.forEach { (permKey, permLabel) ->
                        val isAllowed = checkPerm(permKey)
                        val colorText = if (isAllowed) BlueprintTeal else Color(0xFFEF4444)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isAllowed) BlueprintTeal.copy(alpha = 0.05f) else Color(0xFFEF4444).copy(alpha = 0.05f), RoundedCornerShape(3.dp))
                                .border(0.5.dp, colorText.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isAllowed) "✓ ALLOW" else "✗ DENIED", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = colorText)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(permLabel, fontSize = 7.sp, color = BlueprintTextSecondary, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 2. SUB-PANEL SUBTABS (Cockpit Sections Selector)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val subPanels = listOf(
                "TELEMETRIA" to "Status e Infra",
                "SOCIOS" to "Usuários e Academias",
                "COMPETITIVO" to "Torneios e Loja",
                "LEGAL" to "Denúncias e Caixa"
            )

            subPanels.forEach { (key, label) ->
                val isSelected = activeSubPanel == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) BlueprintCyan.copy(alpha = 0.2f) else BlueprintCard, RoundedCornerShape(6.dp))
                        .border(1.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(6.dp))
                        .clickable { activeSubPanel = key }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) BlueprintCyan else BlueprintTextPrimary
                    )
                }
            }
        }

        // ==========================================
        // 3. RENDER SUB-PANEL SPECIFIC VIEW
        // ==========================================
        when (activeSubPanel) {
            "TELEMETRIA" -> {
                // PANEL A: telemetry, Docker containers status, microtransaction real time log stream, and SaaS architectural diagram!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Telemetry card dials left
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("TELEMETRIA LIVE SAAS NODES", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Variable sliders CPU
                            Text("Simular Carga de Recursos CPU: ${(mockCpuPercentage).toInt()}%", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            Slider(
                                value = mockCpuPercentage,
                                onValueChange = { mockCpuPercentage = it },
                                valueRange = 10f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = BlueprintCyan,
                                    activeTrackColor = BlueprintCyan
                                ),
                                modifier = Modifier.height(24.dp)
                            )

                            // Variable WebSockets Connections mock
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Escalabidade de conexões WS: (${mockActiveWebsockets} Concorrentes)", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                listOf(1500, 15000, 45000, 95000).forEach { connectionsVal ->
                                    val act = mockActiveWebsockets == connectionsVal
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (act) BlueprintTeal.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                            .border(0.5.dp, if (act) BlueprintTeal else BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .clickable {
                                                mockActiveWebsockets = connectionsVal
                                                mockCpuPercentage = when (connectionsVal) {
                                                    1500 -> 21.5f
                                                    15000 -> 48.0f
                                                    45000 -> 72.8f
                                                    else -> 91.2f
                                                }
                                                triggerLogAndAudit(
                                                    "Escala de WebSockets emulada para $connectionsVal usuários ativos",
                                                    "SCALE_CONCURRENCY -> $connectionsVal",
                                                    "⚙️"
                                                )
                                            }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${connectionsVal / 1000}k", fontSize = 8.sp, color = if (act) BlueprintTeal else BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                            // Cluster Replica Nodes States
                            Text("Docker / K8s Cluster Replica Orchestrator:", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(
                                    "ACTIVE_5_NODES" to "5 Réplicas OK",
                                    "SCALE_UP_REPLICA" to "Escalar para 15",
                                    "UNHEALTHY" to "Degradado (1 Nó)"
                                ).forEach { (stateKey, labelStr) ->
                                    val isCur = containerReplicaState == stateKey
                                    val bColor = when (stateKey) {
                                        "UNHEALTHY" -> Color(0xFFEF4444)
                                        "SCALE_UP_REPLICA" -> BlueprintOrange
                                        else -> BlueprintTeal
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isCur) bColor.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                            .border(0.5.dp, if (isCur) bColor else BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .clickable {
                                                containerReplicaState = stateKey
                                                if (stateKey == "UNHEALTHY") {
                                                    mockCpuPercentage = 95.5f
                                                }
                                                triggerLogAndAudit(
                                                    "Docker Swarm Orquestrador mudado para status: $stateKey",
                                                    "K8S_SINK_STATUS -> $stateKey",
                                                    "🐋"
                                                )
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = labelStr,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCur) bColor else BlueprintTextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Log Queue streams live right side
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("CONSOLE COMPORTAMENTAL / LOGS DO KERNEL", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tráfego de eventos, CDN pipelines e requisições HTTP:", fontSize = 8.sp, color = BlueprintTextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(6.dp))
                                    .padding(6.dp)
                            ) {
                                liveMicrotransactionsLog.take(5).forEach { transaction ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(4.dp).background(BlueprintTeal, CircleShape))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = transaction,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 7.5.sp,
                                            color = Color(0xFFA5F3FC),
                                            lineHeight = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ARCHITECTURAL MULTI-TENANT BLUEPRINT SCHEMA DESIGN DIAGRAM
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.GridOn, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DIAGRAMA DE ARQUITETURA CLOUD SAAS MULTI-TENANT JIUVERSE v2.5", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val archText = """
+------------------------------------------------------------------------------------------------+
| [CLIENTS SIMULATOR app / SDK] ----> AWS Route53 Dynamic Edge GSLB (Geo-Routing CDN, DDoS WAF)  |
+------------------------------------+-----------------------------------------------------------+
                                     |
                                     v HTTPS/WSS (gRPC Frame Payload Encrypted)
+------------------------------------------------------------------------------------------------+
| Nginx Reverse Proxy / Envoy API Gateway Layer 7 Router (JWT Validation, Rate Limiters)        |
+------------------------------------+-----------------------------------------------------------+
                                     |
    +--------------------------------+--------------------------------+--------------------+
    | (Microservices Pods Mesh - Golang / Kotlin Ktor - Distributed in Docker Kubernetes Node Pools)|
    v                                v                                v                    v
[Auth & RBAC Service]      [Matchmaking Broker]             [Market Engine]       [Billing Ledger Hub]
  └─ OAuth2 / Auth0          └─ Match Queue (12ms)            └─ Inventory Sinc     └─ Stripe/PIX API
    |                          |                                |                    |
    +──────────────────────────┼────────────────────────────────┼────────────────────+
                               v Real-Time Streams PubSub Handler
                    [Redis Cache Enterprise Cluster In-Memory Keys (98.7% hit-rate)]
                               |
                               v Dual-Write Write-Through Daemon
+------------------------------------------------------------------------------------------------+
| Multi-Region Google Cloud Spanner Enterprise (Globally Sharded SQL Tables, Strong Consistency) |
+-----------------------------------+------------------------------------------------------------+
|  ├─ Tenant ID: 001 (Alliance)     |  ├── Auth Auditing Records Pipeline (CDC Apache Kafka)     |
|  ├─ Tenant ID: 002 (Gracie Barra) |  └── ElasticSearch Event Logging Kibana Sink                |
+-----------------------------------+------------------------------------------------------------+
                        """.trimIndent()

                        CodeBlock(code = archText, title = "JiuVerse Cloud Architecture Diagram (CTO SaaS Blueprint Specification)")
                    }
                }
            }

            "SOCIOS" -> {
                // PANEL B: User and Academy database accounts with active verify toggle + dynamic role adjuster Matrix + RBAC rule checking simulation!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    
                    // Users Accounts database table (Left Column)
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("REGISTRO GERAL DE PARTICIPANTES (USERS MANAGEMENT)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Troque as funções de atletas diretivas ativas:", fontSize = 8.sp, color = BlueprintTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))

                                managedUsersList.forEach { user ->
                                    val isAuthorizedToModify = checkPerm("USER_WRITE")
                                    
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .border(
                                                0.5.dp,
                                                if (user.status == "SUSPENSO") Color(0xFFEF4444) else BlueprintGridLine,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("@${user.username}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(if (user.status == "SUSPENSO") Color(0xFFEF4444).copy(alpha = 0.2f) else BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(user.status, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = if (user.status == "SUSPENSO") Color(0xFFEF4444) else BlueprintTeal)
                                                    }
                                                }
                                                Text("Dojo: ${user.academy}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                                Text("Role: ${user.role}", fontSize = 8.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                                            }

                                            // Toggle user role dynamically if allowed by active login RBAC
                                            if (isAuthorizedToModify) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    // Quick role cycles
                                                    listOf("ATLETA", "INSTRUTOR", "MODERADOR").forEach { roleOption ->
                                                        val isSelectedRole = user.role == roleOption
                                                        Box(
                                                            modifier = Modifier
                                                                .background(if (isSelectedRole) BlueprintCyan.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(2.dp))
                                                                .border(0.5.dp, if (isSelectedRole) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(2.dp))
                                                                .clickable {
                                                                    // Update the role inside mutable list
                                                                    val index = managedUsersList.indexOf(user)
                                                                    if (index != -1) {
                                                                        managedUsersList[index] = user.copy(role = roleOption)
                                                                        triggerLogAndAudit(
                                                                            "Modificado perfil do usuário @${user.username} para $roleOption",
                                                                            "ROLE_CHANGED: @${user.username} -> $roleOption",
                                                                            "👤"
                                                                        )
                                                                    }
                                                                }
                                                                .padding(horizontal = 3.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(roleOption.take(4), fontSize = 6.5.sp, color = if (isSelectedRole) BlueprintCyan else BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                                                        }
                                                    }

                                                    // Anti-Toxicity Suspender
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFFEF4444).copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                                                            .clickable {
                                                                val index = managedUsersList.indexOf(user)
                                                                if (index != -1) {
                                                                    val nextStatus = if (user.status == "SUSPENSO") "ATIVO" else "SUSPENSO"
                                                                    managedUsersList[index] = user.copy(status = nextStatus)
                                                                    triggerLogAndAudit(
                                                                        "Usuário @${user.username} chaveado para status: $nextStatus",
                                                                        "BAN_TOGGLE: @${user.username} -> $nextStatus",
                                                                        "🛑"
                                                                    )
                                                                }
                                                            }
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("BAN", fontSize = 6.5.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                // Locked badge
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Bloqueado RBAC", fontSize = 7.sp, color = Color(0xFFEF4444))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Academies Register (Right Column)
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("DOMÍNIOS DE DOJO (ACADEMIES DIRECTORY)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Afiliações federadas e multiplicadores ativos:", fontSize = 8.sp, color = BlueprintTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))

                                managedAcademiesList.forEach { academy ->
                                    val isAuthorizedToModify = checkPerm("USER_WRITE")

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(academy.name, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                                    if (academy.isVerified) {
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text("✓SaaS", fontSize = 6.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Text("Mestre Residente: Mestre ${academy.masterCoach}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                                Text("Nível Sandbox: ${academy.tier}", fontSize = 7.5.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                                            }

                                            if (isAuthorizedToModify) {
                                                // Verification badge status switch trigger
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (academy.isVerified) BlueprintTeal.copy(alpha = 0.2f) else Color.DarkGray, RoundedCornerShape(3.dp))
                                                        .clickable {
                                                            val index = managedAcademiesList.indexOf(academy)
                                                            if (index != -1) {
                                                                val currentVerif = academy.isVerified
                                                                managedAcademiesList[index] = academy.copy(isVerified = !currentVerif)
                                                                triggerLogAndAudit(
                                                                    "Status de Verificação de Dojo alterado para ${academy.name}",
                                                                    "VERIFICATION_TOGGLE: ${academy.name} -> ${!currentVerif}",
                                                                    "🎖️"
                                                                )
                                                            }
                                                        }
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        if (academy.isVerified) "VERIFICADO" else "PENDENTE",
                                                        fontSize = 6.5.sp,
                                                        color = if (academy.isVerified) BlueprintTeal else Color.LightGray,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                Text("🔒 Bloqueado", fontSize = 7.sp, color = Color.Gray)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Commission custom rate split display
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("SaaS Commision Split Multiplier:", fontSize = 7.sp, color = BlueprintTextSecondary)
                                            Text(
                                                text = if (academy.tier == "MEGA_DOJO") "85% para Dojo / 15% JiuVerse Hub" else "90% para Dojo / 10% JiuVerse Hub",
                                                fontSize = 7.sp,
                                                color = BlueprintTeal,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "COMPETITIVO" -> {
                // PANEL C: Active Events scheduler with starting triggers + item price controls & stock adjusters!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    
                    // Tournament Events scheduler (Left Column)
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("AGÊNCIAMENTO DE TORNEIOS (GP EVENT SCHEDULER)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Gestão síncrona de chaves e barramentos:", fontSize = 8.sp, color = BlueprintTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))

                                managedEventsList.forEach { evt ->
                                    val canModerateEvents = checkPerm("EVENTS_WRITE")

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, if (evt.status == "EM_ANDAMENTO") BlueprintTeal else BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(evt.title, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                                Text("Categoria do GP: ${evt.category} | Limite: ${evt.participantLimit} combatentes", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                                Text("Financiamento Base Corrente: ${evt.basePrizePool} JiCoins", fontSize = 7.5.sp, color = BlueprintOrange)
                                            }

                                            // Action trigger to start or complete
                                            if (canModerateEvents) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (evt.status == "EM_ANDAMENTO") BlueprintTeal.copy(alpha = 0.2f) else BlueprintCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                        .clickable {
                                                            val index = managedEventsList.indexOf(evt)
                                                            if (index != -1) {
                                                                val nextStatus = when (evt.status) {
                                                                    "AGENDADO" -> "EM_ANDAMENTO"
                                                                    "EM_ANDAMENTO" -> "CONCLUIDO"
                                                                    else -> "AGENDADO"
                                                                }
                                                                managedEventsList[index] = evt.copy(status = nextStatus)
                                                                triggerLogAndAudit(
                                                                    "Torneio '${evt.title}' mudado para status '$nextStatus'",
                                                                    "EVENT_STATE_UPDATE: ${evt.id} -> $nextStatus",
                                                                    "🏆"
                                                                )
                                                            }
                                                        }
                                                        .padding(horizontal = 5.dp, vertical = 3.dp)
                                                ) {
                                                    Text(evt.status, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = if (evt.status == "EM_ANDAMENTO") BlueprintTeal else BlueprintCyan)
                                                }
                                            } else {
                                                Text("🔒 Sem permissão", fontSize = 7.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Marketplace inventory catalog editor (Right Column)
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("LOJA DO MERCADO INTERNO (MARKETPLACE CATALOG)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Ajuste os preços de JiCoins e JiGems administrados:", fontSize = 8.sp, color = BlueprintTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))

                                managedMarketplaceAssets.forEach { asset ->
                                    val canEditMarketInfo = checkPerm("MARKET_EDIT")

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(asset.name, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                        Text("Preço atual: ${asset.priceJC} JC | ${asset.priceJG} JG", fontSize = 8.sp, color = BlueprintCyan)
                                        Text("Estoque Reservado Hub: ${asset.stockRemaining} un", fontSize = 7.5.sp, color = BlueprintTextSecondary)

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Inflation toggle +/- price button
                                        if (canEditMarketInfo) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(BlueprintOrange.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                                            .clickable {
                                                                val idx = managedMarketplaceAssets.indexOf(asset)
                                                                if (idx != -1) {
                                                                    val oldPrice = asset.priceJC
                                                                    val newPrice = (oldPrice * 1.1f).toInt()
                                                                    managedMarketplaceAssets[idx] = asset.copy(priceJC = newPrice)
                                                                    triggerLogAndAudit(
                                                                        "Preço do item '${asset.name}' inflacionado em 10%",
                                                                        "PRICE_INCREASE: ${asset.id} ($oldPrice -> $newPrice)",
                                                                        "📈"
                                                                    )
                                                                }
                                                            }
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("+10% Inflação", fontSize = 6.5.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                                            .clickable {
                                                                val idx = managedMarketplaceAssets.indexOf(asset)
                                                                if (idx != -1) {
                                                                    val oldPrice = asset.priceJC
                                                                    val newPrice = (oldPrice * 0.9f).toInt()
                                                                    managedMarketplaceAssets[idx] = asset.copy(priceJC = newPrice)
                                                                    triggerLogAndAudit(
                                                                        "Preço do item '${asset.name}' deflacionado em 10%",
                                                                        "PRICE_DECREASE: ${asset.id} ($oldPrice -> $newPrice)",
                                                                        "📉"
                                                                    )
                                                                }
                                                            }
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("-10% Promoção", fontSize = 6.5.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                // Buy Simulator triggers
                                                Box(
                                                    modifier = Modifier
                                                        .background(BlueprintTeal, RoundedCornerShape(2.dp))
                                                        .clickable {
                                                            val idx = managedMarketplaceAssets.indexOf(asset)
                                                            if (idx != -1 && asset.stockRemaining > 0) {
                                                                managedMarketplaceAssets[idx] = asset.copy(stockRemaining = asset.stockRemaining - 1)
                                                                triggerLogAndAudit(
                                                                    "Transação Simulação: Vendida 1 unidade extra de '${asset.name}'",
                                                                    "INV_DECREMENT: ${asset.id} (Consumido 1 un)",
                                                                    "🛒"
                                                                )
                                                            }
                                                        }
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("SIMULAR VENDA", fontSize = 6.5.sp, color = Color.Black, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(9.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Apenas auditores fiscais ou CTO podem alterar precificações", fontSize = 7.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "LEGAL" -> {
                // PANEL D: Community Toxicity reports decisions queue + Financial ledger charts & payment logs!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    
                    // Toxicity reports decisions flow (Left Column)
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("SAC CENTRAL DE DENÚNCIAS (TOXICITY REPORTS)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Evidências detectadas por IA no Dojo:", fontSize = 8.sp, color = BlueprintTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))

                                managedToxicityReports.forEach { report ->
                                    val canModerateReports = checkPerm("MODERATION_WRITE")

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .border(
                                                0.5.dp,
                                                if (report.severity == "CRITICA") Color(0xFFEF4444) else BlueprintGridLine,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("@${report.reportedPlayer}", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                            Text(
                                                "SEVERIDADE: ${report.severity}",
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (report.severity == "CRITICA") Color(0xFFEF4444) else BlueprintOrange
                                            )
                                        }
                                        Text("Reportado por: @${report.reporter}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                        Text("Motivo: ${report.category}", fontSize = 7.5.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                                        Text("Evidência: \"${report.evidenceContent}\"", fontSize = 8.sp, color = BlueprintTextPrimary)

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Decision active buttons
                                        if (report.status == "PENDENTE") {
                                            if (canModerateReports) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .background(Color(0xFFEF4444).copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                                            .border(0.5.dp, Color(0xFFEF4444), RoundedCornerShape(2.dp))
                                                            .clickable {
                                                                val idx = managedToxicityReports.indexOf(report)
                                                                if (idx != -1) {
                                                                    managedToxicityReports[idx] = report.copy(status = "BANIDO")
                                                                    triggerLogAndAudit(
                                                                        "Moderador aplicou BAN MUNDIAL no infrator @${report.reportedPlayer}",
                                                                        "BAN_CONFIRMED: @${report.reportedPlayer}",
                                                                        "🛑"
                                                                    )
                                                                }
                                                            }
                                                            .padding(vertical = 4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("CONFIRMAR BAN", fontSize = 7.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .background(BlueprintTeal.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                                                            .border(0.5.dp, BlueprintTeal, RoundedCornerShape(2.dp))
                                                            .clickable {
                                                                val idx = managedToxicityReports.indexOf(report)
                                                                if (idx != -1) {
                                                                    managedToxicityReports[idx] = report.copy(status = "DESCARTADO")
                                                                    triggerLogAndAudit(
                                                                        "Moderador descartou denúncia contra @${report.reportedPlayer}",
                                                                        "REPORT_DISMISSED: ${report.id}",
                                                                        "✓"
                                                                    )
                                                                }
                                                            }
                                                            .padding(vertical = 4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("DESCARTAR", fontSize = 7.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                Text("🔒 Bloqueado: Moderadores de plantão possuem escopo", fontSize = 7.sp, color = Color.Gray)
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.DarkGray, RoundedCornerShape(2.dp))
                                                    .padding(vertical = 2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("RESOLVIDO: ${report.status}", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Corporate ledger and financial configs (Right Column)
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("MÉTRICAS FINANCEIRAS DO CONGLOMERADO (FINANCES)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Resumos macroeconômicos e tesouraria SaaS:", fontSize = 8.sp, color = BlueprintTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))

                                val financialIndicators = listOf(
                                    "Receita Mensal Recorrente (MRR)" to "US$ 145.200",
                                    "Saldo Líquido em Cofre JiuVerse" to "R$ 4.890.120",
                                    "Payout Pendente para Dojos" to "R$ 142.100",
                                    "Gas Fee Tributado Conversão" to "US$ 12.450"
                                )

                                financialIndicators.forEach { (field, valueStr) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(field, fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                        Text(valueStr, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                                    }
                                }

                                Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                                // Simulator trigger to force payouts cycle if allowed by active login role
                                val canHandleFinance = checkPerm("FINANCE_WRITE")

                                Text("Ciclo de Payout do Hub (Dividendo de Copas):", fontSize = 8.sp, color = BlueprintTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))

                                if (canHandleFinance) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(BlueprintTeal, RoundedCornerShape(4.dp))
                                            .clickable {
                                                triggerLogAndAudit(
                                                    "Cofre SaaS processou liberação de dividendos a 14 dōjōs associados.",
                                                    "TREASURY_PAYOUT_TRIGGERED (Total de R$ 142.100 liquidados)",
                                                    "🏦"
                                                )
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("PROCESSAR PAGAMENTOS AGORA", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Black)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.DarkGray, RoundedCornerShape(4.dp))
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🔒 Payout Bloqueado por Escopo RBAC", fontSize = 8.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 4. RETENTION METRICS LEDGER (DAU, MAU, retention cohorts and graphs)
        // ==========================================
        Text(
            text = "4. MÉTRICAS DE ENGAJAMENTO E RETENÇÃO DE LONGO PRAZO",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                
                // Key metrics cards
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val retentionCards = listOf(
                        Triple("DAU / MAU Stickiness", "48.2%", "Meta: >40%"),
                        Triple("Day-7 Cohort Retention", "58.6%", "Meta: >50%"),
                        Triple("Day-30 Cohort Retention", "31.2%", "Meta: >25%"),
                        Triple("User Churn Rate", "4.8%", "Meta: <5%")
                    )

                    retentionCards.forEach { (titleStr, scoreStr, ruleStr) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(titleStr, fontSize = 7.5.sp, color = BlueprintTextSecondary, textAlign = TextAlign.Center)
                            Text(scoreStr, fontSize = 13.sp, fontWeight = FontWeight.Black, color = BlueprintTeal)
                            Text(ruleStr, fontSize = 7.sp, color = BlueprintCyan)
                        }
                    }
                }

                // Textual Graph depicting Cohort day retention curves (Day 1 to 30)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Curva Coorte Retenção Relativa (60d Histórico):", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Simulated Retention Progress vertical/horizontal bars
                val barStats = listOf(
                    "Retenção Dia-1" to 88,
                    "Retenção Dia-3" to 75,
                    "Retenção Dia-7" to 58,
                    "Retenção Dia-14" to 42,
                    "Retenção Dia-30" to 31,
                    "Retenção Dia-60" to 19
                )

                barStats.forEach { (label, pct) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, fontSize = 8.sp, color = BlueprintTextSecondary, modifier = Modifier.weight(1f))
                        
                        // Row bar chart representation
                        Box(
                            modifier = Modifier
                                .weight(3f)
                                .height(8.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(pct / 100f)
                                    .background(BlueprintTeal, RoundedCornerShape(4.dp))
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Text("$pct%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary, modifier = Modifier.weight(0.4f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 5. REGULAR AUDIT LOG REEL (continuous audit checks logging user changes)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "5. COORDENÇÃO DE SEGURANÇA & AUDIT LOG REEL",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                mockAuditLogs.take(6).forEach { audit ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.8f)) {
                            Text(audit.severityIcon, fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(audit.action, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                Text("Invocado por operador de credencial: @${audit.actor}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                            }
                        }

                        Text(
                            text = "[${audit.timestamp}]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = BlueprintCyan,
                            modifier = Modifier.weight(0.5f),
                            textAlign = TextAlign.End
                        )
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
