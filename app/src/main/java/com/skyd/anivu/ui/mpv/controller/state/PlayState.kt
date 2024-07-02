package com.skyd.anivu.ui.mpv.controller.state

import androidx.compose.runtime.Immutable

data class PlayState(
    val isPlaying: Boolean,
    val isSeeking: Boolean,
    val currentPosition: Int,
    val duration: Int,
    val bufferDuration: Int,
    val speed: Float,
    val title: String,
) {
    companion object {
        val initial = PlayState(
            isPlaying = false,
            isSeeking = false,
            currentPosition = 0,
            duration = 0,
            bufferDuration = 0,
            speed = 1f,
            title = "",
        )
    }
}

@Immutable
data class PlayStateCallback(
    val onPlayStateChanged: () -> Unit,
    val onPlayOrPause: () -> Unit,
    val onSeekTo: (position: Int) -> Unit,
    val onSpeedChanged: (Float) -> Unit,
)