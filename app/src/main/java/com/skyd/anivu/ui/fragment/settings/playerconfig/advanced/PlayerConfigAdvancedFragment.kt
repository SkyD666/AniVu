package com.skyd.anivu.ui.fragment.settings.playerconfig.advanced

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.skyd.anivu.model.preference.player.HardwareDecodePreference
import com.skyd.anivu.model.preference.player.MpvConfigPreference
import com.skyd.anivu.model.preference.player.MpvInputConfigPreference
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.BaseSettingsItem
import com.skyd.anivu.ui.component.SwitchSettingsItem
import com.skyd.anivu.ui.component.dialog.TextFieldDialog
import com.skyd.anivu.ui.local.LocalHardwareDecode
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PlayerConfigAdvancedFragment : BaseComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { PlayerConfigAdvancedScreen() }
}

@Composable
fun PlayerConfigAdvancedScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mpvConfEditDialogValue by rememberSaveable { mutableStateOf("") }
    var openMpvConfEditDialog by rememberSaveable { mutableStateOf(false) }
    var mpvInputConfEditDialogValue by rememberSaveable { mutableStateOf("") }
    var openMpvInputConfEditDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.player_config_advanced_screen_name)) },
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
                SwitchSettingsItem(
                    imageVector = Icons.Rounded.DeveloperBoard,
                    text = stringResource(id = R.string.player_config_advanced_screen_hardware_decode),
                    description = stringResource(id = R.string.player_config_advanced_screen_hardware_decode_description),
                    checked = LocalHardwareDecode.current,
                    onCheckedChange = {
                        HardwareDecodePreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.PlayCircle),
                    text = stringResource(id = R.string.player_config_advanced_screen_mpv_config),
                    descriptionText = null,
                    onClick = {
                        mpvConfEditDialogValue = MpvConfigPreference.getValue()
                        openMpvConfEditDialog = true
                    }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.Keyboard),
                    text = stringResource(id = R.string.player_config_advanced_screen_mpv_input_config),
                    descriptionText = null,
                    onClick = {
                        mpvInputConfEditDialogValue = MpvInputConfigPreference.getValue()
                        openMpvInputConfEditDialog = true
                    }
                )
            }
        }

        TextFieldDialog(
            visible = openMpvConfEditDialog,
            value = mpvConfEditDialogValue,
            onValueChange = { mpvConfEditDialogValue = it },
            title = null,
            onConfirm = {
                MpvConfigPreference.put(
                    scope = scope,
                    value = it,
                )
                openMpvConfEditDialog = false
            },
            enableConfirm = { true },
            onDismissRequest = { openMpvConfEditDialog = false },
        )

        TextFieldDialog(
            visible = openMpvInputConfEditDialog,
            value = mpvInputConfEditDialogValue,
            onValueChange = { mpvInputConfEditDialogValue = it },
            title = null,
            onConfirm = {
                MpvInputConfigPreference.put(
                    scope = scope,
                    value = it,
                )
                openMpvInputConfEditDialog = false
            },
            enableConfirm = { true },
            onDismissRequest = { openMpvInputConfEditDialog = false },
        )
    }
}
