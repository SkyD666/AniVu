package com.skyd.anivu.ui.fragment.media

import androidx.lifecycle.viewModelScope
import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.repository.MediaRepository
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
class MediaViewModel @Inject constructor(
    private val mediaRepo: MediaRepository
) : AbstractMviViewModel<MediaIntent, MediaState, MediaEvent>() {

    override val viewState: StateFlow<MediaState>

    init {
        val initialVS = MediaState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<MediaIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is MediaIntent.Init }
        )
            .shareWhileSubscribed()
            .toMediaPartialStateChangeFlow()
            .debugLog("MediaPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<MediaPartialStateChange>.sendSingleEvent(): Flow<MediaPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is MediaPartialStateChange.DeleteUriResult.Failed -> {
                    MediaEvent.DeleteUriResultEvent.Failed(change.msg)
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<MediaIntent>.toMediaPartialStateChangeFlow(): Flow<MediaPartialStateChange> {
        return merge(
            merge(
                filterIsInstance<MediaIntent.Init>().filterNot { it.path.isNullOrBlank() },
                filterIsInstance<MediaIntent.Refresh>().filterNot { it.path.isNullOrBlank() },
            ).flatMapConcat { intent ->
                val path = if (intent is MediaIntent.Init) intent.path
                else (intent as MediaIntent.Refresh).path
                mediaRepo.requestMedias(path = path!!).map {
                    MediaPartialStateChange.MediaListResult.Success(list = it)
                }.startWith(MediaPartialStateChange.MediaListResult.Loading)
            },
            filterIsInstance<MediaIntent.Delete>().flatMapConcat { intent ->
                mediaRepo.requestDelete(intent.file).map {
                    MediaPartialStateChange.DeleteUriResult.Success(file = intent.file)
                }.startWith(MediaPartialStateChange.LoadingDialog.Show)
                    .catchMap { MediaPartialStateChange.DeleteUriResult.Failed(it.message.toString()) }
            },
        )
    }
}