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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

data class AcademyMemberState(
    val id: String,
    val name: String,
    var role: String, // "Dono", "Mestre", "Instrutor", "Aluno"
    val belt: String,
    val joinedDaysAgo: Int
)

data class AcademyEventState(
    val id: String,
    val name: String,
    val type: String, // "Campeonato", "Seminário", "Graduação"
    val entryFee: Int,
    val rewardPool: Int,
    val date: String
)

data class AcademyLeaderboardState(
    val rank: Int,
    val name: String,
    val members: Int,
    val reputation: Int,
    val treasury: Int,
    val symbol: String
)

@Composable
fun AcademyTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Interactive Designer State variables
    var academyName by remember { mutableStateOf("Alliance Moema") }
    var academyDesc by remember { mutableStateOf("Foco em Jiu-Jitsu de competição, treinos de guarda lapela e passagem rápida.") }
    var academyLogo by remember { mutableStateOf("🛡️ Brasão Classic") }
    var tokenSymbol by remember { mutableStateOf("ALLN") }
    var tokenTaxRate by remember { mutableStateOf(10f) } // 10%
    var treasuryBalance by remember { mutableStateOf(15800) } // Local JiuTokens
    
    // Roster Members
    val membersList = remember {
        mutableStateListOf(
            AcademyMemberState("1", "Mestre Fabio Gurgel", "Mestre", "Preta 4º Grau", 412),
            AcademyMemberState("2", "Bruno Malfacine", "Instrutor", "Preta", 230),
            AcademyMemberState("3", "Marcus Buchecha", "Instrutor", "Preta", 185),
            AcademyMemberState("4", "FaixaAzulGuerreiro", "Aluno", "Azul", 45),
            AcademyMemberState("5", "GuardaPassador99", "Aluno", "Roxa", 12)
        )
    }

    // Scheduled Events
    val activeEvents = remember {
        mutableStateListOf(
            AcademyEventState("ev_1", "Seminário de Passagem de Meia Guarda", "Seminário", 50, 500, "Amanhã, 19:30"),
            AcademyEventState("ev_2", "Copa Interna Sem Kimono (NoGi)", "Campeonato", 100, 2000, "14/06/2026"),
            AcademyEventState("ev_3", "Cerimônia de Graduação - Faixas de Verão", "Graduação", 0, 0, "20/06/2026")
        )
    }

    // Other academies leaderboard mock (Game Balance simulation)
    val serverLeaderboard = listOf(
        AcademyLeaderboardState(1, "Alliance Moema", 5, 2300, 15800, "ALLN"),
        AcademyLeaderboardState(2, "Gracie Barra central", 44, 2150, 42000, "GBTX"),
        AcademyLeaderboardState(3, "Atos San Diego", 38, 1980, 29500, "ATOS"),
        AcademyLeaderboardState(4, "Dream Art", 25, 1450, 11000, "DART"),
        AcademyLeaderboardState(5, "Cicero Costha", 22, 1200, 8900, "PSLPB")
    )

    // Form states for adding new recruits / scheduling events
    var inviteNickname by remember { mutableStateOf("") }
    var newEventName by remember { mutableStateOf("") }
    var newEventType by remember { mutableStateOf("Campeonato") }
    var newEventFee by remember { mutableStateOf("50") }
    var newEventReward by remember { mutableStateOf("500") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Sistema de Academias",
            subtitle = "Game Design e Economia das Corporações e Equipes de Luta"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(0.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationImportant,
                contentDescription = null,
                tint = BlueprintCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VISÃO DE GAME PLANNERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Simule de forma interativa como os parâmetros de economia de tokens, contratações de staff e taxas administrativas influenciam a retenção de alunos no JiuVerse.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // First Section: General Customizer Dashboard
        Text(
            text = "1. IDENTIDADE E FUNDO DA ACADEMIA",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Identity fields Card
            Card(
                modifier = Modifier.weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Nome da Guilda / Academia", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                    BasicTextField(
                        value = academyName,
                        onValueChange = { academyName = it },
                        textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF020617), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Descrição Pública", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                    BasicTextField(
                        value = academyDesc,
                        onValueChange = { academyDesc = it },
                        textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF020617), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Brasão Logo", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF020617), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable {
                                        academyLogo = if (academyLogo.contains("Classic")) "🛡️ Brasão Águia" else "🛡️ Brasão Classic"
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(academyLogo, fontSize = 10.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Símbolo Token", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                            BasicTextField(
                                value = tokenSymbol,
                                onValueChange = { tokenSymbol = it.uppercase() },
                                textStyle = TextStyle(color = BlueprintCyan, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF020617), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            // Economic treasury dashboard card
            Card(
                modifier = Modifier.weight(0.8f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF022329)),
                border = BorderDefaults_outlinedCardBorder(BlueprintTeal)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalAtm,
                        contentDescription = null,
                        tint = BlueprintTeal,
                        modifier = Modifier.size(22.dp)
                    )
                    Text("TESOURARIA PROPRIEDADE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = BlueprintTeal)
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "$treasuryBalance $tokenSymbol",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text("Sub-Economia ativa", fontSize = 8.sp, color = BlueprintTextSecondary)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Taxa Administrativa: ${tokenTaxRate.toInt()}%", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                    Text("Reteve de missões diárias", fontSize = 7.sp, color = BlueprintTextSecondary)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                .clickable { tokenTaxRate = Math.max(0f, tokenTaxRate - 5f) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("-5%", fontSize = 9.sp, color = BlueprintRed, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                .clickable { tokenTaxRate = Math.min(50f, tokenTaxRate + 5f) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+5%", fontSize = 9.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Second Section: Interactive Member Roster
        Text(
            text = "2. QUADRO DE FILIADOS (INSTRUTORES E ALUNOS)",
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
                // Table header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BlueprintGridLine)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Lutador / Roster", modifier = Modifier.weight(1.2f), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                    Text("Cargo", modifier = Modifier.weight(0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                    Text("Ações", modifier = Modifier.weight(1f), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                }

                membersList.forEach { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🥋", fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(m.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            }
                            Text("Faixa ${m.belt} • Filiado há ${m.joinedDaysAgo} dias", fontSize = 8.sp, color = BlueprintTextSecondary)
                        }

                        // Role badge
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .padding(horizontal = 2.dp)
                        ) {
                            val roleColor = when(m.role) {
                                "Mestre" -> BlueprintOrange
                                "Instrutor" -> BlueprintTeal
                                "Dono" -> BlueprintCyan
                                else -> BlueprintTextSecondary
                            }
                            Box(
                                modifier = Modifier
                                    .border(0.5.dp, roleColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(m.role.uppercase(), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = roleColor)
                            }
                        }

                        // Administrative Actions Row
                        Row(
                            modifier = Modifier.weight(1.2f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (m.name != "Mestre Fabio Gurgel") {
                                // Promote action
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF132D28), RoundedCornerShape(4.dp))
                                        .clickable {
                                            m.role = when (m.role) {
                                                "Aluno" -> "Instrutor"
                                                "Instrutor" -> "Mestre"
                                                else -> "Aluno"
                                            }
                                            // Force trigger update
                                            val index = membersList.indexOf(m)
                                            if (index != -1) {
                                                membersList[index] = m.copy(role = m.role)
                                            }
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("PROMOVID", fontSize = 7.sp, color = BlueprintTeal, fontWeight = FontWeight.Bold)
                                }

                                // Kick action
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF3B1E28), RoundedCornerShape(4.dp))
                                        .clickable {
                                            membersList.remove(m)
                                            treasuryBalance += 200 // Expelling students releases collateral
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("EXPULS", fontSize = 7.sp, color = BlueprintRed, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("(Fundador)", fontSize = 8.sp, color = BlueprintTextSecondary)
                            }
                        }
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Recruitment Invitation Trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = inviteNickname,
                        onValueChange = { inviteNickname = it },
                        textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF020617), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        decorationBox = { innerTextField ->
                            if (inviteNickname.isEmpty()) {
                                Text("Nome do Atleta para recrutar...", fontSize = 11.sp, color = BlueprintTextSecondary)
                            }
                            innerTextField()
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (inviteNickname.isNotEmpty()) {
                                membersList.add(
                                    AcademyMemberState(
                                        id = (membersList.size + 1).toString(),
                                        name = inviteNickname,
                                        role = "Aluno",
                                        belt = "Azul",
                                        joinedDaysAgo = 0
                                    )
                                )
                                inviteNickname = ""
                                treasuryBalance -= 100 // Enlisting cost
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132D46)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.testTag("academy_recruit_btn")
                    ) {
                        Text("RECRUTAR", fontSize = 10.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Third Section: Event Organiser
        Text(
            text = "3. CRONOGRAMA DE EVENTOS E PREMIAÇÃO EM TOKENS",
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
                Text(
                    text = "Eventos geram taxas de inscrição em JiuTokens para a tesouraria física, dividindo faturamento e pagando prêmios para os vencedores das chaves (Brackets).",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                activeEvents.forEach { ev ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(ev.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                            Text("Tipo: ${ev.type} • Data: ${ev.date}", fontSize = 8.5.sp, color = BlueprintTextSecondary)
                        }

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Insc.: ${ev.entryFee} $tokenSymbol", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                                if (ev.rewardPool > 0) {
                                    Text("Pool: ${ev.rewardPool} $tokenSymbol", fontSize = 8.sp, color = BlueprintOrange)
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Cancelar Evento",
                                tint = BlueprintRed.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { activeEvents.remove(ev) }
                            )
                        }
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Schedule Event Inline Form
                Text("Simulador de Eventos Rápidos", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BasicTextField(
                        value = newEventName,
                        onValueChange = { newEventName = it },
                        textStyle = TextStyle(color = BlueprintTextPrimary, fontSize = 11.sp),
                        modifier = Modifier
                            .weight(1.5f)
                            .background(Color(0xFF020617), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .padding(6.dp),
                        decorationBox = { inner ->
                            if (newEventName.isEmpty()) Text("Copa Verão / Seminário...", fontSize = 10.sp, color = BlueprintTextSecondary)
                            inner()
                        }
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF020617), RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                            .clickable {
                                newEventType = if (newEventType == "Campeonato") "Seminário" else "Campeonato"
                            }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(newEventType, fontSize = 9.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (newEventName.isNotEmpty()) {
                                activeEvents.add(
                                    AcademyEventState(
                                        id = "ev_" + (activeEvents.size + 1),
                                        name = newEventName,
                                        type = newEventType,
                                        entryFee = newEventFee.toIntOrNull() ?: 0,
                                        rewardPool = newEventReward.toIntOrNull() ?: 0,
                                        date = "Próxima Semana"
                                    )
                                )
                                // Add fee to treasury simulator config
                                treasuryBalance -= (newEventReward.toIntOrNull() ?: 0) / 2
                                treasuryBalance += (newEventFee.toIntOrNull() ?: 0) * membersList.size
                                newEventName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueprintTeal),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("AGENDAR", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fourth Section: Leaderboard own stats
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "4. RANKING GERAL DE ACADEMIAS DO JIUVERSE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = BlueprintOrange, modifier = Modifier.size(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF020617))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pos", modifier = Modifier.weight(0.3f), fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                    Text("Equipe", modifier = Modifier.weight(1.5f), fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                    Text("Lutadores", modifier = Modifier.weight(0.7f), fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                    Text("Moeda Símb.", modifier = Modifier.weight(0.7f), fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Prestígio", modifier = Modifier.weight(0.6f), fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                }

                serverLeaderboard.forEach { school ->
                    val isLocal = school.name == "Alliance Moema"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isLocal) Color(0xFF132D46).copy(alpha = 0.5f) else Color.Transparent)
                            .padding(vertical = 6.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${school.rank}",
                            modifier = Modifier.weight(0.3f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (school.rank == 1) BlueprintOrange else BlueprintTextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isLocal) academyName else school.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isLocal) BlueprintCyan else BlueprintTextPrimary)
                            if (isLocal) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF0F766E).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("SUA", fontSize = 7.sp, color = BlueprintTeal, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        Text(text = if (isLocal) "${membersList.size}" else "${school.members}", modifier = Modifier.weight(0.7f), fontSize = 11.sp, color = BlueprintTextPrimary)
                        Text(
                            text = if (isLocal) tokenSymbol else school.symbol,
                            modifier = Modifier.weight(0.7f),
                            fontSize = 11.sp,
                            color = BlueprintTeal,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${school.reputation + (if(isLocal) membersList.size * 10 else 0)} pts",
                            modifier = Modifier.weight(0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintOrange
                        )
                    }
                    Divider(color = BlueprintGridLine, thickness = 0.5.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Custom Border factory workaround for Compose
@Composable
private fun BorderDefaults_outlinedCardBorder(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.dp, color)
}
