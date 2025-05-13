// MainActivity.kt
package com.lessonsync.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.navigation.Route
import com.lessonsync.app.ui.LessonRecordScreen
import com.lessonsync.app.ui.LessonSummaryScreen
import com.lessonsync.app.ui.theme.LessonSyncTheme
import com.lessonsync.app.uicomponent.ProcessingScreen
import com.lessonsync.app.uicomponent.ScoreUploadScreen
import com.lessonsync.app.uicomponent.ScoreViewerScreen
import com.lessonsync.app.uicomponent.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LessonSyncRoot() }
    }
}

@Composable
fun LessonSyncRoot() {
    LessonSyncTheme {
        val navController = rememberNavController()
        Scaffold { padding ->
            NavHost(
                navController = navController,
                startDestination = Route.Upload.path,
                modifier = Modifier.padding(padding)
            ) {
                composable(Route.Upload.path)     { ScoreUploadScreen(navController) }
                composable(Route.Record.path)     { LessonRecordScreen(navController) }
                composable(Route.Processing.path) { ProcessingScreen(navController) }
                composable(Route.Viewer.path)     { ScoreViewerScreen(navController) }
                composable(Route.Summary.path)    { LessonSummaryScreen(navController) }
                composable(Route.Settings.path)   { SettingsScreen(navController) }
            }
        }
    }
}
