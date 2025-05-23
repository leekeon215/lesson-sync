package com.lessonsync.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.navigation.Screen
import com.lessonsync.app.ui.theme.LessonSyncTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    currentDarkTheme: Boolean, // MainActivity로부터 전달받은 현재 다크 모드 상태
    onToggleDarkTheme: (Boolean) -> Unit // MainActivity로부터 전달받은 다크 모드 토글 함수
) {
    val appBarColor = Color(0xFFF5EAFE)
    var selectedBottomNavItem by remember { mutableIntStateOf(1) }
    val bottomNavItems = listOf(
        BottomNavItem("Explore", Icons.Outlined.Folder, Screen.Home.route),
        BottomNavItem("Settings", Icons.Outlined.Settings, Screen.Settings.route)
    )

    var lessonSummaryNotificationEnabled by remember { mutableStateOf(true) }
    // 'darkModeEnabled' 로컬 상태 변수 대신 전달받은 'currentDarkTheme' 사용

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(Screen.Settings.title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = appBarColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
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
                            if (item.route.isNotEmpty() && item.route != Screen.Settings.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingToggleRow(
                label = "레슨 요약 완료 시 알림",
                isChecked = lessonSummaryNotificationEnabled,
                onCheckedChange = { lessonSummaryNotificationEnabled = it }
            )
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // "다크 모드" 설정 항목
            SettingToggleRow(
                label = "다크 모드",
                isChecked = currentDarkTheme, // 전달받은 다크 모드 상태를 스위치에 반영
                onCheckedChange = { newCheckedState ->
                    onToggleDarkTheme(newCheckedState) // 스위치 토글 시 상태 변경 함수 호출
                }
            )
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun SettingScreenPreview() {
    var isPreviewDark by remember { mutableStateOf(false) }

    LessonSyncTheme(useDarkTheme = isPreviewDark) { // isPreviewDark 상태를 테마에 적용
        SettingScreen(
            navController = rememberNavController(),
            currentDarkTheme = isPreviewDark, // isPreviewDark 상태를 SettingScreen에 전달
            onToggleDarkTheme = { newCheckedState -> // 스위치 토글 시 isPreviewDark 상태 변경
                isPreviewDark = newCheckedState
            }
        )
    }
}
