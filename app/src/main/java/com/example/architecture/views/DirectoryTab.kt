package com.example.architecture.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
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
import com.example.architecture.model.FolderItem
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.BlueprintCard
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintGridLine
import com.example.ui.theme.BlueprintTextPrimary
import com.example.ui.theme.BlueprintTextSecondary

@Composable
fun DirectoryTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val expandedFolders = viewModel.expandedFolders.collectAsState()
    val selectedFile = viewModel.selectedFile.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Estrutura do Projeto",
            subtitle = "Layout monorepo de pastas e templates prontos para o ecossistema do jogo"
        )
        
        Text(
            text = "Explore a estrutura de pastas do monorepo de JiuVerse. Toque em arquivos marcados por ícones para carregar códigos-fonte e explicações estruturais:",
            fontSize = 12.sp,
            color = BlueprintTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Render the tree recursively
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard.copy(alpha = 0.9f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                RenderFolderItem(
                    item = BlueprintData.folderTree,
                    path = "jiuverse-root",
                    depth = 0,
                    expanded = expandedFolders.value,
                    onToggleFolder = { viewModel.toggleFolder(it) },
                    onSelectFile = { viewModel.selectFile(it) },
                    selectedFile = selectedFile.value
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Document viewer for selected template code
        selectedFile.value?.let { file ->
            SectionHeader(
                title = "Visualizador técnico: ${file.name}",
                subtitle = "Implementação recomendada para arquitetura de MMO"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PROPÓSITO DO ARQUIVO:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = file.description,
                        fontSize = 13.sp,
                        color = BlueprintTextPrimary,
                        lineHeight = 18.sp
                    )

                    if (file.sampleCode.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        CodeBlock(
                            code = file.sampleCode,
                            title = file.name
                        )
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlueprintCard, RoundedCornerShape(8.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Selecione um arquivo de código acima para exibir o template técnico.",
                fontSize = 12.sp,
                color = BlueprintTextSecondary,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
fun RenderFolderItem(
    item: FolderItem,
    path: String,
    depth: Int,
    expanded: Set<String>,
    onToggleFolder: (String) -> Unit,
    onSelectFile: (FolderItem) -> Unit,
    selectedFile: FolderItem?
) {
    val isExpanded = expanded.contains(path)
    val isSelected = selectedFile?.name == item.name

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!item.isFile) {
                    onToggleFolder(path)
                } else {
                    onSelectFile(item)
                }
            }
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("file_tree_${item.name}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width((depth * 14).dp))

        Icon(
            imageVector = if (item.isFile) {
                Icons.Default.Description
            } else {
                if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder
            },
            contentDescription = null,
            tint = if (item.isFile) {
                if (isSelected) BlueprintCyan else BlueprintTextSecondary
            } else {
                BlueprintCyan
            },
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = item.name,
            fontSize = 13.sp,
            fontWeight = if (item.isFile) FontWeight.Normal else FontWeight.Bold,
            color = if (isSelected) BlueprintCyan else BlueprintTextPrimary,
            fontFamily = if (item.isFile) FontFamily.Monospace else FontFamily.SansSerif
        )
    }

    if (!item.isFile && isExpanded) {
        item.children.forEach { child ->
            val childPath = "$path/${child.name}"
            RenderFolderItem(
                item = child,
                path = childPath,
                depth = depth + 1,
                expanded = expanded,
                onToggleFolder = onToggleFolder,
                onSelectFile = onSelectFile,
                selectedFile = selectedFile
            )
        }
    }
}
