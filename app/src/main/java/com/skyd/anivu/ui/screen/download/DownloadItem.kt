package com.skyd.anivu.ui.screen.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ext.fileSize
import com.skyd.anivu.ext.toPercentage
import com.skyd.anivu.model.bean.download.DownloadInfoBean
import com.skyd.anivu.ui.component.AniVuIconButton

@Composable
fun DownloadItem(
    data: DownloadInfoBean,
    onPause: (DownloadInfoBean) -> Unit,
    onResume: (DownloadInfoBean) -> Unit,
    onCancel: (DownloadInfoBean) -> Unit,
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf(data.description) }
    var pauseButtonIcon by remember { mutableStateOf(Icons.Outlined.Pause) }
    var pauseButtonContentDescription by rememberSaveable { mutableStateOf("") }
    var pauseButtonEnabled by rememberSaveable { mutableStateOf(true) }
    var cancelButtonEnabled by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(data.downloadState) {
        when (data.downloadState) {
            DownloadInfoBean.DownloadState.Seeding -> {
                pauseButtonEnabled = true
                pauseButtonIcon = Icons.Outlined.Pause
                pauseButtonContentDescription = context.getString(R.string.download_pause)
                description = context.getString(R.string.download_seeding)
            }

            DownloadInfoBean.DownloadState.Downloading -> {
                pauseButtonEnabled = true
                pauseButtonIcon = Icons.Outlined.Pause
                pauseButtonContentDescription = context.getString(R.string.download_pause)
                description = context.getString(R.string.downloading)
            }

            DownloadInfoBean.DownloadState.StorageMovedFailed,
            DownloadInfoBean.DownloadState.ErrorPaused -> {
                pauseButtonEnabled = true
                pauseButtonIcon = Icons.Outlined.Refresh
                pauseButtonContentDescription = context.getString(R.string.download_retry)
                description = context.getString(R.string.download_error_paused)
            }

            DownloadInfoBean.DownloadState.SeedingPaused -> {
                pauseButtonEnabled = true
                pauseButtonIcon = Icons.Outlined.CloudUpload
                pauseButtonContentDescription =
                    context.getString(R.string.download_click_to_seeding)
                description = context.getString(R.string.download_paused)
            }

            DownloadInfoBean.DownloadState.Paused -> {
                pauseButtonEnabled = true
                pauseButtonIcon = Icons.Outlined.PlayArrow
                pauseButtonContentDescription = context.getString(R.string.download)
                description = context.getString(R.string.download_paused)
            }

            DownloadInfoBean.DownloadState.Init -> {
                pauseButtonEnabled = false
                pauseButtonIcon = Icons.Outlined.PlayArrow
                pauseButtonContentDescription = context.getString(R.string.download)
                description = context.getString(R.string.download_initializing)
            }

            DownloadInfoBean.DownloadState.Completed -> {
                pauseButtonEnabled = true
                pauseButtonIcon = Icons.Outlined.CloudUpload
                pauseButtonContentDescription =
                    context.getString(R.string.download_click_to_seeding)
                description = context.getString(R.string.download_completed)
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = data.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    description?.let { desc ->
                        Text(
                            modifier = Modifier.padding(end = 12.dp),
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.download_peer_count,
                            data.peerInfoList.count()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Text(
                        text = data.progress.toPercentage(),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = stringResource(
                            R.string.download_download_payload_rate,
                            data.downloadPayloadRate.toLong().fileSize(context) + "/s"
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = stringResource(
                            R.string.download_upload_payload_rate,
                            data.uploadPayloadRate.toLong().fileSize(context) + "/s"
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            AniVuIconButton(
                enabled = pauseButtonEnabled,
                onClick = {
                    when (data.downloadState) {
                        DownloadInfoBean.DownloadState.Seeding,
                        DownloadInfoBean.DownloadState.Downloading -> onPause(data)

                        DownloadInfoBean.DownloadState.SeedingPaused,
                        DownloadInfoBean.DownloadState.Paused -> onResume(data)

                        DownloadInfoBean.DownloadState.Completed,
                        DownloadInfoBean.DownloadState.StorageMovedFailed,
                        DownloadInfoBean.DownloadState.ErrorPaused -> onResume(data)

                        else -> Unit
                    }
                },
                imageVector = pauseButtonIcon,
                contentDescription = pauseButtonContentDescription,
            )
            AniVuIconButton(
                enabled = cancelButtonEnabled,
                onClick = {
                    onCancel(data)
                    pauseButtonEnabled = false
                    cancelButtonEnabled = false
                },
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(id = R.string.delete)
            )
        }
        ProgressIndicator(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth(),
            data = data,
        )
    }
}

@Composable
private fun ProgressIndicator(
    modifier: Modifier = Modifier,
    data: DownloadInfoBean
) {
    when (data.downloadState) {
        DownloadInfoBean.DownloadState.Downloading,
        DownloadInfoBean.DownloadState.StorageMovedFailed,
        DownloadInfoBean.DownloadState.ErrorPaused,
        DownloadInfoBean.DownloadState.Paused -> {
            LinearProgressIndicator(modifier = modifier, progress = { data.progress })
        }

        DownloadInfoBean.DownloadState.Init -> LinearProgressIndicator(modifier = modifier)
        DownloadInfoBean.DownloadState.Seeding,
        DownloadInfoBean.DownloadState.SeedingPaused,
        DownloadInfoBean.DownloadState.Completed -> LinearProgressIndicator(
            modifier = modifier,
            progress = { 1f },
        )
    }
}