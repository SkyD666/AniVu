package com.skyd.anivu.ui.activity

import android.annotation.SuppressLint
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
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.skyd.anivu.base.BaseActivity
import com.skyd.anivu.databinding.ActivityPlayBinding
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.preference.player.PlayerShow85sButtonPreference
import com.skyd.anivu.ui.player.TorrentDataSource


@SuppressLint("UnsafeOptInUsageError")
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
        playerView.setForward85sButton(dataStore.getOrDefault(PlayerShow85sButtonPreference))
//        playerView.setOnScreenshotListener {
//            val retriever = MediaMetadataRetriever()
//            retriever.setDataSource(player.curren)
//            val bitmap = retriever.getFrameAtTime(simpleExoPlayer.getCurrentPosition())
//        }
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
        if (videoUri.toString().startsWith("magnet:")) {
            player.setMediaSource(DefaultMediaSourceFactory { TorrentDataSource() }
                .createMediaSource(MediaItem.fromUri(videoUri!!)))
        } else {
            // Set the media item to be played.
            player.setMediaItem(MediaItem.fromUri(videoUri!!))
        }
//        player.setMediaItem(MediaItem.fromUri(Uri.parse("magnet:?xt=urn:btih:344a65fde5ab561370ad5f144319a9f4951ac125&dn=%5BLPSub%5D%20Kaii%20to%20Otome%20to%20Kamikakushi%20%5B01%5D%5BAVC%20AAC%5D%5B1080p%5D%5BJPTC%5D.mp4&tr=udp%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=http%3A%2F%2F104.143.10.186%3A8000%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=udp%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=udp%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce&tr=http%3A%2F%2Ftracker.publicbt.com%3A80%2Fannounce&tr=http%3A%2F%2Ftracker.prq.to%2Fannounce&tr=http%3A%2F%2Fopen.acgtracker.com%3A1096%2Fannounce&tr=https%3A%2F%2Ft-115.rhcloud.com%2Fonly_for_ylbud&tr=udp%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=udp%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=http%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=http%3A%2F%2Ftracker2.itzmx.com%3A6961%2Fannounce&tr=http%3A%2F%2Ftracker3.itzmx.com%3A6961%2Fannounce&tr=http%3A%2F%2Ftracker4.itzmx.com%3A2710%2Fannounce")))
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