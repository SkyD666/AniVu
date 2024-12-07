package com.skyd.anivu.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import androidx.core.util.Consumer
import com.skyd.anivu.base.BaseComposeActivity
import com.skyd.anivu.ext.savePictureToMediaStore
import com.skyd.anivu.ui.component.showToast
import com.skyd.anivu.ui.mpv.MPVView
import com.skyd.anivu.ui.mpv.PlayerView
import com.skyd.anivu.ui.mpv.copyAssetsForMpv
import java.io.File


class PlayActivity : BaseComposeActivity() {
    companion object {
        const val VIDEO_URI_KEY = "videoUri"
        const val VIDEO_TITLE_KEY = "videoTitle"

        fun play(activity: Activity, uri: Uri, title: String? = null) {
            activity.startActivity(
                Intent(activity, PlayActivity::class.java).apply {
                    putExtra(VIDEO_URI_KEY, uri)
                    putExtra(VIDEO_TITLE_KEY, title)
                }
            )
        }
    }

    private var player: MPVView? = null
    private lateinit var picture: File
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            picture.savePictureToMediaStore(this)
        } else {
            getString(com.skyd.anivu.R.string.player_no_permission_cannot_save_screenshot).showToast()
        }
    }

    private var videoUri by mutableStateOf<Uri?>(null)
    private var videoTitle by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        copyAssetsForMpv(this)

        super.onCreate(savedInstanceState)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        handleIntent(intent)

        setContentBase {
            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { newIntent -> handleIntent(newIntent) }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }
            videoUri?.let { uri ->
                PlayerView(
                    uri = uri,
                    title = videoTitle,
                    onBack = { finish() },
                    onSaveScreenshot = {
                        picture = it
                        saveScreenshot()
                    },
                    onPlayerChanged = { player = it }
                )
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return

        videoUri = IntentCompat.getParcelableExtra(
            intent, VIDEO_URI_KEY, Uri::class.java
        ) ?: intent.data
        videoTitle = intent.getStringExtra(VIDEO_TITLE_KEY)
    }

    private fun saveScreenshot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            picture.savePictureToMediaStore(this)
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (player?.onKey(event) == true) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}