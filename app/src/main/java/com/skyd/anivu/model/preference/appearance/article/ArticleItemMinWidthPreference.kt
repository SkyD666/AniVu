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

object ArticleItemMinWidthPreference : BasePreference<Float> {
    private const val ARTICLE_ITEM_MIN_WIDTH = "articleItemMinWidth"
    override val default = 360f

    val key = floatPreferencesKey(ARTICLE_ITEM_MIN_WIDTH)

    fun put(context: Context, scope: CoroutineScope, value: Float) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Float = preferences[key] ?: default
}