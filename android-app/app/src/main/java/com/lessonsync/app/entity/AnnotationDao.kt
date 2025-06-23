package com.lessonsync.app.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(annotations: List<AnnotationEntity>)

    @Query("Delete FROM annotations WHERE scoreOwnerId = :scoreOwnerId")
    suspend fun deleteAllForScore(scoreOwnerId: Int)

    @Insert
    suspend fun insert(annotation: AnnotationEntity)

    @Query("SELECT * FROM annotations WHERE scoreOwnerId = :scoreOwnerId")
    fun getAnnotationsForScore(scoreOwnerId: Int): Flow<List<AnnotationEntity>>  // Flow를 사용해 DB 변경을 실시간으로 감지

    @Query("DELETE FROM annotations WHERE scoreOwnerId = :scoreOwnerId AND measureNumber = :measureNumber")
    suspend fun deleteAnnotation(scoreOwnerId: Int, measureNumber: Int)
}