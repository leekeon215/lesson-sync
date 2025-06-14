package com.lessonsync.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.entity.LessonUiState
import com.lessonsync.app.ui.theme.LessonSyncTheme
import com.lessonsync.app.viewmodel.LessonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    navController: NavHostController,
    scoreId: String,
    lessonViewModel: LessonViewModel // ViewModel을 파라미터로 받음
) {
    // ViewModel의 UI 상태를 구독합니다.
    val uiState by lessonViewModel.uiState.collectAsStateWithLifecycle()

    // 이 화면을 벗어날 때 ViewModel의 상태를 초기화하여 다음 요청에 영향을 주지 않도록 합니다.
    DisposableEffect(Unit) {
        onDispose {
            lessonViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("레슨 요약 및 분석") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // UI 상태에 따라 다른 화면을 보여줍니다.
            when (val state = uiState) {
                is LessonUiState.Success -> {
                    // 성공 시, 서버에서 받은 데이터로 UI를 구성합니다.
                    val lessonData = state.lessonData
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // summary가 null이 아니면 표시
                        if (!lessonData.summary.isNullOrBlank()) {
                            item {
                                ReviewSection(title = "📝 레슨 요약", content = lessonData.summary)
                            }
                        }

                        if (!lessonData.speechSegments.isNullOrEmpty()) {
                            item {
                                ReviewSection(
                                    title = "🎙️ 발화 구간 분석",
                                    content = lessonData.speechSegments.joinToString("\n") { "• ${it.text}" }
                                )
                            }
                        }
                    }
                }
                is LessonUiState.Error -> {
                    // 에러 발생 시
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("오류 발생", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message)
                    }
                }
                else -> {
                    // Idle 또는 Loading 상태 (보통 ProcessingScreen에서 처리되지만 안전장치)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("데이터를 불러오는 중입니다...")
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewSection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp
        )
    }
}