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

object PlayerSeekOptionPreference : BasePreference<String> {
    private const val PLAYER_SEEK_OPTION = "playerSeekOption"

    const val FAST = "Fast"
    const val EXACT = "Exact"

    val values = arrayOf(FAST, EXACT)

    override val default = FAST

    val key = stringPreferencesKey(PLAYER_SEEK_OPTION)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default

    fun isPrecise(
        context: Context,
        value: String = context.dataStore.getOrDefault(this),
    ): Boolean = value == EXACT

    fun toDisplayName(
        context: Context,
        value: String = context.dataStore.getOrDefault(this),
    ): String = when (value) {
        FAST -> context.getString(R.string.player_seek_fast)
        EXACT -> context.getString(R.string.player_seek_exact)
        else -> context.getString(R.string.unknown)
    }
}
