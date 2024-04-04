package com.skyd.anivu.model.preference.appearance

import android.content.Context
import android.os.Build
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
import kotlinx.coroutines.withContext

object ThemePreference : BasePreference<String> {
    private const val THEME = "theme"

    const val DYNAMIC = "Dynamic"
    const val PINK = "Pink"
    const val GREEN = "Green"
    const val BLUE = "Blue"
    const val YELLOW = "Yellow"
    const val PURPLE = "Purple"

    val values: Array<String>
        get() {
            val v = arrayOf(PINK, GREEN, BLUE, YELLOW, PURPLE)
            return if (supportDynamicTheme()) arrayOf(DYNAMIC, *v) else v
        }

    override val default = if (supportDynamicTheme()) DYNAMIC else PINK

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

    private fun supportDynamicTheme(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

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
}
