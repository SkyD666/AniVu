package com.skyd.anivu.ui.mpv.mvi

import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.model.bean.MediaPlayHistoryBean
import com.skyd.anivu.model.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepo: PlayerRepository,
) : AbstractMviViewModel<PlayerIntent, PlayerState, PlayerEvent>() {

    private val globalScope = CoroutineScope(Dispatchers.IO)

    fun updatePlayHistory(path: String, lastPlayPosition: Long) {
        globalScope.launch {
            playerRepo.updatePlayHistory(
                MediaPlayHistoryBean(path, lastPlayPosition)
            ).collect()
        }
    }

    override val viewState: StateFlow<PlayerState>

    init {
        val initialVS = PlayerState.initial()

        viewState = merge(
            intentFlow.filterIsInstance<PlayerIntent.TrySeekToLast>().take(1),
            intentFlow.filterNot { it is PlayerIntent.TrySeekToLast }
        )
            .toPlayerPartialStateChangeFlow()
            .debugLog("PlayerPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .toState(initialVS)
    }

    private fun Flow<PlayerPartialStateChange>.sendSingleEvent(): Flow<PlayerPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is PlayerPartialStateChange.TrySeekToLast.Success -> {
                    PlayerEvent.TrySeekToLastResultEvent.Success(change.position)
                }

                is PlayerPartialStateChange.TrySeekToLast.NoNeed -> {
                    PlayerEvent.TrySeekToLastResultEvent.NoNeed
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun Flow<PlayerIntent>.toPlayerPartialStateChangeFlow(): Flow<PlayerPartialStateChange> {
        return merge(
            filterIsInstance<PlayerIntent.TrySeekToLast>().flatMapConcat { intent ->
                playerRepo.requestLastPlayPosition(intent.path).map {
                    if (it.toDouble() / intent.duration > 0.9) {
                        PlayerPartialStateChange.TrySeekToLast.NoNeed
                    } else {
                        PlayerPartialStateChange.TrySeekToLast.Success(it)
                    }
                }.catchMap<PlayerPartialStateChange> {
                    PlayerPartialStateChange.TrySeekToLast.Failed(it.message.toString())
                }
            },
        )
    }
}