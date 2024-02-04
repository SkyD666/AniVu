package com.skyd.anivu.ui.fragment.article

import com.skyd.anivu.base.mvi.MviSingleEvent

sealed interface ArticleEvent : MviSingleEvent {
    sealed interface InitArticleListResultEvent : ArticleEvent {
        data class Failed(val msg: String) : InitArticleListResultEvent
    }
    sealed interface RefreshArticleListResultEvent : ArticleEvent {
        data class Failed(val msg: String) : RefreshArticleListResultEvent
    }
}