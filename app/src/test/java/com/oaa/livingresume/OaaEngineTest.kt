package com.oaa.livingresume

import com.oaa.livingresume.data.mock.MockData
import com.oaa.livingresume.data.model.Grade
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.TargetRole
import com.oaa.livingresume.engine.OaaEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OaaEngineTest {

    @Test
    fun `letter grade thresholds follow the OAA rules`() {
        assertEquals(Grade.A_PLUS, OaaEngine.gradeFor(90.0))
        assertEquals(Grade.A_PLUS, OaaEngine.gradeFor(100.0))
        assertEquals(Grade.A, OaaEngine.gradeFor(89.9))
        assertEquals(Grade.A, OaaEngine.gradeFor(80.0))
        assertEquals(Grade.B_PLUS, OaaEngine.gradeFor(79.9))
        assertEquals(Grade.B_PLUS, OaaEngine.gradeFor(70.0))
        assertEquals(Grade.B, OaaEngine.gradeFor(69.9))
        assertEquals(Grade.B, OaaEngine.gradeFor(60.0))
        assertEquals(Grade.C, OaaEngine.gradeFor(50.0))
        assertEquals(Grade.D, OaaEngine.gradeFor(40.0))
        assertEquals(Grade.E, OaaEngine.gradeFor(39.9))
        assertEquals(Grade.E, OaaEngine.gradeFor(0.0))
    }

    @Test
    fun `weighted overall uses 35 30 20 15 weights`() {
        val scores = mapOf(
            Pillar.ACADEMIC to 84.0,
            Pillar.ADAPTABILITY to 76.0,
            Pillar.SOCIAL to 62.0,
            Pillar.PHYSICAL to 71.0,
        )
        // 84*.35 + 76*.30 + 62*.20 + 71*.15 = 29.4 + 22.8 + 12.4 + 10.65 = 75.25
        assertEquals(75.25, OaaEngine.overall(scores), 1e-6)
        assertEquals(Grade.B_PLUS, OaaEngine.overallGrade(scores))
    }

    @Test
    fun `points to next tier are exact and rounded up`() {
        assertEquals(4.8, OaaEngine.pointsToNextTier(75.25), 1e-6) // -> A (80)
        assertEquals(10.0, OaaEngine.pointsToNextTier(70.0), 1e-6) // B+ -> A
        assertEquals(0.0, OaaEngine.pointsToNextTier(95.0), 1e-6)  // already A+
        assertNull(OaaEngine.nextGrade(Grade.A_PLUS))
        assertEquals(Grade.A_PLUS, OaaEngine.nextGrade(Grade.A))
    }

    @Test
    fun `weakest pillar is social for the mock student`() {
        val scores = MockData.me.pillarScores
        assertEquals(Pillar.SOCIAL, OaaEngine.weakestPillar(scores))
        assertEquals(Pillar.ACADEMIC, OaaEngine.strongestPillar(scores))
    }

    @Test
    fun `quest generation is role mapped and targets the weakest pillar first`() {
        val me = MockData.me
        val plan = OaaEngine.buildImprovementPlan(me)

        assertEquals(Pillar.SOCIAL, plan.weakestPillar)
        assertTrue(plan.quests.isNotEmpty())
        // Weakest pillar gets 2 quests, the other three get 1 each => 5
        assertEquals(5, plan.quests.size)
        // Every quest maps to the target job role
        assertEquals(TargetRole.SOFTWARE_ENGINEER, plan.quests.first().targetRole)
        // The first two quests belong to the weakest pillar
        assertEquals(Pillar.SOCIAL, plan.quests[0].pillar)
        assertEquals(Pillar.SOCIAL, plan.quests[1].pillar)
        // Social 62 + 6 = 68 still B => B to B+ upgrade needs more than +6
        plan.quests.forEach { assertTrue(it.points in 1..10) }
        // Points needed for the weakest pillar to reach B+ = 8.0
        assertEquals(8.0, plan.pointsToNextTier, 1e-6)
    }

    @Test
    fun `balanced team picks distinct students covering different pillars`() {
        val team = OaaEngine.suggestTeam(MockData.batchmates, size = 3)
        assertEquals(3, team.size)
        assertEquals(3, team.distinct().size)
        assertTrue(OaaEngine.teamPillarCover(team).size == 4)
    }

    @Test
    fun `semester growth curve only includes scored semesters`() {
        val growth = OaaEngine.semesterGrowth(MockData.semesters)
        assertEquals(7, growth.size) // S1..S7 scored, S8 upcoming
        // Monotonic-ish improvement: final semester is above the first
        assertTrue(growth.last().second > growth.first().second)
    }
}
