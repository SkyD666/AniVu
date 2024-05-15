package com.skyd.anivu.ui.mpv

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R

data class SubtitleTrackMenuState(
    val expanded: Boolean,
    val currentSubtitleTrack: MPVView.Track,
    val subtitleTrack: List<MPVView.Track>,
)

@Composable
internal fun SubtitleTrackMenu(
    onDismissRequest: () -> Unit,
    subtitleTrackMenuState: () -> SubtitleTrackMenuState,
    onSubtitleTrackChanged: (MPVView.Track) -> Unit,
) {
    val state = subtitleTrackMenuState()
    DropdownMenu(
        expanded = state.expanded,
        onDismissRequest = onDismissRequest,
        containerColor = ControllerLabelGray,
        shadowElevation = 0.dp,
    ) {
        repeat(state.subtitleTrack.size) { index ->
            val track = state.subtitleTrack[index]
            DropdownMenuItem(
                text = { Text(track.name) },
                onClick = { onSubtitleTrackChanged(track) },
                leadingIcon = {
                    if (state.currentSubtitleTrack.trackId == track.trackId) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(id = R.string.item_selected)
                        )
                    }
                },
                colors = MenuDefaults.itemColors().copy(
                    textColor = Color.White,
                    leadingIconColor = Color.White,
                )
            )
        }
    }
}