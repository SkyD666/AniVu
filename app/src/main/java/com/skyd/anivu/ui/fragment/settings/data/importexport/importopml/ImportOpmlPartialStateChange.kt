package com.skyd.anivu.ui.fragment.settings.data.importexport.importopml

import com.skyd.anivu.model.repository.importexport.ImportExportRepository


internal sealed interface ImportOpmlPartialStateChange {
    fun reduce(oldState: ImportOpmlState): ImportOpmlState

    sealed interface LoadingDialog : ImportOpmlPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: ImportOpmlState) = oldState.copy(loadingDialog = true)
        }
    }

    data object Init : ImportOpmlPartialStateChange {
        override fun reduce(oldState: ImportOpmlState) = oldState.copy(loadingDialog = false)
    }

    sealed interface ImportOpml : ImportOpmlPartialStateChange {
        override fun reduce(oldState: ImportOpmlState): ImportOpmlState {
            return when (this) {
                is Success -> oldState.copy(
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data class Success(val result: ImportExportRepository.ImportOpmlResult) : ImportOpml
        data class Failed(val msg: String) : ImportOpml
    }
}