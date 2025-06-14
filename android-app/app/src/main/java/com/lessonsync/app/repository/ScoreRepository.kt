package com.lessonsync.app.repository

import com.lessonsync.app.entity.ScoreDao
import com.lessonsync.app.entity.ScoreEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScoreRepository(private val scoreDao: ScoreDao) {

    val allScores: Flow<List<ScoreEntity>> = scoreDao.getAllScores()

    suspend fun insert(score: ScoreEntity) {
        withContext(Dispatchers.IO) {
            scoreDao.insertScore(score)
        }
    }

    suspend fun getScoreById(id: Int): ScoreEntity? {
        return withContext(Dispatchers.IO) { // IO 스레드에서 실행
            scoreDao.getScoreById(id)
        }
    }

    suspend fun delete(score: ScoreEntity) {
        withContext(Dispatchers.IO) {
            scoreDao.deleteScore(score)
        }
    }

    suspend fun deleteByIds(ids: List<Int>) {
        withContext(Dispatchers.IO) {
            scoreDao.deleteScoresByIds(ids)
        }
    }

    fun searchScores(query: String): Flow<List<ScoreEntity>> {
        return scoreDao.searchScores(query)
    }
}