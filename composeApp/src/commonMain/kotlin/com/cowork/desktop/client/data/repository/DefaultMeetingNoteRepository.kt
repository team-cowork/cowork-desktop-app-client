package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.MeetingNoteApi
import com.cowork.desktop.client.domain.model.MeetingNote
import com.cowork.desktop.client.domain.model.MeetingNoteTemplate

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

    override suspend fun getTemplates(channelId: Long): List<MeetingNoteTemplate> =
        authorized { meetingNoteApi.getTemplates(it, channelId) }

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.authorized(block)
}
