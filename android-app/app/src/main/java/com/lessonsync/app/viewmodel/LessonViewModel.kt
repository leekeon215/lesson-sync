package com.lessonsync.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessonsync.app.entity.LessonUiState
import com.lessonsync.app.repository.LessonRepository
import com.lessonsync.app.retrofit.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class LessonViewModel : ViewModel() {

    private val lessonRepository = LessonRepository(RetrofitClient.audioService)

    // 레슨 분석 상태(요약 결과 포함)만 관리
    private val _uiState = MutableStateFlow<LessonUiState>(LessonUiState.Idle)
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    /**
     * 녹음 파일을 서버에 업로드하고 요약 결과를 받아 UI 상태를 업데이트합니다.
     * @param file 녹음된 .wav 파일
     */
    fun uploadAndProcessRecording(file: File) {
        viewModelScope.launch {
            _uiState.value = LessonUiState.Loading
            val result = lessonRepository.processLesson(file)

            result.onSuccess { lessonData ->
                _uiState.value = LessonUiState.Success(lessonData)
                // [변경] 주석 적용 로직은 이제 필요 없으므로 모두 삭제합니다.
            }.onFailure { exception ->
                _uiState.value = LessonUiState.Error(exception.message ?: "알 수 없는 오류")
            }
        }
    }

    /**
     * UI 상태를 초기값으로 리셋합니다.
     * ReviewScreen을 벗어날 때 호출하면 좋습니다.
     */
    fun resetState() {
        _uiState.value = LessonUiState.Idle
    }
}