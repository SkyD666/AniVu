package com.skyd.anivu.ui.fragment.settings.playerconfig

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.model.preference.player.PlayerAutoPipPreference
import com.skyd.anivu.model.preference.player.PlayerDoubleTapPreference
import com.skyd.anivu.model.preference.player.PlayerShow85sButtonPreference
import com.skyd.anivu.model.preference.player.PlayerShowScreenshotButtonPreference
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.BaseSettingsItem
import com.skyd.anivu.ui.component.CategorySettingsItem
import com.skyd.anivu.ui.component.CheckableListMenu
import com.skyd.anivu.ui.component.SwitchSettingsItem
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.local.LocalPlayerAutoPip
import com.skyd.anivu.ui.local.LocalPlayerDoubleTap
import com.skyd.anivu.ui.local.LocalPlayerShow85sButton
import com.skyd.anivu.ui.local.LocalPlayerShowScreenshotButton
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PlayerConfigFragment : BaseComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { PlayerConfigScreen() }
}

@Composable
fun PlayerConfigScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    var expandDoubleTapMenu by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.player_config_screen_name)) },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                CategorySettingsItem(text = stringResource(id = R.string.player_config_screen_behavior_category))
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Outlined.TouchApp),
                    text = stringResource(id = R.string.player_config_screen_double_tap),
                    descriptionText = PlayerDoubleTapPreference.toDisplayName(
                        context,
                        LocalPlayerDoubleTap.current,
                    ),
                    extraContent = {
                        DoubleTapMenu(
                            expanded = expandDoubleTapMenu,
                            onDismissRequest = { expandDoubleTapMenu = false }
                        )
                    },
                    onClick = { expandDoubleTapMenu = true },
                )
            }
            item {
                SwitchSettingsItem(
                    imageVector = Icons.Outlined.PictureInPictureAlt,
                    text = stringResource(id = R.string.player_config_screen_auto_pip),
                    description = stringResource(id = R.string.player_config_screen_auto_pip_description),
                    checked = LocalPlayerAutoPip.current,
                    onCheckedChange = {
                        PlayerAutoPipPreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    }
                )
            }
            item {
                CategorySettingsItem(text = stringResource(id = R.string.player_config_screen_appearance_category))
            }
            item {
                SwitchSettingsItem(
                    imageVector = Icons.Outlined.FastForward,
                    text = stringResource(id = R.string.player_config_screen_show_85s_button),
                    description = stringResource(id = R.string.player_config_screen_show_85s_button_description),
                    checked = LocalPlayerShow85sButton.current,
                    onCheckedChange = {
                        PlayerShow85sButtonPreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    }
                )
            }
            item {
                SwitchSettingsItem(
                    imageVector = Icons.Outlined.PhotoCamera,
                    text = stringResource(id = R.string.player_config_screen_show_screenshot_button),
                    checked = LocalPlayerShowScreenshotButton.current,
                    onCheckedChange = {
                        PlayerShowScreenshotButtonPreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    }
                )
            }
            item {
                CategorySettingsItem(text = stringResource(id = R.string.player_config_screen_advanced_category))
            }
            item {
                BaseSettingsItem(
                    icon = null,
                    text = stringResource(id = R.string.player_config_advanced_screen_name),
                    descriptionText = null,
                    onClick = { navController.navigate(R.id.action_to_player_config_advanced_fragment) },
                )
            }
        }
    }
}

@Composable
private fun DoubleTapMenu(expanded: Boolean, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val playerDoubleTap = LocalPlayerDoubleTap.current

    CheckableListMenu(
        expanded = expanded,
        current = playerDoubleTap,
        values = remember { PlayerDoubleTapPreference.values.toList() },
        displayName = { PlayerDoubleTapPreference.toDisplayName(context, it) },
        onChecked = { PlayerDoubleTapPreference.put(context, scope, it) },
        onDismissRequest = onDismissRequest,
    )
}