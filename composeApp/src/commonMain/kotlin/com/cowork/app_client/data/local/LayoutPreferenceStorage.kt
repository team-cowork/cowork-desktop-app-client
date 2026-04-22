package com.cowork.app_client.data.local

interface LayoutPreferenceStorage {
    fun getTeamRailWidth(): Float?
    fun getChannelPaneWidth(): Float?
    fun saveTeamRailWidth(width: Float)
    fun saveChannelPaneWidth(width: Float)
}
