package com.cowork.desktop.client.feature.main.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cowork.desktop.client.data.repository.ProjectRepository
import com.cowork.desktop.client.data.repository.UserRepository
import com.cowork.desktop.client.domain.model.GithubPullRequest
import com.cowork.desktop.client.domain.model.GithubPullRequestBoard
import com.cowork.desktop.client.domain.model.GithubPullRequestFile
import com.cowork.desktop.client.domain.model.GithubPullRequestSummary
import com.cowork.desktop.client.domain.model.Project
import com.cowork.desktop.client.domain.model.ProjectMember
import com.cowork.desktop.client.domain.model.ProjectRole
import com.cowork.desktop.client.domain.model.ProjectStatus
import com.cowork.desktop.client.domain.model.UserProfile
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private enum class ProjectTab(val label: String) {
    Overview("개요"),
    Members("멤버"),
    Github("GitHub PR"),
}

@Composable
internal fun ColumnScope.ProjectWorkspaceContent(
    project: Project,
    teamCanManage: Boolean,
    currentUserId: Long?,
    onChanged: () -> Unit,
) {
    val projectRepository = koinInject<ProjectRepository>()
    val userRepository = koinInject<UserRepository>()
    val scope = rememberCoroutineScope()
    var selectedTab by remember(project.id) { mutableStateOf(ProjectTab.Overview) }
    var loadedProject by remember(project.id) { mutableStateOf(project) }
    var members by remember(project.id) { mutableStateOf<List<ProjectMember>>(emptyList()) }
    var profiles by remember(project.id) { mutableStateOf<Map<Long, UserProfile>>(emptyMap()) }
    var board by remember(project.id) { mutableStateOf<GithubPullRequestBoard?>(null) }
    var isLoading by remember(project.id) { mutableStateOf(true) }
    var error by remember(project.id) { mutableStateOf<String?>(null) }
    var refreshKey by remember(project.id) { mutableStateOf(0) }
    var selectedPullRequest by remember(project.id) { mutableStateOf<GithubPullRequest?>(null) }
    var selectedPullRequestFiles by remember(project.id) { mutableStateOf<List<GithubPullRequestFile>>(emptyList()) }
    var isLoadingPullRequest by remember(project.id) { mutableStateOf(false) }
    val myProjectRole = members.firstOrNull { it.userId == currentUserId }?.role
    val canModify = teamCanManage || myProjectRole == ProjectRole.Owner || myProjectRole == ProjectRole.Editor
    val canOwn = teamCanManage || myProjectRole == ProjectRole.Owner

    LaunchedEffect(project.id, refreshKey) {
        isLoading = true
        error = null
        runCatching {
            loadedProject = projectRepository.getProject(project.id)
            members = projectRepository.getMembers(project.id)
            profiles = members.associate { member ->
                member.userId to (userRepository.getUserProfile(member.userId) ?: projectFallbackProfile(member.userId))
            }
            board = if (loadedProject.githubRepoUrl != null) {
                runCatching { projectRepository.getGithubPullRequests(project.id) }.getOrNull()
            } else {
                null
            }
        }.onFailure { error = it.userMessage() }
        isLoading = false
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f)) {
            Text(loadedProject.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            loadedProject.description?.takeIf(String::isNotBlank)?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        ProjectStatusBadge(loadedProject.status)
    }
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ProjectTab.entries.forEach { tab ->
            if (selectedTab == tab) Button(onClick = { selectedTab = tab }) { Text(tab.label) }
            else OutlinedButton(onClick = { selectedTab = tab }) { Text(tab.label) }
        }
    }
    Spacer(Modifier.height(16.dp))
    error?.let { ManagementError(it) }
    if (isLoading) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
            when (selectedTab) {
                ProjectTab.Overview -> ProjectOverviewPanel(
                    project = loadedProject,
                    canModify = canModify,
                    canDelete = canOwn,
                    onSave = { name, description ->
                        scope.launch {
                            runCatching {
                                projectRepository.updateProject(
                                    projectId = project.id,
                                    name = name,
                                    description = description,
                                    clearDescription = description == null,
                                )
                            }
                                .onSuccess {
                                    loadedProject = it
                                    onChanged()
                                }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onArchiveToggle = {
                        scope.launch {
                            val status = if (loadedProject.status == ProjectStatus.Active) "ARCHIVED" else "ACTIVE"
                            runCatching { projectRepository.updateProject(project.id, status = status) }
                                .onSuccess {
                                    loadedProject = it
                                    onChanged()
                                }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onDelete = {
                        scope.launch {
                            runCatching { projectRepository.deleteProject(project.id) }
                                .onSuccess { onChanged() }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                )

                ProjectTab.Members -> ProjectMembersPanel(
                    members = members,
                    profiles = profiles,
                    canManage = canOwn,
                    onAdd = { userId, role ->
                        scope.launch {
                            runCatching { projectRepository.addMember(project.id, userId, role) }
                                .onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onRoleChange = { memberId, role ->
                        scope.launch {
                            runCatching { projectRepository.updateMemberRole(project.id, memberId, role) }
                                .onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onRemove = { memberId ->
                        scope.launch {
                            runCatching { projectRepository.removeMember(project.id, memberId) }
                                .onSuccess { refreshKey++ }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                )

                ProjectTab.Github -> GithubBoardPanel(
                    project = loadedProject,
                    board = board,
                    canManage = canModify,
                    onLink = { url ->
                        scope.launch {
                            runCatching { projectRepository.linkGithubRepository(project.id, url) }
                                .onSuccess {
                                    loadedProject = it
                                    refreshKey++
                                    onChanged()
                                }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onUnlink = {
                        scope.launch {
                            runCatching { projectRepository.unlinkGithubRepository(project.id) }
                                .onSuccess {
                                    loadedProject = it
                                    board = null
                                    onChanged()
                                }
                                .onFailure { error = it.userMessage() }
                        }
                    },
                    onRefresh = { refreshKey++ },
                    onPullRequestClick = { summary ->
                        scope.launch {
                            isLoadingPullRequest = true
                            runCatching {
                                val detail = projectRepository.getGithubPullRequest(project.id, summary.number)
                                val files = projectRepository.getGithubPullRequestFiles(project.id, summary.number)
                                detail to files
                            }.onSuccess { (detail, files) ->
                                selectedPullRequest = detail
                                selectedPullRequestFiles = files
                            }.onFailure { error = it.userMessage() }
                            isLoadingPullRequest = false
                        }
                    },
                )
            }
        }
    }

    if (isLoadingPullRequest) {
        Surface(Modifier.fillMaxWidth().padding(24.dp), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.width(20.dp), strokeWidth = 2.dp)
                Text("PR 상세를 불러오는 중…")
            }
        }
    }

    selectedPullRequest?.let { pullRequest ->
        GithubPullRequestDialog(
            pullRequest = pullRequest,
            files = selectedPullRequestFiles,
            canManage = canModify,
            onDismiss = { selectedPullRequest = null },
            onApprove = {
                scope.launch {
                    runCatching { projectRepository.approveGithubPullRequest(project.id, pullRequest.number) }
                        .onSuccess { refreshKey++; selectedPullRequest = null }
                        .onFailure { error = it.userMessage() }
                }
            },
            onMerge = {
                scope.launch {
                    runCatching { projectRepository.mergeGithubPullRequest(project.id, pullRequest.number) }
                        .onSuccess { refreshKey++; selectedPullRequest = null }
                        .onFailure { error = it.userMessage() }
                }
            },
        )
    }
}

@Composable
private fun ProjectOverviewPanel(
    project: Project,
    canModify: Boolean,
    canDelete: Boolean,
    onSave: (String, String?) -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    var name by remember(project.id, project.name) { mutableStateOf(project.name) }
    var description by remember(project.id, project.description) { mutableStateOf(project.description.orEmpty()) }
    var confirmDelete by remember { mutableStateOf(false) }
    PanelTitle("프로젝트 정보")
    OutlinedTextField(name, { name = it }, label = { Text("이름") }, modifier = Modifier.fillMaxWidth(), enabled = canModify)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(description, { description = it }, label = { Text("설명") }, modifier = Modifier.fillMaxWidth(), minLines = 3, enabled = canModify)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onArchiveToggle, enabled = canModify) {
            Text(if (project.status == ProjectStatus.Active) "프로젝트 보관" else "보관 해제")
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = { onSave(name.trim(), description.trim().ifBlank { null }) }, enabled = canModify && name.isNotBlank()) { Text("저장") }
    }
    Spacer(Modifier.height(28.dp))
    HorizontalDivider()
    Spacer(Modifier.height(16.dp))
    if (confirmDelete) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("프로젝트를 삭제할까요?", color = MaterialTheme.colorScheme.error)
            Button(onClick = onDelete, enabled = canDelete) { Text("삭제 확정") }
            TextButton(onClick = { confirmDelete = false }) { Text("취소") }
        }
    } else {
        OutlinedButton(onClick = { confirmDelete = true }, enabled = canDelete) { Text("프로젝트 삭제") }
    }
}

@Composable
private fun ProjectMembersPanel(
    members: List<ProjectMember>,
    profiles: Map<Long, UserProfile>,
    canManage: Boolean,
    onAdd: (Long, ProjectRole) -> Unit,
    onRoleChange: (Long, ProjectRole) -> Unit,
    onRemove: (Long) -> Unit,
) {
    var userIdText by remember { mutableStateOf("") }
    var newRole by remember { mutableStateOf(ProjectRole.Viewer) }
    PanelTitle("프로젝트 멤버 ${members.size}명")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            userIdText,
            { userIdText = it.filter(Char::isDigit) },
            label = { Text("사용자 ID") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = canManage,
        )
        ProjectRoleSelector(newRole, canManage) { newRole = it }
        Button(
            onClick = { userIdText.toLongOrNull()?.let { onAdd(it, newRole) }; userIdText = "" },
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
                    Text(profile?.email?.ifBlank { "ID ${member.userId}" } ?: "ID ${member.userId}", style = MaterialTheme.typography.bodySmall)
                }
                ProjectRoleSelector(member.role, canManage && member.role != ProjectRole.Owner) { onRoleChange(member.id, it) }
                TextButton(onClick = { onRemove(member.id) }, enabled = canManage && member.role != ProjectRole.Owner) { Text("제거") }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ProjectRoleSelector(value: ProjectRole, enabled: Boolean, onSelected: (ProjectRole) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled) { Text(value.label()) }
        DropdownMenu(expanded, { expanded = false }) {
            listOf(ProjectRole.Editor, ProjectRole.Viewer).forEach { role ->
                DropdownMenuItem(text = { Text(role.label()) }, onClick = { expanded = false; onSelected(role) })
            }
        }
    }
}

@Composable
private fun GithubBoardPanel(
    project: Project,
    board: GithubPullRequestBoard?,
    canManage: Boolean,
    onLink: (String) -> Unit,
    onUnlink: () -> Unit,
    onRefresh: () -> Unit,
    onPullRequestClick: (GithubPullRequestSummary) -> Unit,
) {
    var repositoryUrl by remember(project.id, project.githubRepoUrl) { mutableStateOf(project.githubRepoUrl.orEmpty()) }
    PanelTitle("GitHub Pull Request 보드")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            repositoryUrl,
            { repositoryUrl = it },
            label = { Text("GitHub 저장소 URL") },
            placeholder = { Text("https://github.com/org/repository") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = canManage,
        )
        if (project.githubRepoUrl == null) {
            Button(onClick = { onLink(repositoryUrl.trim()) }, enabled = canManage && repositoryUrl.startsWith("https://github.com/")) { Text("연결") }
        } else {
            OutlinedButton(onClick = onUnlink, enabled = canManage) { Text("연결 해제") }
            TextButton(onClick = onRefresh) { Text("새로고침") }
        }
    }
    Spacer(Modifier.height(18.dp))
    if (project.githubRepoUrl == null) {
        Text("저장소를 연결하면 Draft와 리뷰 중 PR을 여기서 확인하고 승인·머지할 수 있습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    if (board == null) {
        Text("PR 보드를 불러오지 못했습니다. 프로젝트 서비스 상태와 GitHub 연결을 확인해 주세요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GithubBoardColumn("Draft", board.draft, Modifier.weight(1f), onPullRequestClick)
        GithubBoardColumn("리뷰 중", board.inReview, Modifier.weight(1f), onPullRequestClick)
    }
}

@Composable
private fun GithubBoardColumn(
    title: String,
    pullRequests: List<GithubPullRequestSummary>,
    modifier: Modifier,
    onPullRequestClick: (GithubPullRequestSummary) -> Unit,
) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("$title ${pullRequests.size}", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            if (pullRequests.isEmpty()) Text("PR 없음", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            pullRequests.forEach { pullRequest ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onPullRequestClick(pullRequest) },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(9.dp),
                    tonalElevation = 1.dp,
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Text("#${pullRequest.number} ${pullRequest.title}", fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(4.dp))
                        Text("@${pullRequest.author}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (pullRequest.labels.isNotEmpty()) {
                            Text(pullRequest.labels.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun GithubPullRequestDialog(
    pullRequest: GithubPullRequest,
    files: List<GithubPullRequestFile>,
    canManage: Boolean,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onMerge: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    ManagementDialogShell(
        title = "#${pullRequest.number} ${pullRequest.title}",
        onDismiss = onDismiss,
        sidebar = {
            Text("${pullRequest.headRef}\n↓\n${pullRequest.baseRef}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("@${pullRequest.author} · ${pullRequest.state}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("머지 상태: ${pullRequest.mergeableState}", style = MaterialTheme.typography.bodySmall)
                pullRequest.reviewDecision?.let { Text("리뷰: $it", style = MaterialTheme.typography.bodySmall) }
            }
            TextButton(onClick = { uriHandler.openUri(pullRequest.htmlUrl) }) { Text("GitHub에서 열기") }
        }
        pullRequest.body?.takeIf(String::isNotBlank)?.let {
            Spacer(Modifier.height(14.dp))
            Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f), shape = RoundedCornerShape(10.dp)) {
                Text(it, Modifier.padding(12.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        PanelTitle("변경 파일 ${files.size}개")
        files.forEach { file ->
            Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.fillMaxWidth().padding(10.dp)) {
                    Text(file.filename, fontWeight = FontWeight.SemiBold)
                    Text("${file.status}  +${file.additions}  -${file.deletions}", style = MaterialTheme.typography.bodySmall)
                    file.patch?.takeIf(String::isNotBlank)?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 12, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onApprove, enabled = canManage) { Text("승인") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onMerge, enabled = canManage && pullRequest.mergeable != false) { Text("머지") }
        }
    }
}

@Composable
private fun ProjectStatusBadge(status: ProjectStatus) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
        Text(
            when (status) {
                ProjectStatus.Active -> "활성"
                ProjectStatus.Archived -> "보관됨"
                ProjectStatus.Unknown -> "알 수 없음"
            },
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun ProjectRole.label(): String = when (this) {
    ProjectRole.Owner -> "소유자"
    ProjectRole.Editor -> "편집자"
    ProjectRole.Viewer -> "보기 전용"
    ProjectRole.Unknown -> "알 수 없음"
}

private fun projectFallbackProfile(userId: Long) = UserProfile(
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
