package com.cowork.desktop.client.feature.main.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cowork.desktop.client.data.repository.ChannelRepository
import com.cowork.desktop.client.data.repository.ChatRepository
import com.cowork.desktop.client.data.repository.UserRepository
import com.cowork.desktop.client.domain.model.UserProfile
import com.cowork.desktop.client.domain.model.UserSearchCriteria
import com.cowork.desktop.client.domain.model.UserSearchPage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val DirectoryPageSize = 20

@Composable
internal fun PeopleDirectoryDialog(
    onDismiss: () -> Unit,
    onDirectMessageOpened: (Long) -> Unit,
    userRepository: UserRepository = koinInject(),
    channelRepository: ChannelRepository = koinInject(),
    chatRepository: ChatRepository = koinInject(),
) {
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    var query by remember { mutableStateOf("") }
    var searchRefreshKey by remember { mutableStateOf(0) }
    var results by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var loadedPage by remember { mutableStateOf(0) }
    var hasNextPage by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    var selectedSummary by remember { mutableStateOf<UserProfile?>(null) }
    var selectedProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isProfileLoading by remember { mutableStateOf(false) }
    var profileLoadFailed by remember { mutableStateOf(false) }
    var profileRefreshKey by remember { mutableStateOf(0) }

    var currentUserId by remember { mutableStateOf<Long?>(null) }
    var blockedUserIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isBlockedListLoading by remember { mutableStateOf(true) }
    var openingDmUserIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var changingBlockUserIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var operationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(userRepository) {
        currentUserId = userRepository.getMyProfile()?.id
    }

    LaunchedEffect(chatRepository) {
        isBlockedListLoading = true
        try {
            blockedUserIds = chatRepository.getBlockedUserIds().toSet()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Throwable) {
            operationError = failure.directoryMessage("차단 상태를 불러오지 못했습니다.")
        } finally {
            isBlockedListLoading = false
        }
    }

    LaunchedEffect(query, searchRefreshKey, userRepository) {
        if (query.isNotBlank()) delay(280)
        val normalizedQuery = query.trim()
        isSearching = true
        searchError = null
        loadedPage = 0
        hasNextPage = false
        results = emptyList()
        try {
            val page = userRepository.loadDirectoryPage(normalizedQuery, page = 1)
            results = page.items
            loadedPage = 1
            hasNextPage = page.hasNext
            if (selectedSummary?.id !in page.items.map(UserProfile::id)) {
                selectedSummary = page.items.firstOrNull()
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Throwable) {
            searchError = failure.directoryMessage("사용자를 검색하지 못했습니다.")
            selectedSummary = null
        } finally {
            isSearching = false
        }
    }

    LaunchedEffect(selectedSummary?.id, profileRefreshKey, userRepository) {
        val summary = selectedSummary
        selectedProfile = null
        profileLoadFailed = false
        if (summary == null) return@LaunchedEffect

        isProfileLoading = true
        try {
            selectedProfile = userRepository.getUserProfile(summary.id)
            profileLoadFailed = selectedProfile == null
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            profileLoadFailed = true
        } finally {
            isProfileLoading = false
        }
    }

    fun openDirectMessage(profile: UserProfile) {
        if (profile.id == currentUserId || profile.id in openingDmUserIds) return
        operationError = null
        openingDmUserIds = openingDmUserIds + profile.id
        scope.launch {
            try {
                val channelId = channelRepository.openDirectMessage(profile.id)
                onDirectMessageOpened(channelId)
                onDismiss()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Throwable) {
                operationError = failure.directoryMessage("대화를 열지 못했습니다.")
            } finally {
                openingDmUserIds = openingDmUserIds - profile.id
            }
        }
    }

    fun toggleBlock(profile: UserProfile) {
        if (
            profile.id == currentUserId ||
            profile.id in changingBlockUserIds ||
            isBlockedListLoading
        ) {
            return
        }
        operationError = null
        changingBlockUserIds = changingBlockUserIds + profile.id
        scope.launch {
            val wasBlocked = profile.id in blockedUserIds
            try {
                if (wasBlocked) {
                    chatRepository.unblockUser(profile.id)
                    blockedUserIds = blockedUserIds - profile.id
                } else {
                    chatRepository.blockUser(profile.id)
                    blockedUserIds = blockedUserIds + profile.id
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Throwable) {
                operationError = failure.directoryMessage(
                    if (wasBlocked) "차단을 해제하지 못했습니다." else "사용자를 차단하지 못했습니다.",
                )
            } finally {
                changingBlockUserIds = changingBlockUserIds - profile.id
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.width(920.dp).height(640.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 18.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                DirectoryHeader(
                    resultCount = results.size,
                    isSearching = isSearching,
                    onDismiss = onDismiss,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                operationError?.let { message ->
                    DirectoryErrorBanner(
                        message = message,
                        onDismiss = { operationError = null },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .width(474.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .focusRequester(focusRequester),
                            placeholder = { Text("이름 또는 닉네임으로 검색") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(Icons.Rounded.Close, contentDescription = "검색어 지우기")
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { searchRefreshKey++ },
                            ),
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                isSearching -> DirectoryLoading("사용자를 찾는 중")

                                searchError != null -> DirectorySearchFailure(
                                    message = searchError.orEmpty(),
                                    onRetry = { searchRefreshKey++ },
                                )

                                results.isEmpty() -> DirectoryEmpty(query = query)

                                else -> LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    items(results, key = UserProfile::id) { profile ->
                                        DirectoryUserRow(
                                            profile = profile,
                                            selected = profile.id == selectedSummary?.id,
                                            isSelf = profile.id == currentUserId,
                                            isBlocked = profile.id in blockedUserIds,
                                            isOpeningDm = profile.id in openingDmUserIds,
                                            isChangingBlock = profile.id in changingBlockUserIds,
                                            blockActionEnabled = !isBlockedListLoading,
                                            onSelect = { selectedSummary = profile },
                                            onOpenDm = { openDirectMessage(profile) },
                                            onToggleBlock = { toggleBlock(profile) },
                                        )
                                    }

                                    if (hasNextPage) {
                                        item(key = "load-more") {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                TextButton(
                                                    enabled = !isLoadingMore,
                                                    onClick = {
                                                        val requestedQuery = query.trim()
                                                        val requestedPage = loadedPage + 1
                                                        isLoadingMore = true
                                                        operationError = null
                                                        scope.launch {
                                                            try {
                                                                val page = userRepository.loadDirectoryPage(
                                                                    query = requestedQuery,
                                                                    page = requestedPage,
                                                                )
                                                                if (query.trim() == requestedQuery) {
                                                                    results = (results + page.items)
                                                                        .distinctBy(UserProfile::id)
                                                                    loadedPage = requestedPage
                                                                    hasNextPage = page.hasNext
                                                                }
                                                            } catch (cancellation: CancellationException) {
                                                                throw cancellation
                                                            } catch (failure: Throwable) {
                                                                operationError = failure.directoryMessage(
                                                                    "다음 검색 결과를 불러오지 못했습니다.",
                                                                )
                                                            } finally {
                                                                isLoadingMore = false
                                                            }
                                                        }
                                                    },
                                                ) {
                                                    if (isLoadingMore) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(16.dp),
                                                            strokeWidth = 2.dp,
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                    }
                                                    Text(if (isLoadingMore) "불러오는 중" else "더 보기")
                                                }
                                            }
                                        }
                                    } else {
                                        item(key = "list-bottom-space") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    ProfileDetailPane(
                        summary = selectedSummary,
                        profile = selectedProfile,
                        isLoading = isProfileLoading,
                        loadFailed = profileLoadFailed,
                        isSelf = selectedSummary?.id == currentUserId,
                        isBlocked = selectedSummary?.id in blockedUserIds,
                        isOpeningDm = selectedSummary?.id in openingDmUserIds,
                        isChangingBlock = selectedSummary?.id in changingBlockUserIds,
                        blockActionEnabled = !isBlockedListLoading,
                        onRetryProfile = { profileRefreshKey++ },
                        onOpenDm = {
                            (selectedProfile ?: selectedSummary)?.let(::openDirectMessage)
                        },
                        onToggleBlock = {
                            (selectedProfile ?: selectedSummary)?.let(::toggleBlock)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectoryHeader(
    resultCount: Int,
    isSearching: Boolean,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 10.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(7.dp),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Text(
                text = "PEOPLE",
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "사람 찾기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (isSearching) "디렉터리를 확인하고 있습니다." else "현재 결과 ${resultCount}명",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Rounded.Close, contentDescription = "사람 찾기 닫기")
        }
    }
}

@Composable
private fun DirectoryUserRow(
    profile: UserProfile,
    selected: Boolean,
    isSelf: Boolean,
    isBlocked: Boolean,
    isOpeningDm: Boolean,
    isChangingBlock: Boolean,
    blockActionEnabled: Boolean,
    onSelect: () -> Unit,
    onOpenDm: () -> Unit,
    onToggleBlock: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        } else {
            Color.Transparent
        },
        border = if (selected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DirectoryAvatar(profile = profile, size = 42)

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = profile.directoryName(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isSelf) {
                        DirectoryPill(text = "나", emphasized = true)
                    } else if (isBlocked) {
                        DirectoryPill(text = "차단됨", emphasized = false)
                    }
                }
                Text(
                    text = profile.directorySubtitle(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                profile.statusMessage?.takeIf(String::isNotBlank)?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                DirectoryMiniAction(
                    label = "메시지",
                    icon = Icons.Rounded.ChatBubble,
                    loading = isOpeningDm,
                    enabled = !isSelf,
                    onClick = onOpenDm,
                )
                DirectoryMiniAction(
                    label = if (isBlocked) "해제" else "차단",
                    icon = Icons.Rounded.Block,
                    loading = isChangingBlock,
                    enabled = !isSelf && blockActionEnabled,
                    destructive = !isBlocked,
                    onClick = onToggleBlock,
                )
            }
        }
    }
}

@Composable
private fun ProfileDetailPane(
    summary: UserProfile?,
    profile: UserProfile?,
    isLoading: Boolean,
    loadFailed: Boolean,
    isSelf: Boolean,
    isBlocked: Boolean,
    isOpeningDm: Boolean,
    isChangingBlock: Boolean,
    blockActionEnabled: Boolean,
    onRetryProfile: () -> Unit,
    onOpenDm: () -> Unit,
    onToggleBlock: () -> Unit,
) {
    val displayedProfile = profile ?: summary
    Box(modifier = Modifier.fillMaxSize()) {
        if (displayedProfile == null) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text("프로필을 선택하세요", fontWeight = FontWeight.SemiBold)
                Text(
                    "검색 결과를 선택하면 자세한 정보를 볼 수 있습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    DirectoryAvatar(profile = displayedProfile, size = 72)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayedProfile.directoryName(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (
                            !displayedProfile.nickname.isNullOrBlank() &&
                            displayedProfile.nickname != displayedProfile.name
                        ) {
                            Text(
                                text = displayedProfile.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = displayedProfile.status.directoryStatusLabel(),
                            style = MaterialTheme.typography.labelMedium,
                            color = directoryStatusColor(displayedProfile.status),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(
                            text = "지금의 상태",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                        )
                        Text(
                            text = displayedProfile.statusMessage
                                ?.takeIf(String::isNotBlank)
                                ?: "상태 메시지가 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "프로필",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(6.dp))

                DirectoryProfileLine(
                    icon = Icons.Rounded.Email,
                    label = "이메일",
                    value = displayedProfile.email.takeIf(String::isNotBlank) ?: "공개되지 않음",
                )
                DirectoryProfileLine(
                    icon = Icons.Rounded.School,
                    label = "전공",
                    value = listOfNotNull(
                        displayedProfile.major?.takeIf(String::isNotBlank),
                        displayedProfile.specialty?.takeIf(String::isNotBlank),
                    ).joinToString(" · ").ifBlank { "등록되지 않음" },
                )
                DirectoryProfileLine(
                    icon = Icons.Rounded.Person,
                    label = "학생 정보",
                    value = listOfNotNull(
                        displayedProfile.studentRole?.takeIf(String::isNotBlank),
                        displayedProfile.studentNumber?.takeIf(String::isNotBlank),
                    ).joinToString(" · ").ifBlank { "등록되지 않음" },
                )

                if (displayedProfile.roles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        displayedProfile.roles.take(3).forEach { role ->
                            DirectoryPill(
                                text = role.removePrefix("ROLE_"),
                                emphasized = false,
                            )
                        }
                    }
                }

                displayedProfile.description?.takeIf(String::isNotBlank)?.let { description ->
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "소개",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp)
                        Text(
                            "최신 프로필을 확인하는 중",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else if (loadFailed) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "상세 프로필을 불러오지 못해 검색 정보를 표시합니다.",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            IconButton(onClick = onRetryProfile) {
                                Icon(
                                    Icons.Rounded.Refresh,
                                    contentDescription = "프로필 다시 불러오기",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isSelf) {
                    Text(
                        "내 프로필입니다.",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    OutlinedButton(
                        enabled = blockActionEnabled && !isChangingBlock,
                        onClick = onToggleBlock,
                    ) {
                        if (isChangingBlock) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                Icons.Rounded.Block,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                        }
                        Text(if (isBlocked) "차단 해제" else "차단")
                    }
                    Button(
                        enabled = !isOpeningDm,
                        onClick = onOpenDm,
                    ) {
                        if (isOpeningDm) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                Icons.Rounded.ChatBubble,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                        }
                        Text("메시지")
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectoryAvatar(profile: UserProfile, size: Int) {
    val statusColor = directoryStatusColor(profile.status)
    Box(modifier = Modifier.size(size.dp)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = profile.directoryName().firstOrNull()?.uppercase() ?: "?",
                    style = if (size >= 60) {
                        MaterialTheme.typography.headlineMedium
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Black,
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(if (size >= 60) 18.dp else 14.dp)
                .border(
                    width = if (size >= 60) 3.dp else 2.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                )
                .background(statusColor, CircleShape),
        )
    }
}

@Composable
private fun DirectoryMiniAction(
    label: String,
    icon: ImageVector,
    loading: Boolean,
    enabled: Boolean,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        destructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = Color.Transparent,
        modifier = Modifier.clickable(enabled = enabled && !loading, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = contentColor,
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = contentColor,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DirectoryPill(text: String, emphasized: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (emphasized) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (emphasized) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
        )
    }
}

@Composable
private fun DirectoryProfileLine(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DirectoryLoading(label: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DirectorySearchFailure(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(onClick = onRetry) {
            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(7.dp))
            Text("다시 시도")
        }
    }
}

@Composable
private fun DirectoryEmpty(query: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Rounded.Search,
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            if (query.isBlank()) "표시할 사용자가 없습니다." else "“${query.trim()}” 검색 결과가 없습니다.",
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            if (query.isBlank()) "잠시 후 다시 시도해 주세요." else "이름이나 닉네임 철자를 확인해 주세요.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DirectoryErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.errorContainer) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "오류 닫기",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun directoryStatusColor(status: String?): Color = when (status?.uppercase()) {
    "ONLINE", "ACTIVE" -> MaterialTheme.colorScheme.tertiary
    "DO_NOT_DISTURB", "DND", "BUSY" -> MaterialTheme.colorScheme.error
    "AWAY", "IDLE" -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.outline
}

private fun String?.directoryStatusLabel(): String = when (this?.uppercase()) {
    "ONLINE", "ACTIVE" -> "온라인"
    "DO_NOT_DISTURB", "DND", "BUSY" -> "방해 금지"
    "AWAY", "IDLE" -> "자리 비움"
    "OFFLINE" -> "오프라인"
    else -> "상태 정보 없음"
}

private fun UserProfile.directoryName(): String =
    nickname?.takeIf(String::isNotBlank)
        ?: name.takeIf(String::isNotBlank)
        ?: email.takeIf(String::isNotBlank)
        ?: "사용자 $id"

private fun UserProfile.directorySubtitle(): String =
    listOfNotNull(
        name.takeIf { it.isNotBlank() && it != directoryName() },
        major?.takeIf(String::isNotBlank),
        studentRole?.takeIf(String::isNotBlank),
    ).joinToString(" · ").ifBlank {
        email.takeIf(String::isNotBlank) ?: "프로필 정보 없음"
    }

private data class DirectoryPage(
    val items: List<UserProfile>,
    val hasNext: Boolean,
)

private data class SearchAttempt(
    val page: UserSearchPage?,
    val failure: Throwable?,
)

private suspend fun UserRepository.loadDirectoryPage(query: String, page: Int): DirectoryPage {
    if (query.isBlank()) {
        val result = searchUsers(
            UserSearchCriteria(
                page = page,
                pageSize = DirectoryPageSize,
            ),
        )
        return DirectoryPage(items = result.items, hasNext = result.hasNext)
    }

    return coroutineScope {
        val nameSearch = async {
            searchAttempt(
                UserSearchCriteria(
                    name = query,
                    page = page,
                    pageSize = DirectoryPageSize,
                ),
            )
        }
        val nicknameSearch = async {
            searchAttempt(
                UserSearchCriteria(
                    nickname = query,
                    page = page,
                    pageSize = DirectoryPageSize,
                ),
            )
        }
        val nameResult = nameSearch.await()
        val nicknameResult = nicknameSearch.await()
        val availablePages = listOfNotNull(nameResult.page, nicknameResult.page)
        if (availablePages.isEmpty()) {
            throw nameResult.failure
                ?: nicknameResult.failure
                ?: IllegalStateException("검색 결과를 받지 못했습니다.")
        }

        DirectoryPage(
            items = availablePages
                .flatMap(UserSearchPage::items)
                .distinctBy(UserProfile::id)
                .sortedBy { it.directoryName().lowercase() },
            hasNext = availablePages.any(UserSearchPage::hasNext),
        )
    }
}

private suspend fun UserRepository.searchAttempt(criteria: UserSearchCriteria): SearchAttempt =
    try {
        SearchAttempt(page = searchUsers(criteria), failure = null)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        SearchAttempt(page = null, failure = failure)
    }

private fun Throwable.directoryMessage(fallback: String): String {
    val detail = message?.takeIf(String::isNotBlank) ?: return fallback
    return "$fallback $detail"
}
