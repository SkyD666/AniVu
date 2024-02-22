package com.skyd.anivu.ui.fragment.settings.data

import com.skyd.anivu.base.mvi.MviSingleEvent

sealed interface DataEvent : MviSingleEvent {
    sealed interface ClearCacheResultEvent : DataEvent {
        data class Success(val msg: String) : ClearCacheResultEvent
        data class Failed(val msg: String) : ClearCacheResultEvent
    }
}