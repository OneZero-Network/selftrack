package com.selftrack.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.selftrack.app.data.database.entity.ActivityEntity
import com.selftrack.app.domain.model.ActivityType
import com.selftrack.app.ui.components.ActivityTypeChip
import com.selftrack.app.ui.components.StatCard
import com.selftrack.app.ui.theme.SelfTrackTheme
import com.selftrack.app.util.Formatters

@Composable
fun DashboardScreen(
    onStartActivity: (ActivityType) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(uiState = uiState, onStartActivity = onStartActivity)
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onStartActivity: (ActivityType) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SelfTrack") })
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding)
        ) {
            item {
                Text(
                    text = "Quick Start",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(ActivityType.entries) { type ->
                        ActivityTypeChip(activityType = type, onClick = { onStartActivity(type) })
                    }
                }
            }

            item {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = "Distance",
                        value = Formatters.distanceKm(uiState.todayDistanceMeters),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Time",
                        value = Formatters.duration(uiState.todayDurationMillis),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Calories",
                        value = "${uiState.todayCalories} kcal",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "This Week",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = "Distance",
                        value = Formatters.distanceKm(uiState.weeklyDistanceMeters),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Time",
                        value = Formatters.duration(uiState.weeklyDurationMillis),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Recent Activities",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (uiState.recentActivities.isEmpty()) {
                item {
                    Text(
                        text = "No activities yet — tap a sport above to start your first one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(uiState.recentActivities) { activity: ActivityEntity ->
                    RecentActivityRow(activity)
                }
            }
        }
    }
}

@Composable
private fun RecentActivityRow(activity: ActivityEntity) {
    val type = ActivityType.fromName(activity.activityType)
    StatCard(
        label = type.displayName,
        value = "${Formatters.distanceKm(activity.distanceMeters)} · ${Formatters.duration(activity.durationMillis)}",
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview
@Composable
private fun DashboardPreview() {
    SelfTrackTheme {
        DashboardContent(uiState = DashboardUiState(), onStartActivity = {})
    }
}
