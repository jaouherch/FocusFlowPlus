package com.example.focusflowplus.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflowplus.data.repository.SessionRepository
import com.example.focusflowplus.domain.model.FocusSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SummaryUiState(
    val session: FocusSession? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class SummaryViewModel(
    private val sessionId: Long,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getSessionById(sessionId)
                _uiState.value = if (session != null) {
                    SummaryUiState(session = session, isLoading = false)
                } else {
                    SummaryUiState(isLoading = false, error = "Session not found.")
                }
            } catch (e: Exception) {
                _uiState.value = SummaryUiState(
                    isLoading = false,
                    error = e.message ?: "Could not load summary."
                )
            }
        }
    }
}
