package com.cowork.desktop.client.feature.main.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.window.Dialog
import com.cowork.desktop.client.domain.model.AppLanguage
import com.cowork.desktop.client.domain.model.AppTheme
import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelType
import com.cowork.desktop.client.domain.model.DateFormat
import com.cowork.desktop.client.domain.model.Project
import com.cowork.desktop.client.domain.model.ProjectStatus
import com.cowork.desktop.client.domain.model.TeamRole
import com.cowork.desktop.client.domain.model.TeamSummary
import com.cowork.desktop.client.domain.model.Thread
import com.cowork.desktop.client.domain.model.TimeFormat
import com.cowork.desktop.client.domain.model.Webhook
import com.cowork.desktop.client.domain.model.UserStatus
import com.cowork.desktop.client.feature.main.component.MainComponent
import com.cowork.desktop.client.feature.main.store.MainStore
import com.cowork.desktop.client.data.repository.ChatRepository
import com.cowork.desktop.client.data.repository.ThreadRepository
import com.cowork.desktop.client.data.repository.UserRepository
import com.cowork.desktop.client.data.repository.WebhookRepository
import com.cowork.desktop.client.data.repository.MeetingNoteRepository
import com.cowork.desktop.client.domain.model.UserProfileUpdate
import com.cowork.desktop.client.domain.model.UserStatusUpdate
import com.cowork.desktop.client.ui.theme.coworkExtendedColors
import com.cowork.desktop.client.ui.theme.CoworkTheme
import com.cowork.desktop.client.util.decodeImageBitmap
import com.cowork.desktop.client.util.horizontalResizeCursor
import com.cowork.desktop.client.util.pickImageBytes
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cowork.desktop.client.Res
import com.cowork.desktop.client.logo_cowork
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

// status colors resolved at composition time via coworkExtendedColors

private val DndOptions = listOf(
    "30분" to 0.5,
    "1시간" to 1.0,
    "2시간" to 2.0,
    "4시간" to 4.0,
    "해제 없음" to null,
)

@Composable
fun MainScreen(component: MainComponent) {
    val state by component.state.collectAsState()
    val density = LocalDensity.current
    var teamRailWidth by remember {
        mutableStateOf(component.layoutPreferenceStorage.getTeamRailWidth()?.dp?.coerceIn(60.dp, 80.dp) ?: 68.dp)
    }
    var channelPaneWidth by remember {
        mutableStateOf(component.layoutPreferenceStorage.getChannelPaneWidth()?.dp?.coerceIn(220.dp, 420.dp) ?: 280.dp)
    }
    var managedTeamId by remember { mutableStateOf<Long?>(null) }
    var managedChannelId by remember { mutableStateOf<Long?>(null) }
    var isPeopleDirectoryOpen by remember { mutableStateOf(false) }
    var directMessageChannelId by remember { mutableStateOf<Long?>(null) }
    var isMessageSearchOpen by remember { mutableStateOf(false) }
    var isJoinTeamOpen by remember { mutableStateOf(false) }

    CoworkTheme(darkTheme = state.accountTheme == AppTheme.Dark) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                TeamRail(
                    state = state,
                    width = teamRailWidth,
                    onTeamClick = component::onTeamClick,
                    onCreateTeamClick = component::onCreateTeamClick,
                    onManageTeam = { managedTeamId = it },
                    onJoinTeam = { isJoinTeamOpen = true },
                )

                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.34f))

                ChannelPane(
                    state = state,
                    width = channelPaneWidth,
                    onChannelClick = component::onChannelClick,
                    onCreateChannelClick = component::onCreateChannelClick,
                    onProjectClick = component::onProjectClick,
                    onCreateProjectClick = component::onCreateProjectClick,
                    onAccountBarClick = component::onAccountMenuClick,
                    onReorderChannels = component::onReorderChannels,
                    onReorderProjects = component::onReorderProjects,
                    onThreadClick = component::onThreadClick,
                    onManageTeam = { managedTeamId = state.selectedTeamId },
                    onManageChannel = { managedChannelId = it },
                    onOpenPeople = { isPeopleDirectoryOpen = true },
                    onOpenSearch = { isMessageSearchOpen = true },
                )

                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

                WorkspacePane(state = state, component = component)
            }

            // 드래그 핸들을 Row 바깥에서 오버레이 — 1dp 시각 라인과 겹치도록 배치해 갭 없음
            PanelDragHandle(
                xOffset = teamRailWidth - 3.dp,
                    onDrag = { delta ->
                        teamRailWidth = with(density) {
                            (teamRailWidth + delta.toDp()).coerceIn(60.dp, 80.dp)
                        }
                        component.layoutPreferenceStorage.saveTeamRailWidth(teamRailWidth.value)
                    },
                )
            PanelDragHandle(
                xOffset = teamRailWidth + 1.dp + channelPaneWidth - 3.dp,
                    onDrag = { delta ->
                        channelPaneWidth = with(density) {
                            (channelPaneWidth + delta.toDp()).coerceIn(220.dp, 420.dp)
                        }
                        component.layoutPreferenceStorage.saveChannelPaneWidth(channelPaneWidth.value)
                    },
                )

            if (state.isAccountMenuOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = component::onAccountMenuDismiss,
                        ),
                )
            }

            AnimatedVisibility(
                visible = state.isAccountMenuOpen,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = teamRailWidth + 2.dp, bottom = 64.dp)
                    .width(436.dp),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                AccountMenuCard(
                    state = state,
                    onSettingsClick = component::onSettingsClick,
                    onStatusChange = component::onStatusChange,
                    onSignOut = component::onSignOutClick,
                    onUploadProfileImage = component::onUploadProfileImage,
                )
            }

            if (state.isCreateTeamOpen) {
                CreateTeamDialog(
                    state = state,
                    onDismiss = component::onCreateTeamDismiss,
                    onNameChange = component::onCreateTeamNameChange,
                    onDescriptionChange = component::onCreateTeamDescriptionChange,
                    onIconChange = component::onCreateTeamIconChange,
                    onSubmit = component::onCreateTeamSubmit,
                )
            }

            if (state.isCreateChannelOpen) {
                CreateChannelDialog(
                    state = state,
                    onDismiss = component::onCreateChannelDismiss,
                    onNameChange = component::onCreateChannelNameChange,
                    onDescriptionChange = component::onCreateChannelDescriptionChange,
                    onTypeChange = component::onCreateChannelTypeChange,
                    onPrivateChange = component::onCreateChannelPrivateChange,
                    onSubmit = component::onCreateChannelSubmit,
                )
            }

            if (state.isCreateProjectOpen) {
                CreateProjectDialog(
                    state = state,
                    onDismiss = component::onCreateProjectDismiss,
                    onNameChange = component::onCreateProjectNameChange,
                    onDescriptionChange = component::onCreateProjectDescriptionChange,
                    onSubmit = component::onCreateProjectSubmit,
                )
            }

            if (state.isSettingsOpen) {
                SettingsDialog(
                    state = state,
                    onDismiss = component::onSettingsDismiss,
                    onThemeChange = component::onThemeChange,
                    onLanguageChange = component::onLanguageChange,
                    onTimeFormatChange = component::onTimeFormatChange,
                    onDateFormatChange = component::onDateFormatChange,
                    onMarketingEmailChange = component::onMarketingEmailChange,
                    onReload = component::onReloadClick,
                )
            }

            state.teams.firstOrNull { it.id == managedTeamId }?.let { team ->
                TeamManagementDialog(
                    teamSummary = team,
                    currentUserId = state.accountId,
                    onDismiss = { managedTeamId = null },
                    onChanged = component::onReloadClick,
                )
            }

            state.channels.firstOrNull { it.id == managedChannelId }?.let { channel ->
                ChannelManagementDialog(
                    channel = channel,
                    accountId = state.accountId,
                    canManage = state.selectedTeam?.myRole?.isAtLeastAdmin() == true || channel.createdBy == state.accountId,
                    profiles = state.memberProfiles,
                    projects = state.projects,
                    onDismiss = { managedChannelId = null },
                    onChanged = component::onReloadClick,
                )
            }

            if (isPeopleDirectoryOpen) {
                PeopleDirectoryDialog(
                    onDismiss = { isPeopleDirectoryOpen = false },
                    onDirectMessageOpened = { channelId ->
                        isPeopleDirectoryOpen = false
                        directMessageChannelId = channelId
                    },
                )
            }

            directMessageChannelId?.let { channelId ->
                DirectMessageDialog(channelId = channelId, onDismiss = { directMessageChannelId = null })
            }

            if (isMessageSearchOpen && state.selectedTeamId != null) {
                MessageSearchDialog(
                    teamId = state.selectedTeamId!!,
                    projectId = state.selectedProjectId,
                    onDismiss = { isMessageSearchOpen = false },
                )
            }

            if (isJoinTeamOpen) {
                JoinTeamDialog(
                    onDismiss = { isJoinTeamOpen = false },
                    onJoined = {
                        isJoinTeamOpen = false
                        component.onReloadClick()
                    },
                )
            }
        }
    }
    }
}

@Composable
private fun TeamRail(
    state: MainStore.State,
    width: Dp,
    onTeamClick: (Long) -> Unit,
    onCreateTeamClick: () -> Unit,
    onManageTeam: (Long) -> Unit,
    onJoinTeam: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CoworkLogoIcon()

        HorizontalDivider(modifier = Modifier.padding(horizontal = 10.dp))

        if (state.isLoadingTeams) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.teams, key = { it.id }) { team ->
                TeamAvatar(
                    team = team,
                    isSelected = team.id == state.selectedTeamId,
                    onClick = { onTeamClick(team.id) },
                    onManage = { onManageTeam(team.id) },
                )
            }

            item {
                AddTeamButton(onClick = onCreateTeamClick)
            }
            item {
                JoinTeamButton(onClick = onJoinTeam)
            }
        }
    }
}

@Composable
private fun AddTeamButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(if (isHovered) MaterialTheme.shapes.medium else CircleShape)
            .background(
                if (isHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .hoverable(interactionSource)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "팀 추가",
            modifier = Modifier.size(19.dp),
            tint = Color(0xFF23A55A),
        )
    }
}

@Composable
private fun JoinTeamButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Surface(
        modifier = Modifier.size(36.dp).hoverable(interactionSource).clickable(onClick = onClick),
        color = if (isHovered) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = CircleShape,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Link, contentDescription = "초대 코드로 팀 참여", modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun CoworkLogoIcon() {
    Image(
        painter = painterResource(Res.drawable.logo_cowork),
        contentDescription = "CoWork",
        modifier = Modifier.size(36.dp),
    )
}

@Composable
private fun TeamAvatar(
    team: TeamSummary,
    isSelected: Boolean,
    onClick: () -> Unit,
    onManage: () -> Unit,
) {
    val background = if (isSelected) MaterialTheme.colorScheme.primary
                     else MaterialTheme.colorScheme.primaryContainer
    val foreground = if (isSelected) MaterialTheme.colorScheme.onPrimary
                     else MaterialTheme.colorScheme.onPrimaryContainer
    var contextMenuVisible by remember { mutableStateOf(false) }
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val httpClient = koinInject<HttpClient>()
    val iconBitmap = rememberRemoteImageBitmap(team.iconUrl, httpClient)

    Box {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(background)
                .clickable(onClick = onClick)
                .pointerInput(team.id) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                contextMenuVisible = true
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = team.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = team.name.firstOrNull()?.uppercase() ?: "?",
                    color = foreground,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        DropdownMenu(
            expanded = contextMenuVisible,
            onDismissRequest = { contextMenuVisible = false },
        ) {
            Text(
                text = team.name,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = { Text("팀 이름 복사", style = MaterialTheme.typography.bodySmall) },
                onClick = {
                    @Suppress("DEPRECATION")
                    clipboardManager.setText(AnnotatedString(team.name))
                    contextMenuVisible = false
                },
                leadingIcon = { Icon(Icons.Rounded.ContentCopy, null, Modifier.size(16.dp)) },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = { Text(if (team.myRole.isAtLeastAdmin()) "팀 관리" else "팀 정보 / 나가기", style = MaterialTheme.typography.bodySmall) },
                onClick = {
                    contextMenuVisible = false
                    onManage()
                },
                leadingIcon = { Icon(Icons.Rounded.Settings, null, Modifier.size(16.dp)) },
            )
        }
    }
}

// Row 바깥 Box 위에 absoluteOffset으로 올리는 투명 드래그 핸들.
// 시각 라인(VerticalDivider)과 분리되어 있어 패널 배경색과 1dp 선 사이 갭이 없음.
@Composable
private fun PanelDragHandle(
    xOffset: Dp,
    onDrag: (Float) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .absoluteOffset(x = xOffset)
            .fillMaxHeight()
            .width(6.dp)
            .horizontalResizeCursor()
            .hoverable(interactionSource)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    onDrag(dragAmount)
                }
            },
    )
}

@Composable
private fun ChannelPane(
    state: MainStore.State,
    width: Dp,
    onChannelClick: (Long) -> Unit,
    onCreateChannelClick: () -> Unit,
    onProjectClick: (Long) -> Unit,
    onCreateProjectClick: () -> Unit,
    onAccountBarClick: () -> Unit,
    onReorderChannels: (Int, Int) -> Unit,
    onReorderProjects: (Int, Int) -> Unit,
    onThreadClick: (Long) -> Unit,
    onManageTeam: () -> Unit,
    onManageChannel: (Long) -> Unit,
    onOpenPeople: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 64.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.selectedTeam?.name ?: "팀 없음",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = state.selectedTeam?.myRole?.label() ?: "팀을 생성하거나 초대받아 시작하세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (state.selectedTeamId != null) {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Rounded.Tune, contentDescription = "메시지 검색", modifier = Modifier.size(19.dp))
                    }
                    IconButton(onClick = onOpenPeople) {
                        Icon(Icons.Rounded.Person, contentDescription = "사용자 찾기", modifier = Modifier.size(19.dp))
                    }
                    IconButton(onClick = onManageTeam) {
                        Icon(Icons.Rounded.Settings, contentDescription = "팀 관리", modifier = Modifier.size(19.dp))
                    }
                }
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (state.selectedTeamId != null) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "채널",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                    TextButton(onClick = onCreateChannelClick) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            when {
                state.selectedTeamId == null -> EmptyPaneText("왼쪽 + 버튼으로 팀을 생성하세요.")
                state.isLoadingChannels -> Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
                state.channels.isEmpty() -> EmptyPaneText("아직 채널이 없습니다.")
                else -> DraggableItemList(
                    items = state.channels,
                    key = { it.id },
                    onReorder = onReorderChannels,
                ) { channel, isDragging ->
                    Column {
                        ChannelRow(
                            channel = channel,
                            isSelected = channel.id == state.selectedChannelId && !isDragging,
                            canManage = state.selectedTeam?.myRole?.isAtLeastAdmin() == true || channel.createdBy == state.accountId,
                            onClick = { onChannelClick(channel.id) },
                            onManage = { onManageChannel(channel.id) },
                            unreadCount = state.unreadCounts[channel.id] ?: 0,
                        )
                        if (channel.id == state.selectedChannelId && channel.type == ChannelType.Text) {
                            state.threads.filter { !it.isArchived }.forEach { thread ->
                                ThreadSidebarRow(
                                    thread = thread,
                                    isSelected = thread.id == state.selectedThreadId,
                                    onClick = { onThreadClick(thread.id) },
                                )
                            }
                        }
                    }
                }
            }

            if (state.selectedTeamId != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "프로젝트",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                    TextButton(onClick = onCreateProjectClick) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                when {
                    state.isLoadingProjects -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).padding(top = 4.dp),
                        strokeWidth = 2.dp,
                    )
                    state.projects.isEmpty() -> EmptyPaneText("아직 프로젝트가 없습니다.")
                    else -> DraggableItemList(
                        items = state.projects,
                        key = { it.id },
                        onReorder = onReorderProjects,
                    ) { project, isDragging ->
                        ProjectRow(
                            project = project,
                            isSelected = project.id == state.selectedProjectId && !isDragging,
                            teamRole = state.selectedTeam?.myRole ?: TeamRole.Unknown,
                            onClick = { onProjectClick(project.id) },
                        )
                    }
                }
            }
        }

        // 계정 바 (하단 고정)
        AccountBar(
            state = state,
            modifier = Modifier.align(Alignment.BottomStart),
            onClick = onAccountBarClick,
        )
    }
}

@Composable
private fun AccountBar(
    state: MainStore.State,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // 팝업이 열려있거나 호버 중이면 @github 표시 (팝업 열릴 때 hover 해제로 인한 역방향 애니메이션 방지)
    val showGithub = (isHovered || state.isAccountMenuOpen) && !state.accountGithub.isNullOrBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                if (isHovered) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                else Color.Transparent
            )
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ProfileAvatar(
            imageUrl = state.accountProfileImageUrl,
            fallback = state.accountInitial(),
            size = 36.dp,
            status = state.accountStatus,
            ringColor = MaterialTheme.colorScheme.surface,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.accountDisplayName(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // 자기 공간 내에서 위로 슬라이드하며 텍스트 전환
            AnimatedContent(
                targetState = showGithub,
                transitionSpec = {
                    if (targetState) {
                        // 호버 진입: 아래서 위로 올라옴
                        (slideInVertically(tween(200)) { it } + fadeIn(tween(150))) togetherWith
                        (slideOutVertically(tween(200)) { -it } + fadeOut(tween(120)))
                    } else {
                        // 호버 해제: 위에서 아래로 내려옴
                        (slideInVertically(tween(200)) { -it } + fadeIn(tween(150))) togetherWith
                        (slideOutVertically(tween(200)) { it } + fadeOut(tween(120)))
                    }
                },
                modifier = Modifier.fillMaxWidth().clipToBounds(),
                label = "subtitleSlide",
            ) { showingGithub ->
                Text(
                    text = if (showingGithub) "@${state.accountGithub}" else state.accountStatus.label(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (showingGithub) MaterialTheme.colorScheme.primary else state.accountStatus.dotColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AccountMenuCard(
    state: MainStore.State,
    onSettingsClick: () -> Unit,
    onStatusChange: (UserStatus, Double?) -> Unit,
    onSignOut: () -> Unit,
    onUploadProfileImage: (ByteArray, String) -> Unit,
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var isDndSelectorOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.width(436.dp)) {
        Surface(
            modifier = Modifier.width(280.dp).padding(8.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
            ),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .align(Alignment.TopStart)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary,
                                    ),
                                ),
                            ),
                    )

                    ProfileAvatar(
                        imageUrl = state.accountProfileImageUrl,
                        fallback = state.accountInitial(),
                        size = 68.dp,
                        status = state.accountStatus,
                        ringColor = MaterialTheme.colorScheme.surface,
                        isUploading = state.isUploadingProfileImage,
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp),
                        onEditClick = {
                            coroutineScope.launch {
                                val result = pickImageBytes()
                                if (result != null) onUploadProfileImage(result.first, result.second)
                            }
                        },
                    )

                }

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = state.accountDisplayName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = state.accountEmail ?: "이메일 정보 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    val hasStudentNumber = !state.accountStudentNumber.isNullOrBlank()
                    val profileLine = listOfNotNull(
                        state.accountStudentNumber?.takeIf { it.isNotBlank() },
                        state.accountMajor?.takeIf { it.isNotBlank() },
                        state.accountStudentRole?.takeIf { role ->
                            role.isNotBlank() && (
                                !hasStudentNumber ||
                                !role.equals("GENERAL_STUDENT", ignoreCase = true)
                            )
                        },
                    ).joinToString(" · ")

                    if (profileLine.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profileLine,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    state.accountGithub?.takeIf { it.isNotBlank() }?.let { github ->
                        val uriHandler = LocalUriHandler.current
                        val githubInteraction = remember { MutableInteractionSource() }
                        val isGithubHovered by githubInteraction.collectIsHoveredAsState()
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "@$github",
                            style = MaterialTheme.typography.labelMedium.copy(
                                textDecoration = if (isGithubHovered) TextDecoration.Underline else TextDecoration.None,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .hoverable(githubInteraction)
                                .clickable(indication = null, interactionSource = githubInteraction) {
                                    uriHandler.openUri("https://github.com/$github")
                                },
                        )
                    }

                    state.accountDescription?.takeIf { it.isNotBlank() }?.let { description ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()

                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Text(
                        text = "상태",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )

                    CompactStatusOption(
                        label = "온라인",
                        status = UserStatus.Online,
                        currentStatus = state.accountStatus,
                        isLoading = state.isUpdatingStatus,
                        onSelect = {
                            isDndSelectorOpen = false
                            onStatusChange(UserStatus.Online, null)
                        },
                    )

                    DndStatusOption(
                        currentStatus = state.accountStatus,
                        isLoading = state.isUpdatingStatus,
                        isSelectorOpen = isDndSelectorOpen,
                        onToggleSelector = {
                            if (!state.isUpdatingStatus) {
                                isDndSelectorOpen = !isDndSelectorOpen
                            }
                        },
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onSignOut)
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = "로그아웃",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    SettingsIconButton(onClick = onSettingsClick)
                }
            }
        }

        if (isDndSelectorOpen) {
            DndExpiryFlyout(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 280.dp, bottom = 48.dp),
                isLoading = state.isUpdatingStatus,
                onSelect = { hours ->
                    onStatusChange(UserStatus.DoNotDisturb, hours)
                    isDndSelectorOpen = false
                },
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    imageUrl: String?,
    fallback: String,
    size: Dp,
    status: UserStatus?,
    ringColor: Color,
    modifier: Modifier = Modifier,
    isUploading: Boolean = false,
    onEditClick: (() -> Unit)? = null,
) {
    val httpClient = koinInject<HttpClient>()
    val image = rememberRemoteImageBitmap(imageUrl, httpClient)
    val dotOuterSize = if (size >= 60.dp) 18.dp else 12.dp
    val dotInnerSize = if (size >= 60.dp) 12.dp else 8.dp

    val editInteraction = remember { MutableInteractionSource() }
    val isHovered by editInteraction.collectIsHoveredAsState()

    Box(modifier = modifier.size(size)) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (image != null) {
                    Image(
                        bitmap = image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = fallback,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = if (size >= 60.dp) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.labelLarge
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // 편집 오버레이 (모달 아바타 전용)
        if (onEditClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .hoverable(editInteraction)
                    .then(
                        if (!isUploading) Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onEditClick,
                        ) else Modifier
                    )
                    .background(
                        when {
                            isUploading -> Color.Black.copy(alpha = 0.45f)
                            isHovered -> Color.Black.copy(alpha = 0.38f)
                            else -> Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size * 0.36f),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else if (isHovered) {
                    Text(
                        text = "변경",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (status != null) {
            Box(
                modifier = Modifier
                    .size(dotOuterSize)
                    .clip(CircleShape)
                    .background(ringColor)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center,
            ) {
                StatusGlyph(status = status, size = dotInnerSize)
            }
        }
    }
}

@Composable
private fun rememberRemoteImageBitmap(imageUrl: String?, httpClient: HttpClient): ImageBitmap? {
    val imageState = produceState<ImageBitmap?>(initialValue = null, key1 = imageUrl) {
        value = null
        val url = imageUrl?.takeIf { it.isNotBlank() } ?: return@produceState
        value = runCatching {
            decodeImageBitmap(httpClient.get(url).readRawBytes())
        }.getOrNull()
    }
    return imageState.value
}

@Composable
private fun SettingsIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else Color.Transparent,
            )
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        SettingsIcon(
            color = if (isHovered) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun SettingsIcon(
    color: Color,
    modifier: Modifier = Modifier.size(15.dp),
) {
    Icon(
        imageVector = Icons.Rounded.Settings,
        contentDescription = null,
        modifier = modifier,
        tint = color,
    )
}

@Composable
private fun CompactStatusOption(
    label: String,
    status: UserStatus,
    currentStatus: UserStatus,
    isLoading: Boolean,
    onSelect: () -> Unit,
) {
    val isSelected = currentStatus == status
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .then(if (!isLoading) Modifier.clickable(onClick = onSelect) else Modifier)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        StatusGlyph(status = status)
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun DndStatusOption(
    currentStatus: UserStatus,
    isLoading: Boolean,
    isSelectorOpen: Boolean,
    onToggleSelector: () -> Unit,
) {
    val isSelected = currentStatus == UserStatus.DoNotDisturb

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .then(if (!isLoading) Modifier.clickable(onClick = onToggleSelector) else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        StatusGlyph(status = UserStatus.DoNotDisturb)
        Text(
            text = "방해금지",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        ChevronRight(
            color = if (isSelectorOpen) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun DndExpiryFlyout(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onSelect: (Double?) -> Unit,
) {
    Surface(
        modifier = modifier.width(142.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(
                text = "방해금지 시간",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )

            DndOptions.forEach { (labelText, hours) ->
                DndExpiryOption(
                    label = labelText,
                    enabled = !isLoading,
                    onClick = { onSelect(hours) },
                )
            }
        }
    }
}

@Composable
private fun DndExpiryOption(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val optionBackground = when {
        !enabled -> Color.Transparent
        isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(optionBackground)
            .then(
                if (enabled) {
                    Modifier
                        .hoverable(interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick,
                        )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusGlyph(
    status: UserStatus,
    size: Dp = 12.dp,
) {
    val color = status.dotColor()

    Canvas(modifier = Modifier.size(size)) {
        when (status) {
            UserStatus.Online -> drawCircle(color = color, radius = this.size.minDimension / 2f)
            UserStatus.DoNotDisturb -> {
                drawCircle(color = color, radius = this.size.minDimension / 2f)
                drawLine(
                    color = Color.White,
                    start = Offset(this.size.width * 0.28f, this.size.height * 0.5f),
                    end = Offset(this.size.width * 0.72f, this.size.height * 0.5f),
                    strokeWidth = (1.8f * (size / 12.dp)).dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun ChevronRight(color: Color) {
    Icon(
        imageVector = Icons.Rounded.ChevronRight,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = color,
    )
}

@Composable
private fun ChannelRow(
    channel: Channel,
    isSelected: Boolean,
    canManage: Boolean,
    onClick: () -> Unit,
    onManage: () -> Unit,
    unreadCount: Int,
) {
    var contextMenuVisible by remember { mutableStateOf(false) }
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
                .clickable(onClick = onClick)
                .pointerInput(channel.id) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                contextMenuVisible = true
                            }
                        }
                    }
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = channel.type.icon(),
                contentDescription = channel.type.label(),
                modifier = Modifier.size(17.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = channel.name,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (unreadCount > 0) {
                Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                    Text(
                        if (unreadCount > 99) "99+" else unreadCount.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
        DropdownMenu(
            expanded = contextMenuVisible,
            onDismissRequest = { contextMenuVisible = false },
        ) {
            Text(
                text = channel.name,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = { Text("채널 이름 복사", style = MaterialTheme.typography.bodySmall) },
                onClick = {
                    @Suppress("DEPRECATION")
                    clipboardManager.setText(AnnotatedString(channel.name))
                    contextMenuVisible = false
                },
                leadingIcon = { Icon(Icons.Rounded.ContentCopy, null, Modifier.size(16.dp)) },
            )
            if (canManage) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = { Text("채널 편집", style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        contextMenuVisible = false
                        onManage()
                    },
                    leadingIcon = { Icon(Icons.Rounded.Edit, null, Modifier.size(16.dp)) },
                )
                DropdownMenuItem(
                    text = { Text("멤버 및 알림", style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        contextMenuVisible = false
                        onManage()
                    },
                    leadingIcon = { Icon(Icons.Rounded.Person, null, Modifier.size(16.dp)) },
                )
            }
        }
    }
}

@Composable
private fun ProjectRow(
    project: Project,
    isSelected: Boolean,
    teamRole: TeamRole,
    onClick: () -> Unit,
) {
    var contextMenuVisible by remember { mutableStateOf(false) }
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
                .clickable(onClick = onClick)
                .pointerInput(project.id) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                contextMenuVisible = true
                            }
                        }
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = project.name,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (project.status == ProjectStatus.Archived) {
                Text(
                    text = "보관됨",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        DropdownMenu(
            expanded = contextMenuVisible,
            onDismissRequest = { contextMenuVisible = false },
        ) {
            Text(
                text = project.name,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = { Text("프로젝트 이름 복사", style = MaterialTheme.typography.bodySmall) },
                onClick = {
                    @Suppress("DEPRECATION")
                    clipboardManager.setText(AnnotatedString(project.name))
                    contextMenuVisible = false
                },
                leadingIcon = { Icon(Icons.Rounded.ContentCopy, null, Modifier.size(16.dp)) },
            )
            if (teamRole.isAtLeastAdmin()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = { Text("프로젝트 관리 열기", style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        contextMenuVisible = false
                        onClick()
                    },
                    leadingIcon = { Icon(Icons.Rounded.Edit, null, Modifier.size(16.dp)) },
                )
            }
        }
    }
}

@Composable
private fun ThreadSidebarRow(thread: Thread, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    isHovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    else -> Color.Transparent
                }
            )
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.Article,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = thread.name,
            modifier = Modifier.weight(1f),
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ThreadListDialog(state: MainStore.State, component: MainComponent) {
    val threadRepository = koinInject<ThreadRepository>()
    val scope = rememberCoroutineScope()
    var showArchived by remember { mutableStateOf(false) }
    var allThreads by remember(state.selectedChannelId) { mutableStateOf(state.threads) }
    var archiveLoadError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state.threads, showArchived) {
        if (!showArchived) {
            allThreads = state.threads
        } else {
            val channelId = state.selectedChannelId ?: return@LaunchedEffect
            runCatching { threadRepository.getThreads(channelId, includeArchived = true) }
                .onSuccess { allThreads = it }
                .onFailure { archiveLoadError = it.userMessage() }
        }
    }
    val displayedThreads = if (showArchived) allThreads else allThreads.filter { !it.isArchived }

    Dialog(onDismissRequest = component::onCloseThreadList) {
        Surface(
            modifier = Modifier.width(400.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "스레드",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "보관됨 포함",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        androidx.compose.material3.Switch(
                            checked = showArchived,
                            onCheckedChange = { showArchived = it },
                            modifier = Modifier.height(24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                archiveLoadError?.let { ManagementError(it) }

                when {
                    state.isLoadingThreads -> Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp) }
                    displayedThreads.isEmpty() -> Text(
                        text = if (showArchived) "스레드가 없습니다." else "활성 스레드가 없습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    else -> LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(displayedThreads, key = { it.id }) { thread ->
                            ThreadListItem(thread = thread, onClick = { component.onThreadClick(thread.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreadListItem(thread: Thread, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isHovered) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                else Color.Transparent
            )
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.Article,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (thread.isArchived) MaterialTheme.colorScheme.onSurfaceVariant
                   else MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = thread.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (thread.isArchived) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (thread.isArchived) {
            Text(
                text = "보관됨",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ThreadDetailDialog(
    thread: Thread,
    state: MainStore.State,
    component: MainComponent,
) {
    val chatRepository = koinInject<ChatRepository>()
    val threadRepository = koinInject<ThreadRepository>()
    val scope = rememberCoroutineScope()
    var replyDraft by remember(thread.id) { mutableStateOf("") }
    var actionError by remember(thread.id) { mutableStateOf<String?>(null) }
    var threadMessages by remember(thread.id) { mutableStateOf<List<com.cowork.desktop.client.domain.model.ChatMessage>>(emptyList()) }
    var isLoadingReplies by remember(thread.id) { mutableStateOf(true) }
    val canManageThread = thread.createdBy == state.accountId ||
        state.selectedChannel?.createdBy == state.accountId ||
        state.selectedTeam?.myRole?.isAtLeastAdmin() == true

    LaunchedEffect(thread.id) {
        isLoadingReplies = true
        actionError = null
        while (true) {
            runCatching {
                loadThreadReplies(chatRepository, thread.channelId, thread.parentMessageId)
            }.onSuccess {
                threadMessages = it
                actionError = null
            }.onFailure { actionError = it.userMessage() }
            isLoadingReplies = false
            delay(5_000)
        }
    }

    Dialog(onDismissRequest = component::onCloseThread) {
        Surface(
            modifier = Modifier.width(520.dp).heightIn(max = 640.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Article,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = thread.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (canManageThread) {
                        TextButton(onClick = {
                            scope.launch {
                                runCatching {
                                    threadRepository.updateThread(thread.channelId, thread.id, isArchived = !thread.isArchived)
                                }.onSuccess {
                                    component.onChannelClick(thread.channelId)
                                    component.onCloseThread()
                                }.onFailure { actionError = it.userMessage() }
                            }
                        }) { Text(if (thread.isArchived) "보관 해제" else "보관") }
                    }
                    IconButton(onClick = component::onCloseThread, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "닫기",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                HorizontalDivider()

                // 메시지 목록
                if (isLoadingReplies) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp) }
                } else if (threadMessages.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "이 스레드에 아직 메시지가 없습니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        reverseLayout = true,
                    ) {
                        itemsIndexed(threadMessages, key = { _, m -> m.id }) { index, message ->
                            val showHeader = index == threadMessages.lastIndex ||
                                threadMessages[index + 1].authorId != message.authorId
                            val profile = state.memberProfiles[message.authorId]
                            MessageRow(
                                message = message,
                                showHeader = showHeader,
                                displayName = profile?.nickname ?: profile?.name ?: "사용자 ${message.authorId}",
                                avatarUrl = profile?.profileImageUrl,
                                state = state,
                                component = component,
                            )
                        }
                    }
                }
                actionError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) }
                OutlinedTextField(
                    value = replyDraft,
                    onValueChange = { replyDraft = it },
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    placeholder = { Text("스레드에 답글…") },
                    enabled = !thread.isArchived,
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                val content = replyDraft.trim()
                                if (content.isEmpty()) return@TextButton
                                scope.launch {
                                    val optimisticId = "optimistic-thread-${System.currentTimeMillis()}"
                                    val optimistic = com.cowork.desktop.client.domain.model.ChatMessage(
                                        id = optimisticId,
                                        teamId = state.selectedTeamId,
                                        projectId = state.selectedProjectId,
                                        channelId = thread.channelId,
                                        authorId = state.accountId ?: -1,
                                        content = content,
                                        parentMessageId = thread.parentMessageId,
                                        type = com.cowork.desktop.client.domain.model.MessageType.Text,
                                        fileUrl = null,
                                        fileName = null,
                                        fileSize = null,
                                        createdAt = null,
                                        clientMessageId = optimisticId,
                                    )
                                    val result = runCatching {
                                        chatRepository.sendMessage(
                                            channelId = thread.channelId,
                                            teamId = state.selectedTeamId,
                                            projectId = state.selectedProjectId,
                                            content = content,
                                            parentMessageId = thread.parentMessageId,
                                            clientMessageId = optimisticId,
                                        )
                                    }
                                    if (result.isFailure) {
                                        actionError = result.exceptionOrNull()?.userMessage()
                                        return@launch
                                    }
                                    replyDraft = ""
                                    threadMessages = listOf(optimistic) + threadMessages
                                    for (waitMillis in listOf(500L, 1_000L, 2_000L)) {
                                        delay(waitMillis)
                                        val loaded = runCatching {
                                            loadThreadReplies(chatRepository, thread.channelId, thread.parentMessageId)
                                        }.getOrNull() ?: continue
                                        val persisted = loaded.any {
                                            it.clientMessageId == optimisticId ||
                                                (it.authorId == optimistic.authorId && it.content == content)
                                        }
                                        threadMessages = if (persisted) loaded else {
                                            (listOf(optimistic) + loaded).distinctBy { it.id }
                                        }
                                        if (persisted) break
                                    }
                                }
                            },
                            enabled = replyDraft.isNotBlank(),
                        ) { Text("전송") }
                    },
                )
            }
        }
    }
}

private suspend fun loadThreadReplies(
    chatRepository: ChatRepository,
    channelId: Long,
    parentMessageId: String,
): List<com.cowork.desktop.client.domain.model.ChatMessage> {
    val messages = mutableListOf<com.cowork.desktop.client.domain.model.ChatMessage>()
    var before: String? = null
    repeat(10) {
        val page = chatRepository.getMessages(
            channelId = channelId,
            before = before,
            limit = 100,
            parentMessageId = parentMessageId,
        )
        messages += page
        if (page.size < 100) return messages.distinctBy { it.id }
        before = page.lastOrNull()?.id ?: return messages.distinctBy { it.id }
    }
    return messages.distinctBy { it.id }
}

@Composable
private fun WebhookRow(webhook: Webhook, canManage: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Link,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = webhook.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (webhook.isSecure) {
                    Text(
                        text = "보안 토큰 사용",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                webhook.token?.let { token ->
                    Text(
                        text = token,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (canManage) {
                TextButton(onClick = onEdit) { Text("편집") }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "삭제",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddWebhookDialog(state: MainStore.State, component: MainComponent) {
    Dialog(onDismissRequest = component::onAddWebhookDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.widthIn(min = 360.dp, max = 480.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("웹훅 추가", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = state.addWebhookName,
                    onValueChange = component::onAddWebhookNameChange,
                    label = { Text("웹훅 이름") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("보안 토큰 사용", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "활성화 시 서명 검증용 토큰이 발급됩니다",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.addWebhookIsSecure,
                        onCheckedChange = component::onAddWebhookSecureChange,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = component::onAddWebhookDismiss) { Text("취소") }
                    Button(
                        onClick = component::onAddWebhookSubmit,
                        enabled = state.canSubmitWebhook,
                    ) {
                        if (state.isAddingWebhook) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("추가")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditWebhookDialog(
    webhook: Webhook,
    onDismiss: () -> Unit,
    onSave: (String, String?, Boolean) -> Unit,
) {
    var name by remember(webhook.id) { mutableStateOf(webhook.name) }
    var avatarUrl by remember(webhook.id) { mutableStateOf(webhook.avatarUrl.orEmpty()) }
    var isSecure by remember(webhook.id) { mutableStateOf(webhook.isSecure) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(Modifier.width(440.dp).padding(22.dp)) {
                Text("웹훅 편집", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(name, { name = it }, label = { Text("이름") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(avatarUrl, { avatarUrl = it }, label = { Text("아바타 URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("보안 토큰 사용", modifier = Modifier.weight(1f))
                    Switch(isSecure, { isSecure = it })
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("취소") }
                    Button(onClick = { onSave(name.trim(), avatarUrl.trim().ifBlank { null }, isSecure) }, enabled = name.isNotBlank()) { Text("저장") }
                }
            }
        }
    }
}

@Composable
private fun <T> DraggableItemList(
    items: List<T>,
    key: (T) -> Any,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    itemContent: @Composable (item: T, isDragging: Boolean) -> Unit,
) {
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val itemHeightPx = remember { mutableStateOf(0f) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEachIndexed { index, item ->
            val isDragging = draggingIndex == index
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isDragging) Modifier.absoluteOffset(y = with(LocalDensity.current) { dragOffsetY.toDp() }) else Modifier)
                    .pointerInput(key(item)) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingIndex = index
                                dragOffsetY = 0f
                            },
                            onDrag = { _, dragAmount ->
                                dragOffsetY += dragAmount.y
                                val itemH = itemHeightPx.value.takeIf { it > 0 } ?: 48f
                                val targetIndex = (index + (dragOffsetY / itemH).toInt())
                                    .coerceIn(0, items.lastIndex)
                                if (targetIndex != index) {
                                    onReorder(index, targetIndex)
                                    dragOffsetY -= (targetIndex - index) * itemH
                                    draggingIndex = targetIndex
                                }
                            },
                            onDragEnd = { draggingIndex = null; dragOffsetY = 0f },
                            onDragCancel = { draggingIndex = null; dragOffsetY = 0f },
                        )
                    }
                    .then(if (isDragging) Modifier.background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.shapes.small,
                    ) else Modifier),
            ) {
                Layout(content = { itemContent(item, isDragging) }) { measurables, constraints ->
                    val placeable = measurables.first().measure(constraints)
                    itemHeightPx.value = placeable.height.toFloat()
                    layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
                }
            }
        }
    }
}

@Composable
private fun WorkspacePane(state: MainStore.State, component: MainComponent) {
    val selectedChannel = state.selectedChannel
    val selectedProject = state.selectedProject

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
    ) {
        when {
            selectedChannel != null -> ChannelWorkspace(state = state, channel = selectedChannel, component = component)
            selectedProject != null -> ProjectWorkspaceContent(
                project = selectedProject,
                teamCanManage = state.selectedTeam?.myRole?.isAtLeastAdmin() == true,
                currentUserId = state.accountId,
                onChanged = component::onReloadClick,
            )
            else -> EmptyWorkspace(state = state)
        }
    }
}

@Composable
private fun EmptyWorkspace(state: MainStore.State) {
    Text(
        text = state.selectedTeam?.name ?: "팀을 선택하세요",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "채널 또는 프로젝트를 선택하세요.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ColumnScope.ChannelWorkspace(state: MainStore.State, channel: Channel, component: MainComponent) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = channel.type.icon(),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = channel.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (channel.isPrivate) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "비공개",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    channel.description?.takeIf { it.isNotBlank() }?.let { description ->
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = description,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    when (channel.type) {
        ChannelType.Text -> TextChannelContent(state = state, component = component)
        ChannelType.Webhook -> WebhookChannelContent(state = state, component = component)
        ChannelType.Voice -> VoiceChannelContent(channel)
        ChannelType.MeetingNote -> MeetingNoteChannelContent(state = state, component = component)
        ChannelType.AccountShare -> AccountShareChannelContent(
            channel = channel,
            accountId = state.accountId,
            canManageChannel = state.selectedTeam?.myRole?.isAtLeastAdmin() == true || channel.createdBy == state.accountId,
        )
        ChannelType.FileShare -> FileShareChannelContent(
            channel = channel,
            accountId = state.accountId,
            isSystemAdmin = state.isSystemAdmin,
        )
        ChannelType.Unknown -> ComingSoonContent(icon = Icons.AutoMirrored.Rounded.HelpOutline, message = "알 수 없는 채널 타입입니다.")
    }
}

@Composable
private fun ColumnScope.TextChannelContent(state: MainStore.State, component: MainComponent) {
    val chatRepository = koinInject<ChatRepository>()
    val scope = rememberCoroutineScope()
    var pinnedMessages by remember(state.selectedChannelId) { mutableStateOf<List<com.cowork.desktop.client.domain.model.ChatMessage>>(emptyList()) }
    var isPinnedDialogOpen by remember(state.selectedChannelId) { mutableStateOf(false) }

    LaunchedEffect(state.selectedChannelId, state.messages.firstOrNull()?.id) {
        val channelId = state.selectedChannelId ?: return@LaunchedEffect
        val lastMessageId = state.messages.firstOrNull()?.id ?: return@LaunchedEffect
        runCatching { chatRepository.markChannelRead(channelId, lastMessageId) }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "메시지",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row {
            TextButton(onClick = {
                val channelId = state.selectedChannelId ?: return@TextButton
                scope.launch {
                    pinnedMessages = runCatching { chatRepository.getPinnedMessages(channelId) }.getOrDefault(emptyList())
                    isPinnedDialogOpen = true
                }
            }) { Text("고정됨") }
            IconButton(onClick = component::onOpenThreadList) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Article,
                    contentDescription = "스레드 목록",
                    modifier = Modifier.size(20.dp),
                    tint = if (state.threads.any { !it.isArchived })
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    if (state.typingUserIds.isNotEmpty()) {
        Text(
            state.typingUserIds.joinToString { state.memberProfiles[it]?.nickname ?: state.memberProfiles[it]?.name ?: "사용자 $it" } + " 입력 중…",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
    }

    when {
        state.isLoadingMessages -> CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
        state.messages.isEmpty() -> EmptyPaneText("메시지가 없습니다.")
        else -> LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true,
        ) {
            itemsIndexed(state.messages, key = { _, m -> m.id }) { index, message ->
                val showHeader = index == state.messages.lastIndex ||
                    state.messages[index + 1].authorId != message.authorId
                val profile = state.memberProfiles[message.authorId]
                MessageRow(
                    message = message,
                    showHeader = showHeader,
                    displayName = profile?.nickname ?: profile?.name ?: "사용자 ${message.authorId}",
                    avatarUrl = profile?.profileImageUrl,
                    state = state,
                    component = component,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = state.chatDraft,
        onValueChange = component::onChatDraftChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("메시지 입력...") },
        singleLine = true,
        trailingIcon = {
            IconButton(
                onClick = component::onSendChatMessage,
                enabled = state.chatDraft.isNotBlank(),
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "전송")
            }
        },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            imeAction = androidx.compose.ui.text.input.ImeAction.Send,
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onSend = { component.onSendChatMessage() },
        ),
    )


    if (state.isThreadListOpen) {
        ThreadListDialog(state = state, component = component)
    }

    val selectedThread = state.threads.firstOrNull { it.id == state.selectedThreadId }
    if (selectedThread != null) {
        ThreadDetailDialog(
            thread = selectedThread,
            state = state,
            component = component,
        )
    }

    if (isPinnedDialogOpen) {
        ManagementDialogShell(
            title = "고정된 메시지",
            onDismiss = { isPinnedDialogOpen = false },
            sidebar = { Text("Pins", Modifier.padding(10.dp), fontWeight = FontWeight.Bold) },
        ) {
            if (pinnedMessages.isEmpty()) {
                Text("고정된 메시지가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            pinnedMessages.forEach { message ->
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f), shape = RoundedCornerShape(10.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(message.content)
                        Text("사용자 ${message.authorId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ColumnScope.WebhookChannelContent(state: MainStore.State, component: MainComponent) {
    val webhookRepository = koinInject<WebhookRepository>()
    val scope = rememberCoroutineScope()
    var editingWebhook by remember(state.selectedChannelId) { mutableStateOf<Webhook?>(null) }
    var editError by remember { mutableStateOf<String?>(null) }
    val canManage = state.selectedTeam?.myRole?.isAtLeastAdmin() == true || state.selectedChannel?.createdBy == state.accountId
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "웹훅",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (canManage) {
            TextButton(onClick = component::onAddWebhookClick) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("웹훅 추가")
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    editError?.let { ManagementError(it) }

    when {
        state.isLoadingWebhooks -> CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
        state.webhooks.isEmpty() -> EmptyPaneText("등록된 웹훅이 없습니다. 웹훅을 추가하면 외부 서비스에서 메시지를 전송할 수 있습니다.")
        else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.webhooks.forEach { webhook ->
                WebhookRow(
                    webhook = webhook,
                    canManage = canManage,
                    onEdit = { editingWebhook = webhook },
                    onDelete = { component.onDeleteWebhook(webhook.id) },
                )
            }
        }
    }

    if (state.isAddWebhookOpen && canManage) {
        AddWebhookDialog(state = state, component = component)
    }
    editingWebhook?.takeIf { canManage }?.let { webhook ->
        EditWebhookDialog(
            webhook = webhook,
            onDismiss = { editingWebhook = null },
            onSave = { name, avatarUrl, isSecure ->
                val channelId = state.selectedChannelId ?: return@EditWebhookDialog
                scope.launch {
                    runCatching { webhookRepository.updateWebhook(channelId, webhook.id, name, avatarUrl, isSecure) }
                        .onSuccess {
                            editingWebhook = null
                            component.onChannelClick(channelId)
                        }
                        .onFailure { editError = it.userMessage() }
                }
            },
        )
    }
}

@Composable
private fun ColumnScope.ComingSoonContent(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ColumnScope.MeetingNoteChannelContent(state: MainStore.State, component: MainComponent) {
    var isTemplateManagerOpen by remember(state.selectedChannelId) { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "회의록",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row {
            TextButton(onClick = { isTemplateManagerOpen = true }) { Text("템플릿 관리") }
        if (state.activeTemplate != null) {
            TextButton(onClick = component::onCreateMeetingNoteClick) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("새 회의록")
            }
        }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))

    when {
        state.isLoadingMeetingNotes -> CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
        state.activeTemplate == null -> EmptyPaneText("활성 템플릿이 없습니다. 템플릿을 먼저 생성해주세요.")
        state.meetingNotes.isEmpty() -> EmptyPaneText("작성된 회의록이 없습니다.")
        else -> LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.meetingNotes, key = { it.id }) { note ->
                MeetingNoteRow(note = note, onClick = { component.onMeetingNoteClick(note.id) })
            }
        }
    }

    if (state.isCreateMeetingNoteOpen && state.activeTemplate != null) {
        CreateMeetingNoteDialog(state = state, component = component)
    }

    val selectedNote = state.selectedMeetingNote
    if (selectedNote != null) {
        MeetingNoteDetailDialog(
            note = selectedNote,
            canEdit = selectedNote.createdBy == state.accountId,
            onDismiss = component::onMeetingNoteDetailDismiss,
            onChanged = {
                component.onMeetingNoteDetailDismiss()
                component.onChannelClick(selectedNote.channelId)
            },
        )
    }
    if (isTemplateManagerOpen) {
        val channelId = state.selectedChannelId
        if (channelId != null) {
            MeetingTemplateManagerDialog(
                channelId = channelId,
                onDismiss = { isTemplateManagerOpen = false },
                onChanged = { component.onChannelClick(channelId) },
            )
        }
    }
}

@Composable
private fun MeetingNoteRow(note: com.cowork.desktop.client.domain.model.MeetingNote, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = note.createdAt.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CreateMeetingNoteDialog(state: MainStore.State, component: MainComponent) {
    val template = state.activeTemplate ?: return
    androidx.compose.ui.window.Dialog(onDismissRequest = component::onCreateMeetingNoteDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text("새 회의록", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.createNoteTitle,
                    onValueChange = component::onCreateNoteTitleChange,
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                template.sections.forEach { section ->
                    Spacer(modifier = Modifier.height(12.dp))
                    val sectionContent = state.createNoteSectionContents[section.title] ?: ""
                    OutlinedTextField(
                        value = sectionContent,
                        onValueChange = { component.onCreateNoteSectionContentChange(section.title, it) },
                        label = {
                            Text(if (section.isRequired) "${section.title} *" else section.title)
                        },
                        placeholder = section.placeholder?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = component::onCreateMeetingNoteDismiss) { Text("취소") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = component::onCreateMeetingNoteSubmit,
                        enabled = state.canSubmitNote,
                    ) {
                        if (state.isCreatingNote) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("저장")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeetingNoteDetailDialog(
    note: com.cowork.desktop.client.domain.model.MeetingNote,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onChanged: () -> Unit,
) {
    val repository = koinInject<MeetingNoteRepository>()
    val scope = rememberCoroutineScope()
    var isEditing by remember(note.id) { mutableStateOf(false) }
    var title by remember(note.id) { mutableStateOf(note.title) }
    var content by remember(note.id) { mutableStateOf(note.content) }
    var error by remember(note.id) { mutableStateOf<String?>(null) }
    var confirmDelete by remember(note.id) { mutableStateOf(false) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(note.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Row {
                        if (canEdit) {
                            TextButton(onClick = { isEditing = !isEditing }) { Text(if (isEditing) "보기" else "편집") }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "닫기")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(note.createdAt.take(10), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                val contentText = runCatching {
                    val json = kotlinx.serialization.json.Json.parseToJsonElement(note.content)
                    if (json is kotlinx.serialization.json.JsonObject) {
                        json.entries.joinToString("\n\n") { (k, v) ->
                            val content = (v as? kotlinx.serialization.json.JsonPrimitive)?.content ?: v.toString()
                            "**$k**\n$content"
                        }
                    } else note.content
                }.getOrDefault(note.content)

                if (isEditing) {
                    OutlinedTextField(title, { title = it }, label = { Text("제목") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(content, { content = it }, label = { Text("내용 (JSON)") }, modifier = Modifier.fillMaxWidth(), minLines = 8)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (confirmDelete) {
                            Button(onClick = {
                                scope.launch {
                                    runCatching { repository.deleteNote(note.channelId, note.id) }
                                        .onSuccess { onChanged() }
                                        .onFailure { error = it.userMessage() }
                                }
                            }) { Text("삭제 확정") }
                            TextButton(onClick = { confirmDelete = false }) { Text("취소") }
                        } else {
                            TextButton(onClick = { confirmDelete = true }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            scope.launch {
                                runCatching { repository.updateNote(note.channelId, note.id, title.trim(), content) }
                                    .onSuccess { onChanged() }
                                    .onFailure { error = it.userMessage() }
                            }
                        }, enabled = title.isNotBlank()) { Text("저장") }
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                } else {
                    Text(contentText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun MessageRow(
    message: com.cowork.desktop.client.domain.model.ChatMessage,
    showHeader: Boolean,
    displayName: String,
    avatarUrl: String?,
    state: MainStore.State,
    component: MainComponent,
) {
    val isOptimistic = message.id.startsWith("optimistic-")
    val isEditing = state.editingMessageId == message.id
    val canEdit = !isOptimistic && state.canEditMessage(message.authorId)
    val canDelete = !isOptimistic && state.canDeleteMessage(message.authorId)
    val canPin = !isOptimistic && (message.authorId == state.accountId || state.isSystemAdmin)
    val httpClient = koinInject<HttpClient>()
    val chatRepository = koinInject<ChatRepository>()
    val threadRepository = koinInject<ThreadRepository>()
    val scope = rememberCoroutineScope()
    val avatarImage = rememberRemoteImageBitmap(avatarUrl, httpClient)
    val avatarSize = 36.dp
    var contextMenuVisible by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val hoverInteraction = remember { MutableInteractionSource() }
    val isHovered by hoverInteraction.collectIsHoveredAsState()
    var actionError by remember(message.id) { mutableStateOf<String?>(null) }
    var isCreateThreadOpen by remember(message.id) { mutableStateOf(false) }
    var threadName by remember(message.id) { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(hoverInteraction)
            .background(
                when {
                    isEditing -> MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                    isHovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f)
                    else -> Color.Transparent
                }
            )
            .pointerInput(message.id) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press) {
                            val btn = event.buttons
                            if (btn.isSecondaryPressed) {
                                val position = event.changes.firstOrNull()?.position
                                if (position != null) {
                                    pressOffset = with(density) {
                                        DpOffset(position.x.toDp(), position.y.toDp())
                                    }
                                }
                                contextMenuVisible = true
                            }
                        }
                    }
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isOptimistic) 0.55f else 1f)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = if (showHeader) 12.dp else 2.dp,
                    bottom = 0.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.width(avatarSize)) {
                if (showHeader) {
                    Surface(
                        modifier = Modifier.size(avatarSize).clip(CircleShape),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (avatarImage != null) {
                                Image(
                                    bitmap = avatarImage,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Text(
                                    text = displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                if (showHeader) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                if (isEditing) {
                    OutlinedTextField(
                        value = state.editingMessageContent,
                        onValueChange = component::onChangeEditMessageContent,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        singleLine = false,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Default,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = component::onSubmitEditMessage,
                            enabled = state.editingMessageContent.isNotBlank(),
                        ) { Text("저장", style = MaterialTheme.typography.labelSmall) }
                        TextButton(onClick = component::onCancelEditMessage) {
                            Text("취소", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else {
                    if (message.isPinned || message.isEdited) {
                        Text(
                            listOfNotNull(
                                "고정됨".takeIf { message.isPinned },
                                "수정됨".takeIf { message.isEdited },
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    message.mentionedMessage?.let { quoted ->
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f), shape = RoundedCornerShape(6.dp)) {
                            Text(
                                "사용자 ${quoted.authorId}: ${quoted.content}",
                                modifier = Modifier.fillMaxWidth().padding(7.dp),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    SelectionContainer {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    val attachments = buildList {
                        addAll(message.attachments)
                        if (message.fileUrl != null && message.fileName != null && none { it.url == message.fileUrl }) {
                            add(com.cowork.desktop.client.domain.model.ChatAttachment(
                                name = message.fileName,
                                url = message.fileUrl,
                                size = message.fileSize ?: 0,
                                mimeType = "application/octet-stream",
                            ))
                        }
                    }
                    if (attachments.isNotEmpty()) {
                        val uriHandler = LocalUriHandler.current
                        Spacer(Modifier.height(6.dp))
                        attachments.forEach { attachment ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(attachment.url) },
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    "${attachment.name} · ${attachment.size} bytes",
                                    modifier = Modifier.padding(9.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    if (message.reactions.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            message.reactions.forEach { reaction ->
                                Surface(
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            runCatching {
                                                if (reaction.myReaction) chatRepository.removeReaction(message.channelId, message.id, reaction.emoji)
                                                else chatRepository.addReaction(message.channelId, message.id, reaction.emoji)
                                            }.onSuccess { component.onChannelClick(message.channelId) }
                                                .onFailure { actionError = it.userMessage() }
                                        }
                                    },
                                    color = if (reaction.myReaction) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Text("${reaction.emoji} ${reaction.count}", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    actionError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }
                }
            }
        }

        DropdownMenu(
            expanded = contextMenuVisible,
            onDismissRequest = { contextMenuVisible = false },
            offset = pressOffset,
        ) {
            DropdownMenuItem(
                text = { Text("복사", style = MaterialTheme.typography.bodySmall) },
                onClick = {
                    @Suppress("DEPRECATION")
                    clipboardManager.setText(AnnotatedString(message.content))
                    contextMenuVisible = false
                },
                leadingIcon = { Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)) },
            )
            if (message.parentMessageId == null && !isOptimistic) {
                DropdownMenuItem(
                    text = { Text("스레드 시작", style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        contextMenuVisible = false
                        threadName = message.content.take(40)
                        isCreateThreadOpen = true
                    },
                )
            }
            if (canPin) {
                DropdownMenuItem(
                    text = { Text(if (message.isPinned) "고정 해제" else "메시지 고정", style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        contextMenuVisible = false
                        scope.launch {
                            runCatching {
                                if (message.isPinned) chatRepository.unpinMessage(message.channelId, message.id)
                                else chatRepository.pinMessage(message.channelId, message.id)
                            }.onSuccess { component.onChannelClick(message.channelId) }
                                .onFailure { actionError = it.userMessage() }
                        }
                    },
                )
            }
            listOf("👍", "❤️", "🎉").forEach { emoji ->
                DropdownMenuItem(
                    text = { Text("$emoji 반응", style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        contextMenuVisible = false
                        scope.launch {
                            runCatching { chatRepository.addReaction(message.channelId, message.id, emoji) }
                                .onSuccess { component.onChannelClick(message.channelId) }
                                .onFailure { actionError = it.userMessage() }
                        }
                    },
                )
            }
            if (canEdit) {
                DropdownMenuItem(
                    text = { Text("수정", style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        contextMenuVisible = false
                        component.onStartEditMessage(message.id)
                    },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                )
            }
            if (canDelete) {
                DropdownMenuItem(
                    text = { Text("삭제", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        contextMenuVisible = false
                        component.onDeleteMessage(message.id)
                    },
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error) },
                )
            }
        }

        if (isCreateThreadOpen) {
            Dialog(onDismissRequest = { isCreateThreadOpen = false }) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
                    Column(Modifier.width(420.dp).padding(22.dp)) {
                        Text("스레드 시작", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(threadName, { threadName = it }, label = { Text("스레드 이름") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isCreateThreadOpen = false }) { Text("취소") }
                            Button(
                                onClick = {
                                    scope.launch {
                                        runCatching { threadRepository.createThread(message.channelId, threadName.trim(), message.id) }
                                            .onSuccess {
                                                isCreateThreadOpen = false
                                                component.onChannelClick(message.channelId)
                                            }
                                            .onFailure { actionError = it.userMessage() }
                                    }
                                },
                                enabled = threadName.isNotBlank(),
                            ) { Text("만들기") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPaneText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 16.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private enum class SettingsCategory(val label: String, val icon: ImageVector) {
    Appearance("외관", Icons.Rounded.Tune),
    Account("내 계정", Icons.Rounded.Person),
}

@Composable
private fun SettingsDialog(
    state: MainStore.State,
    onDismiss: () -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onTimeFormatChange: (TimeFormat) -> Unit,
    onDateFormatChange: (DateFormat) -> Unit,
    onMarketingEmailChange: (Boolean) -> Unit,
    onReload: () -> Unit,
) {
    var selectedCategory by remember { mutableStateOf(SettingsCategory.Appearance) }

    CoworkDialog(onDismissRequest = onDismiss) {
        Row(modifier = Modifier.width(700.dp).height(560.dp)) {
            // 왼쪽 카테고리 네비게이션
            Column(
                modifier = Modifier
                    .width(168.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(vertical = 12.dp, horizontal = 8.dp),
            ) {
                Text(
                    text = state.accountDisplayName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                SettingsCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            VerticalDivider()

            // 오른쪽 콘텐츠 패널
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selectedCategory.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "닫기",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                HorizontalDivider()
                when (selectedCategory) {
                    SettingsCategory.Appearance -> AppearanceSettingsPanel(
                        state = state,
                        onThemeChange = onThemeChange,
                        onLanguageChange = onLanguageChange,
                        onTimeFormatChange = onTimeFormatChange,
                        onDateFormatChange = onDateFormatChange,
                    )
                    SettingsCategory.Account -> AccountSettingsPanel(
                        state = state,
                        onMarketingEmailChange = onMarketingEmailChange,
                        onReload = onReload,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppearanceSettingsPanel(
    state: MainStore.State,
    onThemeChange: (AppTheme) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onTimeFormatChange: (TimeFormat) -> Unit,
    onDateFormatChange: (DateFormat) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).verticalScroll(rememberScrollState())) {
        SettingsGroupLabel("표시")
        SettingsRow(label = "테마") {
            SegmentedSelector(
                options = AppTheme.entries,
                selected = state.accountTheme,
                label = { it.label },
                onSelect = onThemeChange,
            )
        }
        SettingsDivider()
        SettingsRow(label = "언어") {
            SegmentedSelector(
                options = AppLanguage.entries,
                selected = state.accountLanguage,
                label = { it.label },
                onSelect = onLanguageChange,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SettingsGroupLabel("날짜 및 시간")
        SettingsRow(label = "시간 형식") {
            SegmentedSelector(
                options = TimeFormat.entries,
                selected = state.accountTimeFormat,
                label = { it.label },
                onSelect = onTimeFormatChange,
            )
        }
        SettingsDivider()
        SettingsRow(label = "날짜 형식") {
            SettingsDropdown(
                options = DateFormat.entries,
                selected = state.accountDateFormat,
                label = { it.label },
                onSelect = onDateFormatChange,
            )
        }
    }
}

@Composable
private fun AccountSettingsPanel(
    state: MainStore.State,
    onMarketingEmailChange: (Boolean) -> Unit,
    onReload: () -> Unit,
) {
    val userRepository = koinInject<UserRepository>()
    val scope = rememberCoroutineScope()
    var name by remember(state.accountId, state.accountName) { mutableStateOf(state.accountName.orEmpty()) }
    var nickname by remember(state.accountId, state.accountNickname) { mutableStateOf(state.accountNickname.orEmpty()) }
    var description by remember(state.accountId, state.accountDescription) { mutableStateOf(state.accountDescription.orEmpty()) }
    var github by remember(state.accountId, state.accountGithub) { mutableStateOf(state.accountGithub.orEmpty()) }
    var statusMessage by remember(state.accountId, state.accountStatusMessage) { mutableStateOf(state.accountStatusMessage.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).verticalScroll(rememberScrollState())) {
        SettingsGroupLabel("프로필")
        OutlinedTextField(name, { name = it }, label = { Text("이름") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(nickname, { nickname = it }, label = { Text("닉네임") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(github, { github = it }, label = { Text("GitHub ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(description, { description = it }, label = { Text("소개") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                scope.launch {
                    error = null
                    runCatching { userRepository.deleteProfileImage() }
                        .onSuccess { onReload() }
                        .onFailure { error = it.userMessage() }
                }
            }, enabled = state.accountProfileImageUrl != null) { Text("프로필 사진 삭제") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                scope.launch {
                    error = null
                    saved = false
                    runCatching {
                        userRepository.updateMyProfile(
                            UserProfileUpdate(
                                name = name.trim(),
                                nickname = nickname.trim(),
                                description = description.trim(),
                                githubId = github.trim().ifBlank { null },
                                clearGithubId = github.isBlank(),
                            )
                        )
                    }.onSuccess { saved = true; onReload() }
                        .onFailure { error = it.userMessage() }
                }
            }, enabled = name.isNotBlank()) { Text("프로필 저장") }
        }
        Spacer(Modifier.height(18.dp))
        SettingsGroupLabel("상태 메시지")
        OutlinedTextField(statusMessage, { statusMessage = it }, label = { Text("커스텀 상태") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                scope.launch {
                    runCatching { userRepository.updateMyStatus(UserStatusUpdate("ONLINE", message = null)) }
                        .onSuccess { statusMessage = ""; onReload() }
                        .onFailure { error = it.userMessage() }
                }
            }) { Text("상태 지우기") }
            Button(onClick = {
                scope.launch {
                    val status = if (state.accountStatus == UserStatus.DoNotDisturb) "DO_NOT_DISTURB" else "ONLINE"
                    runCatching { userRepository.updateMyStatus(UserStatusUpdate(status, statusMessage.trim().ifBlank { null })) }
                        .onSuccess { saved = true; onReload() }
                        .onFailure { error = it.userMessage() }
                }
            }) { Text("상태 저장") }
        }
        error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        if (saved) { Spacer(Modifier.height(8.dp)); Text("저장되었습니다.", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(22.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        SettingsGroupLabel("이메일")
        SettingsRow(label = "마케팅 이메일 수신") {
            Switch(
                checked = state.accountMarketingEmail,
                onCheckedChange = onMarketingEmailChange,
            )
        }
    }
}

@Composable
private fun SettingsGroupLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun SettingsRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        content()
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
private fun <T> SegmentedSelector(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), MaterialTheme.shapes.small),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        else Color.Transparent
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (index < options.lastIndex) {
                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                )
            }
        }
    }
}

@Composable
private fun <T> SettingsDropdown(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), MaterialTheme.shapes.small)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label(selected),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label(option),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (option == selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (option == selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun CreateTeamDialog(
    state: MainStore.State,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onIconChange: (ByteArray, String) -> Unit,
    onSubmit: () -> Unit,
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val iconBytes = state.createTeamIconBytes
    val iconBitmap = remember(iconBytes) { iconBytes?.let { decodeImageBitmap(it) } }

    CoworkDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.width(440.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CoworkDialogHeader(
                title = "새 팀 만들기",
                subtitle = "팀 아이콘과 이름을 설정하세요.",
                onDismiss = onDismiss,
            )

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 팀 아이콘 업로드
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            coroutineScope.launch {
                                val result = pickImageBytes()
                                if (result != null) onIconChange(result.first, result.second)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = "팀 아이콘",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White,
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "아이콘",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    DialogFieldLabel("팀 이름")
                    Spacer(modifier = Modifier.height(6.dp))
                    DialogTextField(
                        value = state.createTeamName,
                        onValueChange = onNameChange,
                        placeholder = "예: 백엔드팀",
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DialogFieldLabel("설명 (선택)")
                    Spacer(modifier = Modifier.height(6.dp))
                    DialogTextField(
                        value = state.createTeamDescription,
                        onValueChange = onDescriptionChange,
                        placeholder = "팀에 대한 간단한 설명",
                        minLines = 3,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    DialogSubmitButton(
                        label = "팀 만들기",
                        enabled = state.canSubmitTeam,
                        isLoading = state.isCreatingTeam,
                        onClick = onSubmit,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateChannelDialog(
    state: MainStore.State,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (ChannelType) -> Unit,
    onPrivateChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
) {
    CoworkDialog(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.width(440.dp)) {
            CoworkDialogHeader(
                title = "새 채널 만들기",
                subtitle = "채널 유형을 선택하고 이름을 지정하세요.",
                onDismiss = onDismiss,
            )

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                DialogFieldLabel("채널 유형")
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    ChannelType.Text,
                    ChannelType.Voice,
                    ChannelType.Webhook,
                    ChannelType.MeetingNote,
                    ChannelType.AccountShare,
                    ChannelType.FileShare,
                ).chunked(3).forEach { rowTypes ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowTypes.forEach { type ->
                            TypeButton(
                                type = type,
                                isSelected = type == state.createChannelType,
                                onClick = { onTypeChange(type) },
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                DialogFieldLabel("채널 이름")
                Spacer(modifier = Modifier.height(6.dp))
                DialogTextField(
                    value = state.createChannelName,
                    onValueChange = onNameChange,
                    placeholder = "예: 일반",
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))

                DialogFieldLabel("설명 (선택)")
                Spacer(modifier = Modifier.height(6.dp))
                DialogTextField(
                    value = state.createChannelDescription,
                    onValueChange = onDescriptionChange,
                    placeholder = "채널에 대한 간단한 설명",
                    minLines = 2,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        DialogFieldLabel("비공개 채널")
                        Text(
                            text = "초대된 멤버만 접근할 수 있습니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.createChannelIsPrivate,
                        onCheckedChange = onPrivateChange,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                DialogSubmitButton(
                    label = "채널 만들기",
                    enabled = state.canSubmitChannel,
                    isLoading = state.isCreatingChannel,
                    onClick = onSubmit,
                )
            }
        }
    }
}

@Composable
private fun CreateProjectDialog(
    state: MainStore.State,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    CoworkDialog(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.width(440.dp)) {
            CoworkDialogHeader(
                title = "새 프로젝트 만들기",
                subtitle = "프로젝트 이름과 설명을 입력하세요.",
                onDismiss = onDismiss,
            )

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                DialogFieldLabel("프로젝트 이름")
                Spacer(modifier = Modifier.height(6.dp))
                DialogTextField(
                    value = state.createProjectName,
                    onValueChange = onNameChange,
                    placeholder = "예: 코워크 앱 개발",
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))

                DialogFieldLabel("설명 (선택)")
                Spacer(modifier = Modifier.height(6.dp))
                DialogTextField(
                    value = state.createProjectDescription,
                    onValueChange = onDescriptionChange,
                    placeholder = "프로젝트에 대한 간단한 설명",
                    minLines = 3,
                )

                Spacer(modifier = Modifier.height(20.dp))

                DialogSubmitButton(
                    label = "프로젝트 만들기",
                    enabled = state.canSubmitProject,
                    isLoading = state.isCreatingProject,
                    onClick = onSubmit,
                )
            }
        }
    }
}

@Composable
private fun CoworkDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        val visibleState = remember {
            MutableTransitionState(false).apply { targetState = true }
        }
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                    scaleIn(spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow), initialScale = 0.90f),
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 16.dp,
                tonalElevation = 0.dp,
                content = content,
            )
        }
    }
}

@Composable
private fun CoworkDialogHeader(
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 16.dp, bottom = if (subtitle != null) 2.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "닫기",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(bottom = if (subtitle != null) 0.dp else 0.dp),
        )
    }
}

@Composable
private fun DialogFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DialogSubmitButton(
    label: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = false,
    minLines: Int = 1,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val textColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        minLines = minLines,
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
        cursorBrush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(bgColor)
                    .border(1.5.dp, borderColor, MaterialTheme.shapes.small)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = placeholderColor,
                    )
                }
                inner()
            }
        },
    )
}

@Composable
private fun TypeButton(
    type: ChannelType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (isSelected) MaterialTheme.colorScheme.primary
                     else MaterialTheme.colorScheme.surfaceVariant
    val foreground = if (isSelected) MaterialTheme.colorScheme.onPrimary
                     else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = type.icon(),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = foreground,
        )
        Text(
            text = type.label(),
            color = foreground,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun MainStore.State.accountDisplayName(): String =
    accountNickname?.takeIf { it.isNotBlank() }
        ?: accountName?.takeIf { it.isNotBlank() }
        ?: accountEmail?.takeIf { it.isNotBlank() }
        ?: "내 계정"

private fun MainStore.State.accountInitial(): String =
    accountDisplayName().firstOrNull()?.uppercase() ?: "?"

@Composable
private fun UserStatus.dotColor(): Color {
    val ext = coworkExtendedColors
    return when (this) {
        UserStatus.Online -> ext.statusOnline
        UserStatus.DoNotDisturb -> ext.statusDnd
    }
}

private fun UserStatus.label(): String = when (this) {
    UserStatus.Online -> "온라인"
    UserStatus.DoNotDisturb -> "방해금지"
}

private fun TeamRole.label(): String = when (this) {
    TeamRole.Owner -> "OWNER"
    TeamRole.Admin -> "ADMIN"
    TeamRole.Member -> "MEMBER"
    TeamRole.Unknown -> "UNKNOWN"
}

private fun TeamRole.isAtLeastAdmin(): Boolean =
    this == TeamRole.Owner || this == TeamRole.Admin

private fun ChannelType.icon(): ImageVector = when (this) {
    ChannelType.Text -> Icons.Rounded.ChatBubble
    ChannelType.Voice -> Icons.AutoMirrored.Rounded.VolumeUp
    ChannelType.Webhook -> Icons.Rounded.Link
    ChannelType.MeetingNote -> Icons.AutoMirrored.Rounded.Article
    ChannelType.AccountShare -> Icons.Rounded.Person
    ChannelType.FileShare -> Icons.AutoMirrored.Rounded.Article
    ChannelType.Unknown -> Icons.AutoMirrored.Rounded.HelpOutline
}

private fun ChannelType.label(): String = when (this) {
    ChannelType.Text -> "텍스트"
    ChannelType.Voice -> "음성"
    ChannelType.Webhook -> "웹훅"
    ChannelType.MeetingNote -> "회의록"
    ChannelType.AccountShare -> "계정 공유"
    ChannelType.FileShare -> "파일 공유"
    ChannelType.Unknown -> "알 수 없음"
}
