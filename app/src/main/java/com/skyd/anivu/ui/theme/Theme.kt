package com.skyd.anivu.ui.theme

import android.app.WallpaperManager
import android.os.Build
import android.view.View
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.dynamicColorScheme
import com.materialkolor.rememberDynamicColorScheme
import com.skyd.anivu.ext.activity
import com.skyd.anivu.model.preference.appearance.DarkModePreference
import com.skyd.anivu.model.preference.appearance.ThemePreference
import com.skyd.anivu.ui.local.LocalTheme

@Composable
fun AniVuTheme(
    darkTheme: Int,
    content: @Composable () -> Unit
) {
    AniVuTheme(
        darkTheme = DarkModePreference.inDark(darkTheme),
        content = content
    )
}

@Composable
fun AniVuTheme(
    darkTheme: Boolean,
    colors: Map<String, ColorScheme> = extractAllColors(darkTheme),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            setSystemBarsColor(view, darkTheme)
        }
    }

    val themeName = LocalTheme.current
    val context = LocalContext.current

    MaterialTheme(
        colorScheme = remember(themeName) {
            colors.getOrElse(themeName) {
                dynamicColorScheme(
                    seedColor = ThemePreference.toSeedColor(
                        context = context,
                        value = ThemePreference.values[0],
                    ),
                    isDark = darkTheme,
                )
            }
        },
        typography = Typography,
        content = content
    )
}

private fun setSystemBarsColor(view: View, darkMode: Boolean) {
    val window = view.context.activity.window
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.apply {
        statusBarColor = android.graphics.Color.TRANSPARENT
        navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            navigationBarDividerColor = android.graphics.Color.TRANSPARENT
        }
        // 状态栏和导航栏字体颜色
        WindowInsetsControllerCompat(this, view).apply {
            isAppearanceLightStatusBars = !darkMode
            isAppearanceLightNavigationBars = !darkMode
        }
    }
}

@Composable
fun extractAllColors(darkTheme: Boolean): Map<String, ColorScheme> {
    return extractColors(darkTheme) + extractDynamicColor(darkTheme)
}

@Composable
fun extractColors(darkTheme: Boolean): Map<String, ColorScheme> {
    return ThemePreference.values.associateWith {
        rememberDynamicColorScheme(
            seedColor = ThemePreference.toSeedColor(LocalContext.current, it),
            isDark = darkTheme,
        )
    }.toMutableMap()
}

@Composable
fun extractDynamicColor(darkTheme: Boolean): Map<String, ColorScheme> {
    val context = LocalContext.current
    val preset = mutableMapOf<String, ColorScheme>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !LocalView.current.isInEditMode) {
        val colors = WallpaperManager.getInstance(context)
            .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
        val primary = colors?.primaryColor?.toArgb()
        if (primary != null) {
            preset[ThemePreference.DYNAMIC] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                rememberSystemDynamicColorScheme(isDark = darkTheme)
            } else {
                rememberDynamicColorScheme(seedColor = Color(primary), isDark = darkTheme)
            }
        }
    }
    return preset
}