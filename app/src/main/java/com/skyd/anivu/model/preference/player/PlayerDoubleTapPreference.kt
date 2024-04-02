package com.skyd.anivu.model.preference.player

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PlayerDoubleTapPreference : BasePreference<String> {
    private const val PLAYER_DOUBLE_TAP = "playerDoubleTap"

    const val PAUSE_PLAY = "PausePlay"
    const val BACKWARD_FORWARD = "BackwardForward"
    const val BACKWARD_PAUSE_PLAY_FORWARD = "BackwardPausePlayForward"

    val values = arrayOf(PAUSE_PLAY, BACKWARD_FORWARD, BACKWARD_PAUSE_PLAY_FORWARD)

    override val default = PAUSE_PLAY

    val key = stringPreferencesKey(PLAYER_DOUBLE_TAP)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default

    fun toDisplayName(
        context: Context,
        value: String = context.dataStore.getOrDefault(this),
    ): String = when (value) {
        PAUSE_PLAY -> context.getString(R.string.player_pause)
        BACKWARD_FORWARD -> context.getString(R.string.player_backward_forward)
        BACKWARD_PAUSE_PLAY_FORWARD -> context.getString(R.string.player_backward_pause_forward)
        else -> context.getString(R.string.unknown)
    }
}
