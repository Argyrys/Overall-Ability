package com.oaa.livingresume.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaa.livingresume.audio.Sfx
import com.oaa.livingresume.data.model.Grade
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.Student
import com.oaa.livingresume.data.model.TargetRole
import com.oaa.livingresume.engine.OaaEngine
import com.oaa.livingresume.ui.components.Eyebrow
import com.oaa.livingresume.ui.components.GradeBadge
import com.oaa.livingresume.ui.components.HudCard
import com.oaa.livingresume.ui.components.InitialsAvatar
import com.oaa.livingresume.ui.components.PillarTag
import com.oaa.livingresume.ui.components.RadarChartCanvas
import com.oaa.livingresume.ui.components.RankChip
import com.oaa.livingresume.ui.components.ScoreProgressBar
import com.oaa.livingresume.ui.components.SectionHeader
import com.oaa.livingresume.ui.components.SkillBadgeChip
import com.oaa.livingresume.ui.components.initialsOf
import com.oaa.livingresume.ui.motion.AnimatedScoreText
import com.oaa.livingresume.ui.motion.entrance
import com.oaa.livingresume.ui.motion.playSfx
import com.oaa.livingresume.ui.theme.CyanGlow
import com.oaa.livingresume.ui.theme.DeepNavy
import com.oaa.livingresume.ui.theme.GoldGlow
import com.oaa.livingresume.ui.theme.IndigoGlow
import com.oaa.livingresume.ui.theme.NavyCardAlt
import com.oaa.livingresume.ui.theme.NavyCardDeep
import com.oaa.livingresume.ui.theme.NavySurface
import com.oaa.livingresume.ui.theme.OaaThemeState
import com.oaa.livingresume.ui.theme.OutlineFaint
import com.oaa.livingresume.ui.theme.TextPrimary
import com.oaa.livingresume.ui.theme.TextSecondary
import com.oaa.livingresume.ui.theme.accent
import com.oaa.livingresume.ui.theme.exo
import com.oaa.livingresume.ui.theme.gradeColor
import com.oaa.livingresume.ui.theme.numeric
import com.oaa.livingresume.viewmodel.OaaViewModel

/**
 * Tab 1 - Home: futuristic Student OAA Card (name, branch, grad year, target role,
 * overall grade + 4-axis radar chart), verified skill badges and the
 * "Visible to Batch" opt-in switch.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: OaaViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val me = state.me ?: return
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isLight = OaaThemeState.isLight

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Box(Modifier.fillMaxWidth().entrance(0)) {
                GreetingHeader(
                    me = me,
                    isLight = isLight,
                    onToggleTheme = {
                        OaaThemeState.toggle()
                        OaaThemeState.persist(context)
                        playSfx(Sfx.SUBMIT)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onSignOut = viewModel::logout,
                )
            }
        }
        item {
            Box(Modifier.fillMaxWidth().entrance(1)) {
                val myOverall = OaaEngine.overallRounded(me.pillarScores)
                val rank = state.batchmates.count {
                    OaaEngine.overallRounded(it.pillarScores) > myOverall
                } + 1
                OaaProfileCard(
                    me = me,
                    rank = rank,
                    total = state.batchmates.size + 1,
                )
            }
        }
        item { Box(Modifier.fillMaxWidth().entrance(2)) { BatchVisibilityCard(me) { viewModel.toggleVisibility() } } }
        item { Box(Modifier.fillMaxWidth().entrance(3)) { SkillBadgesCard(me) } }
        item {
            Text(
                text = "Backend: ${state.backend} · ${state.pendingSubmissions.size} proof(s) awaiting verification",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth().entrance(4),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GreetingHeader(
    me: Student,
    isLight: Boolean,
    onToggleTheme: () -> Unit,
    onSignOut: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Eyebrow("OAA // 学生記録 Living Resume")
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Namaste, ${me.name.substringBefore(' ')}",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = me.college,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.width(10.dp))
        InitialsAvatar(name = me.name, color = CyanGlow, size = 52.dp)
        Spacer(Modifier.width(8.dp))
        // Manual dark/light switch (persisted)
        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NavySurface)
                .border(1.dp, OutlineFaint, RoundedCornerShape(12.dp)),
        ) {
            Icon(
                imageVector = if (isLight) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                contentDescription = if (isLight) "Switch to dark theme" else "Switch to light theme",
                tint = if (isLight) IndigoGlow else GoldGlow,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(6.dp))
        IconButton(
            onClick = onSignOut,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NavySurface)
                .border(1.dp, OutlineFaint, RoundedCornerShape(12.dp)),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = "Sign out",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** The collectible-style hero card with the custom Canvas radar chart. */
@Composable
private fun OaaProfileCard(me: Student, rank: Int, total: Int) {
    val overall = OaaEngine.overallRounded(me.pillarScores)
    val grade = OaaEngine.gradeFor(overall)
    val nextGrade = OaaEngine.nextGrade(grade)
    val accent = gradeColor(grade)

    val heroShape = androidx.compose.foundation.shape.CutCornerShape(22.dp)
    // Measure the actual space available: phones, foldables and tablets all
    // get their own padding, radar size and column count without hard-coded screens.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(heroShape)
            .background(Brush.verticalGradient(listOf(NavyCardAlt, NavyCardDeep)))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(CyanGlow, IndigoGlow, GoldGlow, CyanGlow)),
                shape = heroShape,
            ),
    ) {
        val compact = maxWidth < 340.dp
        val wide = maxWidth >= 620.dp
        val cardPadding = when {
            compact -> 12.dp
            wide -> 24.dp
            else -> 18.dp
        }
        val gap = if (compact) 10.dp else 14.dp
        val radarHeight = if (wide) {
            (maxWidth * 0.42f).coerceIn(240.dp, 320.dp)
        } else {
            (maxWidth * 0.72f).coerceIn(170.dp, 250.dp)
        }

        Column(Modifier.padding(cardPadding)) {
            if (wide) {
                // Tablet / landscape: identity + legend on the left, radar on the right.
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        IdentityHeader(me, grade, rank, total, compact = false)
                        Spacer(Modifier.height(gap))
                        RoleRow(me)
                        Spacer(Modifier.height(gap + 4.dp))
                        PillarLegend(me.pillarScores)
                    }
                    Spacer(Modifier.width(24.dp))
                    RadarChartCanvas(
                        scores = me.pillarScores,
                        modifier = Modifier
                            .weight(1.15f)
                            .height(radarHeight),
                    )
                }
            } else {
                IdentityHeader(me, grade, rank, total, compact = compact)
                Spacer(Modifier.height(gap))
                RoleRow(me)
                Spacer(Modifier.height(gap + 2.dp))
                RadarChartCanvas(
                    scores = me.pillarScores,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(radarHeight),
                )
                Spacer(Modifier.height(gap + 2.dp))
                PillarLegend(me.pillarScores)
            }

            Spacer(Modifier.height(gap))
            HorizontalDivider(color = OutlineFaint)
            Spacer(Modifier.height(gap))
            ScoreFooter(overall, grade, nextGrade, me.pillarScores.weakest().first, accent, compact)
        }
    }
}

/** Name / branch / grade / rank block; stacks vertically on narrow screens. */
@Composable
private fun IdentityHeader(
    me: Student,
    grade: Grade,
    rank: Int,
    total: Int,
    compact: Boolean,
) {
    val identity: @Composable () -> Unit = {
        Eyebrow("Student OAA Card")
        Spacer(Modifier.height(if (compact) 4.dp else 6.dp))
        Text(
            me.name,
            style = if (compact) {
                MaterialTheme.typography.titleMedium.exo(FontWeight.W700)
            } else {
                MaterialTheme.typography.titleLarge.exo(FontWeight.W700)
            },
            color = TextPrimary,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            me.branch,
            style = MaterialTheme.typography.bodySmall.exo(FontWeight.W400),
            color = TextSecondary,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
        Text(
            "Roll ${me.rollNo} · Batch of ${me.gradYear}",
            style = MaterialTheme.typography.bodySmall.exo(FontWeight.W400),
            color = TextSecondary,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
    val badges: @Composable () -> Unit = {
        GradeBadge(grade, large = !compact)
        Spacer(Modifier.height(6.dp))
        RankChip(rank = rank, total = total)
    }

    if (compact) {
        Column {
            identity()
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                badges()
            }
        }
    } else {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) { identity() }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) { badges() }
        }
    }
}

/** Role + CGPA pills wrap onto the next line when the card is narrow. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoleRow(me: Student) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        RoleChip(me.targetRole)
        InfoPill("CGPA ${me.cgpa}", GoldGlow)
    }
}

/** Overall score, progress bar and exact points to the next letter tier. */
@Composable
private fun ScoreFooter(
    overall: Double,
    grade: Grade,
    nextGrade: Grade?,
    weakest: Pillar,
    accent: androidx.compose.ui.graphics.Color,
    compact: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Eyebrow("Overall OAA · 総合力")
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                AnimatedScoreText(
                    value = overall,
                    format = { v -> v.toInt().toString() },
                    style = if (compact) {
                        MaterialTheme.typography.headlineSmall.copy(letterSpacing = (-2).sp).numeric(FontWeight.W700)
                    } else {
                        MaterialTheme.typography.displaySmall.copy(letterSpacing = (-2).sp).numeric(FontWeight.W700)
                    },
                    color = accent,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.titleMedium.numeric(FontWeight.W600),
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            ScoreProgressBar(overall, accent, height = 6.dp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (nextGrade != null) {
                    "${OaaEngine.pointsToNextTier(overall)} points to reach ${nextGrade.label}"
                } else {
                    "Highest letter tier achieved - keep it above 90"
                },
                style = MaterialTheme.typography.bodySmall.exo(FontWeight.W400),
                color = TextSecondary,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(if (compact) 8.dp else 12.dp))
        Column(horizontalAlignment = Alignment.End) {
            GradeBadge(grade)
            Spacer(Modifier.height(6.dp))
            PillarTag(weakest)
        }
    }
}

private fun Map<Pillar, Double>.weakest(): Pair<Pillar, Double> = OaaEngine.ranked(this).first()

@Composable
private fun RoleChip(role: TargetRole) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(CyanGlow.copy(alpha = 0.12f))
            .border(1.dp, CyanGlow.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.BusinessCenter,
            contentDescription = null,
            tint = CyanGlow,
            modifier = Modifier.size(14.dp),
        )
        Text(role.label, style = MaterialTheme.typography.labelMedium.exo(FontWeight.W500), color = TextPrimary)
    }
}

@Composable
private fun InfoPill(text: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium.numeric(FontWeight.W700),
            color = color,
        )
    }
}

/**4 pillar rows: colour dot, name, weight %, score · tier, progress bar. */
@Composable
private fun PillarLegend(scores: Map<Pillar, Double>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Pillar.entries.forEach { pillar ->
            val score = scores[pillar] ?: 0.0
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(pillar.accent),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = pillar.label,
                        style = MaterialTheme.typography.bodySmall.exo(FontWeight.W400),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${pillar.weightPercent}%",
                        style = MaterialTheme.typography.bodySmall.numeric(FontWeight.W600),
                        color = TextSecondary,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "${score.toInt()} · ${OaaEngine.gradeFor(score).label}",
                        style = MaterialTheme.typography.titleSmall.numeric(FontWeight.W700),
                        color = pillar.accent,
                    )
                }
                Spacer(Modifier.height(6.dp))
                ScoreProgressBar(score, pillar.accent, height = 5.dp)
            }
        }
    }
}

@Composable
private fun BatchVisibilityCard(me: Student, onToggle: () -> Unit) {
    HudCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Visible to Batch", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Opt in to appear on the batch leaderboard & team finder. " +
                        "Classmates only ever see your letter grade - never your exact marks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = me.optedInPublic,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DeepNavy,
                    checkedTrackColor = CyanGlow,
                    checkedBorderColor = CyanGlow,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = NavySurface,
                    uncheckedBorderColor = OutlineFaint,
                ),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillBadgesCard(me: Student) {
    HudCard {
        SectionHeader(
            title = "Verified Skill Badges",
            subtitle = "${me.badges.size} badges verified by faculty, clubs & platforms",
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            me.badges.forEach { badge -> SkillBadgeChip(badge) }
        }
        if (me.badges.isEmpty()) {
            Text("No badges yet - submit your first proof on the Share tab.", color = TextSecondary)
        }
    }
}
