package com.example.focusflowplus.ui.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflowplus.data.repository.AiRepository
import com.example.focusflowplus.domain.model.AiRecommendation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InputUiState(
    val taskDescription: String = "",
    val energyLevel: Int = 3,
    val availableMinutes: Int = 60,
    val isLoading: Boolean = false,
    val error: String? = null,
    val recommendation: AiRecommendation? = null
)

class InputViewModel(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InputUiState())
    val uiState: StateFlow<InputUiState> = _uiState

    fun onTaskDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(taskDescription = value)
    }

    fun onEnergyLevelChange(value: Int) {
        _uiState.value = _uiState.value.copy(energyLevel = value)
    }

    fun onAvailableMinutesChange(value: Int) {
        _uiState.value = _uiState.value.copy(availableMinutes = value)
    }

    fun getRecommendation(onSuccess: (AiRecommendation) -> Unit) {
        val state = _uiState.value
        if (state.taskDescription.isBlank()) {
            _uiState.value = state.copy(error = "Please describe your task first!")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val recommendation = aiRepository.getRecommendation(
                    taskDescription = state.taskDescription,
                    energyLevel = state.energyLevel,
                    availableMinutes = state.availableMinutes
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    recommendation = recommendation
                )
                onSuccess(recommendation)
            }
            catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "HTTP ${e.code()}: $errorBody"
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "${e.javaClass.simpleName}: ${e.message}"
            )
        }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}