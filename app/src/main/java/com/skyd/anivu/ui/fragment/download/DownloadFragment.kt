package com.skyd.anivu.ui.fragment.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.plus
import com.skyd.anivu.ext.showSnackbar
import com.skyd.anivu.model.worker.download.DownloadTorrentWorker
import com.skyd.anivu.model.worker.download.doIfMagnetOrTorrentLink
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.dialog.TextFieldDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadFragment : BaseComposeFragment() {
    companion object {
        const val RESOURCE_URI_KEY = "resourceUri"
    }

    private val downloadLink: String? by lazy { arguments?.getString(RESOURCE_URI_KEY) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { DownloadScreen(downloadLink) }
}

@Composable
fun DownloadScreen(downloadLink: String? = null, viewModel: DownloadViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var openLinkDialog by rememberSaveable(downloadLink) { mutableStateOf(downloadLink) }

    var fabHeight by remember { mutableStateOf(0.dp) }

    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val dispatch = viewModel.getDispatcher(startWith = DownloadIntent.Init)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.CenterAligned,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.download_fragment_name)) },
            )
        },
        floatingActionButton = {
            AniVuFloatingActionButton(
                onClick = { openLinkDialog = "" },
                contentDescription = stringResource(id = R.string.download_fragment_add_download),
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        when (val downloadListState = uiState.downloadListState) {
            is DownloadListState.Failed,
            DownloadListState.Init,
            DownloadListState.Loading -> Unit

            is DownloadListState.Success -> {
                LazyColumn(
                    contentPadding = paddingValues + PaddingValues(bottom = fabHeight + 16.dp),
                ) {
                    itemsIndexed(
                        items = downloadListState.downloadInfoBeanList,
                        key = { _, item -> item.link },
                    ) { index, item ->
                        if (index > 0) HorizontalDivider()
                        DownloadItem(
                            data = item,
                            onPause = {
                                DownloadTorrentWorker.pause(
                                    context = context,
                                    requestId = it.downloadRequestId,
                                    link = it.link,
                                )
                            },
                            onResume = { video ->
                                DownloadTorrentWorker.startWorker(
                                    context = context,
                                    torrentLink = video.link,
                                    requestId = video.downloadRequestId,
                                )
                            },
                            onCancel = { video ->
                                DownloadTorrentWorker.cancel(
                                    context = context,
                                    requestId = video.downloadRequestId,
                                    link = video.link,
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    TextFieldDialog(
        visible = openLinkDialog != null,
        icon = { Icon(imageVector = Icons.Outlined.Download, contentDescription = null) },
        titleText = stringResource(id = R.string.download),
        value = openLinkDialog.orEmpty(),
        onValueChange = { openLinkDialog = it },
        placeholder = stringResource(id = R.string.download_fragment_add_download_hint),
        onDismissRequest = { openLinkDialog = null },
        onConfirm = { text ->
            openLinkDialog = null
            doIfMagnetOrTorrentLink(
                link = text,
                onMagnet = { dispatch(DownloadIntent.AddDownload(it)) },
                onTorrent = { dispatch(DownloadIntent.AddDownload(it)) },
                onUnsupported = {
                    snackbarHostState.showSnackbar(
                        scope = scope,
                        message = context.getString(R.string.download_fragment_unsupported_link)
                    )
                },
            )
        },
    )
}