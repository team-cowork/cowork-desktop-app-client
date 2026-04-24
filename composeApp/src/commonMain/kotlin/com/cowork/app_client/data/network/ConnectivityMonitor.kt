package com.cowork.app_client.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

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

    // 백오프 대기 중에도 BACKOFF_POLL_MS 간격으로 헬스체크 → 복구 시 즉시 재연결
    private suspend fun waitWithEarlyRecovery(backoffMs: Long): Boolean {
        val endTimeMs = System.currentTimeMillis() + backoffMs
        while (true) {
            val remainingMs = endTimeMs - System.currentTimeMillis()
            if (remainingMs <= 0) break
            _retryIn.value = (remainingMs / 1_000L).coerceAtLeast(1L)
            delay(minOf(BACKOFF_POLL_MS, remainingMs))
            if (checkHealth()) {
                _isConnected.value = true
                _retryIn.value = 0
                return true
            }
        }
        _retryIn.value = 0
        return false
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
    private suspend fun checkHealth(): Boolean = try {
        val response = httpClient.get(healthUrl)
        response.status.value < 500
    } catch (_: ClientRequestException) {
        true
    } catch (_: Exception) {
        false
    }

    companion object {
        private const val CONNECTED_POLL_MS = 30_000L
        private const val BACKOFF_POLL_MS = 5_000L
    }
}
