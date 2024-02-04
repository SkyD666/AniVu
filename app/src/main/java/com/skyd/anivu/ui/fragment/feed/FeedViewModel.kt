package com.skyd.anivu.ui.fragment.feed

import androidx.lifecycle.viewModelScope
import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.ext.startWith
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
    private val feedRepo: FeedRepository
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
                is FeedPartialStateChange.AddFeed.Success -> {
                    FeedEvent.AddFeedResultEvent.Success
                }

                is FeedPartialStateChange.AddFeed.Failed -> {
                    FeedEvent.AddFeedResultEvent.Failed(change.msg)
                }

                is FeedPartialStateChange.RemoveFeed.Success -> {
                    FeedEvent.RemoveFeedResultEvent.Success
                }

                is FeedPartialStateChange.RemoveFeed.Failed -> {
                    FeedEvent.RemoveFeedResultEvent.Failed(change.msg)
                }

                is FeedPartialStateChange.FeedList.Failed -> {
                    FeedEvent.InitFeetListResultEvent.Failed(change.msg)
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<FeedIntent>.toFeedPartialStateChangeFlow(): Flow<FeedPartialStateChange> {
        return merge(
            filterIsInstance<FeedIntent.Init>().flatMapConcat {
                feedRepo.requestFeedList().map {
                    FeedPartialStateChange.FeedList.Success(feedList = it)
                }.startWith(FeedPartialStateChange.FeedList.Loading)
            },
            filterIsInstance<FeedIntent.AddFeed>().flatMapConcat { intent ->
                feedRepo.setFeed(intent.url).map {
                    FeedPartialStateChange.AddFeed.Success
                }.startWith(FeedPartialStateChange.LoadingDialog.Show)
                    .catchMap { FeedPartialStateChange.AddFeed.Failed(it.message.toString()) }
            },
            filterIsInstance<FeedIntent.RemoveFeed>().flatMapConcat { intent ->
                feedRepo.removeFeed(intent.url).map {
                    if (it > 0) FeedPartialStateChange.RemoveFeed.Success
                    else FeedPartialStateChange.RemoveFeed.Failed("Remove failed!")
                }.startWith(FeedPartialStateChange.LoadingDialog.Show)
            },
        )
    }
}