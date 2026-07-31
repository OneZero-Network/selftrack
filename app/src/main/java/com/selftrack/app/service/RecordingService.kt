package com.selftrack.app.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.selftrack.app.R
import com.selftrack.app.SelfTrackApp
import com.selftrack.app.domain.model.ActivityType
import com.selftrack.app.location.LocationTracker
import com.selftrack.app.location.RecordingSessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that keeps GPS recording alive while the app is backgrounded.
 * State updates are written into the shared RecordingSessionManager, which the UI observes.
 */
@AndroidEntryPoint
class RecordingService : Service() {

    @Inject lateinit var locationTracker: LocationTracker
    @Inject lateinit var sessionManager: RecordingSessionManager

    private val serviceScope = CoroutineScope(SupervisorJob())
    private var locationJob: Job? = null
    private var tickerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val typeName = intent.getStringExtra(EXTRA_ACTIVITY_TYPE) ?: ActivityType.RUNNING.name
                startForeground(NOTIFICATION_ID, buildNotification("Recording started"))
                sessionManager.start(ActivityType.fromName(typeName))
                startTracking()
            }
            ACTION_PAUSE -> sessionManager.pause()
            ACTION_RESUME -> sessionManager.resume()
            ACTION_STOP -> {
                sessionManager.finish()
                stopTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startTracking() {
        locationJob?.cancel()
        locationJob = serviceScope.launch {
            locationTracker.trackLocation().collect { location ->
                sessionManager.onNewLocation(location)
            }
        }
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (true) {
                sessionManager.tick(System.currentTimeMillis())
                delay(1_000L)
            }
        }
    }

    private fun stopTracking() {
        locationJob?.cancel()
        tickerJob?.cancel()
    }

    private fun buildNotification(contentText: String): Notification =
        NotificationCompat.Builder(this, SelfTrackApp.RECORDING_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        stopTracking()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.selftrack.app.action.START"
        const val ACTION_PAUSE = "com.selftrack.app.action.PAUSE"
        const val ACTION_RESUME = "com.selftrack.app.action.RESUME"
        const val ACTION_STOP = "com.selftrack.app.action.STOP"
        const val EXTRA_ACTIVITY_TYPE = "extra_activity_type"
        private const val NOTIFICATION_ID = 1001
    }
}
