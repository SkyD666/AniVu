package com.skyd.anivu.ui.fragment.media

import com.skyd.anivu.base.mvi.MviSingleEvent

sealed interface MediaEvent : MviSingleEvent {
    sealed interface DeleteUriResultEvent : MediaEvent {
        data class Failed(val msg: String) : DeleteUriResultEvent
    }
}