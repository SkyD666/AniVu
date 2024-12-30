package com.skyd.anivu.ui.screen.settings.transmission

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.anivu.R
import com.skyd.anivu.model.preference.transmission.SeedingWhenCompletePreference
import com.skyd.anivu.ui.component.PodAuraTopBar
import com.skyd.anivu.ui.component.PodAuraTopBarStyle
import com.skyd.anivu.ui.component.BaseSettingsItem
import com.skyd.anivu.ui.component.CategorySettingsItem
import com.skyd.anivu.ui.component.SwitchSettingsItem
import com.skyd.anivu.ui.screen.settings.transmission.proxy.PROXY_SCREEN_ROUTE
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.local.LocalSeedingWhenComplete


const val TRANSMISSION_SCREEN_ROUTE = "transmissionScreen"

@Composable
fun TransmissionScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            PodAuraTopBar(
                style = PodAuraTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.transmission_screen_name)) },
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
                CategorySettingsItem(text = stringResource(id = R.string.transmission_screen_transmission_behavior_category))
            }
            item {
                SwitchSettingsItem(
                    imageVector = Icons.Outlined.CloudUpload,
                    text = stringResource(id = R.string.transmission_screen_seeding_when_complete),
                    description = stringResource(id = R.string.transmission_screen_seeding_when_complete_description),
                    checked = LocalSeedingWhenComplete.current,
                    onCheckedChange = {
                        SeedingWhenCompletePreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    }
                )
            }
            item {
                CategorySettingsItem(text = stringResource(id = R.string.transmission_screen_proxy_category))
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Outlined.VpnKey),
                    text = stringResource(id = R.string.proxy_screen_name),
                    descriptionText = stringResource(id = R.string.proxy_screen_description),
                    onClick = { navController.navigate(PROXY_SCREEN_ROUTE) },
                )
            }
        }
    }
}