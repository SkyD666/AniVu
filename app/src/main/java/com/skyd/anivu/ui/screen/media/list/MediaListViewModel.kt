package com.skyd.anivu.ui.screen.media.list

import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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
import javax.inject.Inject

@HiltViewModel
class MediaListViewModel @Inject constructor(
    private val mediaRepo: MediaRepository
) : AbstractMviViewModel<MediaListIntent, MediaListState, MediaListEvent>() {

    override val viewState: StateFlow<MediaListState>

    init {
        val initialVS = MediaListState.initial()

        viewState = merge(
            intentFlow.filterIsInstance<MediaListIntent.Init>().distinctUntilChanged(),
            intentFlow.filterNot { it is MediaListIntent.Init }
        )
            .toMediaListPartialStateChangeFlow()
            .debugLog("MediaListPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .toState(initialVS)
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

    private fun Flow<MediaListIntent>.toMediaListPartialStateChangeFlow(): Flow<MediaListPartialStateChange> {
        return merge(
            filterIsInstance<MediaListIntent.Init>().flatMapConcat { intent ->
                combine(
                    mediaRepo.requestFiles(path = intent.path, intent.group),
                    mediaRepo.requestGroups(path = intent.path),
                ) { files, groups ->
                    MediaListPartialStateChange.MediaListResult.Success(
                        list = files, groups = groups
                    )
                }.startWith(MediaListPartialStateChange.LoadingDialog.Show)
                    .catchMap { MediaListPartialStateChange.MediaListResult.Failed(it.message.toString()) }
            },
            filterIsInstance<MediaListIntent.Refresh>().flatMapConcat {
                mediaRepo.refreshFile().map {
                    MediaListPartialStateChange.RefreshFilesResult.Success
                }.startWith(MediaListPartialStateChange.LoadingDialog.Show)
                    .catchMap { MediaListPartialStateChange.RefreshFilesResult.Failed(it.message.toString()) }
            },
            filterIsInstance<MediaListIntent.DeleteFile>().flatMapConcat { intent ->
                mediaRepo.deleteFile(intent.file).map {
                    MediaListPartialStateChange.DeleteFileResult.Success(file = intent.file)
                }.startWith(MediaListPartialStateChange.LoadingDialog.Show)
                    .catchMap { MediaListPartialStateChange.DeleteFileResult.Failed(it.message.toString()) }
            },
            filterIsInstance<MediaListIntent.RenameFile>().flatMapConcat { intent ->
                mediaRepo.renameFile(intent.file, intent.newName).map { newFile ->
                    MediaListPartialStateChange.RenameFileResult.Success(
                        oldFile = intent.file, newFile = newFile!!
                    )
                }.startWith(MediaListPartialStateChange.LoadingDialog.Show)
                    .catchMap { MediaListPartialStateChange.RenameFileResult.Failed(it.message.toString()) }
            },
            filterIsInstance<MediaListIntent.SetFileDisplayName>().flatMapConcat { intent ->
                mediaRepo.setFileDisplayName(intent.media, intent.displayName).map {
                    MediaListPartialStateChange.SetFileDisplayNameResult.Success(
                        media = intent.media, displayName = intent.displayName
                    )
                }.startWith(MediaListPartialStateChange.LoadingDialog.Show)
                    .catchMap { MediaListPartialStateChange.SetFileDisplayNameResult.Failed(it.message.toString()) }
            },
        )
    }
}