package com.skyd.anivu.base

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.skyd.anivu.model.preference.SettingsProvider
import com.skyd.anivu.model.preference.appearance.ThemePreference
import com.skyd.anivu.ui.local.LocalDarkMode
import com.skyd.anivu.ui.local.LocalWindowSizeClass
import com.skyd.anivu.ui.theme.AniVuTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initTheme()
    }

    fun setContentBase(content: @Composable () -> Unit) = setContent {
        CompositionLocalProvider(
            LocalWindowSizeClass provides calculateWindowSizeClass(this@BaseComposeActivity)
        ) {
            SettingsProvider { AniVuTheme(darkTheme = LocalDarkMode.current, content) }
        }
    }

    private fun initTheme() {
        setTheme(ThemePreference.toResId(this))
    }
}