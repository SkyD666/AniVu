package com.skyd.anivu.model.preference.data.medialib

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object MediaLibLocationPreference : BasePreference<String> {
    private const val MEDIA_LIB_LOCATION = "mediaLibLocation"

    override val default: String = Const.VIDEO_DIR.path

    val key = stringPreferencesKey(MEDIA_LIB_LOCATION)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default
}
