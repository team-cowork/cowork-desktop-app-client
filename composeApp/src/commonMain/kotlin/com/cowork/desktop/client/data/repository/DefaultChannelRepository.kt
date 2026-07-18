package com.cowork.desktop.client.data.repository

import com.cowork.desktop.client.data.remote.ChannelApi
import com.cowork.desktop.client.domain.model.Channel
import com.cowork.desktop.client.domain.model.ChannelMember
import com.cowork.desktop.client.domain.model.ChannelType
import com.cowork.desktop.client.domain.model.SharedAccount
import com.cowork.desktop.client.domain.model.SharedAccountProvider

class DefaultChannelRepository(
    private val authRepository: AuthRepository,
    private val channelApi: ChannelApi,
) : ChannelRepository {

    override suspend fun getTeamChannels(teamId: Long): List<Channel> =
        authorized { channelApi.getTeamChannels(it, teamId) }

    override suspend fun getProjectChannels(projectId: Long): List<Channel> =
        authorized { channelApi.getProjectChannels(it, projectId) }

    override suspend fun searchChannels(teamId: Long, query: String): List<Channel> =
        authorized { channelApi.searchChannels(it, teamId, query) }

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
        clearDescription: Boolean,
        updateProjectId: Boolean,
    ): Channel =
        authorized {
            channelApi.updateChannel(
                it,
                channelId,
                name,
                description,
                isPrivate,
                projectId,
                clearDescription,
                updateProjectId,
            )
        }

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

    override suspend fun openDirectMessage(targetUserId: Long): Long =
        authorized { channelApi.openDirectMessage(it, targetUserId) }

    override suspend fun getSharedAccounts(channelId: Long): List<SharedAccount> =
        authorized { channelApi.getSharedAccounts(it, channelId) }

    override suspend fun getSharedAccount(channelId: Long, accountId: Long): SharedAccount =
        authorized { channelApi.getSharedAccount(it, channelId, accountId) }

    override suspend fun createSharedAccount(
        channelId: Long,
        provider: SharedAccountProvider,
        providerLabel: String?,
        accountIdentifier: String?,
        credential: String?,
    ): SharedAccount = authorized {
        channelApi.createSharedAccount(it, channelId, provider, providerLabel, accountIdentifier, credential)
    }

    override suspend fun updateSharedAccount(
        channelId: Long,
        accountId: Long,
        providerLabel: String?,
        accountIdentifier: String?,
        credential: String?,
    ): SharedAccount = authorized {
        channelApi.updateSharedAccount(it, channelId, accountId, providerLabel, accountIdentifier, credential)
    }

    override suspend fun deleteSharedAccount(channelId: Long, accountId: Long) =
        authorized { channelApi.deleteSharedAccount(it, channelId, accountId) }

    override suspend fun copySharedAccountCredential(channelId: Long, accountId: Long): String =
        authorized { channelApi.copySharedAccountCredential(it, channelId, accountId) }

    override suspend fun getSharedAccountOAuthUrl(channelId: Long, provider: SharedAccountProvider): String =
        authorized { channelApi.getSharedAccountOAuthUrl(it, channelId, provider) }

    private suspend fun <T> authorized(block: suspend (String) -> T): T =
        authRepository.authorized(block)
}
