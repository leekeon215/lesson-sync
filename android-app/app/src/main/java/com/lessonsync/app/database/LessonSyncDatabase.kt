package com.lessonsync.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lessonsync.app.entity.AnnotationEntity
import com.lessonsync.app.entity.LessonResultEntity
import com.lessonsync.app.entity.ScoreDao
import com.lessonsync.app.entity.ScoreEntity

// entities 배열에 새 엔티티 추가, version을 1에서 2로 올림
@Database(entities = [ScoreEntity::class, LessonResultEntity::class, AnnotationEntity::class], version = 2, exportSchema = false)
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
                )
                    // 스키마 변경에 따른 마이그레이션 정책 추가 (개발 중에는 간단하게 파괴 후 재생성)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}