package com.example.focusflowplus.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.focusflowplus.ui.components.FocusCard
import com.example.focusflowplus.ui.components.PrimaryActionButton
import com.example.focusflowplus.ui.components.ScreenHero
import com.example.focusflowplus.ui.components.SectionTitle
import com.example.focusflowplus.ui.components.StatTile
import com.example.focusflowplus.ui.components.StatusPill

@Composable
fun SessionScreen(
    viewModel: SessionViewModel,
    onBack: () -> Unit,
    onFinish: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.session
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(uiState.error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onBack) { Text("Back") }
                }
            }

            session != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    ScreenHero(
                        title = phaseTitle(uiState.phase),
                        subtitle = session.taskDescription,
                        emoji = phaseEmoji(uiState.phase),
                        trailing = { StatusPill(text = phaseBadge(uiState)) }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatTile(
                            label = "Focus",
                            value = "${session.focusMinutes}m",
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "Break",
                            value = "${session.breakMinutes}m",
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "Energy",
                            value = "${session.energyLevel}/5",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TimerCard(uiState = uiState)

                    FocusCard {
                        SectionTitle(
                            title = "Gemini plan",
                            subtitle = "Follow this plan, but adjust gently if you feel tired."
                        )
                        Text(
                            text = session.aiRecommendation.substringBefore("Raw Gemini response:").trim(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (uiState.phase == SessionPhase.Completed) {
                        PrimaryActionButton(
                            text = "View Summary",
                            onClick = { onFinish(session.id) }
                        )
                    } else {
                        PrimaryActionButton(
                            text = if (uiState.isRunning) "Pause Timer" else "Start Timer",
                            onClick = viewModel::toggleTimer
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = viewModel::resetPhase,
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Reset")
                            }
                            OutlinedButton(
                                onClick = viewModel::skipToNextPhaseOrFinish,
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(if (uiState.phase == SessionPhase.Focus) "Go to Break" else "Finish")
                            }
                        }

                        OutlinedButton(
                            onClick = viewModel::completeSession,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Mark Session Complete")
                        }
                    }

                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Back to Planner")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun TimerCard(uiState: SessionUiState) {
    val remainingProgress = if (uiState.totalPhaseSeconds <= 0) {
        0f
    } else {
        uiState.secondsRemaining.toFloat() / uiState.totalPhaseSeconds.toFloat()
    }.coerceIn(0f, 1f)
    val elapsedProgress = (1f - remainingProgress).coerceIn(0f, 1f)

    FocusCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = elapsedProgress,
                    modifier = Modifier.size(224.dp),
                    strokeWidth = 12.dp,
                    color = phaseColor(uiState.phase),
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatSeconds(uiState.secondsRemaining),
                        style = MaterialTheme.typography.displayLarge,
                        color = phaseColor(uiState.phase),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.isRunning) "timer running" else "timer paused",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LinearProgressIndicator(
                progress = elapsedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = phaseColor(uiState.phase),
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = phaseGuidance(uiState.phase),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun phaseTitle(phase: SessionPhase): String = when (phase) {
    SessionPhase.Focus -> "Focus time"
    SessionPhase.Break -> "Break time"
    SessionPhase.Completed -> "Session complete"
}

private fun phaseEmoji(phase: SessionPhase): String = when (phase) {
    SessionPhase.Focus -> "🎯"
    SessionPhase.Break -> "☕"
    SessionPhase.Completed -> "✅"
}

private fun phaseBadge(uiState: SessionUiState): String = when (uiState.phase) {
    SessionPhase.Focus -> if (uiState.isRunning) "Deep work" else "Ready"
    SessionPhase.Break -> if (uiState.isRunning) "Resting" else "Break"
    SessionPhase.Completed -> "Saved"
}

@Composable
private fun phaseColor(phase: SessionPhase) = when (phase) {
    SessionPhase.Focus -> MaterialTheme.colorScheme.primary
    SessionPhase.Break -> MaterialTheme.colorScheme.secondary
    SessionPhase.Completed -> MaterialTheme.colorScheme.primary
}

private fun phaseGuidance(phase: SessionPhase): String = when (phase) {
    SessionPhase.Focus -> "Keep one tab, one task, one intention. You can do this."
    SessionPhase.Break -> "Rest your eyes, drink water, and move a little before continuing."
    SessionPhase.Completed -> "Nice work. Your session has been saved locally."
}

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
