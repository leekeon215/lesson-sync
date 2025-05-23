package com.lessonsync.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement // Arrangement 임포트 추가
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf // selectedBottomNavItem 상태 관리를 위해 추가
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController // NavController 타입으로 변경
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.navigation.Screen
import com.lessonsync.app.ui.theme.LessonSyncTheme

// MxlFile 데이터 클래스: 악보 파일 정보를 담음 (id와 이름)
data class MxlFile(val id: String, val name: String)

// "삭제화면" 이미지에 있는 한국어 파일 목록으로 업데이트된 더미 데이터
val demoFilesFromImage = listOf(
    MxlFile("1", "소나타 Op.13"),
    MxlFile("2", "왈츠 Op.64"),
    MxlFile("3", "Minuet_No.3"),
    MxlFile("4", "캐논 변주곡"),
    MxlFile("5", "Greensleeves"),
    MxlFile("6", "El Condor Pasa"),
    MxlFile("7", "Summer"),
    MxlFile("8", "인생의 회전목마"),
    MxlFile("9", "World's Smallest Violin"),
    MxlFile("10", "Jingle Bells")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) { // NavController 타입으로 변경
    // 하단 네비게이션 바에서 현재 선택된 아이템 인덱스를 저장하는 상태 변수
    var selectedBottomNavItem by remember { mutableIntStateOf(0) } // 초기값 0 (Explore)
    // 하단 네비게이션 아이템 목록 정의
    val bottomNavItems = listOf(
        BottomNavItem("Explore", Icons.Outlined.Folder, Screen.Home.route),
        BottomNavItem("Settings", Icons.Outlined.Settings, Screen.Settings.route)
    )

    // 앱 바 및 하단 바의 배경색
    val appBarColor = Color(0xFFF5EAFE)

    // 삭제 모드 활성화 여부
    var isSelectionModeActive by remember { mutableStateOf(false) }
    // 선택된 악보들의 ID 리스트
    val selectedScoreIds = remember { mutableStateListOf<String>() }
    // 현재 화면에 표시할 악보 목록
    val currentFiles = remember { mutableStateListOf(*demoFilesFromImage.toTypedArray()) }

    Scaffold(
        topBar = {
            if (isSelectionModeActive) {
                // 삭제 모드 TopAppBar
                TopAppBar(
                    title = {
                        Text(
                            "LessonSync",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionModeActive = false
                            selectedScoreIds.clear()
                        }) {
                            Icon(Icons.Filled.Close, "선택 모드 종료")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            currentFiles.removeAll { file -> selectedScoreIds.contains(file.id) }
                            isSelectionModeActive = false
                            selectedScoreIds.clear()
                        }) {
                            Icon(Icons.Filled.Delete, "선택된 항목 삭제")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = appBarColor,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    windowInsets = WindowInsets(0.dp)
                )
            } else {
                // 일반 모드 TopAppBar
                TopAppBar(
                    title = {
                        Text(
                            "LessonSync",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                            Icon(Icons.Filled.Search, "검색")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.AddScore.route) }) {
                            Icon(Icons.Filled.Add, "악보 추가")
                        }
                        IconButton(onClick = { isSelectionModeActive = true }) {
                            Icon(Icons.Filled.MoreVert, "더보기 (항목 선택)")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = appBarColor,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    windowInsets = WindowInsets(0.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = appBarColor,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedBottomNavItem == index,
                        onClick = {
                            selectedBottomNavItem = index
                            if (item.route.isNotEmpty()) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(currentFiles, key = { it.id }) { file ->
                val isSelected = selectedScoreIds.contains(file.id)
                ScoreListItem(
                    file = file,
                    isSelected = isSelected,
                    isSelectionModeActive = isSelectionModeActive,
                    onItemClick = {
                        if (isSelectionModeActive) {
                            // 삭제 모드: 선택 토글
                            if (isSelected) {
                                selectedScoreIds.remove(file.id)
                            } else {
                                selectedScoreIds.add(file.id)
                            }
                        } else {
                            // 일반 모드: ScoreViewerScreen으로 이동 (악보 ID 전달)
                            navController.navigate(Screen.ScoreViewer.route + "/${file.id}")
                        }
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun ScoreListItem(
    file: MxlFile,
    isSelected: Boolean,
    isSelectionModeActive: Boolean,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier // Modifier 파라미터 추가
) {
    Row(
        modifier = modifier // 전달받은 modifier 사용
            .fillMaxWidth()
            .clickable(onClick = onItemClick) // 클릭 리스너 적용
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // 추가: 텍스트와 아이콘을 양 끝으로
    ) {
        // 삭제 모드일 때 체크박스 표시
        if (isSelectionModeActive) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onItemClick() }, // 체크박스 클릭도 onItemClick으로 처리
                modifier = Modifier.padding(end = 16.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        // 악보 이름
        Text(
            text = file.name,
            style = MaterialTheme.typography.bodyLarge,
            // Modifier.weight(1f)는 삭제 모드 체크박스 유무에 따라 레이아웃이 달라질 수 있으므로,
            // Arrangement.SpaceBetween과 함께 사용 시 주의.
            // 여기서는 SpaceBetween을 사용하므로 weight 제거 또는 조건부 적용 고려.
            // 만약 체크박스가 없을 때 텍스트가 공간을 다 차지하게 하려면 아래와 같이 조건부로 weight 적용
            modifier = if (isSelectionModeActive) Modifier else Modifier.weight(1f)
        )
        // 일반 모드일 때만 오른쪽 화살표 아이콘 표시
        if (!isSelectionModeActive) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "악보 보기: ${file.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 하단 네비게이션 아이템 데이터 클래스
data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector, // ImageVector 타입 명시
    val route: String
)

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun HomeScreenPreview() {
    LessonSyncTheme {
        HomeScreen(navController = rememberNavController())
    }
}