package com.skyd.anivu.ui.mpv

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.skyd.anivu.R
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.rememberSystemUiController
import com.skyd.anivu.ui.mpv.state.SubtitleTrackDialogCallback
import com.skyd.anivu.ui.mpv.state.SubtitleTrackDialogState


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
        BasicAlertDialog(onDismissRequest = onDismissRequest) {
            rememberSystemUiController().apply {
                isSystemBarsVisible = false
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            Surface(
                modifier = Modifier,
                shape = AlertDialogDefaults.shape,
                color = AlertDialogDefaults.containerColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
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
                        SubtitleItem(
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
}

@Composable
private fun SubtitleItem(
    imageVector: ImageVector?,
    iconContentDescription: String? = null,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontSize = TextUnit(16f, TextUnitType.Sp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        if (imageVector != null) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = imageVector,
                contentDescription = iconContentDescription,
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}