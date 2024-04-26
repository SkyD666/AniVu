package com.skyd.anivu.model.preference.appearance

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.put
import com.skyd.anivu.ui.component.AniVuTextFieldStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object TextFieldStylePreference : BasePreference<String> {
    private const val TEXT_FIELD_STYLE = "textFieldStyle"

    val values = AniVuTextFieldStyle.entries.map { it.value }

    override val default = AniVuTextFieldStyle.Normal.value

    val key = stringPreferencesKey(TEXT_FIELD_STYLE)

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
        AniVuTextFieldStyle.Normal.value -> context.getString(R.string.normal_text_field_style)
        AniVuTextFieldStyle.Outlined.value -> context.getString(R.string.outlined_text_field_style)
        else -> context.getString(R.string.unknown)
    }
}
