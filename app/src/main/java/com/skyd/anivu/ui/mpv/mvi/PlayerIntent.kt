package com.skyd.anivu.ui.mpv.mvi

import com.skyd.anivu.base.mvi.MviIntent

sealed interface PlayerIntent : MviIntent {
    data class TrySeekToLast(val path: String, val duration: Long) : PlayerIntent
}