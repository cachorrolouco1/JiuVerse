package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlueprintColorScheme = darkColorScheme(
    primary = BlueprintCyan,
    onPrimary = Color(0xFF0F172A),
    secondary = BlueprintTeal,
    onSecondary = Color(0xFF0F172A),
    tertiary = BlueprintOrange,
    background = BlueprintBg,
    onBackground = BlueprintTextPrimary,
    surface = BlueprintCard,
    onSurface = BlueprintTextPrimary,
    surfaceVariant = BlueprintHeader,
    onSurfaceVariant = BlueprintTextSecondary,
    outline = BlueprintGridLine
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for blueprint-feeling coding vibe
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our beautiful theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlueprintColorScheme,
        typography = Typography,
        content = content
    )
}
