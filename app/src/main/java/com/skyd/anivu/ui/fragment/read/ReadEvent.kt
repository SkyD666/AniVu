package com.skyd.anivu.ui.fragment.read

import com.skyd.anivu.base.mvi.MviSingleEvent

sealed interface ReadEvent : MviSingleEvent {
    sealed interface FavoriteArticleResultEvent : ReadEvent {
        data class Failed(val msg: String) : FavoriteArticleResultEvent
    }

    sealed interface ReadArticleResultEvent : ReadEvent {
        data class Failed(val msg: String) : ReadArticleResultEvent
    }
}