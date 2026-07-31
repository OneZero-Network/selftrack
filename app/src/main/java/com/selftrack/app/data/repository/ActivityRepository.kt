package com.selftrack.app.data.repository

import com.selftrack.app.data.database.dao.ActivityDao
import com.selftrack.app.data.database.entity.ActivityEntity
import com.selftrack.app.data.database.entity.RoutePointEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao
) {
    suspend fun saveActivity(activity: ActivityEntity, points: List<RoutePointEntity>): Long =
        activityDao.saveActivityWithRoute(activity, points)

    fun observeRecentActivities(limit: Int = 5): Flow<List<ActivityEntity>> =
        activityDao.observeRecentActivities(limit)

    fun observeAllActivities(): Flow<List<ActivityEntity>> =
        activityDao.observeAllActivities()

    fun observeActivity(id: Long): Flow<ActivityEntity?> = activityDao.observeActivity(id)

    fun observeRoutePoints(activityId: Long): Flow<List<RoutePointEntity>> =
        activityDao.observeRoutePoints(activityId)

    fun observeTodayDistance(): Flow<Double> =
        activityDao.observeTodayDistance(startOfTodayMillis())

    fun observeTodayDuration(): Flow<Long> =
        activityDao.observeTodayDuration(startOfTodayMillis())

    fun observeTodayCalories(): Flow<Int> =
        activityDao.observeTodayCalories(startOfTodayMillis())

    fun observeWeeklyDistance(): Flow<Double> =
        activityDao.observeWeeklyDistance(startOfWeekMillis())

    fun observeWeeklyDuration(): Flow<Long> =
        activityDao.observeWeeklyDuration(startOfWeekMillis())

    private fun startOfTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun startOfWeekMillis(): Long {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
