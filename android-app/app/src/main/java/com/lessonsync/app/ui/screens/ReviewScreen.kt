package com.lessonsync.app.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lessonsync.app.ui.theme.LessonSyncTheme
import org.json.JSONObject

// 데이터 클래스 예시
data class LessonSummaryItem(val title: String, val content: String)
data class PracticeTip(val category: String, val points: List<String>)

val sampleSummary = listOf(
    LessonSummaryItem("레슨 일시", "2025년 5월 8일 (수)\n1시간 30분"),
    LessonSummaryItem("총평 및 피드백 요약",
        "• 활의 각도와 속도 조절이 불균형한 구간이 있음\n  → 보우 컨트롤 강화 필요\n• 왼손 포지션 이동 시 음정이 흔들림\n  → 느린 템포 반복 연습\n• 전체적인 표현력은 향상되고 있으며, 아티큘레이션에 더 집중할 필요 있음"),
)
val samplePracticeTips = listOf(
    PracticeTip("연주 기술 점검", listOf(
        "활 운용: 활의 중심만 사용하려는 경향이 있음 → 끝까지 활용해 균형 잡힌 소리 내기",
        "비브라토: 속도가 빠르고 일정하지 않음 → 메트로놈과 함께 일정한 진동 연습"
    )),
    PracticeTip("마디별 주의사항", listOf(
        "1-4마디: 테마 도입부 - 활이 짧게 움직이며 음이 작음 → 긴 활로 풍부하게 연주",
        "9-12마디: 리듬 불균형 - 점음표와 16분음표 구분 안 됨 → 리듬 정확도 향상 필요",
        "13-16마디: 비브라토 너무 빠르고 좁음 → 느리고 넓은 진동으로 연습",
        "25-28마디: 슬러 음정 불안정 → 왼손 압력 일정하게 유지하며 연습",
        "29-32마디: 프레이징 마무리 흐림 → 호흡과 다이내믹 정리 필요"
    ))
)


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ReviewScreen(navController: NavHostController, scoreId: String) {
//    // TODO: scoreId를 사용하여 실제 레슨 요약 데이터 로드
//    val lessonTitle = "바이올린 레슨 요약" // 예시 제목
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(lessonTitle) },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
//                    titleContentColor = MaterialTheme.colorScheme.onSurface,
//                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
//                )
//            )
//        },
//        containerColor = MaterialTheme.colorScheme.background
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .padding(horizontal = 16.dp, vertical = 8.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            item {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        imageVector = Icons.Filled.MusicNote, // 예시 아이콘
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(36.dp)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = lessonTitle, // "🎻 바이올린 레슨 요약" 와 같은 형태로도 가능
//                        style = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//
//            items(sampleSummary) { item ->
//                SummarySection(title = item.title, content = item.content)
//            }
//
//            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
//
//            items(samplePracticeTips) { tip ->
//                PracticeTipSection(category = tip.category, points = tip.points)
//            }
//
//            item { Spacer(modifier = Modifier.height(16.dp)) } // 하단 여백
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(navController: NavHostController, scoreId: String) {
    val context = LocalContext.current
    var summaryText by remember { mutableStateOf("요약을 불러오는 중...") }
    var speechSegments by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(scoreId) {
        val prefs = context.getSharedPreferences("LessonSyncPrefs", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("summaryJson_$scoreId", null)

        if (jsonString != null) {
            try {
                val json = JSONObject(jsonString)
                summaryText = json.optString("summary", "요약 없음")
                val segmentsArray = json.optJSONArray("speech_segments")
                if (segmentsArray != null) {
                    val segments = mutableListOf<String>()
                    for (i in 0 until segmentsArray.length()) {
                        segments.add(segmentsArray.getString(i))
                    }
                    speechSegments = segments
                }
            } catch (e: Exception) {
                summaryText = "요약 데이터를 해석하는 데 실패했습니다."
            }
        } else {
            summaryText = "서버로부터 받은 요약 정보가 없습니다."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("레슨 요약 보기") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("📝 레슨 요약", style = MaterialTheme.typography.titleLarge)
                Text(summaryText)
            }

            if (speechSegments.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🎙️ 발화 구간", style = MaterialTheme.typography.titleLarge)
                }

                items(speechSegments) { segment ->
                    Text("• $segment", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}


@Composable
fun SummarySection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 22.sp // 줄 간격 조절
        )
    }
}

@Composable
fun PracticeTipSection(category: String, points: List<String>) {
    Column {
        Text(
            text = category,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        points.forEach { point ->
            Text(
                text = "• $point", // 불릿 포인트
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 4.dp),
                lineHeight = 22.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewScreenPreview() {
    LessonSyncTheme {
        ReviewScreen(navController = rememberNavController(), scoreId = "1")
    }
}