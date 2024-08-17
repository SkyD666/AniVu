package com.skyd.anivu.ui.screen.feed.reorder

import androidx.lifecycle.viewModelScope
import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.repository.feed.ReorderGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class ReorderGroupViewModel @Inject constructor(
    private val reorderGroupRepo: ReorderGroupRepository,
) : AbstractMviViewModel<ReorderGroupIntent, ReorderGroupState, ReorderGroupEvent>() {

    override val viewState: StateFlow<ReorderGroupState>

    init {
        val initialVS = ReorderGroupState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<ReorderGroupIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is ReorderGroupIntent.Init }
        )
            .shareWhileSubscribed()
            .toReorderGroupPartialStateChangeFlow()
            .debugLog("ReorderGroupPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<ReorderGroupPartialStateChange>.sendSingleEvent(): Flow<ReorderGroupPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is ReorderGroupPartialStateChange.Reorder.Failed ->
                    ReorderGroupEvent.ReorderResultEvent.Failed(change.msg)

                is ReorderGroupPartialStateChange.GroupList.Failed ->
                    ReorderGroupEvent.GroupListResultEvent.Failed(change.msg)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<ReorderGroupIntent>.toReorderGroupPartialStateChangeFlow(): Flow<ReorderGroupPartialStateChange> {
        return merge(
            filterIsInstance<ReorderGroupIntent.Init>().flatMapConcat {
                reorderGroupRepo.requestGroupList().map {
                    ReorderGroupPartialStateChange.GroupList.Success(dataList = it)
                }.startWith(ReorderGroupPartialStateChange.LoadingDialog.Show)
                    .catchMap { ReorderGroupPartialStateChange.GroupList.Failed(it.message.orEmpty()) }
            },
            filterIsInstance<ReorderGroupIntent.Reset>().flatMapConcat {
                reorderGroupRepo.requestResetGroupOrder().map {
                    ReorderGroupPartialStateChange.Reset.Success(dataList = it)
                }.startWith(ReorderGroupPartialStateChange.LoadingDialog.Show)
                    .catchMap { ReorderGroupPartialStateChange.Reset.Failed(it.message.orEmpty()) }
            },
            filterIsInstance<ReorderGroupIntent.Reorder>().flatMapConcat { intent ->
                reorderGroupRepo.requestReorderGroup(
                    intent.movedGroupId,
                    intent.newPreviousGroupId,
                    intent.newNextGroupId,
                ).map {
                    if (it) ReorderGroupPartialStateChange.Reorder.Success
                    else ReorderGroupPartialStateChange.Reorder.Failed("Reorder error: $intent")
                }.catchMap { ReorderGroupPartialStateChange.Reorder.Failed(it.message.orEmpty()) }
            },
            filterIsInstance<ReorderGroupIntent.ReorderView>().flatMapConcat { intent ->
                flowOf(Unit).map {
                    ReorderGroupPartialStateChange.ReorderView.Success(intent.from, intent.to)
                }.catchMap<ReorderGroupPartialStateChange.ReorderView> {
                    ReorderGroupPartialStateChange.ReorderView.Failed(it.message.orEmpty())
                }
            },
        )
    }
}