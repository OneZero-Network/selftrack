package com.selftrack.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.selftrack.app.data.database.dao.ActivityDao
import com.selftrack.app.data.database.entity.ActivityEntity
import com.selftrack.app.data.database.entity.RoutePointEntity

@Database(
    entities = [ActivityEntity::class, RoutePointEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    companion object {
        const val DATABASE_NAME = "selftrack.db"
    }
}
