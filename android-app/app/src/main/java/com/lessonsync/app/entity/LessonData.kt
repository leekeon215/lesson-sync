package com.lessonsync.app.entity

// 서버 응답을 파싱하기 위한 데이터 클래스
data class Segment(
    val start: Double,
    val end: Double,
    val text: String
)

data class LessonData(
    val speechSegments: List<Segment>?,
    val summary: String?
)

// UI 상태를 나타내는 Sealed Interface
sealed interface LessonUiState {
    data object Idle : LessonUiState
    data object Loading : LessonUiState
    data class Success(val lessonData: LessonData) : LessonUiState
    data class Error(val message: String) : LessonUiState
}