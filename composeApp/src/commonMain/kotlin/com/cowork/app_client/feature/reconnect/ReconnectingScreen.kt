package com.cowork.app_client.feature.reconnect

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coworkappclient.composeapp.generated.resources.Res
import coworkappclient.composeapp.generated.resources.logo_cowork
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

// facts = ["...", "...", ...] 형태의 TOML 배열을 파싱
// - 입력 크기 상한으로 이상 파일 차단
// - 수량자 바운딩({0,500})으로 백트래킹 범위 제한
private fun parseTomlStringList(content: String): List<String> {
    if (content.length > MAX_TOML_BYTES) return emptyList()
    return Regex(""""([^"\\]{0,500}(?:\\.[^"\\]{0,500})*)"""")
        .findAll(content)
        .map { it.groupValues[1] }
        .filter { it.isNotBlank() }
        .take(MAX_FACTS)
        .toList()
}

private val cursedFacts = listOf(
    "솔직히 이메일로 다 됩니다.",
    "서버 담당자는 아마 지금 자고 있을 겁니다.",
    "지금 이 순간에도 마감은 1초씩 다가오고 있어요.",
    "재밌는 사실: 이 화면을 보는 건 당신 잘못이 아닙니다. 아마도.",
    "서버가 응답하지 않는 건 당신을 싫어해서가 아닙니다. 그냥 죽은 겁니다.",
    "cowork 없이도 구글 드라이브는 잘 됩니다. 생각해보세요.",
    "개발팀 누군가가 오늘 커밋을 push 했을 가능성이 있습니다.",
    "종윤이를 변기에 넣고 내려",
    "안녕 홍진아 나는 기니피그야",
    "안녕 재희야 나는 기니피그야",
    "열정있는 프론트엔드 개발자 나재희입니다. 제 꿈은 언젠가 서버도 고치는 거예요.",
    "열정 있는 프론트엔드 개발자를 모집 중입니다.",
    "8기 | 김태은 <---- 서버 및 웹 클라이언트/4월 26일임",
    "8기 | 이시우 <---- 치어리더/2월 30일임",
)

private const val MAX_TOML_BYTES = 16_000
private const val MAX_FACTS = 100
private const val CURSED_ODDS = 150

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ReconnectingScreen(retryIn: Long) {
    var facts by remember { mutableStateOf(emptyList<String>()) }
    var factIndex by remember { mutableIntStateOf(0) }
    var factTick by remember { mutableIntStateOf(0) }
    var displayFact by remember { mutableStateOf("") }
    var dotCount by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        try {
            val content = Res.readBytes("files/cowork_facts.toml").decodeToString()
            facts = parseTomlStringList(content)
        } catch (_: Exception) {
            // 파싱 실패 시 fallback
        }
        displayFact = facts.firstOrNull() ?: "cowork는 광주소프트웨어마이스터고등학교 학생들이 만들고 있어요."
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(6_000L)
            if (facts.isNotEmpty()) {
                val next = if (Random.nextInt(CURSED_ODDS) == 0) {
                    cursedFacts.random()
                } else {
                    factIndex = (factIndex + 1) % facts.size
                    facts[factIndex]
                }
                displayFact = next
                factTick++
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(550L)
            dotCount = if (dotCount >= 3) 1 else dotCount + 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141517)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .widthIn(max = 380.dp)
                .padding(horizontal = 32.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.logo_cowork),
                contentDescription = "CoWork",
                modifier = Modifier.size(52.dp),
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "cowork",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFFE3E5E8),
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "서버에 연결할 수 없습니다",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFED4245),
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = if (retryIn > 0) "${retryIn}초 후 재시도" else "재연결 시도 중" + ".".repeat(dotCount),
                fontSize = 12.sp,
                color = Color(0xFF4E5058),
            )

            Spacer(Modifier.height(36.dp))

            HorizontalDivider(color = Color(0xFF2B2D31))

            Spacer(Modifier.height(20.dp))

            if (displayFact.isNotEmpty()) {
                AnimatedContent(
                    targetState = factTick,
                    transitionSpec = {
                        (fadeIn(tween(500)) + slideInVertically(tween(400)) { it / 4 }) togetherWith
                        (fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 4 })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 36.dp),
                    contentAlignment = Alignment.Center,
                    label = "factSlide",
                ) {
                    Text(
                        text = displayFact,
                        fontSize = 12.sp,
                        color = Color(0xFF6D7176),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Spacer(Modifier.height(36.dp))
            }
        }
    }
}
