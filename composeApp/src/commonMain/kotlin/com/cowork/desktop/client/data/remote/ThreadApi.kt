package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.Thread
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class ThreadApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getThreads(accessToken: String, channelId: Long, includeArchived: Boolean = false): List<Thread> {
        val result = mutableListOf<ThreadResponse>()
        var page = 0
        while (true) {
            val pageData = client.get("$baseUrl/channels/$channelId/threads") {
                bearerAuth(accessToken)
                parameter("includeArchived", includeArchived)
                parameter("size", PAGE_SIZE)
                parameter("page", page)
            }.bodyPayload<PageResponse<ThreadResponse>?>() ?: break
            result.addAll(pageData.content)
            if (page >= pageData.totalPages - 1) break
            page++
        }
        return result.map(ThreadResponse::toDomain)
    }

    suspend fun createThread(accessToken: String, channelId: Long, name: String, parentMessageId: String): Thread =
        client.post("$baseUrl/channels/$channelId/threads") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateThreadRequest(name = name, parentMessageId = parentMessageId))
        }.bodyPayload<ThreadResponse?>()?.toDomain()
            ?: error("스레드 생성 응답에 data가 없습니다")

    suspend fun updateThread(
        accessToken: String,
        channelId: Long,
        threadId: Long,
        name: String? = null,
        isArchived: Boolean? = null,
    ): Thread =
        client.patch("$baseUrl/channels/$channelId/threads/$threadId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(UpdateThreadRequest(name = name, isArchived = isArchived))
        }.bodyPayload<ThreadResponse?>()?.toDomain()
            ?: error("스레드 수정 응답에 data가 없습니다")

    private companion object {
        const val PAGE_SIZE = 50
    }

    @Serializable
    private data class CreateThreadRequest(val name: String, val parentMessageId: String)

    @Serializable
    private data class UpdateThreadRequest(val name: String?, val isArchived: Boolean?)

    @Serializable
    private data class ThreadResponse(
        val id: Long,
        val channelId: Long,
        val name: String,
        val parentMessageId: String,
        val isArchived: Boolean = false,
        val createdBy: Long,
    ) {
        fun toDomain(): Thread = Thread(
            id = id,
            channelId = channelId,
            name = name,
            parentMessageId = parentMessageId,
            isArchived = isArchived,
            createdBy = createdBy,
        )
    }

}
