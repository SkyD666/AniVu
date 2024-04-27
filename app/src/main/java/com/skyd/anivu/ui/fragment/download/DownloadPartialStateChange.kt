package com.skyd.anivu.ui.fragment.download

import com.skyd.anivu.model.bean.download.DownloadInfoBean


internal sealed interface DownloadPartialStateChange {
    fun reduce(oldState: DownloadState): DownloadState


    sealed interface LoadingDialog : DownloadPartialStateChange {
        data object Show : LoadingDialog {
            override fun reduce(oldState: DownloadState) = oldState.copy(loadingDialog = true)
        }
    }

    sealed interface DownloadListResult : DownloadPartialStateChange {
        override fun reduce(oldState: DownloadState): DownloadState {
            return when (this) {
                is Success -> oldState.copy(
                    downloadListState = DownloadListState.Success(downloadInfoBeanList = downloadInfoBeanList),
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    downloadListState = DownloadListState.Failed(msg = msg),
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    downloadListState = DownloadListState.Loading,
                    loadingDialog = false,
                )
            }
        }

        data class Success(val downloadInfoBeanList: List<DownloadInfoBean>) : DownloadListResult
        data class Failed(val msg: String) : DownloadListResult
        data object Loading : DownloadListResult
    }

    sealed interface AddDownloadResult : DownloadPartialStateChange {
        override fun reduce(oldState: DownloadState): DownloadState {
            return when (this) {
                is Success -> oldState.copy(
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(
                    loadingDialog = false,
                )
            }
        }

        data object Success : AddDownloadResult
        data class Failed(val msg: String) : AddDownloadResult
    }
}
