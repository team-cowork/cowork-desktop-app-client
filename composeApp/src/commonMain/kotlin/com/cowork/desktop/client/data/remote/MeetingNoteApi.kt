package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.MeetingNote
import com.cowork.desktop.client.domain.model.MeetingNoteTemplate
import com.cowork.desktop.client.domain.model.TemplateSection
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
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
        }.bodyPayload<List<NoteResponse>?>().orEmpty().map(NoteResponse::toDomain)

    suspend fun getNote(accessToken: String, channelId: Long, noteId: Long): MeetingNote =
        client.get("$baseUrl/channels/$channelId/meeting-notes/$noteId") {
            bearerAuth(accessToken)
        }.bodyPayload<NoteResponse?>()?.toDomain()
            ?: error("회의록 상세 조회 응답에 data가 없습니다")

    suspend fun createNote(accessToken: String, channelId: Long, templateId: Long, title: String, content: String): MeetingNote =
        client.post("$baseUrl/channels/$channelId/meeting-notes") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateNoteRequest(templateId = templateId, title = title, content = content))
        }.bodyPayload<NoteResponse?>()?.toDomain()
            ?: error("회의록 생성 응답에 data가 없습니다")

    suspend fun updateNote(
        accessToken: String,
        channelId: Long,
        noteId: Long,
        title: String? = null,
        content: String? = null,
    ): MeetingNote =
        client.patch("$baseUrl/channels/$channelId/meeting-notes/$noteId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(UpdateNoteRequest(title, content))
        }.bodyPayload<NoteResponse?>()?.toDomain()
            ?: error("회의록 수정 응답에 data가 없습니다")

    suspend fun deleteNote(accessToken: String, channelId: Long, noteId: Long) {
        client.delete("$baseUrl/channels/$channelId/meeting-notes/$noteId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun getTemplates(accessToken: String, channelId: Long): List<MeetingNoteTemplate> =
        client.get("$baseUrl/channels/$channelId/meeting-note-templates") {
            bearerAuth(accessToken)
        }.bodyPayload<List<TemplateResponse>?>().orEmpty().map(TemplateResponse::toDomain)

    suspend fun getTemplate(accessToken: String, channelId: Long, templateId: Long): MeetingNoteTemplate =
        client.get("$baseUrl/channels/$channelId/meeting-note-templates/$templateId") {
            bearerAuth(accessToken)
        }.bodyPayload<TemplateResponse?>()?.toDomain()
            ?: error("회의록 템플릿 조회 응답에 data가 없습니다")

    suspend fun createTemplate(accessToken: String, channelId: Long, name: String): MeetingNoteTemplate =
        client.post("$baseUrl/channels/$channelId/meeting-note-templates") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateTemplateRequest(name))
        }.bodyPayload<TemplateResponse?>()?.toDomain()
            ?: error("회의록 템플릿 생성 응답에 data가 없습니다")

    suspend fun updateTemplate(
        accessToken: String,
        channelId: Long,
        templateId: Long,
        name: String,
    ): MeetingNoteTemplate =
        client.patch("$baseUrl/channels/$channelId/meeting-note-templates/$templateId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(UpdateTemplateRequest(name))
        }.bodyPayload<TemplateResponse?>()?.toDomain()
            ?: error("회의록 템플릿 수정 응답에 data가 없습니다")

    suspend fun deleteTemplate(accessToken: String, channelId: Long, templateId: Long) {
        client.delete("$baseUrl/channels/$channelId/meeting-note-templates/$templateId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun activateTemplate(accessToken: String, channelId: Long, templateId: Long): MeetingNoteTemplate =
        client.patch("$baseUrl/channels/$channelId/meeting-note-templates/$templateId/activate") {
            bearerAuth(accessToken)
        }.bodyPayload<TemplateResponse?>()?.toDomain()
            ?: error("회의록 템플릿 활성화 응답에 data가 없습니다")

    suspend fun createSection(
        accessToken: String,
        channelId: Long,
        templateId: Long,
        title: String,
        type: String,
        placeholder: String?,
        isRequired: Boolean,
    ): TemplateSection =
        client.post("$baseUrl/channels/$channelId/meeting-note-templates/$templateId/sections") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateSectionRequest(title, type, placeholder, isRequired))
        }.bodyPayload<TemplateSectionResponse?>()?.toDomain()
            ?: error("회의록 템플릿 섹션 생성 응답에 data가 없습니다")

    suspend fun updateSection(
        accessToken: String,
        channelId: Long,
        templateId: Long,
        sectionId: Long,
        title: String? = null,
        type: String? = null,
        placeholder: String? = null,
        isRequired: Boolean? = null,
    ): TemplateSection =
        client.patch("$baseUrl/channels/$channelId/meeting-note-templates/$templateId/sections/$sectionId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(UpdateSectionRequest(title, type, placeholder, isRequired))
        }.bodyPayload<TemplateSectionResponse?>()?.toDomain()
            ?: error("회의록 템플릿 섹션 수정 응답에 data가 없습니다")

    suspend fun deleteSection(
        accessToken: String,
        channelId: Long,
        templateId: Long,
        sectionId: Long,
    ) {
        client.delete("$baseUrl/channels/$channelId/meeting-note-templates/$templateId/sections/$sectionId") {
            bearerAuth(accessToken)
        }
    }

    @Serializable
    private data class CreateNoteRequest(val templateId: Long, val title: String, val content: String)

    @Serializable
    private data class UpdateNoteRequest(val title: String?, val content: String?)

    @Serializable
    private data class CreateTemplateRequest(val name: String)

    @Serializable
    private data class UpdateTemplateRequest(val name: String)

    @Serializable
    private data class CreateSectionRequest(
        val title: String,
        val type: String,
        val placeholder: String?,
        val isRequired: Boolean,
    )

    @Serializable
    private data class UpdateSectionRequest(
        val title: String?,
        val type: String?,
        val placeholder: String?,
        val isRequired: Boolean?,
    )

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
    data class TemplateSectionResponse(
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
