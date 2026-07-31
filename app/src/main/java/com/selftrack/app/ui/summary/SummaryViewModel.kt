package com.selftrack.app.ui.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selftrack.app.data.database.entity.ActivityEntity
import com.selftrack.app.data.database.entity.RoutePointEntity
import com.selftrack.app.data.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SummaryUiState(
    val activity: ActivityEntity? = null,
    val routePoints: List<RoutePointEntity> = emptyList()
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    repository: ActivityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val activityId: Long = checkNotNull(savedStateHandle["activityId"])

    val uiState: StateFlow<SummaryUiState> = combine(
        repository.observeActivity(activityId),
        repository.observeRoutePoints(activityId)
    ) { activity, points ->
        SummaryUiState(activity = activity, routePoints = points)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SummaryUiState()
    )
}
