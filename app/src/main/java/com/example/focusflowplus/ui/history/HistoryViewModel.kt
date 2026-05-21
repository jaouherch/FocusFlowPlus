package com.example.focusflowplus.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflowplus.data.repository.SessionRepository
import com.example.focusflowplus.domain.model.FocusSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

data class HistoryUiState(
    val sessions: List<FocusSession> = emptyList(),
    val completedCount: Int = 0,
    val totalFocusMinutes: Int = 0,
    val completedToday: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class HistoryViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState

    init {
        observeHistory()
    }

    private fun observeHistory() {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        viewModelScope.launch {
            try {
                combine(
                    sessionRepository.getAllSessions(),
                    sessionRepository.getCompletedSessionCount(),
                    sessionRepository.getTotalFocusMinutes(),
                    sessionRepository.getSessionsCompletedToday(startOfDay)
                ) { sessions, completedCount, totalFocusMinutes, completedToday ->
                    HistoryUiState(
                        sessions = sessions,
                        completedCount = completedCount,
                        totalFocusMinutes = totalFocusMinutes ?: 0,
                        completedToday = completedToday,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState(
                    isLoading = false,
                    error = e.message ?: "Could not load history."
                )
            }
        }
    }
}
