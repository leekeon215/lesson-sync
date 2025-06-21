package com.lessonsync.app.retrofit

import com.lessonsync.app.entity.AnnotationRequest
import com.lessonsync.app.entity.AnnotationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 악보 및 주석 관련 API를 정의하는 인터페이스
 */
interface ScoreService {

    @POST("parse-directives")
    suspend fun getAnnotations(
        @Body request: AnnotationRequest
    ): Response<AnnotationResponse>
}