package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.ChatMessage
import com.cowork.desktop.client.domain.model.MessageType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChatSocketCompatibilityTest {

    @Test
    fun legacyConnectCallbackReceivesOnlyMessageEvents() {
        val socket = FakeChatSocket()
        var received: ChatMessage? = null
        socket.connect("https://example.test", "token") { received = it }

        socket.dispatch(ChatSocketEvent.ServerError("ignored by legacy callback"))
        assertNull(received)

        val message = testMessage()
        socket.dispatch(ChatSocketEvent.MessageReceived(message))
        assertEquals(message, received)
    }

    @Test
    fun chatMessageSupportsDmAndNewServerFields() {
        val message = testMessage()

        assertNull(message.teamId)
        assertEquals(MessageType.System, message.type)
        assertEquals(true, message.isEdited)
        assertEquals(true, message.isPinned)
        assertEquals(listOf(7L, 9L), message.mentions)
    }

    private fun testMessage() = ChatMessage(
        id = "message-id",
        teamId = null,
        projectId = null,
        channelId = 10,
        authorId = 20,
        content = "system message",
        parentMessageId = null,
        type = MessageType.System,
        fileUrl = null,
        fileName = null,
        fileSize = null,
        createdAt = "2026-07-18T00:00:00Z",
        isEdited = true,
        isPinned = true,
        mentions = listOf(7, 9),
    )

    private class FakeChatSocket : ChatSocket {
        private var listener: ((ChatSocketEvent) -> Unit)? = null

        override fun connectWithEvents(
            wsBaseUrl: String,
            token: String,
            onEvent: (ChatSocketEvent) -> Unit,
        ) {
            listener = onEvent
        }

        fun dispatch(event: ChatSocketEvent) {
            listener?.invoke(event)
        }

        override fun disconnect() = Unit
        override fun joinChannel(channelId: Long) = Unit
        override fun leaveChannel(channelId: Long) = Unit
        override fun joinTeam(teamId: Long) = Unit
        override fun leaveTeam(teamId: Long) = Unit
        override fun startTyping(channelId: Long) = Unit
        override fun stopTyping(channelId: Long) = Unit
        override val isConnected: Boolean = false
    }
}
