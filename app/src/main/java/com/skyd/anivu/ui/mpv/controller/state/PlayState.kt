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
    val mediaTitle: String,
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
            mediaTitle = "",
        )
    }

    fun copyIfNecessary(
        isPlaying: Boolean = this.isPlaying,
        isSeeking: Boolean = this.isSeeking,
        currentPosition: Int = this.currentPosition,
        duration: Int = this.duration,
        bufferDuration: Int = this.bufferDuration,
        speed: Float = this.speed,
        title: String = this.title,
        mediaTitle: String = this.mediaTitle,
    ): PlayState {
        return if (isPlaying != this.isPlaying ||
            isSeeking != this.isSeeking ||
            currentPosition != this.currentPosition ||
            duration != this.duration ||
            bufferDuration != this.bufferDuration ||
            speed != this.speed ||
            title != this.title ||
            mediaTitle != this.mediaTitle
        ) copy(
            isPlaying = isPlaying,
            isSeeking = isSeeking,
            currentPosition = currentPosition,
            duration = duration,
            bufferDuration = bufferDuration,
            speed = speed,
            title = title,
            mediaTitle = mediaTitle
        ) else this
    }
}

@Immutable
data class PlayStateCallback(
    val onPlayStateChanged: () -> Unit,
    val onPlayOrPause: () -> Unit,
    val onSeekTo: (position: Int) -> Unit,
    val onSpeedChanged: (Float) -> Unit,
)