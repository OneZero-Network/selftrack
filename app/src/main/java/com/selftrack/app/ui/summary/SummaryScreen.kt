package com.selftrack.app.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.selftrack.app.BuildConfig
import com.selftrack.app.domain.model.ActivityType
import com.selftrack.app.ui.components.StatCard
import com.selftrack.app.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    activityId: Long,
    onDone: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = uiState.activity

    Scaffold(topBar = { TopAppBar(title = { Text("Activity Summary") }) }) { padding ->
        if (activity == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Saving activity…", style = MaterialTheme.typography.bodyMedium)
            }
            return@Scaffold
        }

        val type = ActivityType.fromName(activity.activityType)
        val latLngPoints = uiState.routePoints.map { LatLng(it.latitude, it.longitude) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SummaryMap(
                points = latLngPoints,
                color = type.color,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.2f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = type.displayName, style = MaterialTheme.typography.titleLarge)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Distance", Formatters.distanceKm(activity.distanceMeters), Modifier.weight(1f))
                    StatCard("Time", Formatters.duration(activity.durationMillis), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Avg Pace", Formatters.pacePerKm(activity.avgSpeedMps), Modifier.weight(1f))
                    StatCard("Max Speed", Formatters.speedKmh(activity.maxSpeedMps), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Elevation Gain", Formatters.elevation(activity.elevationGainMeters), Modifier.weight(1f))
                    StatCard("Calories", "${activity.calories} kcal", Modifier.weight(1f))
                }

                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun SummaryMap(points: List<LatLng>, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    val keyMissing = BuildConfig.MAPS_API_KEY.isBlank() ||
        BuildConfig.MAPS_API_KEY == "YOUR_GOOGLE_MAPS_API_KEY"

    if (keyMissing || points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = if (keyMissing) {
                    "Maps API key missing.\nAdd MAPS_API_KEY to local.properties to enable the map."
                } else {
                    "No route recorded."
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    val bounds = remember(points) {
        val builder = LatLngBounds.Builder()
        points.forEach { builder.include(it) }
        builder.build()
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bounds.center, 15f)
    }

    GoogleMap(modifier = modifier, cameraPositionState = cameraPositionState) {
        Polyline(points = points, color = color, width = 10f)
    }
}
