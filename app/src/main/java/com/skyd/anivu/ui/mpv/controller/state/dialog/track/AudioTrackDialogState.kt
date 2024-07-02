package com.skyd.anivu.ui.mpv.controller.state.dialog.track

import androidx.compose.runtime.Immutable
import com.skyd.anivu.ui.mpv.MPVView

data class AudioTrackDialogState(
    val show: Boolean,
    val currentAudioTrack: MPVView.Track,
    val audioTrack: List<MPVView.Track>,
) {
    companion object {
        val initial = AudioTrackDialogState(
            show = false,
            currentAudioTrack = MPVView.Track(0, ""),
            audioTrack = emptyList(),
        )
    }
}

@Immutable
data class AudioTrackDialogCallback(
    val onAudioTrackChanged: (MPVView.Track) -> Unit,
    val onAddAudioTrack: (String) -> Unit,
)