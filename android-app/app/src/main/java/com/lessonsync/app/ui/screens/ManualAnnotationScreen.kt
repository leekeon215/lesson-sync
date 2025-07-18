package com.lessonsync.app.ui.screens

// import androidx.compose.foundation.layout.width // 사용하지 않으므로 제거 가능
import ScoreWebView
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.navigation.demoScores
import com.lessonsync.app.ui.theme.LessonSyncTheme
import com.lessonsync.app.viewmodel.ScoreViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.core.graphics.convertTo

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAnnotationScreen(navController: NavHostController, scoreId: String) {

    val parentEntry = remember(navController) {
        navController.getBackStackEntry("score_details_graph") // 1단계에서 정의한 그래프 route
    }

    val scoreViewModel: ScoreViewModel = viewModel(viewModelStoreOwner = parentEntry)
    val scoreState by scoreViewModel.selectedScore.collectAsState()
    val annotations by scoreViewModel.annotations.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) } // 삭제 확인 다이얼로그 상태
    var measureToDelete by remember { mutableIntStateOf(0) } // 삭제할 마디 번호

    var annotationText by remember { mutableStateOf("") }
    var currentMeasure by remember { mutableIntStateOf(1) } // 예시 마디 번호
    val maxMeasures = 32 // 예시 최대 마디 번호 (실제 악보에 따라 동적으로 설정 필요)

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // 화면 진입 시 악보와 주석 데이터를 로드
    LaunchedEffect(scoreId) {
        // 악보 정보 로드
        scoreViewModel.getScoreById(scoreId.toInt())

        // 주석 데이터 로드
        scoreViewModel.loadScoreAndAnnotations(scoreId.toInt())

        // 로그 출력
        Log.d("ManualAnnotationScreen", "Loaded score ID: $scoreId with ${annotations.size} annotations")
    }

    // 삭제 요청 시 다이얼로그 표시
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("주석 삭제") },
            text = { Text("${measureToDelete}번째 마디의 주석을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // ViewModel의 삭제 함수 호출
                        scoreState?.id?.let {
                            scoreViewModel.deleteAnnotation(it, measureToDelete)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("예")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("아니오")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(scoreState?.title ?: "수동 주석") }, // 악보 제목 또는 기본 제목
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // 주석 저장 로직
                        Log.d("ManualAnnotationScreen", "주석 저장: 마디 $currentMeasure, 내용: $annotationText")
                        if (annotationText.isNotBlank()) {
                            scoreViewModel.addAnnotation(
                                scoreOwnerId = scoreState?.id ?: 0, // 현재 악보 ID
                                measureNumber = currentMeasure,
                                directive = annotationText
                            )
                            annotationText = "" // 입력 필드 초기화

                            // 주석 데이터를 다시 로드하여 UI에 즉시 반영되도록 함
                            scoreViewModel.loadScoreAndAnnotations(scoreId.toInt())
                        }
                        keyboardController?.hide() // 키보드 숨기기
                    }) {
                        Icon(Icons.Filled.Check, "주석 저장")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                ),
                windowInsets = WindowInsets(0.dp) // 상태 표시줄 공백 제거
            )
        },
        containerColor = MaterialTheme.colorScheme.background // 전체 화면 배경
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold의 기본 패딩 적용
                .navigationBarsPadding() // 하단 시스템 바 패딩
                .imePadding() // 키보드 패딩 (키보드가 UI를 가리지 않도록)
        ) {
            // 1. 악보 렌더링 영역
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 남은 공간을 차지하되, 하단 입력 UI를 위한 공간 남김 (비율 조정 가능)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White), // 악보는 항상 흰색 배경
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // 악보 렌더링
                    if (scoreState?.filePath.isNullOrBlank()) {
                        Text("악보를 불러오는 중...", color = Color.Black)
                    } else {
                        // key를 여러 파라미터로 설정하여 변경될 때마다 새로고침
                        key(scoreState!!.filePath, annotations.size, currentMeasure) {
                            Log.d("ManualAnnotationScreen", "Rendering ScoreWebView with ${annotations.size} annotations, highlighted measure: $currentMeasure")

                            ScoreWebView(
                                filePath = scoreState!!.filePath,
                                modifier = Modifier.fillMaxSize(),
                                zoomLevel = 0.7f, // 기본 줌 레벨 또는 상태 변수 전달
                                annotations = annotations, // ViewModel에서 가져온 기존 주석들
                                showAnnotations = true, // 주석 화면이므로 항상 주석 표시
                                highlightedMeasure = currentMeasure, // 현재 선택된 마디 번호 전달
                                onDeleteReuqest = { measureNumber ->
                                    // 삭제 요청 콜백
                                    measureToDelete = measureNumber
                                    showDeleteDialog = true // 다이얼로그 표시
                                    Log.d(
                                        "ManualAnnotationScreen",
                                        "Delete request for measure: $measureNumber"
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // 2. 주석 입력 및 컨트롤 영역 (스크롤 가능하도록)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()), // 내용이 많아지면 스크롤
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp)) // 상단 악보 카드와의 간격

                Text(
                    text = "삽입할 주석 입력",
                    style = MaterialTheme.typography.titleMedium, // 이미지와 유사하게 크기 조정
                    fontWeight = FontWeight.SemiBold, // 이미지와 유사하게 두께 조정
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 마디 번호 선택 UI
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    IconButton(onClick = {
                        if (currentMeasure > 1) {
                            currentMeasure--
                            // 마디 번호가 변경될 때마다 로그 출력
                            Log.d("ManualAnnotationScreen", "Selected measure: $currentMeasure")
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "이전 마디")
                    }
                    Text(
                        text = "$currentMeasure", // 현재 선택된 마디 표시
                        style = MaterialTheme.typography.headlineSmall, // 마디 번호 크기 조정
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    IconButton(onClick = {
                        if (currentMeasure < maxMeasures) {
                            currentMeasure++
                            // 마디 번호가 변경될 때마다 로그 출력
                            Log.d("ManualAnnotationScreen", "Selected measure: $currentMeasure")
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "다음 마디")
                    }
                }

                // 주석 입력 필드 제목
                Text(
                    text = "입력",
                    style = MaterialTheme.typography.labelMedium, // 레이블 크기 조정
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp, top = 8.dp),
                    textAlign = TextAlign.Start
                )
                OutlinedTextField(
                    value = annotationText,
                    onValueChange = { annotationText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp) // 높이 약간 줄임 (전체 화면 비율 고려)
                        .focusRequester(focusRequester), // 포커스 요청자 연결
                    placeholder = { Text("여기에 주석을 입력하세요...") },
                    label = { Text("주석 내용") }, // Label 추가 (선택 사항)
                    shape = RoundedCornerShape(8.dp), // 모서리 약간 조정
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary, // 포커스 시 레이블 색상
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 5 // 여러 줄 입력 가능
                )
                Spacer(modifier = Modifier.height(16.dp)) // 하단 여백
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun ManualAnnotationScreenPreview() {
    LessonSyncTheme {
        ManualAnnotationScreen(navController = rememberNavController(), scoreId = "1")
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ManualAnnotationScreenDarkPreview() {
    LessonSyncTheme(useDarkTheme = true) {
        ManualAnnotationScreen(navController = rememberNavController(), scoreId = "1")
    }
}