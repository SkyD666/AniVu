package com.skyd.anivu.ui.fragment.search

import com.skyd.anivu.base.mvi.MviIntent

sealed interface SearchIntent : MviIntent {
    data object Init : SearchIntent
    data class SearchAll(val query: String) : SearchIntent
    data class SearchFeed(val query: String) : SearchIntent
    data class SearchArticle(val feedUrls: List<String>, val query: String) : SearchIntent
}