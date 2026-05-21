package com.example.focusflowplus.ui.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.focusflowplus.ui.components.ErrorBanner
import com.example.focusflowplus.ui.components.FocusCard
import com.example.focusflowplus.ui.components.PrimaryActionButton
import com.example.focusflowplus.ui.components.ScreenHero
import com.example.focusflowplus.ui.components.SectionTitle
import com.example.focusflowplus.ui.components.StatusPill

@Composable
fun InputScreen(
    viewModel: InputViewModel,
    onStartSession: (Long) -> Unit,
    onViewHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ScreenHero(
                title = "FocusFlow+",
                subtitle = "AI-guided focus sessions based on your energy and available time.",
                emoji = "🌿",
                trailing = {
                    OutlinedButton(
                        onClick = onViewHistory,
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("History")
                    }
                }
            )

            FocusCard {
                SectionTitle(
                    title = "What are you working on?",
                    subtitle = "Write one clear task. Gemini will turn it into a realistic session plan."
                )
                OutlinedTextField(
                    value = uiState.taskDescription,
                    onValueChange = viewModel::onTaskDescriptionChange,
                    placeholder = {
                        Text("Example: Review Kotlin Compose notes and prepare 5 revision questions")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 118.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    )
                )
                Text(
                    text = "${uiState.taskDescription.length}/500 characters",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }

            FocusCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionTitle(title = "How's your energy?")
                    StatusPill(text = energyLabel(uiState.energyLevel))
                }
                Text(
                    text = energyEmoji(uiState.energyLevel),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Slider(
                    value = uiState.energyLevel.toFloat(),
                    onValueChange = { viewModel.onEnergyLevelChange(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Low", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Balanced", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("High", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            FocusCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionTitle(title = "Available time")
                    StatusPill(text = "${uiState.availableMinutes} min")
                }
                Text(
                    text = readableDuration(uiState.availableMinutes),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = uiState.availableMinutes.toFloat(),
                    onValueChange = { viewModel.onAvailableMinutesChange(it.toInt()) },
                    valueRange = 15f..180f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimePresetButton("25m", uiState.availableMinutes == 25, Modifier.weight(1f)) { viewModel.onAvailableMinutesChange(25) }
                    TimePresetButton("45m", uiState.availableMinutes == 45, Modifier.weight(1f)) { viewModel.onAvailableMinutesChange(45) }
                    TimePresetButton("60m", uiState.availableMinutes == 60, Modifier.weight(1f)) { viewModel.onAvailableMinutesChange(60) }
                    TimePresetButton("90m", uiState.availableMinutes == 90, Modifier.weight(1f)) { viewModel.onAvailableMinutesChange(90) }
                }
            }

            AnimatedVisibility(visible = uiState.error != null) {
                ErrorBanner(message = uiState.error.orEmpty())
            }

            PrimaryActionButton(
                text = "Generate Plan & Start ✨",
                loadingText = "Asking Gemini...",
                loading = uiState.isLoading,
                enabled = !uiState.isLoading,
                onClick = {
                    viewModel.createSession { sessionId ->
                        onStartSession(sessionId)
                    }
                }
            )

            FocusCard {
                SectionTitle(
                    title = "How it works",
                    subtitle = "1. Describe your task  2. Set energy and time  3. Gemini creates a plan  4. Complete the timer and save your session."
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TimePresetButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(42.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(42.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text)
        }
    }
}

private fun energyEmoji(level: Int): String = when (level) {
    1 -> "😴"
    2 -> "😔"
    3 -> "😐"
    4 -> "😊"
    5 -> "⚡"
    else -> "😐"
}

private fun energyLabel(level: Int): String = when (level) {
    1 -> "Exhausted"
    2 -> "Tired"
    3 -> "Okay"
    4 -> "Good"
    5 -> "Energized"
    else -> "Okay"
}

private fun readableDuration(minutes: Int): String {
    return if (minutes < 60) {
        "$minutes minutes"
    } else {
        val hours = minutes / 60
        val remaining = minutes % 60
        if (remaining == 0) "$hours hour${if (hours > 1) "s" else ""}" else "$hours h $remaining min"
    }
}
