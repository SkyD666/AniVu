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

object ProxyTypePreference : BasePreference<String> {
    private const val PROXY_TYPE = "proxyType"

    const val HTTP = "HTTP"
    const val SOCKS4 = "Socks4"
    const val SOCKS5 = "Socks5"

    override val default = HTTP

    val key = stringPreferencesKey(PROXY_TYPE)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default
}
