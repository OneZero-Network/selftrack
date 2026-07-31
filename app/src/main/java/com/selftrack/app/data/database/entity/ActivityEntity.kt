package com.selftrack.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityType: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMillis: Long,
    val distanceMeters: Double,
    val avgSpeedMps: Double,
    val maxSpeedMps: Double,
    val elevationGainMeters: Double,
    val calories: Int,
    val notes: String? = null
)
