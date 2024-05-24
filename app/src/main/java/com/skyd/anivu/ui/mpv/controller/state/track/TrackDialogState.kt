package com.skyd.anivu.ui.mpv.controller.state.track

import androidx.compose.runtime.Immutable

@Immutable
data class TrackDialogState(
    val audioTrackDialogState: () -> AudioTrackDialogState,
    val subtitleTrackDialogState: () -> SubtitleTrackDialogState,
) {
    companion object {
        val initial = TrackDialogState(
            audioTrackDialogState = { AudioTrackDialogState.initial },
            subtitleTrackDialogState = { SubtitleTrackDialogState.initial },
        )
    }
}