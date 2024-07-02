package com.skyd.anivu.ui.fragment.download

import androidx.lifecycle.viewModelScope
import com.skyd.anivu.appContext
import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.base.mvi.MviSingleEvent
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.repository.DownloadRepository
import com.skyd.anivu.model.worker.download.DownloadTorrentWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val downloadRepo: DownloadRepository
) : AbstractMviViewModel<DownloadIntent, DownloadState, MviSingleEvent>() {

    override val viewState: StateFlow<DownloadState>

    init {
        val initialVS = DownloadState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<DownloadIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is DownloadIntent.Init }
        )
            .shareWhileSubscribed()
            .toReadPartialStateChangeFlow()
            .debugLog("DownloadPartialStateChange")
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun SharedFlow<DownloadIntent>.toReadPartialStateChangeFlow(): Flow<DownloadPartialStateChange> {
        return merge(
            filterIsInstance<DownloadIntent.Init>().flatMapConcat {
                downloadRepo.requestDownloadingVideos().map {
                    DownloadPartialStateChange.DownloadListResult.Success(downloadInfoBeanList = it)
                }.startWith(DownloadPartialStateChange.DownloadListResult.Loading)
                    .catchMap { DownloadPartialStateChange.DownloadListResult.Failed(it.message.toString()) }
            },
            filterIsInstance<DownloadIntent.AddDownload>().flatMapConcat { intent ->
                flow<Unit> {
                    DownloadTorrentWorker.startWorker(
                        context = appContext,
                        torrentLink = intent.link,
                    )
                }.map {
                    DownloadPartialStateChange.AddDownloadResult.Success
                }.startWith(DownloadPartialStateChange.LoadingDialog.Show)
                    .catchMap { DownloadPartialStateChange.AddDownloadResult.Failed(it.message.toString()) }
            },
        )
    }
}