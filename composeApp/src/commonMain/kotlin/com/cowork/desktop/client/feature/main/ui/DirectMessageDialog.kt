package com.cowork.desktop.client.feature.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cowork.desktop.client.data.repository.ChatRepository
import com.cowork.desktop.client.data.repository.UserRepository
import com.cowork.desktop.client.domain.model.ChatMessage
import com.cowork.desktop.client.domain.model.MessageType
import com.cowork.desktop.client.domain.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
internal fun DirectMessageDialog(
    channelId: Long,
    onDismiss: () -> Unit,
    chatRepository: ChatRepository = koinInject(),
    userRepository: UserRepository = koinInject(),
) {
    val scope = rememberCoroutineScope()
    var messages by remember(channelId) { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var otherUser by remember(channelId) { mutableStateOf<UserProfile?>(null) }
    var currentUserId by remember(channelId) { mutableStateOf<Long?>(null) }
    var draft by remember(channelId) { mutableStateOf("") }
    var isLoading by remember(channelId) { mutableStateOf(true) }
    var isSending by remember(channelId) { mutableStateOf(false) }
    var isLoadingMore by remember(channelId) { mutableStateOf(false) }
    var hasMore by remember(channelId) { mutableStateOf(false) }
    var error by remember(channelId) { mutableStateOf<String?>(null) }

    LaunchedEffect(channelId) {
        isLoading = true
        error = null
        runCatching {
            messages = chatRepository.getMessages(channelId, limit = 100)
            hasMore = messages.size == 100
            if (currentUserId == null) currentUserId = userRepository.getMyProfile()?.id
            val conversation = chatRepository.getDms().firstOrNull { it.channelId == channelId }
            otherUser = conversation?.otherUserId?.let { userRepository.getUserProfile(it) }
            messages.firstOrNull()?.id?.let { chatRepository.markChannelRead(channelId, it) }
        }.onFailure { error = it.userMessage() }
        isLoading = false
    }

    LaunchedEffect(channelId) {
        while (true) {
            delay(3_000)
            val loaded = runCatching { chatRepository.getMessages(channelId, limit = 100) }.getOrNull() ?: continue
            val pending = messages.filter { it.id.startsWith("optimistic-dm-") }.filterNot { optimistic ->
                loaded.any {
                    it.clientMessageId == optimistic.clientMessageId ||
                        (it.authorId == optimistic.authorId && it.content == optimistic.content)
                }
            }
            val persistedHistory = messages.filterNot { it.id.startsWith("optimistic-dm-") }
            messages = (pending + loaded + persistedHistory).distinctBy(ChatMessage::id)
            loaded.firstOrNull()?.id?.let { runCatching { chatRepository.markChannelRead(channelId, it) } }
        }
    }

    ManagementDialogShell(
        title = otherUser?.let { it.nickname ?: it.name } ?: "다이렉트 메시지",
        onDismiss = onDismiss,
        sidebar = {
            Text("DM", Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                scope.launch {
                    runCatching { chatRepository.hideDm(channelId) }
                        .onSuccess { onDismiss() }
                        .onFailure { error = it.userMessage() }
                }
            }) { Text("목록에서 숨기기") }
        },
    ) {
        error?.let { ManagementError(it) }
        when {
            isLoading -> CircularProgressIndicator()
            messages.isEmpty() -> Text("첫 메시지를 보내 대화를 시작하세요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> LazyColumn(Modifier.fillMaxWidth().heightIn(max = 390.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                if (hasMore) {
                    item(key = "load-older") {
                        OutlinedButton(
                            onClick = {
                                val before = messages.lastOrNull()?.id ?: return@OutlinedButton
                                scope.launch {
                                    isLoadingMore = true
                                    runCatching { chatRepository.getMessages(channelId, before = before, limit = 100) }
                                        .onSuccess { older ->
                                            messages = (messages + older).distinctBy(ChatMessage::id)
                                            hasMore = older.size == 100
                                        }
                                        .onFailure { error = it.userMessage() }
                                    isLoadingMore = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoadingMore,
                        ) { Text(if (isLoadingMore) "불러오는 중…" else "이전 메시지 더 보기") }
                    }
                }
                items(messages.reversed(), key = { it.id }) { message ->
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f), shape = RoundedCornerShape(10.dp)) {
                        Column(Modifier.fillMaxWidth().padding(10.dp)) {
                            Text("사용자 ${message.authorId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(message.content)
                            message.createdAt?.let { Text(it.take(16), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("메시지 입력…") },
            trailingIcon = {
                TextButton(
                    onClick = {
                        val content = draft.trim()
                        if (content.isEmpty()) return@TextButton
                        scope.launch {
                            isSending = true
                            error = null
                            val optimisticId = "optimistic-dm-${System.currentTimeMillis()}"
                            val optimistic = ChatMessage(
                                id = optimisticId,
                                teamId = null,
                                projectId = null,
                                channelId = channelId,
                                authorId = currentUserId ?: -1,
                                content = content,
                                parentMessageId = null,
                                type = MessageType.Text,
                                fileUrl = null,
                                fileName = null,
                                fileSize = null,
                                createdAt = null,
                                clientMessageId = optimisticId,
                            )
                            val result = runCatching {
                                chatRepository.sendMessage(
                                    channelId = channelId,
                                    teamId = null,
                                    content = content,
                                    clientMessageId = optimisticId,
                                )
                            }
                            if (result.isFailure) {
                                error = result.exceptionOrNull()?.userMessage()
                                isSending = false
                                return@launch
                            }
                            draft = ""
                            messages = listOf(optimistic) + messages
                            isSending = false

                            // 서버는 Kafka 큐 등록 직후 응답하므로 저장 완료까지 짧게 재조회한다.
                            for (waitMillis in listOf(500L, 1_000L, 2_000L)) {
                                delay(waitMillis)
                                val loaded = runCatching { chatRepository.getMessages(channelId, limit = 100) }.getOrNull() ?: continue
                                val persisted = loaded.any {
                                    it.clientMessageId == optimisticId ||
                                        (it.authorId == optimistic.authorId && it.content == content)
                                }
                                val existingWithoutOptimistic = messages.filterNot { it.id == optimisticId }
                                messages = if (persisted) {
                                    (loaded + existingWithoutOptimistic).distinctBy(ChatMessage::id)
                                } else {
                                    (listOf(optimistic) + loaded + existingWithoutOptimistic).distinctBy(ChatMessage::id)
                                }
                                if (persisted) break
                            }
                        }
                    },
                    enabled = draft.isNotBlank() && !isSending,
                ) { Text("전송") }
            },
        )
    }
}
