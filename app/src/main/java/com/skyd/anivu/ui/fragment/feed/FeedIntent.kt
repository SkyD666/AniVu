package com.skyd.anivu.ui.fragment.feed

import com.skyd.anivu.base.mvi.MviIntent
import com.skyd.anivu.model.bean.GroupBean

sealed interface FeedIntent : MviIntent {
    data object Init : FeedIntent
    data class AddFeed(val url: String, val nickname: String?, val group: GroupBean) :
        FeedIntent

    data class EditFeed(
        val oldUrl: String,
        val newUrl: String,
        val nickname: String?,
        val groupId: String?,
    ) : FeedIntent

    data class RemoveFeed(val url: String) : FeedIntent
    data class CreateGroup(val group: GroupBean) : FeedIntent
    data class DeleteGroup(val groupId: String) : FeedIntent
    data class MoveFeedsToGroup(val fromGroupId: String, val toGroupId: String) : FeedIntent
}