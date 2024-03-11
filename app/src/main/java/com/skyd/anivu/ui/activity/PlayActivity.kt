package com.skyd.anivu.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.IntentCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.skyd.anivu.base.BaseActivity
import com.skyd.anivu.databinding.ActivityPlayBinding

class PlayActivity : BaseActivity<ActivityPlayBinding>() {
    companion object {
        const val VIDEO_URI_KEY = "videoUri"
    }

    private val player: ExoPlayer by lazy { ExoPlayer.Builder(this@PlayActivity).build() }
    private var videoUri: Uri? = null
    private var beforePauseIsPlaying = false

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
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) {
            val data = IntentCompat.getParcelableExtra(intent, VIDEO_URI_KEY, Uri::class.java)
                ?: intent.data
            if (data != null) {
                videoUri = data
                play()
            }
        }
    }

    override fun ActivityPlayBinding.initView() {
        videoUri = IntentCompat.getParcelableExtra(intent, VIDEO_URI_KEY, Uri::class.java)
            ?: intent.data

        if (videoUri != null) {
            playerView.setOnBackButtonClickListener { finish() }
            // Attach player to the view.
            playerView.player = player
            play()
        }
    }

    private fun play(): Boolean {
        if (videoUri == null) {
            return false
        }
        // Set the media item to be played.
        player.setMediaItem(MediaItem.fromUri(videoUri!!))
        // Prepare the player.
        player.prepare()
        player.play()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        player.stop()
        player.release()
    }

    override fun onResume() {
        super.onResume()

        if (beforePauseIsPlaying) {
            beforePauseIsPlaying = false
            player.play()
        }
    }

    override fun onPause() {
        super.onPause()

        if (player.isPlaying) {
            beforePauseIsPlaying = true
            player.pause()
        }
    }

    override fun getViewBinding() = ActivityPlayBinding.inflate(layoutInflater)
}