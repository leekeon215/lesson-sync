// ============================== app/src/main/java/com/lessonsync/app/navigation/Screen.kt
package com.lessonsync.app.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "악보 목록")
    data object Search : Screen("search", "검색")
    data object Settings : Screen("settings", "설정")
    data object ScoreViewer : Screen("viewer", "악보 보기")
    data object Recording : Screen("record", "녹음 중")
    data object Processing : Screen("processing", "분석 중")
    data object Review : Screen("review", "주석 악보")
    data object ManualAnnotation : Screen("manualAnno", "주석 입력")
    data object Summary : Screen("summary", "레슨 요약")
    data object AddScore : Screen("addScore", "악보 추가")
}