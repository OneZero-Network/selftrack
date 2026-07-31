package com.selftrack.app.location

import android.location.Location
import com.selftrack.app.domain.model.ActivityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

enum class RecordingStatus { IDLE, RECORDING, PAUSED, FINISHED }

data class TrackedPoint(
    val timestampMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val speedMps: Float
)

data class RecordingState(
    val status: RecordingStatus = RecordingStatus.IDLE,
    val activityType: ActivityType = ActivityType.RUNNING,
    val startTimeMillis: Long = 0L,
    val elapsedMillis: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentSpeedMps: Float = 0f,
    val avgSpeedMps: Double = 0.0,
    val maxSpeedMps: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val points: List<TrackedPoint> = emptyList(),
    val gpsAccuracyMeters: Float? = null,
    val hasGpsSignal: Boolean = false
) {
    val calories: Int
        get() = (distanceMeters / 1000.0 * activityType.caloriesPerKm()).toInt()
}

private fun ActivityType.caloriesPerKm(): Double = when (this) {
    ActivityType.RUNNING -> 62.0
    ActivityType.WALKING -> 50.0
    ActivityType.CYCLING -> 30.0
    ActivityType.HIKING -> 55.0
    ActivityType.DRIVING -> 2.0
    ActivityType.TREKKING -> 58.0
    ActivityType.CUSTOM -> 45.0
}

/**
 * Single source of truth for an in-progress recording session.
 * The foreground RecordingService writes to this; Compose screens observe it.
 * Kept as an app-scoped singleton so recording survives configuration changes
 * and screen navigation while the service is alive.
 */
@Singleton
class RecordingSessionManager @Inject constructor() {

    private val _state = MutableStateFlow(RecordingState())
    val state: StateFlow<RecordingState> = _state

    private var lastLocation: Location? = null

    fun start(activityType: ActivityType) {
        lastLocation = null
        _state.value = RecordingState(
            status = RecordingStatus.RECORDING,
            activityType = activityType,
            startTimeMillis = System.currentTimeMillis()
        )
    }

    fun pause() {
        _state.update { it.copy(status = RecordingStatus.PAUSED) }
    }

    fun resume() {
        _state.update { it.copy(status = RecordingStatus.RECORDING) }
    }

    fun finish() {
        _state.update { it.copy(status = RecordingStatus.FINISHED) }
    }

    fun reset() {
        lastLocation = null
        _state.value = RecordingState()
    }

    fun tick(nowMillis: Long) {
        val current = _state.value
        if (current.status != RecordingStatus.RECORDING) return
        _state.update { it.copy(elapsedMillis = nowMillis - it.startTimeMillis) }
    }

    fun onNewLocation(location: Location) {
        val current = _state.value
        if (current.status != RecordingStatus.RECORDING) return

        val previous = lastLocation
        var newDistance = current.distanceMeters
        var elevationGain = current.elevationGainMeters

        if (previous != null) {
            val segment = previous.distanceTo(location)
            // Basic noise filter: ignore jitter shorter than 1.5m at rest.
            if (segment > 1.5f) {
                newDistance += segment
            }
            val altDelta = location.altitude - previous.altitude
            if (altDelta > 0) {
                elevationGain += altDelta
            }
        }
        lastLocation = location

        val speed = location.speed
        val newMax = maxOf(current.maxSpeedMps, speed.toDouble())
        val elapsedSeconds = (current.elapsedMillis / 1000.0).coerceAtLeast(1.0)
        val avgSpeed = if (newDistance > 0) newDistance / elapsedSeconds else 0.0

        val point = TrackedPoint(
            timestampMillis = System.currentTimeMillis(),
            latitude = location.latitude,
            longitude = location.longitude,
            altitudeMeters = location.altitude,
            speedMps = speed
        )

        _state.update {
            it.copy(
                distanceMeters = newDistance,
                currentSpeedMps = speed,
                avgSpeedMps = avgSpeed,
                maxSpeedMps = newMax,
                elevationGainMeters = elevationGain,
                points = it.points + point,
                gpsAccuracyMeters = location.accuracy,
                hasGpsSignal = true
            )
        }
    }
}
