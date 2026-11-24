package com.oaa.livingresume.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oaa.livingresume.data.model.Milestone
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.SemesterEntry
import com.oaa.livingresume.data.model.SemesterStatus
import com.oaa.livingresume.engine.OaaEngine
import com.oaa.livingresume.ui.components.Eyebrow
import com.oaa.livingresume.ui.components.GrowthLineChart
import com.oaa.livingresume.ui.components.HudCard
import com.oaa.livingresume.ui.components.SectionHeader
import com.oaa.livingresume.ui.motion.entrance
import com.oaa.livingresume.ui.theme.CyanGlow
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
 * Tab 2 - Timeline: a vertical 4-year living resume from Semester 1 to 8,
 * with the semester growth curve and verified milestones (internships,
 * hackathons, club leadership).
 */
@Composable
fun TimelineScreen(
    viewModel: OaaViewModel,
    onSemesterClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val me = state.me ?: return
    val growth = remember(me.semesters) { OaaEngine.semesterGrowth(me.semesters) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(Modifier.fillMaxWidth().entrance(0)) {
                Eyebrow("OAA // 学生記録 Student Record")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "4-Year Timeline",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${me.name} · ${me.branch}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        // Start padding = rail gutter (22dp) + gap (10dp) so the growth card
        // aligns with the semester cards below instead of jutting out left.
        item {
            Box(Modifier.fillMaxWidth().padding(start = 32.dp).entrance(1)) {
                GrowthCard(growth)
            }
        }

        items(8) { index ->
            val semesterNumber = index + 1
            val entry = me.semesters.find { it.semester == semesterNumber }
            Box(Modifier.fillMaxWidth().entrance((index + 2).coerceAtMost(12))) {
                SemesterRow(
                    entry = entry,
                    semesterNumber = semesterNumber,
                    isLast = index == 7,
                    onClick = { onSemesterClick(semesterNumber) },
                )
            }
        }
    }
}

@Composable
private fun GrowthCard(growth: List<Pair<Int, Double>>) {
    HudCard {
        SectionHeader(
            title = "Semester Growth Curve",
            subtitle = "Weighted OAA overall score, semester by semester",
        )
        Spacer(Modifier.height(14.dp))
        GrowthLineChart(
            values = growth.map { it.second },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            growth.forEach { (semester, _) ->
                Text(
                    text = "S$semester",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        if (growth.size >= 2) {
            val first = OaaEngine.gradeFor(growth.first().second)
            val last = OaaEngine.gradeFor(growth.last().second)
            Text(
                text = "Trend: ${first.label} (Sem ${growth.first().first}) -> " +
                    "${last.label} (Sem ${growth.last().first}) · " +
                    "+${OaaEngine.round1(growth.last().second - growth.first().second)} pts overall",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

/** One rail row: status node + connector line + semester card. */
@Composable
private fun SemesterRow(
    entry: SemesterEntry?,
    semesterNumber: Int,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val status = entry?.status ?: SemesterStatus.UPCOMING
    Box(Modifier.fillMaxWidth()) {
        // Content row reserves the 22dp rail gutter on the left
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(22.dp))
            Spacer(Modifier.width(10.dp))
            SemesterCard(
                entry = entry,
                semesterNumber = semesterNumber,
                modifier = Modifier.weight(1f),
                onClick = onClick,
            )
        }

        // Rail overlays the gutter and stretches to the card's full height
        Column(
            modifier = Modifier
                .width(22.dp)
                .matchParentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(semesterStatusColor(status))
                    .border(2.dp, com.oaa.livingresume.ui.theme.DeepNavy, CircleShape),
            )
            if (!isLast) {
                Box(
                    Modifier
                        .weight(1f)
                        .width(2.dp)
                        .background(OutlineFaint),
                )
            }
        }
    }
}

@Composable
private fun SemesterCard(
    entry: SemesterEntry?,
    semesterNumber: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val status = entry?.status ?: SemesterStatus.UPCOMING
    val overall = entry?.let { if (it.pillarScores.isNotEmpty()) OaaEngine.overallRounded(it.pillarScores) else null }
    val grade = overall?.let { OaaEngine.gradeFor(it) }

    HudCard(
        modifier = modifier.clickable(onClick = onClick),
        borderColor = if (status == SemesterStatus.ONGOING) GoldGlow.copy(alpha = 0.5f) else OutlineFaint,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Semester $semesterNumber",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.weight(1f))
            StatusChip(status)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = entry?.term ?: "Jan - May 2027",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )

        Spacer(Modifier.height(12.dp))
        if (entry == null || entry.status == SemesterStatus.UPCOMING) {
            Text(
                text = entry?.note ?: "Upcoming semester - plan your capstone and placement prep.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            return@HudCard
        }

        // GPA / OAA / tier stats
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            StatBox("GPA", String.format("%.1f", entry.gpa), TextPrimary, Modifier.weight(1f))
            StatBox(
                "OAA",
                overall?.let { OaaEngine.round1(it).toString() } ?: "-",
                grade?.let { gradeColor(it) } ?: TextPrimary,
                Modifier.weight(1f),
            )
            StatBox(
                "Tier",
                grade?.label ?: "-",
                grade?.let { gradeColor(it) } ?: TextPrimary,
                Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(12.dp))
        // Mini pillar scores
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pillar.entries.forEach { pillar ->
                val score = entry.pillarScores[pillar] ?: 0.0
                Column(Modifier.weight(1f)) {
                    Text(
                        text = pillar.short,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = score.toInt().toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = pillar.accent,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(OutlineFaint),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth((score / 100.0).toFloat())
                                .height(4.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(pillar.accent),
                        )
                    }
                }
            }
        }

        if (entry.milestones.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            entry.milestones.forEach { milestone ->
                MilestoneRow(milestone)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold,
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
private fun MilestoneRow(milestone: Milestone) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = if (milestone.verified) Icons.Outlined.Verified else Icons.Outlined.HourglassEmpty,
            contentDescription = if (milestone.verified) "Verified" else "Pending",
            tint = if (milestone.verified) GreenGlow else TextSecondary,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
            )
            Text(
                text = buildString {
                    append(milestone.detail)
                    if (milestone.date.isNotEmpty()) append(" · ${milestone.date}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}
