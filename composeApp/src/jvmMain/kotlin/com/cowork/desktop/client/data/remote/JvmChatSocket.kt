package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.ChatAttachment
import com.cowork.desktop.client.domain.model.ChatMessage
import com.cowork.desktop.client.domain.model.ChatReaction
import com.cowork.desktop.client.domain.model.MentionedChatMessage
import com.cowork.desktop.client.domain.model.MessageType
import com.cowork.desktop.client.data.network.CoworkPinnedTrustManager
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.security.SecureRandom
import javax.net.ssl.SSLContext

class JvmChatSocket : ChatSocket {

    private var socket: Socket? = null
    private var eventCallback: ((ChatSocketEvent) -> Unit)? = null
    private var currentUserId: Long? = null
    private val joinedChannelIds = ConcurrentHashMap.newKeySet<Long>()
    private val joinedTeamIds = ConcurrentHashMap.newKeySet<Long>()

    override val isConnected: Boolean
        get() = socket?.connected() == true

    override fun connectWithEvents(
        wsBaseUrl: String,
        token: String,
        onEvent: (ChatSocketEvent) -> Unit,
    ) {
        disposeSocket()
        eventCallback = onEvent
        currentUserId = parseJwtSubject(token)

        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(CoworkPinnedTrustManager), SecureRandom())
        }
        val socketHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, CoworkPinnedTrustManager)
            .build()
        IO.setDefaultOkHttpCallFactory(socketHttpClient)
        IO.setDefaultOkHttpWebSocketFactory(socketHttpClient)

        val opts = IO.Options.builder()
            .setForceNew(true)
            .setPath("/ws/chat")
            .setAuth(mapOf("token" to token))
            .build()

        val nextSocket = IO.socket("${wsBaseUrl.trimEnd('/')}/chat", opts)
        socket = nextSocket
        registerListeners(nextSocket)
        nextSocket.connect()
    }

    override fun disconnect() {
        val callback = eventCallback
        disposeSocket()
        joinedChannelIds.clear()
        joinedTeamIds.clear()
        currentUserId = null
        eventCallback = null
        callback?.invoke(ChatSocketEvent.Disconnected("io client disconnect"))
    }

    override fun joinChannel(channelId: Long) {
        joinedChannelIds += channelId
        socket.takeIf { it?.connected() == true }?.emitChannel("join", channelId)
    }

    override fun leaveChannel(channelId: Long) {
        joinedChannelIds -= channelId
        socket.takeIf { it?.connected() == true }?.emitChannel("leave", channelId)
    }

    override fun joinTeam(teamId: Long) {
        joinedTeamIds += teamId
        socket.takeIf { it?.connected() == true }?.emitTeam("join:team", teamId)
    }

    override fun leaveTeam(teamId: Long) {
        joinedTeamIds -= teamId
        socket.takeIf { it?.connected() == true }?.emitTeam("leave:team", teamId)
    }

    override fun startTyping(channelId: Long) {
        socket.takeIf { it?.connected() == true }?.emitChannel("typing:start", channelId)
    }

    override fun stopTyping(channelId: Long) {
        socket.takeIf { it?.connected() == true }?.emitChannel("typing:stop", channelId)
    }

    private fun registerListeners(target: Socket) {
        target.on(Socket.EVENT_CONNECT) {
            if (socket !== target) return@on
            joinedChannelIds.forEach { target.emitChannel("join", it) }
            joinedTeamIds.forEach { target.emitTeam("join:team", it) }
            emit(target, ChatSocketEvent.Connected)
        }
        target.on(Socket.EVENT_DISCONNECT) { args ->
            emit(target, ChatSocketEvent.Disconnected(args.firstOrNull()?.toString()))
        }
        target.on(Socket.EVENT_CONNECT_ERROR) { args ->
            emit(
                target,
                ChatSocketEvent.ConnectionError(args.firstOrNull().toErrorMessage("연결에 실패했습니다.")),
            )
        }
        target.on("error") { args ->
            emit(target, ChatSocketEvent.ServerError(args.firstOrNull().toErrorMessage("소켓 오류가 발생했습니다.")))
        }
        target.on("exception") { args ->
            emit(target, ChatSocketEvent.ServerError(args.firstOrNull().toErrorMessage("서버가 요청을 거부했습니다.")))
        }

        target.onJson("message") { json ->
            parseMessage(json)?.let { emit(target, ChatSocketEvent.MessageReceived(it)) }
        }
        target.onJson("message:edited") { json ->
            val messageId = json.stringOrNull("messageId") ?: return@onJson
            emit(
                target,
                ChatSocketEvent.MessageEdited(
                    messageId = messageId,
                    content = json.optString("content"),
                    editedAt = json.stringOrNull("editedAt"),
                ),
            )
        }
        target.onJson("message:deleted") { json ->
            json.stringOrNull("messageId")
                ?.let { emit(target, ChatSocketEvent.MessageDeleted(it)) }
        }
        target.onJson("message:pinned") { json ->
            val messageId = json.stringOrNull("messageId") ?: return@onJson
            val channelId = json.longOrNull("channelId") ?: return@onJson
            emit(target, ChatSocketEvent.MessagePinned(messageId, channelId))
        }
        target.onJson("message:unpinned") { json ->
            val messageId = json.stringOrNull("messageId") ?: return@onJson
            val channelId = json.longOrNull("channelId") ?: return@onJson
            emit(target, ChatSocketEvent.MessageUnpinned(messageId, channelId))
        }
        target.onJson("message:reaction:added") { json ->
            parseReactionEvent(json, added = true)?.let { emit(target, it) }
        }
        target.onJson("message:reaction:removed") { json ->
            parseReactionEvent(json, added = false)?.let { emit(target, it) }
        }
        target.onJson("typing") { json ->
            val channelId = json.longOrNull("channelId") ?: return@onJson
            val userId = json.longOrNull("userId") ?: return@onJson
            emit(target, ChatSocketEvent.Typing(channelId, userId, json.optBoolean("isTyping")))
        }

        target.onJson("channel:created") { json ->
            parseChannel(json)?.let { emit(target, ChatSocketEvent.ChannelCreated(it)) }
        }
        target.onJson("channel:updated") { json ->
            parseChannel(json)?.let { emit(target, ChatSocketEvent.ChannelUpdated(it)) }
        }
        target.onJson("channel:deleted") { json ->
            val channelId = json.longOrNull("channelId") ?: return@onJson
            val teamId = json.longOrNull("teamId") ?: return@onJson
            emit(target, ChatSocketEvent.ChannelDeleted(channelId, teamId))
        }

        target.onJson("project:created") { json ->
            parseProject(json)?.let { emit(target, ChatSocketEvent.ProjectCreated(it)) }
        }
        target.onJson("project:updated") { json ->
            parseProject(json)?.let { emit(target, ChatSocketEvent.ProjectUpdated(it)) }
        }
        target.onJson("project:deleted") { json ->
            val projectId = json.longOrNull("projectId") ?: return@onJson
            val teamId = json.longOrNull("teamId") ?: return@onJson
            emit(target, ChatSocketEvent.ProjectDeleted(projectId, teamId))
        }

        target.onJson("member:joined") { json ->
            val channelId = json.longOrNull("channelId") ?: return@onJson
            val userId = json.longOrNull("userId") ?: return@onJson
            emit(
                target,
                ChatSocketEvent.MemberJoined(
                    channelId = channelId,
                    teamId = json.longOrNull("teamId"),
                    userId = userId,
                    role = json.stringOrNull("role"),
                ),
            )
        }
        target.onJson("member:left") { json ->
            val channelId = json.longOrNull("channelId") ?: return@onJson
            val userId = json.longOrNull("userId") ?: return@onJson
            emit(
                target,
                ChatSocketEvent.MemberLeft(
                    channelId = channelId,
                    teamId = json.longOrNull("teamId"),
                    userId = userId,
                ),
            )
        }
        target.onJson("member:role:updated") { json ->
            val channelId = json.longOrNull("channelId") ?: return@onJson
            val userId = json.longOrNull("userId") ?: return@onJson
            emit(
                target,
                ChatSocketEvent.MemberRoleUpdated(
                    channelId = channelId,
                    teamId = json.longOrNull("teamId"),
                    userId = userId,
                    role = json.stringOrNull("role"),
                ),
            )
        }
        target.onJson("channel:unread:updated") { json ->
            val channelId = json.longOrNull("channelId") ?: return@onJson
            emit(
                target,
                ChatSocketEvent.ChannelUnreadUpdated(
                    channelId = channelId,
                    unreadCount = json.optInt("unreadCount"),
                ),
            )
        }
    }

    private fun parseMessage(json: JSONObject): ChatMessage? = runCatching {
        val attachments = json.arrayOrEmpty("attachments").mapObjects { attachment ->
            ChatAttachment(
                name = attachment.optString("name"),
                url = attachment.optString("url"),
                size = attachment.optLong("size"),
                mimeType = attachment.optString("mimeType"),
            )
        }
        val legacyUrl = json.stringOrNull("fileUrl")
        val normalizedAttachments = attachments.ifEmpty {
            legacyUrl?.let {
                listOf(
                    ChatAttachment(
                        name = json.optString("fileName"),
                        url = it,
                        size = json.optLong("fileSize"),
                        mimeType = "application/octet-stream",
                    ),
                )
            }.orEmpty()
        }
        val reactions = json.arrayOrEmpty("reactions").mapObjects { reaction ->
            val userIds = reaction.arrayOrEmpty("userIds").mapLongs()
            ChatReaction(
                emoji = reaction.optString("emoji"),
                count = reaction.intOrNull("count") ?: userIds.size,
                myReaction = if (reaction.has("myReaction")) {
                    reaction.optBoolean("myReaction")
                } else {
                    currentUserId in userIds
                },
            )
        }
        val firstAttachment = normalizedAttachments.firstOrNull()

        ChatMessage(
            id = json.stringOrNull("_id") ?: json.stringOrNull("id") ?: return null,
            teamId = json.longOrNull("teamId"),
            projectId = json.longOrNull("projectId"),
            channelId = json.longOrNull("channelId") ?: return null,
            authorId = json.longOrNull("authorId") ?: return null,
            content = json.optString("content"),
            parentMessageId = json.stringOrNull("parentMessageId"),
            type = json.optString("type").toMessageType(),
            fileUrl = legacyUrl ?: firstAttachment?.url,
            fileName = json.stringOrNull("fileName") ?: firstAttachment?.name,
            fileSize = json.longOrNull("fileSize") ?: firstAttachment?.size,
            createdAt = json.stringOrNull("createdAt"),
            attachments = normalizedAttachments,
            reactions = reactions,
            isEdited = json.optBoolean("isEdited"),
            isPinned = json.optBoolean("isPinned"),
            clientMessageId = json.stringOrNull("clientMessageId"),
            mentions = json.arrayOrEmpty("mentions").mapLongs(),
            mentionedMessage = json.objectOrNull("mentionedMessage")?.toMentionedMessage(),
            updatedAt = json.stringOrNull("updatedAt"),
        )
    }.getOrNull()

    private fun parseReactionEvent(json: JSONObject, added: Boolean): ChatSocketEvent? {
        val messageId = json.stringOrNull("messageId") ?: return null
        val channelId = json.longOrNull("channelId") ?: return null
        val emoji = json.stringOrNull("emoji") ?: return null
        val userId = json.longOrNull("userId") ?: return null
        val count = json.optInt("count")
        return if (added) {
            ChatSocketEvent.ReactionAdded(messageId, channelId, emoji, userId, count)
        } else {
            ChatSocketEvent.ReactionRemoved(messageId, channelId, emoji, userId, count)
        }
    }

    private fun parseChannel(json: JSONObject): ChatSocketChannel? {
        val channelId = json.longOrNull("channelId") ?: return null
        val teamId = json.longOrNull("teamId") ?: return null
        return ChatSocketChannel(
            channelId = channelId,
            teamId = teamId,
            name = json.optString("name"),
            type = json.optString("type"),
            viewType = json.optString("viewType"),
            description = json.stringOrNull("description"),
            isPrivate = json.optBoolean("isPrivate"),
        )
    }

    private fun parseProject(json: JSONObject): ChatSocketProject? {
        val projectId = json.longOrNull("projectId") ?: return null
        val teamId = json.longOrNull("teamId") ?: return null
        return ChatSocketProject(
            projectId = projectId,
            teamId = teamId,
            name = json.optString("name"),
            description = json.stringOrNull("description"),
            status = json.optString("status"),
        )
    }

    private fun emit(target: Socket, event: ChatSocketEvent) {
        if (socket === target) {
            eventCallback?.invoke(event)
        }
    }

    private fun disposeSocket() {
        socket?.off()
        socket?.disconnect()
        socket = null
    }

    private fun parseJwtSubject(token: String): Long? = runCatching {
        val payload = token.split('.').getOrNull(1) ?: return null
        val decoded = Base64.getUrlDecoder().decode(payload)
        JSONObject(decoded.toString(Charsets.UTF_8)).opt("sub")?.toString()?.toLongOrNull()
    }.getOrNull()
}

private fun Socket.emitChannel(event: String, channelId: Long) {
    emit(event, JSONObject().put("channelId", channelId))
}

private fun Socket.emitTeam(event: String, teamId: Long) {
    emit(event, JSONObject().put("teamId", teamId))
}

private fun Socket.onJson(event: String, callback: (JSONObject) -> Unit) {
    on(event) { args ->
        (args.firstOrNull() as? JSONObject)?.let(callback)
    }
}

private fun JSONObject.stringOrNull(key: String): String? {
    if (!has(key) || isNull(key)) return null
    val value = opt(key)
    return when (value) {
        is JSONObject -> value.optString("\$oid").takeIf(String::isNotEmpty) ?: value.toString()
        else -> value?.toString()?.takeIf(String::isNotEmpty)
    }
}

private fun JSONObject.longOrNull(key: String): Long? {
    if (!has(key) || isNull(key)) return null
    val value = opt(key)
    return when (value) {
        is Number -> value.toLong()
        else -> value?.toString()?.toLongOrNull()
    }
}

private fun JSONObject.intOrNull(key: String): Int? {
    if (!has(key) || isNull(key)) return null
    val value = opt(key)
    return when (value) {
        is Number -> value.toInt()
        else -> value?.toString()?.toIntOrNull()
    }
}

private fun JSONObject.objectOrNull(key: String): JSONObject? {
    if (!has(key) || isNull(key)) return null
    return optJSONObject(key)
}

private fun JSONObject.arrayOrEmpty(key: String): JSONArray =
    optJSONArray(key) ?: JSONArray()

private fun JSONArray.mapLongs(): List<Long> = buildList {
    repeat(length()) { index ->
        val value = opt(index)
        when (value) {
            is Number -> add(value.toLong())
            else -> value?.toString()?.toLongOrNull()?.let(::add)
        }
    }
}

private inline fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> = buildList {
    repeat(length()) { index ->
        optJSONObject(index)?.let { add(transform(it)) }
    }
}

private fun JSONObject.toMentionedMessage(): MentionedChatMessage? {
    val id = stringOrNull("_id") ?: stringOrNull("id") ?: return null
    val authorId = longOrNull("authorId") ?: return null
    return MentionedChatMessage(
        id = id,
        authorId = authorId,
        content = optString("content"),
        type = optString("type").toMessageType(),
        createdAt = stringOrNull("createdAt"),
    )
}

private fun String.toMessageType(): MessageType = when (uppercase()) {
    "TEXT" -> MessageType.Text
    "FILE" -> MessageType.File
    "SYSTEM" -> MessageType.System
    else -> MessageType.Unknown
}

private fun Any?.toErrorMessage(fallback: String): String = when (this) {
    is JSONObject -> optString("message").takeIf(String::isNotEmpty) ?: toString()
    is Throwable -> message ?: toString()
    null -> fallback
    else -> toString().takeIf(String::isNotEmpty) ?: fallback
}
