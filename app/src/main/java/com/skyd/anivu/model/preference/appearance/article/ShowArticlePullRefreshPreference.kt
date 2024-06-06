package com.skyd.anivu.model.preference.appearance.article

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ShowArticlePullRefreshPreference : BasePreference<Boolean> {
    private const val SHOW_ARTICLE_PULL_REFRESH = "showArticlePullRefresh"
    override val default = true

    val key = booleanPreferencesKey(SHOW_ARTICLE_PULL_REFRESH)

    fun put(context: Context, scope: CoroutineScope, value: Boolean) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Boolean = preferences[key] ?: default
}