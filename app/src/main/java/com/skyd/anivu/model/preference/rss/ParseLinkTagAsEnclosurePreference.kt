package com.skyd.anivu.model.preference.rss

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ParseLinkTagAsEnclosurePreference : BasePreference<Boolean> {
    private const val PARSE_LINK_TAG_AS_ENCLOSURE = "parseLinkTagAsEnclosure"
    override val default = true

    val key = booleanPreferencesKey(PARSE_LINK_TAG_AS_ENCLOSURE)

    fun put(context: Context, scope: CoroutineScope, value: Boolean) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Boolean = preferences[key] ?: default
}
