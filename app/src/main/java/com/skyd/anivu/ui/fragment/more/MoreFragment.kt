package com.skyd.anivu.ui.fragment.more

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.ext.isCompact
import com.skyd.anivu.ext.plus
import com.skyd.anivu.model.bean.MoreBean
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.lazyverticalgrid.AniVuLazyVerticalGrid
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.MoreProxy
import com.skyd.anivu.ui.component.shape.CloverShape
import com.skyd.anivu.ui.component.shape.CurlyCornerShape
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.local.LocalWindowSizeClass
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoreFragment : BaseComposeFragment() {
    override val transitionProvider = nullTransitionProvider

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { MoreScreen() }
}

@Composable
fun MoreScreen() {
    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val density = LocalDensity.current
    val windowSizeClass = LocalWindowSizeClass.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.CenterAligned,
                title = { Text(text = stringResource(id = R.string.more_screen_name)) },
                navigationIcon = {},
                windowInsets = WindowInsets.safeDrawing.only(
                    (WindowInsetsSides.Top + WindowInsetsSides.Right).run {
                        if (windowSizeClass.isCompact) plus(WindowInsetsSides.Left) else this
                    }
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            (WindowInsetsSides.Top + WindowInsetsSides.Right).run {
                if (windowSizeClass.isCompact) plus(WindowInsetsSides.Left) else this
            }
        )
    ) {
        val colorScheme: ColorScheme = MaterialTheme.colorScheme
        val adapter = remember {
            LazyGridAdapter(
                mutableListOf(
                    MoreProxy(onClickListener = { data -> data.action.invoke() })
                )
            )
        }
        AniVuLazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            dataList = remember(context, colorScheme, density, navController) {
                getMoreBeanList(context, colorScheme, density, navController)
            },
            adapter = adapter,
            contentPadding = it + PaddingValues(vertical = 10.dp),
        )
    }
}

private fun getMoreBeanList(
    context: Context,
    colorScheme: ColorScheme,
    density: Density,
    navController: NavController,
): MutableList<MoreBean> {
    return mutableListOf(
        MoreBean(
            title = context.getString(R.string.settings_fragment_name),
            icon = R.drawable.ic_settings_24,
            iconTint = colorScheme.onPrimary,
            shape = CloverShape,
            shapeColor = colorScheme.primary,
            action = { navController.navigate(R.id.action_to_settings_fragment) },
        ),
        MoreBean(
            title = context.getString(R.string.about_screen_name),
            icon = R.drawable.ic_info_24,
            iconTint = colorScheme.onSecondary,
            shape = CurlyCornerShape(amp = with(density) { 1.6.dp.toPx() }, count = 10),
            shapeColor = colorScheme.secondary,
            action = { navController.navigate(R.id.action_to_about_fragment) }
        ),
    )
}