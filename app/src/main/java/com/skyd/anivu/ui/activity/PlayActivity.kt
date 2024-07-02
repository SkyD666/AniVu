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
import androidx.compose.runtime.remember
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

        fun play(activity: Activity, uri: Uri) {
            activity.startActivity(
                Intent(activity, PlayActivity::class.java).apply {
                    putExtra(VIDEO_URI_KEY, uri)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        copyAssetsForMpv(this)

        super.onCreate(savedInstanceState)

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
                    onSaveScreenshot = {
                        picture = it
                        saveScreenshot()
                    },
                    onPlayerChanged = { player = it }
                )
            }
        }
    }

    private fun handleIntent(intent: Intent?): Uri? {
        intent ?: return null
        return IntentCompat.getParcelableExtra(intent, VIDEO_URI_KEY, Uri::class.java)
            ?: intent.data
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