package com.skyd.anivu.model.preference.appearance.search

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SearchListTonalElevationPreference : BasePreference<Float> {
    private const val SEARCH_LIST_TONAL_ELEVATION = "searchListTonalElevation"
    override val default = 2f

    val key = floatPreferencesKey(SEARCH_LIST_TONAL_ELEVATION)

    fun put(context: Context, scope: CoroutineScope, value: Float) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Float = preferences[key] ?: default
}