package com.lessonsync.app.repository

import com.lessonsync.app.entity.AnnotationDao // 자신의 DAO 경로로 수정하세요
import com.lessonsync.app.entity.AnnotationEntity
import kotlinx.coroutines.flow.Flow

class AnnotationRepository(private val annotationDao: AnnotationDao) {

    fun getAnnotationsForScore(scoreOwnerId: Int): Flow<List<AnnotationEntity>> {
        return annotationDao.getAnnotationsForScore(scoreOwnerId)
    }

    suspend fun insert(annotation: AnnotationEntity) {
        annotationDao.insert(annotation)
    }

    // 사용자가 추가한 삭제 기능을 호출하는 함수
    suspend fun deleteAnnotationsForScore(scoreOwnerId: Int) {
        annotationDao.deleteAnnotationsForScore(scoreOwnerId)
    }
}