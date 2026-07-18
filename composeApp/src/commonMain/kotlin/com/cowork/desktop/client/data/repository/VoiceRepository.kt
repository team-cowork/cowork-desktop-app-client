package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.LiveJoinResult
import com.cowork.desktop.client.domain.model.LiveStartResult
import com.cowork.desktop.client.domain.model.LiveStatus
import com.cowork.desktop.client.domain.model.VoiceChannelJoinResult
import com.cowork.desktop.client.domain.model.VoiceChannelParticipants
import com.cowork.desktop.client.domain.model.VoiceSession

interface VoiceRepository {
    suspend fun joinVoiceChannel(channelId: Long): VoiceChannelJoinResult
    suspend fun leaveVoiceChannel(channelId: Long)
    suspend fun getVoiceChannelParticipants(channelId: Long): VoiceChannelParticipants
    suspend fun getVoiceSession(sessionId: String): VoiceSession
    suspend fun getLiveStatus(channelId: Long): LiveStatus
    suspend fun startLive(channelId: Long): LiveStartResult
    suspend fun joinLive(channelId: Long): LiveJoinResult
    suspend fun leaveLive(channelId: Long)
}
