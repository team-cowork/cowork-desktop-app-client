package com.cowork.app_client.data.local

import android.content.Context

class AndroidLayoutPreferenceStorage(context: Context) : LayoutPreferenceStorage {
    private val prefs = context.getSharedPreferences("cowork_layout", Context.MODE_PRIVATE)

    override fun getTeamRailWidth(): Float? =
        prefs.getFloat(KEY_TEAM_RAIL_WIDTH, Float.NaN).takeUnless { it.isNaN() }

    override fun getChannelPaneWidth(): Float? =
        prefs.getFloat(KEY_CHANNEL_PANE_WIDTH, Float.NaN).takeUnless { it.isNaN() }

    override fun saveTeamRailWidth(width: Float) {
        prefs.edit().putFloat(KEY_TEAM_RAIL_WIDTH, width).apply()
    }

    override fun saveChannelPaneWidth(width: Float) {
        prefs.edit().putFloat(KEY_CHANNEL_PANE_WIDTH, width).apply()
    }

    private companion object {
        const val KEY_TEAM_RAIL_WIDTH = "team_rail_width"
        const val KEY_CHANNEL_PANE_WIDTH = "channel_pane_width"
    }
}
