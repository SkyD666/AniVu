package com.skyd.anivu.model.preference.appearance

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NavigationBarLabelPreference : BasePreference<String> {
    private const val NAVIGATION_BAR_LABEL = "navigationBarLabel"

    const val SHOW = "Show"
    const val SHOW_ON_ACTIVE = "ShowOnActive"
    const val NONE = "None"
    val values = arrayOf(SHOW, SHOW_ON_ACTIVE, NONE)

    override val default = SHOW

    val key = stringPreferencesKey(NAVIGATION_BAR_LABEL)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default

    fun toDisplayName(
        context: Context,
        value: String = context.dataStore.getOrDefault(this),
    ): String = when (value) {
        SHOW -> context.getString(R.string.navigation_bar_label_show)
        SHOW_ON_ACTIVE -> context.getString(R.string.navigation_bar_label_show_on_active)
        NONE -> context.getString(R.string.navigation_bar_label_none)
        else -> context.getString(R.string.unknown)
    }
}
