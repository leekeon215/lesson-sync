package com.lessonsync.app.repository

import AudioService
import com.lessonsync.app.entity.AnnotationInfo
import com.lessonsync.app.entity.AnnotationRequest
import com.lessonsync.app.entity.LessonData
import com.lessonsync.app.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class LessonRepository(private val audioService: AudioService) {

    suspend fun processLesson(file: File): Result<LessonData> {
        return withContext(Dispatchers.IO) {
            try {
                val requestFile = file.asRequestBody("audio/wav".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val response = audioService.processLesson(body)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("서버 응답 오류: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("네트워크 오류: ${e.message}"))
            }
        }
    }

    suspend fun fetchAnnotations(request: AnnotationRequest): Result<List<AnnotationInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.scoreService.getAnnotations(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.annotations)
                } else {
                    Result.failure(Exception("Failed to fetch annotations: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error while fetching annotations: ${e.message}"))
            }
        }
    }
}