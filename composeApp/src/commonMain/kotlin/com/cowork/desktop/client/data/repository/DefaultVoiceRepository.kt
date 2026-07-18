package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.VoiceApi
import com.cowork.desktop.client.domain.model.LiveJoinResult
import com.cowork.desktop.client.domain.model.LiveStartResult
import com.cowork.desktop.client.domain.model.LiveStatus
import com.cowork.desktop.client.domain.model.VoiceChannelJoinResult
import com.cowork.desktop.client.domain.model.VoiceChannelParticipants
import com.cowork.desktop.client.domain.model.VoiceSession

class DefaultVoiceRepository(
    private val authRepository: AuthRepository,
    private val voiceApi: VoiceApi,
) : VoiceRepository {

    override suspend fun joinVoiceChannel(channelId: Long): VoiceChannelJoinResult =
        authRepository.authorized { voiceApi.joinVoiceChannel(it, channelId) }

    override suspend fun leaveVoiceChannel(channelId: Long) =
        authRepository.authorized { voiceApi.leaveVoiceChannel(it, channelId) }

    override suspend fun getVoiceChannelParticipants(channelId: Long): VoiceChannelParticipants =
        authRepository.authorized { voiceApi.getVoiceChannelParticipants(it, channelId) }

    override suspend fun getVoiceSession(sessionId: String): VoiceSession =
        authRepository.authorized { voiceApi.getVoiceSession(it, sessionId) }

    override suspend fun getLiveStatus(channelId: Long): LiveStatus =
        authRepository.authorized { voiceApi.getLiveStatus(it, channelId) }

    override suspend fun startLive(channelId: Long): LiveStartResult =
        authRepository.authorized { voiceApi.startLive(it, channelId) }

    override suspend fun joinLive(channelId: Long): LiveJoinResult =
        authRepository.authorized { voiceApi.joinLive(it, channelId) }

    override suspend fun leaveLive(channelId: Long) =
        authRepository.authorized { voiceApi.leaveLive(it, channelId) }
}
