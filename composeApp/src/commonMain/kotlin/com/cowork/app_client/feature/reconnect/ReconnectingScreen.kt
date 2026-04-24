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

// facts = ["...", "...", ...] 형태의 TOML 배열을 파싱
private fun parseTomlStringList(content: String): List<String> =
    Regex(""""((?:[^"\\]|\\.)*)"""").findAll(content)
        .map { it.groupValues[1] }
        .toList()

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ReconnectingScreen(retryIn: Long) {
    var facts by remember { mutableStateOf(emptyList<String>()) }
    var factIndex by remember { mutableIntStateOf(0) }
    var dotCount by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        try {
            val content = Res.readBytes("files/cowork_facts.toml").decodeToString()
            facts = parseTomlStringList(content)
        } catch (_: Exception) {
            // 파싱 실패 시 빈 리스트 유지
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(6_000L)
            if (facts.isNotEmpty()) {
                factIndex = (factIndex + 1) % facts.size
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

            if (facts.isNotEmpty()) {
                AnimatedContent(
                    targetState = factIndex,
                    transitionSpec = {
                        (fadeIn(tween(500)) + slideInVertically(tween(400)) { it / 4 }) togetherWith
                        (fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 4 })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 36.dp),
                    contentAlignment = Alignment.Center,
                    label = "factSlide",
                ) { idx ->
                    Text(
                        text = facts[idx],
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
