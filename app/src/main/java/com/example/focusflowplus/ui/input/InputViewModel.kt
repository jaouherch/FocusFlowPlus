package com.example.focusflowplus.ui.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflowplus.data.repository.AiRepository
import com.example.focusflowplus.data.repository.SessionRepository
import com.example.focusflowplus.domain.model.AiRecommendation
import com.example.focusflowplus.domain.model.FocusSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val MAX_TASK_LENGTH = 500

data class InputUiState(
    val taskDescription: String = "",
    val energyLevel: Int = 3,
    val availableMinutes: Int = 60,
    val isLoading: Boolean = false,
    val error: String? = null,
    val recommendation: AiRecommendation? = null
)

class InputViewModel(
    private val aiRepository: AiRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InputUiState())
    val uiState: StateFlow<InputUiState> = _uiState

    fun onTaskDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(
            taskDescription = value.take(MAX_TASK_LENGTH),
            error = null
        )
    }

    fun onEnergyLevelChange(value: Int) {
        _uiState.value = _uiState.value.copy(
            energyLevel = value.coerceIn(1, 5),
            error = null
        )
    }

    fun onAvailableMinutesChange(value: Int) {
        val rounded = ((value + 4) / 5) * 5
        _uiState.value = _uiState.value.copy(
            availableMinutes = rounded.coerceIn(15, 180),
            error = null
        )
    }

    fun createSession(onSuccess: (Long) -> Unit) {
        val state = _uiState.value
        val task = state.taskDescription.trim()

        if (task.isBlank()) {
            _uiState.value = state.copy(error = "Please describe your task first.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val recommendation = aiRepository.getRecommendation(
                    taskDescription = task,
                    energyLevel = state.energyLevel,
                    availableMinutes = state.availableMinutes
                )

                val sessionId = sessionRepository.saveSession(
                    FocusSession(
                        taskDescription = task,
                        energyLevel = state.energyLevel,
                        availableMinutes = state.availableMinutes,
                        focusMinutes = recommendation.focusMinutes,
                        breakMinutes = recommendation.breakMinutes,
                        wasCompleted = false,
                        aiRecommendation = recommendation.toReadableText()
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    recommendation = recommendation,
                    error = null
                )
                onSuccess(sessionId)
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string().orEmpty()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Gemini request failed: HTTP ${e.code()} ${errorBody.take(250)}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Something went wrong while creating your session."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun AiRecommendation.toReadableText(): String {
        return buildString {
            appendLine("Focus: $focusMinutes min")
            appendLine("Break: $breakMinutes min")
            appendLine("Structure: $sessionStructure")
            appendLine("Tips:")
            healthTips.forEach { appendLine("- $it") }
            appendLine("Motivation: $motivationalMessage")
            if (rawResponse.isNotBlank()) {
                appendLine()
                appendLine("Raw Gemini response:")
                append(rawResponse)
            }
        }
    }
}
