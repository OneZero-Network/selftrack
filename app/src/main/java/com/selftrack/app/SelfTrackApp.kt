package com.selftrack.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SelfTrackApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createRecordingNotificationChannel()
    }

    private fun createRecordingNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RECORDING_CHANNEL_ID,
                "Activity Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the ongoing activity recording status"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val RECORDING_CHANNEL_ID = "recording_channel"
    }
}
