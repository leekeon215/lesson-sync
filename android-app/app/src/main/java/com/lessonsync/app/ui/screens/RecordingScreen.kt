package com.lessonsync.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.navigation.Screen
import com.lessonsync.app.navigation.demoScores
import com.lessonsync.app.ui.theme.LessonSyncTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(navController: NavHostController, scoreId: String) {
    val score = demoScores.find { it.id == scoreId }

    // 녹음 상태: true(녹음중/일시정지), false(정지됨)
    var isRecordingActive by remember { mutableStateOf(true) }
    // 재생 상태: true(재생/녹음중), false(일시정지)
    var isPlaying by remember { mutableStateOf(true) }
    var recordingTime by remember { mutableStateOf("00:00") } // TODO: 실제 녹음 시간 연동

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // TODO: 권한 허용 시 녹음 시작 로직
                isRecordingActive = true
                isPlaying = true
            } else {
                // TODO: 권한 거부 시 처리 (예: 이전 화면으로 돌아가거나 안내 메시지)
                navController.popBackStack()
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // 녹음 시간 시뮬레이션 (실제로는 녹음 라이브러리와 연동)
    LaunchedEffect(isPlaying, isRecordingActive) {
        if (isPlaying && isRecordingActive) {
            var seconds = 0
            while (isPlaying && isRecordingActive) {
                delay(1000)
                seconds++
                val mins = seconds / 60
                val secs = seconds % 60
                recordingTime = String.format("%02d:%02d", mins, secs)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(score?.title ?: Screen.Recording.title) }, // 악보 제목 표시
                navigationIcon = {
                    IconButton(onClick = {
                        // TODO: 녹음 중단 확인 로직 또는 바로 중단
                        isRecordingActive = false
                        isPlaying = false
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "녹음 중단")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 악보 렌더링 영역
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 남은 공간 차지
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White), // 악보는 흰색 배경
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // TODO: 악보 렌더링 (ScoreViewerScreen과 동일한 로직 사용 가능)
                    Text("[악보 렌더링 영역]", color = Color.Black)
                }
            }

            // 하단 컨트롤 바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp) // 패딩 증가
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly // 버튼들 균등 배치
            ) {
                // 녹음 시간 표시
                Text(
                    text = recordingTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 재생/일시정지 버튼
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.size(56.dp), // 버튼 크기
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "일시정지" else "계속 녹음",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 정지 버튼
                IconButton(
                    onClick = {
                        isRecordingActive = false
                        isPlaying = false
                        // 녹음 완료 후 ProcessingScreen으로 이동
                        navController.navigate(Screen.Processing.route) {
                            // RecordingScreen을 백스택에서 제거
                            popUpTo(Screen.Recording.route + "/$scoreId") { inclusive = true }
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer, // 정지 버튼은 error 계열 색상 사용
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "녹음 정지 및 완료",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecordingScreenPreview() {
    LessonSyncTheme {
        RecordingScreen(navController = rememberNavController(), scoreId = "1")
    }
}
