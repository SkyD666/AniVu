package com.skyd.anivu.ui.mpv.state

import androidx.compose.ui.geometry.Offset

data class TransformState(
    val videoRotate: Float,
    val videoZoom: Float,
    val videoOffset: Offset,
) {
    companion object {
        val initial = TransformState(
            videoRotate = 0f,
            videoZoom = 1f,
            videoOffset = Offset.Zero,
        )
    }
}