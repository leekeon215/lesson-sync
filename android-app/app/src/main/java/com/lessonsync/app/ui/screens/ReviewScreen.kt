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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.lessonsync.app.entity.LessonUiState
import com.lessonsync.app.viewmodel.LessonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    navController: NavHostController,
    scoreId: String, // scoreId는 유지 (어떤 악보에 대한 요약인지 식별용)
    lessonViewModel: LessonViewModel = viewModel()
) {
    // ViewModel의 UI 상태를 구독
    val uiState by lessonViewModel.uiState.collectAsStateWithLifecycle()

    // 이 화면을 벗어날 때 ViewModel의 상태를 초기화하여
    // 다음 요청에 영향을 주지 않도록 함
    DisposableEffect(Unit) {
        onDispose {
            lessonViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("레슨 요약") }, // 제목 변경
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp) // 전체적인 패딩 추가
        ) {
            // UI 상태에 따라 다른 화면을 표시
            when (val state = uiState) {
                is LessonUiState.Success -> {
                    // 성공 시, 서버에서 받은 요약 데이터로 UI를 구성
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // summary가 null이 아니면 표시
                        state.lessonData.summary?.let { summary ->
                            item {
                                ReviewSection(title = "📝 레슨 요약", content = summary)
                            }
                        }

                        item {
                            // correctedTranscript가 있으면 그것을 표시
                            if (!state.lessonData.correctedTranscript.isNullOrBlank()) {
                                ReviewSection(
                                    title = "🎙️ 전체 발화 내용",
                                    content = state.lessonData.correctedTranscript
                                )
                            }
                            // correctedTranscript가 없으면 (이전 버전 호환 등), 기존 방식대로 speechSegments를 조합해 표시
                            else if (!state.lessonData.speechSegments.isNullOrEmpty()) {
                                val fullTranscript = state.lessonData.speechSegments.joinToString("\n") { "• ${it.text}" }
                                ReviewSection(
                                    title = "🎙️ 전체 발화 내용 (원본)",
                                    content = fullTranscript
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
                is LessonUiState.Loading, LessonUiState.Idle -> {
                    // 로딩 중 또는 초기 상태
                    // (보통 ProcessingScreen에서 처리되지만, 직접 이 화면에 올 경우를 대비)
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

// ReviewSection Composable은 재사용
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