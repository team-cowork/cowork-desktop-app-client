package com.cowork.desktop.client.feature.main.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cowork.desktop.client.data.repository.ChannelRepository
import com.cowork.desktop.client.data.repository.ChatRepository
import com.cowork.desktop.client.data.repository.VoiceRepository
import com.cowork.desktop.client.data.repository.PreferenceRepository
import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChatAttachment
import com.cowork.desktop.client.domain.model.ChatFileItem
import com.cowork.desktop.client.domain.model.LiveStatus
import com.cowork.desktop.client.domain.model.SharedAccount
import com.cowork.desktop.client.domain.model.SharedAccountProvider
import com.cowork.desktop.client.domain.model.VoiceChannelParticipants
import com.cowork.desktop.client.domain.model.VoiceChannelSettings
import com.cowork.desktop.client.util.pickFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
internal fun ColumnScope.AccountShareChannelContent(
    channel: Channel,
    accountId: Long?,
    canManageChannel: Boolean,
) {
    val repository = koinInject<ChannelRepository>()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    var accounts by remember(channel.id) { mutableStateOf<List<SharedAccount>>(emptyList()) }
    var isLoading by remember(channel.id) { mutableStateOf(true) }
    var isCreateOpen by remember(channel.id) { mutableStateOf(false) }
    var editingAccount by remember(channel.id) { mutableStateOf<SharedAccount?>(null) }
    var error by remember(channel.id) { mutableStateOf<String?>(null) }
    var refreshKey by remember(channel.id) { mutableStateOf(0) }

    LaunchedEffect(channel.id, refreshKey) {
        isLoading = true
        error = null
        runCatching { repository.getSharedAccounts(channel.id) }
            .onSuccess { accounts = it }
            .onFailure { error = it.userMessage() }
        isLoading = false
    }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("공유 계정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("팀이 함께 사용하는 서비스 계정을 안전하게 관리합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(onClick = { isCreateOpen = true }) { Text("계정 추가") }
    }
    Spacer(Modifier.height(14.dp))
    error?.let { ManagementError(it) }
    when {
        isLoading -> CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
        accounts.isEmpty() -> EmptySpecialChannel("등록된 공유 계정이 없습니다.")
        else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(accounts, key = { it.id }) { account ->
                SharedAccountRow(
                    account = account,
                    canEdit = canManageChannel || account.createdBy == accountId,
                    onOpen = { account.loginUrl?.let(uriHandler::openUri) },
                    onEdit = { editingAccount = account },
                    onCopy = {
                        scope.launch {
                            runCatching { repository.copySharedAccountCredential(channel.id, account.id) }
                                .onSuccess {
                                    @Suppress("DEPRECATION")
                                    clipboard.setText(AnnotatedString(it))
                                }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onDelete = {
                        scope.launch {
                            runCatching { repository.deleteSharedAccount(channel.id, account.id) }
                                .onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                )
            }
        }
    }

    if (isCreateOpen) {
        CreateSharedAccountDialog(
            channelId = channel.id,
            onDismiss = { isCreateOpen = false },
            onCreate = { provider, label, identifier, credential ->
                scope.launch {
                    runCatching {
                        repository.createSharedAccount(channel.id, provider, label, identifier, credential)
                    }.onSuccess {
                        isCreateOpen = false
                        refreshKey++
                    }.onFailure { error = it.userMessage() }
                }
            },
            onOAuth = { provider ->
                scope.launch {
                    runCatching { repository.getSharedAccountOAuthUrl(channel.id, provider) }
                        .onSuccess(uriHandler::openUri)
                        .onFailure { error = it.userMessage() }
                }
            },
        )
    }
    editingAccount?.takeIf { canManageChannel || it.createdBy == accountId }?.let { account ->
        EditSharedAccountDialog(
            account = account,
            onDismiss = { editingAccount = null },
            onSave = { label, identifier, credential ->
                scope.launch {
                    runCatching { repository.updateSharedAccount(channel.id, account.id, label, identifier, credential) }
                        .onSuccess {
                            editingAccount = null
                            refreshKey++
                        }
                        .onFailure { error = it.userMessage() }
                }
            },
        )
    }
}

@Composable
private fun SharedAccountRow(
    account: SharedAccount,
    canEdit: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                Text(account.provider.label.take(2), Modifier.padding(horizontal = 10.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(account.displayName ?: account.providerLabel ?: account.provider.label, fontWeight = FontWeight.SemiBold)
                Text(
                    account.accountIdentifier ?: account.maskedCredential ?: if (account.connectedViaOAuth) "OAuth 연결" else "자격 증명 등록됨",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (account.loginUrl != null) TextButton(onClick = onOpen) { Text("열기") }
            if (canEdit) TextButton(onClick = onEdit) { Text("편집") }
            TextButton(onClick = onCopy) { Text("복사") }
            if (canEdit) TextButton(onClick = onDelete) { Text("삭제") }
        }
    }
}

@Composable
private fun EditSharedAccountDialog(
    account: SharedAccount,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String?) -> Unit,
) {
    var label by remember(account.id) { mutableStateOf(account.providerLabel.orEmpty()) }
    var identifier by remember(account.id) { mutableStateOf(account.accountIdentifier.orEmpty()) }
    var credential by remember(account.id) { mutableStateOf("") }
    ManagementDialogShell(
        title = "${account.provider.label} 계정 편집",
        onDismiss = onDismiss,
        sidebar = { Text(account.provider.label, Modifier.padding(10.dp), fontWeight = FontWeight.Bold) },
    ) {
        OutlinedTextField(label, { label = it }, label = { Text("표시 이름") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(identifier, { identifier = it }, label = { Text("계정 ID / 이메일") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(credential, { credential = it }, label = { Text("새 비밀번호 / 토큰 (변경 시 입력)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("취소") }
            Button(onClick = {
                onSave(label.trim().ifBlank { null }, identifier.trim().ifBlank { null }, credential.ifBlank { null })
            }) { Text("저장") }
        }
    }
}

@Composable
private fun CreateSharedAccountDialog(
    channelId: Long,
    onDismiss: () -> Unit,
    onCreate: (SharedAccountProvider, String?, String?, String?) -> Unit,
    onOAuth: (SharedAccountProvider) -> Unit,
) {
    var provider by remember(channelId) { mutableStateOf(SharedAccountProvider.GitHub) }
    var providerExpanded by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var credential by remember { mutableStateOf("") }
    ManagementDialogShell(
        title = "공유 계정 추가",
        onDismiss = onDismiss,
        sidebar = { Text("Account\nShare", Modifier.padding(10.dp), fontWeight = FontWeight.Bold) },
    ) {
        PanelTitle("서비스 연결")
        Box {
            OutlinedButton(onClick = { providerExpanded = true }) { Text(provider.label) }
            DropdownMenu(providerExpanded, { providerExpanded = false }) {
                SharedAccountProvider.entries.filter { it != SharedAccountProvider.Unknown }.forEach { option ->
                    DropdownMenuItem(text = { Text(option.label) }, onClick = { provider = option; providerExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(label, { label = it }, label = { Text("표시 이름") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(identifier, { identifier = it }, label = { Text("계정 ID / 이메일") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(credential, { credential = it }, label = { Text("비밀번호 / 토큰") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (provider.supportsOAuth) {
                OutlinedButton(onClick = { onOAuth(provider) }) { Text("OAuth로 연결") }
                Spacer(Modifier.width(8.dp))
            }
            Button(
                onClick = {
                    onCreate(
                        provider,
                        label.trim().ifBlank { null },
                        identifier.trim().ifBlank { null },
                        credential.ifBlank { null },
                    )
                },
                enabled = credential.isNotBlank() || provider.supportsOAuth,
            ) { Text("저장") }
        }
    }
}

@Composable
internal fun ColumnScope.FileShareChannelContent(
    channel: Channel,
    accountId: Long?,
    isSystemAdmin: Boolean,
) {
    val repository = koinInject<ChatRepository>()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    var files by remember(channel.id) { mutableStateOf<List<ChatFileItem>>(emptyList()) }
    var isLoading by remember(channel.id) { mutableStateOf(true) }
    var isUploading by remember(channel.id) { mutableStateOf(false) }
    var isLoadingMore by remember(channel.id) { mutableStateOf(false) }
    var nextCursor by remember(channel.id) { mutableStateOf<String?>(null) }
    var error by remember(channel.id) { mutableStateOf<String?>(null) }
    var refreshKey by remember(channel.id) { mutableStateOf(0) }

    LaunchedEffect(channel.id, refreshKey) {
        isLoading = true
        runCatching { repository.getFiles(channel.id) }
            .onSuccess { page ->
                files = page.files
                nextCursor = page.nextCursor
            }
            .onFailure { error = it.userMessage() }
        isLoading = false
    }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("파일", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("채널에 공유된 파일을 업로드하고 관리합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
            onClick = {
                scope.launch {
                    val file = pickFile() ?: return@launch
                    isUploading = true
                    error = null
                    val uploadResult = runCatching {
                        val upload = repository.createFileUploadUrl(channel.id, file.name, file.contentType, file.bytes.size.toLong())
                        repository.putFile(upload, file.bytes)
                        val fileUrl = repository.confirmFileUpload(channel.id, upload.objectKey)
                        repository.sendMessage(
                            channelId = channel.id,
                            teamId = channel.teamId,
                            projectId = channel.projectId,
                            content = file.name,
                            attachments = listOf(
                                ChatAttachment(
                                    name = file.name,
                                    url = fileUrl,
                                    size = file.bytes.size.toLong(),
                                    mimeType = file.contentType,
                                ),
                            ),
                        )
                        fileUrl
                    }
                    isUploading = false
                    if (uploadResult.isFailure) {
                        error = uploadResult.exceptionOrNull()?.userMessage()
                        return@launch
                    }

                    // 메시지 저장은 Kafka 비동기이므로 목록에 나타날 때까지 제한적으로 재조회한다.
                    val uploadedUrl = uploadResult.getOrThrow()
                    for (waitMillis in listOf(500L, 1_000L, 2_000L, 4_000L)) {
                        delay(waitMillis)
                        val page = runCatching { repository.getFiles(channel.id) }.getOrNull() ?: continue
                        files = page.files
                        nextCursor = page.nextCursor
                        if (page.files.any { it.fileUrl == uploadedUrl }) return@launch
                    }
                    error = "파일 업로드는 완료됐지만 목록 반영이 지연되고 있습니다. 잠시 후 채널을 다시 열어 주세요."
                }
            },
            enabled = !isUploading,
        ) { Text(if (isUploading) "업로드 중…" else "파일 업로드") }
    }
    Spacer(Modifier.height(14.dp))
    error?.let { ManagementError(it) }
    when {
        isLoading -> CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
        files.isEmpty() -> EmptySpecialChannel("공유된 파일이 없습니다.")
        else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(files, key = { it.fileId }) { file ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(file.fileUrl) },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(file.fileName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${formatFileSize(file.fileSize)} · ${file.uploaderName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { uriHandler.openUri(file.fileUrl) }) { Text("열기") }
                        if (file.uploaderId == accountId || isSystemAdmin) {
                            TextButton(onClick = {
                                scope.launch {
                                    runCatching { repository.deleteFile(channel.id, file.fileId) }
                                        .onSuccess { refreshKey++ }
                                        .onFailure { error = it.userMessage() }
                                }
                            }) { Text("삭제") }
                        }
                    }
                }
            }
            nextCursor?.let { cursor ->
                item(key = "load-more-$cursor") {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isLoadingMore = true
                                error = null
                                runCatching { repository.getFiles(channel.id, before = cursor) }
                                    .onSuccess { page ->
                                        files = (files + page.files).distinctBy(ChatFileItem::fileId)
                                        nextCursor = page.nextCursor
                                    }
                                    .onFailure { error = it.userMessage() }
                                isLoadingMore = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoadingMore,
                    ) { Text(if (isLoadingMore) "불러오는 중…" else "이전 파일 더 보기") }
                }
            }
        }
    }
}

@Composable
internal fun ColumnScope.VoiceChannelContent(channel: Channel) {
    val repository = koinInject<VoiceRepository>()
    val preferenceRepository = koinInject<PreferenceRepository>()
    val scope = rememberCoroutineScope()
    var participants by remember(channel.id) { mutableStateOf<VoiceChannelParticipants?>(null) }
    var liveStatus by remember(channel.id) { mutableStateOf<LiveStatus?>(null) }
    var isLoading by remember(channel.id) { mutableStateOf(true) }
    var error by remember(channel.id) { mutableStateOf<String?>(null) }
    var refreshKey by remember(channel.id) { mutableStateOf(0) }
    var bitrateText by remember(channel.id) { mutableStateOf("") }
    var maxParticipantsText by remember(channel.id) { mutableStateOf("") }

    LaunchedEffect(channel.id, refreshKey) {
        isLoading = true
        runCatching {
            participants = repository.getVoiceChannelParticipants(channel.id)
            liveStatus = runCatching { repository.getLiveStatus(channel.id) }.getOrNull()
            runCatching { preferenceRepository.getVoiceChannelSettings(channel.id) }.getOrNull()?.let { settings ->
                bitrateText = settings.bitrate?.toString().orEmpty()
                maxParticipantsText = settings.maxParticipants?.toString().orEmpty()
            }
        }.onFailure { error = it.userMessage() }
        isLoading = false
    }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("음성 채널", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "참여자 ${participants?.participants?.size ?: 0}명",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = { refreshKey++ }) { Text("새로고침") }
        Spacer(Modifier.width(8.dp))
        OutlinedButton(onClick = {}, enabled = false) { Text("미디어 엔진 필요") }
    }
    Spacer(Modifier.height(14.dp))
    error?.let { ManagementError(it) }
    if (isLoading) CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
    participants?.participants?.forEach { participant ->
        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f), shape = RoundedCornerShape(10.dp)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("사용자 ${participant.userId}", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text("참여 ${participant.joinedAt.take(16)}", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(6.dp))
    }
    Spacer(Modifier.height(18.dp))
    Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text("라이브", fontWeight = FontWeight.Bold)
            Text(
                if (liveStatus?.isLive == true) "방송 중 · 시청자 ${liveStatus?.viewerCount ?: 0}명" else "현재 방송이 없습니다.",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = {}, enabled = false) {
                Text(if (liveStatus?.isLive == true) "데스크톱 시청 미지원" else "데스크톱 방송 미지원")
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text("음성 품질 설정", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    bitrateText,
                    { bitrateText = it.filter(Char::isDigit) },
                    label = { Text("비트레이트") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    maxParticipantsText,
                    { maxParticipantsText = it.filter(Char::isDigit) },
                    label = { Text("최대 참여자") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                Button(onClick = {
                    scope.launch {
                        runCatching {
                            preferenceRepository.updateVoiceChannelSettings(
                                channel.id,
                                VoiceChannelSettings(bitrateText.toIntOrNull(), maxParticipantsText.toIntOrNull()),
                            )
                        }.onFailure { error = it.userMessage() }
                    }
                }) { Text("저장") }
            }
        }
    }
    Spacer(Modifier.weight(1f))
    Text(
        "서버의 참가자·라이브 상태 조회와 채널 설정은 지원합니다. 공식 Kotlin SDK는 Android용이라, JVM 데스크톱 미디어 엔진이 준비되기 전에는 유령 세션 방지를 위해 참여·방송을 비활성화했습니다.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ColumnScope.EmptySpecialChannel(message: String) {
    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1024L * 1024L * 1024L -> "${bytes / (1024L * 1024L * 1024L)} GB"
    bytes >= 1024L * 1024L -> "${bytes / (1024L * 1024L)} MB"
    bytes >= 1024L -> "${bytes / 1024L} KB"
    else -> "$bytes B"
}
