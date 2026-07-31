package com.selftrack.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selftrack.app.data.database.entity.ActivityEntity
import com.selftrack.app.data.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val todayDistanceMeters: Double = 0.0,
    val todayDurationMillis: Long = 0L,
    val todayCalories: Int = 0,
    val weeklyDistanceMeters: Double = 0.0,
    val weeklyDurationMillis: Long = 0L,
    val recentActivities: List<ActivityEntity> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: ActivityRepository
) : ViewModel() {

    private data class TodayStats(val distance: Double, val duration: Long, val calories: Int)
    private data class WeeklyStats(val distance: Double, val duration: Long)

    private val todayStats = combine(
        repository.observeTodayDistance(),
        repository.observeTodayDuration(),
        repository.observeTodayCalories()
    ) { distance, duration, calories -> TodayStats(distance, duration, calories) }

    private val weeklyStats = combine(
        repository.observeWeeklyDistance(),
        repository.observeWeeklyDuration()
    ) { distance, duration -> WeeklyStats(distance, duration) }

    val uiState: StateFlow<DashboardUiState> = combine(
        todayStats,
        weeklyStats,
        repository.observeRecentActivities(5)
    ) { today, weekly, recent ->
        DashboardUiState(
            todayDistanceMeters = today.distance,
            todayDurationMillis = today.duration,
            todayCalories = today.calories,
            weeklyDistanceMeters = weekly.distance,
            weeklyDurationMillis = weekly.duration,
            recentActivities = recent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )
}
