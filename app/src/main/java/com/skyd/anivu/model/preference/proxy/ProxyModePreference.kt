package com.skyd.anivu.model.preference.proxy

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

object ProxyModePreference : BasePreference<String> {
    private const val PROXY_MODE = "proxyMode"

    const val AUTO_MODE = "Auto"
    const val MANUAL_MODE = "Manual"

    override val default = AUTO_MODE

    val key = stringPreferencesKey(PROXY_MODE)

    val values = listOf(AUTO_MODE, MANUAL_MODE)

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
        AUTO_MODE -> context.getString(R.string.proxy_mode_auto)
        MANUAL_MODE -> context.getString(R.string.proxy_mode_manual)
        else -> context.getString(R.string.unknown)
    }
}
