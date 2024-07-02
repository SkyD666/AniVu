package com.skyd.anivu.ui.fragment.settings.data.importexport.importopml

import com.skyd.anivu.base.mvi.MviSingleEvent
import com.skyd.anivu.model.repository.importexport.ImportExportRepository

sealed interface ImportOpmlEvent : MviSingleEvent {
    sealed interface ImportOpmlResultEvent : ImportOpmlEvent {
        data class Success(val result: ImportExportRepository.ImportOpmlResult) : ImportOpmlResultEvent
        data class Failed(val msg: String) : ImportOpmlResultEvent
    }
}