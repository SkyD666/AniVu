package com.skyd.anivu.ui.fragment.article

import com.skyd.anivu.base.mvi.MviIntent

sealed interface ArticleIntent : MviIntent {
    data class Init(val url: String) : ArticleIntent
    data class Refresh(val url: String) : ArticleIntent
}