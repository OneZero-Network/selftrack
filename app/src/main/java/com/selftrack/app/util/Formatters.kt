package com.selftrack.app.util

import java.util.Locale
import kotlin.math.roundToInt

object Formatters {

    /** e.g. 5423.0 meters -> "5.42 km" */
    fun distanceKm(meters: Double): String =
        String.format(Locale.US, "%.2f km", meters / 1000.0)

    fun distanceKmShort(meters: Double): String =
        String.format(Locale.US, "%.1f km", meters / 1000.0)

    /** millis -> "01:23:45" or "12:34" if under an hour */
    fun duration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    /** meters/sec -> "5'12\"/km" pace string */
    fun pacePerKm(speedMps: Double): String {
        if (speedMps <= 0.05) return "--'--\"/km"
        val secondsPerKm = 1000.0 / speedMps
        val minutes = (secondsPerKm / 60).toInt()
        val seconds = (secondsPerKm % 60).roundToInt()
        return String.format(Locale.US, "%d'%02d\"/km", minutes, seconds)
    }

    fun speedKmh(speedMps: Double): String =
        String.format(Locale.US, "%.1f km/h", speedMps * 3.6)

    fun elevation(meters: Double): String =
        String.format(Locale.US, "%.0f m", meters)
}
