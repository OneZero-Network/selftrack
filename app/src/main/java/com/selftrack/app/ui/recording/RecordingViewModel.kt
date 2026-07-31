package com.selftrack.app.ui.recording

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.selftrack.app.data.database.entity.ActivityEntity
import com.selftrack.app.data.database.entity.RoutePointEntity
import com.selftrack.app.data.repository.ActivityRepository
import com.selftrack.app.domain.model.ActivityType
import com.selftrack.app.location.RecordingSessionManager
import com.selftrack.app.location.RecordingStatus
import com.selftrack.app.service.RecordingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingViewModel @Inject constructor(
    application: Application,
    private val sessionManager: RecordingSessionManager,
    private val repository: ActivityRepository
) : AndroidViewModel(application) {

    val state: StateFlow<com.selftrack.app.location.RecordingState> = sessionManager.state

    fun startRecording(activityType: ActivityType) {
        val context = getApplication<Application>()
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START
            putExtra(RecordingService.EXTRA_ACTIVITY_TYPE, activityType.name)
        }
        context.startForegroundService(intent)
    }

    fun pause() = sendAction(RecordingService.ACTION_PAUSE)

    fun resume() = sendAction(RecordingService.ACTION_RESUME)

    fun finish(onSaved: (Long) -> Unit) {
        sendAction(RecordingService.ACTION_STOP)
        val current = state.value
        viewModelScope.launch {
            val activity = ActivityEntity(
                activityType = current.activityType.name,
                startTimeMillis = current.startTimeMillis,
                endTimeMillis = System.currentTimeMillis(),
                durationMillis = current.elapsedMillis,
                distanceMeters = current.distanceMeters,
                avgSpeedMps = current.avgSpeedMps,
                maxSpeedMps = current.maxSpeedMps,
                elevationGainMeters = current.elevationGainMeters,
                calories = current.calories
            )
            val points = current.points.mapIndexed { index, point ->
                RoutePointEntity(
                    activityId = 0,
                    timestampMillis = point.timestampMillis,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    altitudeMeters = point.altitudeMeters,
                    speedMps = point.speedMps,
                    sequence = index
                )
            }
            val id = repository.saveActivity(activity, points)
            sessionManager.reset()
            onSaved(id)
        }
    }

    fun discard() {
        sendAction(RecordingService.ACTION_STOP)
        sessionManager.reset()
    }

    private fun sendAction(action: String) {
        val context = getApplication<Application>()
        val intent = Intent(context, RecordingService::class.java).apply { this.action = action }
        context.startService(intent)
    }
}
