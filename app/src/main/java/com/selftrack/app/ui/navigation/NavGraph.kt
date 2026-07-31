package com.selftrack.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.selftrack.app.ui.dashboard.DashboardScreen
import com.selftrack.app.ui.recording.RecordingScreen
import com.selftrack.app.ui.summary.SummaryScreen

@Composable
fun SelfTrackNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destinations.DASHBOARD,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        composable(Destinations.DASHBOARD) {
            DashboardScreen(
                onStartActivity = { activityType ->
                    navController.navigate(Destinations.recording(activityType.name))
                }
            )
        }

        composable(
            route = Destinations.RECORDING,
            arguments = listOf(navArgument("activityType") { type = NavType.StringType })
        ) { backStackEntry ->
            val activityTypeName = backStackEntry.arguments?.getString("activityType").orEmpty()
            RecordingScreen(
                activityTypeName = activityTypeName,
                onFinished = { activityId ->
                    navController.navigate(Destinations.summary(activityId)) {
                        popUpTo(Destinations.DASHBOARD)
                    }
                },
                onDiscarded = {
                    navController.popBackStack(Destinations.DASHBOARD, inclusive = false)
                }
            )
        }

        composable(
            route = Destinations.SUMMARY,
            arguments = listOf(navArgument("activityId") { type = NavType.LongType })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getLong("activityId") ?: -1L
            SummaryScreen(
                activityId = activityId,
                onDone = {
                    navController.popBackStack(Destinations.DASHBOARD, inclusive = false)
                }
            )
        }
    }
}
