package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.ChatMessage
import com.cowork.desktop.client.domain.model.MessageType
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class JvmChatSocket : ChatSocket {

    private var socket: Socket? = null
    private var messageCallback: ((ChatMessage) -> Unit)? = null

    override val isConnected: Boolean
        get() = socket?.connected() == true

    override fun connect(wsBaseUrl: String, token: String, onMessage: (ChatMessage) -> Unit) {
        messageCallback = onMessage
        val opts = IO.Options.builder()
            .setPath("/chat-ws")
            .setAuth(mapOf("token" to token))
            .build()
        socket = IO.socket("$wsBaseUrl/chat", opts).also { s ->
            s.on("message") { args ->
                val json = args.firstOrNull() as? JSONObject ?: return@on
                parseMessage(json)?.let { onMessage(it) }
            }
            s.connect()
        }
    }

    override fun disconnect() {
        socket?.disconnect()
        socket = null
        messageCallback = null
    }

    override fun joinChannel(channelId: Long) {
        socket?.emit("join", JSONObject().put("channelId", channelId))
    }

    override fun leaveChannel(channelId: Long) {
        socket?.emit("leave", JSONObject().put("channelId", channelId))
    }

    private fun parseMessage(json: JSONObject): ChatMessage? = runCatching {
        ChatMessage(
            id = json.optString("_id").ifEmpty { json.optString("id") },
            teamId = json.optLong("teamId"),
            projectId = if (json.isNull("projectId")) null else json.optLong("projectId"),
            channelId = json.optLong("channelId"),
            authorId = json.optLong("authorId"),
            content = json.optString("content"),
            parentMessageId = json.optString("parentMessageId").takeIf { it.isNotEmpty() },
            type = when (json.optString("type").lowercase()) {
                "file" -> MessageType.File
                else -> MessageType.Text
            },
            fileUrl = json.optString("fileUrl").takeIf { it.isNotEmpty() },
            fileName = json.optString("fileName").takeIf { it.isNotEmpty() },
            fileSize = if (json.isNull("fileSize")) null else json.optLong("fileSize"),
            createdAt = json.optString("createdAt").takeIf { it.isNotEmpty() },
        )
    }.getOrNull()
}
