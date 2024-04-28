package com.skyd.anivu.ui.fragment.media

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ext.fileSize
import com.skyd.anivu.ext.openWith
import com.skyd.anivu.ext.share
import com.skyd.anivu.ext.toDateTimeString
import com.skyd.anivu.ext.toUri
import com.skyd.anivu.model.bean.VideoBean
import com.skyd.anivu.ui.component.AniVuImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Media1Item(
    data: VideoBean,
    onPlay: (VideoBean) -> Unit,
    onOpenDir: (VideoBean) -> Unit,
    onRemove: (VideoBean) -> Unit,
) {
    val context = LocalContext.current
    var expandMenu by rememberSaveable { mutableStateOf(false) }
    val retriever: MediaMetadataRetriever = remember { MediaMetadataRetriever() }

    val isMedia = rememberSaveable(data) { data.isMedia(context) }
    val isDir = rememberSaveable(data) { data.isDir }

    Column(
        modifier = Modifier
            .combinedClickable(onLongClick = { expandMenu = true }) {
                if (isDir) {
                    onOpenDir(data)
                } else if (isMedia) {
                    onPlay(data)
                } else {
                    data.file
                        .toUri(context)
                        .openWith(context)
                }
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        var bitmap by remember(data) { mutableStateOf<Bitmap?>(null) }
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isMedia) {
                LaunchedEffect(data) {
                    bitmap = withContext(Dispatchers.IO) {
                        getMediaThumbnail(retriever = retriever, path = data.file.path)
                    }
                }
                OutlinedCard(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .heightIn(min = 50.dp)
                        .fillMaxHeight()
                        .width(70.dp),
                ) {
                    AniVuImage(
                        model = bitmap,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            Text(text = data.name, maxLines = 3, style = MaterialTheme.typography.titleSmall)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = data.size.fileSize(context),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = data.date.toDateTimeString(context = context),
                style = MaterialTheme.typography.labelMedium,
            )
            if (isMedia || isDir) {
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = if (isMedia) Icons.Outlined.Movie else Icons.Outlined.Folder,
                    contentDescription = stringResource(id = R.string.video),
                )
            }
        }

        DropdownMenu(
            expanded = expandMenu,
            onDismissRequest = { expandMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.open_with)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                    )
                },
                onClick = {
                    data.file.toUri(context).openWith(context)
                    expandMenu = false
                },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.share)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                    )
                },
                onClick = {
                    data.file.toUri(context).share(context)
                    expandMenu = false
                },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.remove)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                    )
                },
                onClick = {
                    onRemove(data)
                    expandMenu = false
                },
            )
        }
    }
}

private fun getMediaThumbnail(retriever: MediaMetadataRetriever, path: String): Bitmap? {
    runCatching { retriever.setDataSource(path) }
        .onFailure { return null }
    val duration = retriever
        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        ?.toLongOrNull() ?: return null
    return retriever.getFrameAtTime(
        (1000 * duration) shr 1,
        MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
    )
}