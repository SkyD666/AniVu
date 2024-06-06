package com.skyd.anivu.ui.fragment.settings.data.importexport.exportopml

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.plus
import com.skyd.anivu.ext.safeLaunch
import com.skyd.anivu.ext.showSnackbar
import com.skyd.anivu.ext.showSnackbarWithLaunchedEffect
import com.skyd.anivu.model.preference.data.OpmlExportDirPreference
import com.skyd.anivu.ui.component.AniVuExtendedFloatingActionButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.BaseSettingsItem
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import com.skyd.anivu.ui.local.LocalOpmlExportDir
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ExportOpmlFragment : BaseComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { ExportOpmlScreen() }
}

@Composable
fun ExportOpmlScreen(viewModel: ExportOpmlViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)

    val dispatch = viewModel.getDispatcher(startWith = ExportOpmlIntent.Init)

    val exportDir = LocalOpmlExportDir.current
    val pickExportDirLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            OpmlExportDirPreference.put(context, scope, uri.toString())
        }
    }
    val lazyListState = rememberLazyListState()
    var fabHeight by remember { mutableStateOf(0.dp) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.export_opml_screen_name)) },
            )
        },
        floatingActionButton = {
            AniVuExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.export_opml_screen_export)) },
                icon = { Icon(imageVector = Icons.Default.Done, contentDescription = null) },
                onClick = {
                    if (exportDir.isBlank()) {
                        snackbarHostState.showSnackbar(
                            scope = scope,
                            message = context.getString(R.string.export_opml_screen_dir_not_selected),
                        )
                    } else {
                        dispatch(ExportOpmlIntent.ExportOpml(outputDir = Uri.parse(exportDir)))
                    }
                },
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                contentDescription = stringResource(R.string.export_opml_screen_export)
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues + PaddingValues(bottom = fabHeight),
            state = lazyListState,
        ) {
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Outlined.Folder),
                    text = stringResource(id = R.string.export_opml_screen_select_dir),
                    descriptionText = exportDir.ifBlank { null },
                    onClick = { pickExportDirLauncher.safeLaunch(Uri.parse(exportDir)) }
                )
            }
        }
    }

    WaitingDialog(visible = uiState.loadingDialog)

    when (val event = uiEvent) {
        is ExportOpmlEvent.ExportOpmlResultEvent.Failed ->
            snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

        is ExportOpmlEvent.ExportOpmlResultEvent.Success ->
            snackbarHostState.showSnackbarWithLaunchedEffect(
                message = stringResource(id = R.string.success_time_msg, event.time / 1000f),
                key1 = event,
            )

        null -> Unit
    }
}
