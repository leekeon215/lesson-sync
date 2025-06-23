// android-app/app/src/main/java/com/lessonsync/app/viewmodel/ScoreViewModel.kt

package com.lessonsync.app.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.lessonsync.app.database.LessonSyncDatabase
import com.lessonsync.app.entity.AnnotationEntity
import com.lessonsync.app.entity.ScoreEntity
import com.lessonsync.app.repository.AnnotationRepository
import com.lessonsync.app.repository.ScoreRepository
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

    init {
        val database = LessonSyncDatabase.getDatabase(application)
        scoreRepository = ScoreRepository(database.scoreDao())
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

    fun deleteAnnotationsForScore(scoreId: Int) {
        viewModelScope.launch {
            annotationRepository.deleteAnnotationsForScore(scoreId)
        }
    }
}