package com.oaa.livingresume.data.supabase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.oaa.livingresume.BuildConfig
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.ProofStatus
import com.oaa.livingresume.data.model.ProofSubmission
import com.oaa.livingresume.data.model.Student
import com.oaa.livingresume.data.repository.OaaRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.*
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Backend-ready Supabase implementation (Postgrest + Auth + Storage).
 *
 * Used automatically when SUPABASE_URL / SUPABASE_KEY are present in local.properties.
 * Expected tables (see README for the SQL):
 *   students(id text pk, name, roll_no, branch, college, grad_year, target_role, email,
 *            slug, cgpa, pillar_scores jsonb, badges jsonb, semesters jsonb,
 *            opted_in_public boolean, headline, skills jsonb, github, linkedin)
 *   proofs(id text pk, student_id, student_name, title, link, pillar, points,
 *          status, submitted_at, attachment, verifier_note)
 * Storage bucket: "proofs" (public read for verifier links).
 *
 * On approval the score recalculation is performed here as a read-modify-write
 * (or move it into a Postgres function / trigger for multi-client use).
 */
class SupabaseOaaRepository : OaaRepository {

    private val client: SupabaseClient by lazy {
        createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) {
            install(Postgrest)
            install(Auth)
            install(Storage)
        }
    }

    // ---------------------------------------------------------------- reads

    override suspend fun authenticate(email: String, password: String): String {
        val ok = signIn(email, password)
        if (!ok) throw IllegalArgumentException("Invalid email or password")
        return client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Sign-in succeeded but no session was created")
    }

    override suspend fun loadStudent(studentId: String): Student =
        client.from(STUDENTS)
            .select { filter { eq("id", studentId) } }
            .decodeList<Student>()
            .firstOrNull()
            ?: error("Student profile not found")

    override suspend fun loadBatchmates(excludeId: String): List<Student> =
        client.from(STUDENTS)
            .select { filter { eq("opted_in_public", true) } }
            .decodeList<Student>()
            .filter { it.id != excludeId }

    override suspend fun loadSubmissions(): List<ProofSubmission> =
        client.from(PROOFS).select().decodeList<ProofSubmission>()

    // --------------------------------------------------------------- writes

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
            id = java.util.UUID.randomUUID().toString(),
            studentId = studentId,
            studentName = studentName,
            title = title,
            link = link,
            pillar = pillar,
            points = points,
            status = ProofStatus.PENDING,
            submittedAt = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")),
            attachment = attachment,
        )
        client.from(PROOFS).insert(submission)
        return submission
    }

    override suspend fun decide(submissionId: String, approve: Boolean, note: String?): ProofSubmission? {
        val pending = client.from(PROOFS)
            .select { filter { eq("id", submissionId) } }
            .decodeList<ProofSubmission>()
            .firstOrNull() ?: return null
        if (pending.status != ProofStatus.PENDING) return null

        val decided = pending.copy(
            status = if (approve) ProofStatus.APPROVED else ProofStatus.REJECTED,
            verifierNote = note,
        )
        client.from(PROOFS).update(
            ProofStatusPatch(status = decided.status.name, verifierNote = note)
        ) { filter { eq("id", submissionId) } }

        if (approve) applyScore(pending.studentId, pending.pillar, pending.points)
        return decided
    }

    override suspend fun setOptedIn(studentId: String, visible: Boolean): Student? {
        client.from(STUDENTS).update(VisibilityPatch(visible)) { filter { eq("id", studentId) } }
        return client.from(STUDENTS)
            .select { filter { eq("id", studentId) } }
            .decodeList<Student>()
            .firstOrNull()
    }

    /** Read-modify-write recalculation of a pillar score after an approval. */
    private suspend fun applyScore(studentId: String, pillar: Pillar, points: Int) {
        val student = client.from(STUDENTS)
            .select { filter { eq("id", studentId) } }
            .decodeList<Student>()
            .firstOrNull() ?: return
        val scores = student.pillarScores.toMutableMap()
        scores[pillar] = ((scores[pillar] ?: 0.0) + points).coerceAtMost(100.0)
        client.from(STUDENTS).update(StudentScoresPatch(scores)) { filter { eq("id", studentId) } }
    }

    // ---------------------------------------------------------- auth/storage

    /** Sign in with Supabase Auth (email + password). Returns true on success. */
    suspend fun signIn(email: String, password: String): Boolean = try {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        true
    } catch (e: Exception) {
        false
    }

    suspend fun signOut() {
        runCatching { client.auth.signOut() }
    }

    /** Uploads an attached certificate to the public "proofs" bucket, returns its public URL. */
    suspend fun uploadCertificate(context: Context, uri: Uri, fileName: String): String? =
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null
            val path = "certificates/${System.currentTimeMillis()}_$fileName"
            val bucket = client.storage.from("proofs")
            bucket.upload(path, bytes) { upsert = false }
            bucket.publicUrl(path)
        }.getOrNull()

    companion object {
        private const val STUDENTS = "students"
        private const val PROOFS = "proofs"
    }
}

@Serializable
private data class ProofStatusPatch(
    @SerialName("status") val status: String,
    @SerialName("verifier_note") val verifierNote: String? = null,
)

@Serializable
private data class VisibilityPatch(@SerialName("opted_in_public") val visible: Boolean)

@Serializable
private data class StudentScoresPatch(
    @SerialName("pillar_scores") val pillarScores: Map<Pillar, Double>,
)

/** Resolve a picked document's display name for the proof form. */
fun displayNameOf(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) return cursor.getString(index) ?: "certificate"
            }
        }
    return uri.lastPathSegment ?: "certificate"
}
