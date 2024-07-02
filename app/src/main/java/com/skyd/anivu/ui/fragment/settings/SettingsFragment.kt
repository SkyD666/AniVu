package com.skyd.anivu.ui.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.BaseSettingsItem
import com.skyd.anivu.ui.local.LocalNavController
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingsFragment : BaseComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { SettingsScreen() }
}

@Composable
fun SettingsScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.settings_screen_name)) },
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
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.Palette),
                    text = stringResource(id = R.string.appearance_fragment_name),
                    descriptionText = stringResource(id = R.string.appearance_fragment_description),
                    onClick = { navController.navigate(R.id.action_to_appearance_fragment) }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.TouchApp),
                    text = stringResource(id = R.string.behavior_screen_name),
                    descriptionText = stringResource(id = R.string.behavior_screen_description),
                    onClick = { navController.navigate(R.id.action_to_behavior_fragment) }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.RssFeed),
                    text = stringResource(id = R.string.rss_config_fragment_name),
                    descriptionText = stringResource(id = R.string.rss_config_fragment_description),
                    onClick = { navController.navigate(R.id.action_to_rss_config_fragment) }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.SmartDisplay),
                    text = stringResource(id = R.string.player_config_fragment_name),
                    descriptionText = stringResource(id = R.string.player_config_fragment_description),
                    onClick = { navController.navigate(R.id.action_to_player_config_fragment) }
                )
            }
            item {
                BaseSettingsItem(
                    icon = painterResource(id = R.drawable.ic_database_24),
                    text = stringResource(id = R.string.data_fragment_name),
                    descriptionText = stringResource(id = R.string.data_fragment_description),
                    onClick = { navController.navigate(R.id.action_to_data_fragment) }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.SwapVert),
                    text = stringResource(id = R.string.transmission_fragment_name),
                    descriptionText = stringResource(id = R.string.transmission_fragment_description),
                    onClick = { navController.navigate(R.id.action_to_transmission_fragment) }
                )
            }
        }
    }
}
