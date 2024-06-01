package com.skyd.anivu.model.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.toSettings
import com.skyd.anivu.model.preference.appearance.DarkModePreference
import com.skyd.anivu.model.preference.appearance.DateStylePreference
import com.skyd.anivu.model.preference.appearance.NavigationBarLabelPreference
import com.skyd.anivu.model.preference.appearance.TextFieldStylePreference
import com.skyd.anivu.model.preference.appearance.ThemePreference
import com.skyd.anivu.model.preference.appearance.feed.FeedGroupExpandPreference
import com.skyd.anivu.model.preference.autodelete.AutoDeleteArticleBeforePreference
import com.skyd.anivu.model.preference.autodelete.AutoDeleteArticleFrequencyPreference
import com.skyd.anivu.model.preference.autodelete.UseAutoDeletePreference
import com.skyd.anivu.model.preference.behavior.PickImageMethodPreference
import com.skyd.anivu.model.preference.behavior.article.ArticleSwipeLeftActionPreference
import com.skyd.anivu.model.preference.behavior.article.ArticleTapActionPreference
import com.skyd.anivu.model.preference.behavior.article.DeduplicateTitleInDescPreference
import com.skyd.anivu.model.preference.behavior.feed.HideEmptyDefaultPreference
import com.skyd.anivu.model.preference.player.HardwareDecodePreference
import com.skyd.anivu.model.preference.player.PlayerDoubleTapPreference
import com.skyd.anivu.model.preference.player.PlayerShow85sButtonPreference
import com.skyd.anivu.model.preference.player.PlayerShowScreenshotButtonPreference
import com.skyd.anivu.ui.local.LocalArticleSwipeLeftAction
import com.skyd.anivu.ui.local.LocalArticleTapAction
import com.skyd.anivu.ui.local.LocalAutoDeleteArticleBefore
import com.skyd.anivu.ui.local.LocalAutoDeleteArticleFrequency
import com.skyd.anivu.ui.local.LocalDarkMode
import com.skyd.anivu.ui.local.LocalDateStyle
import com.skyd.anivu.ui.local.LocalDeduplicateTitleInDesc
import com.skyd.anivu.ui.local.LocalFeedGroupExpand
import com.skyd.anivu.ui.local.LocalHardwareDecode
import com.skyd.anivu.ui.local.LocalHideEmptyDefault
import com.skyd.anivu.ui.local.LocalIgnoreUpdateVersion
import com.skyd.anivu.ui.local.LocalNavigationBarLabel
import com.skyd.anivu.ui.local.LocalPickImageMethod
import com.skyd.anivu.ui.local.LocalPlayerDoubleTap
import com.skyd.anivu.ui.local.LocalPlayerShow85sButton
import com.skyd.anivu.ui.local.LocalPlayerShowScreenshotButton
import com.skyd.anivu.ui.local.LocalTextFieldStyle
import com.skyd.anivu.ui.local.LocalTheme
import com.skyd.anivu.ui.local.LocalUseAutoDelete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map

data class Settings(
    // Appearance
    val theme: String = ThemePreference.default,
    val darkMode: Int = DarkModePreference.default,
    val feedGroupExpand: Boolean = FeedGroupExpandPreference.default,
    val textFieldStyle: String = TextFieldStylePreference.default,
    val dateStyle: String = DateStylePreference.default,
    val navigationBarLabel: String = NavigationBarLabelPreference.default,
    // Update
    val ignoreUpdateVersion: Long = IgnoreUpdateVersionPreference.default,
    // Behavior
    val deduplicateTitleInDesc: Boolean = DeduplicateTitleInDescPreference.default,
    val articleTapAction: String = ArticleTapActionPreference.default,
    val articleSwipeLeftAction: String = ArticleSwipeLeftActionPreference.default,
    val hideEmptyDefault: Boolean = HideEmptyDefaultPreference.default,
    val pickImageMethod: String = PickImageMethodPreference.default,
    // Player
    val playerDoubleTap: String = PlayerDoubleTapPreference.default,
    val playerShow85sButton: Boolean = PlayerShow85sButtonPreference.default,
    val playerShowScreenshotButton: Boolean = PlayerShowScreenshotButtonPreference.default,
    val hardwareDecode: Boolean = HardwareDecodePreference.default,
    // Data
    val useAutoDelete: Boolean = UseAutoDeletePreference.default,
    val autoDeleteArticleFrequency: Long = AutoDeleteArticleFrequencyPreference.default,
    val autoDeleteArticleBefore: Long = AutoDeleteArticleBeforePreference.default,
)

@Composable
fun SettingsProvider(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val settings by remember { context.dataStore.data.map { it.toSettings() } }
        .collectAsState(initial = Settings(), context = Dispatchers.Default)

    CompositionLocalProvider(
        // Appearance
        LocalTheme provides settings.theme,
        LocalDarkMode provides settings.darkMode,
        LocalFeedGroupExpand provides settings.feedGroupExpand,
        LocalTextFieldStyle provides settings.textFieldStyle,
        LocalDateStyle provides settings.dateStyle,
        LocalNavigationBarLabel provides settings.navigationBarLabel,
        // Update
        LocalIgnoreUpdateVersion provides settings.ignoreUpdateVersion,
        // Behavior
        LocalDeduplicateTitleInDesc provides settings.deduplicateTitleInDesc,
        LocalArticleTapAction provides settings.articleTapAction,
        LocalArticleSwipeLeftAction provides settings.articleSwipeLeftAction,
        LocalHideEmptyDefault provides settings.hideEmptyDefault,
        LocalPickImageMethod provides settings.pickImageMethod,
        // Player
        LocalPlayerDoubleTap provides settings.playerDoubleTap,
        LocalPlayerShow85sButton provides settings.playerShow85sButton,
        LocalPlayerShowScreenshotButton provides settings.playerShowScreenshotButton,
        LocalHardwareDecode provides settings.hardwareDecode,
        // Data
        LocalUseAutoDelete provides settings.useAutoDelete,
        LocalAutoDeleteArticleFrequency provides settings.autoDeleteArticleFrequency,
        LocalAutoDeleteArticleBefore provides settings.autoDeleteArticleBefore,
    ) {
        content()
    }
}