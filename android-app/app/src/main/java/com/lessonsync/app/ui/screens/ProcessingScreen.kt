package com.lessonsync.app.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.lessonsync.app.entity.LessonUiState
import com.lessonsync.app.navigation.Screen
import com.lessonsync.app.noitification.LessonNotificationReceiver
import com.lessonsync.app.viewmodel.LessonViewModel
import kotlinx.coroutines.delay

@Composable
fun ProcessingScreen(
    navController: NavHostController,
    scoreId: String,
    lessonViewModel: LessonViewModel = viewModel()
) {
    val context = LocalContext.current
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var isProcessingFinished by remember { mutableStateOf(false) }
    var isBackPressed by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showNotificationPermissionDialog = true
        }
    }

    val uiState by lessonViewModel.uiState.collectAsStateWithLifecycle()

    // uiState가 변경될 때마다 실행
    LaunchedEffect(uiState) {
        when (uiState) {
            is LessonUiState.Success -> {
                // 성공 시 ReviewScreen으로 이동
                // 이전 화면 스택을 정리하고 싶다면 popUpTo 사용
                navController.navigate(Screen.Review.route + "/$scoreId") {
                    popUpTo(Screen.Home.route)
                }
            }
            is LessonUiState.Error -> {
                // 에러 처리 (예: Toast 메시지, 이전 화면으로 복귀)
                // Toast.makeText(context, (uiState as LessonUiState.Error).message, Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
            else -> {
                // Loading 또는 Idle 상태
            }
        }
    }

    LaunchedEffect(Unit) {
        // 알림 권한 요청 (Android 13 이상)
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // 항상 백그라운드 알림 예약
        val prefs = context.getSharedPreferences("LessonSyncPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("lessonSummaryNotification", true)) {
            scheduleNotification(context, scoreId)
        }

        delay(5000) // 서버 응답 시뮬레이션 (예: 5초 후 응답 도착)
        isProcessingFinished = true

        // 응답 도착 여부를 SharedPreferences에 저장
        context.getSharedPreferences("LessonSyncPrefs", Context.MODE_PRIVATE)
            .edit {
                putBoolean("summaryReady_$scoreId", true)
            }

        if (!isBackPressed) {
            navController.popBackStack(Screen.ScoreViewer.route + "/$scoreId", inclusive = false)
            navController.navigate(Screen.Review.route + "/$scoreId")
        } else {
            // 뒤로가기로 빠져나가더라도 알림은 이미 예약됨 (중복 예약 방지 가능)
        }
    }

    BackHandler {
        isBackPressed = true
        navController.popBackStack(Screen.ScoreViewer.route + "/$scoreId", inclusive = false)
    }

    if (showNotificationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationPermissionDialog = false },
            confirmButton = {
                TextButton(onClick = { showNotificationPermissionDialog = false }) {
                    Text("확인")
                }
            },
            title = { Text("알림 권한 필요") },
            text = { Text("레슨 요약 알림을 받기 위해 알림 권한이 필요합니다. 설정에서 권한을 허용해주세요.") }
        )
    }

    // 로딩 중 UI
    Scaffold(containerColor = MaterialTheme.colorScheme.surface) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "레슨 분석 중...",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

}

fun scheduleNotification(context: Context, scoreId: String) {
    val intent = Intent(context, LessonNotificationReceiver::class.java).apply {
        putExtra("scoreId", scoreId)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        1001,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.set(
        AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + 10000,
        pendingIntent
    )
}