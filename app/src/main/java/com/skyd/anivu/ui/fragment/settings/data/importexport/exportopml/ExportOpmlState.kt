package com.skyd.anivu.ui.fragment.settings.data.importexport.exportopml

import com.skyd.anivu.base.mvi.MviViewState

data class ExportOpmlState(
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = ExportOpmlState(
            loadingDialog = false,
        )
    }
}