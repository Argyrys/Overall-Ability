package com.oaa.livingresume.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.oaa.livingresume.data.model.Grade
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.SkillBadge
import com.oaa.livingresume.ui.theme.CyanGlow
import com.oaa.livingresume.ui.theme.GoldGlow
import com.oaa.livingresume.ui.theme.NavyCard
import com.oaa.livingresume.ui.theme.OutlineFaint
import com.oaa.livingresume.ui.theme.Rajdhani
import com.oaa.livingresume.ui.theme.TextPrimary
import com.oaa.livingresume.ui.theme.TextSecondary
import com.oaa.livingresume.ui.theme.accent
import com.oaa.livingresume.ui.theme.exo
import com.oaa.livingresume.ui.theme.gradeColor
import com.oaa.livingresume.ui.theme.microHeader
import com.oaa.livingresume.ui.theme.numeric

/** Standard HUD card used across all screens: cut corners + cyan brackets. */
@Composable
fun HudCard(
    modifier: Modifier = Modifier,
    borderColor: Color = OutlineFaint,
    background: Color = NavyCard,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    brackets: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = androidx.compose.foundation.shape.CutCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape),
    ) {
        Column(Modifier.padding(contentPadding), content = content)
        if (brackets) {
            CornerBrackets(
                modifier = Modifier.matchParentSize(),
                color = if (borderColor == OutlineFaint) CyanGlow.copy(alpha = 0.55f) else borderColor,
            )
        }
    }
}

/**
 * Four small L-shaped HUD brackets inset from the corners of a cut-corner card.
 * The inset keeps them clear of the 16dp diagonal cut.
 */
@Composable
fun CornerBrackets(
    modifier: Modifier = Modifier,
    color: Color = CyanGlow,
    inset: Dp = 10.dp,
    length: Dp = 11.dp,
    thickness: Dp = 2.dp,
) {
    androidx.compose.foundation.Canvas(modifier) {
        val i = inset.toPx()
        val len = length.toPx()
        val t = thickness.toPx()
        val w = size.width
        val h = size.height
        fun arm(x: Float, y: Float, dx: Float, dy: Float) {
            drawLine(color, Offset(x, y), Offset(x + dx * len, y), strokeWidth = t)
            drawLine(color, Offset(x, y), Offset(x, y + dy * len), strokeWidth = t)
        }
        arm(i, i, 1f, 1f)
        arm(w - i, i, -1f, 1f)
        arm(i, h - i, 1f, -1f)
        arm(w - i, h - i, -1f, -1f)
    }
}

/** Anime-style rank chip (RANK #3 / 10) used on the student card. */
@Composable
fun RankChip(rank: Int, total: Int, modifier: Modifier = Modifier) {
    val shape = androidx.compose.foundation.shape.CutCornerShape(8.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(CyanGlow.copy(alpha = 0.10f))
            .border(1.dp, CyanGlow.copy(alpha = 0.5f), shape)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = "RANK",
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall.microHeader(),
            color = TextSecondary,
        )
        Text(
            text = "#$rank",
            color = GoldGlow,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium.numeric(FontWeight.W600),
        )
        Text(
            text = "/$total",
            color = TextSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium.numeric(FontWeight.W600),
        )
    }
}

/** Small uppercase section title with optional subtitle. */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    accent: Color = CyanGlow,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

/** Letter-grade pill (A+ .. E). */
@Composable
fun GradeBadge(
    grade: Grade,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    val color = gradeColor(grade)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (large) 14.dp else 10.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.2.dp, color.copy(alpha = 0.65f), RoundedCornerShape(if (large) 14.dp else 10.dp))
            .padding(horizontal = if (large) 16.dp else 10.dp, vertical = if (large) 8.dp else 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = grade.label,
            color = color,
            style = TextStyle(
                fontFamily = Rajdhani,
                fontWeight = FontWeight.W700,
                fontSize = if (large) 34.sp else 14.sp,
                letterSpacing = if (large) 1.sp else 0.sp,
                fontFeatureSettings = "tnum",
            ),
        )
    }
}

/** Small pillar chip with a colour dot. */
@Composable
fun PillarTag(pillar: Pillar, modifier: Modifier = Modifier, showWeight: Boolean = false) {
    val accent = pillar.accent
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
        Text(
            text = if (showWeight) "${pillar.short} ${pillar.weightPercent}%" else pillar.short,
            color = accent,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium.exo(FontWeight.W500),
        )
    }
}

/** Thin animated score bar (0..100). */
@Composable
fun ScoreProgressBar(
    score: Double,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
) {
    val fraction by animateFloatAsState(
        targetValue = (score / 100.0).coerceIn(0.0, 1.0).toFloat(),
        animationSpec = tween(700),
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(OutlineFaint),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(height)
                .clip(RoundedCornerShape(999.dp))
                .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.65f), color))),
        )
    }
}

/** Label + value stat cell (CGPA, OAA score, tier...). */
@Composable
fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall.microHeader(),
            color = TextSecondary,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.numeric(FontWeight.W700),
            color = valueColor,
        )
    }
}

/** Circular initials avatar tinted with [color]. */
@Composable
fun InitialsAvatar(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .border(1.2.dp, color.copy(alpha = 0.55f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(name),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value / 2.6f).sp,
        )
    }
}

/** Verified skill badge chip (Top 5% Coder, SIH Finalist, ...). */
@Composable
fun SkillBadgeChip(badge: SkillBadge, modifier: Modifier = Modifier) {
    val accent = badge.pillar.accent
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column {
            Text(
                text = badge.title,
                color = TextPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge.exo(FontWeight.W500),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = badge.subtitle,
                color = TextSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall.exo(FontWeight.W400),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (badge.verified) {
            Box(
                Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(GoldGlow),
            )
        }
    }
}

/** Uppercase HUD eyebrow label. */
@Composable
fun Eyebrow(text: String, color: Color = CyanGlow, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall.microHeader(),
        color = color,
        modifier = modifier,
    )
}

fun initialsOf(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .mapNotNull { it.firstOrNull() }
        .take(2)
        .joinToString("")
        .uppercase()
