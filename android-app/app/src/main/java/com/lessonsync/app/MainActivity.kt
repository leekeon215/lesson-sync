package com.lessonsync.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lessonsync.app.navigation.LessonSyncApp
import com.lessonsync.app.ui.theme.LessonSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // isSystemInDarkTheme() 호출을 Composable 컨텍스트 내부로 이동
            val systemIsDark = isSystemInDarkTheme()
            var useDarkTheme by remember { mutableStateOf(systemIsDark) }

            LessonSyncTheme(useDarkTheme = useDarkTheme) {
                LessonSyncApp(
                    currentDarkTheme = useDarkTheme,
                    onToggleDarkTheme = { newDarkThemeState ->
                        useDarkTheme = newDarkThemeState
                    }
                )
            }
        }
    }
}