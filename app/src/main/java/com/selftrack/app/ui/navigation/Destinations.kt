package com.selftrack.app.ui.navigation

object Destinations {
    const val DASHBOARD = "dashboard"
    const val RECORDING = "recording/{activityType}"
    const val SUMMARY = "summary/{activityId}"

    fun recording(activityType: String) = "recording/$activityType"
    fun summary(activityId: Long) = "summary/$activityId"
}
