package com.lessonsync.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 뒤로가기 아이콘
import androidx.compose.material.icons.filled.Search // 검색 아이콘
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.navigation.Screen
import com.lessonsync.app.navigation.demoScores // 데모 악보 데이터
import com.lessonsync.app.ui.theme.LessonSyncTheme // 프리뷰를 위한 테마

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    // 검색어를 저장하고 UI 상태를 관리하기 위한 상태 변수
    var query by remember { mutableStateOf(TextFieldValue("")) }

    // 검색어(query.text)가 변경될 때마다 필터링된 악보 목록을 다시 계산
    // query.text를 키로 사용하여 query 객체 자체가 아닌 텍스트 내용 변경 시에만 재계산
    val filteredScores = remember(query.text) {
        if (query.text.isBlank()) { // 검색어가 비어있거나 공백만 있을 경우
            emptyList() // 빈 리스트 반환 (초기 상태 또는 검색어 없을 때)
        } else {
            demoScores.filter { score ->
                // 악보 제목(score.title)에 검색어(query.text)가 포함되어 있는지 대소문자 구분 없이 확인
                score.title.contains(query.text, ignoreCase = true)
            }
        }
    }

    // TextField에 포커스를 주기 위한 FocusRequester
    val focusRequester = remember { FocusRequester() }
    // 소프트 키보드 컨트롤러
    val keyboardController = LocalSoftwareKeyboardController.current
    // TopAppBar 배경색 (HomeScreen과 일관성 유지)
    val appBarColor = Color(0xFFF5EAFE)

    // SearchScreen이 처음 표시될 때 TextField에 포커스를 주고 키보드를 올림
    LaunchedEffect(Unit) {
        focusRequester.requestFocus() // 포커스 요청
        keyboardController?.show()    // 키보드 표시
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // 검색어 입력 필드
                    TextField(
                        value = query, // 현재 검색어
                        onValueChange = { query = it }, // 검색어 변경 시 상태 업데이트
                        placeholder = { Text("search title") }, // 안내 문구
                        modifier = Modifier
                            .fillMaxWidth() // 가로 폭 전체 채우기
                            .focusRequester(focusRequester), // 포커스 설정
                        singleLine = true, // 한 줄 입력
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Search // 키보드 '완료' 버튼을 검색 아이콘으로 변경
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide() // 검색 실행 시 키보드 숨기기
                                // 필요시 여기서 명시적인 검색 로직 호출 (현재는 실시간 필터링)
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            // TextField 배경을 투명하게 만들어 TopAppBar와 자연스럽게 통합
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            // 밑줄(indicator)도 투명하게 처리
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary, // 커서 색상
                            // placeholder 색상 (기본값이 적절하지만, 필요시 여기서 변경)
                            // focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            // unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        // 입력된 텍스트 스타일
                        textStyle = MaterialTheme.typography.titleMedium.copy( // TopAppBar 제목과 유사한 스타일
                            color = MaterialTheme.colorScheme.onSurface // 텍스트 색상
                        )
                    )
                },
                navigationIcon = {
                    // 뒤로 가기 버튼
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                actions = {
                    // 오른쪽 검색 아이콘 (장식 또는 추가 기능용)
                    IconButton(onClick = {
                        // 예: 검색어 초기화
                        // if (query.text.isNotEmpty()) query = TextFieldValue("")
                        // 또는 명시적 검색 실행
                        keyboardController?.hide()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "검색 실행"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appBarColor, // TopAppBar 배경색
                    titleContentColor = MaterialTheme.colorScheme.onSurface, // 제목(TextField) 내용 색상
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface, // 네비게이션 아이콘 색상
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface // 액션 아이콘 색상
                ),
                windowInsets = WindowInsets(0.dp) // 상태 표시줄 영역만큼의 패딩 제거 (필요시)
            )
        }
    ) { paddingValues ->
        // 화면 본문 내용
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
                // 검색어가 비어있을 경우 안내 메시지 표시
                query.text.isBlank() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "악보 제목을 입력하여 검색하세요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // 검색어는 있지만 결과가 없을 경우 메시지 표시
                filteredScores.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "\"${query.text}\"에 대한 검색 결과가 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // 검색 결과가 있을 경우 목록 표시
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredScores, key = { it.id }) { score ->
                            ListItem(
                                headlineContent = { Text(score.title, style = MaterialTheme.typography.bodyLarge) },
                                modifier = Modifier.clickable {
                                    keyboardController?.hide() // 다른 화면으로 이동 전 키보드 숨기기
                                    navController.popBackStack() // 현재 검색 화면을 백스택에서 제거
                                    navController.navigate(Screen.ScoreViewer.route + "/${score.id}") // 악보 뷰어 화면으로 이동
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent // ListItem 배경 투명 처리 (Scaffold 배경 사용)
                                )
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) // 항목 구분선
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun SearchScreenPreview() {
    LessonSyncTheme {
        SearchScreen(navController = rememberNavController())
    }
}