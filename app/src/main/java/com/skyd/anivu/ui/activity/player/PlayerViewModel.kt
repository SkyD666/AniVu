package com.skyd.anivu.ui.activity.player

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModel
import com.skyd.anivu.ui.activity.player.PlayActivity.Companion.VIDEO_TITLE_KEY
import com.skyd.anivu.ui.activity.player.PlayActivity.Companion.VIDEO_URI_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {
    val uri: MutableStateFlow<Uri> = MutableStateFlow(Uri.EMPTY)
    val title: MutableStateFlow<String?> = MutableStateFlow(null)

    fun handleIntent(intent: Intent?) {
        intent ?: return

        val uri = IntentCompat.getParcelableExtra(
            intent, VIDEO_URI_KEY, Uri::class.java
        ) ?: intent.data ?: return
        this.uri.tryEmit(uri)
        this.title.tryEmit(intent.getStringExtra(VIDEO_TITLE_KEY))
    }
}