package com.lessonsync.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun LessonRecordScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Record Icon",
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = { /* TODO: 녹음 시작 로직 */ }) {
            Text("녹음 시작")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = {
            navController.navigate("viewer")
        }) {
            Text("악보 뷰어로")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLessonRecord() {
    LessonRecordScreen(navController = rememberNavController())
}
