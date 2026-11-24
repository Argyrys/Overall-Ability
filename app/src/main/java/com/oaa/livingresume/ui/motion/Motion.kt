package com.oaa.livingresume.ui.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.oaa.livingresume.audio.Sfx
import com.oaa.livingresume.audio.SoundManager
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Access the shared [SoundManager] from any composable. */
val LocalSound = compositionLocalOf<SoundManager?> { null }

/**
 * Fire-and-forget handle for UI sounds. Bound by [ProvideSound]; reads a
 * volatile field so plain (non-composable) click handlers can play sounds.
 */
private var soundManager: SoundManager? = null

/** Provides the singleton sound manager for this subtree. */
@Composable
fun ProvideSound(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val manager = remember(context) { SoundManager.get(context) }
    androidx.compose.runtime.DisposableEffect(manager) {
        soundManager = manager
        onDispose { soundManager = null }
    }
    CompositionLocalProvider(LocalSound provides manager) { content() }
}

/** Play a UI sound effect. Safe no-op when no provider is installed (previews/tests). */
fun playSfx(sfx: Sfx) {
    soundManager?.play(sfx)
}

/**
 * Staggered entrance: slide up + fade in, delayed by [index] * 60ms.
 * Call inside a composable (state survives recomposition via remember).
 */
@Composable
fun Modifier.entrance(index: Int): Modifier {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(30f) }
    LaunchedEffect(index) {
        delay(index.coerceAtMost(12) * 60L)
        alpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
        offsetY.animateTo(0f, tween(380, easing = FastOutSlowInEasing))
    }
    return this.graphicsLayer {
        this.alpha = alpha.value
        this.translationY = offsetY.value
    }
}

/**
 * Press-scale feedback: scales down slightly while pressed.
 * Pass the SAME interaction source that your clickable uses.
 */
@Composable
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "press-scale",
    )
    return this.scale(scale)
}

/** Gentle infinite pulse (0.94f..1f) for hero elements like the radar's best node. */
@Composable
fun rememberPulse(speedMs: Int = 1400): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(speedMs, easing = FastOutSlowInEasing)),
        label = "pulse-value",
    )
    return pulse
}

/**
 * Rolling score readout: animates from the previous value to [value]
 * (e.g. overall OAA jumps after a verifier approves a proof).
 */
@Composable
fun AnimatedScoreText(
    value: Double,
    modifier: Modifier = Modifier,
    format: (Double) -> String = { v -> ((v * 10).roundToInt() / 10.0).toString() },
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.MaterialTheme.typography.titleLarge,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
) {
    val animated = remember { Animatable(value.toFloat()) }
    LaunchedEffect(value) {
        animated.animateTo(
            targetValue = value.toFloat(),
            animationSpec = tween(700, easing = FastOutSlowInEasing),
        )
    }
    androidx.compose.material3.Text(
        text = format(animated.value.toDouble()),
        modifier = modifier,
        style = style,
        color = color,
    )
}
