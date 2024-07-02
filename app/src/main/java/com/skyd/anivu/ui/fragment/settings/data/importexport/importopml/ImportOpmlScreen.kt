package com.skyd.anivu.ui.fragment.settings.data.importexport.importopml

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Segment
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.skyd.anivu.model.repository.importexport.ImportOpmlConflictStrategy
import com.skyd.anivu.ui.component.AniVuExtendedFloatingActionButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.BaseSettingsItem
import com.skyd.anivu.ui.component.TipSettingsItem
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ImportOpmlFragment : BaseComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { ImportOpmlScreen() }
}

@Composable
fun ImportOpmlScreen(viewModel: ImportOpmlViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)
    val importedStickerProxyList = rememberSaveable {
        listOf(ImportOpmlConflictStrategy.SkipStrategy, ImportOpmlConflictStrategy.ReplaceStrategy)
    }
    var selectedImportedStickerProxyIndex by rememberSaveable { mutableIntStateOf(0) }

    val dispatch = viewModel.getDispatcher(startWith = ImportOpmlIntent.Init)

    var opmlUri by rememberSaveable { mutableStateOf(Uri.EMPTY) }
    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            opmlUri = uri
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
                title = { Text(text = stringResource(R.string.import_opml_screen_name)) },
            )
        },
        floatingActionButton = {
            AniVuExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.import_opml_screen_import)) },
                icon = { Icon(imageVector = Icons.Default.Done, contentDescription = null) },
                onClick = {
                    if (opmlUri == Uri.EMPTY) {
                        snackbarHostState.showSnackbar(
                            scope = scope,
                            message = context.getString(R.string.import_opml_screen_opml_not_selected),
                        )
                    } else {
                        dispatch(
                            ImportOpmlIntent.ImportOpml(
                                opmlUri = opmlUri,
                                strategy = importedStickerProxyList[selectedImportedStickerProxyIndex],
                            )
                        )
                    }
                },
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                contentDescription = stringResource(R.string.import_opml_screen_import)
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
                    icon = rememberVectorPainter(image = Icons.AutoMirrored.Outlined.Segment),
                    text = stringResource(id = R.string.import_opml_screen_select_file),
                    descriptionText = opmlUri.toString().ifBlank { null },
                    onClick = { pickFileLauncher.safeLaunch("*/*") }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.AutoMirrored.Outlined.HelpOutline),
                    text = stringResource(R.string.import_opml_screen_on_conflict),
                    description = {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            importedStickerProxyList.forEachIndexed { index, proxy ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = importedStickerProxyList.size
                                    ),
                                    onClick = { selectedImportedStickerProxyIndex = index },
                                    selected = index == selectedImportedStickerProxyIndex
                                ) {
                                    Text(text = proxy.displayName)
                                }
                            }
                        }
                    }
                )
            }
            item {
                TipSettingsItem(
                    text = stringResource(id = R.string.import_opml_screen_desc)
                )
            }
        }
    }

    WaitingDialog(visible = uiState.loadingDialog)

    when (val event = uiEvent) {
        is ImportOpmlEvent.ImportOpmlResultEvent.Success ->
            snackbarHostState.showSnackbarWithLaunchedEffect(
                message = context.resources.getQuantityString(
                    R.plurals.import_opml_result,
                    event.result.importedFeedCount,
                    event.result.importedFeedCount,
                    event.result.time / 1000f,
                ),
                key2 = event,
            )

        is ImportOpmlEvent.ImportOpmlResultEvent.Failed ->
            snackbarHostState.showSnackbarWithLaunchedEffect(
                message = context.getString(R.string.failed_msg, event.msg),
                key2 = event,
            )

        null -> Unit
    }
}