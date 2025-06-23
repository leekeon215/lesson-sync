package com.lessonsync.app.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScore(score: ScoreEntity)

    @Query("SELECT * FROM scores ORDER BY createdAt DESC")
    fun getAllScores(): Flow<List<ScoreEntity>>

    @Query("SELECT * FROM scores WHERE id = :scoreId")
    fun getScoreById(scoreId: Int): ScoreEntity?

    @Query("UPDATE scores SET recordedFilePath = :filePath WHERE id = :scoreId")
    suspend fun updateRecordedFilePath(scoreId: Int, filePath: String)

    @Delete
    fun deleteScore(score: ScoreEntity)

    @Query("DELETE FROM scores WHERE id IN (:scoreIds)")
    fun deleteScoresByIds(scoreIds: List<Int>)

    @Query("SELECT * FROM scores WHERE title LIKE :searchQuery")
    fun searchScores(searchQuery: String): Flow<List<ScoreEntity>>

    //LessonResult 및 Annotation 저장을 위한 함수
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLessonResult(lessonResult: LessonResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAnnotations(annotations: List<AnnotationEntity>)

    @Query("SELECT * FROM lesson_results WHERE scoreOwnerId = :scoreId LIMIT 1")
    suspend fun getLessonResultForScore(scoreId: Int): LessonResultEntity?

    @Query("SELECT * FROM annotations WHERE scoreOwnerId = :scoreId")
    fun getAnnotationsForScore(scoreId: Int): Flow<List<AnnotationEntity>>

    // 이전 분석 결과가 있다면 삭제하기 위한 함수
    @Query("DELETE FROM lesson_results WHERE scoreOwnerId = :scoreId")
    fun deletePreviousLessonResult(scoreId: Int)

    @Query("DELETE FROM annotations WHERE scoreOwnerId = :scoreId")
    fun deletePreviousAnnotations(scoreId: Int)
}