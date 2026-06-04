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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

// 1. Data Models for competitive MMORPG Guild Wars Simulation
data class AcademyWarState(
    val id: String,
    val name: String,
    val logo: String,
    var reputation: Int,
    var territoriesCount: Int,
    var allianceName: String?,
    var isAtWar: Boolean,
    var currentOpponent: String?,
    var warStakeCredits: Int,
    val winRate: Float,
    val totalWars: Int
)

data class TerritoryNode(
    val id: String,
    val name: String,
    val emoji: String,
    var currentHolder: String,
    var defenseMultiplier: Float,
    val weeklyJiuTokenYield: Int,
    var isUnderContest: Boolean,
    val district: String
)

data class WarSeasonInfo(
    val seasonNumber: Int,
    val title: String,
    val daysRemaining: Int,
    val totalCreditsPool: Int,
    val legendaryRewardLabel: String
)

data class WarLogEvent(
    val timeLabel: String,
    val description: String,
    val decorationTag: String,
    val colorAccent: Color
)

@Composable
fun GuildWarsTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- State: Game Master Seasonal Setup ---
    val currentSeason = remember {
        WarSeasonInfo(
            seasonNumber = 4,
            title = "Cinturão de Titânio Cósmico",
            daysRemaining = 12,
            totalCreditsPool = 1250000,
            legendaryRewardLabel = "Quimono de Ouro Maciço (+15% Atributo Crítico)"
        )
    }

    // --- State: Academy List Databases (Simulating MMO participants) ---
    val academyList = remember {
        mutableStateListOf(
            AcademyWarState("acad_1", "Alliance Copacabana", "🦁", 4500, 3, "Coalizão Sul", false, null, 0, 0.78f, 120),
            AcademyWarState("acad_2", "Gracie Barra Imperial", "🦅", 4200, 2, "Coalizão Sul", false, null, 0, 0.74f, 115),
            AcademyWarState("acad_3", "Atos Orla Central", "🦈", 3900, 1, null, false, null, 0, 0.69f, 95),
            AcademyWarState("acad_4", "Dream Art Elite", "⚡", 3500, 1, null, false, null, 0, 0.65f, 80),
            AcademyWarState("acad_5", "Cicero Costha Dojo", "🐅", 3100, 0, "Aliança Zona Norte", false, null, 0, 0.58f, 72),
            AcademyWarState("acad_6", "Nova União Celestial", "🔥", 2800, 0, "Aliança Zona Norte", false, null, 0, 0.52f, 60),
            AcademyWarState("acad_7", "Dojo da Restinga (Seu CT)", "🥋", 2500, 1, null, false, null, 0, 0.50f, 10) // Player's own Academy
        )
    }

    // --- State: Territory Map (Conquista de Territórios) ---
    val territoriesList = remember {
        mutableStateListOf(
            TerritoryNode("t_1", "Centro de Treinamento Copacabana", "🏖️", "Alliance Copacabana", 1.8f, 25000, false, "Zona Sul"),
            TerritoryNode("t_2", "Dojo Flutuante da Restinga", "🌊", "Dojo da Restinga (Seu CT)", 1.2f, 8000, false, "Litoral"),
            TerritoryNode("t_3", "Palácio de Cristal de Angra", "🏰", "Gracie Barra Imperial", 1.9f, 45000, false, "Angra Resort"),
            TerritoryNode("t_4", "Academia eSports Arena VIP", "🎮", "Atos Orla Central", 1.5f, 18000, false, "Metropóli"),
            TerritoryNode("t_5", "Templo do Combate Tradicional", "⛩️", "Alliance Copacabana", 1.6f, 22000, false, "Lapa Antiga"),
            TerritoryNode("t_6", "CT das Montanhas de Petrópolis", "🏔️", "Dream Art Elite", 1.4f, 15000, false, "Serra")
        )
    }

    // --- State: Live War Logs ---
    val liveWarLogs = remember {
        mutableStateListOf(
            WarLogEvent("Agora", "Alliance Copacabana propôs aliança com Gracie Barra Imperial formando 'Coalizão Sul'.", "ALIANÇA", BlueprintTeal),
            WarLogEvent("5m atrás", "Dream Art Elite declarou guerra contra Atos Orla Central disputando Arena VIP!", "DECLARAÇÃO", BlueprintOrange),
            WarLogEvent("18m atrás", "Dojo da Restinga (Seu CT) repeliu incursão invasora clandestina do Cicero Costha Dojo.", "DEFESA", BlueprintGreen),
            WarLogEvent("45m atrás", "Atos Orla Central conquistou o território 'Academia eSports Arena VIP' somando +18000 JiuTokens semanais.", "CONQUISTA", BlueprintCyan)
        )
    }

    // --- State: Interactive Match Planner & Declarar Guerra ---
    var selectedTargetAcademyIndex by remember { mutableStateOf(0) }
    val selectedTargetAcademy = academyList[selectedTargetAcademyIndex]
    var stakeAmountInput by remember { mutableStateOf("15000") }
    var warDeclareStatusMsg by remember { mutableStateOf("Pronto para despachar ofício de guerra.") }

    // --- State: Player Live PvP Contribution Selector ---
    var selectedCombatTactic by remember { mutableStateOf("Passador de Guarda") } // "Passador de Guarda", "Guarda De La Riva", "Chave de Braço Tática"
    var pvpSimResult by remember { mutableStateOf<String?>(null) }
    var pointsEarnedThisTurn by remember { mutableStateOf(0) }
    var totalCoinsCreditsMultiplier by remember { mutableStateOf(42000) }

    // --- State: 10,000 Concurrent Players MMO Engine Load Simulator ---
    var isSimulatingScale by remember { mutableStateOf(false) }
    var simulationProgress by remember { mutableStateOf(0f) }
    var simulatedQueriesPerSecond by remember { mutableStateOf(0) }
    var pipelineLatencyMs by remember { mutableStateOf(4.5f) }
    var simulatedConflictsResolved by remember { mutableStateOf(0) }

    // Coroutine helper to simulate thousands of players in real-time
    fun runMMOLoadSimulation() {
        if (isSimulatingScale) return
        scope.launch {
            isSimulatingScale = true
            simulationProgress = 0f
            pipelineLatencyMs = 4.2f
            simulatedQueriesPerSecond = 50

            while (simulationProgress < 1.0f) {
                delay(120)
                simulationProgress += 0.05f
                simulatedQueriesPerSecond = kotlin.random.Random.nextInt(4800, 10500)
                pipelineLatencyMs = kotlin.random.Random.nextDouble(2.1, 14.8).toFloat()
                simulatedConflictsResolved += kotlin.random.Random.nextInt(15, 60)
            }

            simulationProgress = 1.0f
            simulatedQueriesPerSecond = 9200
            pipelineLatencyMs = 3.1f
            isSimulatingScale = false
            liveWarLogs.add(0, WarLogEvent("Agora", "Simulação MMO: Processado e validado 10.000 sparrins em rede concorrente via Actor Model em 2.4 segundos.", "BENCHMARK", BlueprintCyan))
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
            title = "JiuVerse Guild Wars & Conquista de Territórios",
            subtitle = "Motor do Ecossistema Competitivo de Alianças de Alta Frequência. Declare guerra contra dojo rivais, capture distritos estratégicos de Rio de Janeiro e treine alianças."
        )

        // I. CRONOGRAMA DA TEMPORADA E RECOMPENSAS PRINCIPAIS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(1.dp, BlueprintCyan, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(BlueprintOrange, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TEMPORADA COMPETITIVA AVANÇADA - S0${currentSeason.seasonNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = BlueprintOrange,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Torneio: '${currentSeason.title}'",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Prêmio da Season: ${currentSeason.legendaryRewardLabel}",
                    fontSize = 9.5.sp,
                    color = BlueprintTeal,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(0.8f)
            ) {
                Text(
                    text = "Dias Restantes: ${currentSeason.daysRemaining}d",
                    fontSize = 10.sp,
                    color = BlueprintTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pool Geral: ${currentSeason.totalCreditsPool} C",
                    fontSize = 10.sp,
                    color = BlueprintCyan,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .clickable {
                            if (pointsEarnedThisTurn >= 100) {
                                totalCoinsCreditsMultiplier += 15000
                                pointsEarnedThisTurn = 0
                                warDeclareStatusMsg = "Recompensa de Guerra Semanal de 15,000 JiuTokens Resgatada!"
                            } else {
                                warDeclareStatusMsg = "Precisa acumular pelo menos 100 pontos de glória no PvP hoje para receber prêmio."
                            }
                        }
                        .background(BlueprintOrange.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, BlueprintOrange, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("RESGATAR RECOMPENSAS", fontSize = 7.5.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // II. QUADRO COMPARATIVO E DECLARAR GUERRA (COMPETITIVE DOCKER)
        Text(
            text = "I. DECLARAR GUERRA E SISTEMA DE EMBAIXADAS (ALIANÇAS)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "O sistema exige o depósito de stakes em JiuTokens para propor batalhas oficiais de cinturão. Equipes em guerra ganham direito de lutar nos dias síncronos da guerra semanal.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // LEFT COLUMN: ACADEMY LIST & OPPONENTS SELECTOR
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Selecione Dojo Alvo para Combates:", fontSize = 8.5.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                        
                        Column(
                            modifier = Modifier
                                .height(160.dp)
                                .fillMaxWidth()
                                .background(Color.Black)
                                .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                .padding(4.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            academyList.forEachIndexed { idx, academy ->
                                val isSelected = selectedTargetAcademyIndex == idx
                                val hasAlliance = academy.allianceName != null
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) Color(0xFF1B2A4A) else Color(0xFF0C1424))
                                        .border(0.5.dp, if (isSelected) BlueprintCyan else Color.Transparent)
                                        .clickable { selectedTargetAcademyIndex = idx }
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(academy.logo, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(academy.name, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Wr: ${String.format("%.0f", academy.winRate * 100)}% • Wars: ${academy.totalWars}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                                if (hasAlliance) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(BlueprintTeal.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                                            .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                                    ) {
                                                        Text("Allied", fontSize = 5.5.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${academy.reputation} Elo",
                                        fontSize = 9.sp,
                                        color = BlueprintOrange,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // RIGHT COLUMN: WAR PROGRESION & ALLIANCE DECLARATOR
                    Column(
                        modifier = Modifier.weight(0.9f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Painel de Governança e Guerra", fontSize = 8.5.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            border = BorderStroke(0.5.dp, BlueprintGridLine)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "OPONENTE SELECIONADO:",
                                    fontSize = 7.5.sp,
                                    color = BlueprintCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${selectedTargetAcademy.logo} ${selectedTargetAcademy.name}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Aliança integrada: ${selectedTargetAcademy.allianceName ?: "Nenhuma"}",
                                    fontSize = 8.sp,
                                    color = BlueprintTextSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Stake (C):", fontSize = 7.5.sp, color = Color.White, modifier = Modifier.weight(1f))
                                    OutlinedTextField(
                                        value = stakeAmountInput,
                                        onValueChange = { stakeAmountInput = it },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.sp, color = Color.White),
                                        modifier = Modifier
                                            .width(70.dp)
                                            .height(30.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.Black,
                                            unfocusedContainerColor = Color.Black
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Operations: War & Treaty
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (selectedTargetAcademy.id == "acad_7") {
                                                warDeclareStatusMsg = "Você não pode declarar guerra contra sua própria academia física de Angra!"
                                                return@Button
                                            }
                                            val cost = stakeAmountInput.toIntOrNull() ?: 15000
                                            if (totalCoinsCreditsMultiplier >= cost) {
                                                totalCoinsCreditsMultiplier -= cost
                                                selectedTargetAcademy.isAtWar = true
                                                selectedTargetAcademy.currentOpponent = "Dojo da Restinga (Seu CT)"
                                                selectedTargetAcademy.warStakeCredits = cost
                                                warDeclareStatusMsg = "DESAFIO FORMALIZADO! Stakes de $cost C travados no Contrato Inteligente de Glória."
                                                liveWarLogs.add(0, WarLogEvent("Agora", "Dojo da Restinga declarou guerra síncrona contra '${selectedTargetAcademy.name}' valendo prêmio de $cost Credits.", "GUERRA DECLARADA", BlueprintRed))
                                            } else {
                                                warDeclareStatusMsg = "Finanças Insuficientes para cobrir o stake mínimo de $cost Credits."
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintRed, contentColor = Color.White),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(28.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("GUERRA ⚔️", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            if (selectedTargetAcademy.id == "acad_7") {
                                                warDeclareStatusMsg = "Não faz sentido emitir coalizão com o próprio CT."
                                                return@Button
                                            }
                                            actionDeclareAlliance(
                                                academy = selectedTargetAcademy,
                                                onSuccess = { alliance ->
                                                    warDeclareStatusMsg = "Tratado assinado com '${selectedTargetAcademy.name}' sob Coalizão '$alliance'."
                                                    liveWarLogs.add(0, WarLogEvent("Agora", "Formada aliança entre Dojo da Restinga e ${selectedTargetAcademy.name} via tratado de cooperação.", "TRATADO DOJO", BlueprintTeal))
                                                }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal, contentColor = Color.Black),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(28.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("ALIANÇA 🤝", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Telemetry status text output block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .border(1.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "📜 TELEMETRIA CONSULAR: $warDeclareStatusMsg",
                        fontSize = 9.sp,
                        color = BlueprintCyan,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // III. CONQUISTA DE TERRITÓRIOS E MAPA DO JIUVERSE
        Text(
            text = "II. CONQUISTA DE TERRITÓRIOS E MAPA ESTRATÉGICO",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Abaixo está o mapa síncrono dos distritos conquistáveis do JiuVerse. Cada região ocupada gera faturamento passivo de JiuTokens distribuído para o cofre da equipe todos os sábados síncronos às 22:00.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Layout of Strategic Map Tiles
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    territoriesList.forEachIndexed { i, territory ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (territory.currentHolder.contains("Restinga")) Color(0xFF132D28) else Color.Black)
                                .border(1.dp, if (territory.currentHolder.contains("Restinga")) BlueprintTeal else BlueprintGridLine, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF1E293B), CircleShape)
                                        .border(1.dp, BlueprintCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(territory.emoji, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(territory.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Ocupante: ", fontSize = 8.sp, color = BlueprintTextSecondary)
                                        Text(territory.currentHolder, fontSize = 8.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "Distrito: ${territory.district} | Multiplicador de Defesa (Raid): ${territory.defenseMultiplier}x",
                                        fontSize = 7.5.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(0.7f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "+${territory.weeklyJiuTokenYield} C/s.",
                                    fontSize = 10.sp,
                                    color = BlueprintTeal,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Attack & Contest actions (Simulate combat engagement)
                                val ownsTerritory = territory.currentHolder.contains("Seu CT")
                                Button(
                                    onClick = {
                                        if (ownsTerritory) {
                                            // Fortify defenses
                                            territoriesList[i] = territory.copy(defenseMultiplier = territory.defenseMultiplier + 0.1f)
                                            liveWarLogs.add(0, WarLogEvent("Agora", "Dojo da Restinga investiu 5000 C nas barreiras de segurança de '${territory.name}', elevando a defesa.", "SEGURANÇA", BlueprintTeal))
                                            warDeclareStatusMsg = "Defesas de ${territory.name} fortificadas síncronamente."
                                        } else {
                                            // Raid and capture simulation
                                            val oldHolder = territory.currentHolder
                                            territoriesList[i] = territory.copy(currentHolder = "Dojo da Restinga (Seu CT)", defenseMultiplier = 1.0f)
                                            liveWarLogs.add(0, WarLogEvent("Agora", "Ataque Sucedido! Dojo da Restinga derrotou guardas do $oldHolder e capturou '${territory.name}'.", "VITORIA RAID", BlueprintCyan))
                                            warDeclareStatusMsg = "Você derrotou as sentinelas de '${territory.name}' e assumiu a soberania da área!"
                                            // update parent stats of player academy
                                            val ownAcademy = academyList.find { it.id == "acad_7" }
                                            if (ownAcademy != null) {
                                                ownAcademy.territoriesCount += 1
                                                ownAcademy.reputation += 350
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (ownsTerritory) BlueprintTeal else BlueprintOrange,
                                        contentColor = Color.Black
                                    ),
                                    modifier = Modifier.height(24.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text(
                                        text = if (ownsTerritory) "FORTIFICAR DEFESAS" else "CONQUISTAR ÁREA",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // IV. LIVE PVP COMBAT SIMULATION & PLAYER TACTICS
        Text(
            text = "III. CONTRIBUIÇÃO DE GUERRA INTERATIVA (SIMULAÇÃO PVP)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Lute na arena para testar táticas e creditar pontos de contribuição para a guilda. Cada vitória no PvP aumenta as chances de sucesso da sua academia na guerra semanal.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // LEFT COLUMN: TACTICS LIST
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Selecione sua Tática de Combate:", fontSize = 8.5.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))

                        val tactics = listOf(
                            Pair("Passador de Guarda", "Foco em cansar oponente, aplicar pressão por cima e buscar controle lateral."),
                            Pair("Guarda De La Riva", "Guarda flexível ideal para desequilibrar oponentes pesados e buscar as costas."),
                            Pair("Chave de Braço Tática", "Explosividade tática visando finalização rápida em double leg e armlock seco.")
                        )

                        tactics.forEach { pair ->
                            val isSelected = selectedCombatTactic == pair.first
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) Color(0xFF132D28) else Color.Black)
                                    .border(0.5.dp, if (isSelected) BlueprintTeal else BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable { selectedCombatTactic = pair.first }
                                    .padding(6.dp)
                            ) {
                                Text(pair.first, fontSize = 9.sp, color = if (isSelected) BlueprintTeal else Color.White, fontWeight = FontWeight.Bold)
                                Text(pair.second, fontSize = 7.5.sp, color = BlueprintTextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    // RIGHT COLUMN: LIVE PVP TRIGGER ENGINE
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Arena de Simulação Síncrona", fontSize = 8.5.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color.Black)
                                .border(1.dp, BlueprintGridLine, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (pvpSimResult == null) {
                                Text(
                                    text = "Clique abaixo para confrontar um campeão e validar estratégia '$selectedCombatTactic'",
                                    fontSize = 9.sp,
                                    color = BlueprintTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = pvpSimResult ?: "",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Launch simulated Sparring match
                            Button(
                                onClick = {
                                    val isSuccess = (0..100).random() > 33
                                    if (isSuccess) {
                                        pointsEarnedThisTurn += 25
                                        val ownAcademy = academyList.find { it.id == "acad_7" }
                                        if (ownAcademy != null) {
                                            ownAcademy.reputation += 50
                                        }
                                        pvpSimResult = "VITÓRIA! 🥋 Movimento '$selectedCombatTactic' encaixado perfeito!\n+25 Pontos de Guerra para o CT."
                                        liveWarLogs.add(0, WarLogEvent("Agora", "Sua atividade PvP na taverna garantiu +50 Elo para o Dojo da Restinga.", "COMBATE", BlueprintGreen))
                                    } else {
                                        pvpSimResult = "DERROTA. 🤕 Oponente antecipou com passagem de meia-guarda astuta.\nTente novarte com outro set."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlueprintOrange, contentColor = Color.Black),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("ROLAR SPARRING 🥋", fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }

                            // Score reset or training
                            Button(
                                onClick = {
                                    pvpSimResult = null
                                    pointsEarnedThisTurn = 0
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("RESET", fontSize = 9.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Seus Pontos de Hoje: $pointsEarnedThisTurn GP / 100",
                            fontSize = 8.sp,
                            color = BlueprintCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // V. ULTRA HIGH MULTI-PLAYER LOAD BENCHMARKER / ARQUITETURA DE REDE
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "IV. ARQUITETURA CONCORRENTE MMO (MILHARES DE JOGADORES)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Box(
                modifier = Modifier
                    .background(BlueprintOrange.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .border(0.5.dp, BlueprintOrange, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("Carga: 10k Concorrente", fontSize = 8.sp, color = BlueprintOrange, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "A robusta infraestrutura de rede do JiuVerse é projetada com cluster de atores em Kotlin Coroutines (Ktor/WebSockets), caching paralelo em Redis Cluster para evitar sobrecarga no PostgreSQL de Angra.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Grid stats for MMO Stress Test Simulation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(0.5.dp, BlueprintGridLine)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("QUERIES / SEGUNDO", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                            Text(
                                text = if (isSimulatingScale) "$simulatedQueriesPerSecond QPS" else "Zero Idle",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSimulatingScale) BlueprintOrange else Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(0.5.dp, BlueprintGridLine)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("PIPELINE LATENCY", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                            Text(
                                text = if (isSimulatingScale) "${String.format("%.1f", pipelineLatencyMs)} ms" else "0.3 ms",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintTeal,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        modifier = Modifier.weight(1.2f),
                        border = BorderStroke(0.5.dp, BlueprintGridLine)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("BATTLES RESOLVED", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                            Text(
                                text = "$simulatedConflictsResolved confl.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Load Progress bar trigger
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSimulatingScale) "🔥 PROCESSANDO ESTRESSE SÍNCRONO PARALELO..." else "INATIVO - PRONTO PARA DISPARO",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimulatingScale) BlueprintOrange else Color.Gray
                        )
                        Text(
                            text = "${(simulationProgress * 100).toInt()}%",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    LinearProgressIndicator(
                        progress = simulationProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = BlueprintOrange,
                        trackColor = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { runMMOLoadSimulation() },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSimulatingScale) Color.DarkGray else BlueprintCyan, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        enabled = !isSimulatingScale
                    ) {
                        Text(
                            text = "⚡ DISPARAR MASSIVE LOAD BENCHMARK (10.000 JOGADORES)",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // VI. HISTÓRICO DE AUDITORIA & REGISTROS DE GUERRA DO CANAL DE COMUNIDADE (Live War Logs)
        Text(
            text = "IV. DIÁRIO DE COMBATES & MOVIMENTAÇÃO DE FRONTEIRAS (LIVE FEEDS)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    liveWarLogs.forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(0.5.dp, BlueprintGridLine)
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(log.colorAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, log.colorAccent, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(text = log.decorationTag, fontSize = 7.sp, color = log.colorAccent, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = log.description,
                                    fontSize = 8.5.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = log.timeLabel,
                                fontSize = 7.5.sp,
                                color = BlueprintTextSecondary,
                                modifier = Modifier.weight(0.5f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// Emulate Alianças Logic
private fun actionDeclareAlliance(academy: AcademyWarState, onSuccess: (String) -> Unit) {
    if (academy.allianceName == null) {
        val availableAlliances = listOf("Coalizão Sul", "Aliança Zona Norte", "Frente Imperial Restinga")
        val chosen = availableAlliances.random()
        academy.allianceName = chosen
        onSuccess(chosen)
    } else {
        onSuccess(academy.allianceName !!)
    }
}
