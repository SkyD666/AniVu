package com.skyd.anivu.ui.activity

import android.net.Uri
import android.os.Build
import android.os.Bundle
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

    private val videoUri by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getParcelable(VIDEO_URI_KEY, Uri::class.java)
        } else {
            intent.extras?.getParcelable(VIDEO_URI_KEY)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Add a listener to update the behavior of the toggle fullscreen button when
        // the system bars are hidden or revealed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
                // You can hide the caption bar even when the other system bars are visible.
                // To account for this, explicitly check the visibility of navigationBars()
                // and statusBars() rather than checking the visibility of systemBars().
                if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                    || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
                ) {
                    binding.playerView.setOnClickListener {
                        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                    }
                } else {
                    binding.playerView.setOnClickListener {
                        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                    }
                }
                view.onApplyWindowInsets(windowInsets)
            }
        } else {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun ActivityPlayBinding.initView() {
        if (videoUri == null) {
            return
        }
        val player = ExoPlayer.Builder(this@PlayActivity).build()
        // Attach player to the view.
        playerView.player = player
        // Set the media item to be played.
        player.setMediaItem(MediaItem.fromUri(videoUri!!))
        // Prepare the player.
        player.prepare()
        player.play()
    }

    override fun onDestroy() {
        super.onDestroy()

        binding.playerView.player?.stop()
        binding.playerView.player?.release()
    }

    override fun onResume() {
        super.onResume()

        binding.playerView.player?.play()
    }

    override fun onPause() {
        super.onPause()

        binding.playerView.player?.pause()
    }

    override fun getViewBinding() = ActivityPlayBinding.inflate(layoutInflater)
}