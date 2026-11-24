package com.oaa.livingresume.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.ui.motion.rememberPulse
import com.oaa.livingresume.ui.theme.CyanGlow
import com.oaa.livingresume.ui.theme.GoldGlow
import com.oaa.livingresume.ui.theme.OutlineFaint
import com.oaa.livingresume.ui.theme.TextPrimary
import com.oaa.livingresume.ui.theme.accent
import com.oaa.livingresume.ui.theme.exo
import com.oaa.livingresume.ui.theme.numeric
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Custom 4-axis radar chart, drawn entirely with a Jetpack Compose [Canvas].
 *
 * Layout (clockwise from top):
 *   top    -> Academic Ability   (35%)
 *   right  -> Adaptability       (30%)
 *   bottom -> Social             (20%)
 *   left   -> Physical           (15%)
 *
 * The polygon animates in and replays whenever the scores are recalculated
 * (e.g. after a verifier approves a proof).
 */
@Composable
fun RadarChartCanvas(
    scores: Map<Pillar, Double>,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(scores) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(900))
    }

    // Gentle breathing pulse on the best pillar's gold halo
    val haloPulse = rememberPulse()

    val pillars = Pillar.entries

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 62.dp, vertical = 34.dp),
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = min(size.width, size.height) / 2f - 2.dp.toPx()

            /** Point on axis [index] at [fraction] (0..1) of the radius. */
            fun vertex(index: Int, fraction: Float): Offset {
                val angle = Math.toRadians(-90.0 + (360.0 / pillars.size) * index)
                val radius = maxRadius * fraction
                return Offset(
                    center.x + (radius * cos(angle)).toFloat(),
                    center.y + (radius * sin(angle)).toFloat(),
                )
            }

            // Concentric rings (25 / 50 / 75 / 100%)
            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { ring ->
                val ringPath = Path()
                pillars.indices.forEach { i ->
                    val p = vertex(i, ring)
                    if (i == 0) ringPath.moveTo(p.x, p.y) else ringPath.lineTo(p.x, p.y)
                }
                ringPath.close()
                drawPath(
                    path = ringPath,
                    color = OutlineFaint,
                    style = Stroke(width = if (ring == 1f) 1.6.dp.toPx() else 1.dp.toPx()),
                )
            }

            // Axis lines from the centre to each vertex
            pillars.indices.forEach { i ->
                drawLine(
                    color = OutlineFaint,
                    start = center,
                    end = vertex(i, 1f),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            // Score polygon (fill + stroke)
            val dataPath = Path()
            pillars.forEachIndexed { i, pillar ->
                val fraction = (((scores[pillar] ?: 0.0) / 100.0).toFloat() * progress.value)
                val p = vertex(i, fraction)
                if (i == 0) dataPath.moveTo(p.x, p.y) else dataPath.lineTo(p.x, p.y)
            }
            dataPath.close()
            drawPath(path = dataPath, color = CyanGlow.copy(alpha = 0.18f), style = Fill)
            drawPath(
                path = dataPath,
                color = CyanGlow,
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            // Vertex nodes, tinted per pillar, plus a gold halo on the current best pillar
            val best = Pillar.entries.maxByOrNull { scores[it] ?: 0.0 }
            pillars.forEachIndexed { i, pillar ->
                val fraction = (((scores[pillar] ?: 0.0) / 100.0).toFloat() * progress.value)
                val p = vertex(i, fraction)
                if (pillar == best) {
                    val halo = 8.dp.toPx() * haloPulse
                    drawCircle(color = GoldGlow.copy(alpha = 0.30f * haloPulse), radius = halo, center = p)
                }
                drawCircle(color = pillar.accent, radius = 4.5.dp.toPx(), center = p)
            }
        }

        // Axis labels in the reserved margins (top / right / bottom / left)
        RadarLabel(pillars[0], scores[pillars[0]] ?: 0.0, Modifier.align(Alignment.TopCenter))
        RadarLabel(pillars[1], scores[pillars[1]] ?: 0.0, Modifier.align(Alignment.CenterEnd))
        RadarLabel(pillars[2], scores[pillars[2]] ?: 0.0, Modifier.align(Alignment.BottomCenter))
        RadarLabel(pillars[3], scores[pillars[3]] ?: 0.0, Modifier.align(Alignment.CenterStart))
    }
}

@Composable
private fun RadarLabel(pillar: Pillar, score: Double, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.material3.Text(
            text = pillar.short,
            style = MaterialTheme.typography.labelSmall.exo(FontWeight.W500),
            color = pillar.accent,
            maxLines = 1,
        )
        androidx.compose.material3.Text(
            text = if (score > 0) score.toInt().toString() else "-",
            style = MaterialTheme.typography.titleSmall.numeric(FontWeight.W700),
            color = TextPrimary,
        )
    }
}
