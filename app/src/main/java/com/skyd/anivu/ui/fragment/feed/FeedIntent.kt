package com.skyd.anivu.ui.fragment.feed

import com.skyd.anivu.base.mvi.MviIntent

sealed interface FeedIntent : MviIntent {
    data object Init : FeedIntent
    data class AddFeed(val url: String) : FeedIntent
    data class EditFeed(val oldUrl: String, val newUrl: String) : FeedIntent
    data class RemoveFeed(val url: String) : FeedIntent
}