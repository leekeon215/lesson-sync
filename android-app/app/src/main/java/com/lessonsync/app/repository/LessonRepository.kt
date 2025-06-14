package com.lessonsync.app.repository

import AudioService
import com.lessonsync.app.entity.LessonData
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
}