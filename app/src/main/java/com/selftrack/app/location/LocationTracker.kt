package com.selftrack.app.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps FusedLocationProviderClient as a cold Flow of high-accuracy location updates.
 * Uses an adaptive interval suited for GPS-based activity recording.
 */
@Singleton
class LocationTracker @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient
) {
    @SuppressLint("MissingPermission")
    fun trackLocation(intervalMillis: Long = 2_000L): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            request,
            callback,
            null
        )

        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(callback)
        }
    }
}
