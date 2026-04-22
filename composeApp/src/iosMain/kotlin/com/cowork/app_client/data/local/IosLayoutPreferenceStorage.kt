package com.cowork.app_client.data.local

import platform.Foundation.NSUserDefaults

class IosLayoutPreferenceStorage : LayoutPreferenceStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getTeamRailWidth(): Float? =
        defaults.objectForKey(KEY_TEAM_RAIL_WIDTH)?.let { defaults.floatForKey(KEY_TEAM_RAIL_WIDTH) }

    override fun getChannelPaneWidth(): Float? =
        defaults.objectForKey(KEY_CHANNEL_PANE_WIDTH)?.let { defaults.floatForKey(KEY_CHANNEL_PANE_WIDTH) }

    override fun saveTeamRailWidth(width: Float) {
        defaults.setFloat(width, KEY_TEAM_RAIL_WIDTH)
    }

    override fun saveChannelPaneWidth(width: Float) {
        defaults.setFloat(width, KEY_CHANNEL_PANE_WIDTH)
    }

    private companion object {
        const val KEY_TEAM_RAIL_WIDTH = "cowork_team_rail_width"
        const val KEY_CHANNEL_PANE_WIDTH = "cowork_channel_pane_width"
    }
}
