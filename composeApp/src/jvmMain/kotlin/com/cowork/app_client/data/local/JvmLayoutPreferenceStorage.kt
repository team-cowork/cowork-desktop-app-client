package com.cowork.app_client.data.local

import java.util.prefs.Preferences

class JvmLayoutPreferenceStorage : LayoutPreferenceStorage {
    private val prefs: Preferences = Preferences.userRoot().node("com/cowork/app_client/layout")

    override fun getTeamRailWidth(): Float? =
        prefs.getFloat(KEY_TEAM_RAIL_WIDTH, Float.NaN).takeUnless { it.isNaN() }

    override fun getChannelPaneWidth(): Float? =
        prefs.getFloat(KEY_CHANNEL_PANE_WIDTH, Float.NaN).takeUnless { it.isNaN() }

    override fun saveTeamRailWidth(width: Float) {
        prefs.putFloat(KEY_TEAM_RAIL_WIDTH, width)
        prefs.flush()
    }

    override fun saveChannelPaneWidth(width: Float) {
        prefs.putFloat(KEY_CHANNEL_PANE_WIDTH, width)
        prefs.flush()
    }

    private companion object {
        const val KEY_TEAM_RAIL_WIDTH = "team_rail_width"
        const val KEY_CHANNEL_PANE_WIDTH = "channel_pane_width"
    }
}
