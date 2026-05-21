package com.example.focusflowplus.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflowplus.data.repository.SessionRepository
import com.example.focusflowplus.domain.model.FocusSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class SessionPhase {
    Focus,
    Break,
    Completed
}

data class SessionUiState(
    val session: FocusSession? = null,
    val phase: SessionPhase = SessionPhase.Focus,
    val secondsRemaining: Int = 0,
    val totalPhaseSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class SessionViewModel(
    private val sessionId: Long,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState

    private var timerJob: Job? = null

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getSessionById(sessionId)
                if (session == null) {
                    _uiState.value = SessionUiState(
                        isLoading = false,
                        error = "Session not found."
                    )
                    return@launch
                }

                val focusSeconds = (session.focusMinutes * 60).coerceAtLeast(1)
                _uiState.value = SessionUiState(
                    session = session,
                    phase = if (session.wasCompleted) SessionPhase.Completed else SessionPhase.Focus,
                    secondsRemaining = if (session.wasCompleted) 0 else focusSeconds,
                    totalPhaseSeconds = focusSeconds,
                    isRunning = false,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = SessionUiState(
                    isLoading = false,
                    error = e.message ?: "Could not load session."
                )
            }
        }
    }

    fun toggleTimer() {
        val state = _uiState.value
        if (state.phase == SessionPhase.Completed || state.session == null) return

        if (state.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun resetPhase() {
        val state = _uiState.value
        if (state.phase == SessionPhase.Completed) return
        pauseTimer()
        _uiState.value = state.copy(
            secondsRemaining = state.totalPhaseSeconds,
            isRunning = false
        )
    }

    fun skipToNextPhaseOrFinish() {
        val state = _uiState.value
        when (state.phase) {
            SessionPhase.Focus -> startBreakOrComplete()
            SessionPhase.Break -> completeSession()
            SessionPhase.Completed -> Unit
        }
    }

    fun completeSession() {
        val state = _uiState.value
        val session = state.session ?: return
        timerJob?.cancel()
        timerJob = null

        viewModelScope.launch {
            val completed = session.copy(
                completedAt = System.currentTimeMillis(),
                wasCompleted = true
            )
            sessionRepository.updateSession(completed)
            _uiState.value = _uiState.value.copy(
                session = completed,
                phase = SessionPhase.Completed,
                secondsRemaining = 0,
                totalPhaseSeconds = 1,
                isRunning = false
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isRunning = true)
        timerJob = viewModelScope.launch {
            while (_uiState.value.secondsRemaining > 0 && _uiState.value.isRunning) {
                delay(1000)
                val current = _uiState.value
                if (!current.isRunning) break
                _uiState.value = current.copy(secondsRemaining = (current.secondsRemaining - 1).coerceAtLeast(0))
            }

            val endState = _uiState.value
            if (endState.isRunning && endState.secondsRemaining == 0) {
                when (endState.phase) {
                    SessionPhase.Focus -> startBreakOrComplete(autoStartBreak = false)
                    SessionPhase.Break -> completeSession()
                    SessionPhase.Completed -> Unit
                }
            }
        }
    }

    private fun startBreakOrComplete(autoStartBreak: Boolean = false) {
        val session = _uiState.value.session ?: return
        timerJob?.cancel()
        timerJob = null

        if (session.breakMinutes <= 0) {
            completeSession()
            return
        }

        val breakSeconds = (session.breakMinutes * 60).coerceAtLeast(1)
        _uiState.value = _uiState.value.copy(
            phase = SessionPhase.Break,
            secondsRemaining = breakSeconds,
            totalPhaseSeconds = breakSeconds,
            isRunning = false
        )

        if (autoStartBreak) startTimer()
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
