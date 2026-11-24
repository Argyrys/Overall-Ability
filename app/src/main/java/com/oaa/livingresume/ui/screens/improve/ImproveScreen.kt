package com.oaa.livingresume.ui.screens.improve

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
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
import androidx.compose.ui.unit.dp
import com.oaa.livingresume.data.model.ImprovementPlan
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.Quest
import com.oaa.livingresume.data.model.Student
import com.oaa.livingresume.engine.OaaEngine
import com.oaa.livingresume.ui.components.Eyebrow
import com.oaa.livingresume.ui.components.GradeBadge
import com.oaa.livingresume.ui.components.HudCard
import com.oaa.livingresume.ui.components.PillarTag
import com.oaa.livingresume.ui.components.ScoreProgressBar
import com.oaa.livingresume.ui.components.SectionHeader
import com.oaa.livingresume.ui.motion.entrance
import com.oaa.livingresume.ui.theme.CyanGlow
import com.oaa.livingresume.ui.theme.DangerRed
import com.oaa.livingresume.ui.theme.GoldGlow
import com.oaa.livingresume.ui.theme.NavyCard
import com.oaa.livingresume.ui.theme.OutlineFaint
import com.oaa.livingresume.ui.theme.TextPrimary
import com.oaa.livingresume.ui.theme.TextSecondary
import com.oaa.livingresume.ui.theme.accent
import com.oaa.livingresume.ui.theme.gradeColor
import com.oaa.livingresume.viewmodel.OaaViewModel

/**
 * Tab 3 - Improve: STRICTLY PRIVATE to the logged-in student.
 * Identifies the lowest-scoring pillar, calculates the exact points needed for the
 * next letter tier and generates actionable "OAA Quests" mapped to the target job role.
 */
@Composable
fun ImproveScreen(
    viewModel: OaaViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val me = state.me ?: return
    val plan = remember(me.pillarScores, me.targetRole) { OaaEngine.buildImprovementPlan(me) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Box(Modifier.fillMaxWidth().entrance(0)) { PrivateHeader() } }
        item { Box(Modifier.fillMaxWidth().entrance(1)) { WeakestPillarCard(plan) } }
        item { Box(Modifier.fillMaxWidth().entrance(2)) { OverallTierCard(plan) } }
        item {
            Box(Modifier.fillMaxWidth().entrance(3)) {
                SectionHeader(
                    title = "OAA Quests",
                    subtitle = "Generated for your target role: ${me.targetRole.label}",
                )
            }
        }
        items(plan.quests, key = { it.id }) { quest ->
            Box(Modifier.fillMaxWidth().entrance(4)) { QuestCard(quest) }
        }
        item {
            Box(Modifier.fillMaxWidth().entrance(5)) {
                Text(
                    text = "Quests recalculate every time your scores change. " +
                        "Approved proofs on the Share tab boost these pillars instantly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Private banner - this page is never exposed to recruiters or classmates. */
@Composable
private fun PrivateHeader() {
    val shape = androidx.compose.foundation.shape.CutCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GoldGlow.copy(alpha = 0.10f))
            .border(1.dp, GoldGlow.copy(alpha = 0.45f), shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Lock, contentDescription = null, tint = GoldGlow, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "非公開クエスト · Private quest log",
                style = MaterialTheme.typography.titleSmall,
                color = GoldGlow,
            )
            Text(
                text = "Visible only to you. Recruiters and classmates never see this page.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

/** Lowest pillar + exact points needed to reach the next letter tier. */
@Composable
private fun WeakestPillarCard(plan: ImprovementPlan) {
    val accent = plan.weakestPillar.accent
    HudCard(borderColor = accent.copy(alpha = 0.5f)) {
        Eyebrow("Lowest scoring pillar", color = accent)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = plan.weakestPillar.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Weighted ${plan.weakestPillar.weightPercent}% of your overall OAA",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.width(10.dp))
            GradeBadge(plan.weakestGrade, large = true)
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${plan.weakestScore}",
                style = MaterialTheme.typography.headlineMedium,
                color = accent,
            )
            Text(
                text = " / 100",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.weight(1f))
            if (plan.nextTierGrade != null) {
                Text(
                    text = "${plan.pointsToNextTier} pts to ${plan.nextTierGrade.label}",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoldGlow,
                )
            } else {
                Text("Max tier", style = MaterialTheme.typography.titleSmall, color = GoldGlow)
            }
        }
        Spacer(Modifier.height(8.dp))
        ScoreProgressBar(plan.weakestScore, accent, height = 10.dp)
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (plan.nextTierGrade != null) {
                "Earn ${plan.pointsToNextTier} more points in ${plan.weakestPillar.short} " +
                    "to upgrade ${plan.weakestGrade.label} -> ${plan.nextTierGrade.label}."
            } else {
                "${plan.weakestPillar.short} is already at the top tier A+ - protect it."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

/** Overall tier card - how far the weighted score is from the next tier. */
@Composable
private fun OverallTierCard(plan: ImprovementPlan) {
    HudCard(borderColor = gradeColor(plan.overallGrade).copy(alpha = 0.5f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Eyebrow("Overall OAA")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${plan.overallScore} / 100",
                    style = MaterialTheme.typography.titleLarge,
                    color = gradeColor(plan.overallGrade),
                )
            }
            GradeBadge(plan.overallGrade, large = true)
        }
        Spacer(Modifier.height(10.dp))
        ScoreProgressBar(plan.overallScore, gradeColor(plan.overallGrade), height = 8.dp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (plan.nextOverallGrade != null) {
                "${plan.overallPointsToNextTier} more points to reach ${plan.nextOverallGrade.label} " +
                    "(${plan.nextOverallGrade.minScore.toInt()}) - quests below are your fastest route."
            } else {
                "You are in the highest letter tier. Hold the line above 90."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

/** One actionable OAA Quest card. */
@Composable
private fun QuestCard(quest: Quest) {
    val accent = quest.pillar.accent
    HudCard(
        borderColor = accent.copy(alpha = 0.45f),
        background = NavyCard,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PillarTag(quest.pillar)
            Spacer(Modifier.weight(1f))
            Text(
                text = "+${quest.points} ${quest.pillar.short}",
                style = MaterialTheme.typography.labelLarge,
                color = accent,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = quest.title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = quest.detail,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (quest.tierUpgrade) {
                UpgradeChip("${quest.upgradeFrom.label} -> ${quest.upgradeTo.label}")
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Improves ${quest.pillar.short} score",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LockOpen,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Private",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun UpgradeChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(GoldGlow.copy(alpha = 0.12f))
            .border(1.dp, GoldGlow.copy(alpha = 0.55f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = GoldGlow,
            fontWeight = FontWeight.Bold,
        )
    }
}
