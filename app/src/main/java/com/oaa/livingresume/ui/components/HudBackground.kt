package com.oaa.livingresume.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oaa.livingresume.ui.theme.CyanGlow
import com.oaa.livingresume.ui.theme.OaaThemeState

/**
 * Faint blueprint grid + CRT scanlines behind the whole app -
 * the "anime HUD terminal" backdrop. Theme-aware (subtle navy grid on paper
 * in light mode, cyan grid on deep navy in dark mode).
 */
@Composable
fun HudGridBackground(modifier: Modifier = Modifier) {
    val light = OaaThemeState.isLight
    Canvas(modifier) {
        val grid = if (light) Color(0x140D1422) else CyanGlow.copy(alpha = 0.055f)
        val scan = if (light) Color(0x0A0D1422) else Color(0x1200E5FF)

        // Blueprint grid
        val step = 30.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(grid, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
        }

        // CRT scanlines
        val scanStep = 6.dp.toPx()
        y = 0f
        while (y < size.height) {
            drawLine(scan, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += scanStep
        }
    }
}
