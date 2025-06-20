package com.lessonsync.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessonsync.app.entity.AnnotationInfo
import com.lessonsync.app.entity.AnnotationRequest
import com.lessonsync.app.entity.LessonData
import com.lessonsync.app.entity.LessonUiState
import com.lessonsync.app.repository.LessonRepository
import com.lessonsync.app.retrofit.RetrofitClient
import com.lessonsync.app.util.MusicXmlEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

class LessonViewModel : ViewModel() {

    private val lessonRepository = LessonRepository(RetrofitClient.audioService)

    // 레슨 요약/분석 UI 상태 (ProcessingScreen, ReviewScreen에서 사용)
    private val _uiState = MutableStateFlow<LessonUiState>(LessonUiState.Idle)
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    // 원본 및 주석이 적용된 악보 XML 데이터를 관리할 StateFlow (ScoreViewerScreen, ReviewScreen에서 사용)
    private val _scoreXmlState = MutableStateFlow<String?>(null)
    val scoreXmlState: StateFlow<String?> = _scoreXmlState.asStateFlow()

    /**
     * 화면 진입 시, 파일 경로로부터 원본 악보 XML을 로드하여 상태를 설정합니다.
     * @param scoreFilePath 앱 내부 저장소의 악보 파일 절대 경로
     */
    fun loadInitialScoreXml(scoreFilePath: String?) {
        if (scoreFilePath == null) {
            _scoreXmlState.value = null // 또는 오류 상태 처리
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(scoreFilePath)
                val xmlContent = FileInputStream(file).bufferedReader(Charset.forName("UTF-8")).use {
                    it.readText()
                }
                withContext(Dispatchers.Main) {
                    _scoreXmlState.value = xmlContent
                }
            } catch (e: Exception) {
                // 파일 읽기 오류 처리
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _scoreXmlState.value = null
                }
            }
        }
    }

    /**
     * 녹음 파일을 서버에 업로드하고 전체 분석 프로세스를 시작합니다.
     * @param file 녹음된 .wav 파일
     */
    fun uploadAndProcessRecording(file: File) {
        val originalXml = _scoreXmlState.value
        if (originalXml == null) {
            _uiState.value = LessonUiState.Error("주석을 적용할 원본 악보가 없습니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LessonUiState.Loading
            val result = lessonRepository.processLesson(file)

            result.onSuccess { lessonData ->
                // STT 및 요약이 성공하면, 받은 텍스트로 주석 생성 프로세스 시작
                _uiState.value = LessonUiState.Success(lessonData)
                processToCreateAnnotations(lessonData)
            }.onFailure { exception ->
                _uiState.value = LessonUiState.Error(exception.message ?: "알 수 없는 오류")
            }
        }
    }

    private fun processToCreateAnnotations(lessonData: LessonData) {
        val fullTranscript = lessonData.speechSegments?.joinToString(" ") { it.text } ?: ""
        if (fullTranscript.isBlank()) {
            // 주석으로 만들 텍스트가 없으면 여기서 종료
            return
        }

        viewModelScope.launch {
            val request = AnnotationRequest(text = fullTranscript)
            val result = lessonRepository.fetchAnnotations(request)

            result.onSuccess { annotations ->
                // 받은 정보로 악보 XML 수정
                applyAnnotationsToScore(annotations)
            }.onFailure { exception ->
                _uiState.value = LessonUiState.Error(exception.message ?: "주석 정보를 가져오는 데 실패했습니다.")
            }
        }
    }

    private fun applyAnnotationsToScore(annotations: List<AnnotationInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            val originalXml = _scoreXmlState.value ?: return@launch

            try {
                val editor = MusicXmlEditor(originalXml)
                annotations.forEach { annotation ->
                    editor.addTextAnnotation(
                        measureNumber = annotation.measure,
                        text = annotation.directive
                    )
                }
                val updatedXml = editor.getUpdatedXml()

                withContext(Dispatchers.Main) {
                    _scoreXmlState.value = updatedXml
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = LessonUiState.Error("악보에 주석을 적용하는 중 오류가 발생했습니다.")
                }
            }
        }
    }

    /**
     * 모든 상태를 초기값으로 리셋합니다.
     */
    fun resetState() {
        _uiState.value = LessonUiState.Idle
        _scoreXmlState.value = null
    }
}