package com.skyd.anivu.ui.mpv.controller.dialog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.mpv.controller.state.dialog.track.SubtitleTrackDialogCallback
import com.skyd.anivu.ui.mpv.controller.state.dialog.track.SubtitleTrackDialogState
import com.skyd.anivu.ui.mpv.resolveUri


@Composable
internal fun SubtitleTrackDialog(
    onDismissRequest: () -> Unit,
    subtitleTrackDialogState: () -> SubtitleTrackDialogState,
    subtitleTrackDialogCallback: SubtitleTrackDialogCallback,
) {
    val state = subtitleTrackDialogState()
    val context = LocalContext.current
    val pickSubtitleFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { subtitleUri ->
        subtitleUri?.resolveUri(context)?.let { filePath ->
            subtitleTrackDialogCallback.onAddSubtitle(filePath)
        }
    }

    if (state.show) {
        BasicPlayerDialog(onDismissRequest = onDismissRequest) {
            Column(
                modifier = Modifier
                    .padding(PaddingValues(16.dp))
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, bottom = 6.dp),
                        text = stringResource(id = R.string.player_subtitle_track),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    AniVuIconButton(
                        onClick = { pickSubtitleFileLauncher.launch("*/*") },
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(id = R.string.player_add_external_subtitle),
                    )
                }
                repeat(state.subtitleTrack.size) { index ->
                    val track = state.subtitleTrack[index]
                    TrackDialogListItem(
                        imageVector = if (state.currentSubtitleTrack.trackId == track.trackId)
                            Icons.Rounded.Check else null,
                        iconContentDescription = stringResource(id = R.string.item_selected),
                        text = track.name,
                        onClick = { subtitleTrackDialogCallback.onSubtitleTrackChanged(track) }
                    )
                }
            }
        }
    }
}