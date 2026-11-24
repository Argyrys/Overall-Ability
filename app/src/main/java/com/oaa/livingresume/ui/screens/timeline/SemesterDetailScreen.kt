package com.oaa.livingresume.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oaa.livingresume.data.model.Milestone
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.SemesterEntry
import com.oaa.livingresume.data.model.SemesterStatus
import com.oaa.livingresume.engine.OaaEngine
import com.oaa.livingresume.ui.components.Eyebrow
import com.oaa.livingresume.ui.components.GradeBadge
import com.oaa.livingresume.ui.components.HudCard
import com.oaa.livingresume.ui.components.RadarChartCanvas
import com.oaa.livingresume.ui.components.ScoreProgressBar
import com.oaa.livingresume.ui.components.SectionHeader
import com.oaa.livingresume.ui.motion.entrance
import com.oaa.livingresume.ui.theme.CyanGlow
import com.oaa.livingresume.ui.theme.DangerRed
import com.oaa.livingresume.ui.theme.GoldGlow
import com.oaa.livingresume.ui.theme.GreenGlow
import com.oaa.livingresume.ui.theme.NavyCard
import com.oaa.livingresume.ui.theme.OutlineFaint
import com.oaa.livingresume.ui.theme.TextPrimary
import com.oaa.livingresume.ui.theme.TextSecondary
import com.oaa.livingresume.ui.theme.accent
import com.oaa.livingresume.ui.theme.gradeColor
import com.oaa.livingresume.ui.theme.semesterStatusColor
import com.oaa.livingresume.viewmodel.OaaViewModel

/**
 * Semester detail (reached from the Timeline rail): hero stats, that semester's
 * radar chart, per-pillar deltas vs the previous semester and its milestones.
 */
@Composable
fun SemesterDetailScreen(
    viewModel: OaaViewModel,
    semesterNumber: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val me = state.me ?: return

    // Soft whoosh as the semester record opens.
    androidx.compose.runtime.LaunchedEffect(semesterNumber) {
        com.oaa.livingresume.ui.motion.playSfx(com.oaa.livingresume.audio.Sfx.OPEN)
    }

    val entry: SemesterEntry? = remember(me.semesters, semesterNumber) {
        me.semesters.find { it.semester == semesterNumber }
    }
    val previous: SemesterEntry? = remember(me.semesters, semesterNumber) {
        me.semesters.find { it.semester == semesterNumber - 1 }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyCard)
                        .border(1.dp, OutlineFaint, RoundedCornerShape(12.dp)),
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Eyebrow("OAA // 学期記録 Semester Record")
                    Text(
                        text = "Semester $semesterNumber",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                }
            }
        }

        if (entry == null || entry.status == SemesterStatus.UPCOMING) {
            item {
                HudCard {
                    Text(
                        text = entry?.note
                            ?: "Semester $semesterNumber hasn't started yet - plan your capstone and placement prep.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
            return@LazyColumn
        }

        item { Box(Modifier.fillMaxWidth().entrance(0)) { SemesterHeroCard(entry, previous) } }

        if (entry.pillarScores.isNotEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().entrance(1)) {
                    HudCard(borderColor = CyanGlow.copy(alpha = 0.4f)) {
                        SectionHeader(
                            title = "4-Axis Snapshot",
                            subtitle = "Pillar scores as they stood in Semester $semesterNumber",
                        )
                        Spacer(Modifier.height(12.dp))
                        RadarChartCanvas(
                            scores = entry.pillarScores,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp),
                        )
                    }
                }
            }

            item { Box(Modifier.fillMaxWidth().entrance(2)) { DeltaCard(entry, previous) } }
        }

        if (entry.milestones.isNotEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().entrance(3)) {
                    SectionHeader(
                        title = "Verified Milestones",
                        subtitle = "${entry.milestones.size} record(s) signed off this semester",
                    )
                }
            }
            items(entry.milestones.size) { index ->
                Box(Modifier.fillMaxWidth().entrance((index + 4).coerceAtMost(12))) {
                    MilestoneDetailCard(entry.milestones[index])
                }
            }
        }
    }
}

@Composable
private fun SemesterHeroCard(entry: SemesterEntry, previous: SemesterEntry?) {
    val overall = if (entry.pillarScores.isNotEmpty()) OaaEngine.overallRounded(entry.pillarScores) else null
    val grade = overall?.let { OaaEngine.gradeFor(it) }
    val prevOverall = previous?.let {
        if (it.pillarScores.isNotEmpty()) OaaEngine.overallRounded(it.pillarScores) else null
    }

    HudCard(borderColor = grade?.let { gradeColor(it).copy(alpha = 0.5f) } ?: OutlineFaint) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.term,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "GPA ${String.format("%.2f", entry.gpa)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                )
            }
            if (grade != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GradeBadge(grade, large = true)
                    Spacer(Modifier.height(6.dp))
                    StatusChip(entry.status)
                }
            } else {
                StatusChip(entry.status)
            }
        }

        if (overall != null && grade != null) {
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "OAA $overall",
                    style = MaterialTheme.typography.titleMedium,
                    color = gradeColor(grade),
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(Modifier.weight(1f))
                if (prevOverall != null) {
                    val delta = OaaEngine.round1(overall - prevOverall)
                    DeltaChip(delta)
                }
            }
            Spacer(Modifier.height(8.dp))
            ScoreProgressBar(overall, gradeColor(grade), height = 8.dp)
        }
    }
}

/** Per-pillar change vs the previous semester. */
@Composable
private fun DeltaCard(entry: SemesterEntry, previous: SemesterEntry?) {
    HudCard {
        SectionHeader(
            title = "Vs Previous Semester",
            subtitle = if (previous == null) "First scored semester - baseline established" else "Semester ${previous.semester} -> ${entry.semester}",
        )
        Spacer(Modifier.height(12.dp))
        Pillar.entries.forEach { pillar ->
            val score = entry.pillarScores[pillar] ?: 0.0
            val prevScore = previous?.pillarScores?.get(pillar)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(pillar.accent),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = pillar.short,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (prevScore != null) {
                    DeltaChip(OaaEngine.round1(score - prevScore))
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = score.toInt().toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = pillar.accent,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Spacer(Modifier.height(8.dp))
            ScoreProgressBar(score, pillar.accent, height = 5.dp)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun DeltaChip(delta: Double) {
    val up = delta >= 0
    val color = when {
        delta > 0 -> GreenGlow
        delta < 0 -> DangerRed
        else -> TextSecondary
    }
    val icon = when {
        delta > 0 -> Icons.Outlined.TrendingUp
        delta < 0 -> Icons.Outlined.TrendingDown
        else -> null
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        }
        Text(
            text = if (up) "+$delta" else "$delta",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun StatusChip(status: SemesterStatus) {
    val color = semesterStatusColor(status)
    val label = when (status) {
        SemesterStatus.COMPLETED -> "Completed"
        SemesterStatus.ONGOING -> "In progress"
        SemesterStatus.UPCOMING -> "Upcoming"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun MilestoneDetailCard(milestone: Milestone) {
    HudCard(
        borderColor = if (milestone.verified) GreenGlow.copy(alpha = 0.35f) else OutlineFaint,
        contentPadding = PaddingValues(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Outlined.Verified,
                contentDescription = "Verified",
                tint = if (milestone.verified) GreenGlow else TextSecondary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = milestone.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(milestone.pillar.accent.copy(alpha = 0.12f))
                            .border(
                                1.dp,
                                milestone.pillar.accent.copy(alpha = 0.45f),
                                RoundedCornerShape(999.dp),
                            )
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = milestone.pillar.short,
                            style = MaterialTheme.typography.labelMedium,
                            color = milestone.pillar.accent,
                        )
                    }
                    if (milestone.date.isNotEmpty()) {
                        Text(
                            text = milestone.date,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }
    }
}
