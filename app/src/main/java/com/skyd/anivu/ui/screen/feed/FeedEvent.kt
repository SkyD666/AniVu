package com.skyd.anivu.ui.screen.feed

import com.skyd.anivu.base.mvi.MviSingleEvent
import com.skyd.anivu.model.bean.feed.FeedBean
import com.skyd.anivu.model.bean.group.GroupVo

sealed interface FeedEvent : MviSingleEvent {
    sealed interface InitFeetListResultEvent : FeedEvent {
        data class Failed(val msg: String) : InitFeetListResultEvent
    }

    sealed interface AddFeedResultEvent : FeedEvent {
        data class Success(val feed: FeedBean) : AddFeedResultEvent
        data class Failed(val msg: String) : AddFeedResultEvent
    }

    sealed interface EditFeedResultEvent : FeedEvent {
        data class Success(val feed: FeedBean) : EditFeedResultEvent
        data class Failed(val msg: String) : EditFeedResultEvent
    }

    sealed interface RemoveFeedResultEvent : FeedEvent {
        data object Success : RemoveFeedResultEvent
        data class Failed(val msg: String) : RemoveFeedResultEvent
    }

    sealed interface ClearFeedArticlesResultEvent : FeedEvent {
        data object Success : ClearFeedArticlesResultEvent
        data class Failed(val msg: String) : ClearFeedArticlesResultEvent
    }

    sealed interface RefreshFeedResultEvent : FeedEvent {
        data object Success : RefreshFeedResultEvent
        data class Failed(val msg: String) : RefreshFeedResultEvent
    }

    sealed interface CreateGroupResultEvent : FeedEvent {
        data object Success : CreateGroupResultEvent
        data class Failed(val msg: String) : CreateGroupResultEvent
    }

    sealed interface DeleteGroupResultEvent : FeedEvent {
        data object Success : DeleteGroupResultEvent
        data class Failed(val msg: String) : DeleteGroupResultEvent
    }

    sealed interface ClearGroupArticlesResultEvent : FeedEvent {
        data object Success : ClearGroupArticlesResultEvent
        data class Failed(val msg: String) : ClearGroupArticlesResultEvent
    }

    sealed interface MoveFeedsToGroupResultEvent : FeedEvent {
        data object Success : MoveFeedsToGroupResultEvent
        data class Failed(val msg: String) : MoveFeedsToGroupResultEvent
    }

    sealed interface EditGroupResultEvent : FeedEvent {
        data class Success(val group: GroupVo) : EditGroupResultEvent
        data class Failed(val msg: String) : EditGroupResultEvent
    }

    sealed interface ReadAllResultEvent : FeedEvent {
        data class Success(val count: Int) : ReadAllResultEvent
        data class Failed(val msg: String) : ReadAllResultEvent
    }
}