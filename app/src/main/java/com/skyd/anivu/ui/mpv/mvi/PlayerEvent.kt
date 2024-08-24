package com.skyd.anivu.ui.mpv.mvi

import com.skyd.anivu.base.mvi.MviSingleEvent

sealed interface PlayerEvent : MviSingleEvent {
    sealed interface TrySeekToLastResultEvent : PlayerEvent {
        data class Success(val position: Long) : TrySeekToLastResultEvent
        data object NoNeed : TrySeekToLastResultEvent
    }
}