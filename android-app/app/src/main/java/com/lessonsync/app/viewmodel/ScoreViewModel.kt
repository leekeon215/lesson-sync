// android-app/app/src/main/java/com/lessonsync/app/viewmodel/ScoreViewModel.kt

package com.lessonsync.app.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.lessonsync.app.database.LessonSyncDatabase
import com.lessonsync.app.entity.ScoreEntity
import com.lessonsync.app.repository.ScoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileOutputStream

class ScoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScoreRepository

    val allScores: LiveData<List<ScoreEntity>>

    private val _searchResults = MutableStateFlow<List<ScoreEntity>>(emptyList())
    val searchResults: StateFlow<List<ScoreEntity>> = _searchResults.asStateFlow()

    init {
        val scoreDao = LessonSyncDatabase.getDatabase(application).scoreDao()
        repository = ScoreRepository(scoreDao)
        allScores = repository.allScores.asLiveData()
    }

    fun insert(score: ScoreEntity) = viewModelScope.launch {
        repository.insert(score)
    }

    fun deleteByIds(ids: List<Int>) = viewModelScope.launch {
        repository.deleteByIds(ids)
    }

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
        repository.insert(scoreEntity)
    }

    fun searchScores(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                // LIKE 쿼리를 위해 앞뒤에 % 와일드카드 추가
                repository.searchScores("%${query}%").collect { results ->
                    _searchResults.value = results
                }
            }
        }
    }
}