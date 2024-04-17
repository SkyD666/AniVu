package com.skyd.anivu.ext

import androidx.datastore.preferences.core.Preferences
import com.skyd.anivu.model.preference.Settings
import com.skyd.anivu.model.preference.appearance.DarkModePreference
import com.skyd.anivu.model.preference.appearance.ThemePreference

fun Preferences.toSettings(): Settings {
    return Settings(
        // Theme
        theme = ThemePreference.fromPreferences(this),
        darkMode = DarkModePreference.fromPreferences(this),
    )
}
