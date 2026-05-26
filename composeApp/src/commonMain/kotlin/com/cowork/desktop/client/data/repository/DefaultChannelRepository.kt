package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.ChannelApi
import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelMember
import com.cowork.desktop.client.domain.model.ChannelType

class DefaultChannelRepository(
    private val authRepository: AuthRepository,
    private val channelApi: ChannelApi,
) : ChannelRepository {

    override suspend fun getTeamChannels(teamId: Long): List<Channel> =
        authorized { channelApi.getTeamChannels(it, teamId) }

    override suspend fun getChannel(channelId: Long): Channel =
        authorized { channelApi.getChannel(it, channelId) }

    override suspend fun createChannel(
        teamId: Long,
        type: ChannelType,
        name: String,
        description: String?,
        isPrivate: Boolean,
    ): Channel =
        authorized { channelApi.createChannel(it, teamId, type, name, description, isPrivate) }

    override suspend fun updateChannel(
        channelId: Long,
        name: String?,
        description: String?,
        isPrivate: Boolean?,
        projectId: Long?,
    ): Channel =
        authorized { channelApi.updateChannel(it, channelId, name, description, isPrivate, projectId) }

    override suspend fun reorderChannels(teamId: Long, orderedChannelIds: List<Long>): List<Channel> =
        authorized { channelApi.reorderChannels(it, teamId, orderedChannelIds) }

    override suspend fun deleteChannel(channelId: Long) =
        authorized { channelApi.deleteChannel(it, channelId) }

    override suspend fun getMembers(channelId: Long): List<ChannelMember> =
        authorized { channelApi.getMembers(it, channelId) }

    override suspend fun addMember(channelId: Long, userId: Long): ChannelMember =
        authorized { channelApi.addMember(it, channelId, userId) }

    override suspend fun removeMember(channelId: Long, memberId: Long) =
        authorized { channelApi.removeMember(it, channelId, memberId) }

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.authorized(block)
}
