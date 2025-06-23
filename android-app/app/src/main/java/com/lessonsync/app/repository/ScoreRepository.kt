package com.lessonsync.app.repository

import com.lessonsync.app.entity.AnnotationDao
import com.lessonsync.app.entity.AnnotationEntity
import com.lessonsync.app.entity.AnnotationInfo
import com.lessonsync.app.entity.LessonResultEntity
import com.lessonsync.app.entity.ScoreDao
import com.lessonsync.app.entity.ScoreEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScoreRepository(
    private val scoreDao: ScoreDao,
    private val annotationDao: AnnotationDao // AnnotationDao를 사용하기 위한 Repository
) {

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

    // --- [신규] 분석 결과를 DB에 저장하는 함수 ---
    suspend fun saveLessonAnalysis(
        scoreId: Int,
        summary: String,
        transcript: String,
        annotations: List<AnnotationInfo>
    ) {
        withContext(Dispatchers.IO) {
            // 새 분석 결과를 저장하기 전에 이전 결과가 있다면 삭제
            scoreDao.deletePreviousLessonResult(scoreId)
            annotationDao.deleteAllForScore(scoreId)  //이전 주석 삭제 해야됨?..


            // 새 레슨 결과 Entity 생성 및 저장
            val lessonResult = LessonResultEntity(
                scoreOwnerId = scoreId,
                summary = summary,
                fullTranscript = transcript
            )
            scoreDao.insertLessonResult(lessonResult)

            // 새 주석 Entity 리스트 생성 및 저장
            if (annotations.isNotEmpty()) {
                val annotationEntities = annotations.map {
                    AnnotationEntity(
                        scoreOwnerId = scoreId,
                        measureNumber = it.measure,
                        directive = it.directive
                    )

                }
//                scoreDao.insertAnnotations(annotationEntities)
                annotationDao.insertAll(annotationEntities) // AnnotationRepository를 통해 저장
            }
        }
    }

    suspend fun updateRecordedFilePath(scoreId: Int, absolutePath: String) {
        scoreDao.updateRecordedFilePath(scoreId, absolutePath)
    }
}