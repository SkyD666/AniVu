package com.skyd.anivu.ui.fragment.settings.data

import androidx.lifecycle.viewModelScope
import com.skyd.anivu.R
import com.skyd.anivu.appContext
import com.skyd.anivu.base.mvi.AbstractMviViewModel
import com.skyd.anivu.ext.catchMap
import com.skyd.anivu.ext.fileSize
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    private val dataRepo: DataRepository
) : AbstractMviViewModel<DataIntent, DataState, DataEvent>() {

    override val viewState: StateFlow<DataState>

    init {
        val initialVS = DataState.initial()

        viewState = intentSharedFlow
            .toDataPartialStateChangeFlow()
            .debugLog("DataPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<DataPartialStateChange>.sendSingleEvent(): Flow<DataPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is DataPartialStateChange.ClearCacheResult.Success -> {
                    DataEvent.ClearCacheResultEvent.Success(
                        appContext.getString(
                            R.string.data_fragment_data_cleared_size,
                            change.deletedSize.fileSize(appContext),
                        )
                    )
                }

                is DataPartialStateChange.ClearCacheResult.Failed -> {
                    DataEvent.ClearCacheResultEvent.Failed(change.msg)
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<DataIntent>.toDataPartialStateChangeFlow(): Flow<DataPartialStateChange> {
        return merge(
            filterIsInstance<DataIntent.ClearCache>().flatMapConcat {
                dataRepo.requestClearCache().map {
                    DataPartialStateChange.ClearCacheResult.Success(deletedSize = it)
                }.startWith(DataPartialStateChange.LoadingDialog.Show)
                    .catchMap { DataPartialStateChange.ClearCacheResult.Failed(it.message.toString()) }
            },
        )
    }
}