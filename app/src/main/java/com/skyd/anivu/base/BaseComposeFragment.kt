package com.skyd.anivu.base

import android.os.Bundle
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.NavHostController
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.model.preference.SettingsProvider
import com.skyd.anivu.ui.local.LocalDarkMode
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.local.LocalWindowSizeClass
import com.skyd.anivu.ui.theme.AniVuTheme


abstract class BaseComposeFragment : Fragment() {
    private lateinit var navController: NavHostController
    fun setContentBase(content: @Composable () -> Unit): ComposeView {
        navController = findMainNavController() as NavHostController
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CompositionLocalProvider(
                    LocalNavController provides navController,
                    LocalWindowSizeClass provides calculateWindowSizeClass(requireActivity())
                ) {
                    SettingsProvider { AniVuTheme(darkTheme = LocalDarkMode.current, content) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transitionProvider?.let { provider ->
            enterTransition = provider.enterTransition
            returnTransition = provider.returnTransition
            exitTransition = provider.exitTransition
            reenterTransition = provider.reenterTransition
        }
    }

    protected val defaultTransitionProvider = BaseFragment.TransitionProvider()

    protected val nullTransitionProvider: BaseFragment.TransitionProvider? = null

    protected open val transitionProvider: BaseFragment.TransitionProvider? =
        defaultTransitionProvider
}
