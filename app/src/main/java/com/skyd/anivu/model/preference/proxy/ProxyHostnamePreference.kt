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

object ProxyHostnamePreference : BasePreference<String> {
    private const val PROXY_HOSTNAME = "proxyHostname"

    override val default = "127.0.0.1"

    val key = stringPreferencesKey(PROXY_HOSTNAME)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default
}
