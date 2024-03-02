package com.skyd.anivu.model.preference.proxy

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ProxyPasswordPreference : BasePreference<String> {
    private const val PROXY_PASSWORD = "proxyPassword"

    override val default = ""

    val key = stringPreferencesKey(PROXY_PASSWORD)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default
}
