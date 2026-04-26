package com.cowork.app_client.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class ConnectivityMonitor(
    private val httpClient: HttpClient,
    private val healthUrl: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _retryIn = MutableStateFlow(0L)
    val retryIn: StateFlow<Long> = _retryIn.asStateFlow()

    init {
        scope.launch { poll() }
    }

    private suspend fun poll() {
        var failureCount = 0
        while (true) {
            val connected = checkHealth()
            _isConnected.value = connected
            if (connected) {
                _retryIn.value = 0
                failureCount = 0
                delay(CONNECTED_POLL_MS)
            } else {
                val backoffMs = backoffForAttempt(failureCount)
                failureCount++
                val recoveredEarly = waitWithEarlyRecovery(backoffMs)
                if (recoveredEarly) failureCount = 0
            }
        }
    }

    // 카운트다운(1초 tick)과 헬스체크(BACKOFF_POLL_MS 간격)를 별도 코루틴으로 분리
    // → 헬스체크 지연 시간이 카운트다운 정확도에 영향을 주지 않음
    private suspend fun waitWithEarlyRecovery(backoffMs: Long): Boolean {
        val deadline = TimeSource.Monotonic.markNow() + backoffMs.milliseconds
        val recovered = MutableStateFlow(false)

        val healthJob = scope.launch {
            delay(BACKOFF_POLL_MS)
            while (isActive && !recovered.value) {
                if (checkHealth()) {
                    _isConnected.value = true
                    _retryIn.value = 0
                    recovered.value = true
                    return@launch
                }
                delay(BACKOFF_POLL_MS)
            }
        }

        try {
            while (!recovered.value) {
                val remainingMs = (-deadline.elapsedNow()).inWholeMilliseconds.coerceAtLeast(0L)
                if (remainingMs <= 0L) break
                _retryIn.value = ((remainingMs + 999L) / 1_000L).coerceAtLeast(1L)
                delay(1_000L)
            }
        } finally {
            healthJob.cancel()
            _retryIn.value = 0
        }

        return recovered.value
    }

    // 5->5->5->10->15->30->30->45->60->60->80->80->random(80..120,5회)->120 forever
    private fun backoffForAttempt(attempt: Int): Long = when {
        attempt < 3  -> 5_000L
        attempt == 3 -> 10_000L
        attempt == 4 -> 15_000L
        attempt < 7  -> 30_000L
        attempt == 7 -> 45_000L
        attempt < 10 -> 60_000L
        attempt < 12 -> 80_000L
        attempt < 17 -> Random.nextLong(80_000L, 120_001L)
        else         -> 120_000L
    }

    // 5xx는 서버 오류이므로 연결 불가로 판단, 4xx는 게이트웨이가 살아있으므로 연결 가능
    // CancellationException은 협력적 취소를 위해 반드시 재던짐
    private suspend fun checkHealth(): Boolean = try {
        val response = httpClient.get(healthUrl)
        response.status.value < 500
    } catch (_: ClientRequestException) {
        true
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        false
    }

    companion object {
        private const val CONNECTED_POLL_MS = 30_000L
        private const val BACKOFF_POLL_MS = 5_000L
    }
}
