package com.lessonsync.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 다크 모드에서 사용할 밝은 텍스트/아이콘 색상 정의
val DarkThemeOnSurface = Color.White // 기본 텍스트 및 아이콘
val DarkThemeOnSurfaceVariant = Color(0xFFCAC4D0) // 보조 텍스트, 비활성 요소 (밝은 회색)
val DarkThemePrimaryText = Color.White // 주요 컴포넌트 위 텍스트

@Composable
fun LessonSyncTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) {
        darkColorScheme(
            primary = Color(0xFFD0BCFF), // Purple (기존 유지 또는 약간 더 밝게 조정 가능)
            onPrimary = Color(0xFF381E72), // primary 색상 위의 텍스트/아이콘 (어두운 보라색, 대비 충분)
            primaryContainer = Color(0xFF4A4458), // primary 관련 컨테이너 (어두운 편)
            onPrimaryContainer = Color(0xFFEADDFF), // primaryContainer 위의 텍스트/아이콘 (밝은 보라)

            secondary = Color(0xFFCCC2DC), // Light Purple (기존 유지)
            onSecondary = Color(0xFF332D41), // secondary 색상 위의 텍스트/아이콘 (어두운 회색-보라, 대비 충분)
            secondaryContainer = Color(0xFF4A4458), // secondary 관련 컨테이너 (어두운 편, 예: 네비바 선택 인디케이터)
            // onSecondaryContainer를 밝게 변경하여 secondaryContainer 위의 텍스트/아이콘 가독성 확보
            onSecondaryContainer = Color(0xFFE8DEF8), // 예: 네비바 선택된 아이템의 텍스트/아이콘 (밝은 연보라)

            tertiary = Color(0xFFEFB8C8), // Pink (기존 유지)
            onTertiary = Color(0xFF492532), // tertiary 색상 위의 텍스트/아이콘 (어두운 분홍, 대비 충분)
            tertiaryContainer = Color(0xFF633B48), // tertiary 관련 컨테이너
            onTertiaryContainer = Color(0xFFFFD8E4), // tertiaryContainer 위의 텍스트/아이콘

            background = Color(0xFF1C1B1F), // 매우 어두운 배경 (기존 유지)
            // onBackground를 훨씬 밝게 변경하여 배경 위 텍스트 가독성 확보
            onBackground = DarkThemeOnSurface, // 흰색으로 변경

            surface = Color(0xFF1C1B1F), // 카드, 시트 등 표면 색상 (배경과 동일하게 또는 약간 다르게)
            // onSurface를 훨씬 밝게 변경하여 표면 위 텍스트 가독성 확보
            onSurface = DarkThemeOnSurface, // 흰색으로 변경

            surfaceVariant = Color(0xFF49454F), // surface보다 약간 다른 톤의 표면 (예: 비활성 요소 배경)
            // onSurfaceVariant를 밝게 변경 (예: 비활성 요소 위 텍스트, 구분선 등)
            onSurfaceVariant = DarkThemeOnSurfaceVariant, // 밝은 회색으로 변경

            outline = Color(0xFF938F99), // 구분선, 테두리 등 (중간 밝기 회색)
            outlineVariant = Color(0xFF49454F), // 더 연한 구분선, 테두리

            error = Color(0xFFF2B8B5),
            onError = Color(0xFF601410),
            errorContainer = Color(0xFF8C1D18),
            onErrorContainer = Color(0xFFF9DEDC),

            // 추가적으로 필요한 색상들 (예: inverseSurface 등)
            inverseSurface = Color(0xFFE6E1E5), // 다크모드에서 라이트모드 표면 색상처럼 밝은 색
            inverseOnSurface = Color(0xFF313033), // inverseSurface 위의 텍스트
            inversePrimary = Color(0xFF6750A4), // 다크모드에서 라이트모드의 primary 처럼

            // scrim: Color = Color.Black, // Usually for modal backgrounds, etc.
            // surfaceTint: Color = primary // Usually the same as primary
        )
    }else {
        lightColorScheme(
            primary = Color(0xFF6750A4),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260),
            background = Color.White,
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = Color(0xFF1C1B1F), // 어두운 텍스트 (대비 좋음)
            onSurface = Color(0xFF1C1B1F),    // 어두운 텍스트 (대비 좋음)
            secondaryContainer = Color(0xFFE8DEF8), // 연보라 인디케이터
            onSecondaryContainer = Color(0xFF1D192B), // 인디케이터 위 어두운 텍스트 (대비 좋음)
            onSurfaceVariant = Color(0xFF49454F) // 라이트모드에서는 회색 계열의 보조 텍스트

        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography, // Typography가 정의되어 있다고 가정
        content = content
    )
}
