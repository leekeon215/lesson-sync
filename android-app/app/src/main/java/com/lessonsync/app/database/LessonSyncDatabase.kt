package com.lessonsync.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lessonsync.app.entity.ScoreDao
import com.lessonsync.app.entity.ScoreEntity

@Database(entities = [ScoreEntity::class], version = 1, exportSchema = false)
abstract class LessonSyncDatabase : RoomDatabase() {

    abstract fun scoreDao(): ScoreDao

    companion object {
        @Volatile
        private var INSTANCE: LessonSyncDatabase? = null

        fun getDatabase(context: Context): LessonSyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LessonSyncDatabase::class.java,
                    "lessonsync_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}