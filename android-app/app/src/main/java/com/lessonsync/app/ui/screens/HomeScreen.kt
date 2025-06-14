package com.lessonsync.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.entity.ScoreEntity
import com.lessonsync.app.navigation.Screen
import com.lessonsync.app.ui.theme.LessonSyncTheme
import com.lessonsync.app.viewmodel.ScoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    scoreViewModel: ScoreViewModel = viewModel() // ViewModel 주입
) {
    // ViewModel의 LiveData를 관찰하여 DB의 악보 목록을 실시간으로 가져옴
    val currentFiles by scoreViewModel.allScores.observeAsState(initial = emptyList())

    // 하단 네비게이션 바 상태 관리
    var selectedBottomNavItem by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf(
        BottomNavItem("Explore", Icons.Outlined.Folder, Screen.Home.route),
        BottomNavItem("Settings", Icons.Outlined.Settings, Screen.Settings.route)
    )

    // 앱 바 및 하단 바의 배경색
    val appBarColor = MaterialTheme.colorScheme.surfaceContainerLowest

    // 삭제 모드 상태 관리
    var isSelectionModeActive by remember { mutableStateOf(false) }
    val selectedScoreIds = remember { mutableStateListOf<Int>() }

    Scaffold(
        topBar = {
            if (isSelectionModeActive) {
                // 삭제 모드 TopAppBar
                TopAppBar(
                    title = {
                        Text(
                            "${selectedScoreIds.size}개 선택됨", // 선택된 항목 수 표시
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
                            // ViewModel을 통해 DB에서 선택된 항목들 삭제
                            scoreViewModel.deleteByIds(selectedScoreIds.toList())
                            isSelectionModeActive = false
                            selectedScoreIds.clear()
                        }) {
                            Icon(Icons.Filled.Delete, "선택된 항목 삭제")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = appBarColor,
                    )
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
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = appBarColor
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedBottomNavItem == index,
                        onClick = {
                            selectedBottomNavItem = index
                            if (item.route.isNotEmpty()) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
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
                            if (isSelected) selectedScoreIds.remove(file.id)
                            else selectedScoreIds.add(file.id)
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
    file: ScoreEntity, // 데이터 타입을 ScoreEntity로 변경
    isSelected: Boolean,
    isSelectionModeActive: Boolean,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isSelectionModeActive) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onItemClick() },
                modifier = Modifier.padding(end = 16.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        Text(
            text = file.title, // ScoreEntity의 title 필드 사용
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (!isSelectionModeActive) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "악보 보기: ${file.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun HomeScreenPreview() {
    LessonSyncTheme {
        HomeScreen(navController = rememberNavController())
    }
}