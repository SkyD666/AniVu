package com.skyd.anivu.ui.mpv.controller.state.dialog.track

import androidx.compose.runtime.Immutable
import com.skyd.anivu.ui.mpv.MPVPlayer

data class SubtitleTrackDialogState(
    val show: Boolean,
    val currentSubtitleTrack: MPVPlayer.Track,
    val subtitleTrack: List<MPVPlayer.Track>,
) {
    companion object {
        val initial = SubtitleTrackDialogState(
            show = false,
            currentSubtitleTrack = MPVPlayer.Track(0, ""),
            subtitleTrack = emptyList(),
        )
    }

    fun copyIfNecessary(
        show: Boolean = this.show,
        currentSubtitleTrack: MPVPlayer.Track = this.currentSubtitleTrack,
        subtitleTrack: List<MPVPlayer.Track> = this.subtitleTrack,
    ): SubtitleTrackDialogState {
        return if (show != this.show ||
            currentSubtitleTrack != this.currentSubtitleTrack ||
            subtitleTrack != this.subtitleTrack
        ) copy(
            show = show,
            currentSubtitleTrack = currentSubtitleTrack,
            subtitleTrack = subtitleTrack,
        ) else this
    }
}

@Immutable
data class SubtitleTrackDialogCallback(
    val onSubtitleTrackChanged: (MPVPlayer.Track) -> Unit,
    val onAddSubtitle: (String) -> Unit,
)