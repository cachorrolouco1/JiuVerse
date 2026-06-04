package com.example.architecture.views

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.ui.theme.BlueprintCard
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintGridLine
import com.example.ui.theme.BlueprintTeal
import com.example.ui.theme.BlueprintTextPrimary
import com.example.ui.theme.BlueprintTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val messages = viewModel.chatMessages.collectAsState()
    val isLoading = viewModel.isChatLoading.collectAsState()
    val inputText = remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Auto-scroll to bottom of chat when messages size changes
    LaunchedEffect(messages.value.size) {
        if (messages.value.isNotEmpty()) {
            listState.animateScrollToItem(messages.value.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
    ) {
        SectionHeader(
            title = "Consultoria de IA: Arquiteto Sênior",
            subtitle = "Sane dúvidas estruturais e solicite templates de código ao vivo"
        )

        // Chat messages box list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp))
                .background(BlueprintCard.copy(alpha = 0.85f))
                .padding(8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages.value) { msg ->
                    val isAi = msg.sender == "ai"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .testTag("chat_message_${msg.sender}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAi) Color(0xFF142035) else Color(0xFF132F33)
                            ),
                            border = BorderStrokeCustom(
                                width = 1.dp,
                                color = if (isAi) BlueprintCyan.copy(alpha = 0.3f) else BlueprintTeal.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.SupportAgent,
                                            contentDescription = null,
                                            tint = if (isAi) BlueprintCyan else BlueprintTeal,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isAi) "Arquiteto Sênior (IA)" else "Você (Engenheiro)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAi) BlueprintCyan else BlueprintTeal
                                        )
                                    }
                                    Text(
                                        text = msg.timestamp,
                                        fontSize = 10.sp,
                                        color = BlueprintTextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = msg.content,
                                    fontSize = 12.sp,
                                    color = BlueprintTextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // Temporary loading message
                if (isLoading.value) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(0.5f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStrokeCustom(1.dp, BlueprintGridLine)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = BlueprintCyan
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Processando...",
                                        fontSize = 11.sp,
                                        color = BlueprintTextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Predefined quick questions list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val quickQuestions = listOf(
                "Como WebRTC escala?" to "Como WebRTC escala para áudio por proximidade no dojo?",
                "Sincronia no Redis" to "Como usar canais de Redis PubSub para sincronizar posições Socket.IO?",
                "Duplicacao no Market" to "Como travar exploits de duplicação de itens no Marketplace?"
            )

            quickQuestions.forEach { (label, rawPrompt) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF0F172A), RoundedCornerShape(14.dp))
                        .border(1.dp, BlueprintGridLine, RoundedCornerShape(14.dp))
                        .clickable(enabled = !isLoading.value) {
                            viewModel.askArchitectAI(rawPrompt)
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("quick_question_$label"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input textbox with Send icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText.value,
                onValueChange = { inputText.value = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text"),
                placeholder = { Text("Peça desenhos técnicos ou templates de código...", fontSize = 12.sp, color = BlueprintTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BlueprintTextPrimary,
                    unfocusedTextColor = BlueprintTextPrimary,
                    cursorColor = BlueprintCyan,
                    focusedBorderColor = BlueprintCyan,
                    unfocusedBorderColor = BlueprintGridLine,
                    focusedContainerColor = BlueprintCard,
                    unfocusedContainerColor = BlueprintCard
                ),
                maxLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.value.trim().isNotEmpty()) {
                        viewModel.askArchitectAI(inputText.value)
                        inputText.value = ""
                        focusManager.clearFocus()
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.value.trim().isNotEmpty()) {
                        viewModel.askArchitectAI(inputText.value)
                        inputText.value = ""
                        focusManager.clearFocus()
                    }
                },
                enabled = !isLoading.value && inputText.value.trim().isNotEmpty(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (inputText.value.trim().isNotEmpty() && !isLoading.value) BlueprintTeal else BlueprintGridLine,
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = BlueprintTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
