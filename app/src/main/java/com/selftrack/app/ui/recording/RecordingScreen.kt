package com.selftrack.app.ui.recording

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.selftrack.app.BuildConfig
import com.selftrack.app.domain.model.ActivityType
import com.selftrack.app.location.RecordingStatus
import com.selftrack.app.ui.components.StatCard
import com.selftrack.app.util.Formatters

@Composable
fun RecordingScreen(
    activityTypeName: String,
    onFinished: (Long) -> Unit,
    onDiscarded: () -> Unit,
    viewModel: RecordingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activityType = remember(activityTypeName) { ActivityType.fromName(activityTypeName) }
    val recordingState by viewModel.state.collectAsStateWithLifecycle()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasLocationPermission = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (hasLocationPermission && recordingState.status == RecordingStatus.IDLE) {
            viewModel.startRecording(activityType)
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            if (recordingState.status == RecordingStatus.IDLE) {
                viewModel.startRecording(activityType)
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    if (!hasLocationPermission) {
        PermissionRationale(onGrant = {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        })
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        RecordingMap(
            points = recordingState.points.map { LatLng(it.latitude, it.longitude) },
            activityColor = activityType.color,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = activityType.displayName,
                style = MaterialTheme.typography.titleLarge
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Distance",
                    value = Formatters.distanceKm(recordingState.distanceMeters),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Time",
                    value = Formatters.duration(recordingState.elapsedMillis),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Pace",
                    value = Formatters.pacePerKm(recordingState.avgSpeedMps),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Speed",
                    value = Formatters.speedKmh(recordingState.currentSpeedMps.toDouble()),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Elevation",
                    value = Formatters.elevation(recordingState.elevationGainMeters),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "GPS",
                    value = if (recordingState.hasGpsSignal) "Locked" else "Searching…",
                    modifier = Modifier.weight(1f)
                )
            }

            RecordingControls(
                status = recordingState.status,
                onPause = viewModel::pause,
                onResume = viewModel::resume,
                onFinish = { viewModel.finish(onFinished) },
                onDiscard = {
                    viewModel.discard()
                    onDiscarded()
                }
            )
        }
    }
}

@Composable
private fun RecordingMap(
    points: List<LatLng>,
    activityColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val keyMissing = BuildConfig.MAPS_API_KEY.isBlank() ||
        BuildConfig.MAPS_API_KEY == "YOUR_GOOGLE_MAPS_API_KEY"

    if (keyMissing) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "Maps API key missing.\nAdd MAPS_API_KEY to local.properties to enable the map.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            points.lastOrNull() ?: LatLng(0.0, 0.0),
            17f
        )
    }

    LaunchedEffect(points.lastOrNull()) {
        points.lastOrNull()?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 17f)
        }
    }

    GoogleMap(modifier = modifier, cameraPositionState = cameraPositionState) {
        if (points.size > 1) {
            Polyline(points = points, color = activityColor, width = 10f)
        }
    }
}

@Composable
private fun RecordingControls(
    status: RecordingStatus,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
    onDiscard: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (status) {
            RecordingStatus.RECORDING -> {
                Button(onClick = onPause, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Pause)
                    Text("  Pause")
                }
            }
            RecordingStatus.PAUSED -> {
                Button(onClick = onResume, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.PlayArrow)
                    Text("  Resume")
                }
            }
            else -> {}
        }
        Button(
            onClick = onFinish,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Filled.Stop)
            Text("  Finish")
        }
    }
    Text(
        text = "Discard activity",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .padding(top = 4.dp)
            .clickable(onClick = onDiscard)
    )
}

@Composable
private fun Icon(imageVector: androidx.compose.ui.graphics.vector.ImageVector) {
    androidx.compose.material3.Icon(imageVector = imageVector, contentDescription = null)
}

@Composable
private fun PermissionRationale(onGrant: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Location permission is required to record your route.",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onGrant) {
                Text("Grant Permission")
            }
        }
    }
}
