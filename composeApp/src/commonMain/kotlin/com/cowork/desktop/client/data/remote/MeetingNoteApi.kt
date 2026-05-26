package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.MeetingNote
import com.cowork.desktop.client.domain.model.MeetingNoteTemplate
import com.cowork.desktop.client.domain.model.TemplateSection
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class MeetingNoteApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getNotes(accessToken: String, channelId: Long): List<MeetingNote> =
        client.get("$baseUrl/channels/$channelId/meeting-notes") {
            bearerAuth(accessToken)
        }.body<ApiResponse<List<NoteResponse>>>().data.orEmpty().map(NoteResponse::toDomain)

    suspend fun getNote(accessToken: String, channelId: Long, noteId: Long): MeetingNote =
        client.get("$baseUrl/channels/$channelId/meeting-notes/$noteId") {
            bearerAuth(accessToken)
        }.body<ApiResponse<NoteResponse>>().data?.toDomain()
            ?: error("회의록 상세 조회 응답에 data가 없습니다")

    suspend fun createNote(accessToken: String, channelId: Long, templateId: Long, title: String, content: String): MeetingNote =
        client.post("$baseUrl/channels/$channelId/meeting-notes") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateNoteRequest(templateId = templateId, title = title, content = content))
        }.body<ApiResponse<NoteResponse>>().data?.toDomain()
            ?: error("회의록 생성 응답에 data가 없습니다")

    suspend fun getTemplates(accessToken: String, channelId: Long): List<MeetingNoteTemplate> =
        client.get("$baseUrl/channels/$channelId/meeting-note-templates") {
            bearerAuth(accessToken)
        }.body<ApiResponse<List<TemplateResponse>>>().data.orEmpty().map(TemplateResponse::toDomain)

    @Serializable
    private data class CreateNoteRequest(val templateId: Long, val title: String, val content: String)

    @Serializable
    private data class NoteResponse(
        val id: Long,
        val channelId: Long,
        val templateId: Long,
        val title: String,
        val content: String,
        val createdBy: Long,
        val createdAt: String,
        val updatedAt: String,
    ) {
        fun toDomain(): MeetingNote = MeetingNote(
            id = id,
            channelId = channelId,
            templateId = templateId,
            title = title,
            content = content,
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    @Serializable
    private data class TemplateSectionResponse(
        val id: Long,
        val templateId: Long,
        val title: String,
        val type: String,
        val placeholder: String? = null,
        val isRequired: Boolean = false,
    ) {
        fun toDomain(): TemplateSection = TemplateSection(
            id = id,
            templateId = templateId,
            title = title,
            type = type,
            placeholder = placeholder,
            isRequired = isRequired,
        )
    }

    @Serializable
    private data class TemplateResponse(
        val id: Long,
        val channelId: Long,
        val name: String,
        val isActive: Boolean = false,
        val createdBy: Long,
        val createdAt: String,
        val updatedAt: String,
        val sections: List<TemplateSectionResponse> = emptyList(),
    ) {
        fun toDomain(): MeetingNoteTemplate = MeetingNoteTemplate(
            id = id,
            channelId = channelId,
            name = name,
            isActive = isActive,
            createdBy = createdBy,
            sections = sections.map(TemplateSectionResponse::toDomain),
        )
    }
}
