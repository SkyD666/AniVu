package com.skyd.anivu.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.skyd.anivu.base.BaseComposeActivity
import com.skyd.anivu.ui.mpv.PlayerView


class PlayActivity : BaseComposeActivity() {
    companion object {
        const val VIDEO_URI_KEY = "videoUri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            view.onApplyWindowInsets(windowInsets)
        }
        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentBase {
            var videoUri by remember { mutableStateOf(handleIntent(intent)) }

            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { newIntent ->
                    videoUri = handleIntent(newIntent)
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }
            videoUri?.let { uri ->
                PlayerView(
                    uri = uri,
                    onBack = { finish() },
                )
            }
        }
    }

    private fun handleIntent(intent: Intent?): Uri? {
        intent ?: return null
        return IntentCompat.getParcelableExtra(intent, VIDEO_URI_KEY, Uri::class.java)
            ?: intent.data
    }
}