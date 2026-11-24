package com.oaa.livingresume.data.repository

import com.oaa.livingresume.BuildConfig
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.ProofSubmission
import com.oaa.livingresume.data.model.Student
import com.oaa.livingresume.data.supabase.SupabaseOaaRepository

/**
 * Repository pattern: the UI/ViewModel only ever talks to this interface.
 * [MockOaaRepository] ships pre-populated with realistic Indian-college data so the
 * app runs immediately; [SupabaseOaaRepository] (Postgrest + Auth + Storage) is used
 * automatically as soon as SUPABASE_URL / SUPABASE_KEY are set in local.properties.
 */
interface OaaRepository {

    /**
     * Mock sign-in: validates credentials against the student roster and returns
     * the signed-in student id. (Supabase impl delegates to Auth.)
     */
    suspend fun authenticate(email: String, password: String): String

    /** The signed-in student's full profile (OAA scores, timeline, badges). */
    suspend fun loadStudent(studentId: String): Student

    /** Classmates who opted into public batch visibility (leaderboard + team finder). */
    suspend fun loadBatchmates(excludeId: String): List<Student>

    /** All proof submissions - pending ones feed the verifier approval queue. */
    suspend fun loadSubmissions(): List<ProofSubmission>

    /** Submit a new achievement for verification (starts as PENDING). */
    suspend fun submitProof(
        studentId: String,
        studentName: String,
        title: String,
        link: String,
        pillar: Pillar,
        points: Int,
        attachment: String?,
    ): ProofSubmission

    /**
     * Approve or reject a submission. Approving automatically recalculates the
     * student's pillar score (capped at 100) and therefore the OAA grade.
     * Returns the updated submission, or null if it was not pending.
     */
    suspend fun decide(submissionId: String, approve: Boolean, note: String?): ProofSubmission?

    /** Toggle "Visible to Batch" opt-in. Returns the updated student, or null. */
    suspend fun setOptedIn(studentId: String, visible: Boolean): Student?

    companion object {
        /** Mock data by default; Supabase as soon as credentials exist. */
        fun create(): OaaRepository =
            if (BuildConfig.SUPABASE_URL.isBlank()) MockOaaRepository
            else SupabaseOaaRepository()
    }
}
