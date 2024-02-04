package com.skyd.anivu.ui.fragment.feed

import com.skyd.anivu.base.mvi.MviSingleEvent

sealed interface FeedEvent : MviSingleEvent {
    sealed interface InitFeetListResultEvent : FeedEvent {
        data class Failed(val msg: String) : InitFeetListResultEvent
    }

    sealed interface AddFeedResultEvent : FeedEvent {
        data object Success : AddFeedResultEvent
        data class Failed(val msg: String) : AddFeedResultEvent
    }

    sealed interface RemoveFeedResultEvent : FeedEvent {
        data object Success : RemoveFeedResultEvent
        data class Failed(val msg: String) : RemoveFeedResultEvent
    }
}