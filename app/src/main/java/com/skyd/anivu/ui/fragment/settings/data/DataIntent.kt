package com.skyd.anivu.ui.fragment.settings.data

import com.skyd.anivu.base.mvi.MviIntent

sealed interface DataIntent : MviIntent {
    data object ClearCache : DataIntent
}