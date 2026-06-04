package com.example.architecture.views

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlueprintCard
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintGridLine
import com.example.ui.theme.BlueprintTextPrimary
import com.example.ui.theme.BlueprintTextSecondary

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(BlueprintCyan, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BlueprintTextPrimary,
                fontFamily = FontFamily.SansSerif
            )
        }
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = BlueprintTextSecondary,
            modifier = Modifier.padding(start = 14.dp, top = 2.dp)
        )
    }
}

@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    val context = LocalContext.current
    val hScroll = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BlueprintGridLine, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = BlueprintCard),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Title Header of Code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title ?: "Código Fonte",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintCyan
                )

                Row(
                    modifier = Modifier
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Blueprint Code", code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Código copiado!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar Código",
                        tint = BlueprintTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "COPIAR",
                        fontSize = 10.sp,
                        color = BlueprintTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Code lines
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B13))
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFA5F3FC), // Soft teal coding color
                    lineHeight = 18.sp,
                    modifier = Modifier.horizontalScroll(hScroll)
                )
            }
        }
    }
}

// Draw a beautiful blueprint engineering network/grid behind contents!
fun Modifier.drawBlueprintGrid(): Modifier = this.drawBehind {
    val step = 45.dp.toPx()
    val width = size.width
    val height = size.height
    
    // Draw horizontal grid lines
    var y = 0f
    while (y < height) {
        drawLine(
            color = Color(0xFF151D30),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
        )
        y += step
    }

    // Draw vertical grid lines
    var x = 0f
    while (x < width) {
        drawLine(
            color = Color(0xFF151D30),
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1f
        )
        x += step
    }
}
