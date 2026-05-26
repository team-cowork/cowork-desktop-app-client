package com.cowork.desktop.client.domain.model

data class MeetingNote(
    val id: Long,
    val channelId: Long,
    val templateId: Long,
    val title: String,
    val content: String,
    val createdBy: Long,
    val createdAt: String,
    val updatedAt: String,
)

data class MeetingNoteTemplate(
    val id: Long,
    val channelId: Long,
    val name: String,
    val isActive: Boolean,
    val createdBy: Long,
    val sections: List<TemplateSection>,
)

data class TemplateSection(
    val id: Long,
    val templateId: Long,
    val title: String,
    val type: String,
    val placeholder: String?,
    val isRequired: Boolean,
)
