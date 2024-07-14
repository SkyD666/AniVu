package com.skyd.anivu.ui.fragment.media.list

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MediaListViewModel @Inject constructor(
    private val mediaRepo: MediaRepository
) : AbstractMviViewModel<MediaListIntent, MediaListState, MediaListEvent>() {

    override val viewState: StateFlow<MediaListState>

    init {
        val initialVS = MediaListState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<MediaListIntent.Init>().distinctUntilChanged(),
            intentSharedFlow.filterNot { it is MediaListIntent.Init }
        )
            .shareWhileSubscribed()
            .toMediaListPartialStateChangeFlow()
            .debugLog("MediaListPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<MediaListPartialStateChange>.sendSingleEvent(): Flow<MediaListPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is MediaListPartialStateChange.MediaListResult.Failed ->
                    MediaListEvent.MediaListResultEvent.Failed(change.msg)

                is MediaListPartialStateChange.DeleteFileResult.Failed ->
                    MediaListEvent.DeleteFileResultEvent.Failed(change.msg)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<MediaListIntent>.toMediaListPartialStateChangeFlow(): Flow<MediaListPartialStateChange> {
        return merge(
            merge(
                filterIsInstance<MediaListIntent.Init>().filterNot { it.path.isNullOrBlank() },
                filterIsInstance<MediaListIntent.Refresh>().filterNot { it.path.isNullOrBlank() },
            ).flatMapConcat { intent ->
                val path = if (intent is MediaListIntent.Init) intent.path
                else (intent as MediaListIntent.Refresh).path
                val group = if (intent is MediaListIntent.Init) intent.group
                else (intent as MediaListIntent.Refresh).group
                combine(
                    mediaRepo.requestFiles(uriPath = path!!, group),
                    mediaRepo.requestGroups(uriPath = path),
                ) { files, groups ->
                    MediaListPartialStateChange.MediaListResult.Success(
                        list = files,
                        groups = groups
                    )
                }.startWith(MediaListPartialStateChange.LoadingDialog.Show)
                    .catchMap { MediaListPartialStateChange.MediaListResult.Failed(it.message.toString()) }
            },
            filterIsInstance<MediaListIntent.DeleteFile>().flatMapConcat { intent ->
                mediaRepo.requestDelete(intent.file).map {
                    MediaListPartialStateChange.DeleteFileResult.Success(file = intent.file)
                }.startWith(MediaListPartialStateChange.LoadingDialog.Show)
                    .catchMap { MediaListPartialStateChange.DeleteFileResult.Failed(it.message.toString()) }
            },
        )
    }
}