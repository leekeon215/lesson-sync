package com.lessonsync.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.navigation.Screen
import com.lessonsync.app.ui.theme.LessonSyncTheme
import kotlinx.coroutines.delay

@Composable
fun ProcessingScreen(navController: NavHostController) {
    // TODO: 실제 scoreId를 이전 화면에서 전달받아 ReviewScreen으로 넘겨야 함
    val tempScoreIdForNavigation = "1" // 임시 ID

    LaunchedEffect(Unit) {
        delay(3000) // 3초 딜레이 (시뮬레이션)
        // ScoreViewer 화면을 백스택에서 찾아 제거하고 Review 화면으로 이동
        navController.popBackStack(Screen.ScoreViewer.route + "/$tempScoreIdForNavigation", inclusive = true, saveState = false)
        // inclusive = true 로 하면 ScoreViewer도 스택에서 제거됨.
        // 만약 ScoreViewer로 돌아가고 싶다면 inclusive = false 또는 popUpTo 사용
        navController.navigate(Screen.Review.route + "/$tempScoreIdForNavigation")
    }

    Scaffold( // Scaffold를 사용하여 배경색 등 테마 적용
        containerColor = MaterialTheme.colorScheme.surface // 이미지의 흰색 배경
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 이미지에는 없지만, 로딩 인디케이터 추가
                // CircularProgressIndicator(modifier = Modifier.size(48.dp))
                // Spacer(Modifier.height(24.dp))
                Text(
                    text = "변환 및 주석 삽입 중 ...", // 이미지의 텍스트
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProcessingScreenPreview() {
    LessonSyncTheme {
        ProcessingScreen(navController = rememberNavController())
    }
}