package com.skyd.anivu.ui.screen.download

import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.skyd.anivu.R
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.navigate
import com.skyd.anivu.ext.plus
import com.skyd.anivu.ext.showSnackbar
import com.skyd.anivu.model.bean.download.DownloadInfoBean
import com.skyd.anivu.model.worker.download.DownloadTorrentWorker
import com.skyd.anivu.model.worker.download.DownloadTorrentWorker.Companion.rememberDownloadWorkStarter
import com.skyd.anivu.model.worker.download.doIfMagnetOrTorrentLink
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.CircularProgressPlaceholder
import com.skyd.anivu.ui.component.EmptyPlaceholder
import com.skyd.anivu.ui.component.deeplink.DeepLinkData
import com.skyd.anivu.ui.component.dialog.TextFieldDialog


const val DOWNLOAD_SCREEN_ROUTE = "downloadScreen"
val DOWNLOAD_SCREEN_DEEP_LINK_DATA = DeepLinkData(
    deepLink = "anivu://download.screen",
    navOptions = navOptions { launchSingleTop = true },
)
const val DOWNLOAD_LINK_KEY = "downloadLink"

fun openDownloadScreen(
    navController: NavController,
    downloadLink: String? = null,
) {
    navController.navigate(
        DOWNLOAD_SCREEN_ROUTE,
        Bundle().apply {
            putString(DOWNLOAD_LINK_KEY, downloadLink)
        },
        navOptions = navOptions { launchSingleTop = true },
    )
}

@Composable
fun DownloadScreen(downloadLink: String? = null, viewModel: DownloadViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var openLinkDialog by rememberSaveable(downloadLink) { mutableStateOf(downloadLink) }

    var fabHeight by remember { mutableStateOf(0.dp) }

    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    viewModel.getDispatcher(startWith = DownloadIntent.Init)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.CenterAligned,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.download_screen_name)) },
            )
        },
        floatingActionButton = {
            AniVuFloatingActionButton(
                onClick = { openLinkDialog = "" },
                contentDescription = stringResource(id = R.string.download_screen_add_download),
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        when (val downloadListState = uiState.downloadListState) {
            is DownloadListState.Failed -> Unit
            DownloadListState.Init,
            DownloadListState.Loading -> CircularProgressPlaceholder(contentPadding = paddingValues)

            is DownloadListState.Success -> DownloadList(
                downloadInfoBeanList = downloadListState.downloadInfoBeanList,
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                contentPadding = paddingValues + PaddingValues(bottom = fabHeight + 16.dp)
            )
        }
    }

    val downloadWorkStarter = rememberDownloadWorkStarter()
    TextFieldDialog(
        visible = openLinkDialog != null,
        icon = { Icon(imageVector = Icons.Outlined.Download, contentDescription = null) },
        titleText = stringResource(id = R.string.download),
        value = openLinkDialog.orEmpty(),
        onValueChange = { openLinkDialog = it },
        placeholder = stringResource(id = R.string.download_screen_add_download_hint),
        onDismissRequest = { openLinkDialog = null },
        onConfirm = { text ->
            openLinkDialog = null
            doIfMagnetOrTorrentLink(
                link = text,
                onMagnet = { downloadWorkStarter.start(torrentLink = it, requestId = null) },
                onTorrent = { downloadWorkStarter.start(torrentLink = it, requestId = null) },
                onUnsupported = {
                    snackbarHostState.showSnackbar(
                        scope = scope,
                        message = context.getString(R.string.download_screen_unsupported_link)
                    )
                },
            )
        },
    )
}

@Composable
private fun DownloadList(
    downloadInfoBeanList: List<DownloadInfoBean>,
    nestedScrollConnection: NestedScrollConnection,
    contentPadding: PaddingValues,
) {
    if (downloadInfoBeanList.isNotEmpty()) {
        val context = LocalContext.current
        val downloadWorkStarter = rememberDownloadWorkStarter()
        LazyColumn(
            modifier = Modifier.nestedScroll(nestedScrollConnection),
            contentPadding = contentPadding,
        ) {
            itemsIndexed(
                items = downloadInfoBeanList,
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
                        downloadWorkStarter.start(
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
    } else {
        EmptyPlaceholder(contentPadding = contentPadding)
    }
}