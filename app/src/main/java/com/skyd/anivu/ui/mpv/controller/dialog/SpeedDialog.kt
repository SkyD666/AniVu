package com.skyd.anivu.ui.mpv.controller.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ui.mpv.controller.state.dialog.SpeedDialogCallback
import com.skyd.anivu.ui.mpv.controller.state.dialog.SpeedDialogState


val speeds = listOf(
    3f, 2f, 1.75f, 1.5f, 1.25f, 1f, 0.75f, 0.5f, 0.25f,
)

@Composable
internal fun SpeedDialog(
    onDismissRequest: () -> Unit,
    speedDialogState: () -> SpeedDialogState,
    speedDialogCallback: SpeedDialogCallback,
) {
    val state = speedDialogState()
    if (state.show) {
        BasicPlayerDialog(onDismissRequest = onDismissRequest) {
            Column(modifier = Modifier.padding(PaddingValues(16.dp))) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 6.dp),
                    text = stringResource(id = R.string.player_speed),
                    style = MaterialTheme.typography.headlineSmall,
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                ) {
                    items(speeds.size) { index ->
                        val speed = speeds[index]
                        TrackDialogListItem(
                            imageVector = if (state.currentSpeed == speed)
                                Icons.Rounded.Check else null,
                            iconContentDescription = stringResource(id = R.string.item_selected),
                            text = "${speed}x",
                            onClick = {
                                speedDialogCallback.onSpeedChanged(speed)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }
    }
}