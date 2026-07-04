package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val CosmicBlue = Color(0xFF3B82F6)
val GlowingAmber = Color(0xFFF59E0B)
val LightGray = Color(0xFFF1F5F9)
val PureWhite = Color(0xFFFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = CosmicBlue,
    onPrimary = PureWhite,
    secondary = GlowingAmber,
    onSecondary = Slate900,
    background = Slate900,
    onBackground = LightGray,
    surface = Slate800,
    onSurface = LightGray,
    surfaceVariant = Slate700,
    onSurfaceVariant = LightGray
)

private val LightColorScheme = lightColorScheme(
    primary = CosmicBlue,
    onPrimary = PureWhite,
    secondary = GlowingAmber,
    onSecondary = Slate900,
    background = LightGray,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Slate800
)

@Composable
fun TalentDevTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We prioritize DarkColorScheme to preserve the Cosmic Slate visual theme requested in instructions
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
