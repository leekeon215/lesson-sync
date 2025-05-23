// ---------- SummaryScreen.kt ----------
package com.lessonsync.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.lessonsync.app.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(nav: NavHostController, id: String) {
    Scaffold(topBar = { TopAppBar(title = { Text(Screen.Summary.title) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(16.dp).padding(padding)) {
            Text("[요약 내용 로드 예정]")
        }
    }
}