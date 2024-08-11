package com.skyd.anivu.ui.screen.article.enclosure

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.copy
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.fileSize
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.bean.ArticleWithEnclosureBean
import com.skyd.anivu.model.bean.EnclosureBean
import com.skyd.anivu.model.bean.LinkEnclosureBean
import com.skyd.anivu.model.preference.rss.ParseLinkTagAsEnclosurePreference
import com.skyd.anivu.model.worker.download.DownloadTorrentWorker
import com.skyd.anivu.model.worker.download.doIfMagnetOrTorrentLink
import com.skyd.anivu.model.worker.download.isTorrentMimetype
import com.skyd.anivu.ui.activity.PlayActivity
import com.skyd.anivu.ui.component.AniVuIconButton

fun getEnclosuresList(
    context: Context,
    articleWithEnclosureBean: ArticleWithEnclosureBean,
): List<Any> {
    val dataList: MutableList<Any> = articleWithEnclosureBean.enclosures.toMutableList()
    if (context.dataStore.getOrDefault(ParseLinkTagAsEnclosurePreference)) {
        articleWithEnclosureBean.article.link?.let { link ->
            doIfMagnetOrTorrentLink(
                link = link,
                onMagnet = { dataList += LinkEnclosureBean(link = link) },
                onTorrent = { dataList += LinkEnclosureBean(link = link) },
            )
        }
    }
    return dataList
}

@Composable
fun EnclosureBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    dataList: List<Any>,
) {
    val context = LocalContext.current
    val onDownload: (Any) -> Unit = remember {
        {
            val url = when (it) {
                is EnclosureBean -> it.url
                is LinkEnclosureBean -> it.link
                else -> null
            }
            if (!url.isNullOrBlank()) {
                DownloadTorrentWorker.startWorker(
                    context = context, torrentLink = url
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.bottom_sheet_enclosure_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyColumn {
                itemsIndexed(dataList) { index, item ->
                    if (index > 0) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    if (item is EnclosureBean) {
                        EnclosureItem(item, onDownload = onDownload)
                    } else if (item is LinkEnclosureBean) {
                        LinkEnclosureItem(item, onDownload = onDownload)
                    }
                }
            }
        }
    }
}

@Composable
private fun EnclosureItem(
    data: EnclosureBean,
    onDownload: (EnclosureBean) -> Unit,
) {
    val context = LocalContext.current
    val isMagnetOrTorrent = rememberSaveable {
        data.url.startsWith("magnet:") || isTorrentMimetype(data.type) ||
                Regex("^(http|https)://.*\\.torrent$").matches(data.url)
    }
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier.clickable { data.url.copy(context) },
                text = data.url,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
            )
            Row(modifier = Modifier.padding(top = 6.dp)) {
                Text(
                    text = data.length.fileSize(context),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
                if (!data.type.isNullOrBlank()) {
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = data.type,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        if (data.isMedia) {
            AniVuIconButton(
                onClick = {
                    try {
                        PlayActivity.play(context.activity, Uri.parse(data.url))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = stringResource(id = R.string.play),
            )
        }
        if (isMagnetOrTorrent) {
            AniVuIconButton(
                onClick = { onDownload(data) },
                imageVector = Icons.Outlined.Download,
                contentDescription = stringResource(id = R.string.download),
            )
        }
    }
}

@Composable
private fun LinkEnclosureItem(
    data: LinkEnclosureBean,
    onDownload: (LinkEnclosureBean) -> Unit,
) {
    val context = LocalContext.current
    val isMagnetOrTorrent = rememberSaveable {
        data.link.startsWith("magnet:") ||
                Regex("^(http|https)://.*\\.torrent$").matches(data.link)
    }
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier
                .weight(1f)
                .clickable { data.link.copy(context) },
            text = data.link,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 5,
        )
        Spacer(modifier = Modifier.width(12.dp))
        if (data.isMedia) {
            AniVuIconButton(
                onClick = {
                    try {
                        PlayActivity.play(context.activity, Uri.parse(data.link))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = stringResource(id = R.string.play),
            )
        }
        if (isMagnetOrTorrent) {
            AniVuIconButton(
                onClick = { onDownload(data) },
                imageVector = Icons.Outlined.Download,
                contentDescription = stringResource(id = R.string.download),
            )
        }
    }
}