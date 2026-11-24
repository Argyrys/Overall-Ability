package com.oaa.livingresume.data.repository

import com.oaa.livingresume.data.mock.MockData
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.ProofStatus
import com.oaa.livingresume.data.model.ProofSubmission
import com.oaa.livingresume.data.model.Student
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * In-memory repository backed by [MockData]. Every mutation (submit / approve /
 * reject / opt-in) is applied immediately so scores, grades, radar charts and the
 * leaderboard all recalculate live - no backend required.
 *
 * Access happens on the ViewModel's Main dispatcher, so plain mutable lists are safe.
 */
object MockOaaRepository : OaaRepository {

    private val students: MutableList<Student> = MockData.allStudents.toMutableList()
    private val submissions: MutableList<ProofSubmission> = MockData.submissions.toMutableList()

    private val stamp: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    /** Mock auth: any known email + password of 4+ characters signs that student in. */
    override suspend fun authenticate(email: String, password: String): String {
        val student = students.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
            ?: throw IllegalArgumentException("No student account found for that email")
        if (password.length < 4) throw IllegalArgumentException("Password must be at least 4 characters")
        return student.id
    }

    override suspend fun loadStudent(studentId: String): Student =
        students.firstOrNull { it.id == studentId }
            ?: throw IllegalStateException("Student profile not found")

    override suspend fun loadBatchmates(excludeId: String): List<Student> =
        students.filter { it.optedInPublic && it.id != excludeId }

    override suspend fun loadSubmissions(): List<ProofSubmission> =
        submissions.sortedWith(
            compareByDescending<ProofSubmission> { it.status == ProofStatus.PENDING }
                .thenByDescending { it.submittedAt }
        )

    override suspend fun submitProof(
        studentId: String,
        studentName: String,
        title: String,
        link: String,
        pillar: Pillar,
        points: Int,
        attachment: String?,
    ): ProofSubmission {
        val submission = ProofSubmission(
            id = "sub_${System.currentTimeMillis()}",
            studentId = studentId,
            studentName = studentName,
            title = title,
            link = link,
            pillar = pillar,
            points = points,
            status = ProofStatus.PENDING,
            submittedAt = LocalDateTime.now().format(stamp),
            attachment = attachment,
        )
        submissions.add(0, submission)
        return submission
    }

    override suspend fun decide(submissionId: String, approve: Boolean, note: String?): ProofSubmission? {
        val index = submissions.indexOfFirst { it.id == submissionId }
        if (index < 0) return null
        val current = submissions[index]
        if (current.status != ProofStatus.PENDING) return null

        val decided = current.copy(
            status = if (approve) ProofStatus.APPROVED else ProofStatus.REJECTED,
            verifierNote = note,
        )
        submissions[index] = decided
        if (approve) applyScore(current.studentId, current.pillar, current.points)
        return decided
    }

    override suspend fun setOptedIn(studentId: String, visible: Boolean): Student? {
        val index = students.indexOfFirst { it.id == studentId }
        if (index < 0) return null
        val updated = students[index].copy(optedInPublic = visible)
        students[index] = updated
        return updated
    }

    /** Approving a proof adds its points to the pillar (capped at 100) -> new weighted OAA. */
    private fun applyScore(studentId: String, pillar: Pillar, points: Int) {
        val index = students.indexOfFirst { it.id == studentId }
        if (index < 0) return
        val student = students[index]
        val scores = student.pillarScores.toMutableMap()
        val current = scores[pillar] ?: 0.0
        scores[pillar] = (current + points).coerceAtMost(100.0)
        students[index] = student.copy(pillarScores = scores)
    }
}
