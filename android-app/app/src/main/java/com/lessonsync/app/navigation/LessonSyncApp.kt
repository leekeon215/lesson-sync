// android-app/app/src/main/java/com/lessonsync/app/navigation/LessonSyncApp.kt
package com.lessonsync.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.ui.screens.*
import com.lessonsync.app.viewmodel.LessonViewModel
import com.lessonsync.app.viewmodel.ScoreViewModel

@Composable
fun LessonSyncApp(
    currentDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    // 공통으로 사용할 ViewModel 인스턴스 생성
    val lessonViewModel: LessonViewModel = viewModel()
    val scoreViewModel: ScoreViewModel = viewModel()


    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 각 화면에 동일한 ViewModel 인스턴스를 전달합니다.
            composable(Screen.Home.route) {
                HomeScreen(navController, scoreViewModel) // scoreViewModel 전달
            }

            composable(Screen.AddScore.route) {
                AddScoreScreen(navController, scoreViewModel) // scoreViewModel 전달
            }

            composable(Screen.Search.route) {
                SearchScreen(navController, scoreViewModel)
            }

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
                // lessonViewModel 전달
                RecordingScreen(navController, id, lessonViewModel)
            }
            composable(Screen.Processing.route + "/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                // lessonViewModel 전달
                ProcessingScreen(navController, id, lessonViewModel)
            }
            composable(Screen.Review.route + "/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                // lessonViewModel 전달
                ReviewScreen(navController, id, lessonViewModel)
            }
            composable(Screen.ManualAnnotation.route + "/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                ManualAnnotationScreen(navController, id)
            }
            composable(Screen.Summary.route + "/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                SummaryScreen(navController, id)
            }
        }
    }
}