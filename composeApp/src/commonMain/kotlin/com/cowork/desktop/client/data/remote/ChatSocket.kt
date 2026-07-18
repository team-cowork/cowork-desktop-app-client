package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.ChatMessage

interface ChatSocket {
    fun connect(wsBaseUrl: String, token: String, onMessage: (ChatMessage) -> Unit) {
        connectWithEvents(wsBaseUrl, token) { event ->
            if (event is ChatSocketEvent.MessageReceived) {
                onMessage(event.message)
            }
        }
    }

    fun connectWithEvents(
        wsBaseUrl: String,
        token: String,
        onEvent: (ChatSocketEvent) -> Unit,
    )

    fun disconnect()
    fun joinChannel(channelId: Long)
    fun leaveChannel(channelId: Long)
    fun joinTeam(teamId: Long)
    fun leaveTeam(teamId: Long)
    fun startTyping(channelId: Long)
    fun stopTyping(channelId: Long)
    val isConnected: Boolean
}

sealed interface ChatSocketEvent {
    data object Connected : ChatSocketEvent
    data class Disconnected(val reason: String?) : ChatSocketEvent
    data class ConnectionError(val message: String) : ChatSocketEvent
    data class ServerError(val message: String) : ChatSocketEvent

    data class MessageReceived(val message: ChatMessage) : ChatSocketEvent
    data class MessageEdited(
        val messageId: String,
        val content: String,
        val editedAt: String?,
    ) : ChatSocketEvent

    data class MessageDeleted(val messageId: String) : ChatSocketEvent
    data class MessagePinned(val messageId: String, val channelId: Long) : ChatSocketEvent
    data class MessageUnpinned(val messageId: String, val channelId: Long) : ChatSocketEvent

    data class ReactionAdded(
        val messageId: String,
        val channelId: Long,
        val emoji: String,
        val userId: Long,
        val count: Int,
    ) : ChatSocketEvent

    data class ReactionRemoved(
        val messageId: String,
        val channelId: Long,
        val emoji: String,
        val userId: Long,
        val count: Int,
    ) : ChatSocketEvent

    data class Typing(
        val channelId: Long,
        val userId: Long,
        val isTyping: Boolean,
    ) : ChatSocketEvent

    data class ChannelCreated(val channel: ChatSocketChannel) : ChatSocketEvent
    data class ChannelUpdated(val channel: ChatSocketChannel) : ChatSocketEvent
    data class ChannelDeleted(val channelId: Long, val teamId: Long) : ChatSocketEvent

    data class ProjectCreated(val project: ChatSocketProject) : ChatSocketEvent
    data class ProjectUpdated(val project: ChatSocketProject) : ChatSocketEvent
    data class ProjectDeleted(val projectId: Long, val teamId: Long) : ChatSocketEvent

    data class MemberJoined(
        val channelId: Long,
        val teamId: Long?,
        val userId: Long,
        val role: String?,
    ) : ChatSocketEvent

    data class MemberLeft(
        val channelId: Long,
        val teamId: Long?,
        val userId: Long,
    ) : ChatSocketEvent

    data class MemberRoleUpdated(
        val channelId: Long,
        val teamId: Long?,
        val userId: Long,
        val role: String?,
    ) : ChatSocketEvent

    data class ChannelUnreadUpdated(
        val channelId: Long,
        val unreadCount: Int,
    ) : ChatSocketEvent
}

data class ChatSocketChannel(
    val channelId: Long,
    val teamId: Long,
    val name: String,
    val type: String,
    val viewType: String,
    val description: String?,
    val isPrivate: Boolean,
)

data class ChatSocketProject(
    val projectId: Long,
    val teamId: Long,
    val name: String,
    val description: String?,
    val status: String,
)
