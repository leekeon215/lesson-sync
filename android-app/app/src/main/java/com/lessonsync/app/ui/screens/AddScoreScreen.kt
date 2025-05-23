package com.lessonsync.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.navigation.Screen
import com.lessonsync.app.ui.theme.LessonSyncTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScoreScreen(navController: NavHostController) {
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) } // 업로드/처리 중 상태

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 파일 선택기를 실행하는 ActivityResultLauncher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedFileUri = uri
            if (uri != null) {
                // URI로부터 파일 이름 가져오기 (실제 구현에서는 더 견고한 방법 사용 필요)
                // 예를 들어 ContentResolver를 사용하여 display name을 가져올 수 있습니다.
                selectedFileName = uri.lastPathSegment?.substringAfterLast('/') ?: "선택된 파일"
            } else {
                selectedFileName = null
            }
        }
    )

    fun handleFileUpload() {
        if (selectedFileUri != null) {
            isLoading = true
            // TODO: 실제 파일 업로드 또는 앱 내부 저장 로직 구현
            //      - selectedFileUri를 사용하여 파일 내용을 읽고 처리합니다.
            //      - 성공 시: isLoading = false, navController.popBackStack() 또는 HomeScreen으로 이동 후 목록 새로고침
            //      - 실패 시: isLoading = false, Snackbar 등으로 오류 메시지 표시
            scope.launch {
                // 예시: 3초 후 성공 처리 (실제로는 비동기 작업)
                kotlinx.coroutines.delay(3000)
                isLoading = false
                snackbarHostState.showSnackbar("악보가 성공적으로 추가되었습니다.")
                navController.popBackStack() // 이전 화면(HomeScreen)으로 돌아감
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("먼저 악보 파일을 선택해주세요.")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(Screen.AddScore.title) }, // "악보 추가"
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                },
                actions = {
                    // 파일이 선택되었고 로딩 중이 아닐 때만 저장 버튼 표시
                    if (selectedFileUri != null && !isLoading) {
                        IconButton(onClick = { handleFileUpload() }) {
                            Icon(Icons.Filled.Check, "악보 저장")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
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
                .padding(all = 24.dp), // 전체적인 내부 패딩
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // 내용을 중앙에 배치
        ) {
            if (isLoading) {
                // 로딩 중 UI
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "악보를 추가하는 중입니다...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                // 파일 선택 UI
                Icon(
                    imageVector = Icons.Filled.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp), // 아이콘 크기 증가
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "MusicXML 악보 파일 선택",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "기기에서 MusicXML(.musicxml, .xml) 파일을 선택하여 업로드하세요. 선택된 악보는 목록에 추가됩니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // 약간 연한 색상
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 파일 선택 버튼
                OutlinedButton(
                    onClick = {
                        // "application/vnd.recordare.musicxml+xml" 또는 "application/xml", "text/xml"
                        // 더 구체적인 MIME 타입이 있다면 그것을 사용하거나, "*/*"로 모든 파일 허용 후 확장자 검사
                        filePickerLauncher.launch("application/vnd.recordare.musicxml+xml")
                        // 또는 filePickerLauncher.launch("application/xml")
                        // 또는 filePickerLauncher.launch("*/*") 후 확장자 필터링
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (selectedFileName == null) "파일 선택하기" else "다른 파일 선택",
                        fontSize = 16.sp
                    )
                }

                // 선택된 파일 정보 표시
                selectedFileName?.let { fileName ->
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "선택된 파일:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 업로드 버튼 (파일이 선택되었을 때만 표시)
                if (selectedFileUri != null) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { handleFileUpload() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("이 악보 추가하기", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun AddScoreScreenPreview() {
    LessonSyncTheme {
        AddScoreScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddScoreScreenDarkPreview() {
    LessonSyncTheme(useDarkTheme = true) {
        AddScoreScreen(navController = rememberNavController())
    }
}