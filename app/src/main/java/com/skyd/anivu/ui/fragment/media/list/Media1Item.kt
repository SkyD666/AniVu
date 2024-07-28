package com.skyd.anivu.ui.fragment.media.list

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
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
import java.io.File
import java.util.Locale

@Composable
fun Media1Item(
    data: VideoBean,
    onPlay: (VideoBean) -> Unit,
    onOpenDir: (VideoBean) -> Unit,
    onRemove: (VideoBean) -> Unit,
    onLongClick: ((VideoBean) -> Unit)? = null,
) {
    val context = LocalContext.current
    var expandMenu by rememberSaveable { mutableStateOf(false) }
    val retriever: MediaMetadataRetriever = remember { MediaMetadataRetriever() }

    val isMedia = rememberSaveable(data) { data.isMedia }
    val isDir = rememberSaveable(data) { data.isDir }

    val fileNameWithoutExtension = if (isDir) data.name else data.name.substringBeforeLast(".")
    val fileExtension = if (isDir) "" else data.name.substringAfterLast(".", "")


    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(0.1f))
            .combinedClickable(
                onLongClick = {
                    if (onLongClick == null) {
                        expandMenu = true
                    } else {
                        expandMenu = false
                        onLongClick(data)
                    }
                }
            ) {
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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .size(50.dp),
            contentAlignment = Alignment.Center,
        ) {
            var bitmap by remember(data) { mutableStateOf<Bitmap?>(null) }
            val fileIcon = @Composable {
                Icon(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(id = data.icon),
                    contentDescription = null
                )
            }
            if (isMedia) {
                LaunchedEffect(data) {
                    bitmap = withContext(Dispatchers.IO) {
                        getMediaThumbnail(retriever = retriever, file = data.file)
                    }
                }
                if (bitmap != null) {
                    AniVuImage(
                        modifier = Modifier.fillMaxSize(),
                        model = bitmap,
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    fileIcon()
                }
            } else {
                fileIcon()
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                modifier = Modifier.wrapContentHeight(),
                text = data.displayName ?: fileNameWithoutExtension,
                maxLines = 3,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (fileExtension.isNotBlank()) {
                    Text(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(horizontal = 4.dp),
                        text = fileExtension.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = TextUnit(10f, TextUnitType.Sp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = data.size.fileSize(context),
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = data.date.toDateTimeString(context = context),
                    style = MaterialTheme.typography.labelMedium,
                )
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
                        data.file
                            .toUri(context)
                            .openWith(context)
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
}

private fun getMediaThumbnail(retriever: MediaMetadataRetriever, file: File): Bitmap? {
    runCatching { retriever.setDataSource(file.path) }
        .onFailure { return null }
    val duration = retriever
        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        ?.toLongOrNull() ?: return null
    return retriever.getFrameAtTime(
        (1000 * duration) shr 1,
        MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
    )
}