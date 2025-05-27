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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.lessonsync.app.audio.WavAudioRecorder
import com.lessonsync.app.navigation.Screen
import com.lessonsync.app.retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(navController: NavHostController, scoreId: String) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf("00:00") }
    var outputFile by remember { mutableStateOf<File?>(null) }
    var wavRecorder by remember { mutableStateOf<WavAudioRecorder?>(null) }

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

    // 파일 업로드 처리
    fun uploadRecording(file: File, scoreId: String) {
        val requestFile = file.asRequestBody("audio/wav".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData(
            "file",  // 서버에서 기대하는 필드 이름
            file.name,
            requestFile
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.audioService.processLessonCoroutine(body)
                if (response.isSuccessful) {
                    Log.d("UPLOAD_SUCCESS", "Server response: ${response.body()?.string()}")
                    withContext(Dispatchers.Main) {
                        navController.navigate(Screen.Processing.route)
                    }
                } else {
                    Log.e("UPLOAD_ERROR", "Server error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("UPLOAD_EXCEPTION", "Network error: ${e.localizedMessage}")
            }
        }
    }

    fun stopRecording() {
        wavRecorder?.stopRecording()
        isRecording = false
        outputFile?.let { uploadRecording(it, scoreId) }
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
                        if (isRecording) stopRecording()
                        navController.popBackStack()
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
            verticalArrangement = Arrangement.SpaceBetween,
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
                stopRecording()
            }
            wavRecorder = null
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
