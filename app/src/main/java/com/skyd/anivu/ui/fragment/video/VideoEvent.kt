package com.skyd.anivu.ui.fragment.video

import com.skyd.anivu.base.mvi.MviSingleEvent

sealed interface VideoEvent : MviSingleEvent {
    sealed interface DeleteUriResultEvent : VideoEvent {
        data class Failed(val msg: String) : DeleteUriResultEvent
    }
}