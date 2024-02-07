package com.skyd.anivu.ui.fragment.video

import androidx.lifecycle.viewModelScope
import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.repository.VideoRepository
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
class VideoViewModel @Inject constructor(
    private val videoRepo: VideoRepository
) : AbstractMviViewModel<VideoIntent, VideoState, VideoEvent>() {

    override val viewState: StateFlow<VideoState>

    init {
        val initialVS = VideoState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<VideoIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is VideoIntent.Init }
        )
            .shareWhileSubscribed()
            .toVideoPartialStateChangeFlow()
            .debugLog("VideoPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<VideoPartialStateChange>.sendSingleEvent(): Flow<VideoPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is VideoPartialStateChange.DeleteUriResult.Failed -> {
                    VideoEvent.DeleteUriResultEvent.Failed(change.msg)
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<VideoIntent>.toVideoPartialStateChangeFlow(): Flow<VideoPartialStateChange> {
        return merge(
            merge(
                filterIsInstance<VideoIntent.Init>().filterNot { it.path.isNullOrBlank() },
                filterIsInstance<VideoIntent.Refresh>().filterNot { it.path.isNullOrBlank() },
            ).flatMapConcat { intent ->
                val path = if (intent is VideoIntent.Init) intent.path
                else (intent as VideoIntent.Refresh).path
                videoRepo.requestVideos(path = path!!).map {
                    VideoPartialStateChange.VideoListResult.Success(videoList = it)
                }.startWith(VideoPartialStateChange.VideoListResult.Loading)
            },
            filterIsInstance<VideoIntent.Delete>().flatMapConcat { intent ->
                videoRepo.requestDelete(intent.file).map {
                    VideoPartialStateChange.DeleteUriResult.Success(file = intent.file)
                }.startWith(VideoPartialStateChange.LoadingDialog.Show)
                    .catchMap { VideoPartialStateChange.DeleteUriResult.Failed(it.message.toString()) }
            },
        )
    }
}