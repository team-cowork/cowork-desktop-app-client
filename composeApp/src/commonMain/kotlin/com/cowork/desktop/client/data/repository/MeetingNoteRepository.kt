package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.MeetingNote
import com.cowork.desktop.client.domain.model.MeetingNoteTemplate

interface MeetingNoteRepository {
    suspend fun getNotes(channelId: Long): List<MeetingNote>
    suspend fun getNote(channelId: Long, noteId: Long): MeetingNote
    suspend fun createNote(channelId: Long, templateId: Long, title: String, content: String): MeetingNote
    suspend fun getTemplates(channelId: Long): List<MeetingNoteTemplate>
}
