package com.skyd.anivu.ui.fragment.media

import com.skyd.anivu.base.mvi.MviIntent
import java.io.File

sealed interface MediaIntent : MviIntent {
    data class Init(val path: String?) : MediaIntent
    data class Refresh(val path: String?) : MediaIntent
    data class Delete(val file: File) : MediaIntent
}