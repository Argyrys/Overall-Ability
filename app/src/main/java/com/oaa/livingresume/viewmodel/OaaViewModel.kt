package com.oaa.livingresume.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.oaa.livingresume.data.auth.SessionStore
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.ProofStatus
import com.oaa.livingresume.data.model.ProofSubmission
import com.oaa.livingresume.data.model.Student
import com.oaa.livingresume.data.repository.OaaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything the screens render, produced by [OaaViewModel] (MVVM). */
data class OaaUiState(
    // --- auth gate ---
    val authLoading: Boolean = true,
    val authBusy: Boolean = false,
    val authError: String? = null,
    val signedInStudentId: String? = null,
    // --- data ---
    val loading: Boolean = true,
    val me: Student? = null,
    val batchmates: List<Student> = emptyList(),
    val submissions: List<ProofSubmission> = emptyList(),
    val error: String? = null,
    val backend: String = "Mock data",
) {
    val signedIn: Boolean get() = signedInStudentId != null

    val pendingSubmissions: List<ProofSubmission>
        get() = submissions.filter { it.status == ProofStatus.PENDING }

    val myApprovedProofs: Int
        get() = submissions.count { it.studentId == me?.id && it.status == ProofStatus.APPROVED }
}

class OaaViewModel(
    private val repository: OaaRepository,
    private val session: SessionStore,
) : ViewModel() {

    private val _state = MutableStateFlow(OaaUiState())
    val state: StateFlow<OaaUiState> = _state.asStateFlow()

    init {
        // Restore a previous session, otherwise land on the login screen.
        val saved = session.studentId
        if (saved.isNullOrBlank()) {
            _state.update { it.copy(authLoading = false) }
        } else {
            _state.update { it.copy(authLoading = false, signedInStudentId = saved) }
            refresh()
        }
    }

    // ------------------------------------------------------------------ auth

    /** Mock sign-in: validates via the repository, persists the session, loads data. */
    fun login(email: String, password: String) {
        if (email.isBlank()) {
            _state.update { it.copy(authError = "Enter your college email") }
            return
        }
        _state.update { it.copy(authBusy = true, authError = null) }
        viewModelScope.launch {
            runCatching { repository.authenticate(email, password) }
                .onSuccess { studentId ->
                    session.save(studentId)
                    _state.update {
                        it.copy(authBusy = false, signedInStudentId = studentId, authError = null)
                    }
                    refresh()
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            authBusy = false,
                            authError = e.message ?: "Sign-in failed",
                        )
                    }
                }
        }
    }

    fun logout() {
        session.clear()
        _state.value = OaaUiState(authLoading = false)
    }

    fun clearAuthError() {
        if (_state.value.authError != null) {
            _state.update { it.copy(authError = null) }
        }
    }

    // ----------------------------------------------------------------- data

    /** Reloads everything from the repository (mock or Supabase). */
    fun refresh() {
        val studentId = _state.value.signedInStudentId ?: return
        viewModelScope.launch {
            // Only show the full-screen spinner on the very first load.
            _state.update { it.copy(loading = it.me == null, error = null) }
            runCatching {
                val me = repository.loadStudent(studentId)
                val batchmates = repository.loadBatchmates(studentId)
                val submissions = repository.loadSubmissions()
                OaaUiState(
                    authLoading = false,
                    signedInStudentId = studentId,
                    loading = false,
                    me = me,
                    batchmates = batchmates,
                    submissions = submissions,
                    backend = repository::class.simpleName?.removeSuffix("OaaRepository")
                        ?.replace("Mock", "Mock data")
                        ?: "Mock data",
                )
            }.onSuccess { fresh ->
                _state.value = fresh
            }.onFailure { e ->
                _state.update { it.copy(loading = false, error = e.message ?: "Failed to load data") }
            }
        }
    }

    /** "Visible to Batch" opt-in switch on the Home screen. */
    fun toggleVisibility() {
        val me = _state.value.me ?: return
        viewModelScope.launch {
            repository.setOptedIn(me.id, !me.optedInPublic)
            refresh()
        }
    }

    /** Submit a new proof from the Share & Verify bottom sheet. */
    fun submitProof(
        pillar: Pillar,
        title: String,
        link: String,
        points: Int,
        attachment: String?,
    ) {
        val me = _state.value.me ?: return
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.submitProof(
                studentId = me.id,
                studentName = me.name,
                title = title.trim(),
                link = link.trim().ifBlank { "-" },
                pillar = pillar,
                points = points,
                attachment = attachment,
            )
            refresh()
        }
    }

    /** Approve / reject from the verifier queue - recalculates OAA scores. */
    fun decide(submissionId: String, approve: Boolean, note: String? = null) {
        viewModelScope.launch {
            repository.decide(submissionId, approve, note)
            refresh()
        }
    }
}

object OaaViewModelFactory {
    fun instance(context: Context): ViewModelProvider.Factory {
        val appContext = context.applicationContext
        val repository = OaaRepository.create()
        return viewModelFactory {
            initializer { OaaViewModel(repository, SessionStore(appContext)) }
        }
    }
}
