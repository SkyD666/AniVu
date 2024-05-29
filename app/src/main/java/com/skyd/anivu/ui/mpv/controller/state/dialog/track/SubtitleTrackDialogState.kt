package com.skyd.anivu.ui.mpv.controller.state.dialog.track

import androidx.compose.runtime.Immutable
import com.skyd.anivu.ui.mpv.MPVView

data class SubtitleTrackDialogState(
    val show: Boolean,
    val currentSubtitleTrack: MPVView.Track,
    val subtitleTrack: List<MPVView.Track>,
) {
    companion object {
        val initial = SubtitleTrackDialogState(
            show = false,
            currentSubtitleTrack = MPVView.Track(0, ""),
            subtitleTrack = emptyList(),
        )
    }
}

@Immutable
data class SubtitleTrackDialogCallback(
    val onSubtitleTrackChanged: (MPVView.Track) -> Unit,
    val onAddSubtitle: (String) -> Unit,
)