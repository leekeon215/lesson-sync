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

    @Delete
    fun deleteScore(score: ScoreEntity)

    @Query("DELETE FROM scores WHERE id IN (:scoreIds)")
    fun deleteScoresByIds(scoreIds: List<Int>)

    @Query("SELECT * FROM scores WHERE title LIKE :searchQuery")
    fun searchScores(searchQuery: String): Flow<List<ScoreEntity>>
}