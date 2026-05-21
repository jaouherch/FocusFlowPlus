package com.example.focusflowplus.ui.summary

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.focusflowplus.ui.components.FocusCard
import com.example.focusflowplus.ui.components.PrimaryActionButton
import com.example.focusflowplus.ui.components.ScreenHero
import com.example.focusflowplus.ui.components.SectionTitle
import com.example.focusflowplus.ui.components.StatTile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel,
    onNewSession: () -> Unit,
    onHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.error != null -> Text(
                text = uiState.error ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(24.dp)
            )
            uiState.session != null -> {
                val session = uiState.session!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    ScreenHero(
                        title = "Great work",
                        subtitle = "Your session was completed and saved locally.",
                        emoji = "🎉"
                    )

                    FocusCard {
                        SectionTitle(
                            title = "Session recap",
                            subtitle = session.taskDescription
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
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatTile(
                                label = "Energy",
                                value = "${session.energyLevel}/5",
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                label = "Available",
                                value = "${session.availableMinutes}m",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = "Completed on ${formatDate(session.completedAt)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FocusCard {
                        SectionTitle(
                            title = "AI recommendation used",
                            subtitle = "This is the Gemini plan that guided the session."
                        )
                        Text(
                            text = session.aiRecommendation.substringBefore("Raw Gemini response:").trim(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FocusCard {
                        SectionTitle(
                            title = "Next improvement",
                            subtitle = "For a stronger portfolio project, add mood feedback after each session and use it to personalize future plans."
                        )
                    }

                    PrimaryActionButton(
                        text = "Plan Another Session",
                        onClick = onNewSession
                    )
                    OutlinedButton(
                        onClick = onHistory,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("View History")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(timestamp))
}
