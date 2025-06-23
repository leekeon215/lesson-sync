package com.lessonsync.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lessonsync.app.entity.AnnotationDao
import com.lessonsync.app.entity.AnnotationEntity
import com.lessonsync.app.entity.LessonResultEntity
import com.lessonsync.app.entity.ScoreDao
import com.lessonsync.app.entity.ScoreEntity

// entities 배열에 새 엔티티 추가, version을 1에서 2로 올림
@Database(entities = [ScoreEntity::class, LessonResultEntity::class, AnnotationEntity::class], version = 2, exportSchema = false)
abstract class LessonSyncDatabase : RoomDatabase() {

    abstract fun scoreDao(): ScoreDao
    abstract fun annotationDao(): AnnotationDao // AnnotationEntity를 위한 DAO

    companion object {
        @Volatile
        private var INSTANCE: LessonSyncDatabase? = null

        // [추가] 버전 1 -> 2 마이그레이션 코드
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE scores ADD COLUMN recordedFilePath TEXT")
            }
        }

        fun getDatabase(context: Context): LessonSyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LessonSyncDatabase::class.java,
                    "lessonsync_database"
                )
                    // 스키마 변경에 따른 마이그레이션 정책 추가 (개발 중에는 간단하게 파괴 후 재생성)
//                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2) // 버전 1에서 2로의 마이그레이션 추가
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}