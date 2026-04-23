package com.cowork.app_client.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val CoworkShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // 칩, 배지, 툴팁
    small      = RoundedCornerShape(8.dp),   // 입력 필드, 작은 카드
    medium     = RoundedCornerShape(12.dp),  // 버튼, 메뉴 항목
    large      = RoundedCornerShape(16.dp),  // 다이얼로그, 패널 헤더
    extraLarge = RoundedCornerShape(20.dp),  // 바텀 시트, 팀 아이콘
)

// Material3 Shapes에 포함되지 않는 추가 반경
object CoworkRadius {
    val none   = 0.dp
    val xs     = 4.dp
    val sm     = 8.dp
    val md     = 12.dp
    val lg     = 16.dp
    val xl     = 20.dp
    val xxl    = 28.dp
    val full   = 9999.dp  // 완전 원형
}
