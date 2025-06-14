package com.lessonsync.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessonsync.app.repository.LessonRepository
import com.lessonsync.app.entity.LessonUiState
import com.lessonsync.app.retrofit.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class LessonViewModel : ViewModel() {

    private val lessonRepository = LessonRepository(RetrofitClient.audioService)

    private val _uiState = MutableStateFlow<LessonUiState>(LessonUiState.Idle)
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    fun uploadAndProcessRecording(file: File) {
        viewModelScope.launch {
            _uiState.value = LessonUiState.Loading // 로딩 상태로 변경
            val result = lessonRepository.processLesson(file)
            result.onSuccess { lessonData ->
                _uiState.value = LessonUiState.Success(lessonData) // 성공 상태
            }.onFailure { exception ->
                _uiState.value = LessonUiState.Error(exception.message ?: "알 수 없는 오류") // 에러 상태
            }
        }
    }

    // 처리가 끝나면 상태를 초기화하여 다음 요청을 받을 수 있도록 함
    fun resetState() {
        _uiState.value = LessonUiState.Idle
    }
}