package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.MeetingNote
import com.cowork.desktop.client.domain.model.MeetingNoteTemplate

interface MeetingNoteRepository {
    suspend fun getNotes(channelId: Long): List<MeetingNote>
    suspend fun getNote(channelId: Long, noteId: Long): MeetingNote
    suspend fun createNote(channelId: Long, templateId: Long, title: String, content: String): MeetingNote
    suspend fun updateNote(channelId: Long, noteId: Long, title: String? = null, content: String? = null): MeetingNote
    suspend fun deleteNote(channelId: Long, noteId: Long)
    suspend fun getTemplates(channelId: Long): List<MeetingNoteTemplate>
    suspend fun getTemplate(channelId: Long, templateId: Long): MeetingNoteTemplate
    suspend fun createTemplate(channelId: Long, name: String): MeetingNoteTemplate
    suspend fun updateTemplate(channelId: Long, templateId: Long, name: String): MeetingNoteTemplate
    suspend fun deleteTemplate(channelId: Long, templateId: Long)
    suspend fun activateTemplate(channelId: Long, templateId: Long): MeetingNoteTemplate
    suspend fun createSection(
        channelId: Long,
        templateId: Long,
        title: String,
        type: String,
        placeholder: String? = null,
        isRequired: Boolean = false,
    ): com.cowork.desktop.client.domain.model.TemplateSection
    suspend fun updateSection(
        channelId: Long,
        templateId: Long,
        sectionId: Long,
        title: String? = null,
        type: String? = null,
        placeholder: String? = null,
        isRequired: Boolean? = null,
    ): com.cowork.desktop.client.domain.model.TemplateSection
    suspend fun deleteSection(channelId: Long, templateId: Long, sectionId: Long)
}
