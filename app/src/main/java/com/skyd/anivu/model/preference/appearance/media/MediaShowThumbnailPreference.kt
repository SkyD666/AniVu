package com.skyd.anivu.model.preference.appearance.media

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object MediaShowThumbnailPreference : BasePreference<Boolean> {
    private const val MEDIA_SHOW_THUMBNAIL = "mediaShowThumbnail"
    override val default = true

    val key = booleanPreferencesKey(MEDIA_SHOW_THUMBNAIL)

    fun put(context: Context, scope: CoroutineScope, value: Boolean) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Boolean = preferences[key] ?: default
}