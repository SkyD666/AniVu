package com.skyd.anivu.ui.mpv

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ui.component.dialog.AniVuDialog

data class SubtitleTrackDialogState(
    val show: Boolean,
    val currentSubtitleTrack: MPVView.Track,
    val subtitleTrack: List<MPVView.Track>,
)

@Composable
internal fun SubtitleTrackDialog(
    onDismissRequest: () -> Unit,
    subtitleTrackDialogState: () -> SubtitleTrackDialogState,
    onSubtitleTrackChanged: (MPVView.Track) -> Unit,
) {
    val state = subtitleTrackDialogState()
    AniVuDialog(
        visible = state.show,
        selectable = false,
        scrollable = false,
        icon = null,
        title = { Text(text = stringResource(id = R.string.player_subtitle_track)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                repeat(state.subtitleTrack.size) { index ->
                    val track = state.subtitleTrack[index]
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .fillMaxWidth()
                            .clickable { onSubtitleTrackChanged(track) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        if (state.currentSubtitleTrack.trackId == track.trackId) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(id = R.string.item_selected)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = track.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = TextUnit(16f, TextUnitType.Sp),
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = { },
    )
}