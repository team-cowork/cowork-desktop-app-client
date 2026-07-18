package com.cowork.desktop.client.feature.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cowork.desktop.client.data.repository.MeetingNoteRepository
import com.cowork.desktop.client.domain.model.MeetingNoteTemplate
import com.cowork.desktop.client.domain.model.TemplateSection
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private enum class MeetingSectionType(
    val apiValue: String,
    val label: String,
) {
    Text("TEXT", "짧은 텍스트"),
    Markdown("MARKDOWN", "긴 텍스트"),
    Date("DATE", "날짜"),
    DateTime("DATETIME", "날짜와 시간"),
    UserList("USER_LIST", "참석자 목록"),
}

private data class SectionDraft(
    val title: String,
    val type: MeetingSectionType,
    val placeholder: String?,
    val isRequired: Boolean,
)

@Composable
internal fun MeetingTemplateManagerDialog(
    channelId: Long,
    onDismiss: () -> Unit,
    onChanged: () -> Unit = {},
) {
    val repository = koinInject<MeetingNoteRepository>()
    val scope = rememberCoroutineScope()
    var templates by remember(channelId) { mutableStateOf<List<MeetingNoteTemplate>>(emptyList()) }
    var selectedTemplateId by remember(channelId) { mutableStateOf<Long?>(null) }
    var newTemplateName by remember(channelId) { mutableStateOf("") }
    var isLoading by remember(channelId) { mutableStateOf(true) }
    var isBusy by remember(channelId) { mutableStateOf(false) }
    var error by remember(channelId) { mutableStateOf<String?>(null) }

    LaunchedEffect(channelId) {
        isLoading = true
        error = null
        runCatching { repository.getTemplates(channelId) }
            .onSuccess { loadedTemplates ->
                templates = loadedTemplates
                selectedTemplateId = selectedTemplateId
                    ?.takeIf { selectedId -> loadedTemplates.any { it.id == selectedId } }
                    ?: loadedTemplates.firstOrNull { it.isActive }?.id
                    ?: loadedTemplates.firstOrNull()?.id
            }
            .onFailure { error = it.userMessage() }
        isLoading = false
    }

    fun mutate(operation: suspend () -> Unit) {
        if (isBusy) return
        scope.launch {
            isBusy = true
            error = null
            runCatching { operation() }
                .onFailure { error = it.userMessage() }
            isBusy = false
        }
    }

    fun replaceTemplate(updated: MeetingNoteTemplate) {
        templates = templates.map { template ->
            if (template.id == updated.id) updated else template
        }
    }

    fun replaceSection(templateId: Long, updated: TemplateSection) {
        templates = templates.map { template ->
            if (template.id != templateId) {
                template
            } else {
                template.copy(
                    sections = template.sections.map { section ->
                        if (section.id == updated.id) updated else section
                    },
                )
            }
        }
    }

    val selectedTemplate = templates.firstOrNull { it.id == selectedTemplateId }

    ManagementDialogShell(
        title = "회의록 템플릿 관리",
        onDismiss = onDismiss,
        sidebar = {
            Text(
                text = "템플릿",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
            if (!isLoading && templates.isEmpty()) {
                Text(
                    text = "등록된 템플릿이 없습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp),
                )
            }
            templates.forEach { template ->
                ManagementTabButton(
                    label = buildString {
                        append(template.name)
                        if (template.isActive) append(" · 활성")
                    },
                    selected = template.id == selectedTemplateId,
                    onClick = { selectedTemplateId = template.id },
                )
            }
        },
    ) {
        error?.let { ManagementError(it) }

        PanelTitle("새 템플릿")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = newTemplateName,
                onValueChange = { newTemplateName = it },
                label = { Text("템플릿 이름") },
                singleLine = true,
                enabled = !isBusy,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = {
                    val name = newTemplateName.trim()
                    mutate {
                        val created = repository.createTemplate(channelId, name)
                        templates = templates + created
                        selectedTemplateId = created.id
                        newTemplateName = ""
                        onChanged()
                    }
                },
                enabled = !isLoading && !isBusy && newTemplateName.isNotBlank(),
            ) {
                Text("추가")
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            selectedTemplate == null -> {
                Text(
                    text = "템플릿을 추가하면 섹션을 구성하고 활성화할 수 있습니다.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                MeetingTemplateDetails(
                    template = selectedTemplate,
                    isBusy = isBusy,
                    onRename = { name ->
                        mutate {
                            replaceTemplate(repository.updateTemplate(channelId, selectedTemplate.id, name))
                            onChanged()
                        }
                    },
                    onActivate = {
                        mutate {
                            val activated = repository.activateTemplate(channelId, selectedTemplate.id)
                            templates = templates.map { template ->
                                when (template.id) {
                                    activated.id -> activated.copy(isActive = true)
                                    else -> template.copy(isActive = false)
                                }
                            }
                            onChanged()
                        }
                    },
                    onDelete = {
                        mutate {
                            repository.deleteTemplate(channelId, selectedTemplate.id)
                            val remaining = templates.filterNot { it.id == selectedTemplate.id }
                            templates = remaining
                            selectedTemplateId = remaining.firstOrNull { it.isActive }?.id
                                ?: remaining.firstOrNull()?.id
                            onChanged()
                        }
                    },
                    onCreateSection = { draft, afterSuccess ->
                        mutate {
                            val created = repository.createSection(
                                channelId = channelId,
                                templateId = selectedTemplate.id,
                                title = draft.title,
                                type = draft.type.apiValue,
                                placeholder = draft.placeholder,
                                isRequired = draft.isRequired,
                            )
                            templates = templates.map { template ->
                                if (template.id == selectedTemplate.id) {
                                    template.copy(sections = template.sections + created)
                                } else {
                                    template
                                }
                            }
                            afterSuccess()
                            onChanged()
                        }
                    },
                    onUpdateSection = { sectionId, draft, afterSuccess ->
                        mutate {
                            val updated = repository.updateSection(
                                channelId = channelId,
                                templateId = selectedTemplate.id,
                                sectionId = sectionId,
                                title = draft.title,
                                type = draft.type.apiValue,
                                placeholder = draft.placeholder,
                                isRequired = draft.isRequired,
                            )
                            replaceSection(selectedTemplate.id, updated)
                            afterSuccess()
                            onChanged()
                        }
                    },
                    onDeleteSection = { sectionId, afterSuccess ->
                        mutate {
                            repository.deleteSection(channelId, selectedTemplate.id, sectionId)
                            templates = templates.map { template ->
                                if (template.id == selectedTemplate.id) {
                                    template.copy(
                                        sections = template.sections.filterNot { it.id == sectionId },
                                    )
                                } else {
                                    template
                                }
                            }
                            afterSuccess()
                            onChanged()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun MeetingTemplateDetails(
    template: MeetingNoteTemplate,
    isBusy: Boolean,
    onRename: (String) -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    onCreateSection: (SectionDraft, afterSuccess: () -> Unit) -> Unit,
    onUpdateSection: (Long, SectionDraft, afterSuccess: () -> Unit) -> Unit,
    onDeleteSection: (Long, afterSuccess: () -> Unit) -> Unit,
) {
    var name by remember(template.id, template.name) { mutableStateOf(template.name) }
    var confirmTemplateDelete by remember(template.id) { mutableStateOf(false) }

    PanelTitle("템플릿 설정")
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            singleLine = true,
            enabled = !isBusy,
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = { onRename(name.trim()) },
            enabled = !isBusy && name.isNotBlank() && name.trim() != template.name,
        ) {
            Text("이름 저장")
        }
    }

    Spacer(Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = if (template.isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = if (template.isActive) "현재 활성 템플릿" else "비활성 템플릿",
                style = MaterialTheme.typography.labelMedium,
                color = if (template.isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        if (!template.isActive) {
            OutlinedButton(onClick = onActivate, enabled = !isBusy) {
                Text("활성화")
            }
            Spacer(Modifier.width(8.dp))
        }
        if (!template.isActive) {
            if (confirmTemplateDelete) {
                TextButton(
                    onClick = onDelete,
                    enabled = !isBusy,
                ) {
                    Text("삭제 확정", color = MaterialTheme.colorScheme.error)
                }
                TextButton(
                    onClick = { confirmTemplateDelete = false },
                    enabled = !isBusy,
                ) {
                    Text("취소")
                }
            } else {
                TextButton(
                    onClick = { confirmTemplateDelete = true },
                    enabled = !isBusy,
                ) {
                    Text("템플릿 삭제", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))
    HorizontalDivider()
    Spacer(Modifier.height(20.dp))

    MeetingTemplateSections(
        template = template,
        isBusy = isBusy,
        onCreate = onCreateSection,
        onUpdate = onUpdateSection,
        onDelete = onDeleteSection,
    )
}

@Composable
private fun MeetingTemplateSections(
    template: MeetingNoteTemplate,
    isBusy: Boolean,
    onCreate: (SectionDraft, afterSuccess: () -> Unit) -> Unit,
    onUpdate: (Long, SectionDraft, afterSuccess: () -> Unit) -> Unit,
    onDelete: (Long, afterSuccess: () -> Unit) -> Unit,
) {
    var editingSectionId by remember(template.id) { mutableStateOf<Long?>(null) }
    var sectionTitle by remember(template.id) { mutableStateOf("") }
    var sectionType by remember(template.id) { mutableStateOf(MeetingSectionType.Text) }
    var sectionPlaceholder by remember(template.id) { mutableStateOf("") }
    var isRequired by remember(template.id) { mutableStateOf(false) }
    var isTypeMenuOpen by remember(template.id) { mutableStateOf(false) }
    var pendingDeleteSectionId by remember(template.id) { mutableStateOf<Long?>(null) }

    fun resetEditor() {
        editingSectionId = null
        sectionTitle = ""
        sectionType = MeetingSectionType.Text
        sectionPlaceholder = ""
        isRequired = false
        isTypeMenuOpen = false
    }

    fun edit(section: TemplateSection) {
        editingSectionId = section.id
        sectionTitle = section.title
        sectionType = MeetingSectionType.entries.firstOrNull { it.apiValue == section.type }
            ?: MeetingSectionType.Text
        sectionPlaceholder = section.placeholder.orEmpty()
        isRequired = section.isRequired
        pendingDeleteSectionId = null
    }

    PanelTitle(if (editingSectionId == null) "섹션 추가" else "섹션 수정")
    OutlinedTextField(
        value = sectionTitle,
        onValueChange = { sectionTitle = it },
        label = { Text("섹션 제목") },
        singleLine = true,
        enabled = !isBusy,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = { isTypeMenuOpen = true },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(sectionType.label)
            }
            DropdownMenu(
                expanded = isTypeMenuOpen,
                onDismissRequest = { isTypeMenuOpen = false },
            ) {
                MeetingSectionType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.label) },
                        onClick = {
                            sectionType = type
                            isTypeMenuOpen = false
                        },
                    )
                }
            }
        }
        Text("필수 입력")
        Switch(
            checked = isRequired,
            onCheckedChange = { isRequired = it },
            enabled = !isBusy,
        )
    }
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = sectionPlaceholder,
        onValueChange = { sectionPlaceholder = it },
        label = { Text("입력 안내 (선택)") },
        singleLine = true,
        enabled = !isBusy,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (editingSectionId != null) {
            TextButton(onClick = ::resetEditor, enabled = !isBusy) {
                Text("수정 취소")
            }
            Spacer(Modifier.width(8.dp))
        }
        Button(
            onClick = {
                val sectionId = editingSectionId
                val placeholder = sectionPlaceholder.trim()
                val draft = SectionDraft(
                    title = sectionTitle.trim(),
                    type = sectionType,
                    placeholder = if (sectionId == null) placeholder.ifBlank { null } else placeholder,
                    isRequired = isRequired,
                )
                if (sectionId == null) {
                    onCreate(draft, ::resetEditor)
                } else {
                    onUpdate(sectionId, draft, ::resetEditor)
                }
            },
            enabled = !isBusy && sectionTitle.isNotBlank(),
        ) {
            Text(if (editingSectionId == null) "섹션 추가" else "수정 저장")
        }
    }

    Spacer(Modifier.height(24.dp))
    PanelTitle("구성된 섹션 ${template.sections.size}개")
    if (template.sections.isEmpty()) {
        Text(
            text = "아직 구성된 섹션이 없습니다.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        template.sections.forEachIndexed { index, section ->
            Surface(
                color = if (section.id == editingSectionId) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(7.dp),
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = section.title,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = buildString {
                                append(section.type.sectionTypeLabel())
                                if (section.isRequired) append(" · 필수")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        section.placeholder?.takeIf { it.isNotBlank() }?.let { placeholder ->
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (pendingDeleteSectionId == section.id) {
                        TextButton(
                            onClick = {
                                onDelete(section.id) {
                                    if (editingSectionId == section.id) resetEditor()
                                    pendingDeleteSectionId = null
                                }
                            },
                            enabled = !isBusy,
                        ) {
                            Text("삭제 확정", color = MaterialTheme.colorScheme.error)
                        }
                        TextButton(
                            onClick = { pendingDeleteSectionId = null },
                            enabled = !isBusy,
                        ) {
                            Text("취소")
                        }
                    } else {
                        TextButton(onClick = { edit(section) }, enabled = !isBusy) {
                            Text("수정")
                        }
                        TextButton(
                            onClick = { pendingDeleteSectionId = section.id },
                            enabled = !isBusy,
                        ) {
                            Text("삭제", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun String.sectionTypeLabel(): String =
    MeetingSectionType.entries.firstOrNull { it.apiValue == this }?.label ?: this
