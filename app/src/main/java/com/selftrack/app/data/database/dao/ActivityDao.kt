package com.selftrack.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.selftrack.app.data.database.entity.ActivityEntity
import com.selftrack.app.data.database.entity.RoutePointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePoints(points: List<RoutePointEntity>)

    @Transaction
    suspend fun saveActivityWithRoute(
        activity: ActivityEntity,
        points: List<RoutePointEntity>
    ): Long {
        val activityId = insertActivity(activity)
        insertRoutePoints(points.map { it.copy(activityId = activityId) })
        return activityId
    }

    @Query("SELECT * FROM activities ORDER BY startTimeMillis DESC")
    fun observeAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities ORDER BY startTimeMillis DESC LIMIT :limit")
    fun observeRecentActivities(limit: Int = 5): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :activityId")
    fun observeActivity(activityId: Long): Flow<ActivityEntity?>

    @Query("SELECT * FROM route_points WHERE activityId = :activityId ORDER BY sequence ASC")
    fun observeRoutePoints(activityId: Long): Flow<List<RoutePointEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(distanceMeters), 0) FROM activities
        WHERE startTimeMillis >= :startOfDayMillis
        """
    )
    fun observeTodayDistance(startOfDayMillis: Long): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(durationMillis), 0) FROM activities
        WHERE startTimeMillis >= :startOfDayMillis
        """
    )
    fun observeTodayDuration(startOfDayMillis: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(distanceMeters), 0) FROM activities
        WHERE startTimeMillis >= :startOfWeekMillis
        """
    )
    fun observeWeeklyDistance(startOfWeekMillis: Long): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(durationMillis), 0) FROM activities
        WHERE startTimeMillis >= :startOfWeekMillis
        """
    )
    fun observeWeeklyDuration(startOfWeekMillis: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(calories), 0) FROM activities
        WHERE startTimeMillis >= :startOfDayMillis
        """
    )
    fun observeTodayCalories(startOfDayMillis: Long): Flow<Int>
}
