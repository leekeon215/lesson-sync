// android-app/app/src/main/java/com/lessonsync/app/viewmodel/ScoreViewModel.kt

package com.lessonsync.app.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.lessonsync.app.database.LessonSyncDatabase
import com.lessonsync.app.entity.AnnotationEntity
import com.lessonsync.app.entity.AnnotationRequest
import com.lessonsync.app.entity.ScoreEntity
import com.lessonsync.app.repository.AnnotationRepository
import com.lessonsync.app.repository.ScoreRepository
import com.lessonsync.app.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileOutputStream

class ScoreViewModel(application: Application) : AndroidViewModel(application) {

    private val scoreRepository: ScoreRepository
    private val annotationRepository: AnnotationRepository

    val allScores: LiveData<List<ScoreEntity>>

    private val _searchResults = MutableStateFlow<List<ScoreEntity>>(emptyList())
    val searchResults: StateFlow<List<ScoreEntity>> = _searchResults.asStateFlow()

    private val _selectedScore = MutableStateFlow<ScoreEntity?>(null)
    val selectedScore : StateFlow<ScoreEntity?> = _selectedScore.asStateFlow()

    private val _annotations = MutableStateFlow<List<AnnotationEntity>>(emptyList())
    val annotations: StateFlow<List<AnnotationEntity>> = _annotations.asStateFlow()

    private val scoreService = RetrofitClient.scoreService



    // 테스트를 위한 함수
    fun testParseDirectives(scoreId: Int) {
        // 이전에 파이썬 테스트 코드에서 사용했던 샘플 텍스트
        val sampleText = """
            안녕하세요. 오늘 레슨 시작하겠습니다.
            먼저 5마디부터 조금 빠르게 연주해볼까요? 여기는 좀 더 강한 느낌으로요.
            좋아요. 다음으로 12번째 마디는 부드럽게 이어주세요. 
            음... 20마디에서 박자가 약간 틀렸는데, 거기는 조금만 더 느리게 해봅시다.
            그리고 5마디 부분 다시 한번만 더 신경 써주세요.
            마지막으로 35마디는 아주 작고 여리게 끝내주면 좋겠습니다.
            """.trimIndent()

        val request = AnnotationRequest(text = sampleText)

        // 코루틴을 사용하여 비동기 네트워크 요청을 보냄
        viewModelScope.launch {
            try {
                Log.d("APITEST", "🚀 서버에 주석 파싱을 요청합니다...")
                Log.d("APITEST", "보내는 텍스트: $sampleText")

                // Retrofit 서비스를 통해 API를 호출합니다.
                val response = scoreService.getAnnotations(request)

                if (response.isSuccessful) {
                    // HTTP 응답이 성공적일 때 (2xx 상태 코드)
                    val annotationResponse = response.body()

                    if (annotationResponse != null) {
                        // === 이 부분이 가장 중요합니다 ===
                        // 받은 데이터를 Logcat에 직접 출력하여 확인합니다.
                        Log.d("APITEST", "✅ 요청 성공! 받은 데이터:")
                        Log.d("APITEST", "전체 응답: ${annotationResponse}")

                        // 받은 주석 리스트를 하나씩 출력
                        annotationResponse.annotations.forEachIndexed { index, annotation ->
                            Log.d("APITEST", "  주석[${index}]: 마디=${annotation.measure}, 지시어='${annotation.directive}'")
                            if (annotation.directive.isNotBlank()) {
                            addAnnotation(
                                scoreOwnerId = scoreId,
                                measureNumber = annotation.measure,
                                directive = annotation.directive
                            )
                        }

                        }

                        // TODO: 여기서 받은 annotationResponse.annotations 데이터를
                        //       Room DB나 다른 곳에 저장하는 로직을 호출하면 됩니다.
                        loadScoreAndAnnotations(scoreId)

                    } else {
                        Log.e("APITEST", "❌ 요청은 성공했지만 응답 본문이 비어있습니다.")
                    }
                } else {
                    // 서버가 에러 응답을 보냈을 때 (4xx, 5xx 상태 코드)
                    val errorBody = response.errorBody()?.string()
                    Log.e("APITEST", "❌ 서버 에러 응답: ${response.code()} / ${errorBody}")
                }
            } catch (e: Exception) {
                // 네트워크 연결 실패 등 예외 상황 발생 시
                Log.e("APITEST", "❌ 요청 중 예외 발생!", e)
            }
        }
    }




    init {
        val database = LessonSyncDatabase.getDatabase(application)
        scoreRepository = ScoreRepository(database.scoreDao(), database.annotationDao())
        annotationRepository = AnnotationRepository(database.annotationDao())

        allScores = scoreRepository.allScores.asLiveData()
    }

    //악보를 추가하는 함수
    fun insert(score: ScoreEntity) = viewModelScope.launch {
        scoreRepository.insert(score)
    }

    //특정 악보의 정보를 불러오는 함수(악보만)
    fun getScoreById(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        val score = scoreRepository.getScoreById(id)
        _selectedScore.value = score
    }

    //악보를 삭제하는 함수
    fun deleteByIds(ids: List<Int>) = viewModelScope.launch {
        scoreRepository.deleteByIds(ids)
    }

    //
    fun addScoreFromUri(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        val context = getApplication<Application>().applicationContext
        val contentResolver = context.contentResolver

        // 1. Uri에서 파일 이름 가져오기
        var fileName = "new_score.musicxml"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }

        // 2. 앱 내부 저장소에 파일 복사
        val internalFile = context.getFileStreamPath(fileName)
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(internalFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        // 3. Room DB에 ScoreEntity 저장
        val scoreEntity = ScoreEntity(
            title = fileName.substringBeforeLast("."), // 확장자 제외
            filePath = internalFile.absolutePath // 앱 내부 경로 저장
        )
        scoreRepository.insert(scoreEntity)
    }

    // 악보 검색 기능
    fun searchScores(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                // LIKE 쿼리를 위해 앞뒤에 % 와일드카드 추가
                scoreRepository.searchScores("%${query}%").collect { results ->
                    _searchResults.value = results
                }
            }
        }
    }

    // --- 주석 및 통합 로딩 함수 ---

    //주석 + 악보를 함께 불러오는 함수
    fun loadScoreAndAnnotations(scoreId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // 악보 정보 로드
            _selectedScore.value = scoreRepository.getScoreById(scoreId)

            // 주석 정보 로드 (Flow 구독 시작)
            // Main 스레드에서 Flow를 구독해야 할 수 있으므로 launch 블록을 분리
            launch {
                annotationRepository.getAnnotationsForScore(scoreId).collect { annotationList ->
                    _annotations.value = annotationList
                }
            }
        }
    }

    // 악보에 달려있는 주석 다 불러오는 함수
    fun loadAnnotationsForScore(scoreOwnerId: Int) {
        viewModelScope.launch {
            annotationRepository.getAnnotationsForScore(scoreOwnerId).collect { annotationList ->
                _annotations.value = annotationList
            }
        }
    }

    fun addAnnotation(scoreOwnerId: Int, measureNumber: Int, directive: String) {
        viewModelScope.launch {
            // 5. scoreId 타입을 Int로 통일 (DAO가 Int를 받는다는 가정 하에)
            val newAnnotation = AnnotationEntity(
                scoreOwnerId = scoreOwnerId,
                measureNumber = measureNumber,
                directive = directive
            )
            annotationRepository.insert(newAnnotation)
        }
    }

    // --- ▼ [추가] 주석 삭제 함수 ---
    fun deleteAnnotation(scoreOwnerId: Int, measureNumber: Int) {
        viewModelScope.launch {
            // 예시: annotationRepository에 삭제 함수가 있다고 가정
             annotationRepository.deleteAnnotation(scoreOwnerId, measureNumber)

            // 삭제 후, 주석 목록을 다시 로드하여 UI를 갱신합니다.
            loadScoreAndAnnotations(scoreOwnerId)
        }
    }

    fun deleteAnnotationsForScore(scoreId: Int) {
        viewModelScope.launch {
            annotationRepository.deleteAnnotationsForScore(scoreId)
        }
    }


}