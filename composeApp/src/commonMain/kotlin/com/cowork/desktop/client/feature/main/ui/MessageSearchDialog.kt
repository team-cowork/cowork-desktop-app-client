package com.cowork.desktop.client.feature.main.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cowork.desktop.client.data.repository.ChatRepository
import com.cowork.desktop.client.domain.model.ChatMessageSearchItem
import com.cowork.desktop.client.domain.model.ChatMessageSearchQuery
import com.cowork.desktop.client.domain.model.MessageType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private enum class SearchFileFilter(
    val label: String,
    val value: Boolean?,
) {
    All("전체", null),
    WithFile("파일 포함", true),
    WithoutFile("파일 없음", false),
}

@Composable
internal fun MessageSearchDialog(
    teamId: Long,
    projectId: Long?,
    onDismiss: () -> Unit,
) {
    val repository = koinInject<ChatRepository>()
    val scope = rememberCoroutineScope()

    var queryText by remember(teamId, projectId) { mutableStateOf("") }
    var channelIdText by remember(teamId, projectId) { mutableStateOf("") }
    var authorIdText by remember(teamId, projectId) { mutableStateOf("") }
    var fileFilter by remember(teamId, projectId) { mutableStateOf(SearchFileFilter.All) }
    var results by remember(teamId, projectId) { mutableStateOf<List<ChatMessageSearchItem>>(emptyList()) }
    var nextCursor by remember(teamId, projectId) { mutableStateOf<String?>(null) }
    var activeQuery by remember(teamId, projectId) { mutableStateOf<ChatMessageSearchQuery?>(null) }
    var hasSearched by remember(teamId, projectId) { mutableStateOf(false) }
    var isLoading by remember(teamId, projectId) { mutableStateOf(false) }
    var error by remember(teamId, projectId) { mutableStateOf<String?>(null) }
    var requestVersion by remember(teamId, projectId) { mutableIntStateOf(0) }
    var searchJob by remember(teamId, projectId) { mutableStateOf<Job?>(null) }

    fun search(loadMore: Boolean) {
        if (isLoading) return

        val cursor = if (loadMore) nextCursor ?: return else null
        val baseQuery = if (loadMore) {
            activeQuery ?: return
        } else {
            val normalizedQuery = queryText.trim()
            if (normalizedQuery.isEmpty()) {
                error = "검색어를 입력해 주세요."
                return
            }
            val channelIdInput = channelIdText.toOptionalId("채널")
            if (channelIdInput.error != null) {
                error = channelIdInput.error
                return
            }
            val authorIdInput = authorIdText.toOptionalId("작성자")
            if (authorIdInput.error != null) {
                error = authorIdInput.error
                return
            }
            ChatMessageSearchQuery(
                query = normalizedQuery,
                channelId = channelIdInput.value,
                authorId = authorIdInput.value,
                hasFile = fileFilter.value,
                limit = 30,
            )
        }

        val version = ++requestVersion
        searchJob?.cancel()
        error = null
        isLoading = true
        if (!loadMore) {
            activeQuery = baseQuery
            results = emptyList()
            nextCursor = null
            hasSearched = true
        }

        searchJob = scope.launch {
            try {
                val request = baseQuery.copy(before = cursor)
                val page = if (projectId != null) {
                    repository.searchProjectMessages(projectId, request)
                } else {
                    repository.searchTeamMessages(teamId, request)
                }
                if (version != requestVersion) return@launch
                results = if (loadMore) {
                    (results + page.messages).distinctBy(ChatMessageSearchItem::messageId)
                } else {
                    page.messages
                }
                nextCursor = page.nextCursor
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (throwable: Throwable) {
                if (version == requestVersion) {
                    error = throwable.userMessage()
                }
            } finally {
                if (version == requestVersion) {
                    isLoading = false
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.width(880.dp).height(720.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
        ) {
            Column(Modifier.fillMaxSize()) {
                SearchDialogHeader(
                    scopeLabel = if (projectId != null) "프로젝트 #$projectId" else "팀 #$teamId",
                    onDismiss = onDismiss,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = queryText,
                            onValueChange = { queryText = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("메시지 내용") },
                            placeholder = { Text("찾을 단어나 문장을 입력하세요") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            trailingIcon = {
                                if (queryText.isNotEmpty()) {
                                    IconButton(onClick = { queryText = "" }) {
                                        Icon(Icons.Rounded.Close, contentDescription = "검색어 지우기")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { search(loadMore = false) }),
                        )
                        Button(
                            onClick = { search(loadMore = false) },
                            enabled = queryText.isNotBlank() && !isLoading,
                            modifier = Modifier.height(56.dp),
                        ) {
                            if (isLoading && results.isEmpty()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Text("검색")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SearchIdField(
                            value = channelIdText,
                            onValueChange = { channelIdText = it },
                            label = "채널 ID",
                            modifier = Modifier.weight(0.8f),
                        )
                        SearchIdField(
                            value = authorIdText,
                            onValueChange = { authorIdText = it },
                            label = "작성자 ID",
                            modifier = Modifier.weight(0.8f),
                        )
                        Column(
                            modifier = Modifier.weight(1.4f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "첨부파일",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SearchFileFilter.entries.forEach { option ->
                                    FilterChip(
                                        selected = fileFilter == option,
                                        onClick = { fileFilter = option },
                                        label = { Text(option.label) },
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp),
                ) {
                    error?.let { ManagementError(it) }

                    when {
                        !hasSearched -> SearchEmptyState(
                            title = "대화 속 기록을 찾아보세요",
                            description = "메시지 내용과 필요한 필터를 입력하면 이 공간에 검색 결과가 표시됩니다.",
                        )

                        isLoading && results.isEmpty() -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }

                        results.isEmpty() -> SearchEmptyState(
                            title = "일치하는 메시지가 없습니다",
                            description = "검색어를 줄이거나 채널·작성자·파일 필터를 바꿔 보세요.",
                        )

                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "검색 결과",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "${results.size}개 불러옴",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(results, key = ChatMessageSearchItem::messageId) { item ->
                                    SearchResultCard(item)
                                }
                                if (nextCursor != null) {
                                    item(key = "load-more") {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            OutlinedButton(
                                                onClick = { search(loadMore = true) },
                                                enabled = !isLoading,
                                            ) {
                                                if (isLoading) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp,
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                }
                                                Text(if (isLoading) "불러오는 중" else "결과 더 보기")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchDialogHeader(
    scopeLabel: String,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "메시지 검색",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "$scopeLabel · 접근 가능한 채널에서 검색",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onDismiss) { Text("닫기") }
    }
}

@Composable
private fun SearchIdField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit)) },
        modifier = modifier,
        label = { Text(label) },
        placeholder = { Text("전체") },
        singleLine = true,
    )
}

@Composable
private fun SearchEmptyState(
    title: String,
    description: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp).size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(5.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SearchResultCard(item: ChatMessageSearchItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = "#${item.channelId}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = "작성자 ${item.authorId}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
                SearchResultBadge(item.type.searchLabel())
                if (item.hasAttachments) SearchResultBadge("파일")
                if (item.isPinned) SearchResultBadge("고정")
                Spacer(Modifier.weight(1f))
                Text(
                    text = item.createdAt.searchTimestamp(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            val snippets = item.highlights.ifEmpty { listOf(item.content) }.take(2)
            snippets.forEach { snippet ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        modifier = Modifier.width(3.dp).height(42.dp),
                        shape = RoundedCornerShape(2.dp),
                        color = MaterialTheme.colorScheme.primary,
                    ) {}
                    HighlightedSearchText(
                        source = snippet,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun HighlightedSearchText(
    source: String,
    modifier: Modifier = Modifier,
) {
    val highlightBackground = MaterialTheme.colorScheme.primaryContainer
    val highlightForeground = MaterialTheme.colorScheme.onPrimaryContainer
    val text = remember(source, highlightBackground, highlightForeground) {
        source.toHighlightedText(highlightBackground, highlightForeground)
    }
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
}

private fun String.toHighlightedText(
    background: Color,
    foreground: Color,
) = buildAnnotatedString {
    var cursor = 0
    while (cursor < this@toHighlightedText.length) {
        val open = this@toHighlightedText.indexOf("<em>", startIndex = cursor, ignoreCase = true)
        if (open < 0) {
            append(this@toHighlightedText.substring(cursor).removeHighlightTags())
            break
        }
        append(this@toHighlightedText.substring(cursor, open).removeHighlightTags())
        val matchStart = open + 4
        val close = this@toHighlightedText.indexOf("</em>", startIndex = matchStart, ignoreCase = true)
        if (close < 0) {
            append(this@toHighlightedText.substring(open).removeHighlightTags())
            break
        }
        withStyle(
            SpanStyle(
                color = foreground,
                background = background,
                fontWeight = FontWeight.Bold,
            ),
        ) {
            append(this@toHighlightedText.substring(matchStart, close))
        }
        cursor = close + 5
    }
}

private fun String.removeHighlightTags(): String =
    replace("<em>", "", ignoreCase = true).replace("</em>", "", ignoreCase = true)

private data class OptionalSearchId(
    val value: Long?,
    val error: String? = null,
)

private fun String.toOptionalId(fieldName: String): OptionalSearchId {
    if (isBlank()) return OptionalSearchId(value = null)
    val value = toLongOrNull()
    if (value == null || value <= 0) {
        return OptionalSearchId(
            value = null,
            error = "$fieldName ID는 1 이상의 숫자로 입력해 주세요.",
        )
    }
    return OptionalSearchId(value = value)
}

private fun MessageType.searchLabel(): String = when (this) {
    MessageType.Text -> "텍스트"
    MessageType.File -> "파일 메시지"
    MessageType.System -> "시스템"
    MessageType.Unknown -> "메시지"
}

private fun String.searchTimestamp(): String =
    replace('T', ' ').removeSuffix("Z").take(16)
