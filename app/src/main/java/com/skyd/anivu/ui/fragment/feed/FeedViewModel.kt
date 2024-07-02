package com.skyd.anivu.ui.fragment.feed

import androidx.lifecycle.viewModelScope
import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.repository.ArticleRepository
import com.skyd.anivu.model.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepo: FeedRepository,
    private val articleRepo: ArticleRepository,
) : AbstractMviViewModel<FeedIntent, FeedState, FeedEvent>() {

    override val viewState: StateFlow<FeedState>

    init {
        val initialVS = FeedState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<FeedIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is FeedIntent.Init }
        )
            .shareWhileSubscribed()
            .toFeedPartialStateChangeFlow()
            .debugLog("FeedPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<FeedPartialStateChange>.sendSingleEvent(): Flow<FeedPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is FeedPartialStateChange.AddFeed.Success ->
                    FeedEvent.AddFeedResultEvent.Success(change.feed)

                is FeedPartialStateChange.AddFeed.Failed ->
                    FeedEvent.AddFeedResultEvent.Failed(change.msg)

                is FeedPartialStateChange.EditFeed.Success ->
                    FeedEvent.EditFeedResultEvent.Success(change.feed)

                is FeedPartialStateChange.EditFeed.Failed ->
                    FeedEvent.EditFeedResultEvent.Failed(change.msg)

                is FeedPartialStateChange.RemoveFeed.Success ->
                    FeedEvent.RemoveFeedResultEvent.Success

                is FeedPartialStateChange.RemoveFeed.Failed ->
                    FeedEvent.RemoveFeedResultEvent.Failed(change.msg)

                is FeedPartialStateChange.RefreshFeed.Success ->
                    FeedEvent.RefreshFeedResultEvent.Success

                is FeedPartialStateChange.RefreshFeed.Failed ->
                    FeedEvent.RefreshFeedResultEvent.Failed(change.msg)

                is FeedPartialStateChange.FeedList.Failed ->
                    FeedEvent.InitFeetListResultEvent.Failed(change.msg)

                is FeedPartialStateChange.CreateGroup.Success ->
                    FeedEvent.CreateGroupResultEvent.Success

                is FeedPartialStateChange.CreateGroup.Failed ->
                    FeedEvent.CreateGroupResultEvent.Failed(change.msg)

                is FeedPartialStateChange.DeleteGroup.Success ->
                    FeedEvent.DeleteGroupResultEvent.Success

                is FeedPartialStateChange.DeleteGroup.Failed ->
                    FeedEvent.DeleteGroupResultEvent.Failed(change.msg)

                is FeedPartialStateChange.MoveFeedsToGroup.Success ->
                    FeedEvent.MoveFeedsToGroupResultEvent.Success

                is FeedPartialStateChange.MoveFeedsToGroup.Failed ->
                    FeedEvent.MoveFeedsToGroupResultEvent.Failed(change.msg)

                is FeedPartialStateChange.EditGroup.Success ->
                    FeedEvent.EditGroupResultEvent.Success(change.group)

                is FeedPartialStateChange.EditGroup.Failed ->
                    FeedEvent.EditGroupResultEvent.Failed(change.msg)

                is FeedPartialStateChange.ReadAll.Success ->
                    FeedEvent.ReadAllResultEvent.Success(change.count)

                is FeedPartialStateChange.ReadAll.Failed ->
                    FeedEvent.ReadAllResultEvent.Failed(change.msg)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<FeedIntent>.toFeedPartialStateChangeFlow(): Flow<FeedPartialStateChange> {
        return merge(
            filterIsInstance<FeedIntent.Init>().flatMapConcat {
                feedRepo.requestGroupAnyList().map {
                    FeedPartialStateChange.FeedList.Success(dataList = it)
                }.startWith(FeedPartialStateChange.FeedList.Loading)
            },
            filterIsInstance<FeedIntent.AddFeed>().flatMapConcat { intent ->
                feedRepo.setFeed(
                    url = intent.url,
                    nickname = intent.nickname,
                    groupId = intent.group.groupId,
                ).map {
                    FeedPartialStateChange.AddFeed.Success(it)
                }.startWith(FeedPartialStateChange.LoadingDialog.Show)
                    .catchMap { FeedPartialStateChange.AddFeed.Failed(it.message.toString()) }
            },
            merge(
                filterIsInstance<FeedIntent.EditFeedUrl>().map { intent ->
                    feedRepo.editFeedUrl(oldUrl = intent.oldUrl, newUrl = intent.newUrl)
                },
                filterIsInstance<FeedIntent.EditFeedGroup>().map { intent ->
                    feedRepo.editFeedGroup(url = intent.url, groupId = intent.groupId)
                },
                filterIsInstance<FeedIntent.EditFeedCustomDescription>().map { intent ->
                    feedRepo.editFeedCustomDescription(
                        url = intent.url, customDescription = intent.customDescription,
                    )
                },
                filterIsInstance<FeedIntent.EditFeedCustomIcon>().map { intent ->
                    feedRepo.editFeedCustomIcon(url = intent.url, customIcon = intent.customIcon)
                },
                filterIsInstance<FeedIntent.EditFeedNickname>().map { intent ->
                    feedRepo.editFeedNickname(url = intent.url, nickname = intent.nickname)
                },
            ).flatMapConcat { flow ->
                flow.map { FeedPartialStateChange.EditFeed.Success(it) }
                    .startWith(FeedPartialStateChange.LoadingDialog.Show)
                    .catchMap { FeedPartialStateChange.EditFeed.Failed(it.message.toString()) }
            },
            filterIsInstance<FeedIntent.RemoveFeed>().flatMapConcat { intent ->
                feedRepo.removeFeed(intent.url).map {
                    if (it > 0) FeedPartialStateChange.RemoveFeed.Success
                    else FeedPartialStateChange.RemoveFeed.Failed("Remove failed!")
                }.startWith(FeedPartialStateChange.LoadingDialog.Show)
            },
            merge(
                filterIsInstance<FeedIntent.ReadAllInGroup>()
                    .map { intent -> feedRepo.readAllInGroup(intent.groupId) },
                filterIsInstance<FeedIntent.ReadAllInFeed>()
                    .map { intent -> feedRepo.readAllInFeed(intent.feedUrl) },
            ).flatMapConcat { flow ->
                flow.map { FeedPartialStateChange.ReadAll.Success(it) }
                    .startWith(FeedPartialStateChange.LoadingDialog.Show)
                    .catchMap { FeedPartialStateChange.ReadAll.Failed(it.message.toString()) }
            },
            merge(
                filterIsInstance<FeedIntent.RefreshFeed>()
                    .map { intent -> articleRepo.refreshArticleList(listOf(intent.url)) },
                filterIsInstance<FeedIntent.RefreshGroupFeed>()
                    .map { intent -> articleRepo.refreshGroupArticles(intent.groupId) },
            ).flatMapConcat { flow ->
                flow.map { FeedPartialStateChange.RefreshFeed.Success }
                    .startWith(FeedPartialStateChange.LoadingDialog.Show)
                    .catchMap { FeedPartialStateChange.RefreshFeed.Failed(it.message.toString()) }
            },
            filterIsInstance<FeedIntent.CreateGroup>().flatMapConcat { intent ->
                feedRepo.createGroup(intent.group).map {
                    FeedPartialStateChange.CreateGroup.Success
                }.startWith(FeedPartialStateChange.LoadingDialog.Show)
                    .catchMap { FeedPartialStateChange.CreateGroup.Failed(it.message.toString()) }
            },
            filterIsInstance<FeedIntent.DeleteGroup>().flatMapConcat { intent ->
                feedRepo.deleteGroup(intent.groupId).map {
                    FeedPartialStateChange.DeleteGroup.Success
                }.startWith(FeedPartialStateChange.LoadingDialog.Show)
                    .catchMap { FeedPartialStateChange.DeleteGroup.Failed(it.message.toString()) }
            },
            filterIsInstance<FeedIntent.RenameGroup>().flatMapConcat { intent ->
                feedRepo.renameGroup(intent.groupId, intent.name).map {
                    FeedPartialStateChange.EditGroup.Success(it)
                }.startWith(FeedPartialStateChange.LoadingDialog.Show)
                    .catchMap { FeedPartialStateChange.EditGroup.Failed(it.message.toString()) }
            },
            filterIsInstance<FeedIntent.MoveFeedsToGroup>().flatMapConcat { intent ->
                feedRepo.moveGroupFeedsTo(intent.fromGroupId, intent.toGroupId).map {
                    FeedPartialStateChange.MoveFeedsToGroup.Success
                }.startWith(FeedPartialStateChange.LoadingDialog.Show)
                    .catchMap { FeedPartialStateChange.MoveFeedsToGroup.Failed(it.message.toString()) }
            },
        )
    }
}