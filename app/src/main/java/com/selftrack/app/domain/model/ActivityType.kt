package com.selftrack.app.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ActivityType(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    RUNNING("Running", Icons.Filled.DirectionsRun, Color(0xFFFF5A5F)),
    WALKING("Walking", Icons.Filled.DirectionsWalk, Color(0xFF22D3A5)),
    CYCLING("Cycling", Icons.Filled.DirectionsBike, Color(0xFF4C8DFF)),
    HIKING("Hiking", Icons.Filled.Hiking, Color(0xFFE7A93B)),
    DRIVING("Driving", Icons.Filled.DirectionsCar, Color(0xFF9B7BFF)),
    TREKKING("Trekking", Icons.Filled.Terrain, Color(0xFF54C6EB)),
    CUSTOM("Custom", Icons.Filled.SportsScore, Color(0xFFB0B8C1));

    companion object {
        fun fromName(name: String): ActivityType =
            entries.firstOrNull { it.name == name } ?: CUSTOM
    }
}
