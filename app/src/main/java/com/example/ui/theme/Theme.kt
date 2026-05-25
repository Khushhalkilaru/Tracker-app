package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ObsidianColorScheme = darkColorScheme(
    primary = VoltGreen,
    secondary = NeonCyan,
    tertiary = CyberPink,
    background = ObsidianBg,
    surface = CardGray,
    onPrimary = ObsidianBg,
    onSecondary = ObsidianBg,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = CyberPink
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default
    dynamicColor: Boolean = false, // Disable system dynamic color override to maintain brand identity
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ObsidianColorScheme,
        typography = Typography,
        content = content
    )
}
