package com.cowork.app_client.feature.main.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cowork.app_client.domain.model.Channel
import com.cowork.app_client.domain.model.ChannelType
import com.cowork.app_client.domain.model.TeamRole
import com.cowork.app_client.domain.model.TeamSummary
import com.cowork.app_client.domain.model.UserStatus
import com.cowork.app_client.feature.main.component.MainComponent
import com.cowork.app_client.feature.main.store.MainStore
import com.cowork.app_client.ui.theme.CoworkColors
import com.cowork.app_client.util.decodeImageBitmap
import com.cowork.app_client.util.horizontalResizeCursor
import com.cowork.app_client.util.pickImageBytes
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.launch
import coworkappclient.composeapp.generated.resources.Res
import coworkappclient.composeapp.generated.resources.logo_cowork
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

private val StatusOnlineColor = Color(0xFF23A55A)
private val StatusDndColor = Color(0xFFF23F42)

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
                )

                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.34f))

                ChannelPane(
                    state = state,
                    width = channelPaneWidth,
                    onChannelClick = component::onChannelClick,
                    onCreateChannelClick = component::onCreateChannelClick,
                    onAccountBarClick = component::onAccountMenuClick,
                )

                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

                WorkspacePane(state = state)
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
                    onSubmit = component::onCreateTeamSubmit,
                )
            }

            if (state.isCreateChannelOpen) {
                CreateChannelDialog(
                    state = state,
                    onDismiss = component::onCreateChannelDismiss,
                    onNameChange = component::onCreateChannelNameChange,
                    onNoticeChange = component::onCreateChannelNoticeChange,
                    onTypeChange = component::onCreateChannelTypeChange,
                    onSubmit = component::onCreateChannelSubmit,
                )
            }

            if (state.isSettingsOpen) {
                SettingsDialog(
                    state = state,
                    onDismiss = component::onSettingsDismiss,
                )
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
                )
            }

            item {
                AddTeamButton(onClick = onCreateTeamClick)
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
            .clip(if (isHovered) RoundedCornerShape(12.dp) else CircleShape)
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
) {
    val background = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val foreground = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = team.name.firstOrNull()?.uppercase() ?: "?",
            color = foreground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
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
    onAccountBarClick: () -> Unit,
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
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.channels, key = { it.id }) { channel ->
                        ChannelRow(
                            channel = channel,
                            isSelected = channel.id == state.selectedChannelId,
                            onClick = { onChannelClick(channel.id) },
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
            .clickable(onClick = onClick)
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
            Text(
                text = state.accountStatus.label(),
                style = MaterialTheme.typography.bodySmall,
                color = state.accountStatus.dotColor(),
                maxLines = 1,
            )
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
    var isDndSelectorOpen by remember(state.accountStatus) {
        mutableStateOf(state.accountStatus == UserStatus.DoNotDisturb)
    }

    Box(modifier = Modifier.width(436.dp)) {
        Surface(
            modifier = Modifier.width(280.dp).padding(8.dp),
            shape = RoundedCornerShape(8.dp),
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
                                        CoworkColors.Red700,
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

                    val profileLine = listOfNotNull(
                        state.accountStudentNumber?.takeIf { it.isNotBlank() },
                        state.accountMajor?.takeIf { it.isNotBlank() },
                        state.accountStudentRole?.takeIf { it.isNotBlank() },
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "GitHub @$github",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
                    Text(
                        text = "로그아웃",
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onSignOut)
                            .padding(horizontal = 2.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
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
                Box(
                    modifier = Modifier
                        .size(dotInnerSize)
                        .clip(CircleShape)
                        .background(status.dotColor()),
                )
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
        StatusGlyph(
            status = status,
            isSelected = isSelected,
        )
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
        StatusGlyph(
            status = UserStatus.DoNotDisturb,
            isSelected = isSelected,
        )
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
        shape = RoundedCornerShape(8.dp),
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
    isSelected: Boolean,
) {
    val color = when {
        status == UserStatus.DoNotDisturb -> StatusDndColor
        isSelected -> status.dotColor()
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.48f)
    }

    Canvas(modifier = Modifier.size(12.dp)) {
        when (status) {
            UserStatus.Online -> drawCircle(color = color, radius = size.minDimension / 2f)
            UserStatus.DoNotDisturb -> {
                drawCircle(color = color, radius = size.minDimension / 2f)
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.28f, size.height * 0.5f),
                    end = Offset(size.width * 0.72f, size.height * 0.5f),
                    strokeWidth = 1.8.dp.toPx(),
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
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = channel.type.prefix(),
            color = CoworkColors.Red700,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = channel.name,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WorkspacePane(state: MainStore.State) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
    ) {
        Text(
            text = state.selectedChannel?.name ?: state.selectedTeam?.name ?: "팀을 선택하세요",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = state.selectedChannel?.let { channel ->
                "${channel.type.label()} 채널입니다. 채팅/음성/웹훅 작업 영역이 이 패널에 이어서 구현됩니다."
            } ?: "채팅, 이슈, 음성채팅 작업 영역이 이 패널에 이어서 구현됩니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        state.selectedChannel?.notice?.takeIf { it.isNotBlank() }?.let { notice ->
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = notice,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        if (state.selectedChannel?.type == ChannelType.Text) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "메시지",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoadingMessages -> CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp,
                )
                state.messages.isEmpty() -> EmptyPaneText(
                    "메시지 조회 API가 연결되면 최근 메시지가 표시됩니다.",
                )
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        MessageRow(message = message)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                placeholder = { Text("Socket.io JWT 인증 연결 후 메시지 전송 활성화") },
                singleLine = true,
            )
        }
    }
}

@Composable
private fun MessageRow(message: com.cowork.app_client.domain.model.ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
    ) {
        Text(
            text = "user ${message.authorId}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
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

@Composable
private fun SettingsDialog(
    state: MainStore.State,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.widthIn(min = 440.dp, max = 520.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
            ),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SettingsIcon(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "설정",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = state.accountDisplayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("닫기")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                Spacer(modifier = Modifier.height(14.dp))

                SettingsSection(
                    title = "계정",
                    description = "프로필, 상태, GitHub 정보를 이 화면에서 다룰 예정입니다.",
                )
                SettingsSection(
                    title = "알림",
                    description = "방해금지 시간과 데스크톱 알림 정책을 이어서 연결합니다.",
                )
                SettingsSection(
                    title = "화면",
                    description = "사이드바 폭, 테마, 창 동작 같은 클라이언트 설정을 관리합니다.",
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CreateTeamDialog(
    state: MainStore.State,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("팀 생성") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.createTeamName,
                    onValueChange = onNameChange,
                    modifier = Modifier.widthIn(min = 320.dp),
                    singleLine = true,
                    label = { Text("팀 이름") },
                )
                OutlinedTextField(
                    value = state.createTeamDescription,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.widthIn(min = 320.dp),
                    minLines = 3,
                    label = { Text("설명") },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = state.canSubmitTeam,
            ) {
                Text(if (state.isCreatingTeam) "생성 중" else "생성")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
private fun CreateChannelDialog(
    state: MainStore.State,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onNoticeChange: (String) -> Unit,
    onTypeChange: (ChannelType) -> Unit,
    onSubmit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("채널 생성") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf(
                        ChannelType.Text,
                        ChannelType.Voice,
                        ChannelType.Webhook,
                        ChannelType.MeetingNote,
                    ).forEach { type ->
                        TypeButton(
                            type = type,
                            isSelected = type == state.createChannelType,
                            onClick = { onTypeChange(type) },
                        )
                    }
                }

                OutlinedTextField(
                    value = state.createChannelName,
                    onValueChange = onNameChange,
                    modifier = Modifier.widthIn(min = 360.dp),
                    singleLine = true,
                    label = { Text("채널 이름") },
                )
                OutlinedTextField(
                    value = state.createChannelNotice,
                    onValueChange = onNoticeChange,
                    modifier = Modifier.widthIn(min = 360.dp),
                    minLines = 3,
                    label = { Text("공지") },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = state.canSubmitChannel,
            ) {
                Text(if (state.isCreatingChannel) "생성 중" else "생성")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
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
    val background = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val foreground = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Text(
        text = type.label(),
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        color = foreground,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
    )
}

private fun MainStore.State.accountDisplayName(): String =
    accountNickname?.takeIf { it.isNotBlank() }
        ?: accountName?.takeIf { it.isNotBlank() }
        ?: accountEmail?.takeIf { it.isNotBlank() }
        ?: "내 계정"

private fun MainStore.State.accountInitial(): String =
    accountDisplayName().firstOrNull()?.uppercase() ?: "?"

private fun UserStatus.dotColor(): Color = when (this) {
    UserStatus.Online -> StatusOnlineColor
    UserStatus.DoNotDisturb -> StatusDndColor
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

private fun ChannelType.prefix(): String = when (this) {
    ChannelType.Text -> "#"
    ChannelType.Voice -> "음성"
    ChannelType.Webhook -> "훅"
    ChannelType.MeetingNote -> "회의"
    ChannelType.Unknown -> "?"
}

private fun ChannelType.label(): String = when (this) {
    ChannelType.Text -> "텍스트"
    ChannelType.Voice -> "음성"
    ChannelType.Webhook -> "웹훅"
    ChannelType.MeetingNote -> "회의록"
    ChannelType.Unknown -> "알 수 없음"
}
