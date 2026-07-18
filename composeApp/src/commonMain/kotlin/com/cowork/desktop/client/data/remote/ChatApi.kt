package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.ChannelUnreadCount
import com.cowork.desktop.client.domain.model.ChatAttachment
import com.cowork.desktop.client.domain.model.ChatFileItem
import com.cowork.desktop.client.domain.model.ChatFilePage
import com.cowork.desktop.client.domain.model.ChatFileUpload
import com.cowork.desktop.client.domain.model.ChatMessage
import com.cowork.desktop.client.domain.model.ChatMessageSearchItem
import com.cowork.desktop.client.domain.model.ChatMessageSearchPage
import com.cowork.desktop.client.domain.model.ChatMessageSearchQuery
import com.cowork.desktop.client.domain.model.ChatReaction
import com.cowork.desktop.client.domain.model.DirectMessageConversation
import com.cowork.desktop.client.domain.model.DirectMessagePreview
import com.cowork.desktop.client.domain.model.MentionedChatMessage
import com.cowork.desktop.client.domain.model.MessageType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class ChatApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getMessages(
        accessToken: String,
        channelId: Long,
        before: String?,
        limit: Int,
        parentMessageId: String? = null,
    ): List<ChatMessage> =
        client.get("$baseUrl/channels/$channelId/messages") {
            bearerAuth(accessToken)
            before?.let { parameter("before", it) }
            parameter("limit", limit)
            parentMessageId?.let { parameter("parentMessageId", it) }
        }.bodyPayload<List<MessageResponse>>().map(MessageResponse::toDomain)

    suspend fun sendMessage(
        accessToken: String,
        channelId: Long,
        teamId: Long?,
        content: String,
        projectId: Long? = null,
        attachments: List<ChatAttachment> = emptyList(),
        parentMessageId: String? = null,
        clientMessageId: String? = null,
    ) {
        client.post("$baseUrl/channels/$channelId/messages") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(
                SendMessageRequest(
                    teamId = teamId,
                    projectId = projectId,
                    content = content,
                    type = if (attachments.isEmpty()) "TEXT" else "FILE",
                    attachments = attachments.map {
                        AttachmentRequest(
                            name = it.name,
                            url = it.url,
                            size = it.size,
                            mimeType = it.mimeType,
                        )
                    },
                    parentMessageId = parentMessageId,
                    clientMessageId = clientMessageId,
                ),
            )
        }
    }

    suspend fun editMessage(accessToken: String, channelId: Long, messageId: String, content: String) {
        client.patch("$baseUrl/channels/$channelId/messages/$messageId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(EditMessageRequest(content = content))
        }
    }

    suspend fun deleteMessage(accessToken: String, channelId: Long, messageId: String) {
        client.delete("$baseUrl/channels/$channelId/messages/$messageId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun createFileUploadUrl(
        accessToken: String,
        channelId: Long,
        filename: String,
        contentType: String,
        size: Long,
    ): ChatFileUpload =
        client.post("$baseUrl/channels/$channelId/files/presigned-url") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(FileUploadUrlRequest(filename, contentType, size))
        }.bodyPayload<FileUploadResponse>().toDomain()

    suspend fun putFile(upload: ChatFileUpload, bytes: ByteArray) {
        client.put(upload.uploadUrl) {
            headers {
                upload.headers.forEach { (name, value) -> append(name, value) }
            }
            setBody(ByteArrayContent(bytes))
        }
    }

    suspend fun confirmFileUpload(accessToken: String, channelId: Long, objectKey: String): String =
        client.post("$baseUrl/channels/$channelId/files/confirm") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(ConfirmFileUploadRequest(objectKey))
        }.bodyPayload<ConfirmFileUploadResponse>().fileUrl

    suspend fun getFiles(
        accessToken: String,
        channelId: Long,
        before: String? = null,
        limit: Int = 20,
    ): ChatFilePage =
        client.get("$baseUrl/channels/$channelId/files") {
            bearerAuth(accessToken)
            before?.let { parameter("before", it) }
            parameter("limit", limit)
        }.bodyPayload<FileListResponse>().toDomain()

    suspend fun deleteFile(accessToken: String, channelId: Long, fileId: String) {
        client.delete("$baseUrl/channels/$channelId/files/${fileId.encodeURLPathPart()}") {
            bearerAuth(accessToken)
        }
    }

    suspend fun pinMessage(accessToken: String, channelId: Long, messageId: String): ChatMessage =
        client.post("$baseUrl/channels/$channelId/pins/$messageId") {
            bearerAuth(accessToken)
        }.bodyPayload<MessageResponse>().toDomain()

    suspend fun unpinMessage(accessToken: String, channelId: Long, messageId: String) {
        client.delete("$baseUrl/channels/$channelId/pins/$messageId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun getPinnedMessages(accessToken: String, channelId: Long): List<ChatMessage> =
        client.get("$baseUrl/channels/$channelId/pins") {
            bearerAuth(accessToken)
        }.bodyPayload<List<MessageResponse>>().map(MessageResponse::toDomain)

    suspend fun addReaction(accessToken: String, channelId: Long, messageId: String, emoji: String) {
        client.post("$baseUrl/channels/$channelId/messages/$messageId/reactions") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(ReactionRequest(emoji))
        }
    }

    suspend fun removeReaction(accessToken: String, channelId: Long, messageId: String, emoji: String) {
        client.delete(
            "$baseUrl/channels/$channelId/messages/$messageId/reactions/${emoji.encodeURLPathPart()}",
        ) {
            bearerAuth(accessToken)
        }
    }

    suspend fun markChannelRead(
        accessToken: String,
        channelId: Long,
        lastReadMessageId: String,
    ) {
        client.post("$baseUrl/channels/$channelId/read") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(ReadChannelRequest(lastReadMessageId))
        }
    }

    suspend fun getTeamUnread(accessToken: String, teamId: Long): List<ChannelUnreadCount> =
        client.get("$baseUrl/teams/$teamId/unread") {
            bearerAuth(accessToken)
        }.bodyPayload<List<UnreadCountResponse>>().map(UnreadCountResponse::toDomain)

    suspend fun searchTeamMessages(
        accessToken: String,
        teamId: Long,
        query: ChatMessageSearchQuery,
    ): ChatMessageSearchPage =
        client.get("$baseUrl/search/messages") {
            bearerAuth(accessToken)
            parameter("teamId", teamId)
            addSearchParameters(query)
        }.bodyPayload<SearchMessagesResponse>().toDomain()

    suspend fun searchProjectMessages(
        accessToken: String,
        projectId: Long,
        query: ChatMessageSearchQuery,
    ): ChatMessageSearchPage =
        client.get("$baseUrl/projects/$projectId/messages/search") {
            bearerAuth(accessToken)
            addSearchParameters(query)
        }.bodyPayload<SearchMessagesResponse>().toDomain()

    suspend fun createGithubIssue(
        accessToken: String,
        channelId: Long,
        projectId: Long,
        title: String,
        body: String? = null,
    ): Boolean =
        client.post("$baseUrl/channels/$channelId/slash-commands") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(
                SlashCommandRequest(
                    command = "github.issue.create",
                    payload = GithubIssuePayload(projectId, title, body),
                ),
            )
        }.bodyPayload<QueuedResponse>().queued

    suspend fun getDms(accessToken: String): List<DirectMessageConversation> =
        client.get("$baseUrl/dms") {
            bearerAuth(accessToken)
        }.bodyPayload<List<DmResponse>>().map(DmResponse::toDomain)

    suspend fun hideDm(accessToken: String, channelId: Long) {
        client.delete("$baseUrl/dms/$channelId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun blockUser(accessToken: String, targetUserId: Long) {
        client.post("$baseUrl/block/$targetUserId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun unblockUser(accessToken: String, targetUserId: Long) {
        client.delete("$baseUrl/block/$targetUserId") {
            bearerAuth(accessToken)
        }
    }

    suspend fun getBlockedUserIds(accessToken: String): List<Long> =
        client.get("$baseUrl/block") {
            bearerAuth(accessToken)
        }.bodyPayload()

    private fun HttpRequestBuilder.addSearchParameters(query: ChatMessageSearchQuery) {
        parameter("q", query.query)
        query.channelId?.let { parameter("channelId", it) }
        query.authorId?.let { parameter("authorId", it) }
        query.type?.toApiValue()?.let { parameter("type", it) }
        query.hasFile?.let { parameter("hasFile", it) }
        query.before?.let { parameter("before", it) }
        parameter("limit", query.limit)
    }

    @Serializable
    private data class EditMessageRequest(val content: String)

    @Serializable
    private data class SendMessageRequest(
        val teamId: Long? = null,
        val projectId: Long? = null,
        val content: String,
        val type: String,
        val attachments: List<AttachmentRequest> = emptyList(),
        val parentMessageId: String? = null,
        val clientMessageId: String? = null,
    )

    @Serializable
    private data class AttachmentRequest(
        val name: String,
        val url: String,
        val size: Long,
        val mimeType: String,
    )

    @Serializable
    private data class FileUploadUrlRequest(
        val filename: String,
        val contentType: String,
        val size: Long,
    )

    @Serializable
    private data class FileUploadResponse(
        val objectKey: String,
        val uploadUrl: String,
        val fileUrl: String,
        val expiresInSeconds: Long,
        val headers: Map<String, String> = emptyMap(),
    ) {
        fun toDomain() = ChatFileUpload(
            objectKey = objectKey,
            uploadUrl = uploadUrl,
            fileUrl = fileUrl,
            expiresInSeconds = expiresInSeconds,
            headers = headers,
        )
    }

    @Serializable
    private data class ConfirmFileUploadRequest(val objectKey: String)

    @Serializable
    private data class ConfirmFileUploadResponse(val fileUrl: String)

    @Serializable
    private data class FileListResponse(
        val files: List<FileItemResponse> = emptyList(),
        val nextCursor: String? = null,
    ) {
        fun toDomain() = ChatFilePage(
            files = files.map(FileItemResponse::toDomain),
            nextCursor = nextCursor,
        )
    }

    @Serializable
    private data class FileItemResponse(
        val fileId: String,
        val messageId: String,
        val fileName: String,
        val fileSize: Long,
        val fileUrl: String,
        val mimeType: String,
        val uploaderId: Long,
        val uploaderName: String,
        val uploadedAt: String,
    ) {
        fun toDomain() = ChatFileItem(
            fileId = fileId,
            messageId = messageId,
            fileName = fileName,
            fileSize = fileSize,
            fileUrl = fileUrl,
            mimeType = mimeType,
            uploaderId = uploaderId,
            uploaderName = uploaderName,
            uploadedAt = uploadedAt,
        )
    }

    @Serializable
    private data class ReactionRequest(val emoji: String)

    @Serializable
    private data class ReadChannelRequest(val lastReadMessageId: String)

    @Serializable
    private data class UnreadCountResponse(
        val channelId: Long,
        val unreadCount: Int,
    ) {
        fun toDomain() = ChannelUnreadCount(channelId, unreadCount)
    }

    @Serializable
    private data class SearchMessagesResponse(
        val messages: List<SearchMessageResponse> = emptyList(),
        val nextCursor: String? = null,
    ) {
        fun toDomain() = ChatMessageSearchPage(
            messages = messages.map(SearchMessageResponse::toDomain),
            nextCursor = nextCursor,
        )
    }

    @Serializable
    private data class SearchMessageResponse(
        val messageId: String,
        val channelId: Long,
        val authorId: Long,
        val content: String,
        val highlight: List<String> = emptyList(),
        val type: String,
        val hasAttachments: Boolean,
        val isPinned: Boolean,
        val createdAt: String,
    ) {
        fun toDomain() = ChatMessageSearchItem(
            messageId = messageId,
            channelId = channelId,
            authorId = authorId,
            content = content,
            highlights = highlight,
            type = type.toMessageType(),
            hasAttachments = hasAttachments,
            isPinned = isPinned,
            createdAt = createdAt,
        )
    }

    @Serializable
    private data class SlashCommandRequest(
        val command: String,
        val payload: GithubIssuePayload,
    )

    @Serializable
    private data class GithubIssuePayload(
        val projectId: Long,
        val title: String,
        val body: String? = null,
    )

    @Serializable
    private data class QueuedResponse(val queued: Boolean)

    @Serializable
    private data class DmResponse(
        val channelId: Long,
        val otherUserId: Long? = null,
        val unreadCount: Int = 0,
        val lastMessage: DmLastMessageResponse? = null,
    ) {
        fun toDomain() = DirectMessageConversation(
            channelId = channelId,
            otherUserId = otherUserId,
            unreadCount = unreadCount,
            lastMessage = lastMessage?.toDomain(),
        )
    }

    @Serializable
    private data class DmLastMessageResponse(
        val messageId: String,
        val authorId: Long,
        val content: String,
        val type: String,
        val createdAt: String,
    ) {
        fun toDomain() = DirectMessagePreview(
            messageId = messageId,
            authorId = authorId,
            content = content,
            type = type.toMessageType(),
            createdAt = createdAt,
        )
    }

    @Serializable
    private data class MessageResponse(
        @SerialName("_id")
        val mongoId: String? = null,
        val id: String? = null,
        val teamId: Long? = null,
        val projectId: Long? = null,
        val channelId: Long,
        val authorId: Long,
        val content: String,
        val parentMessageId: String? = null,
        val type: String = "TEXT",
        val attachments: List<AttachmentResponse> = emptyList(),
        val reactions: List<ReactionResponse> = emptyList(),
        val isEdited: Boolean = false,
        val isPinned: Boolean = false,
        val clientMessageId: String? = null,
        val mentions: List<Long> = emptyList(),
        val mentionedMessage: MentionedMessageResponse? = null,
        val fileUrl: String? = null,
        val fileName: String? = null,
        val fileSize: Long? = null,
        val createdAt: String? = null,
        val updatedAt: String? = null,
    ) {
        fun toDomain(): ChatMessage {
            val domainAttachments = attachments.map(AttachmentResponse::toDomain)
                .ifEmpty {
                    fileUrl?.let {
                        listOf(
                            ChatAttachment(
                                name = fileName.orEmpty(),
                                url = it,
                                size = fileSize ?: 0,
                                mimeType = "application/octet-stream",
                            ),
                        )
                    }.orEmpty()
                }
            val firstAttachment = domainAttachments.firstOrNull()
            return ChatMessage(
                id = id ?: mongoId.orEmpty(),
                teamId = teamId,
                projectId = projectId,
                channelId = channelId,
                authorId = authorId,
                content = content,
                parentMessageId = parentMessageId,
                type = type.toMessageType(),
                fileUrl = fileUrl ?: firstAttachment?.url,
                fileName = fileName ?: firstAttachment?.name,
                fileSize = fileSize ?: firstAttachment?.size,
                createdAt = createdAt,
                attachments = domainAttachments,
                reactions = reactions.map(ReactionResponse::toDomain),
                isEdited = isEdited,
                isPinned = isPinned,
                clientMessageId = clientMessageId,
                mentions = mentions,
                mentionedMessage = mentionedMessage?.toDomain(),
                updatedAt = updatedAt,
            )
        }
    }

    @Serializable
    private data class AttachmentResponse(
        val name: String,
        val url: String,
        val size: Long,
        val mimeType: String,
    ) {
        fun toDomain() = ChatAttachment(name, url, size, mimeType)
    }

    @Serializable
    private data class ReactionResponse(
        val emoji: String,
        val count: Int? = null,
        val myReaction: Boolean = false,
        val userIds: List<Long> = emptyList(),
    ) {
        fun toDomain() = ChatReaction(
            emoji = emoji,
            count = count ?: userIds.size,
            myReaction = myReaction,
        )
    }

    @Serializable
    private data class MentionedMessageResponse(
        @SerialName("_id")
        val mongoId: String? = null,
        val id: String? = null,
        val authorId: Long,
        val content: String,
        val type: String,
        val createdAt: String? = null,
    ) {
        fun toDomain() = MentionedChatMessage(
            id = id ?: mongoId.orEmpty(),
            authorId = authorId,
            content = content,
            type = type.toMessageType(),
            createdAt = createdAt,
        )
    }

    private suspend inline fun <reified T> HttpResponse.bodyPayload(): T {
        val response = body<JsonElement>()
        val payload = (response as? JsonObject)
            ?.get("data")
            ?.takeUnless { it is JsonNull }
            ?: response
        return responseJson.decodeFromJsonElement(payload)
    }

    private companion object {
        val responseJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}

private fun String.toMessageType(): MessageType = when (uppercase()) {
    "TEXT" -> MessageType.Text
    "FILE" -> MessageType.File
    "SYSTEM" -> MessageType.System
    else -> MessageType.Unknown
}

private fun MessageType.toApiValue(): String? = when (this) {
    MessageType.Text -> "TEXT"
    MessageType.File -> "FILE"
    MessageType.System -> "SYSTEM"
    MessageType.Unknown -> null
}
