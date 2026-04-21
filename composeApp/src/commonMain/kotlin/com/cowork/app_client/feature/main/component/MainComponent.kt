package com.cowork.app_client.feature.main.component

import com.cowork.app_client.feature.main.store.MainStore
import com.cowork.app_client.domain.model.ChannelType
import kotlinx.coroutines.flow.StateFlow

interface MainComponent {
    val state: StateFlow<MainStore.State>

    fun onReloadClick()
    fun onTeamClick(teamId: Long)
    fun onChannelClick(channelId: Long)
    fun onCreateTeamClick()
    fun onCreateTeamDismiss()
    fun onCreateTeamNameChange(name: String)
    fun onCreateTeamDescriptionChange(description: String)
    fun onCreateTeamSubmit()
    fun onCreateChannelClick()
    fun onCreateChannelDismiss()
    fun onCreateChannelNameChange(name: String)
    fun onCreateChannelNoticeChange(notice: String)
    fun onCreateChannelTypeChange(type: ChannelType)
    fun onCreateChannelSubmit()
}
