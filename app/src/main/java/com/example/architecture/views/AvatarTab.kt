package com.example.architecture.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

data class CustomizableOption(
    val id: String,
    val name: String,
    val category: String, // "HAIR", "BEARD", "SKIN", "UNIFORM", "BELT", "ACCESSORY", "EMOTE", "PET"
    val rarity: String,   // "COMUM", "RARO", "ÉPICO", "LENDÁRIO", "MÍTICO"
    val rarityColor: Color,
    val hexValue: Color = Color.Transparent,
    val description: String = "",
    val unlockReq: String = "Imediato",
    var isUnlocked: Boolean = true
)

@Composable
fun AvatarTab(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // 1. Full database definitions of parts & slots
    val customizableItems = remember {
        listOf(
            // Skin
            CustomizableOption("s1", "Tom Amêndoa", "SKIN", "COMUM", BlueprintTeal, Color(0xFFE2B08D)),
            CustomizableOption("s2", "Tom Bronzeado", "SKIN", "COMUM", BlueprintTeal, Color(0xFFC68C5E)),
            CustomizableOption("s3", "Tom Ébano Raro", "SKIN", "RARO", BlueprintCyan, Color(0xFF50331E)),
            
            // Hair
            CustomizableOption("h1", "Topete Clássico (Preto)", "HAIR", "COMUM", BlueprintTeal, Color(0xFF1E1E1E)),
            CustomizableOption("h2", "Careca Reluzente", "HAIR", "COMUM", BlueprintTeal, Color(0x33A0A0A0)),
            CustomizableOption("h3", "Coque Samurai Dojo", "HAIR", "ÉPICO", Color(0xFFA855F7), Color(0xFF4A3E3D)),
            CustomizableOption("h4", "Super Saiyajin Aurum", "HAIR", "MÍTICO", Color(0xFFEF4444), Color(0xFFFACC15)),

            // Beard
            CustomizableOption("b0", "Sem Barba (Limpo)", "BEARD", "COMUM", BlueprintTeal, Color.Transparent),
            CustomizableOption("b1", "Barba Cheia Lenhador", "BEARD", "COMUM", BlueprintTeal, Color(0xFF3B2314)),
            CustomizableOption("b2", "Cavanhaque Old School", "BEARD", "COMUM", BlueprintTeal, Color(0xFF1F1F1F)),
            CustomizableOption("b3", "Barba de Mestre Longa Branca", "BEARD", "LENDÁRIO", BlueprintOrange, Color(0xFFE0E0E0)),

            // Uniforms (Kimono/Rashguards)
            CustomizableOption("u1", "Kimono Alliance Branco Clássico", "UNIFORM", "COMUM", BlueprintTeal, Color(0xFFE5E7EB)),
            CustomizableOption("u2", "Rashguard Samurai de Competição", "UNIFORM", "ÉPICO", Color(0xFFA855F7), Color(0xFF1E293B)),
            CustomizableOption("u3", "Kimono Armadura Negra de Ouro", "UNIFORM", "MÍTICO", Color(0xFFEF4444), Color(0xFF0F172A)),
            CustomizableOption("u4", "Kimono Azul Escuro Competidor", "UNIFORM", "COMUM", BlueprintTeal, Color(0xFF1E3A8A)),

            // Belts (Faixas)
            CustomizableOption("f1", "Faixa Branca Iniciante", "BELT", "COMUM", BlueprintTeal, Color(0xFFF3F4F6)),
            CustomizableOption("f2", "Faixa Azul Graduado", "BELT", "COMUM", BlueprintTeal, Color(0xFF2563EB)),
            CustomizableOption("f3", "Faixa Roxa Guardeiro", "BELT", "RARO", BlueprintCyan, Color(0xFF7C3AED)),
            CustomizableOption("f4", "Faixa Marrom Passador", "BELT", "ÉPICO", Color(0xFFA855F7), Color(0xFF78350F)),
            CustomizableOption("f5", "Faixa Preta Mapeada", "BELT", "LENDÁRIO", BlueprintOrange, Color(0xFF111827)),
            CustomizableOption("f6", "Faixa Coral de Grande Mestre", "BELT", "MÍTICO", Color(0xFFEF4444), Color(0xFFDC2626)),

            // Accessories
            CustomizableOption("a1", "Bandagem Esportiva de Dedos", "ACCESSORY", "COMUM", BlueprintTeal, Color(0xFFF3F4F6)),
            CustomizableOption("a2", "Protetor de Orelha de Silicone", "ACCESSORY", "RARO", BlueprintCyan, Color(0xFF06B6D4)),
            CustomizableOption("a3", "Óculos Escuros Estilo Rickson", "ACCESSORY", "LENDÁRIO", BlueprintOrange, Color(0xFF1E293B)),
            CustomizableOption("a4", "Luvas de Treino de Grappling", "ACCESSORY", "COMUM", BlueprintTeal, Color(0xFFEF4444), description = "Proteção esportiva de pulso/dedos de alta performance."),

            // Emotes
            CustomizableOption("e1", "Emote: Cumprimentar 'Oss!'", "EMOTE", "COMUM", BlueprintTeal, description = "Sinal de respeito sincero de kimono."),
            CustomizableOption("e2", "Emote: Desafio 'Vem pro Guard' ", "EMOTE", "RARO", BlueprintCyan, description = "Sentar no chão convidando a passagem."),
            CustomizableOption("e3", "Emote: Dança do Campeão Mundial", "EMOTE", "ÉPICO", Color(0xFFA855F7), description = "Comemoração vibrante típica de pódio."),

            // Pets
            CustomizableOption("p1", "Pitbull Guerreiro de Coleira", "PET", "LENDÁRIO", BlueprintOrange, description = "Fiel companheiro animado ao lado do tatame.", unlockReq = "Sorteio de Caixa Ouro"),
            CustomizableOption("p2", "Capivara de Faixa Preta", "PET", "MÍTICO", Color(0xFFEF4444), description = "Mascote secreto mitológico do cerrado brasileiro.", unlockReq = "Torneio Mundial GP Estelar")
        )
    }

    // 2. Active equipped items state
    var activeSkin by remember { mutableStateOf(customizableItems.first { it.id == "s1" }) }
    var activeHair by remember { mutableStateOf(customizableItems.first { it.id == "h1" }) }
    var activeBeard by remember { mutableStateOf(customizableItems.first { it.id == "b0" }) }
    var activeUniform by remember { mutableStateOf(customizableItems.first { it.id == "u4" }) }
    var activeBelt by remember { mutableStateOf(customizableItems.first { it.id == "f2" }) } 
    var activeAccessory by remember { mutableStateOf(customizableItems.first { it.id == "a4" }) }
    var activeEmote by remember { mutableStateOf(customizableItems.first { it.id == "e1" }) }
    var activePet by remember { mutableStateOf(customizableItems.first { it.id == "p2" }) } // Default Capivara

    // Dynamic animation triggers
    var currentAnimationState by remember { mutableStateOf("IDLE") } // "IDLE", "WALK", "RUN"
    var activeDirection by remember { mutableStateOf("FRENTE") } // "FRENTE", "COSTAS", "ESQUERDA", "DIREITA"
    val infiniteTransition = rememberInfiniteTransition()
    
    // Smooth idle scale simulator
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = { it }),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Walk frame oscillation
    val walkFrame by infiniteTransition.animateFloat(
        initialValue = -1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = { it }),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Current category filtering logic
    var selectedCategoryFilter by remember { mutableStateOf("SKIN") } // "SKIN", "HAIR", "BEARD", "UNIFORM", "BELT", "ACCESSORY", "EMOTE", "PET"

    Column(
        modifier = modifier
            .fillMaxSize()
            .drawBlueprintGrid()
            .padding(14.dp)
            .verticalScroll(scrollState)
    ) {
        SectionHeader(
            title = "Desenhos de Customização e Avatar",
            subtitle = "Sistemas de Customização Estética de Combatentes, Raridades e Progressão de Faixas"
        )

        // General Avatar Statement
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(0.5.dp, BlueprintCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Accessibility,
                contentDescription = null,
                tint = BlueprintTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VISÃO DIRETOR ARTÍSTICO (CHARACTER ART)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintTeal,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "No JiuVerse, o visual do lutador espelha seu peso e prestígio. Das faixas comuns (obtenção linear) aos pets míticos (itens raros de conquistas estelares), todas as camadas usam canais de desenho de baixo custo.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Grid Layout: Left is Avatar preview canvas, Right is Customizer UI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            
            // Left Half: Avatar Preview Engine (Interactive canvas simulation)
            Card(
                modifier = Modifier.weight(1.0f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("JiuVerse ORIGINAL CHIP PROTOTYPE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                    Text("Pixel Art 2.5D Isometric (Habbo Style)", fontSize = 8.sp, color = BlueprintTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Angle / Direction selector row (Frente, Costas, Lado esquerdo, Lado direito)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "FRENTE" to "FRENTE",
                            "COSTAS" to "COSTAS",
                            "ESQUERDA" to "ESQ (2.5D)",
                            "DIREITA" to "DIR (2.5D)"
                        ).forEach { (dir, label) ->
                            val isSelected = activeDirection == dir
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) BlueprintCyan.copy(alpha = 0.25f) else Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable { activeDirection = dir }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (isSelected) BlueprintCyan else BlueprintTextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Simulated 2D Isometric Character drawing canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(0.5.dp, BlueprintGridLine, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing circles and vectors inside Box
                        Canvas(
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.Center)
                        ) {
                            val centerWidth = size.width / 2
                            val centerHeight = size.height / 2
                            
                            // Smooth idle bounce or walk bounce
                            val bounceY = if (currentAnimationState == "RUN") {
                                kotlin.math.abs(walkFrame) * 8f - 4f
                            } else if (currentAnimationState == "WALK") {
                                kotlin.math.abs(walkFrame) * 4f - 2f
                            } else {
                                (breathScale - 1.0f) * 60f
                            }
                            
                            val bodyLeanX = if (currentAnimationState == "RUN") {
                                5f
                            } else {
                                0f
                            }

                            // Ground footprint shadow
                            drawOval(
                                color = Color(0x3F000000),
                                size = androidx.compose.ui.geometry.Size(90f, 20f),
                                topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 45f, centerHeight + 65f)
                            )

                            // Active customizer options mapped to palette colors dynamically
                            val colSkin = activeSkin.hexValue
                            val colHair = if (activeHair.id == "h2") Color.Transparent else activeHair.hexValue
                            val colUniform = activeUniform.hexValue
                            val colBelt = activeBelt.hexValue
                            val colBeard = activeBeard.hexValue

                            val colPreto = Color(0xFF1E293B)
                            val colBranco = Color(0xFFF8FAFC)
                            val colVermelho = Color(0xFFEF4444)
                            val colCinzaGrafite = Color(0xFF475569)

                            when (activeDirection) {
                                "FRENTE" -> {
                                    val headY = centerHeight - 35f + bounceY
                                    val headX = centerWidth + bodyLeanX

                                    // 1. HEAD (Slightly larger, Habbo style)
                                    drawRoundRect(
                                        color = colSkin,
                                        size = androidx.compose.ui.geometry.Size(62f, 62f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX - 31f, headY - 31f),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                                    )

                                    // 2. HAIR (Short black hair or dynamic styles)
                                    if (activeHair.id != "h2") {
                                        // Base hair caps
                                        drawRect(
                                            color = colHair,
                                            size = androidx.compose.ui.geometry.Size(64f, 20f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX - 32f, headY - 32f)
                                        )
                                        drawRect(
                                            color = colHair,
                                            size = androidx.compose.ui.geometry.Size(12f, 10f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX - 28f, headY - 12f)
                                        )
                                        drawRect(
                                            color = colHair,
                                            size = androidx.compose.ui.geometry.Size(12f, 10f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX + 16f, headY - 12f)
                                        )

                                        // Special Coque Samurai Dojo "h3"
                                        if (activeHair.id == "h3") {
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(20f, 20f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 10f, headY - 48f)
                                            )
                                        }

                                        // Special Super Saiyajin "h4" spikes
                                        if (activeHair.id == "h4") {
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(14f, 22f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 20f, headY - 46f)
                                            )
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(14f, 28f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 7f, headY - 52f)
                                            )
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(14f, 22f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX + 6f, headY - 46f)
                                            )
                                        }
                                    }

                                    // 3. EYES & FACE (Expressive minimalist eyes, cheek blush, smile)
                                    drawRect(
                                        color = Color.White,
                                        size = androidx.compose.ui.geometry.Size(12f, 12f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX - 18f, headY - 2f)
                                    )
                                    drawRect(
                                        color = Color.Black,
                                        size = androidx.compose.ui.geometry.Size(6f, 12f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX - 15f, headY - 2f)
                                    )

                                    drawRect(
                                        color = Color.White,
                                        size = androidx.compose.ui.geometry.Size(12f, 12f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX + 6f, headY - 2f)
                                    )
                                    drawRect(
                                        color = Color.Black,
                                        size = androidx.compose.ui.geometry.Size(6f, 12f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX + 9f, headY - 2f)
                                    )

                                    // Rose cheek blush
                                    drawCircle(
                                        color = Color(0xFFFDA4AF).copy(alpha = 0.8f),
                                        radius = 5f,
                                        center = androidx.compose.ui.geometry.Offset(headX - 22f, headY + 14f)
                                    )
                                    drawCircle(
                                        color = Color(0xFFFDA4AF).copy(alpha = 0.8f),
                                        radius = 5f,
                                        center = androidx.compose.ui.geometry.Offset(headX + 22f, headY + 14f)
                                    )

                                    // Friendly Smile
                                    drawRect(
                                        color = Color(0xFFE11D48),
                                        size = androidx.compose.ui.geometry.Size(12f, 3f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX - 6f, headY + 16f)
                                    )

                                    // Dynamic Beard Layer if equipped
                                    if (activeBeard.id != "b0") {
                                        drawRect(
                                            color = colBeard,
                                            size = androidx.compose.ui.geometry.Size(62f, 14f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX - 31f, headY + 17f)
                                        )
                                        drawRect(
                                            color = colBeard,
                                            size = androidx.compose.ui.geometry.Size(36f, 6f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX - 18f, headY + 11f)
                                        )
                                    }

                                    // 4. BODY / UNIFORM (Sport white rashguard/kimono jacket with black side stripes and red center crest badge)
                                    val bodyY = centerHeight + 25f + (bounceY * 0.4f)
                                    
                                    // Primary jacket color
                                    drawRect(
                                        color = colUniform,
                                        size = androidx.compose.ui.geometry.Size(80f, 40f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 40f, bodyY)
                                    )
                                    // Side panels (high contrast black compression trim)
                                    drawRect(
                                        color = colPreto,
                                        size = androidx.compose.ui.geometry.Size(12f, 40f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 40f, bodyY)
                                    )
                                    drawRect(
                                        color = colPreto,
                                        size = androidx.compose.ui.geometry.Size(12f, 40f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 28f, bodyY)
                                    )
                                    // Red training crest badge
                                    drawCircle(
                                        color = colVermelho,
                                        radius = 8f,
                                        center = androidx.compose.ui.geometry.Offset(centerWidth, bodyY + 18f)
                                    )

                                    // Dynamic Arms with Training Gloves (Glove colors are bound to accessories option)
                                    val leftArmSwingY = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") walkFrame * 8f else 0f
                                    val rtArmSwingY = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") -walkFrame * 8f else 0f
                                    val hasGloves = activeAccessory.id == "a4"
                                    val leftHandColor = if (hasGloves) activeAccessory.hexValue else if (activeAccessory.id == "a1") Color.White else colSkin
                                    val rightHandColor = if (hasGloves) activeAccessory.hexValue else if (activeAccessory.id == "a1") Color.White else colSkin

                                    // Left Arm / Sleeve
                                    drawRect(
                                        color = colUniform,
                                        size = androidx.compose.ui.geometry.Size(14f, 22f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 52f, bodyY + 4f + leftArmSwingY)
                                    )
                                    // Left Hand/Glove
                                    drawRect(
                                        color = leftHandColor,
                                        size = androidx.compose.ui.geometry.Size(14f, 14f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 52f, bodyY + 26f + leftArmSwingY)
                                    )

                                    // Right Arm / Sleeve
                                    drawRect(
                                        color = colUniform,
                                        size = androidx.compose.ui.geometry.Size(14f, 22f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 38f, bodyY + 4f + rtArmSwingY)
                                    )
                                    // Right Hand/Glove
                                    drawRect(
                                        color = rightHandColor,
                                        size = androidx.compose.ui.geometry.Size(14f, 14f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 38f, bodyY + 26f + rtArmSwingY)
                                    )

                                    // 5. BELT (Equipped belt rank) with rank bar stripes
                                    drawRect(
                                        color = colBelt,
                                        size = androidx.compose.ui.geometry.Size(84f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 42f, bodyY + 32f)
                                    )
                                    // The black sleeve tag on the belt
                                    drawRect(
                                        color = Color.Black,
                                        size = androidx.compose.ui.geometry.Size(22f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 10f, bodyY + 32f)
                                    )
                                    // White tips/degrees on the sleeve
                                    drawRect(
                                        color = Color.White,
                                        size = androidx.compose.ui.geometry.Size(4f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 14f, bodyY + 32f)
                                    )

                                    // 6. LEGS & FEET
                                    val leftLegOffset = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") walkFrame * 12f else 0f
                                    val rightLegOffset = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") -walkFrame * 12f else 0f

                                    // Pants color matching the uniform style
                                    drawRect(
                                        color = colCinzaGrafite,
                                        size = androidx.compose.ui.geometry.Size(25f, 20f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 30f + leftLegOffset, bodyY + 40f)
                                    )
                                    drawRect(
                                        color = colCinzaGrafite,
                                        size = androidx.compose.ui.geometry.Size(25f, 20f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 5f + rightLegOffset, bodyY + 40f)
                                    )
                                    // Bare feet skin matches skin tone selection perfectly
                                    drawRect(
                                        color = colSkin,
                                        size = androidx.compose.ui.geometry.Size(14f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 25f + leftLegOffset, bodyY + 58f)
                                    )
                                    drawRect(
                                        color = colSkin,
                                        size = androidx.compose.ui.geometry.Size(14f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 11f + rightLegOffset, bodyY + 58f)
                                    )
                                }
                                "COSTAS" -> {
                                    val headY = centerHeight - 35f + bounceY
                                    val headX = centerWidth - bodyLeanX

                                    // 1. HEAD BACK (Full hair or bare skin if bald)
                                    drawRoundRect(
                                        color = if (activeHair.id == "h2") colSkin else colHair,
                                        size = androidx.compose.ui.geometry.Size(62f, 62f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX - 31f, headY - 31f),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                                    )

                                    if (activeHair.id != "h2") {
                                        // Back headband or shadow detail
                                        drawRect(
                                            color = colCinzaGrafite.copy(alpha = 0.4f),
                                            size = androidx.compose.ui.geometry.Size(40f, 6f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX - 20f, headY - 26f)
                                        )

                                        // Special Coque Samurai Dojo "h3"
                                        if (activeHair.id == "h3") {
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(20f, 20f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 10f, headY - 48f)
                                            )
                                        }

                                        // Special Super Saiyajin "h4" spikes
                                        if (activeHair.id == "h4") {
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(14f, 22f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 20f, headY - 46f)
                                            )
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(14f, 28f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 7f, headY - 52f)
                                            )
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(14f, 22f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX + 6f, headY - 46f)
                                            )
                                        }
                                    }

                                    // 2. BACK UNIFORM (White/selected with full Red/Black JiuVerse logo print)
                                    val bodyY = centerHeight + 25f + (bounceY * 0.4f)
                                    
                                    drawRect(
                                        color = colUniform,
                                        size = androidx.compose.ui.geometry.Size(80f, 40f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 40f, bodyY)
                                    )
                                    // Giant Elite Back Mark logo (Red/Black)
                                    // Dynamic Back Arms with Training Gloves
                                    val leftArmSwingY = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") walkFrame * 8f else 0f
                                    val rtArmSwingY = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") -walkFrame * 8f else 0f
                                    val hasGloves = activeAccessory.id == "a4"
                                    val leftHandColor = if (hasGloves) activeAccessory.hexValue else if (activeAccessory.id == "a1") Color.White else colSkin
                                    val rightHandColor = if (hasGloves) activeAccessory.hexValue else if (activeAccessory.id == "a1") Color.White else colSkin

                                    // Left Arm / Sleeve (Back)
                                    drawRect(
                                        color = colUniform,
                                        size = androidx.compose.ui.geometry.Size(14f, 22f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 52f, bodyY + 4f + leftArmSwingY)
                                    )
                                    drawRect(
                                        color = leftHandColor,
                                        size = androidx.compose.ui.geometry.Size(14f, 14f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 52f, bodyY + 26f + leftArmSwingY)
                                    )

                                    // Right Arm / Sleeve (Back)
                                    drawRect(
                                        color = colUniform,
                                        size = androidx.compose.ui.geometry.Size(14f, 22f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 38f, bodyY + 4f + rtArmSwingY)
                                    )
                                    drawRect(
                                        color = rightHandColor,
                                        size = androidx.compose.ui.geometry.Size(14f, 14f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 38f, bodyY + 26f + rtArmSwingY)
                                    )
                                    drawRect(
                                        color = colVermelho,
                                        size = androidx.compose.ui.geometry.Size(44f, 24f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 22f, bodyY + 6f)
                                    )
                                    drawRect(
                                        color = colPreto,
                                        size = androidx.compose.ui.geometry.Size(30f, 14f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 15f, bodyY + 11f)
                                    )

                                    // 3. BELT BACK (With back side knot)
                                    drawRect(
                                        color = colBelt,
                                        size = androidx.compose.ui.geometry.Size(84f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 42f, bodyY + 32f)
                                    )
                                    // Knot tails hanging down in matching belt rank color
                                    drawRect(
                                        color = colBelt,
                                        size = androidx.compose.ui.geometry.Size(8f, 24f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 8f, bodyY + 32f)
                                    )
                                    drawRect(
                                        color = colBelt,
                                        size = androidx.compose.ui.geometry.Size(8f, 18f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 2f, bodyY + 32f)
                                    )

                                    // 4. LEGS (Costas view)
                                    val leftLegOffset = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") walkFrame * 12f else 0f
                                    val rightLegOffset = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") -walkFrame * 12f else 0f

                                    drawRect(
                                        color = colCinzaGrafite,
                                        size = androidx.compose.ui.geometry.Size(25f, 20f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 30f + leftLegOffset, bodyY + 40f)
                                    )
                                    drawRect(
                                        color = colCinzaGrafite,
                                        size = androidx.compose.ui.geometry.Size(25f, 20f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 5f + rightLegOffset, bodyY + 40f)
                                    )

                                    drawRect(
                                        color = colSkin,
                                        size = androidx.compose.ui.geometry.Size(14f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 25f + leftLegOffset, bodyY + 58f)
                                    )
                                    drawRect(
                                        color = colSkin,
                                        size = androidx.compose.ui.geometry.Size(14f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 11f + rightLegOffset, bodyY + 58f)
                                    )
                                }
                                "ESQUERDA" -> {
                                    val headY = centerHeight - 35f + bounceY
                                    val headX = centerWidth - bodyLeanX

                                    // 1. HEAD PROFILE (Facing left)
                                    drawRoundRect(
                                        color = colSkin,
                                        size = androidx.compose.ui.geometry.Size(54f, 62f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX - 27f, headY - 31f),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                                    )
                                    // Hair left side profile
                                    if (activeHair.id != "h2") {
                                        drawRect(
                                            color = colHair,
                                            size = androidx.compose.ui.geometry.Size(36f, 62f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX - 27f, headY - 32f)
                                        )
                                        drawRect(
                                            color = colHair,
                                            size = androidx.compose.ui.geometry.Size(56f, 20f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX - 28f, headY - 32f)
                                        )

                                        // Special Coque Samurai Dojo "h3"
                                        if (activeHair.id == "h3") {
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(20f, 20f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 18f, headY - 48f)
                                            )
                                        }

                                        // Special Super Saiyajin "h4" spikes
                                        if (activeHair.id == "h4") {
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(12f, 24f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 15f, headY - 48f)
                                            )
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(12f, 28f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 3f, headY - 52f)
                                            )
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(12f, 20f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX + 9f, headY - 44f)
                                            )
                                        }
                                    }

                                    // Left Eye profile
                                    drawRect(
                                        color = Color.White,
                                        size = androidx.compose.ui.geometry.Size(10f, 12f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX + 10f, headY - 2f)
                                    )
                                    drawRect(
                                        color = Color.Black,
                                        size = androidx.compose.ui.geometry.Size(5f, 12f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX + 10f, headY - 2f)
                                    )

                                    // Profile mouth
                                    drawRect(
                                        color = Color(0xFFE11D48),
                                        size = androidx.compose.ui.geometry.Size(5f, 3f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX + 15f, headY + 16f)
                                    )

                                    // Beard profile if equipped
                                    if (activeBeard.id != "b0") {
                                        drawRect(
                                            color = colBeard,
                                            size = androidx.compose.ui.geometry.Size(16f, 14f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX + 11f, headY + 17f)
                                        )
                                        drawRect(
                                            color = colBeard,
                                            size = androidx.compose.ui.geometry.Size(12f, 6f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX + 15f, headY + 11f)
                                        )
                                    }

                                    // 2. SHIRT SLEEVE PROFILE (With red badge bar)
                                    val bodyY = centerHeight + 25f + (bounceY * 0.4f)
                                    
                                    // Dynamic Left Profile Arm with Training Gloves
                                    val leftArmSwingXS = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") walkFrame * 10f else 0f
                                    val leftArmSwingYS = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") kotlin.math.abs(walkFrame) * 4f else 0f
                                    val hasGloves = activeAccessory.id == "a4"
                                    val leftHandColor = if (hasGloves) activeAccessory.hexValue else if (activeAccessory.id == "a1") Color.White else colSkin

                                    drawRect(
                                        color = colUniform,
                                        size = androidx.compose.ui.geometry.Size(50f, 40f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 25f, bodyY)
                                    )

                                    // Draw Arm on profile body
                                    drawRect(
                                        color = colUniform,
                                        size = androidx.compose.ui.geometry.Size(16f, 24f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 8f + leftArmSwingXS, bodyY + 12f + leftArmSwingYS)
                                    )
                                    // Draw Hand/Glove on profile body
                                    drawRect(
                                        color = leftHandColor,
                                        size = androidx.compose.ui.geometry.Size(16f, 12f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 8f + leftArmSwingXS, bodyY + 36f + leftArmSwingYS)
                                    )
                                    drawRect(
                                        color = colVermelho,
                                        size = androidx.compose.ui.geometry.Size(10f, 30f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 8f, bodyY + 4f)
                                    )
                                    drawRect(
                                        color = colPreto,
                                        size = androidx.compose.ui.geometry.Size(10f, 40f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 23f, bodyY)
                                    )

                                    // 3. WHITE BELT (Profile side)
                                    drawRect(
                                        color = colBelt,
                                        size = androidx.compose.ui.geometry.Size(54f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 27f, bodyY + 32f)
                                    )

                                    // 4. LEGS (Profile movement swinging)
                                    val legSwing = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") walkFrame * 14f else 0f
                                    
                                    drawRect(
                                        color = colCinzaGrafite,
                                        size = androidx.compose.ui.geometry.Size(26f, 20f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 13f + legSwing, bodyY + 40f)
                                    )
                                    drawRect(
                                        color = colSkin,
                                        size = androidx.compose.ui.geometry.Size(16f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 8f + legSwing, bodyY + 58f)
                                    )
                                }
                                "DIREITA" -> {
                                    val headY = centerHeight - 35f + bounceY
                                    val headX = centerWidth + bodyLeanX

                                    // 1. HEAD PROFILE (Facing right)
                                    drawRoundRect(
                                        color = colSkin,
                                        size = androidx.compose.ui.geometry.Size(54f, 62f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX - 27f, headY - 31f),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                                    )
                                    // Hair right side profile
                                    if (activeHair.id != "h2") {
                                        drawRect(
                                            color = colHair,
                                            size = androidx.compose.ui.geometry.Size(36f, 62f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX - 9f, headY - 32f)
                                        )
                                        drawRect(
                                            color = colHair,
                                            size = androidx.compose.ui.geometry.Size(56f, 20f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX - 28f, headY - 32f)
                                        )

                                        // Special Coque Samurai Dojo "h3"
                                        if (activeHair.id == "h3") {
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(20f, 20f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 2f, headY - 48f)
                                            )
                                        }

                                        // Special Super Saiyajin "h4" spikes
                                        if (activeHair.id == "h4") {
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(12f, 20f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 21f, headY - 44f)
                                            )
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(12f, 28f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX - 9f, headY - 52f)
                                            )
                                            drawRect(
                                                color = colHair,
                                                size = androidx.compose.ui.geometry.Size(12f, 24f),
                                                topLeft = androidx.compose.ui.geometry.Offset(headX + 3f, headY - 48f)
                                            )
                                        }
                                    }

                                    // Right Eye Profile
                                    drawRect(
                                        color = Color.White,
                                        size = androidx.compose.ui.geometry.Size(10f, 12f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX - 20f, headY - 2f)
                                    )
                                    drawRect(
                                        color = Color.Black,
                                        size = androidx.compose.ui.geometry.Size(5f, 12f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX - 15f, headY - 2f)
                                    )

                                    // Profile mouth
                                    drawRect(
                                        color = Color(0xFFE11D48),
                                        size = androidx.compose.ui.geometry.Size(5f, 3f),
                                        topLeft = androidx.compose.ui.geometry.Offset(headX - 20f, headY + 16f)
                                    )

                                    // Beard profile if equipped
                                    if (activeBeard.id != "b0") {
                                        drawRect(
                                            color = colBeard,
                                            size = androidx.compose.ui.geometry.Size(16f, 14f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX - 27f, headY + 17f)
                                        )
                                        drawRect(
                                            color = colBeard,
                                            size = androidx.compose.ui.geometry.Size(12f, 6f),
                                            topLeft = androidx.compose.ui.geometry.Offset(headX - 27f, headY + 11f)
                                        )
                                    }

                                    // 2. SHIRT SLEEVE PROFILE RIGHT
                                    val bodyY = centerHeight + 25f + (bounceY * 0.4f)
                                    
                                    // Dynamic Right Profile Arm with Training Gloves
                                    val rightArmSwingXS = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") -walkFrame * 10f else 0f
                                    val rightArmSwingYS = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") kotlin.math.abs(walkFrame) * 4f else 0f
                                    val hasGloves = activeAccessory.id == "a4"
                                    val rightHandColor = if (hasGloves) activeAccessory.hexValue else if (activeAccessory.id == "a1") Color.White else colSkin

                                    drawRect(
                                        color = colUniform,
                                        size = androidx.compose.ui.geometry.Size(50f, 40f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 25f, bodyY)
                                    )

                                    // Draw Arm on profile body (Right)
                                    drawRect(
                                        color = colUniform,
                                        size = androidx.compose.ui.geometry.Size(16f, 24f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 8f + rightArmSwingXS, bodyY + 12f + rightArmSwingYS)
                                    )
                                    // Draw Hand/Glove on profile body (Right)
                                    drawRect(
                                        color = rightHandColor,
                                        size = androidx.compose.ui.geometry.Size(16f, 12f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 8f + rightArmSwingXS, bodyY + 36f + rightArmSwingYS)
                                    )
                                    drawRect(
                                        color = colVermelho,
                                        size = androidx.compose.ui.geometry.Size(10f, 30f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 2f, bodyY + 4f)
                                    )
                                    drawRect(
                                        color = colPreto,
                                        size = androidx.compose.ui.geometry.Size(10f, 40f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth + 13f, bodyY)
                                    )

                                    // 3. BELT PROFILE SIDE RIGHT
                                    drawRect(
                                        color = colBelt,
                                        size = androidx.compose.ui.geometry.Size(54f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 27f, bodyY + 32f)
                                    )

                                    // 4. LEGS RIGHT SWING
                                    val legSwing = if (currentAnimationState == "WALK" || currentAnimationState == "RUN") -walkFrame * 14f else 0f
                                    
                                    drawRect(
                                        color = colCinzaGrafite,
                                        size = androidx.compose.ui.geometry.Size(26f, 20f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 13f + legSwing, bodyY + 40f)
                                    )
                                    drawRect(
                                        color = colSkin,
                                        size = androidx.compose.ui.geometry.Size(16f, 10f),
                                        topLeft = androidx.compose.ui.geometry.Offset(centerWidth - 8f + legSwing, bodyY + 58f)
                                    )
                                }
                            }

                            // Outline border ring mapping
                            drawCircle(
                                color = BlueprintCyan.copy(alpha = 0.25f),
                                radius = 95f + (bounceY * 0.2f),
                                style = Stroke(width = 1f),
                                center = androidx.compose.ui.geometry.Offset(centerWidth, centerHeight + 10f)
                            )
                        }

                        // Overlay indicator of animation state
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, BlueprintCyan, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MODO: ${currentAnimationState} | DST: ${activeDirection}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueprintCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Play animation testing row
                    Text("CONTROLADOR DE MOVIMENTOS (ANIMAÇÕES):", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BlueprintTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "IDLE" to "IDLE (RESPIRA)",
                            "WALK" to "WALK (15 FPS)",
                            "RUN" to "RUN (SQUARE)"
                        ).forEach { (anim, label) ->
                            val isSelected = currentAnimationState == anim
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) BlueprintTeal.copy(alpha = 0.25f) else Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, if (isSelected) BlueprintTeal else BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable { currentAnimationState = anim }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (isSelected) BlueprintTeal else BlueprintTextSecondary)
                            }
                        }
                    }

                    // Equiped HUD cards
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                        border = BorderStroke_workaround(BlueprintGridLine)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("ESPECIFICAÇÕES DO AVATAR ORIGINAL:", fontSize = 8.sp, color = BlueprintCyan, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("• Uniforme: White Body Rashguard, Black Compression, Red Badge", fontSize = 7.5.sp, color = BlueprintTextPrimary)
                            Text("• Orientação: Isometric 4-Way Seamless 2.5D Social Virtual Base", fontSize = 7.5.sp, color = BlueprintTextPrimary)
                            Text("• Palette Hex: Black (#0C0A09), White (#F8FAFC), Red (#EF4444), Graphite (#475569)", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                        }
                    }
                }
            }

            // Right Half: Select parts categories and apply cosmetic
            Card(
                modifier = Modifier.weight(1.1f),
                colors = CardDefaults.cardColors(containerColor = BlueprintCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {

                    // Horizontal Categorized Tabs
                    Text("SELECIONE A CATEGORIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BlueprintCyan)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "SKIN" to "Pele",
                            "HAIR" to "Cabelo",
                            "BEARD" to "Barba",
                            "UNIFORM" to "Quim"
                        ).forEach { (category, label) ->
                            val isSelected = selectedCategoryFilter == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) BlueprintCyan.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable { selectedCategoryFilter = category }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) BlueprintCyan else BlueprintTextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "BELT" to "Faixa",
                            "ACCESSORY" to "Acess",
                            "EMOTE" to "Emote",
                            "PET" to "Pets"
                        ).forEach { (category, label) ->
                            val isSelected = selectedCategoryFilter == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) BlueprintCyan.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, if (isSelected) BlueprintCyan else BlueprintGridLine, RoundedCornerShape(4.dp))
                                    .clickable { selectedCategoryFilter = category }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) BlueprintCyan else BlueprintTextSecondary)
                            }
                        }
                    }

                    Divider(color = BlueprintGridLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    // Options Grid items selection based on Category
                    Text("ITENS DISPONÍVEIS:", fontSize = 9.sp, color = BlueprintTextSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier.height(180.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filteredOptions = customizableItems.filter { it.category == selectedCategoryFilter }

                        filteredOptions.forEach { item ->
                            val isEquipped = when(item.category) {
                                "SKIN" -> activeSkin.id == item.id
                                "HAIR" -> activeHair.id == item.id
                                "BEARD" -> activeBeard.id == item.id
                                "UNIFORM" -> activeUniform.id == item.id
                                "BELT" -> activeBelt.id == item.id
                                "ACCESSORY" -> activeAccessory.id == item.id
                                "EMOTE" -> activeEmote.id == item.id
                                "PET" -> activePet.id == item.id
                                else -> false
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isEquipped) BlueprintTeal.copy(alpha = 0.15f) else Color(0xFF0F172A),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        0.5.dp,
                                        if (isEquipped) BlueprintTeal else BlueprintGridLine,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        // Equip item
                                        when (item.category) {
                                            "SKIN" -> activeSkin = item
                                            "HAIR" -> activeHair = item
                                            "BEARD" -> activeBeard = item
                                            "UNIFORM" -> activeUniform = item
                                            "BELT" -> activeBelt = item
                                            "ACCESSORY" -> activeAccessory = item
                                            "EMOTE" -> activeEmote = item
                                            "PET" -> activePet = item
                                        }
                                    }
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Colored dot representing the asset
                                    if (item.hexValue != Color.Transparent) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(item.hexValue, CircleShape)
                                                .border(0.5.dp, Color.White, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    
                                    Column {
                                        Text(item.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueprintTextPrimary)
                                        if (item.description.isNotEmpty()) {
                                            Text(item.description, fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                        } else {
                                            Text("Desbloqueio: ${item.unlockReq}", fontSize = 7.5.sp, color = BlueprintTextSecondary)
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Rarity label block
                                    Box(
                                        modifier = Modifier
                                            .background(item.rarityColor.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(item.rarity, fontSize = 7.sp, color = item.rarityColor, fontWeight = FontWeight.Black)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))

                                    if (isEquipped) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Visual progression hierarchy stats (White Belt to Grand Master Belt)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2. HIERARQUIA DE PROGRESSÃO ESTÉTICA DO LUTADOR (TIERS)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintCyan
            )
            Icon(imageVector = Icons.Default.Style, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueprintCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "A jornada visual do JiuVerse possui 5 níveis (tiers) distintos de raridade das customizações. Colecionáveis mais raros adicionam multiplicadores de Prestígio e impulsionam o seu dojo no ranking.",
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                // Five levels cards
                val visualTiers = listOf(
                    Triple("COMUM (White/Blue)", BlueprintTeal, "Itens iniciais com drop simples e cores padrão de tatames (Alliance/Gracie)."),
                    Triple("RARO (Purple)", BlueprintCyan, "Disponíveis via conquistas regionais, mudam padrões de Rashguards e faixas intermediárias."),
                    Triple("ÉPICO (Brown)", Color(0xFFA855F7), "Uniformes customizados de marcas licenciadas e fardas estilizadas de dojo."),
                    Triple("LENDÁRIO (Black Belt)", BlueprintOrange, "Cabelos especiais iluminados e mascote Pitbull Guerreiro de alta atração."),
                    Triple("MÍTICO (Coral)", Color(0xFFEF4444), "Faixas Corais de Grande Mestre e a Capivara de Faixa Preta do Cerrado.")
                )

                visualTiers.forEach { (title, color, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(color.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Black, color = color)
                            Text(desc, fontSize = 9.sp, color = BlueprintTextPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Tech details / Sprite compilation structure
        Text(
            text = "3. COMPILADOR DE TEXTURAS & SPRITESHEETS (VISÃO COMPILADOR)",
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = BlueprintTeal, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MOTOR DE COMPREENSÃO DE SPRITES ISOMÉTIRCOS 2.5D", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueprintTeal)
                }
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "• Geração Dinâmica de Mesh: As texturas do kimono carregado (Alliance/Kimonos clássicos) montam-se em cima do corpo base do lutador usando uma única chamada de empate (Draw Call) síncrona na GPU.\n" +
                           "• Sprite-sheet dinâmico: Peças como o pet (Capivara) e emotes são carregados em canal alpha com renderizador de quadros de 15fps, garantindo leveza total e mantendo a taxa de quadros (60fps) estável para celulares de entrada.",
                    fontSize = 10.sp,
                    color = BlueprintTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Workaround Border Stroke function
private fun BorderStroke_workaround(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(0.5.dp, color)
}
