package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelMember
import com.cowork.desktop.client.domain.model.ChannelType
import com.cowork.desktop.client.domain.model.SharedAccount
import com.cowork.desktop.client.domain.model.SharedAccountProvider

interface ChannelRepository {
    suspend fun getTeamChannels(teamId: Long): List<Channel>
    suspend fun getProjectChannels(projectId: Long): List<Channel>
    suspend fun searchChannels(teamId: Long, query: String): List<Channel>
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
        clearDescription: Boolean = false,
        updateProjectId: Boolean = false,
    ): Channel
    suspend fun deleteChannel(channelId: Long)
    suspend fun reorderChannels(teamId: Long, orderedChannelIds: List<Long>): List<Channel>
    suspend fun getMembers(channelId: Long): List<ChannelMember>
    suspend fun addMember(channelId: Long, userId: Long): ChannelMember
    suspend fun removeMember(channelId: Long, memberId: Long)
    suspend fun openDirectMessage(targetUserId: Long): Long
    suspend fun getSharedAccounts(channelId: Long): List<SharedAccount>
    suspend fun getSharedAccount(channelId: Long, accountId: Long): SharedAccount
    suspend fun createSharedAccount(
        channelId: Long,
        provider: SharedAccountProvider,
        providerLabel: String? = null,
        accountIdentifier: String? = null,
        credential: String? = null,
    ): SharedAccount
    suspend fun updateSharedAccount(
        channelId: Long,
        accountId: Long,
        providerLabel: String? = null,
        accountIdentifier: String? = null,
        credential: String? = null,
    ): SharedAccount
    suspend fun deleteSharedAccount(channelId: Long, accountId: Long)
    suspend fun copySharedAccountCredential(channelId: Long, accountId: Long): String
    suspend fun getSharedAccountOAuthUrl(channelId: Long, provider: SharedAccountProvider): String
}
