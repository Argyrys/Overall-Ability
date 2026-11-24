package com.oaa.livingresume.ui.screens.batch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.Student
import com.oaa.livingresume.engine.OaaEngine
import com.oaa.livingresume.ui.components.Eyebrow
import com.oaa.livingresume.ui.components.GradeBadge
import com.oaa.livingresume.ui.components.HudCard
import com.oaa.livingresume.ui.components.InitialsAvatar
import com.oaa.livingresume.ui.components.PillarTag
import com.oaa.livingresume.ui.components.SectionHeader
import com.oaa.livingresume.ui.motion.entrance
import com.oaa.livingresume.ui.theme.CyanGlow
import com.oaa.livingresume.ui.theme.GoldGlow
import com.oaa.livingresume.ui.theme.NavyCard
import com.oaa.livingresume.ui.theme.OutlineFaint
import com.oaa.livingresume.ui.theme.TextPrimary
import com.oaa.livingresume.ui.theme.TextSecondary
import com.oaa.livingresume.ui.theme.accent
import com.oaa.livingresume.viewmodel.OaaViewModel

private enum class BatchFilter(val label: String) {
    ALL("All"),
    ADAPTABILITY("High Adaptability"),
    SOCIAL("High Social"),
    ACADEMIC("High Academic"),
    CONSISTENCY("High Consistency");

    fun matches(student: Student): Boolean {
        val scores = student.pillarScores
        return when (this) {
            ALL -> true
            ADAPTABILITY -> (scores[Pillar.ADAPTABILITY] ?: 0.0) >= 80.0
            SOCIAL -> (scores[Pillar.SOCIAL] ?: 0.0) >= 80.0
            ACADEMIC -> (scores[Pillar.ACADEMIC] ?: 0.0) >= 80.0
            CONSISTENCY -> (scores[Pillar.PHYSICAL] ?: 0.0) >= 80.0
        }
    }
}

private const val TEAM_SIZE_LIMIT = 4

/**
 * Tab 4 - Opt-in batch leaderboard & hackathon team finder.
 * Only classmates who enabled "Visible to Batch" appear, and only their
 * LETTER GRADE is shown - raw marks are never displayed.
 */
@Composable
fun BatchScreen(
    viewModel: OaaViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val me = state.me ?: return

    var filter by remember { mutableStateOf(BatchFilter.ALL) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    val ranked = remember(state.batchmates, me) {
        val pool = if (me.optedInPublic) state.batchmates + me else state.batchmates
        pool.sortedByDescending { OaaEngine.overall(it.pillarScores) }
    }
    val filtered = remember(ranked, filter) { ranked.filter { filter.matches(it) } }
    val selected = remember(ranked, selectedIds) { ranked.filter { it.id in selectedIds } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(Modifier.fillMaxWidth().entrance(0)) {
                Eyebrow("OAA // 成績表 Batch Ranking")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Batch Leaderboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${ranked.size} classmates opted in · letter grades only, marks stay private",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        if (!me.optedInPublic) {
            item {
                Box(Modifier.fillMaxWidth().entrance(1)) {
                    HudCard(borderColor = GoldGlow.copy(alpha = 0.45f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.VisibilityOff, contentDescription = null, tint = GoldGlow, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "You are hidden from the batch. Turn on \"Visible to Batch\" on the Home tab to appear here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        }

        // Filter chips (horizontally scrollable)
        item {
            Box(Modifier.fillMaxWidth().entrance(2)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BatchFilter.entries.forEach { f ->
                        FilterPill(
                            label = f.label,
                            selected = filter == f,
                            onClick = { filter = f },
                        )
                    }
                }
            }
        }

        // Team actions
        item {
            Box(Modifier.fillMaxWidth().entrance(3)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = {
                        selectedIds = OaaEngine.suggestTeam(ranked, size = 3).map { it.id }.toSet()
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow.copy(alpha = 0.5f)),
                ) {
                    Icon(Icons.Outlined.Groups, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Auto-balance team", color = CyanGlow)
                }
                Spacer(Modifier.weight(1f))
                if (selectedIds.isNotEmpty()) {
                    TextButton(onClick = { selectedIds = emptySet() }) {
                        Text("Clear (${selected.size})", color = TextSecondary)
                    }
                }
                }
            }
        }

        if (selected.isNotEmpty()) {
            item { Box(Modifier.fillMaxWidth().entrance(4)) { TeamSummaryCard(selected) } }
        }

        if (filtered.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().entrance(4)) {
                    HudCard {
                        Text(
                            text = "No opted-in classmates match this filter yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { student ->
            val rank = ranked.indexOfFirst { it.id == student.id } + 1
            Box(Modifier.fillMaxWidth().entrance(5)) {
                ClassmateRow(
                    student = student,
                    rank = rank,
                    isMe = student.id == me.id,
                    selected = student.id in selectedIds,
                    onToggle = {
                        selectedIds = if (student.id in selectedIds) {
                            selectedIds - student.id
                        } else if (selectedIds.size < TEAM_SIZE_LIMIT) {
                            selectedIds + student.id
                        } else {
                            selectedIds
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) CyanGlow else TextSecondary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) CyanGlow.copy(alpha = 0.14f) else NavyCard)
            .border(1.dp, if (selected) CyanGlow.copy(alpha = 0.6f) else OutlineFaint, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

/** Leaderboard row - letter grade only (no raw marks), tap to add to the team. */
@Composable
private fun ClassmateRow(
    student: Student,
    rank: Int,
    isMe: Boolean,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    val grade = OaaEngine.gradeOf(student)
    val strongest = OaaEngine.strongestPillar(student.pillarScores)
    // Podium tint: gold / silver / bronze for the top 3 of the batch
    val rankColor = when (rank) {
        1 -> GoldGlow
        2 -> androidx.compose.ui.graphics.Color(0xFFC9D2E3)
        3 -> androidx.compose.ui.graphics.Color(0xFFE0955A)
        else -> TextSecondary
    }
    val borderColor = when {
        selected -> CyanGlow.copy(alpha = 0.7f)
        rank <= 3 -> rankColor.copy(alpha = 0.55f)
        else -> OutlineFaint
    }

    HudCard(
        borderColor = borderColor,
        background = if (selected) {
            CyanGlow.copy(alpha = 0.06f)
        } else if (rank <= 3) {
            rankColor.copy(alpha = 0.05f)
        } else {
            NavyCard
        },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onToggle)) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (rank <= 3) 20.sp else 14.sp,
                color = rankColor,
                modifier = Modifier.width(46.dp),
            )
            InitialsAvatar(name = student.name, color = strongest.accent, size = 40.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (isMe) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(GoldGlow.copy(alpha = 0.15f))
                                .border(1.dp, GoldGlow.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 7.dp, vertical = 1.dp),
                        ) {
                            Text("You", style = MaterialTheme.typography.labelSmall, color = GoldGlow)
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "${student.rollNo} · ${student.branch}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(5.dp))
                PillarTag(strongest, showWeight = false)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GradeBadge(grade)
                if (rank <= 3) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "第${rank}位",
                        style = MaterialTheme.typography.labelSmall,
                        color = rankColor,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (selected) CyanGlow else androidx.compose.ui.graphics.Color.Transparent)
                        .border(1.4.dp, if (selected) CyanGlow else OutlineFaint, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = "Selected",
                            tint = com.oaa.livingresume.ui.theme.DeepNavy,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Balanced-team summary: who covers which pillar best. */
@Composable
private fun TeamSummaryCard(team: List<Student>) {
    HudCard(borderColor = CyanGlow.copy(alpha = 0.45f)) {
        SectionHeader(
            title = "Your hackathon team (${team.size})",
            subtitle = "Pillar coverage - each strength is covered by your best member",
        )
        Spacer(Modifier.height(10.dp))
        team.forEach { member ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                InitialsAvatar(name = member.name, color = OaaEngine.strongestPillar(member.pillarScores).accent, size = 26.dp)
                Spacer(Modifier.width(8.dp))
                Text(member.name, style = MaterialTheme.typography.labelLarge, color = TextPrimary, modifier = Modifier.weight(1f))
                GradeBadge(OaaEngine.gradeOf(member))
            }
        }
        Spacer(Modifier.height(8.dp))
        OaaEngine.teamPillarCover(team).forEach { (pillar, member) ->
            val pillarScore = member?.pillarScores?.get(pillar) ?: 0.0
            val tier = if (member != null) OaaEngine.gradeFor(pillarScore).label else "-"
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(pillar.accent)
                        .align(Alignment.CenterVertically),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${pillar.short} covered by ${member?.name ?: "-"} · $tier",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}
