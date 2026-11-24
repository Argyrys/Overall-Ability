package com.oaa.livingresume.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun darkScheme() = darkColorScheme(
    primary = CyanGlow,
    onPrimary = Color(0xFF00141A),
    primaryContainer = Color(0xFF00323D),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = GoldGlow,
    onSecondary = Color(0xFF1A1400),
    secondaryContainer = Color(0xFF3D3000),
    onSecondaryContainer = Color(0xFFFFE27A),
    tertiary = IndigoGlow,
    onTertiary = Color(0xFF12093D),
    background = DeepNavy,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSecondary,
    outline = OutlineFaint,
    outlineVariant = OutlineFaint,
    error = DangerRed,
    onError = Color(0xFF3B0010),
)

private fun lightScheme() = lightColorScheme(
    primary = CyanGlow,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCDEFF6),
    onPrimaryContainer = Color(0xFF00363F),
    secondary = GoldGlow,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFEFC2),
    onSecondaryContainer = Color(0xFF4A3200),
    tertiary = IndigoGlow,
    onTertiary = Color(0xFFFFFFFF),
    background = DeepNavy,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSecondary,
    outline = OutlineFaint,
    outlineVariant = OutlineFaint,
    error = DangerRed,
    onError = Color(0xFFFFFFFF),
)

private val OaaTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = (-0.5).sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    ),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.4.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.6.sp),
)

/**
 * Futuristic HUD theme with a manual dark/light switch ([OaaThemeState]).
 * The Material color scheme is rebuilt whenever the toggle flips.
 */
@Composable
fun OaaTheme(content: @Composable () -> Unit) {
    val light = OaaThemeState.isLight
    val scheme = remember(light) { if (light) lightScheme() else darkScheme() }
    MaterialTheme(
        colorScheme = scheme,
        typography = OaaTypography,
        content = content,
    )
}
