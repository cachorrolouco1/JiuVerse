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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.model.BlueprintData
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.BlueprintCard
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintGridLine
import com.example.ui.theme.BlueprintOrange
import com.example.ui.theme.BlueprintTeal
import com.example.ui.theme.BlueprintTextPrimary
import com.example.ui.theme.BlueprintTextSecondary

@Composable
fun RoadmapTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val selectedPhase = viewModel.selectedPhase.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Cronograma de Lançamento (Roadmap)",
            subtitle = "Sprints de arquitetura para deploy seguro de 6 meses"
        )

        Text(
            text = "Planejar o desenvolvimento de um MMORPG necessita de progresso estrito e modularidade para evitar desperdício de refatorações. Toque em qualquer fase do Gantt abaixo para obter tarefas de engenharia, tamanhos de equipe recomendados e controle de riscos técnicos:",
            fontSize = 12.sp,
            color = BlueprintTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Visual horizontal flow milestones (Gantt-like selection buttons)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BlueprintData.roadmapPhases.forEachIndexed { index, phase ->
                val isSelected = selectedPhase.value.id == phase.id
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectPhase(phase) }
                        .testTag("roadmap_phase_card_${phase.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF14293D) else BlueprintCard
                    ),
                    border = BorderStrokeCustom(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) BlueprintCyan else BlueprintGridLine
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Phase counter
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = if (isSelected) BlueprintCyan else BlueprintGridLine,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF0F172A) else BlueprintTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = phase.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BlueprintTextPrimary
                                )
                                Text(
                                    text = phase.subtitle.split(" — ")[0],
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = if (isSelected) BlueprintCyan else BlueprintTextSecondary,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Ver Detalhes",
                            tint = if (isSelected) BlueprintCyan else BlueprintGridLine,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Active Inspected Roadmap details ---
        SectionHeader(
            title = "Detalhamento da ${selectedPhase.value.title.split(":")[0]}",
            subtitle = "Informações minuciosas de execução técnica"
        )

        AnimatedVisibility(visible = true) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Resource and Timeline metadata row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = BlueprintTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Duração: ${selectedPhase.value.duration}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintTextPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = BlueprintCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Equipe: ${selectedPhase.value.team.split(" (")[0]}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintTextPrimary
                            )
                        }
                    }

                    // Task list section
                    Text(
                        text = "TAREFAS DE ENGENHARIA DE BACKEND/FRONTEND:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintCyan,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    selectedPhase.value.tasks.forEach { task ->
                        Row(
                            modifier = Modifier.padding(bottom = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "⚡ ",
                                color = BlueprintCyan,
                                fontSize = 11.sp
                            )
                            Text(
                                text = task,
                                fontSize = 12.sp,
                                color = BlueprintTextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Deliverables section
                    Text(
                        text = "ENTREGÁVEL/SPRINT MILESTONE:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintTeal,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    selectedPhase.value.deliverables.forEach { del ->
                        Row(
                            modifier = Modifier.padding(bottom = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "✔ ",
                                color = BlueprintTeal,
                                fontSize = 12.sp
                            )
                            Text(
                                text = del,
                                fontSize = 12.sp,
                                color = BlueprintTextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Risks & Mitigations section
                    Text(
                        text = "PLANEJAMENTO DE RISCOS DO ARQUITETO:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintOrange,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    selectedPhase.value.risks.forEach { risk ->
                        Row(
                            modifier = Modifier.padding(bottom = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = BlueprintOrange,
                                modifier = Modifier
                                    .padding(top = 2.dp, end = 4.dp)
                                    .size(12.dp)
                            )
                            Text(
                                text = risk,
                                fontSize = 12.sp,
                                color = BlueprintTextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
