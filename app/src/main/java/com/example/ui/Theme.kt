package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.Brush

// Re-map standard Color name to our adaptive Light-Mode friendly Color object!
object Color {
    operator fun invoke(color: Long): ComposeColor = ComposeColor(color)
    operator fun invoke(color: Int): ComposeColor = ComposeColor(color)
    operator fun invoke(red: Float, green: Float, blue: Float, alpha: Float = 1f): ComposeColor = ComposeColor(red, green, blue, alpha)
    operator fun invoke(red: Int, green: Int, blue: Int, alpha: Int = 255): ComposeColor = ComposeColor(red, green, blue, alpha)

    val White = ComposeColor(0xFF1E293B) // Beautiful Charcoal Slate (slate-800) for light-mode text
    val Black = ComposeColor(0xFFFFFFFF) // Beautiful White background/elements
    val Transparent = ComposeColor.Transparent
    val Red = ComposeColor(0xFFEF4444)
    val Green = ComposeColor(0xFF10B981)
    val Blue = ComposeColor(0xFF3B82F6)
    val Yellow = ComposeColor(0xFFFBBF24)
    val Gray = ComposeColor(0xFF64748B)
    val LightGray = ComposeColor(0xFFE2E8F0)

    val TrueWhite = ComposeColor(0xFFFFFFFF)
    val TrueBlack = ComposeColor(0xFF000000)
}

// Global light-colored Slate theme tokens
val Slate950 = ComposeColor(0xFF090D16) // Rich very dark Slate
val Slate900 = ComposeColor(0xFFF8FAFC) // Clean Slate-50 background
val Slate800 = ComposeColor(0xFFFFFFFF) // Pure White Card/Surface container
val Slate700 = ComposeColor(0xFFE2E8F0) // Crisp light border (Slate-200)
val Slate600 = ComposeColor(0xFF64748B) // Slate-500 secondary icons & labels
val CosmicBlue = ComposeColor(0xFF4F46E5) // Vibrant Indigo for energetic primary actions
val GlowingAmber = ComposeColor(0xFFD97706) // Rich bronze amber for high contrast
val LightGray = ComposeColor(0xFF334155) // Slate-700 dark grey text for readable body copy
val PureWhite = ComposeColor(0xFF0F172A) // Slate-900 black text for strong headers

// New Flashy Neon Accents scaled for gorgeous light background contrast
val NeonPink = ComposeColor(0xFFDB2777) // Rich Rose Pink
val NeonCyan = ComposeColor(0xFF0891B2) // Clean Ocean Cyan
val NeonPurple = ComposeColor(0xFF7C3AED) // Vivid Royal Amethyst
val NeonGreen = ComposeColor(0xFF059669) // Success Emerald Green

object HighDesignTokens {
    val SparkleGradient = Brush.linearGradient(
        colors = listOf(NeonPurple, CosmicBlue, NeonPink, GlowingAmber)
    )
    val OceanGlowGradient = Brush.linearGradient(
        colors = listOf(CosmicBlue, NeonCyan, NeonGreen)
    )
    val LuxuryGoldenGradient = Brush.linearGradient(
        colors = listOf(GlowingAmber, NeonPink, NeonPurple)
    )
}

private val LightColorScheme = lightColorScheme(
    primary = CosmicBlue,
    onPrimary = Color.TrueWhite,
    secondary = GlowingAmber,
    onSecondary = Slate900,
    background = Slate900,
    onBackground = LightGray,
    surface = Slate800,
    onSurface = LightGray,
    surfaceVariant = Slate700,
    onSurfaceVariant = LightGray
)

@Composable
fun TalentDevTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We prioritize our beautiful light-colored scheme to honor user preference
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
