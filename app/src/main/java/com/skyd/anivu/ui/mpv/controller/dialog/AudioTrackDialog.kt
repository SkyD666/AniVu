package com.skyd.anivu.ui.mpv.controller.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ui.mpv.controller.state.dialog.track.AudioTrackDialogCallback
import com.skyd.anivu.ui.mpv.controller.state.dialog.track.AudioTrackDialogState


@Composable
internal fun AudioTrackDialog(
    onDismissRequest: () -> Unit,
    audioTrackDialogState: () -> AudioTrackDialogState,
    audioTrackDialogCallback: AudioTrackDialogCallback,
) {
    val state = audioTrackDialogState()

    if (state.show) {
        BasicPlayerDialog(onDismissRequest = onDismissRequest) {
            Column(
                modifier = Modifier
                    .padding(PaddingValues(16.dp))
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 6.dp),
                    text = stringResource(id = R.string.player_audio_track),
                    style = MaterialTheme.typography.headlineSmall,
                )
                repeat(state.audioTrack.size) { index ->
                    val track = state.audioTrack[index]
                    TrackDialogListItem(
                        imageVector = if (state.currentAudioTrack.trackId == track.trackId)
                            Icons.Rounded.Check else null,
                        iconContentDescription = stringResource(id = R.string.item_selected),
                        text = track.name,
                        onClick = { audioTrackDialogCallback.onAudioTrackChanged(track) }
                    )
                }
            }
        }
    }
}