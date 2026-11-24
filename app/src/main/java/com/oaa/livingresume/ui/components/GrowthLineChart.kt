package com.oaa.livingresume.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.oaa.livingresume.ui.theme.CyanGlow
import com.oaa.livingresume.ui.theme.GoldGlow
import com.oaa.livingresume.ui.theme.OutlineFaint

/**
 * Lightweight semester growth line chart (0..100) drawn with Compose Canvas.
 * Used on the Timeline screen to show the 4-year living-resume curve.
 */
@Composable
fun GrowthLineChart(
    values: List<Double>,
    modifier: Modifier = Modifier,
) {
    if (values.isEmpty()) return

    Canvas(modifier = modifier) {
        // Horizontal grid at 25 / 50 / 75 / 100
        listOf(25.0, 50.0, 75.0, 100.0).forEach { grid ->
            val y = size.height * (1f - (grid / 100.0).toFloat())
            drawLine(
                color = OutlineFaint,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        val stepX = if (values.size > 1) size.width / (values.size - 1) else 0f
        fun pointAt(index: Int, value: Double): Offset {
            val x = index * stepX
            val y = size.height * (1f - (value / 100.0).toFloat()).coerceIn(0f, 1f)
            return Offset(x, y)
        }
        val points = values.mapIndexed { index, value -> pointAt(index, value) }

        // Area fill under the curve
        val area = Path().apply {
            moveTo(points.first().x, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, size.height)
            close()
        }
        drawPath(
            path = area,
            brush = Brush.verticalGradient(
                colors = listOf(CyanGlow.copy(alpha = 0.30f), CyanGlow.copy(alpha = 0.02f)),
                startY = 0f,
                endY = size.height,
            ),
        )

        // The curve itself
        val line = Path()
        points.forEachIndexed { index, p ->
            if (index == 0) line.moveTo(p.x, p.y) else line.lineTo(p.x, p.y)
        }
        drawPath(
            path = line,
            color = CyanGlow,
            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // Nodes - gold for the latest semester
        points.forEachIndexed { index, p ->
            val isLast = index == points.lastIndex
            if (isLast) {
                drawCircle(color = GoldGlow.copy(alpha = 0.28f), radius = 8.dp.toPx(), center = p)
            }
            drawCircle(
                color = if (isLast) GoldGlow else CyanGlow,
                radius = if (isLast) 5.dp.toPx() else 3.6.dp.toPx(),
                center = p,
            )
        }
    }
}
