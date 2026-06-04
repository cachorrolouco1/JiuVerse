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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.*

// 1. Data Structure representing state of an Elite intelligent NPC in JiuVerse
data class IntelligentNpc(
    val id: String,
    val name: String,
    val role: String,
    val emoji: String,
    val personality: String,
    val defaultGreeting: String,
    val specialTechnique: String,
    val recommendedDojo: String,
    val initialQuest: String,
    val memoryFact: String, // Dynamic context retrieved about player
    val baseColor: Color,
    val reputationNeeded: Int
)

@Composable
fun AInpcTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Definition of the 7 mandatory intelligent NPCs
    val npcsList = remember {
        listOf(
            IntelligentNpc(
                id = "npc_jiujitsu",
                name = "Grande Mestre Robson",
                role = "Mestre de Jiu-Jitsu",
                emoji = "👴🥋",
                personality = "Sábio, tradicionalista, focado em alavancas invisíveis e na filosofia do respeito mútuo. Exige dedicação extrema.",
                defaultGreeting = "Oss, jovem guardeiro. A força física falha, mas a mecânica pura é eterna. Está pronto para o tatame estelar?",
                specialTechnique = "Lapel Helix Sweep (Raspagem de Lapela Cósmica)",
                recommendedDojo = "Gracie Barra Angra Central",
                initialQuest = "Derrubar 3 oponentes de categoria superior sem ceder a montada em sparring de simulação.",
                memoryFact = "Lembra que você quase bateu na Kimura ontem e prefere o jogo por baixo (guardeiro).",
                baseColor = BlueprintCyan,
                reputationNeeded = 50
            ),
            IntelligentNpc(
                id = "npc_judo",
                name = "Sensei Yamato",
                role = "Mestre de Judô",
                emoji = "🥋🇯🇵",
                personality = "Honroso, rígido com a postura (Shisei), obcecado por desequilíbrio (Kuzushi) e projeções táticas perfeitas.",
                defaultGreeting = "Rei! O judoca que não domina o Kuzushi é apenas um peso morto gravitando no tatame. Vamos afiar o Uchi-mata?",
                specialTechnique = "Kuzushi Uchi-Mata (Projeção Giratória de Alta Inércia)",
                recommendedDojo = "Kodan Shugyo Academy",
                initialQuest = "Concluir 10 repetições de Uchi-Komi síncronas com 100% de precisão de cursor.",
                memoryFact = "Registrou que seu quadril é rígido nas transições laterais e recomenda treino de flexibilidade.",
                baseColor = Color(0xFFC084FC),
                reputationNeeded = 75
            ),
            IntelligentNpc(
                id = "npc_muaythai",
                name = "Kru Somchai",
                role = "Mestre de Muay Thai",
                emoji = "🥊🇹🇭",
                personality = "Explosivo, rítmico, usa expressões em tailandês (Sawadee krap). Focado na resiliência mental e canelas de aço.",
                defaultGreeting = "Sawadee krap! Oito armas! Punhos, cotovelos, joelhos e canelas. Se sua canela estiver mole, vá treinar futebol!",
                specialTechnique = "Tei Kha Low Kick (Chute circular cinético na articulação da coxa)",
                recommendedDojo = "Tiger Muay Thai RJ filial Angra",
                initialQuest = "Destruir 5 sacos de pancada de alta densidade no ginásio em menos de 45 segundos.",
                memoryFact = "Sabe que você possui resistência a pancadas mas precisa manter a guarda alta no terceiro round.",
                baseColor = BlueprintOrange,
                reputationNeeded = 60
            ),
            IntelligentNpc(
                id = "npc_frontdesk",
                name = "Clara",
                role = "Recepcionista",
                emoji = "👩‍💻📞",
                personality = "Simpática, organizada, sabe tudo sobre convênios de planos, taxas de dojo e exames de fita de todos os atletas.",
                defaultGreeting = "Olá, campeão! Registro de entrada validado! Não esqueça de limpar seus pés no lava-pés sanitário antes do tatame, hein?",
                specialTechnique = "Dojo Membership Discount (Desconto em microtransações estéticas)",
                recommendedDojo = "Todos os Dojos credenciados do metaverso",
                initialQuest = "Assinar o termo de isenção de integridade física no terminal digital do lobby.",
                memoryFact = "Lembra que sua mensalidade vence em 3 dias e que você esqueceu a garrafinha de água no banco ontem.",
                baseColor = BlueprintTeal,
                reputationNeeded = 0
            ),
            IntelligentNpc(
                id = "npc_merchant",
                name = "Sr. Akira",
                role = "Comerciante",
                emoji = "🧔🏮",
                personality = "Focado em negócios, mas respeita quem tem honra. Negocia quimonos raros de fibra de carbono e sucos energéticos premium.",
                defaultGreeting = "Seja bem-vindo à minha vitrine de relíquias do tatame! Compre bem e role bem. Para você, faço preço de faixa marrom!",
                specialTechnique = "Weave Armor Patch (Reforço termoestável de mangas para evitar pegadas)",
                recommendedDojo = "Mercado Central da Baía",
                initialQuest = "Trazer 5 amostras de algodão egípcio cru dropados de treinos pesados na pedreira.",
                memoryFact = "Guarda estatísticas de que você costuma pechinchar e que adora patches holográficos vermelhos.",
                baseColor = Color(0xFFF472B6),
                reputationNeeded = 10
            ),
            IntelligentNpc(
                id = "npc_guide",
                name = "Kauã",
                role = "Guia Inicial",
                emoji = "🏄‍♂️🤙",
                personality = "Carioca local, surfista, super relaxado, ajuda novatos a se situarem nos hubs de praia e dojo do JiuVerse.",
                defaultGreeting = "Faaaaala irmão! Sem estresse na onda! Chegou agora na ilha virtual? Relaxa, vou te mostrar onde rola os melhores treinos de NoGi!",
                specialTechnique = "Beach Guard Escape (Fuga de quadril na areia molhada)",
                recommendedDojo = "Arpoador Beach Dojo (Open Air)",
                initialQuest = "Fazer um tour guiado de 3 minutos pelos 3 principais dojos municipais e praias.",
                memoryFact = "Sabe que você é recém-chegado com equipamento básico e necessita de moedas de patrocínio.",
                baseColor = Color(0xFF10B981),
                reputationNeeded = 0
            ),
            IntelligentNpc(
                id = "npc_eventos",
                name = "Renato",
                role = "Organizador de Eventos",
                emoji = "🎤🏆",
                personality = "Entusiasta de eSports, narrador profissional de artes marciais síncronas. Busca talentos para o GP e torneios sancionados.",
                defaultGreeting = "Atenção metaverso! Senhoras e senhores, as inscrições para o Grand Prix de Peso Absoluto estão abertas! Quem vai buscar a fivela dourada?",
                specialTechnique = "Championship Pressure (Aumento de foco em lutas sob transmissão de TV)",
                recommendedDojo = "Dojo das Estrelas (Estádio Olímpico)",
                initialQuest = "Inscrever-se na liga semanal do JiuVerse Arena e disputar ao menos 1 round qualificatório.",
                memoryFact = "Anotou que você perdeu sua última final por pontos no segundo final de round por falta de gás.",
                baseColor = Color(0xFFF59E0B),
                reputationNeeded = 100
            )
        )
    }

    // Dynamic state trackers
    var selectedNpcIndex by remember { mutableStateOf(0) }
    val currentNpc = npcsList[selectedNpcIndex]

    // Player states inside simulated memory container
    var playerSystemReputation by remember { mutableStateOf(65) }
    var interactivePlayerMessage by remember { mutableStateOf("") }
    
    // Live dialogue feed state
    val dialogueHistory = remember {
        mutableStateListOf<Pair<String, String>>(
            "Mestre" to "Oss! Escolha um dos 7 guias espirituais na barra superior para iniciar o diálogo neural."
        )
    }

    // Interactive message templates to speed up evaluation testing
    val questionTemplates = remember(selectedNpcIndex) {
        listOf(
            "Quais técnicas você pode me ensinar hoje?",
            "Como posso me preparar melhor para o torneio de JiuVerse?",
            "Você tem alguma missão disponível para meu nível?",
            "Recomende uma academia ideal para meu estilo de luta.",
            "Qual o segredo de sua respiração nos momentos de pressão?"
        )
    }

    // Active Simulation console
    var consoleSystemMessage by remember { mutableStateOf("Conexão neural síncrona com NPCs ativos estabelecida.") }

    // When NPC changes, reset dialog feed with greeting
    LaunchedEffect(selectedNpcIndex) {
        dialogueHistory.clear()
        dialogueHistory.add("Mestre" to currentNpc.defaultGreeting)
        consoleSystemMessage = "Contexto carregado: ${currentNpc.name} (${currentNpc.role}) pronto na escuta neural."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Arquitetura de Inteligência Artificial de NPCs e Oráculo do Dojo",
            subtitle = "Sistemas cognitivos baseados em Personalidade Própria, Memória Persistente do Jogador, Avaliação Dinâmica de Evolução e Reputação em Tempo Real."
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
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = BlueprintCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "SPEC DE IA AUTÔNOMA DO JIUVERSE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Nossa tecnologia conjuga APIs de Large Language Models (LLM) síncronas com uma camada local de 'Cognitive Player Registry'. Conforme você treina, o Mestre anota sua assiduidade e tipo de guarda, adaptando o vocabulário e liberando técnicas com exclusividade.",
                    fontSize = 10.5.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // =========================================================================
        // SECTION 1: NPC SELECTION DECK (DYNAMIC SELECTOR WITH REPUTATION CHIPS)
        // =========================================================================
        Text(
            text = "I. SELEÇÃO DE MESTRE OU GUIA RESIDENTE NO DOJO (7 NPCS CHAVES)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Reputacao status manual lever so developer can test locks
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("REPUTAÇÃO DO JOGADOR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                        Text("Sua Reputação: $playerSystemReputation RP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { playerSystemReputation = (playerSystemReputation - 10).coerceAtLeast(0) },
                            modifier = Modifier.size(28.dp).background(Color.Black, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Diminuir RP", tint = Color.Red, modifier = Modifier.size(14.dp))
                        }
                        IconButton(
                            onClick = { playerSystemReputation = (playerSystemReputation + 10).coerceAtMost(120) },
                            modifier = Modifier.size(28.dp).background(Color.Black, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar RP", tint = BlueprintTeal, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Horizontal Row of the 7 interactive NPCs with avatars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            npcsList.forEachIndexed { index, npc ->
                val isSelected = selectedNpcIndex == index
                val isLocked = playerSystemReputation < npc.reputationNeeded
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) npc.baseColor.copy(alpha = 0.25f)
                            else if (isLocked) Color.Red.copy(alpha = 0.05f)
                            else BlueprintCard
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) npc.baseColor else if (isLocked) Color.Red.copy(alpha = 0.4f) else BlueprintGridLine,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedNpcIndex = index }
                        .padding(vertical = 8.dp, horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(npc.emoji, fontSize = 18.sp)
                        Text(
                            text = npc.name.split(" ").last(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked) Color.Red else BlueprintTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isLocked) "🔒 Lvl ${npc.reputationNeeded}" else "✓ Liberado",
                            fontSize = 7.sp,
                            color = if (isLocked) Color.Red else BlueprintTeal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // =========================================================================
        // SECTION 2: INTERACTIVE COGNITIVE SIMULATOR CHAT BOX (80/20 GRAPHICAL VIEW)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Interactive Chat Window (Left Side - 65% space)
            Card(
                modifier = Modifier.weight(1.3f).fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B13)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    // Chat Header showing current interlocutor status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(currentNpc.baseColor.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, currentNpc.baseColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(currentNpc.emoji, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = currentNpc.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentNpc.baseColor
                                )
                                Text(
                                    text = "${currentNpc.role} • Personalidade Ativa",
                                    fontSize = 8.sp,
                                    color = BlueprintTextSecondary
                                )
                            }
                        }
                        
                        // Action: Lockout shield if player lack RP
                        if (playerSystemReputation < currentNpc.reputationNeeded) {
                            Badge(containerColor = Color.Red, contentColor = Color.White) {
                                Text("BLOQUEADO: REPUTAÇÃO INSUFICIENTE", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Badge(containerColor = BlueprintTeal.copy(alpha = 0.2f), contentColor = BlueprintTeal) {
                                Text("CONEXÃO ESTÁVEL", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = BlueprintGridLine, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Dialog feed pane
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        dialogueHistory.forEach { (sender, text) ->
                            val isPlayer = sender == "Você"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isPlayer) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isPlayer) BlueprintCyan.copy(alpha = 0.15f) else Color(0xFF131926)
                                    ),
                                    border = if (isPlayer) BorderStroke(0.5.dp, BlueprintCyan.copy(alpha = 0.3f)) else BorderStroke(0.5.dp, BlueprintGridLine)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Text(
                                            text = if (isPlayer) "Jogador (Você)" else currentNpc.name,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPlayer) BlueprintCyan else currentNpc.baseColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = text,
                                            fontSize = 9.sp,
                                            lineHeight = 12.sp,
                                            color = BlueprintTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Input Form
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = interactivePlayerMessage,
                            onValueChange = { interactivePlayerMessage = it },
                            placeholder = { Text("Faça uma pergunta do tatame...", fontSize = 9.sp, color = BlueprintTextSecondary) },
                            enabled = playerSystemReputation >= currentNpc.reputationNeeded,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black,
                                focusedBorderColor = currentNpc.baseColor,
                                unfocusedBorderColor = BlueprintGridLine,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.sp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = {
                                if (interactivePlayerMessage.isNotBlank()) {
                                    val userText = interactivePlayerMessage
                                    dialogueHistory.add("Você" to userText)
                                    interactivePlayerMessage = ""
                                    
                                    // Simulated Custom Cognitive AI Engine Logic
                                    val response = handleNpcAiResponseSimulation(
                                        npc = currentNpc,
                                        playerText = userText,
                                        rep = playerSystemReputation
                                    )
                                    dialogueHistory.add("Mestre" to response)
                                    consoleSystemMessage = "API LLM síncrona retornou dados com injeção de Reputação ($playerSystemReputation RP) e Memórias locais."
                                }
                            },
                            enabled = playerSystemReputation >= currentNpc.reputationNeeded && interactivePlayerMessage.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = currentNpc.baseColor, contentColor = Color.Black),
                            modifier = Modifier.height(38.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("ENVIAR", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Cognitive Registry State (Right Side - 35% space)
            Card(
                modifier = Modifier.weight(0.7f).fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(
                            text = "🧠 COGNITIVE PLAYER REGISTRY",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintOrange
                        )
                        Text(
                            text = "Memória injetada no System Prompt:",
                            fontSize = 7.sp,
                            color = BlueprintTextSecondary
                        )
                        
                        // Active Memory details of selected NPC
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlueprintOrange.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "💡 Memória do Atleta:\n\"${currentNpc.memoryFact}\"",
                                fontSize = 8.sp,
                                color = BlueprintOrange,
                                lineHeight = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🏆 SISTEMA DE EVOLUÇÃO & MISSÃO",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintTeal
                        )
                        Text(
                            text = "Missão ativa dada pelo instrutor:",
                            fontSize = 7.sp,
                            color = BlueprintTextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlueprintTeal.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "⚡ Missão:\n${currentNpc.initialQuest}",
                                fontSize = 8.sp,
                                color = BlueprintTeal,
                                lineHeight = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🎓 RECOMENDAÇÃO DE DOJO",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintCyan
                        )
                        Text(
                            text = currentNpc.recommendedDojo,
                            fontSize = 8.sp,
                            color = BlueprintTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Clickable Templates
                    Column {
                        Text(
                            text = "AÇÕES ESPECÍFICAS DE DESENVOLVEDOR (TEMPLATE PIPELINE):",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintTextSecondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Question template 1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable {
                                        interactivePlayerMessage = "Me ensine a técnica especial ${currentNpc.specialTechnique}."
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Aprender Técnica", fontSize = 7.sp, color = BlueprintCyan, textAlign = TextAlign.Center)
                            }

                            // Question template 2
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable {
                                        interactivePlayerMessage = "Qual a academia ideal que você recomenda para mim?"
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Recomendar Dojo", fontSize = 7.sp, color = BlueprintTeal, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Technical Telemetry logger box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(6.dp))
                .border(0.5.dp, BlueprintTeal, RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Computer,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LOG DO COGNITIVE AGENT CONTEXT: $consoleSystemMessage",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = BlueprintTeal
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // SECTION 3: SYSTEM SPEC ARCHITECTURES & CODE GENERATORS
        // =========================================================================
        Text(
            text = "III. BLUEPRINTS SÊNIOR: ARQUITETURA, BANCO DE DADOS E APIS DO SISTEMA DE IA",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BlueprintCyan,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 1. Database SQLite Schema
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("1. MODELAGEM DE BANCO DE DADOS DE MEMÓRIAS E REPUTAÇÃO (SQLITE/ROOM SCHEMA)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                val sqliteSchema = """
-- Tabela de relacionamento de Reputação e afinidade do Jogador com o NPC
CREATE TABLE npc_reputation_registry (
    player_id TEXT NOT NULL,
    npc_id TEXT NOT NULL,
    current_reputation_score INTEGER DEFAULT 0,
    npc_loyalty_tier TEXT DEFAULT 'NOVICE', -- 'NOVICE', 'RESPECTED', 'ELITE_DISCIPLE'
    last_interaction_timestamp INTEGER,
    completed_quests_count INTEGER DEFAULT 0,
    PRIMARY KEY (player_id, npc_id)
);

-- Tabela de fragmentos de Memória Cognitiva detectados em sparrings ou interações
CREATE TABLE npc_cognitive_memories (
    id TEXT PRIMARY KEY,
    player_id TEXT NOT NULL,
    npc_id TEXT NOT NULL,
    extracted_fact TEXT NOT NULL,          -- Ex: "Evitou a meia guarda passando direto"
    confidence_score REAL,                 -- Grau de certeza gerado pelo modelo LLM (0.0 a 1.0)
    is_active_context INTEGER DEFAULT 1,   -- Se deve ser injetado no System Prompt ativo
    created_at INTEGER
);

-- Tabela de Histórico de Conversa Síncrona compactado para economia de tokens
CREATE TABLE npc_chat_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id TEXT NOT NULL,
    npc_id TEXT NOT NULL,
    speaker_role TEXT NOT NULL,            -- 'user' ou 'assistant'
    message_payload TEXT NOT NULL,
    token_usage_count INTEGER,
    created_at INTEGER
);
                """.trimIndent()
                
                CodeBlock(code = sqliteSchema, title = "JiuVerse SQLite / Companion Database Schema Setup")
            }
        }

        // 2. OpenAI & Gemini Connection API Definition
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = BlueprintCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("2. IMPLEMENTAÇÃO DA API DE COMUNICAÇÃO REATIVA (OPENAI / GEMINI API)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                val kotlinCloudCode = """
package com.example.architecture.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@Serializable
data class OpenAiChatMessage(val role: String, val content: String)

@Serializable
data class OpenAiPromptRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<OpenAiChatMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 180
)

@Serializable
data class OpenAiPromptResponse(
    val choices: List<OpenAiChoice>
) {
    @Serializable
    data class OpenAiChoice(val message: OpenAiChatMessage)
}

// Interface Retrofit Síncrona para backend central do dojo
interface IntelligentNpcService {
    @POST("v1/chat/completions")
    suspend fun getStreamNpcChat(
        @Header("Authorization") apiKey: String,
        @Body request: OpenAiPromptRequest
    ): OpenAiPromptResponse
}
                """.trimIndent()

                CodeBlock(code = kotlinCloudCode, title = "IntelligentNpcService Retrofit & kotlinx.serialization Setup")
            }
        }

        // 3. System Prompt Generator Template
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SettingsSuggest, contentDescription = null, tint = BlueprintOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("3. GERADOR DE PROMPT SISTÉMICO COGNITIVO COM INJEÇÃO DE CONTEXTO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintOrange)
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                val promptTemplateCode = """
fun generateNpcSystemPrompt(npc: IntelligentNpc, playerRep: Int, dbMemories: List<String>): String {
    val memoryContext = dbMemories.joinToString("\n- ")
    return ""${"\""}
    Você é o NPC '${"$"}{npc.name}' no MMORPG síncrono 'JiuVerse'.
    Seu papel é '${"$"}{npc.role}'. Sua personalidade é descrita como: '${"$"}{npc.personality}'.
    
    A reputação do jogador atual com você é de '${"$"}{playerRep}' RP (Sendo 0 péssimo e 100 lendário).
    Se a reputação for menor que 30, responda com frieza ou exija comprometimento. Se for alta, seja generoso.
    
    Você possui a seguinte memória cognitiva extraída sobre o comportamento histórico do jogador:
    - '${"$"}{npc.memoryFact}'
    - Memórias adicionais salvas localmente:
    - ${"$"}{memoryContext}
    
    INSTRUÇÕES DE DIÁLOGO:
    1. Nunca saia do personagem. Nunca declare que você é um modelo de linguagem ou IA.
    2. Encoraje o jogador a praticar a técnica: '${"$"}{currentNpc.specialTechnique}'.
    3. Se solicitado, recomende a academia: '${"$"}{currentNpc.recommendedDojo}'.
    4. Mantenha as respostas curtas (máximo de 3 parágrafos) para caberem no HUD horizontal de 20% do jogo móvel.
    5. Termine com expressões ou saudações típicas de sua arte marcial.
    ""${"\""}.trimIndent()
}
                """.trimIndent()
                
                CodeBlock(code = promptTemplateCode, title = "Cognitive System Prompt Context Builder function")
            }
        }
    }
}

// Dynamic response logic to mock excellent AI generation in the demo dashboard
private fun handleNpcAiResponseSimulation(
    npc: IntelligentNpc,
    playerText: String,
    rep: Int
): String {
    val textLower = playerText.lowercase()
    
    val baseReply = when {
        textLower.contains("técnica") || textLower.contains("especial") || textLower.contains("aprender") -> {
            "Excelente escolha! Para dominar o '${npc.specialTechnique}', você precisará alinhar o centro de gravidade e simular a alavanca. Treine síncrono no dojo virtual hoje e repita a sequência!"
        }
        textLower.contains("academia") || textLower.contains("recomenda") || textLower.contains("onde treinar") || textLower.contains("dojo") -> {
            "Sem dúvidas, para o seu estilo de combate, eu indico intensamente o '${npc.recommendedDojo}'. É lá que os de alto nível no JiuVerse se encontram!"
        }
        textLower.contains("missão") || textLower.contains("tarefa") || textLower.contains("ajuda") -> {
            "Atualmente, seu objetivo comigo é: '${npc.initialQuest}'. Venha me relatar assim que concluir!"
        }
        textLower.contains("quem é você") || textLower.contains("sua história") || textLower.contains("papel") -> {
            "Eu sou ${npc.name}, atuando como ${npc.role}. Minha diretriz espiritual no metaverso é: ${npc.personality}"
        }
        textLower.contains("memória") || textLower.contains("lembra") -> {
            "Ah, eu tenho meu registro neural síncrono atualizado! Me lembro perfeitamente de que você: ${npc.memoryFact}"
        }
        else -> {
            "Suas palavras demonstram busca por evolução. Como seu ${npc.role}, afirmo que a dedicação diária destrava seu potencial pleno. Continue sua jornada horizontal! Oss!"
        }
    }
    
    // Prefix response variation depending on reputation score
    val repPrefix = when {
        rep < 30 -> "[Frio & Distante] Desconfio um pouco da sua dedicação no metaverso ainda... Treine mais. "
        rep > 90 -> "[Extremamente Favorável - Respeito Máximo] Ah, reverências ao nobre mestre do tatame! Meus olhos brilham ao ver seu nome. "
        else -> ""
    }
    
    return "$repPrefix$baseReply"
}
