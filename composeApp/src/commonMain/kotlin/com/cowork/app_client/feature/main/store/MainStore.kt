package com.cowork.app_client.feature.main.store

import com.arkivanov.mvikotlin.core.store.Store
import com.cowork.app_client.domain.model.Channel
import com.cowork.app_client.domain.model.ChannelType
import com.cowork.app_client.domain.model.ChatMessage
import com.cowork.app_client.domain.model.TeamSummary
import com.cowork.app_client.feature.main.store.MainStore.Intent
import com.cowork.app_client.feature.main.store.MainStore.Label
import com.cowork.app_client.feature.main.store.MainStore.State

interface MainStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object Reload : Intent
        data class SelectTeam(val teamId: Long) : Intent
        data class SelectChannel(val channelId: Long) : Intent
        data object OpenCreateTeam : Intent
        data object CloseCreateTeam : Intent
        data class ChangeCreateTeamName(val name: String) : Intent
        data class ChangeCreateTeamDescription(val description: String) : Intent
        data object SubmitCreateTeam : Intent
        data object OpenCreateChannel : Intent
        data object CloseCreateChannel : Intent
        data class ChangeCreateChannelName(val name: String) : Intent
        data class ChangeCreateChannelNotice(val notice: String) : Intent
        data class ChangeCreateChannelType(val type: ChannelType) : Intent
        data object SubmitCreateChannel : Intent
    }

    data class State(
        val teams: List<TeamSummary> = emptyList(),
        val selectedTeamId: Long? = null,
        val channels: List<Channel> = emptyList(),
        val selectedChannelId: Long? = null,
        val messages: List<ChatMessage> = emptyList(),
        val isLoadingTeams: Boolean = false,
        val isLoadingChannels: Boolean = false,
        val isLoadingMessages: Boolean = false,
        val chatDraft: String = "",
        val isCreateTeamOpen: Boolean = false,
        val createTeamName: String = "",
        val createTeamDescription: String = "",
        val isCreatingTeam: Boolean = false,
        val isCreateChannelOpen: Boolean = false,
        val createChannelName: String = "",
        val createChannelNotice: String = "",
        val createChannelType: ChannelType = ChannelType.Text,
        val isCreatingChannel: Boolean = false,
        val error: String? = null,
    ) {
        val selectedTeam: TeamSummary?
            get() = teams.firstOrNull { it.id == selectedTeamId }

        val selectedChannel: Channel?
            get() = channels.firstOrNull { it.id == selectedChannelId }

        val canSubmitTeam: Boolean
            get() = createTeamName.isNotBlank() && !isCreatingTeam

        val canSubmitChannel: Boolean
            get() = selectedTeamId != null && createChannelName.isNotBlank() && !isCreatingChannel
    }

    sealed interface Label
}
