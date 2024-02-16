package com.skyd.anivu.base

import androidx.datastore.preferences.core.Preferences

interface BasePreference<T> {
    val default: T

    fun fromPreferences(preferences: Preferences): T
}