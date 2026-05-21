package com.example.focusflowplus.ui.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.focusflowplus.domain.model.FocusSession
import com.example.focusflowplus.ui.components.FocusCard
import com.example.focusflowplus.ui.components.ScreenHero
import com.example.focusflowplus.ui.components.SectionTitle
import com.example.focusflowplus.ui.components.StatTile
import com.example.focusflowplus.ui.components.StatusPill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    onSessionClick: (Long) -> Unit
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
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ScreenHero(
                            title = "History",
                            subtitle = "Track completed sessions and total focus time.",
                            emoji = "📈",
                            trailing = {
                                OutlinedButton(onClick = onBack, shape = RoundedCornerShape(16.dp)) {
                                    Text("Planner")
                                }
                            }
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatTile(
                                    label = "Completed",
                                    value = uiState.completedCount.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                StatTile(
                                    label = "Today",
                                    value = uiState.completedToday.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            StatTile(
                                label = "Total Focus Time",
                                value = readableTotalFocus(uiState.totalFocusMinutes),
                                modifier = Modifier.fillMaxWidth(),
                                helper = "completed minutes"
                            )
                        }
                    }

                    item {
                        SectionTitle(
                            title = "Saved sessions",
                            subtitle = if (uiState.sessions.isEmpty()) "No sessions yet." else "Tap a session to reopen it."
                        )
                    }

                    if (uiState.sessions.isEmpty()) {
                        item {
                            EmptyHistoryCard(onBack = onBack)
                        }
                    } else {
                        items(uiState.sessions, key = { it.id }) { session ->
                            SessionHistoryCard(session = session, onClick = { onSessionClick(session.id) })
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(session: FocusSession, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(text = if (session.wasCompleted) "Completed" else "Planned")
                Text(
                    text = formatDate(session.completedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = session.taskDescription,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${session.focusMinutes} min focus • ${session.breakMinutes} min break • energy ${session.energyLevel}/5",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyHistoryCard(onBack: () -> Unit) {
    FocusCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "No sessions yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Create a Gemini-powered plan and complete your first session to see your progress here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onBack, shape = RoundedCornerShape(16.dp)) {
                Text("Create Session")
            }
        }
    }
}

private fun readableTotalFocus(minutes: Int): String {
    return if (minutes < 60) {
        "${minutes}m"
    } else {
        val hours = minutes / 60
        val remaining = minutes % 60
        if (remaining == 0) "${hours}h" else "${hours}h ${remaining}m"
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault()).format(Date(timestamp))
}
