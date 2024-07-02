package com.skyd.anivu.ui.fragment.search

import com.skyd.anivu.base.mvi.MviSingleEvent

sealed interface SearchEvent : MviSingleEvent {
    sealed interface FavoriteArticleResultEvent : SearchEvent {
        data class Failed(val msg: String) : FavoriteArticleResultEvent
    }

    sealed interface ReadArticleResultEvent : SearchEvent {
        data class Failed(val msg: String) : ReadArticleResultEvent
    }
}