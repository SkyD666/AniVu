package com.skyd.anivu.model.preference.appearance.article

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ArticleTopBarTonalElevationPreference : BasePreference<Float> {
    private const val ARTICLE_TOP_BAR_TONAL_ELEVATION = "articleTopBarTonalElevation"
    override val default = 2f

    val key = floatPreferencesKey(ARTICLE_TOP_BAR_TONAL_ELEVATION)

    fun put(context: Context, scope: CoroutineScope, value: Float) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Float = preferences[key] ?: default
}