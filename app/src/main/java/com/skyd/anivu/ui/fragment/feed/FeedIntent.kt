package com.skyd.anivu.ui.fragment.feed

import android.net.Uri
import com.skyd.anivu.base.mvi.MviIntent
import com.skyd.anivu.model.bean.GroupBean

sealed interface FeedIntent : MviIntent {
    data object Init : FeedIntent
    data class AddFeed(
        val url: String,
        val nickname: String? = null,
        val group: GroupBean = GroupBean.DefaultGroup
    ) : FeedIntent

    data class EditFeedUrl(val oldUrl: String, val newUrl: String) : FeedIntent
    data class EditFeedNickname(val url: String, val nickname: String?) : FeedIntent
    data class EditFeedGroup(val url: String, val groupId: String?) : FeedIntent
    data class EditFeedCustomDescription(val url: String, val customDescription: String?) :
        FeedIntent

    data class EditFeedCustomIcon(val url: String, val customIcon: Uri?) : FeedIntent

    data class RemoveFeed(val url: String) : FeedIntent
    data class ReadAllInFeed(val feedUrl: String) : FeedIntent
    data class ReadAllInGroup(val groupId: String?) : FeedIntent
    data class RefreshFeed(val url: String) : FeedIntent
    data class RefreshGroupFeed(val groupId: String?) : FeedIntent
    data class CreateGroup(val group: GroupBean) : FeedIntent
    data class DeleteGroup(val groupId: String) : FeedIntent
    data class RenameGroup(val groupId: String, val name: String) : FeedIntent
    data class MoveFeedsToGroup(val fromGroupId: String, val toGroupId: String) : FeedIntent
}