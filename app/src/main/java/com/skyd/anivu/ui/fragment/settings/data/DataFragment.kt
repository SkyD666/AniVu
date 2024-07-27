package com.skyd.anivu.ui.fragment.settings.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoDelete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.showSnackbarWithLaunchedEffect
import com.skyd.anivu.model.preference.data.medialib.MediaLibLocationPreference
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.BaseSettingsItem
import com.skyd.anivu.ui.component.CategorySettingsItem
import com.skyd.anivu.ui.component.dialog.DeleteWarningDialog
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import com.skyd.anivu.ui.fragment.filepicker.ListenToFilePicker
import com.skyd.anivu.ui.fragment.filepicker.navigateToFilePicker
import com.skyd.anivu.ui.local.LocalMediaLibLocation
import com.skyd.anivu.ui.local.LocalNavController
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DataFragment : BaseComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { DataScreen() }
}

@Composable
fun DataScreen(viewModel: DataViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)
    val dispatch = viewModel.getDispatcher(startWith = DataIntent.Init)

    ListenToFilePicker { result ->
        MediaLibLocationPreference.put(context, this, result.result)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.data_screen_name)) },
            )
        }
    ) { paddingValues ->
        var openDeleteWarningDialog by rememberSaveable { mutableStateOf(false) }
        var openDeleteBeforeDatePickerDialog by rememberSaveable { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                CategorySettingsItem(
                    text = stringResource(id = R.string.data_screen_media_lib_category),
                )
            }
            item {
                val localMediaLibLocation = LocalMediaLibLocation.current
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.PermMedia),
                    text = stringResource(id = R.string.data_screen_media_lib_location),
                    descriptionText = localMediaLibLocation,
                    onClick = {
                        navigateToFilePicker(
                            navController = navController,
                            path = localMediaLibLocation,
                        )
                    }
                ) {
                    AniVuIconButton(
                        onClick = {
                            MediaLibLocationPreference.put(
                                context,
                                scope,
                                MediaLibLocationPreference.default,
                            )
                        },
                        imageVector = Icons.Outlined.Replay,
                    )
                }
            }
            item {
                CategorySettingsItem(
                    text = stringResource(id = R.string.data_screen_clear_up_category),
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.Delete),
                    text = stringResource(id = R.string.data_screen_clear_cache),
                    descriptionText = stringResource(id = R.string.data_screen_clear_cache_description),
                    onClick = { openDeleteWarningDialog = true }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.Today),
                    text = stringResource(id = R.string.data_screen_delete_article_before),
                    descriptionText = stringResource(id = R.string.data_screen_delete_article_before_description),
                    onClick = { openDeleteBeforeDatePickerDialog = true },
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.AutoDelete),
                    text = stringResource(id = R.string.auto_delete_screen_name),
                    descriptionText = stringResource(id = R.string.auto_delete_article_fragment_description),
                    onClick = { navController.navigate(R.id.action_to_auto_delete_article_fragment) }
                )
            }
            item {
                CategorySettingsItem(
                    text = stringResource(id = R.string.data_screen_sync_category),
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.SwapVert),
                    text = stringResource(id = R.string.import_export_screen_name),
                    descriptionText = stringResource(id = R.string.import_export_screen_description),
                    onClick = { navController.navigate(R.id.action_to_import_export_fragment) }
                )
            }
        }

        if (openDeleteBeforeDatePickerDialog) {
            DeleteArticleBeforeDatePickerDialog(
                onDismissRequest = { openDeleteBeforeDatePickerDialog = false },
                onConfirm = { dispatch(DataIntent.DeleteArticleBefore(it)) }
            )
        }

        DeleteWarningDialog(
            visible = openDeleteWarningDialog,
            text = stringResource(id = R.string.data_screen_clear_cache_warning),
            onDismissRequest = { openDeleteWarningDialog = false },
            onDismiss = { openDeleteWarningDialog = false },
            onConfirm = { dispatch(DataIntent.ClearCache) },
        )

        WaitingDialog(visible = uiState.loadingDialog)

        when (val event = uiEvent) {
            is DataEvent.ClearCacheResultEvent.Success ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            is DataEvent.ClearCacheResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            is DataEvent.DeleteArticleBeforeResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            is DataEvent.DeleteArticleBeforeResultEvent.Success ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            null -> Unit
        }
    }
}

@Composable
private fun DeleteArticleBeforeDatePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(datePickerState.selectedDateMillis!!) },
                enabled = confirmEnabled.value,
            ) {
                Text(stringResource(id = R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}