package com.selftrack.app.di

import android.content.Context
import androidx.room.Room
import com.selftrack.app.data.database.AppDatabase
import com.selftrack.app.data.database.dao.ActivityDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration() // replaced with real migrations before v1.0 ships
            .build()

    @Provides
    @Singleton
    fun provideActivityDao(database: AppDatabase): ActivityDao = database.activityDao()
}
