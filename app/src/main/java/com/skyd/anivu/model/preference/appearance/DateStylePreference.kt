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

object DateStylePreference : BasePreference<String> {
    private const val DATE_STYLE = "dateStyle"

    const val RELATIVE = "Relative"
    private const val FULL = "Full"
    val values = arrayOf(RELATIVE, FULL)

    override val default = RELATIVE

    val key = stringPreferencesKey(DATE_STYLE)

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
        RELATIVE -> context.getString(R.string.date_style_relative)
        FULL -> context.getString(R.string.date_style_full)
        else -> context.getString(R.string.unknown)
    }
}
