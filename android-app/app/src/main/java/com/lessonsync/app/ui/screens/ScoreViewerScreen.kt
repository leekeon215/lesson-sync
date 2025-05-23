package com.lessonsync.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.R
import com.lessonsync.app.navigation.Screen
import com.lessonsync.app.navigation.demoScores
import com.lessonsync.app.ui.theme.LessonSyncTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreViewerScreen(navController: NavHostController, scoreId: String) {
    val score = demoScores.find { it.id == scoreId }

    // true: 주석 보임 (녹음 완료 상태), false: 일반 악보 (녹음 전 또는 주석 숨김)
    var showAnnotations by remember { mutableStateOf(false) }

    // TODO: 이 상태는 실제 녹음 완료 여부에 따라 외부에서 주입받거나 ViewModel을 통해 관리되어야 합니다.
    // Preview 및 UI 데모를 위해 임시로 showAnnotations와 연동합니다.
    val isRecordingCompleted = showAnnotations


    val topBarBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val bottomControlsBackgroundColor = MaterialTheme.colorScheme.surfaceContainer

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(score?.title ?: "악보 보기", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    Switch(
                        checked = showAnnotations,
                        onCheckedChange = { showAnnotations = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = { navController.navigate(Screen.Review.route + "/$scoreId") }) {
                        Icon(Icons.AutoMirrored.Filled.Note, "레슨 요약 보기", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarBackgroundColor),
                windowInsets = WindowInsets(0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 72.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White), // 악보는 항상 흰 배경
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: 악보 렌더링 (WebView 또는 전용 라이브러리 사용)
                    Text(
                        if (showAnnotations) "[주석 포함 악보 렌더링]" else "[일반 악보 렌더링]",
                        color = Color.Black
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 88.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = { /* TODO: 악보 확대 로직 */ },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                ) { Icon(Icons.Default.ZoomIn, "확대") }
                FloatingActionButton(
                    onClick = { /* TODO: 악보 축소 로직 */ },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                ) { Icon(Icons.Default.ZoomOut, "축소") }
            }

            // 하단 컨트롤 바
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(bottomControlsBackgroundColor)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 녹음/재생 아이콘 버튼
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isRecordingCompleted) MaterialTheme.colorScheme.primary else Color(0xFFAA0000)) // 완료 시 다른 색
                        .clickable {
                            if (isRecordingCompleted) {
                                // TODO: 녹음된 파일 재생 로직
                            } else {
                                navController.navigate(Screen.Recording.route + "/$scoreId")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isRecordingCompleted) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow, // 재생 아이콘
                            contentDescription = "재생",
                            tint = MaterialTheme.colorScheme.onPrimary, // primary 색상 위의 아이콘 색
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        // TODO: 여기에 커스텀 녹음 아이콘 이미지를 넣으세요.
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_background), // 예시 (drawable에 추가 필요)
                            contentDescription = "녹음 시작",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                TextButton(
                    onClick = { /* TODO: 녹음/재생 라이브러리 화면으로 이동 */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        if (isRecordingCompleted) "재생 목록" else "녹음 라이브러리",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(onClick = { navController.navigate(Screen.ManualAnnotation.route + "/$scoreId") }) {
                    Icon(Icons.Default.TextFields, "수동 주석 입력", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(onClick = { /* TODO: 현재 보이는 주석 삭제 로직 (만약 주석이 선택 가능하다면) */ }) {
                    Icon(Icons.Default.DeleteOutline, "표시된 주석 삭제", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
// Preview 코드 동일하게 유지

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun ScoreViewerScreenPreview() {
    LessonSyncTheme {
        // Preview에서는 scoreId를 실제 있는 값으로 전달해야 함
        val firstScoreId = demoScores.firstOrNull()?.id ?: "1"
        ScoreViewerScreen(navController = rememberNavController(), scoreId = firstScoreId)
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ScoreViewerScreenDarkPreview() {
    LessonSyncTheme(useDarkTheme = true) {
        val firstScoreId = demoScores.firstOrNull()?.id ?: "1"
        ScoreViewerScreen(navController = rememberNavController(), scoreId = firstScoreId)
    }
}



