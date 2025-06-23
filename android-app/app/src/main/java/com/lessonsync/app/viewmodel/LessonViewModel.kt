package com.lessonsync.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lessonsync.app.database.LessonSyncDatabase
import com.lessonsync.app.entity.AnnotationInfo
import com.lessonsync.app.entity.AnnotationRequest
import com.lessonsync.app.entity.LessonData
import com.lessonsync.app.entity.LessonUiState
import com.lessonsync.app.repository.LessonRepository
import com.lessonsync.app.repository.ScoreRepository
import com.lessonsync.app.retrofit.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class LessonViewModel(application: Application) : AndroidViewModel(application) {

    private val lessonRepository: LessonRepository
    private val scoreRepository: ScoreRepository

    private val _uiState = MutableStateFlow<LessonUiState>(LessonUiState.Idle)
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    init {
        val scoreDao = LessonSyncDatabase.getDatabase(application).scoreDao()
        scoreRepository = ScoreRepository(scoreDao)
        lessonRepository = LessonRepository(RetrofitClient.audioService)
    }

    /**
     * 녹음 파일을 서버에 업로드하고 전체 분석 및 저장 프로세스를 시작합니다.
     */
    fun uploadAndProcessRecording(scoreId: Int, file: File) {
        viewModelScope.launch {

            scoreRepository.updateRecordedFilePath(scoreId, file.absolutePath)

            // 1. 로딩 상태로 변경
            _uiState.value = LessonUiState.Loading

            // 2. (요청 1) 서버에 녹음 파일을 보내 요약 및 전문(transcript)을 요청
            val lessonResult = lessonRepository.processLesson(file)

            lessonResult.onSuccess { lessonData ->
                // 3. (요청 2) 받은 전문으로 주석 파싱을 요청
                processToCreateAnnotations(scoreId, lessonData)

            }.onFailure { exception ->
                _uiState.value = LessonUiState.Error(exception.message ?: "레슨 요약 분석 중 오류 발생")
            }
        }
    }

    /**
     * 서버로부터 받은 텍스트 전문으로 주석 정보를 파싱하도록 서버에 다시 요청하고,
     * 모든 결과를 취합하여 DB에 저장합니다.
     */
    private fun processToCreateAnnotations(scoreId: Int, lessonData: LessonData) {
        val fullTranscript = lessonData.speechSegments?.joinToString(" ") { it.text } ?: ""
        if (fullTranscript.isBlank()) {
            // 주석으로 만들 텍스트가 없으면, 현재까지의 결과(요약, 빈 전문)만 저장
            saveAnalysisResultToDb(scoreId, lessonData.summary ?: "", "", emptyList())
            _uiState.value = LessonUiState.Success(lessonData)
            return
        }

        viewModelScope.launch {
            val request = AnnotationRequest(text = fullTranscript)
            val annotationResult = lessonRepository.fetchAnnotations(request)

            annotationResult.onSuccess { annotations ->
                // 4. 모든 정보(요약, 전문, 주석)를 DB에 최종 저장
                saveAnalysisResultToDb(scoreId, lessonData.summary ?: "", fullTranscript, annotations)
                // 5. 모든 과정이 완료되었으므로 최종 성공 상태로 변경
                _uiState.value = LessonUiState.Success(lessonData)

            }.onFailure { exception ->
                // 주석 파싱은 실패했지만, 앞선 요약/전문 결과는 있으므로 일단 저장하고 에러 상태로 변경
                saveAnalysisResultToDb(scoreId, lessonData.summary ?: "", fullTranscript, emptyList())
                _uiState.value = LessonUiState.Error(exception.message ?: "주석 정보를 가져오는 데 실패했습니다.")
            }
        }
    }

    /**
     * 분석된 모든 결과를 로컬 Room DB에 저장합니다.
     */
    private fun saveAnalysisResultToDb(
        scoreId: Int,
        summary: String,
        transcript: String,
        annotations: List<AnnotationInfo>
    ) {
        viewModelScope.launch {
            scoreRepository.saveLessonAnalysis(scoreId, summary, transcript, annotations)
        }
    }

    /**
     * UI 상태를 초기값으로 리셋합니다.
     */
    fun resetState() {
        _uiState.value = LessonUiState.Idle
    }
}