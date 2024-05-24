package com.skyd.anivu.ui.mpv.controller.state

import androidx.compose.runtime.Immutable
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

@Immutable
data class TransformStateCallback(
    val onVideoRotate: (Float) -> Unit,
    val onVideoZoom: (Float) -> Unit,
    val onVideoOffset: (Offset) -> Unit,
)