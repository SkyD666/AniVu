package com.skyd.anivu.ui.screen.media.sub

import androidx.compose.foundation.basicMarquee
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.skyd.anivu.R
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.toEncodedUrl
import com.skyd.anivu.ui.component.PodAuraTopBar
import com.skyd.anivu.ui.component.PodAuraTopBarStyle
import com.skyd.anivu.ui.component.dialog.PodAuraDialog
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.screen.media.list.MediaList


const val SUB_MEDIA_SCREEN_ROUTE = "subMediaScreen"
const val SUB_MEDIA_SCREEN_PATH_KEY = "path"

fun openSubMediaScreen(
    navController: NavController,
    path: String,
    navOptions: NavOptions? = null,
) {
    navController.navigate(
        "$SUB_MEDIA_SCREEN_ROUTE/${path.toEncodedUrl(allow = null)}",
        navOptions = navOptions,
    )
}

@Composable
fun SubMediaScreenRoute(path: String?) {
    val navController = LocalNavController.current
    if (path.isNullOrBlank()) {
        PodAuraDialog(
            icon = {
                Icon(imageVector = Icons.Outlined.WarningAmber, contentDescription = null)
            },
            title = { Text(text = stringResource(id = R.string.warning)) },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.sub_media_screen_path_illegal, path.toString()
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { navController.popBackStackWithLifecycle() }) {
                    Text(text = stringResource(id = R.string.exit))
                }
            },
        )
    } else {
        SubMediaScreen(path)
    }
}

@Composable
private fun SubMediaScreen(path: String) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            PodAuraTopBar(
                style = PodAuraTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        text = path.substringAfterLast("/"),
                        maxLines = 1,
                    )
                },
            )
        }
    ) { paddingValues ->
        MediaList(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
            path = path,
        )
    }
}
