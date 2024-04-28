package com.skyd.anivu.ui.local

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import com.skyd.anivu.model.preference.IgnoreUpdateVersionPreference
import com.skyd.anivu.model.preference.appearance.DarkModePreference
import com.skyd.anivu.model.preference.appearance.DateStylePreference
import com.skyd.anivu.model.preference.appearance.TextFieldStylePreference
import com.skyd.anivu.model.preference.appearance.ThemePreference
import com.skyd.anivu.model.preference.appearance.feed.FeedGroupExpandPreference
import com.skyd.anivu.model.preference.behavior.article.ArticleSwipeLeftActionPreference
import com.skyd.anivu.model.preference.behavior.article.ArticleTapActionPreference
import com.skyd.anivu.model.preference.behavior.article.DeduplicateTitleInDescPreference
import com.skyd.anivu.model.preference.behavior.feed.HideEmptyDefaultPreference

val LocalNavController = compositionLocalOf<NavHostController> {
    error("LocalNavController not initialized!")
}

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("LocalWindowSizeClass not initialized!")
}

// Appearance
val LocalTheme = compositionLocalOf { ThemePreference.default }
val LocalDarkMode = compositionLocalOf { DarkModePreference.default }
val LocalFeedGroupExpand = compositionLocalOf { FeedGroupExpandPreference.default }
val LocalTextFieldStyle = compositionLocalOf { TextFieldStylePreference.default }
val LocalDateStyle = compositionLocalOf { DateStylePreference.default }

// Update
val LocalIgnoreUpdateVersion = compositionLocalOf { IgnoreUpdateVersionPreference.default }

// Behavior
val LocalDeduplicateTitleInDesc = compositionLocalOf { DeduplicateTitleInDescPreference.default }
val LocalArticleTapAction = compositionLocalOf { ArticleTapActionPreference.default }
val LocalArticleSwipeLeftAction = compositionLocalOf { ArticleSwipeLeftActionPreference.default }
val LocalHideEmptyDefault = compositionLocalOf { HideEmptyDefaultPreference.default }