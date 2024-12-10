package com.skyd.anivu.ui.mpv.controller.state.dialog.track

import androidx.compose.runtime.Immutable
import com.skyd.anivu.ui.mpv.MPVPlayer

data class AudioTrackDialogState(
    val show: Boolean,
    val currentAudioTrack: MPVPlayer.Track,
    val audioTrack: List<MPVPlayer.Track>,
) {
    companion object {
        val initial = AudioTrackDialogState(
            show = false,
            currentAudioTrack = MPVPlayer.Track(0, ""),
            audioTrack = emptyList(),
        )
    }

    fun copyIfNecessary(
        show: Boolean = this.show,
        currentAudioTrack: MPVPlayer.Track = this.currentAudioTrack,
        audioTrack: List<MPVPlayer.Track> = this.audioTrack,
    ): AudioTrackDialogState {
        return if (show != this.show ||
            currentAudioTrack != this.currentAudioTrack ||
            audioTrack != this.audioTrack
        ) copy(
            show = show,
            currentAudioTrack = currentAudioTrack,
            audioTrack = audioTrack,
        ) else this
    }
}

@Immutable
data class AudioTrackDialogCallback(
    val onAudioTrackChanged: (MPVPlayer.Track) -> Unit,
    val onAddAudioTrack: (String) -> Unit,
)