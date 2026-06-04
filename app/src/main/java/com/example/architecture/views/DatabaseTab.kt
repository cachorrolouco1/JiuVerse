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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ui.theme.BlueprintTeal
import com.example.ui.theme.BlueprintTextPrimary
import com.example.ui.theme.BlueprintTextSecondary

@Composable
fun DatabaseTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val selectedModel = viewModel.selectedModel.collectAsState()
    val rawViewActive = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Modelagem de Dados",
            subtitle = "Estrutura do banco relacional de alta integridade (PostgreSQL + Prisma ORM)"
        )

        Text(
            text = "O JiuVerse utiliza PostgreSQL para garantir consistência em compras de itens e leilões, protegendo transações com isolamento estrito. Escolha uma entidade abaixo para explorar seus campos de dados ou copie o schema do Prisma correspondente:",
            fontSize = 12.sp,
            color = BlueprintTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Model Selector Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BlueprintData.prismaModels) { model ->
                val isSelected = selectedModel.value.name == model.name
                
                Card(
                    modifier = Modifier
                        .clickable { viewModel.selectModel(model) }
                        .testTag("db_model_tab_${model.name}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF132D46) else BlueprintCard
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        brush = CardDefaults.outlinedCardBorder().brush // keep default or override
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = if (isSelected) BlueprintCyan else BlueprintTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = model.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) BlueprintCyan else BlueprintTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Model Card Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Model: ${selectedModel.value.name}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintCyan
                        )
                        Text(
                            text = selectedModel.value.purpose,
                            fontSize = 12.sp,
                            color = BlueprintTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle between Schema/Field visualizer or Prisma script
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                        .padding(4.dp)
                ) {
                    // Visual table button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (!rawViewActive.value) BlueprintCard else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { rawViewActive.value = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = if (!rawViewActive.value) BlueprintCyan else BlueprintTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dicionário de Campos",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!rawViewActive.value) BlueprintTextPrimary else BlueprintTextSecondary
                            )
                        }
                    }

                    // Raw Prisma code button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (rawViewActive.value) BlueprintCard else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { rawViewActive.value = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = if (rawViewActive.value) BlueprintCyan else BlueprintTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Esboco Prisma Schema",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rawViewActive.value) BlueprintTextPrimary else BlueprintTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedVisibility(visible = !rawViewActive.value) {
                    // Fields list (Interactive Dictionary)
                    Column(
                        modifier = Modifier.fillMaxWidth().border(1.dp, BlueprintGridLine, RoundedCornerShape(6.dp))
                    ) {
                        // Header Row of fields
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BlueprintGridLine)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Campo",
                                modifier = Modifier.weight(0.3f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Tipo",
                                modifier = Modifier.weight(0.3f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Descrição Técnica",
                                modifier = Modifier.weight(0.4f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintTextPrimary
                            )
                        }

                        // Data list
                        selectedModel.value.fields.forEachIndexed { idx, field ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (idx % 2 == 0) Color(0xFF0F172A) else BlueprintCard)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = field.name + if (field.isId) " 🔑" else "",
                                    modifier = Modifier.weight(0.3f),
                                    fontSize = 12.sp,
                                    color = if (field.isId) BlueprintCyan else if (field.isRelation) BlueprintTeal else BlueprintTextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (field.isId) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = field.type,
                                    modifier = Modifier.weight(0.3f),
                                    fontSize = 11.sp,
                                    color = BlueprintTextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = field.description,
                                    modifier = Modifier.weight(0.4f),
                                    fontSize = 11.sp,
                                    color = BlueprintTextPrimary
                                )
                            }
                            if (idx < selectedModel.value.fields.size - 1) {
                                Divider(color = BlueprintGridLine, modifier = Modifier.height(1.dp))
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = rawViewActive.value) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CodeBlock(
                            code = selectedModel.value.rawCode,
                            title = "${selectedModel.value.name}.prisma",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Model Relations description
                Text(
                    text = "RELAÇÕES E DEPENDÊNCIAS:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintTeal,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                selectedModel.value.relations.forEach { rel ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(BlueprintTeal, RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rel,
                            fontSize = 12.sp,
                            color = BlueprintTextPrimary
                        )
                    }
                }
            }
        }
    }
}
