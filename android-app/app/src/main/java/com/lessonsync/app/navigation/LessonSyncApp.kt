package com.lessonsync.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.ui.screens.AddScoreScreen
import com.lessonsync.app.ui.screens.HomeScreen
import com.lessonsync.app.ui.screens.ManualAnnotationScreen
import com.lessonsync.app.ui.screens.ProcessingScreen
import com.lessonsync.app.ui.screens.RecordingScreen
import com.lessonsync.app.ui.screens.ReviewScreen
import com.lessonsync.app.ui.screens.ScoreViewerScreen
import com.lessonsync.app.ui.screens.SearchScreen
import com.lessonsync.app.ui.screens.SettingScreen
import com.lessonsync.app.ui.screens.SummaryScreen

@Composable
fun LessonSyncApp(
    currentDarkTheme: Boolean, // 현재 다크 모드 상태를 전달받음
    onToggleDarkTheme: (Boolean) -> Unit // 다크 모드 상태 변경 함수를 전달받음
) {
    val navController = rememberNavController()
    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Search.route) { SearchScreen(navController) }
            // SettingScreen으로 다크 모드 상태와 토글 함수 전달
            composable(Screen.Settings.route) {
                SettingScreen(
                    navController = navController,
                    currentDarkTheme = currentDarkTheme,
                    onToggleDarkTheme = onToggleDarkTheme
                )
            }
            composable(Screen.ScoreViewer.route + "/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                ScoreViewerScreen(navController, id)
            }
            composable(Screen.Recording.route + "/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                RecordingScreen(navController, id)
            }
            composable(Screen.Processing.route) { ProcessingScreen(navController) }
            composable(Screen.Review.route + "/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                ReviewScreen(navController, id)
            }
            composable(Screen.ManualAnnotation.route + "/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                ManualAnnotationScreen(navController, id)
            }
            composable(Screen.Summary.route + "/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                SummaryScreen(navController, id)
            }
            composable(Screen.AddScore.route) { AddScoreScreen(navController) }
        }
    }
}