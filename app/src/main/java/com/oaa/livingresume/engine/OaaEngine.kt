package com.oaa.livingresume.engine

import com.oaa.livingresume.data.model.Grade
import com.oaa.livingresume.data.model.ImprovementPlan
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.Quest
import com.oaa.livingresume.data.model.SemesterEntry
import com.oaa.livingresume.data.model.Student
import com.oaa.livingresume.data.model.TargetRole
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * OAA Engine - the single source of truth for scoring.
 *
 * 4 pillars, each scored 0..100, converted to a letter grade:
 *   90-100 A+ | 80-89 A | 70-79 B+ | 60-69 B | 50-59 C | 40-49 D | <40 E
 *
 * Weights: Academic 35% | Adaptability 30% | Social 20% | Physical 15%
 */
object OaaEngine {

    // ---------------------------------------------------------------- grades

    fun gradeFor(score: Double): Grade = when {
        score >= Grade.A_PLUS.minScore -> Grade.A_PLUS
        score >= Grade.A.minScore -> Grade.A
        score >= Grade.B_PLUS.minScore -> Grade.B_PLUS
        score >= Grade.B.minScore -> Grade.B
        score >= Grade.C.minScore -> Grade.C
        score >= Grade.D.minScore -> Grade.D
        else -> Grade.E
    }

    /** The next letter tier above [grade], or null when already A+. */
    fun nextGrade(grade: Grade): Grade? = when (grade) {
        Grade.A_PLUS -> null
        Grade.A -> Grade.A_PLUS
        Grade.B_PLUS -> Grade.A
        Grade.B -> Grade.B_PLUS
        Grade.C -> Grade.B
        Grade.D -> Grade.C
        Grade.E -> Grade.D
    }

    /** Exact points still needed on a 0-100 score to reach the next letter tier (rounded up to 1 decimal). */
    fun pointsToNextTier(score: Double): Double {
        val next = nextGrade(gradeFor(score)) ?: return 0.0
        return ceil(max(0.0, next.minScore - score) * 10.0) / 10.0
    }

    /** Exact points needed to reach [target] tier from [score]. */
    fun pointsNeededFor(score: Double, target: Grade): Double =
        ceil(max(0.0, target.minScore - score) * 10.0) / 10.0

    // -------------------------------------------------------------- scoring

    /** Weighted overall OAA score: 35/30/20/15 across the four pillars. */
    fun overall(scores: Map<Pillar, Double>): Double =
        Pillar.entries.sumOf { pillar -> (scores[pillar] ?: 0.0) * pillar.weight }

    fun overallRounded(scores: Map<Pillar, Double>): Double = round1(overall(scores))

    fun overallGrade(scores: Map<Pillar, Double>): Grade = gradeFor(overall(scores))

    fun scoreOf(student: Student): Double = overall(student.pillarScores)

    fun gradeOf(student: Student): Grade = overallGrade(student.pillarScores)

    fun round1(value: Double): Double = (value * 10).roundToInt() / 10.0

    /** Pillars sorted weakest first (pillar, score). */
    fun ranked(scores: Map<Pillar, Double>): List<Pair<Pillar, Double>> =
        Pillar.entries
            .map { it to (scores[it] ?: 0.0) }
            .sortedBy { it.second }

    fun weakestPillar(scores: Map<Pillar, Double>): Pillar = ranked(scores).first().first

    /** Strongest pillar - used by the batch team finder. */
    fun strongestPillar(scores: Map<Pillar, Double>): Pillar =
        Pillar.entries.maxBy { scores[it] ?: 0.0 }

    /** Progress (0f..1f) inside the current letter tier, for progress bars. */
    fun tierProgress(score: Double): Float {
        val grade = gradeFor(score)
        val next = nextGrade(grade) ?: return 1f
        val span = next.minScore - grade.minScore
        if (span <= 0) return 1f
        return ((score - grade.minScore) / span).coerceIn(0.0, 1.0).toFloat()
    }

    /** Semester-by-semester overall growth (only semesters that have scores). */
    fun semesterGrowth(semesters: List<SemesterEntry>): List<Pair<Int, Double>> =
        semesters
            .filter { it.pillarScores.isNotEmpty() }
            .map { it.semester to overall(it.pillarScores) }

    // --------------------------------------------------------------- quests

    private data class QuestTemplate(val title: String, val detail: String, val points: Int)

    private val genericQuests: Map<Pillar, List<QuestTemplate>> = mapOf(
        Pillar.ACADEMIC to listOf(
            QuestTemplate(
                "Raise your CGPA by 0.3 this semester",
                "Pick the two subjects you score lowest in, attend every tutorial and book two 2-hour focused blocks per week until the internals.",
                6,
            ),
            QuestTemplate(
                "Earn 1 NPTEL / SWAYAM Elite certificate",
                "Complete an 8-week certified course relevant to your branch and upload the certificate for verification.",
                7,
            ),
        ),
        Pillar.ADAPTABILITY to listOf(
            QuestTemplate(
                "Ship 1 verified GitHub project",
                "Push a finished project with a proper README, setup steps and a demo GIF, then submit the repository link.",
                7,
            ),
            QuestTemplate(
                "Complete a 4-week internship or major open-source PR",
                "Ship real work for a company, startup or OSS repo and get a verifier-confirmed completion note.",
                8,
            ),
        ),
        Pillar.SOCIAL to listOf(
            QuestTemplate(
                "Take a club or fest responsibility",
                "Own one measurable deliverable (registration desk, sponsorship mail, workshop) for the semester and get it signed off.",
                6,
            ),
            QuestTemplate(
                "Log 20 hours of volunteering",
                "Volunteer with NSS/NSS-unite or a local NGO and submit photos + the coordinator's confirmation.",
                5,
            ),
        ),
        Pillar.PHYSICAL to listOf(
            QuestTemplate(
                "Cross 90% attendance this semester",
                "Reach 90%+ in every theory and lab subject before the end of term - consistency is a scored pillar.",
                5,
            ),
            QuestTemplate(
                "Keep a 4-week on-time submission streak",
                "Submit every assignment before the deadline for 4 consecutive weeks (LMS timestamps are the proof).",
                4,
            ),
        ),
    )

    /** Role-specific quests so every OAA Quest maps to the student's Target Job Role. */
    private val roleQuests: Map<TargetRole, Map<Pillar, List<QuestTemplate>>> = mapOf(
        TargetRole.SOFTWARE_ENGINEER to mapOf(
            Pillar.ACADEMIC to listOf(
                QuestTemplate(
                    "Finish 'System Design Fundamentals' NPTEL course",
                    "Complete the 8-week NPTEL course and upload the Elite certificate for verification.",
                    7,
                ),
                QuestTemplate(
                    "Ace your DSA lab viva with 90%+",
                    "Re-do all semester DSA lab experiments and defend them in a mock viva with a senior; upload the marked sheet.",
                    6,
                ),
            ),
            Pillar.ADAPTABILITY to listOf(
                QuestTemplate(
                    "Deploy 1 REST API project on GitHub",
                    "Build a Spring Boot / FastAPI REST API, write a README with the endpoint list, deploy it on Render and submit the live link (+8 Adaptability).",
                    8,
                ),
                QuestTemplate(
                    "Solve 150 DSA problems",
                    "Reach 150 solved problems on LeetCode or CodeChef and submit your public profile link for verification.",
                    7,
                ),
            ),
            Pillar.SOCIAL to listOf(
                QuestTemplate(
                    "Mentor 3 juniors for 6 weeks",
                    "Run weekly DSA doubt sessions for 3 juniors through the coding club and get the club head's confirmation.",
                    6,
                ),
                QuestTemplate(
                    "Lead a 4-member hackathon team",
                    "Register and lead a team at the next intra-college hackathon; submit the participation certificate.",
                    7,
                ),
            ),
            Pillar.PHYSICAL to listOf(
                QuestTemplate(
                    "Hit 90% attendance + a 4-week on-time streak",
                    "Attendance and deadline discipline are directly scored - prove both with LMS logs this term.",
                    5,
                ),
            ),
        ),
        TargetRole.DATA_ANALYST to mapOf(
            Pillar.ACADEMIC to listOf(
                QuestTemplate(
                    "Finish Google Data Analytics Professional Certificate",
                    "Complete the 8-course pathway and upload the final certificate for verification.",
                    8,
                ),
                QuestTemplate(
                    "Score 85%+ in Statistics & DBMS",
                    "Target the two subjects that matter most for analytics roles in your internals.",
                    6,
                ),
            ),
            Pillar.ADAPTABILITY to listOf(
                QuestTemplate(
                    "Publish 2 Kaggle notebooks",
                    "Fork a public Indian dataset (census, air quality, IPL), clean it, visualise it and publish 2 notebooks with code.",
                    8,
                ),
                QuestTemplate(
                    "Build a Power BI / Tableau dashboard",
                    "Ship an interactive dashboard on Indian census or sports data and submit the public share link.",
                    7,
                ),
            ),
            Pillar.SOCIAL to listOf(
                QuestTemplate(
                    "Host a 'Data Viz 101' workshop",
                    "Run a 90-minute hands-on workshop for 20+ students; submit the attendance sheet and slides.",
                    6,
                ),
                QuestTemplate(
                    "Start a data-club reading circle",
                    "Organise weekly paper/blog reading circles for 6 weeks with 5+ regular members.",
                    5,
                ),
            ),
            Pillar.PHYSICAL to listOf(
                QuestTemplate(
                    "90% attendance + 4-week on-time streak",
                    "Consistency is 15% of your OAA - keep both metrics green this semester.",
                    5,
                ),
            ),
        ),
        TargetRole.PRODUCT_MANAGER to mapOf(
            Pillar.ACADEMIC to listOf(
                QuestTemplate(
                    "Complete a Product Management fundamentals course",
                    "Finish an NPTEL/Coursera PM course and upload the certificate for verification.",
                    7,
                ),
                QuestTemplate(
                    "Ship a capstone PRD with 85%+ review",
                    "Write a full PRD (problem, users, requirements, metrics) and get it reviewed by a faculty mentor.",
                    6,
                ),
            ),
            Pillar.ADAPTABILITY to listOf(
                QuestTemplate(
                    "Write 3 PRDs for real problems",
                    "Draft PRDs for local problems (canteen queues, bus tracking) with user stories and success metrics; publish on Notion/GitHub.",
                    8,
                ),
                QuestTemplate(
                    "Interview 10 real users",
                    "Run discovery interviews and publish an insight summary with quotes and screenshots.",
                    7,
                ),
            ),
            Pillar.SOCIAL to listOf(
                QuestTemplate(
                    "Own a 100+ attendee fest event",
                    "Drive one tech-fest event end-to-end (budget, sponsors, crew) and submit the post-event report.",
                    8,
                ),
                QuestTemplate(
                    "Lead a cross-club initiative",
                    "Coordinate two clubs for one joint activity and get both heads to verify your role.",
                    6,
                ),
            ),
            Pillar.PHYSICAL to listOf(
                QuestTemplate(
                    "90% attendance + 4-week on-time streak",
                    "PM roles run on reliability - prove yours with attendance and deadline logs.",
                    5,
                ),
            ),
        ),
        TargetRole.ML_ENGINEER to mapOf(
            Pillar.ACADEMIC to listOf(
                QuestTemplate(
                    "Finish the ML Specialization (audit + certificate)",
                    "Complete Andrew Ng's ML Specialization (or NPTEL AI/ML) and upload the certificate.",
                    8,
                ),
                QuestTemplate(
                    "Score 85%+ in AI / ML electives",
                    "Target 85%+ in the electives recruiters screen for ML roles.",
                    6,
                ),
            ),
            Pillar.ADAPTABILITY to listOf(
                QuestTemplate(
                    "Deploy an ML model behind an API",
                    "Train a model, wrap it with FastAPI + Docker and deploy on Hugging Face Spaces or Render; submit the live link.",
                    8,
                ),
                QuestTemplate(
                    "Finish a Kaggle competition in the top 10%",
                    "Join an active competition, submit a solid notebook and screenshot your leaderboard position.",
                    7,
                ),
            ),
            Pillar.SOCIAL to listOf(
                QuestTemplate(
                    "Publish a 3-part technical blog series",
                    "Write explainers (transformers, embeddings, evaluation) for the AI club blog and share the links.",
                    6,
                ),
                QuestTemplate(
                    "Mentor juniors on ML basics",
                    "Run 4 weekly ML primer sessions for juniors and get the club head's confirmation.",
                    5,
                ),
            ),
            Pillar.PHYSICAL to listOf(
                QuestTemplate(
                    "90% attendance + 4-week on-time streak",
                    "Consistency counts for 15% of your OAA - keep the streak alive.",
                    5,
                ),
            ),
        ),
    )

    /**
     * Generates the private "OAA Quests" list.
     * The weakest pillar gets 2 quests, the remaining pillars get 1 each,
     * always preferring role-mapped templates for [Student.targetRole].
     */
    fun generateQuests(student: Student, scores: Map<Pillar, Double> = student.pillarScores): List<Quest> {
        val pools: Map<Pillar, List<QuestTemplate>> = Pillar.entries.associateWith { pillar ->
            (roleQuests[student.targetRole]?.get(pillar).orEmpty()) + genericQuests.getValue(pillar)
        }

        return ranked(scores).mapIndexed { index, (pillar, score) ->
            val pool = pools.getValue(pillar)
            val take = if (index == 0) 2 else 1
            (0 until take).map { offset ->
                val template = pool[(index + offset) % pool.size]
                val projected = min(100.0, score + template.points)
                val from = gradeFor(score)
                val to = gradeFor(projected)
                Quest(
                    id = "quest_${pillar.name}_${index}_$offset",
                    title = template.title,
                    detail = template.detail,
                    pillar = pillar,
                    points = template.points,
                    upgradeFrom = from,
                    upgradeTo = to,
                    tierUpgrade = from != to,
                    targetRole = student.targetRole,
                )
            }
        }.flatten()
    }

    /** Full plan consumed by the private Improve screen. */
    fun buildImprovementPlan(student: Student): ImprovementPlan {
        val scores = student.pillarScores
        val weakest = weakestPillar(scores)
        val weakestScore = scores[weakest] ?: 0.0
        val overallScore = round1(overall(scores))
        val overallGrade = gradeFor(overallScore)
        return ImprovementPlan(
            weakestPillar = weakest,
            weakestScore = round1(weakestScore),
            weakestGrade = gradeFor(weakestScore),
            pointsToNextTier = pointsToNextTier(weakestScore),
            nextTierGrade = nextGrade(gradeFor(weakestScore)),
            overallScore = overallScore,
            overallGrade = overallGrade,
            overallPointsToNextTier = pointsToNextTier(overallScore),
            nextOverallGrade = nextGrade(overallGrade),
            quests = generateQuests(student, scores),
        )
    }

    // ----------------------------------------------------------- team finder

    /**
     * Picks a balanced hackathon team from [candidates]: greedily takes the best
     * available classmate for each pillar in weight order (deduplicated).
     */
    fun suggestTeam(candidates: List<Student>, size: Int = 3): List<Student> {
        val chosen = linkedSetOf<Student>()
        for (pillar in Pillar.entries) {
            if (chosen.size >= size) break
            candidates
                .filterNot { it in chosen }
                .maxByOrNull { it.pillarScores[pillar] ?: 0.0 }
                ?.let { chosen += it }
        }
        return chosen.toList()
    }

    /** For a selected team, the member who covers each pillar best. */
    fun teamPillarCover(team: List<Student>): List<Pair<Pillar, Student?>> =
        Pillar.entries.map { pillar ->
            pillar to team.maxByOrNull { it.pillarScores[pillar] ?: 0.0 }
        }
}
