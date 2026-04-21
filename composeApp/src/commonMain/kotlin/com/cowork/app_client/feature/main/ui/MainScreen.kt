package com.cowork.app_client.feature.main.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cowork.app_client.domain.model.Channel
import com.cowork.app_client.domain.model.ChannelType
import com.cowork.app_client.domain.model.TeamRole
import com.cowork.app_client.domain.model.TeamSummary
import com.cowork.app_client.feature.main.component.MainComponent
import com.cowork.app_client.feature.main.store.MainStore
import com.cowork.app_client.ui.theme.CoworkColors

@Composable
fun MainScreen(component: MainComponent) {
    val state by component.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            TeamRail(
                state = state,
                onTeamClick = component::onTeamClick,
                onCreateTeamClick = component::onCreateTeamClick,
            )

            VerticalDivider()

            ChannelPane(
                state = state,
                onReloadClick = component::onReloadClick,
                onChannelClick = component::onChannelClick,
                onCreateChannelClick = component::onCreateChannelClick,
            )

            VerticalDivider()

            WorkspacePane(state = state)
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
    }
}

@Composable
private fun TeamRail(
    state: MainStore.State,
    onTeamClick: (Long) -> Unit,
    onCreateTeamClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(88.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "cowork",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

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
        }

        TextButton(onClick = onCreateTeamClick) {
            Text("+")
        }
    }
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

@Composable
private fun ChannelPane(
    state: MainStore.State,
    onReloadClick: () -> Unit,
    onChannelClick: (Long) -> Unit,
    onCreateChannelClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
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

            TextButton(onClick = onReloadClick) {
                Text("새로고침")
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
            TextButton(
                onClick = onCreateChannelClick,
                enabled = state.selectedTeamId != null,
            ) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            state.selectedTeamId == null -> EmptyPaneText("왼쪽 아래 + 버튼으로 팀을 생성하세요.")
            state.isLoadingChannels -> Box(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            }
            state.channels.isEmpty() -> EmptyPaneText("아직 채널이 없습니다. 채널 API가 연결되면 여기에서 표시됩니다.")
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
