package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.MeetingNoteApi
import com.cowork.desktop.client.domain.model.MeetingNote
import com.cowork.desktop.client.domain.model.MeetingNoteTemplate
import com.cowork.desktop.client.domain.model.TemplateSection

class DefaultMeetingNoteRepository(
    private val authRepository: AuthRepository,
    private val meetingNoteApi: MeetingNoteApi,
) : MeetingNoteRepository {

    override suspend fun getNotes(channelId: Long): List<MeetingNote> =
        authorized { meetingNoteApi.getNotes(it, channelId) }

    override suspend fun getNote(channelId: Long, noteId: Long): MeetingNote =
        authorized { meetingNoteApi.getNote(it, channelId, noteId) }

    override suspend fun createNote(channelId: Long, templateId: Long, title: String, content: String): MeetingNote =
        authorized { meetingNoteApi.createNote(it, channelId, templateId, title, content) }

    override suspend fun updateNote(channelId: Long, noteId: Long, title: String?, content: String?): MeetingNote =
        authorized { meetingNoteApi.updateNote(it, channelId, noteId, title, content) }

    override suspend fun deleteNote(channelId: Long, noteId: Long) =
        authorized { meetingNoteApi.deleteNote(it, channelId, noteId) }

    override suspend fun getTemplates(channelId: Long): List<MeetingNoteTemplate> =
        authorized { meetingNoteApi.getTemplates(it, channelId) }

    override suspend fun getTemplate(channelId: Long, templateId: Long): MeetingNoteTemplate =
        authorized { meetingNoteApi.getTemplate(it, channelId, templateId) }

    override suspend fun createTemplate(channelId: Long, name: String): MeetingNoteTemplate =
        authorized { meetingNoteApi.createTemplate(it, channelId, name) }

    override suspend fun updateTemplate(channelId: Long, templateId: Long, name: String): MeetingNoteTemplate =
        authorized { meetingNoteApi.updateTemplate(it, channelId, templateId, name) }

    override suspend fun deleteTemplate(channelId: Long, templateId: Long) =
        authorized { meetingNoteApi.deleteTemplate(it, channelId, templateId) }

    override suspend fun activateTemplate(channelId: Long, templateId: Long): MeetingNoteTemplate =
        authorized { meetingNoteApi.activateTemplate(it, channelId, templateId) }

    override suspend fun createSection(
        channelId: Long,
        templateId: Long,
        title: String,
        type: String,
        placeholder: String?,
        isRequired: Boolean,
    ): TemplateSection = authorized {
        meetingNoteApi.createSection(it, channelId, templateId, title, type, placeholder, isRequired)
    }

    override suspend fun updateSection(
        channelId: Long,
        templateId: Long,
        sectionId: Long,
        title: String?,
        type: String?,
        placeholder: String?,
        isRequired: Boolean?,
    ): TemplateSection = authorized {
        meetingNoteApi.updateSection(
            it,
            channelId,
            templateId,
            sectionId,
            title,
            type,
            placeholder,
            isRequired,
        )
    }

    override suspend fun deleteSection(channelId: Long, templateId: Long, sectionId: Long) =
        authorized { meetingNoteApi.deleteSection(it, channelId, templateId, sectionId) }

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.authorized(block)
}
