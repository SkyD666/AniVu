package com.skyd.anivu.ui.mpv.controller.state.dialog

import androidx.compose.runtime.Immutable

data class SpeedDialogState(
    val show: Boolean,
    val currentSpeed: Float,
) {
    companion object {
        val initial = SpeedDialogState(
            show = false,
            currentSpeed = 1f,
        )
    }
}

@Immutable
data class SpeedDialogCallback(
    val onSpeedChanged: (Float) -> Unit,
)