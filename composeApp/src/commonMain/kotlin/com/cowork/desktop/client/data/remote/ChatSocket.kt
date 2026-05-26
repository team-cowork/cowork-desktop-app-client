package com.cowork.desktop.client.data.remote

import com.cowork.desktop.client.domain.model.ChatMessage

interface ChatSocket {
    fun connect(wsBaseUrl: String, token: String, onMessage: (ChatMessage) -> Unit)
    fun disconnect()
    fun joinChannel(channelId: Long)
    fun leaveChannel(channelId: Long)
    val isConnected: Boolean
}
