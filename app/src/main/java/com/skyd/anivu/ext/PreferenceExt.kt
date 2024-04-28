package com.skyd.anivu.ext

import androidx.datastore.preferences.core.Preferences
import com.skyd.anivu.model.preference.IgnoreUpdateVersionPreference
import com.skyd.anivu.model.preference.Settings
import com.skyd.anivu.model.preference.appearance.DarkModePreference
import com.skyd.anivu.model.preference.appearance.DateStylePreference
import com.skyd.anivu.model.preference.appearance.TextFieldStylePreference
import com.skyd.anivu.model.preference.appearance.ThemePreference
import com.skyd.anivu.model.preference.appearance.feed.FeedGroupExpandPreference
import com.skyd.anivu.model.preference.behavior.article.ArticleSwipeLeftActionPreference
import com.skyd.anivu.model.preference.behavior.article.ArticleTapActionPreference
import com.skyd.anivu.model.preference.behavior.article.DeduplicateTitleInDescPreference
import com.skyd.anivu.model.preference.behavior.feed.HideEmptyDefaultPreference

fun Preferences.toSettings(): Settings {
    return Settings(
        // Appearance
        theme = ThemePreference.fromPreferences(this),
        darkMode = DarkModePreference.fromPreferences(this),
        feedGroupExpand = FeedGroupExpandPreference.fromPreferences(this),
        textFieldStyle = TextFieldStylePreference.fromPreferences(this),
        dateStyle = DateStylePreference.fromPreferences(this),

        // Update
        ignoreUpdateVersion = IgnoreUpdateVersionPreference.fromPreferences(this),

        // Behavior
        deduplicateTitleInDesc = DeduplicateTitleInDescPreference.fromPreferences(this),
        articleTapAction = ArticleTapActionPreference.fromPreferences(this),
        articleSwipeLeftAction = ArticleSwipeLeftActionPreference.fromPreferences(this),
        hideEmptyDefault = HideEmptyDefaultPreference.fromPreferences(this),
    )
}
