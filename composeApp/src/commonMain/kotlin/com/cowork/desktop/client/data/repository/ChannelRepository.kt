package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelMember
import com.cowork.desktop.client.domain.model.ChannelType

interface ChannelRepository {
    suspend fun getTeamChannels(teamId: Long): List<Channel>
    suspend fun getChannel(channelId: Long): Channel
    suspend fun createChannel(
        teamId: Long,
        type: ChannelType,
        name: String,
        description: String?,
        isPrivate: Boolean,
    ): Channel
    suspend fun updateChannel(
        channelId: Long,
        name: String? = null,
        description: String? = null,
        isPrivate: Boolean? = null,
        projectId: Long? = null,
    ): Channel
    suspend fun deleteChannel(channelId: Long)
    suspend fun reorderChannels(teamId: Long, orderedChannelIds: List<Long>): List<Channel>
    suspend fun getMembers(channelId: Long): List<ChannelMember>
    suspend fun addMember(channelId: Long, userId: Long): ChannelMember
    suspend fun removeMember(channelId: Long, memberId: Long)
}
