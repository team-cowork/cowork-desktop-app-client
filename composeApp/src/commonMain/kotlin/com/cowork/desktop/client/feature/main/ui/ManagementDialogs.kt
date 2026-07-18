package com.cowork.desktop.client.feature.main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cowork.desktop.client.data.repository.ChannelRepository
import com.cowork.desktop.client.data.repository.PreferenceRepository
import com.cowork.desktop.client.data.repository.TeamRepository
import com.cowork.desktop.client.data.repository.UserRepository
import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelMember
import com.cowork.desktop.client.domain.model.ChannelNotificationSettings
import com.cowork.desktop.client.domain.model.Project
import com.cowork.desktop.client.domain.model.Team
import com.cowork.desktop.client.domain.model.TeamCustomRole
import com.cowork.desktop.client.domain.model.TeamInvite
import com.cowork.desktop.client.domain.model.TeamInviteDuration
import com.cowork.desktop.client.domain.model.TeamMember
import com.cowork.desktop.client.domain.model.TeamRole
import com.cowork.desktop.client.domain.model.TeamSummary
import com.cowork.desktop.client.domain.model.TeamSettings
import com.cowork.desktop.client.domain.model.TextChannelSettings
import com.cowork.desktop.client.domain.model.TextChannelSettingsUpdate
import com.cowork.desktop.client.domain.model.WebhookSettingsUpdate
import com.cowork.desktop.client.domain.model.ChannelType
import com.cowork.desktop.client.domain.model.UserProfile
import com.cowork.desktop.client.util.pickImageBytes
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private enum class TeamManagementTab(val label: String) {
    General("일반"),
    Members("멤버"),
    Invites("초대 링크"),
    Roles("역할"),
}

@Composable
internal fun TeamManagementDialog(
    teamSummary: TeamSummary,
    currentUserId: Long?,
    onDismiss: () -> Unit,
    onChanged: () -> Unit,
) {
    val teamRepository = koinInject<TeamRepository>()
    val userRepository = koinInject<UserRepository>()
    val preferenceRepository = koinInject<PreferenceRepository>()
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(TeamManagementTab.General) }
    var team by remember(teamSummary.id) { mutableStateOf<Team?>(null) }
    var members by remember(teamSummary.id) { mutableStateOf<List<TeamMember>>(emptyList()) }
    var profiles by remember(teamSummary.id) { mutableStateOf<Map<Long, UserProfile>>(emptyMap()) }
    var invites by remember(teamSummary.id) { mutableStateOf<List<TeamInvite>>(emptyList()) }
    var roles by remember(teamSummary.id) { mutableStateOf<List<TeamCustomRole>>(emptyList()) }
    var teamSettings by remember(teamSummary.id) { mutableStateOf<TeamSettings?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(teamSummary.id, refreshKey) {
        isLoading = true
        error = null
        runCatching {
            val loadedTeam = teamRepository.getTeam(teamSummary.id)
            val loadedMembers = teamRepository.getMembers(teamSummary.id)
            val loadedProfiles = loadedMembers.associate { member ->
                member.userId to (userRepository.getUserProfile(member.userId) ?: UserProfile(
                    id = member.userId,
                    name = "사용자 ${member.userId}",
                    email = "",
                    nickname = null,
                    profileImageUrl = null,
                    github = null,
                    studentRole = null,
                    studentNumber = null,
                    major = null,
                    specialty = null,
                    description = null,
                    roles = emptyList(),
                ))
            }
            team = loadedTeam
            members = loadedMembers
            profiles = loadedProfiles
            invites = runCatching { teamRepository.getInvites(teamSummary.id) }.getOrDefault(emptyList())
            roles = runCatching { teamRepository.getRoles(teamSummary.id) }.getOrDefault(emptyList())
            teamSettings = preferenceRepository.getTeamSettings(teamSummary.id)
        }.onFailure { error = it.userMessage() }
        isLoading = false
    }

    ManagementDialogShell(
        title = "${teamSummary.name} 관리",
        onDismiss = onDismiss,
        sidebar = {
            TeamManagementTab.entries.forEach { tab ->
                ManagementTabButton(tab.label, selectedTab == tab) { selectedTab = tab }
            }
        },
    ) {
        if (isLoading && team == null) {
            Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            error?.let { ManagementError(it) }
            when (selectedTab) {
                TeamManagementTab.General -> TeamGeneralPanel(
                    team = team,
                    canManage = teamSummary.myRole == TeamRole.Owner || teamSummary.myRole == TeamRole.Admin,
                    canDeleteTeam = teamSummary.myRole == TeamRole.Owner,
                    onSave = { name, description ->
                        scope.launch {
                            error = null
                            runCatching {
                                teamRepository.updateTeam(
                                    teamId = teamSummary.id,
                                    name = name,
                                    description = description,
                                    clearDescription = description == null,
                                )
                            }
                                .onSuccess {
                                    team = it
                                    onChanged()
                                }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onReplaceIcon = {
                        scope.launch {
                            pickImageBytes()?.let { (bytes, contentType) ->
                                runCatching { teamRepository.replaceTeamIcon(teamSummary.id, bytes, contentType) }
                                    .onSuccess {
                                        refreshKey++
                                        onChanged()
                                    }
                                    .onFailure { error = it.userMessage() }
                            }
                        }
                    },
                    onDeleteIcon = {
                        scope.launch {
                            runCatching { teamRepository.deleteTeamIcon(teamSummary.id) }
                                .onSuccess {
                                    refreshKey++
                                    onChanged()
                                }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onDeleteTeam = {
                        scope.launch {
                            runCatching { teamRepository.deleteTeam(teamSummary.id) }
                                .onSuccess {
                                    onDismiss()
                                    onChanged()
                                }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    settings = teamSettings,
                    onSaveSettings = { settings ->
                        scope.launch {
                            runCatching { preferenceRepository.updateTeamSettings(teamSummary.id, settings) }
                                .onSuccess { teamSettings = it }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                )

                TeamManagementTab.Members -> TeamMembersPanel(
                    members = members,
                    profiles = profiles,
                    roles = roles,
                    canManage = teamSummary.myRole == TeamRole.Owner || teamSummary.myRole == TeamRole.Admin,
                    canChangeDefaultRole = teamSummary.myRole == TeamRole.Owner,
                    currentUserId = currentUserId,
                    onInvite = { userId ->
                        scope.launch {
                            runCatching { teamRepository.inviteMembers(teamSummary.id, listOf(userId)) }
                                .onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onRoleChange = { userId, role ->
                        scope.launch {
                            runCatching { teamRepository.changeMemberRole(teamSummary.id, userId, role) }
                                .onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onAssignCustomRole = { userId, roleId, assigned ->
                        scope.launch {
                            runCatching {
                                if (assigned) teamRepository.assignRole(teamSummary.id, userId, roleId)
                                else teamRepository.revokeRole(teamSummary.id, userId, roleId)
                            }.onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onRemove = { userId ->
                        scope.launch {
                            runCatching { teamRepository.removeMember(teamSummary.id, userId) }
                                .onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onLeave = {
                        val userId = currentUserId ?: return@TeamMembersPanel
                        scope.launch {
                            runCatching { teamRepository.leaveTeam(teamSummary.id, userId) }
                                .onSuccess {
                                    onDismiss()
                                    onChanged()
                                }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                )

                TeamManagementTab.Invites -> TeamInvitesPanel(
                    invites = invites,
                    canManage = teamSummary.myRole == TeamRole.Owner || teamSummary.myRole == TeamRole.Admin,
                    onCreate = { duration ->
                        scope.launch {
                            runCatching { teamRepository.createInvite(teamSummary.id, duration) }
                                .onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onDelete = { code ->
                        scope.launch {
                            runCatching { teamRepository.deleteInvite(teamSummary.id, code) }
                                .onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                )

                TeamManagementTab.Roles -> TeamRolesPanel(
                    roles = roles,
                    canManage = teamSummary.myRole == TeamRole.Owner || teamSummary.myRole == TeamRole.Admin,
                    onCreate = { name, color, priority, mentionable, permissions ->
                        scope.launch {
                            runCatching {
                                teamRepository.createRole(
                                    teamId = teamSummary.id,
                                    name = name,
                                    colorHex = color,
                                    priority = priority,
                                    mentionable = mentionable,
                                    permissions = permissions,
                                )
                            }.onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onDelete = { roleId ->
                        scope.launch {
                            runCatching { teamRepository.deleteRole(teamSummary.id, roleId) }
                                .onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TeamGeneralPanel(
    team: Team?,
    canManage: Boolean,
    canDeleteTeam: Boolean,
    onSave: (String, String?) -> Unit,
    onReplaceIcon: () -> Unit,
    onDeleteIcon: () -> Unit,
    onDeleteTeam: () -> Unit,
    settings: TeamSettings?,
    onSaveSettings: (TeamSettings) -> Unit,
) {
    var name by remember(team?.id, team?.name) { mutableStateOf(team?.name.orEmpty()) }
    var description by remember(team?.id, team?.description) { mutableStateOf(team?.description.orEmpty()) }
    var confirmDelete by remember { mutableStateOf(false) }
    var tagSpamBlock by remember(settings) { mutableStateOf(settings?.tagSpamBlock ?: false) }
    var nicknameFormatEnforced by remember(settings) { mutableStateOf(settings?.nicknameFormatEnforced ?: false) }
    var nicknameExample by remember(settings) { mutableStateOf(settings?.nicknameFormatExample.orEmpty()) }
    val canManageSettings = canManage && settings != null

    PanelTitle("팀 정보")
    OutlinedTextField(name, { name = it }, label = { Text("이름") }, modifier = Modifier.fillMaxWidth(), enabled = canManage)
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        description,
        { description = it },
        label = { Text("설명") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        enabled = canManage,
    )
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onReplaceIcon, enabled = canManage) { Text("아이콘 교체") }
        TextButton(onClick = onDeleteIcon, enabled = canManage && team?.iconUrl != null) { Text("아이콘 삭제") }
        Spacer(Modifier.weight(1f))
        Button(onClick = { onSave(name.trim(), description.trim().ifBlank { null }) }, enabled = canManage && name.isNotBlank()) {
            Text("저장")
        }
    }
    Spacer(Modifier.height(24.dp))
    HorizontalDivider()
    Spacer(Modifier.height(18.dp))
    PanelTitle("팀 정책")
    if (settings == null) {
        Text("팀 정책을 불러오지 못해 편집을 잠갔습니다.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("태그 스팸 차단")
            Text("과도한 멘션과 태그 사용을 제한합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(tagSpamBlock, { tagSpamBlock = it }, enabled = canManageSettings)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("닉네임 형식 강제")
            Text("팀에서 정한 닉네임 형식을 적용합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(nicknameFormatEnforced, { nicknameFormatEnforced = it }, enabled = canManageSettings)
    }
    if (nicknameFormatEnforced) {
        OutlinedTextField(nicknameExample, { nicknameExample = it }, label = { Text("닉네임 형식 예시") }, modifier = Modifier.fillMaxWidth(), enabled = canManageSettings)
        Spacer(Modifier.height(8.dp))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        OutlinedButton(
            onClick = { onSaveSettings(TeamSettings(tagSpamBlock, nicknameFormatEnforced, nicknameExample.trim().ifBlank { null })) },
            enabled = canManageSettings,
        ) { Text("팀 정책 저장") }
    }
    Spacer(Modifier.height(32.dp))
    HorizontalDivider()
    Spacer(Modifier.height(20.dp))
    PanelTitle("위험 구역")
    Text("팀을 삭제하면 복구할 수 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
    if (confirmDelete) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("정말 삭제할까요?", color = MaterialTheme.colorScheme.error)
            Button(onClick = onDeleteTeam, enabled = canDeleteTeam) { Text("삭제 확정") }
            TextButton(onClick = { confirmDelete = false }) { Text("취소") }
        }
    } else {
        OutlinedButton(onClick = { confirmDelete = true }, enabled = canDeleteTeam) { Text("팀 삭제") }
    }
}

@Composable
private fun TeamMembersPanel(
    members: List<TeamMember>,
    profiles: Map<Long, UserProfile>,
    roles: List<TeamCustomRole>,
    canManage: Boolean,
    canChangeDefaultRole: Boolean,
    currentUserId: Long?,
    onInvite: (Long) -> Unit,
    onRoleChange: (Long, TeamRole) -> Unit,
    onAssignCustomRole: (Long, Long, Boolean) -> Unit,
    onRemove: (Long) -> Unit,
    onLeave: () -> Unit,
) {
    var userIdText by remember { mutableStateOf("") }
    PanelTitle("멤버 ${members.size}명")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = userIdText,
            onValueChange = { userIdText = it.filter(Char::isDigit) },
            label = { Text("초대할 사용자 ID") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            enabled = canManage,
        )
        Button(
            onClick = { userIdText.toLongOrNull()?.let(onInvite); userIdText = "" },
            enabled = canManage && userIdText.toLongOrNull() != null,
        ) { Text("초대") }
    }
    Spacer(Modifier.height(16.dp))
    members.forEach { member ->
        val profile = profiles[member.userId]
        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(profile?.nickname ?: profile?.name ?: "사용자 ${member.userId}", fontWeight = FontWeight.SemiBold)
                        Text(profile?.email?.ifBlank { "ID ${member.userId}" } ?: "ID ${member.userId}", style = MaterialTheme.typography.bodySmall)
                    }
                    TeamRoleSelector(member.role, enabled = canChangeDefaultRole && member.userId != currentUserId && member.role != TeamRole.Owner) {
                        onRoleChange(member.userId, it)
                    }
                    TextButton(onClick = { onRemove(member.userId) }, enabled = canManage && member.userId != currentUserId && member.role != TeamRole.Owner) {
                        Text("내보내기", color = MaterialTheme.colorScheme.error)
                    }
                }
                if (roles.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("커스텀 역할", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    roles.forEach { role ->
                        val assigned = member.roles.any { it.id == role.id }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = assigned,
                                onCheckedChange = { onAssignCustomRole(member.userId, role.id, it) },
                                enabled = canManage,
                            )
                            Text(role.name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
    if (currentUserId != null) {
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onLeave, enabled = members.firstOrNull { it.userId == currentUserId }?.role != TeamRole.Owner) {
            Text("팀 나가기")
        }
    }
}

@Composable
private fun TeamRoleSelector(value: TeamRole, enabled: Boolean, onSelected: (TeamRole) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled) { Text(value.managementLabel()) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(TeamRole.Admin, TeamRole.Member).forEach { role ->
                DropdownMenuItem(text = { Text(role.managementLabel()) }, onClick = {
                    expanded = false
                    onSelected(role)
                })
            }
        }
    }
}

@Composable
private fun TeamInvitesPanel(
    invites: List<TeamInvite>,
    canManage: Boolean,
    onCreate: (TeamInviteDuration) -> Unit,
    onDelete: (String) -> Unit,
) {
    var duration by remember { mutableStateOf(TeamInviteDuration.SevenDays) }
    var expanded by remember { mutableStateOf(false) }
    PanelTitle("초대 링크")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            OutlinedButton(onClick = { expanded = true }, enabled = canManage) { Text(duration.managementLabel()) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf(TeamInviteDuration.OneDay, TeamInviteDuration.SevenDays, TeamInviteDuration.ThirtyDays, TeamInviteDuration.Never).forEach { option ->
                    DropdownMenuItem(text = { Text(option.managementLabel()) }, onClick = {
                        duration = option
                        expanded = false
                    })
                }
            }
        }
        Button(onClick = { onCreate(duration) }, enabled = canManage) { Text("링크 만들기") }
    }
    Spacer(Modifier.height(16.dp))
    if (invites.isEmpty()) Text("활성 초대 링크가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    invites.forEach { invite ->
        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), shape = RoundedCornerShape(10.dp)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(invite.inviteCode, fontWeight = FontWeight.SemiBold)
                    Text(
                        invite.expiresAt?.let { "만료 $it" } ?: "만료 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = { onDelete(invite.inviteCode) }, enabled = canManage) { Text("삭제") }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TeamRolesPanel(
    roles: List<TeamCustomRole>,
    canManage: Boolean,
    onCreate: (String, String, Int, Boolean, Set<String>) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#5865F2") }
    var priorityText by remember { mutableStateOf("0") }
    var mentionable by remember { mutableStateOf(true) }
    var permissionsText by remember { mutableStateOf("") }
    PanelTitle("커스텀 역할")
    OutlinedTextField(name, { name = it }, label = { Text("역할 이름") }, modifier = Modifier.fillMaxWidth(), enabled = canManage)
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(color, { color = it }, label = { Text("색상 (#RRGGBB)") }, modifier = Modifier.weight(1f), enabled = canManage)
        OutlinedTextField(
            priorityText,
            { priorityText = it.filter { char -> char.isDigit() || char == '-' } },
            label = { Text("우선순위") },
            modifier = Modifier.width(140.dp),
            enabled = canManage,
        )
    }
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        permissionsText,
        { permissionsText = it },
        label = { Text("권한 (쉼표로 구분)") },
        modifier = Modifier.fillMaxWidth(),
        enabled = canManage,
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(mentionable, { mentionable = it }, enabled = canManage)
        Text("멘션 허용")
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                onCreate(
                    name.trim(),
                    color.trim(),
                    priorityText.toIntOrNull() ?: 0,
                    mentionable,
                    permissionsText.split(',').map(String::trim).filter(String::isNotEmpty).toSet(),
                )
                name = ""
                permissionsText = ""
            },
            enabled = canManage && name.isNotBlank(),
        ) { Text("역할 추가") }
    }
    Spacer(Modifier.height(16.dp))
    roles.forEach { role ->
        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), shape = RoundedCornerShape(10.dp)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(parseRoleColor(role.colorHex), RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(role.name, fontWeight = FontWeight.SemiBold)
                    Text("우선순위 ${role.priority} · 권한 ${role.permissions.size}개", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { onDelete(role.id) }, enabled = canManage) { Text("삭제") }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun ChannelManagementDialog(
    channel: Channel,
    accountId: Long?,
    canManage: Boolean,
    profiles: Map<Long, UserProfile>,
    projects: List<Project>,
    onDismiss: () -> Unit,
    onChanged: () -> Unit,
) {
    val channelRepository = koinInject<ChannelRepository>()
    val preferenceRepository = koinInject<PreferenceRepository>()
    val userRepository = koinInject<UserRepository>()
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf("일반") }
    var loadedChannel by remember(channel.id) { mutableStateOf(channel) }
    var members by remember(channel.id) { mutableStateOf<List<ChannelMember>>(emptyList()) }
    var memberProfiles by remember(channel.id) { mutableStateOf(profiles) }
    var notificationsEnabled by remember(channel.id) { mutableStateOf(true) }
    var notificationsLoaded by remember(channel.id) { mutableStateOf(false) }
    var isUpdatingNotifications by remember(channel.id) { mutableStateOf(false) }
    var textSettings by remember(channel.id) { mutableStateOf(TextChannelSettings()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(channel.id, refreshKey) {
        isLoading = true
        runCatching {
            loadedChannel = channelRepository.getChannel(channel.id)
            members = channelRepository.getMembers(channel.id)
            memberProfiles = members.associate { member ->
                member.userId to (profiles[member.userId] ?: userRepository.getUserProfile(member.userId) ?: fallbackProfile(member.userId))
            }
            if (accountId != null) {
                val notificationResult = runCatching {
                    preferenceRepository.getChannelNotificationSettings(accountId, channel.id).isEnabled
                }
                notificationResult.onSuccess {
                    notificationsEnabled = it
                    notificationsLoaded = true
                }.onFailure { error = it.userMessage() }
            }
            if (channel.type == ChannelType.Text || channel.type == ChannelType.Webhook) {
                textSettings = runCatching { preferenceRepository.getTextChannelSettings(channel.id) }
                    .getOrDefault(TextChannelSettings())
            }
        }.onFailure { error = it.userMessage() }
        isLoading = false
    }

    ManagementDialogShell(
        title = "# ${loadedChannel.name}",
        onDismiss = onDismiss,
        sidebar = {
            buildList {
                add("일반")
                add("멤버")
                if (channel.type == ChannelType.Text || channel.type == ChannelType.Webhook) add("웹훅 정책")
            }.forEach { tab -> ManagementTabButton(tab, selectedTab == tab) { selectedTab = tab } }
        },
    ) {
        error?.let { ManagementError(it) }
        if (isLoading) CircularProgressIndicator()
        when (selectedTab) {
            "일반" -> ChannelGeneralPanel(
                channel = loadedChannel,
                projects = projects,
                canManage = canManage,
                notificationsEnabled = notificationsEnabled,
                notificationsEditable = accountId != null && notificationsLoaded && !isUpdatingNotifications,
                onNotificationsChanged = { enabled ->
                    val previous = notificationsEnabled
                    notificationsEnabled = enabled
                    val userId = accountId ?: return@ChannelGeneralPanel
                    scope.launch {
                        isUpdatingNotifications = true
                        runCatching {
                            preferenceRepository.updateChannelNotificationSettings(
                                userId,
                                channel.id,
                                ChannelNotificationSettings(enabled),
                            )
                        }.onFailure {
                            notificationsEnabled = previous
                            error = it.userMessage()
                        }
                        isUpdatingNotifications = false
                    }
                },
                onSave = { name, description, isPrivate, projectId ->
                    scope.launch {
                        runCatching {
                            channelRepository.updateChannel(
                                channelId = channel.id,
                                name = name,
                                description = description,
                                isPrivate = isPrivate,
                                projectId = projectId,
                                clearDescription = description == null,
                                updateProjectId = true,
                            )
                        }
                            .onSuccess {
                                loadedChannel = it
                                onChanged()
                            }
                            .onFailure { error = it.userMessage() }
                    }
                },
                onDelete = {
                    scope.launch {
                        runCatching { channelRepository.deleteChannel(channel.id) }
                            .onSuccess {
                                onDismiss()
                                onChanged()
                            }
                            .onFailure { error = it.userMessage() }
                    }
                },
            )

            "멤버" -> ChannelMembersPanel(
                members = members,
                profiles = memberProfiles,
                canManage = canManage,
                onAdd = { userId ->
                    scope.launch {
                        runCatching { channelRepository.addMember(channel.id, userId) }
                            .onSuccess { refreshKey++ }
                            .onFailure { error = it.userMessage() }
                    }
                },
                onRemove = { memberId ->
                    scope.launch {
                        runCatching { channelRepository.removeMember(channel.id, memberId) }
                            .onSuccess { refreshKey++ }
                            .onFailure { error = it.userMessage() }
                    }
                },
            )

            "웹훅 정책" -> TextChannelSettingsPanel(
                settings = textSettings,
                canManage = canManage,
                onSave = { update ->
                    scope.launch {
                        runCatching { preferenceRepository.updateTextChannelSettings(channel.id, update) }
                            .onSuccess { textSettings = it }
                            .onFailure { error = it.userMessage() }
                    }
                },
            )
        }
    }
}

@Composable
private fun TextChannelSettingsPanel(
    settings: TextChannelSettings,
    canManage: Boolean,
    onSave: (TextChannelSettingsUpdate) -> Unit,
) {
    var isActive by remember(settings) { mutableStateOf(settings.webhook?.isActive ?: true) }
    var secretKey by remember(settings) { mutableStateOf(settings.webhook?.secretKey.orEmpty()) }
    var retryCountText by remember(settings) { mutableStateOf((settings.webhook?.retryCount ?: 3).toString()) }
    var retryIntervalText by remember(settings) { mutableStateOf((settings.webhook?.retryIntervalMs ?: 1000).toString()) }
    PanelTitle("텍스트 채널 웹훅 정책")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("웹훅 활성화")
            Text("이 채널에서 외부 웹훅 호출을 허용합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(isActive, { isActive = it }, enabled = canManage)
    }
    OutlinedTextField(secretKey, { secretKey = it }, label = { Text("공통 시크릿 키") }, modifier = Modifier.fillMaxWidth(), enabled = canManage)
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            retryCountText,
            { retryCountText = it.filter(Char::isDigit) },
            label = { Text("재시도 횟수") },
            modifier = Modifier.weight(1f),
            enabled = canManage,
        )
        OutlinedTextField(
            retryIntervalText,
            { retryIntervalText = it.filter(Char::isDigit) },
            label = { Text("재시도 간격(ms)") },
            modifier = Modifier.weight(1f),
            enabled = canManage,
        )
    }
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Button(
            onClick = {
                onSave(
                    TextChannelSettingsUpdate(
                        WebhookSettingsUpdate(
                            isActive = isActive,
                            secretKey = secretKey,
                            retryCount = retryCountText.toIntOrNull() ?: 0,
                            retryIntervalMs = retryIntervalText.toIntOrNull() ?: 0,
                        )
                    )
                )
            },
            enabled = canManage && retryCountText.toIntOrNull() != null && retryIntervalText.toIntOrNull() != null,
        ) { Text("정책 저장") }
    }
}

@Composable
private fun ChannelGeneralPanel(
    channel: Channel,
    projects: List<Project>,
    canManage: Boolean,
    notificationsEnabled: Boolean,
    notificationsEditable: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    onSave: (String, String?, Boolean, Long?) -> Unit,
    onDelete: () -> Unit,
) {
    var name by remember(channel.id, channel.name) { mutableStateOf(channel.name) }
    var description by remember(channel.id, channel.description) { mutableStateOf(channel.description.orEmpty()) }
    var isPrivate by remember(channel.id, channel.isPrivate) { mutableStateOf(channel.isPrivate) }
    var projectId by remember(channel.id, channel.projectId) { mutableStateOf(channel.projectId) }
    var projectExpanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    PanelTitle("채널 정보")
    OutlinedTextField(name, { name = it }, label = { Text("이름") }, modifier = Modifier.fillMaxWidth(), enabled = canManage)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(description, { description = it }, label = { Text("설명") }, modifier = Modifier.fillMaxWidth(), minLines = 2, enabled = canManage)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("비공개 채널", modifier = Modifier.weight(1f))
        Switch(isPrivate, { isPrivate = it }, enabled = canManage)
    }
    if (projects.isNotEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("연결 프로젝트", modifier = Modifier.weight(1f))
            Box {
                OutlinedButton(onClick = { projectExpanded = true }, enabled = canManage) {
                    Text(projects.firstOrNull { it.id == projectId }?.name ?: "팀 전체")
                }
                DropdownMenu(projectExpanded, { projectExpanded = false }) {
                    DropdownMenuItem(text = { Text("팀 전체") }, onClick = { projectId = null; projectExpanded = false })
                    projects.forEach { project ->
                        DropdownMenuItem(text = { Text(project.name) }, onClick = { projectId = project.id; projectExpanded = false })
                    }
                }
            }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("알림")
            Text("이 채널의 알림을 받습니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(notificationsEnabled, onNotificationsChanged, enabled = notificationsEditable)
    }
    Spacer(Modifier.height(8.dp))
    Row {
        Spacer(Modifier.weight(1f))
        Button(onClick = { onSave(name.trim(), description.trim().ifBlank { null }, isPrivate, projectId) }, enabled = canManage && name.isNotBlank()) { Text("저장") }
    }
    Spacer(Modifier.height(28.dp))
    HorizontalDivider()
    Spacer(Modifier.height(16.dp))
    if (confirmDelete) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("채널을 삭제할까요?", color = MaterialTheme.colorScheme.error)
            Button(onClick = onDelete, enabled = canManage) { Text("삭제 확정") }
            TextButton(onClick = { confirmDelete = false }) { Text("취소") }
        }
    } else {
        OutlinedButton(onClick = { confirmDelete = true }, enabled = canManage) { Text("채널 삭제") }
    }
}

@Composable
private fun ChannelMembersPanel(
    members: List<ChannelMember>,
    profiles: Map<Long, UserProfile>,
    canManage: Boolean,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit,
) {
    var userIdText by remember { mutableStateOf("") }
    PanelTitle("채널 멤버 ${members.size}명")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            userIdText,
            { userIdText = it.filter(Char::isDigit) },
            label = { Text("사용자 ID") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            enabled = canManage,
        )
        Button(
            onClick = { userIdText.toLongOrNull()?.let(onAdd); userIdText = "" },
            enabled = canManage && userIdText.toLongOrNull() != null,
        ) { Text("추가") }
    }
    Spacer(Modifier.height(16.dp))
    members.forEach { member ->
        val profile = profiles[member.userId]
        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f), shape = RoundedCornerShape(10.dp)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(profile?.nickname ?: profile?.name ?: "사용자 ${member.userId}", fontWeight = FontWeight.SemiBold)
                    Text("ID ${member.userId}", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { onRemove(member.id) }, enabled = canManage) { Text("제거") }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun ManagementDialogShell(
    title: String,
    onDismiss: () -> Unit,
    sidebar: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 520.dp, max = 760.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 8.dp,
        ) {
            Column {
                Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    TextButton(onClick = onDismiss) { Text("닫기") }
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    Column(
                        modifier = Modifier.width(150.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) { sidebar() }
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(22.dp),
                    ) { content() }
                }
            }
        }
    }
}

@Composable
internal fun ManagementTabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 9.dp), fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
internal fun PanelTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
}

@Composable
internal fun ManagementError(message: String) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(message, Modifier.padding(10.dp), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
    }
    Spacer(Modifier.height(12.dp))
}

internal fun Throwable.userMessage(): String = message?.takeIf(String::isNotBlank) ?: "요청을 처리하지 못했습니다."

private fun TeamRole.managementLabel(): String = when (this) {
    TeamRole.Owner -> "소유자"
    TeamRole.Admin -> "관리자"
    TeamRole.Member -> "멤버"
    TeamRole.Unknown -> "알 수 없음"
}

private fun TeamInviteDuration.managementLabel(): String = when (this) {
    TeamInviteDuration.OneDay -> "1일"
    TeamInviteDuration.SevenDays -> "7일"
    TeamInviteDuration.ThirtyDays -> "30일"
    TeamInviteDuration.Never -> "무기한"
    TeamInviteDuration.Unknown -> "알 수 없음"
}

private fun parseRoleColor(value: String): Color = runCatching {
    val normalized = value.removePrefix("#")
    Color((0xFF000000 or normalized.toLong(16)).toULong())
}.getOrDefault(Color(0xFF5865F2))

private fun fallbackProfile(userId: Long) = UserProfile(
    id = userId,
    name = "사용자 $userId",
    email = "",
    nickname = null,
    profileImageUrl = null,
    github = null,
    studentRole = null,
    studentNumber = null,
    major = null,
    specialty = null,
    description = null,
    roles = emptyList(),
)
