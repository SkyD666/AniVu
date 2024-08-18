package com.skyd.anivu.model.preference.player

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PlayerMaxCacheSizePreference : BasePreference<Long> {
    private const val PLAYER_MAX_CACHE_SIZE = "playerMaxCacheSize"
    override val default = 10L * 1024 * 1024    // 10 MB

    val key = longPreferencesKey(PLAYER_MAX_CACHE_SIZE)

    fun put(context: Context, scope: CoroutineScope, value: Long) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Long = preferences[key] ?: default
}