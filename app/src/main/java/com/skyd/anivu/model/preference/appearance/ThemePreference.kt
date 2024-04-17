package com.skyd.anivu.model.preference.appearance

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.material.color.DynamicColors
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ThemePreference : BasePreference<String> {
    private const val THEME = "theme"

    const val DYNAMIC = "Dynamic"
    const val PINK = "Pink"
    const val GREEN = "Green"
    const val BLUE = "Blue"
    const val YELLOW = "Yellow"
    const val PURPLE = "Purple"

    val basicValues = arrayOf(PINK, GREEN, BLUE, YELLOW, PURPLE)

    val values: Array<String>
        get() {
            return if (DynamicColors.isDynamicColorAvailable()) arrayOf(DYNAMIC, *basicValues)
            else basicValues
        }

    override val default = if (DynamicColors.isDynamicColorAvailable()) DYNAMIC else PINK

    val key = stringPreferencesKey(THEME)

    fun put(
        context: Context,
        scope: CoroutineScope,
        value: String,
        onSuccess: (() -> Unit)? = null,
    ) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
            withContext(Dispatchers.Main) {
                onSuccess?.invoke()
            }
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default

    fun toDisplayName(
        context: Context,
        value: String = context.dataStore.getOrDefault(this),
    ): String = when (value) {
        DYNAMIC -> context.getString(R.string.theme_dynamic)
        PINK -> context.getString(R.string.theme_pink)
        GREEN -> context.getString(R.string.theme_green)
        BLUE -> context.getString(R.string.theme_blue)
        YELLOW -> context.getString(R.string.theme_yellow)
        PURPLE -> context.getString(R.string.theme_purple)
        else -> context.getString(R.string.unknown)
    }

    fun toResId(
        context: Context,
        value: String = context.dataStore.getOrDefault(this),
    ): Int = when (value) {
        DYNAMIC -> R.style.Theme_AniVu_Dynamic
        PINK -> R.style.Theme_AniVu_Pink
        GREEN -> R.style.Theme_AniVu_Green
        BLUE -> R.style.Theme_AniVu_Blue
        YELLOW -> R.style.Theme_AniVu_Yellow
        PURPLE -> R.style.Theme_AniVu_Purple
        else -> R.style.Theme_AniVu_Pink
    }

    fun toSeedColor(
        context: Context,
        value: String = context.dataStore.getOrDefault(this),
    ): Color = when (value) {
        PINK -> Color(0xFF884A69)
        GREEN -> Color(0xFF406836)
        BLUE -> Color(0xFF3A608F)
        YELLOW -> Color(0xFF6C5E10)
        PURPLE -> Color(0xFF65558F)
        else -> Color(0xFF884A69)
    }
}
