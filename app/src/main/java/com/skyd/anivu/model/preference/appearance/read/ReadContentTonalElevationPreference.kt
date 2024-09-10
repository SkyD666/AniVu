package com.skyd.anivu.model.preference.appearance.read

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ReadContentTonalElevationPreference : BasePreference<Float> {
    private const val READ_CONTENT_TONAL_ELEVATION = "readContentTonalElevation"
    override val default = 0f

    val key = floatPreferencesKey(READ_CONTENT_TONAL_ELEVATION)

    fun put(context: Context, scope: CoroutineScope, value: Float) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Float = preferences[key] ?: default
}