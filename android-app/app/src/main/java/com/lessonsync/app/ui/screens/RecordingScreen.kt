package com.lessonsync.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.lessonsync.app.audio.WavAudioRecorder
import com.lessonsync.app.navigation.Screen
import com.lessonsync.app.viewmodel.LessonViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    navController: NavHostController,
    scoreId: String,
    lessonViewModel: LessonViewModel = viewModel()
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf("00:00") }
    var outputFile by remember { mutableStateOf<File?>(null) }
    var wavRecorder: WavAudioRecorder? by remember { mutableStateOf(null) }

    // Composable에서 코루틴을 실행하기 위해 CoroutineScope 생성
    val scope = rememberCoroutineScope()

    fun startRecording(context: Context, scoreId: String) {
        val file = createOutputFile(context, scoreId)
        outputFile = file
        wavRecorder = WavAudioRecorder(
            outputFile = file,
            sampleRate = 16000,
            channelConfig = android.media.AudioFormat.CHANNEL_IN_MONO,
            audioEncoding = android.media.AudioFormat.ENCODING_PCM_16BIT
        ).apply {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            startRecording()
            isRecording = true
        }
    }

    // stopRecording 로직을 코루틴 내에서 실행하도록 변경
    fun stopRecording() {
        if (!isRecording) return

        scope.launch {
            wavRecorder?.stopRecording()
            isRecording = false
            outputFile?.let { file ->
                // String 타입의 scoreId를 Int로 변환
                val scoreIdInt = scoreId.toIntOrNull()
                if (scoreIdInt != null) {
                    // [수정] scoreId와 file을 모두 올바른 순서로 전달
//                    lessonViewModel.uploadAndProcessRecording(scoreIdInt, file)

                    lessonViewModel.testUploadFromAssets(scoreIdInt, "short_test_lesson_file.wav")

                    navController.navigate(Screen.Processing.route + "/$scoreId") {
                        popUpTo(Screen.Recording.route + "/$scoreId") { inclusive = true }
                    }
                } else {
                    // scoreId가 숫자가 아닐 경우의 오류 처리 (예: Toast 메시지)
                }
            }
        }
    }

    // 권한 처리
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                startRecording(context, scoreId)
            } else {
                navController.popBackStack()
            }
        }
    )

    // 녹음 시간 업데이트
    LaunchedEffect(isRecording) {
        var seconds = 0
        while (isRecording) {
            kotlinx.coroutines.delay(1000)
            seconds++
            recordingTime = seconds.toTimeFormat()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("녹음 진행 중") },
                navigationIcon = {
                    IconButton(onClick = {
                        // 뒤로가기 시에도 녹음이 진행중이면 중지
                        if (isRecording) stopRecording()
                        else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = recordingTime,
                style = MaterialTheme.typography.displayLarge
            )

            IconButton(
                onClick = { stopRecording() },
                modifier = Modifier.size(80.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "녹음 정지",
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // 권한 체크 및 녹음 시작
    LaunchedEffect(Unit) {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startRecording(context, scoreId)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // 컴포저블 종료 시 리소스 정리
    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) {
                // onDispose는 suspend 함수를 직접 호출할 수 없으므로,
                isRecording = false // 단순히 플래그만 변경
            }
        }
    }
}

// 출력 파일 생성
private fun createOutputFile(context: Context, scoreId: String): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return File(
        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
        "REC_${scoreId}_$timeStamp.wav"
    ).apply { parentFile?.mkdirs() }
}

// 시간 포맷 변환
private fun Int.toTimeFormat(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%02d:%02d".format(minutes, seconds)
}
