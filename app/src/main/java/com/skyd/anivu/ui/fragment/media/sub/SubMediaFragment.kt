package com.skyd.anivu.ui.fragment.media.sub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.dialog.AniVuDialog
import com.skyd.anivu.ui.fragment.media.list.MediaList
import com.skyd.anivu.ui.local.LocalNavController
import dagger.hilt.android.AndroidEntryPoint


const val SUB_MEDIA_SCREEN_PATH_KEY = "path"

@AndroidEntryPoint
class SubMediaFragment : BaseComposeFragment() {
    private val path by lazy { arguments?.getString(SUB_MEDIA_SCREEN_PATH_KEY) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase {
        val navController = LocalNavController.current
        if (path.isNullOrBlank()) {
            AniVuDialog(
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
            SubMediaScreen(path!!)
        }
    }
}

@Composable
fun SubMediaScreen(path: String) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = path.substringAfterLast("/")) },
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
