package com.lessonsync.app.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lessonsync.app.database.LessonSyncDatabase
import com.lessonsync.app.entity.AnnotationInfo
import com.lessonsync.app.entity.AnnotationRequest
import com.lessonsync.app.entity.LessonData
import com.lessonsync.app.entity.LessonUiState
import com.lessonsync.app.entity.Segment
import com.lessonsync.app.repository.LessonRepository
import com.lessonsync.app.repository.ScoreRepository
import com.lessonsync.app.retrofit.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class LessonViewModel(application: Application) : AndroidViewModel(application) {

    private val lessonRepository: LessonRepository
    private val scoreRepository: ScoreRepository

    private val _uiState = MutableStateFlow<LessonUiState>(LessonUiState.Idle)
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    init {
        // DB 인스턴스를 한 번만 가져옵니다.
        val db = LessonSyncDatabase.getDatabase(application)

        // 각 DAO를 가져옵니다.
        val scoreDao = db.scoreDao()
        val annotationDao = db.annotationDao() // annotationDao 가져오기

        // Repository를 생성할 때 두 DAO를 모두 전달합니다.
        scoreRepository = ScoreRepository(scoreDao, annotationDao)
        lessonRepository = LessonRepository(RetrofitClient.audioService)
    }

    // =================================================================
    // === 테스트를 위해 이 함수를 추가하세요! ===
    // =================================================================
    /**
     * assets 폴더에 있는 오디오 파일로 업로드 프로세스를 테스트합니다.
     * @param scoreId 테스트할 악보의 ID
     * @param assetFileName assets 폴더에 있는 오디오 파일 이름 (예: "test_lesson_file.wav")
     */
    fun testUploadFromAssets(scoreId: Int, assetFileName: String) {
        viewModelScope.launch {
            Log.d("FileUploadTest", "Assets에서 파일 업로드 테스트 시작: $assetFileName")
            try {
                // 1. Context를 통해 assets 파일의 InputStream을 엽니다.
                val context: Context = getApplication<Application>().applicationContext
                val inputStream = context.assets.open(assetFileName)

                // 2. InputStream을 실제 File 객체로 변환합니다.
                //    (서버에 보내려면 실제 파일이 필요하므로 캐시 디렉토리에 임시 파일을 생성합니다)
                val tempFile = File(context.cacheDir, assetFileName).apply {
                    FileOutputStream(this).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("FileUploadTest", "임시 파일 생성 완료: ${tempFile.absolutePath}")

                // 3. 생성된 파일로 기존의 업로드 함수를 호출합니다.
                uploadAndProcessRecording(scoreId, tempFile)

            } catch (e: Exception) {
                Log.e("FileUploadTest", "Assets 파일 처리 중 오류 발생", e)
                _uiState.value = LessonUiState.Error("테스트 파일을 처리하는 중 오류가 발생했습니다: ${e.message}")
            }
        }
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
                Log.d("LessonViewModel", "레슨 요약 및 전문 분석 성공: ${lessonData.summary}")

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
        Log.d("LessonViewModel", "전체 전문을 주석 파싱 요청: ${lessonData.speechSegments?.size ?: 0}개 세그먼트")
        val fullTranscript = lessonData.speechSegments?.joinToString(" ") { it.text } ?: ""
        if (fullTranscript.isBlank()) {
            // 주석으로 만들 텍스트가 없으면, 현재까지의 결과(요약, 빈 전문)만 저장
            saveAnalysisResultToDb(scoreId, lessonData.summary ?: "", "", emptyList())
            _uiState.value = LessonUiState.Success(lessonData)
            return
        }

        Log.d("LessonViewModel", "전체 전문: $fullTranscript")

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

    /**
     * [신규] 특정 악보의 리뷰 데이터를 로드하는 함수.
     * 먼저 DB를 확인하고, 데이터가 있으면 UI 상태를 업데이트합니다.
     */
    fun loadReviewData(scoreId: Int) {
        viewModelScope.launch {
            _uiState.value = LessonUiState.Loading // 로딩 상태 시작

            val analysis = scoreRepository.getLessonAnalysis(scoreId)
            val resultEntity = analysis.result

            if (resultEntity != null) {
                // DB에 데이터가 있는 경우

                // DB에 저장된 전체 텍스트(fullTranscript)를 UI가 사용할 수 있도록
                // Segment 객체 리스트로 만들어줍니다.
                // (DB에서 가져올 때는 start/end 시간이 없으므로 0.0으로 설정)
                val segmentsForUi = if (!resultEntity.fullTranscript.isNullOrBlank()) {
                    listOf(Segment(start = 0.0, end = 0.0, text = resultEntity.fullTranscript))
                } else {
                    null
                }

                // UI가 사용하는 LessonData 객체 생성
                val lessonData = LessonData(
                    summary = resultEntity.summary,
                    speechSegments = segmentsForUi
                )
                _uiState.value = LessonUiState.Success(lessonData)

            } else {
                // DB에 데이터가 없는 경우
                _uiState.value = LessonUiState.Error("분석된 레슨 데이터가 없습니다.")
            }
        }
    }
}