/*
* Copyright 2019 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.skyd.anivu.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DoNotInline
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.media3.common.AdOverlayInfo
import androidx.media3.common.AdViewProvider
import androidx.media3.common.C
import androidx.media3.common.ErrorMessageProvider
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.DiscontinuityReason
import androidx.media3.common.Player.PlayWhenReadyChangeReason
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.RepeatModeUtil.RepeatToggleModes
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.AspectRatioFrameLayout.AspectRatioListener
import androidx.media3.ui.AspectRatioFrameLayout.ResizeMode
import androidx.media3.ui.R
import androidx.media3.ui.SubtitleView
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.dp
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.getScreenBrightness
import com.skyd.anivu.ext.tickVibrate
import com.skyd.anivu.ext.tryActivity
import com.skyd.anivu.model.preference.player.PlayerDoubleTapPreference
import com.skyd.anivu.ui.player.PlayerGestureDetector.PlayerGestureListener
import com.skyd.anivu.ui.player.PlayerView.ArtworkDisplayMode
import com.skyd.anivu.ui.player.PlayerView.Companion.ARTWORK_DISPLAY_MODE_FIT
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * A high level view for [Player] media playbacks. It displays video, subtitles and album art
 * during playback, and displays playback controls using a [PlayerControlView].
 *
 * A PlayerView can be customized by setting attributes (or calling corresponding methods), or
 * overriding drawables.
 *
 * <h2>Attributes</h2>
 *
 * The following attributes can be set on a PlayerView when used in a layout XML file:
 *
 *  * **`artwork_display_mode`** - Whether artwork is used if available in audio streams
 * and [ArtworkDisplayMode] how it is displayed.
 *
 *  * Corresponding field: [artworkDisplayMode]
 *  * Default: [ARTWORK_DISPLAY_MODE_FIT]
 *
 *  * **`default_artwork`** - Default artwork to use if no artwork available in audio
 * streams.
 *
 *  * Corresponding field: [defaultArtwork]
 *  * Default: `null`
 *
 *  * **`use_controller`** - Whether the playback controls can be shown.
 *
 *  * Corresponding field: [useController]
 *  * Default: `true`
 *
 *  * **`hide_on_touch`** - Whether the playback controls are hidden by touch events.
 *
 *  * Corresponding field: [controllerHideOnTouch]
 *  * Default: `true`
 *
 *  * **`auto_show`** - Whether the playback controls are automatically shown when
 * playback starts, pauses, ends, or fails. If set to false, the playback controls can be
 * manually operated with [showController] and [hideController].
 *
 *  * Corresponding field: [controllerAutoShow]
 *  * Default: `true`
 *
 *  * **`hide_during_ads`** - Whether the playback controls are hidden during ads.
 * Controls are always shown during ads if they are enabled and the player is paused.
 *
 *  * Corresponding field: [controllerHideDuringAds]
 *  * Default: `true`
 *
 *  * **`show_buffering`** - Whether the buffering spinner is displayed when the player
 * is buffering. Valid values are `never`, `when_playing` and `always`.
 *
 *  * Corresponding field: [showBuffering]
 *  * Default: `never`
 *
 *  * **`resize_mode`** - Controls how video and album art is resized within the view.
 * Valid values are `fit`, `fixed_width`, `fixed_height`, `fill` and
 * `zoom`.
 *
 *  * Corresponding field: [resizeMode]
 *  * Default: `fit`
 *
 *  * **`surface_type`** - The type of surface view used for video playbacks. Valid
 * values are `surface_view`, `texture_view`, `spherical_gl_surface_view`,
 * `video_decoder_gl_surface_view` and `none`. Using `none` is recommended
 * for audio only applications, since creating the surface can be expensive.
 * Using `surface_view` is recommended for video applications.
 * Note, TextureView can only be used in a hardware accelerated window.
 * When rendered in software, TextureView will draw nothing.
 *
 *  * Corresponding method: None
 *  * Default: `surface_view`
 *
 *  * **`shutter_background_color`** - The background color of the `exo_shutter`
 * view.
 *
 *  * Corresponding method: [setShutterBackgroundColor]
 *  * Default: `unset`
 *
 *  * **`keep_content_on_player_reset`** - Whether the currently displayed video frame
 * or media artwork is kept visible when the player is reset.
 *
 *  * Corresponding method: [keepContentOnPlayerReset]
 *  * Default: `false`
 *
 *  * All attributes that can be set on [PlayerControlView] and
 *  [androidx.media3.ui.DefaultTimeBar] can also be set on a PlayerView, and will be
 *  propagated to the inflated [PlayerControlView].
 *
 * <h2>Overriding drawables</h2>
 *
 * The drawables used by [PlayerControlView] can be overridden by drawables with the same
 * names defined in your application. See the [PlayerControlView] documentation for a list of
 * drawables that can be overridden.
 */
@SuppressLint("UnsafeOptInUsageError", "PrivateResource")
class PlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), AdViewProvider {
    /**
     * Listener to be notified about changes of the visibility of the UI controls.
     */
    interface ControllerVisibilityListener {
        /**
         * Called when the visibility changes.
         *
         * @param visibility The new visibility. Either [View.VISIBLE] or [View.GONE].
         */
        fun onVisibilityChanged(visibility: Int)
    }

    /**
     * Listener invoked when the fullscreen button is clicked. The implementation is responsible for
     * changing the UI layout.
     */
    interface FullscreenButtonClickListener {
        /**
         * Called when the fullscreen button is clicked.
         *
         * @param isFullScreen `true` if the video rendering surface should be fullscreen,
         * `false` otherwise.
         */
        fun onFullscreenButtonClick(isFullScreen: Boolean)
    }

    /**
     * Determines the artwork display mode. One of [ARTWORK_DISPLAY_MODE_OFF],
     * [ARTWORK_DISPLAY_MODE_FIT] or [ARTWORK_DISPLAY_MODE_FILL].
     */
    @MustBeDocumented
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.TYPE_PARAMETER)
    @IntDef(ARTWORK_DISPLAY_MODE_OFF, ARTWORK_DISPLAY_MODE_FIT, ARTWORK_DISPLAY_MODE_FILL)
    annotation class ArtworkDisplayMode

    /**
     * Determines when the buffering view is shown. One of [SHOW_BUFFERING_NEVER],
     * [SHOW_BUFFERING_WHEN_PLAYING] or [SHOW_BUFFERING_ALWAYS].
     */
    @MustBeDocumented
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.TYPE_PARAMETER)
    @IntDef(SHOW_BUFFERING_NEVER, SHOW_BUFFERING_WHEN_PLAYING, SHOW_BUFFERING_ALWAYS)
    annotation class ShowBuffering

    private val componentListener: ComponentListener = ComponentListener()
    private val contentFrame: AspectRatioFrameLayout?
    private var shutterView: View? = null

    /**
     * Gets the view onto which video is rendered. This is a:
     *
     * [SurfaceView] by default, or if the `surface_type` attribute is set to `surface_view`.
     * [TextureView] if `surface_type` is `texture_view`.
     * `SphericalGLSurfaceView` if `surface_type` is `spherical_gl_surface_view`.
     * `VideoDecoderGLSurfaceView` if `surface_type` is `video_decoder_gl_surface_view`.
     * `null` if `surface_type` is `none`.
     *
     * @return The [SurfaceView], [TextureView], `SphericalGLSurfaceView`, `VideoDecoderGLSurfaceView` or `null`.
     */
    private val videoSurfaceView: View?
    private val surfaceViewIgnoresVideoAspectRatio: Boolean
    private var artworkView: ImageView? = null

    /**
     * Gets the [SubtitleView].
     *
     * @return The [SubtitleView], or `null` if the layout has been customized and the
     * subtitle view is not present.
     */
    var subtitleView: SubtitleView? = null
    private var bufferingView: View? = null
    private var errorMessageView: TextView? = null
    private val controller: PlayerControlView?
    private var adOverlayFrameLayout: FrameLayout? = null

    /**
     * Gets the overlay [FrameLayout], which can be populated with UI elements to show on top of
     * the player.
     *
     * @return The overlay [FrameLayout], or `null` if the layout has been customized and
     * the overlay is not present.
     */
    private var overlayFrameLayout: FrameLayout? = null

    /**
     * The player currently set on this view, or null if no player is set.
     */
    var player: Player? = null
        /**
         * Sets the [Player] to use.
         *
         * To transition a [Player] from targeting one view to another, it's recommended to use
         * [switchTargetView] rather than this method. If you do wish to use this method directly,
         * be sure to attach the player to the new view *before* calling `player = null` to
         * detach it from the old one. This ordering is significantly more efficient and may allow
         * for more seamless transitions.
         *
         * @param value The [Player] to use, or `null` to detach the current player. Only
         * players which are accessed on the main thread are supported
         * (`value.applicationLooper == Looper.getMainLooper()`).
         */
        set(value) {
            Assertions.checkState(Looper.myLooper() == Looper.getMainLooper())
            Assertions.checkArgument(
                value == null || value.applicationLooper == Looper.getMainLooper()
            )
            if (field === value) {
                return
            }
            field?.let { oldPlayer ->
                oldPlayer.removeListener(componentListener)
                if (oldPlayer.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
                    if (videoSurfaceView is TextureView) {
                        oldPlayer.clearVideoTextureView(videoSurfaceView as? TextureView)
                    } else if (videoSurfaceView is SurfaceView) {
                        oldPlayer.clearVideoSurfaceView(videoSurfaceView as? SurfaceView)
                    }
                }
            }
            subtitleView?.setCues(null)
            field = value
            if (useController() && controller != null) {
                controller.setPlayer(value)
            }
            updateBuffering()
            updateErrorMessage()
            updateForCurrentTrackSelections(true)
            if (value != null) {
                if (value.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
                    if (videoSurfaceView is TextureView) {
                        value.setVideoTextureView(videoSurfaceView as? TextureView)
                    } else if (videoSurfaceView is SurfaceView) {
                        value.setVideoSurfaceView(videoSurfaceView as? SurfaceView)
                    }
                    if (!value.isCommandAvailable(Player.COMMAND_GET_TRACKS)
                        || value.currentTracks.isTypeSupported(C.TRACK_TYPE_VIDEO)
                    ) {
                        // If the player already is or was playing a video,
                        // onVideoSizeChanged isn't called.
                        updateAspectRatio()
                    }
                }
                if (value.isCommandAvailable(Player.COMMAND_GET_TEXT)) {
                    subtitleView?.setCues(value.currentCues.cues)
                }
                value.addListener(componentListener)
                maybeShowController(false)
            } else {
                hideController()
            }
        }

    private var useController: Boolean = true
        /**
         * Sets whether the playback controls can be shown. If set to `false` the playback controls
         * are never visible and are disconnected from the player.
         *
         * This call will update whether the view is clickable. After the call, the view will be
         * clickable if playback controls can be shown or if the view has a registered click listener.
         */
        set(value) {
            Assertions.checkState(!value || controller != null)
            isClickable = value || hasOnClickListeners()
            if (field == value) return
            field = value
            if (useController()) {
                controller?.setPlayer(player)
            } else {
                controller?.hide()
                controller?.setPlayer(null)
            }
            updateContentDescription()
        }

    /**
     * The listener to be notified about visibility changes, or null to remove the
     * current listener.
     */
    private var controllerVisibilityListener: ControllerVisibilityListener? = null

    /**
     * The listener to be notified when the fullscreen button is clicked, or null to
     * remove the current listener and hide the fullscreen button.
     */
    var fullscreenButtonClickListener: FullscreenButtonClickListener? = null
    private var artworkDisplayMode: @ArtworkDisplayMode Int = ARTWORK_DISPLAY_MODE_FIT
        /**
         * Sets whether and how artwork is displayed if present in the media.
         */
        set(value) {
            Assertions.checkState(
                artworkDisplayMode == ARTWORK_DISPLAY_MODE_OFF || artworkView != null
            )
            if (field != value) {
                field = value
                updateForCurrentTrackSelections(false)
            }
        }

    private var defaultArtwork: Drawable? = null
        /**
         * Sets the default artwork to display if `useArtwork` is `true` and no artwork is
         * present in the media.
         */
        set(value) {
            if (field != value) {
                field = value
                updateForCurrentTrackSelections(false)
            }
        }

    /**
     * Whether a buffering spinner is displayed when the player is in the buffering state. The
     * buffering spinner is not displayed by default.
     */
    private var showBuffering: @ShowBuffering Int = SHOW_BUFFERING_NEVER
        /**
         * @param value The mode that defines when the buffering spinner is displayed. One of
         * [SHOW_BUFFERING_NEVER], [SHOW_BUFFERING_WHEN_PLAYING] and [SHOW_BUFFERING_ALWAYS].
         */
        set(value) {
            if (field != value) {
                field = value
                updateBuffering()
            }
        }

    /**
     * Whether the currently displayed video frame or media artwork is kept visible when the
     * player is reset. A player reset is defined to mean the player being re-prepared with different
     * media, the player transitioning to unprepared media or an empty list of media items, or the
     * player being replaced or cleared by calling [player].
     *
     * If enabled, the currently displayed video frame or media artwork will be kept visible until
     * the player set on the view has been successfully prepared with new media and loaded enough of
     * it to have determined the available tracks. Hence enabling this option allows transitioning
     * from playing one piece of media to another, or from using one player instance to another,
     * without clearing the view's content.
     *
     * If disabled, the currently displayed video frame or media artwork will be hidden as soon as
     * the player is reset. Note that the video frame is hidden by making `exo_shutter` visible.
     * Hence the video frame will not be hidden if using a custom layout that omits this view.
     */
    private var keepContentOnPlayerReset = false
        /**
         * @param value Whether the currently displayed video frame or media
         * artwork is kept visible when the player is reset.
         */
        set(value) {
            if (field != value) {
                field = value
                updateForCurrentTrackSelections(false)
            }
        }

    /**
     * Sets the optional [ErrorMessageProvider].
     */
    private var errorMessageProvider: ErrorMessageProvider<in PlaybackException>? = null
        set(value) {
            if (field != value) {
                field = value
                updateErrorMessage()
            }
        }

    /**
     * Sets a custom error message to be displayed by the view. The error message will be displayed
     * permanently, unless it is cleared by passing `null` to this method.
     */
    private var customErrorMessage: CharSequence? = null
        /**
         * @param value The message to display, or `null` to clear a previously set message.
         */
        set(value) {
            Assertions.checkState(errorMessageView != null)
            field = value
            updateErrorMessage()
        }

    /**
     * The playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input and with playback or buffering in progress.
     *
     * A non-positive value will cause the controller to remain visible indefinitely.
     */
    private var controllerShowTimeoutMs: Int = 0
        set(value) {
            field = value
            if (controller?.isFullyVisible == true) {
                // Update the controller's timeout if necessary.
                showController()
            }
        }

    /**
     * Whether the playback controls are automatically shown when playback starts, pauses,
     * ends, or fails. If set to false, the playback controls can be manually operated with
     * [showController] and [hideController].
     */
    private var controllerAutoShow: Boolean = true

    /**
     * Whether the playback controls are hidden when ads are playing. Controls are always shown
     * during ads if they are enabled and the player is paused.
     */
    var controllerHideDuringAds: Boolean = true

    /**
     * Whether the playback controls are hidden by touch events.
     */
    private var controllerHideOnTouch: Boolean = true
        set(value) {
            Assertions.checkStateNotNull(controller)
            field = value
            updateContentDescription()
        }

    private var textureViewRotation = 0

    init {
        if (isInEditMode) {
            contentFrame = null
            videoSurfaceView = null
            surfaceViewIgnoresVideoAspectRatio = false
            controller = null
            val logo = ImageView(context)
            configureEditModeLogo(context, resources, logo)
            addView(logo)
        } else {
            var shutterColorSet = false
            var shutterColor = 0
            var playerLayoutId = R.layout.exo_player_view
            var useArtwork = true
            var artworkDisplayMode = ARTWORK_DISPLAY_MODE_FIT
            var defaultArtworkId = 0
            var useController = true
            var surfaceType = SURFACE_TYPE_SURFACE_VIEW
            var resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            var controllerShowTimeoutMs = PlayerControlView.DEFAULT_SHOW_TIMEOUT_MS
            var controllerHideOnTouch = true
            var controllerAutoShow = true
            var controllerHideDuringAds = true
            var showBuffering = SHOW_BUFFERING_NEVER

            if (attrs != null) {
                val a = context.theme.obtainStyledAttributes(
                    attrs, R.styleable.PlayerView, defStyleAttr, 0
                )
                @Suppress("KotlinConstantConditions")
                try {
                    shutterColorSet = a.hasValue(R.styleable.PlayerView_shutter_background_color)
                    shutterColor = a.getColor(
                        R.styleable.PlayerView_shutter_background_color, shutterColor
                    )
                    playerLayoutId = a.getResourceId(
                        R.styleable.PlayerView_player_layout_id, playerLayoutId
                    )
                    useArtwork = a.getBoolean(R.styleable.PlayerView_use_artwork, useArtwork)
                    artworkDisplayMode = a.getInt(
                        R.styleable.PlayerView_artwork_display_mode, artworkDisplayMode
                    )
                    defaultArtworkId = a.getResourceId(
                        R.styleable.PlayerView_default_artwork, defaultArtworkId
                    )
                    useController = a.getBoolean(
                        R.styleable.PlayerView_use_controller, useController
                    )
                    surfaceType = a.getInt(R.styleable.PlayerView_surface_type, surfaceType)
                    resizeMode = a.getInt(R.styleable.PlayerView_resize_mode, resizeMode)
                    controllerShowTimeoutMs = a.getInt(
                        R.styleable.PlayerView_show_timeout, controllerShowTimeoutMs
                    )
                    controllerHideOnTouch = a.getBoolean(
                        R.styleable.PlayerView_hide_on_touch, controllerHideOnTouch
                    )
                    controllerAutoShow = a.getBoolean(
                        R.styleable.PlayerView_auto_show, controllerAutoShow
                    )
                    showBuffering = a.getInteger(
                        R.styleable.PlayerView_show_buffering, showBuffering
                    )
                    keepContentOnPlayerReset = a.getBoolean(
                        R.styleable.PlayerView_keep_content_on_player_reset,
                        keepContentOnPlayerReset
                    )
                    controllerHideDuringAds = a.getBoolean(
                        R.styleable.PlayerView_hide_during_ads, controllerHideDuringAds
                    )
                } finally {
                    a.recycle()
                }
            }
            LayoutInflater.from(context).inflate(playerLayoutId, this)
            setDescendantFocusability(FOCUS_AFTER_DESCENDANTS)

            // Content frame.
            contentFrame = findViewById<AspectRatioFrameLayout?>(R.id.exo_content_frame)?.apply {
                setResizeModeRaw(this, resizeMode)
            }

            // Shutter view.
            shutterView = findViewById<View?>(R.id.exo_shutter).apply {
                if (shutterColorSet) {
                    setBackgroundColor(shutterColor)
                }
            }

            // Create a surface view and insert it into the content frame, if there is one.
            var surfaceViewIgnoresVideoAspectRatio = false
            if (contentFrame != null && surfaceType != SURFACE_TYPE_NONE) {
                val params = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                when (surfaceType) {
                    SURFACE_TYPE_TEXTURE_VIEW -> videoSurfaceView = TextureView(context)
                    SURFACE_TYPE_SPHERICAL_GL_SURFACE_VIEW -> {
                        try {
                            videoSurfaceView = Class.forName(
                                "androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView"
                            ).getConstructor(Context::class.java).newInstance(context) as View
                        } catch (e: Exception) {
                            throw IllegalStateException(
                                "spherical_gl_surface_view requires an ExoPlayer dependency", e
                            )
                        }
                        surfaceViewIgnoresVideoAspectRatio = true
                    }

                    SURFACE_TYPE_VIDEO_DECODER_GL_SURFACE_VIEW -> {
                        try {
                            videoSurfaceView = Class.forName(
                                "androidx.media3.exoplayer.video.VideoDecoderGLSurfaceView"
                            ).getConstructor(Context::class.java).newInstance(context) as View
                        } catch (e: Exception) {
                            throw IllegalStateException(
                                "video_decoder_gl_surface_view requires an ExoPlayer dependency", e
                            )
                        }
                    }

                    else -> {
                        val view = SurfaceView(context)
                        if (Util.SDK_INT >= 34) {
                            Api34.setSurfaceLifecycleToFollowsAttachment(view)
                        }
                        videoSurfaceView = view
                    }
                }
                videoSurfaceView.setLayoutParams(params)
                // We don't want surfaceView to be clickable separately to the PlayerView itself,
                // but we do want to register as an OnClickListener so that surfaceView
                // implementations can propagate click events up to the PlayerView by calling
                // their own performClick method.
                videoSurfaceView.setOnClickListener(componentListener)
                videoSurfaceView.isClickable = false
                contentFrame.addView(videoSurfaceView, 0)
            } else {
                videoSurfaceView = null
            }
            this.surfaceViewIgnoresVideoAspectRatio = surfaceViewIgnoresVideoAspectRatio

            // Ad overlay frame layout.
            adOverlayFrameLayout = findViewById(R.id.exo_ad_overlay)

            // Overlay frame layout.
            overlayFrameLayout = findViewById(R.id.exo_overlay)

            // Artwork view.
            artworkView = findViewById(R.id.exo_artwork)
            val isArtworkEnabled =
                useArtwork && artworkDisplayMode != ARTWORK_DISPLAY_MODE_OFF && artworkView != null
            this.artworkDisplayMode =
                if (isArtworkEnabled) artworkDisplayMode else ARTWORK_DISPLAY_MODE_OFF
            if (defaultArtworkId != 0) {
                defaultArtwork = ContextCompat.getDrawable(context, defaultArtworkId)
            }

            // Subtitle view.
            subtitleView = findViewById<SubtitleView?>(R.id.exo_subtitles)?.apply {
                setUserDefaultStyle()
                setUserDefaultTextSize()
            }

            // Buffering view.
            bufferingView = findViewById<View?>(R.id.exo_buffering)?.apply {
                visibility = GONE
            }
            this.showBuffering = showBuffering

            // Error message view.
            errorMessageView = findViewById<TextView?>(R.id.exo_error_message)?.apply {
                visibility = GONE
            }

            // Playback control view.
            val customController = findViewById<PlayerControlView>(R.id.exo_controller)
            val controllerPlaceholder = findViewById<View>(R.id.exo_controller_placeholder)
            if (customController != null) {
                controller = customController
            } else if (controllerPlaceholder != null) {
                // Propagate attrs as playbackAttrs so that PlayerControlView's custom attributes are
                // transferred, but standard attributes (e.g. background) are not.
                controller = PlayerControlView(context, null, 0, attrs).apply {
                    setId(R.id.exo_controller)
                    setLayoutParams(controllerPlaceholder.layoutParams)
                }
                val parent = controllerPlaceholder.parent as ViewGroup
                val controllerIndex = parent.indexOfChild(controllerPlaceholder)
                parent.removeView(controllerPlaceholder)
                parent.addView(controller, controllerIndex)
            } else {
                controller = null
            }
            this.controllerShowTimeoutMs = if (controller != null) controllerShowTimeoutMs else 0
            this.controllerHideOnTouch = controllerHideOnTouch
            this.controllerAutoShow = controllerAutoShow
            this.controllerHideDuringAds = controllerHideDuringAds
            this.useController = useController && controller != null
            controller?.apply {
                hideImmediately()
                @Suppress("DEPRECATION")
                controller.addVisibilityListener(componentListener)
                onZoomStateChanged(false)
                setOnResetZoomButtonClickListener {
                    playerGestureDetector.restoreZoom(contentFrame)
                }
            }
            if (useController) {
                isClickable = true
            }
            updateContentDescription()
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        // Work around https://github.com/google/ExoPlayer/issues/3160.
        (videoSurfaceView as? SurfaceView)?.setVisibility(visibility)
    }

    private var resizeMode: @ResizeMode Int
        get() = contentFrame!!.resizeMode
        set(resizeMode) {
            contentFrame!!.resizeMode = resizeMode
        }

    /**
     * Sets the background color of the `exo_shutter` view.
     *
     * @param color The background color.
     */
    private fun setShutterBackgroundColor(@ColorInt color: Int) {
        shutterView?.setBackgroundColor(color)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (player?.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM) == true
            && player?.isPlayingAd == true
        ) {
            return super.dispatchKeyEvent(event)
        }
        val isDpadKey = isDpadKey(event.keyCode)
        var handled = false
        if (isDpadKey && useController() && !controller!!.isFullyVisible) {
            // Handle the key event by showing the controller.
            maybeShowController(true)
            handled = true
        } else if (dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event)) {
            // The key event was handled as a media key or by the super class. We should also show the
            // controller, or extend its show timeout if already visible.
            maybeShowController(true)
            handled = true
        } else if (isDpadKey && useController()) {
            // The key event wasn't handled, but we should extend the controller's show timeout.
            maybeShowController(true)
        }
        return handled
    }

    /**
     * Called to process media key events. Any [KeyEvent] can be passed but only media key
     * events will be handled. Does nothing if playback controls are disabled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    private fun dispatchMediaKeyEvent(event: KeyEvent?): Boolean {
        return useController() && controller!!.dispatchMediaKeyEvent(event)
    }

    /**
     * Whether the controller is currently fully visible.
     */
    val isControllerFullyVisible: Boolean
        get() = controller?.isFullyVisible == true

    /**
     * Shows the playback controls. Does nothing if playback controls are disabled.
     *
     *
     * The playback controls are automatically hidden during playback after
     * [controllerShowTimeoutMs]. They are shown indefinitely when playback has
     * not started yet, is paused, has ended or failed.
     */
    private fun showController() {
        showController(shouldShowControllerIndefinitely())
    }

    /**
     * Hides the playback controls. Does nothing if playback controls are disabled.
     */
    fun hideController() {
        controller?.hide()
    }

    fun setOnBackButtonClickListener(listener: OnClickListener?) {
        controller?.setOnBackButtonClickListener(listener)
    }

    /**
     * Sets whether the rewind button is shown.
     *
     * @param showRewindButton Whether the rewind button is shown.
     */
    fun setShowRewindButton(showRewindButton: Boolean) {
        controller?.setShowRewindButton(showRewindButton)
    }

    /**
     * Sets whether the fast forward button is shown.
     *
     * @param showFastForwardButton Whether the fast forward button is shown.
     */
    fun setShowFastForwardButton(showFastForwardButton: Boolean) {
        controller?.setShowFastForwardButton(showFastForwardButton)
    }

    /**
     * Sets whether the previous button is shown.
     *
     * @param showPreviousButton Whether the previous button is shown.
     */
    fun setShowPreviousButton(showPreviousButton: Boolean) {
        controller?.setShowPreviousButton(showPreviousButton)
    }

    /**
     * Sets whether the next button is shown.
     *
     * @param showNextButton Whether the next button is shown.
     */
    fun setShowNextButton(showNextButton: Boolean) {
        controller?.setShowNextButton(showNextButton)
    }

    /**
     * Sets which repeat toggle modes are enabled.
     *
     * @param repeatToggleModes A set of
     * [androidx.media3.common.util.RepeatModeUtil.RepeatToggleModes].
     */
    fun setRepeatToggleModes(repeatToggleModes: @RepeatToggleModes Int) {
        controller?.setRepeatToggleModes(repeatToggleModes)
    }

    fun setForward85sButton(visible: Boolean) {
        controller?.setForward85sButton(visible)
    }

    fun setOnScreenshotListener(listener: OnClickListener?) {
        controller?.setOnScreenshotListener(listener)
    }

    /**
     * Sets whether the shuffle button is shown.
     *
     * @param showShuffleButton Whether the shuffle button is shown.
     */
    fun setShowShuffleButton(showShuffleButton: Boolean) {
        controller?.setShowShuffleButton(showShuffleButton)
    }

    /**
     * Sets whether the subtitle button is shown.
     *
     * @param showSubtitleButton Whether the subtitle button is shown.
     */
    fun setShowSubtitleButton(showSubtitleButton: Boolean) {
        controller?.showSubtitleButton = showSubtitleButton
    }

    /**
     * Sets whether the vr button is shown.
     *
     * @param showVrButton Whether the vr button is shown.
     */
    fun setShowVrButton(showVrButton: Boolean) {
        controller?.showVrButton = showVrButton
    }

    /**
     * Sets whether a play button is shown if playback is [Player.getPlaybackSuppressionReason].
     * The default is `true`.
     *
     * @param showPlayButtonIfSuppressed Whether to show a play button if playback is
     * [Player.getPlaybackSuppressionReason].
     */
    @UnstableApi
    fun setShowPlayButtonIfPlaybackIsSuppressed(showPlayButtonIfSuppressed: Boolean) {
        controller?.setShowPlayButtonIfPlaybackIsSuppressed(showPlayButtonIfSuppressed)
    }

    /**
     * Sets the millisecond positions of extra ad markers relative to the start of the window (or
     * timeline, if in multi-window mode) and whether each extra ad has been played or not. The
     * markers are shown in addition to any ad markers for ads in the player's timeline.
     *
     * @param extraAdGroupTimesMs The millisecond timestamps of the extra ad markers to show, or
     * `null` to show no extra ad markers.
     * @param extraPlayedAdGroups Whether each ad has been played, or `null` to show no extra ad
     * markers.
     */
    fun setExtraAdGroupMarkers(
        extraAdGroupTimesMs: LongArray?,
        extraPlayedAdGroups: BooleanArray?,
    ) {
        controller?.setExtraAdGroupMarkers(extraAdGroupTimesMs, extraPlayedAdGroups)
    }

    /**
     * Sets the [AspectRatioFrameLayout.AspectRatioListener].
     *
     * @param listener The listener to be notified about aspect ratios changes of the video
     * content or the content frame.
     */
    fun setAspectRatioListener(listener: AspectRatioListener?) {
        contentFrame?.setAspectRatioListener(listener)
    }

    //    @Override
    //    public boolean performClick() {
    //        toggleControllerVisibility();
    //        return super.performClick();
    //    }
    override fun onTrackballEvent(ev: MotionEvent): Boolean {
        if (!useController() || player == null) {
            return false
        }
        maybeShowController(true)
        return true
    }

    /**
     * Should be called when the player is visible to the user, if the `surface_type` extends
     * [GLSurfaceView]. It is the counterpart to [onPause].
     *
     * This method should typically be called in `Activity.onStart()`, or `Activity.onResume()`
     * for API versions &lt;= 23.
     */
    private fun onResume() {
        if (videoSurfaceView is GLSurfaceView) {
            videoSurfaceView.onResume()
        }
    }

    /**
     * Should be called when the player is no longer visible to the user, if the `surface_type`
     * extends [GLSurfaceView]. It is the counterpart to [onResume].
     *
     * This method should typically be called in `Activity.onStop()`, or `Activity.onPause()`
     * for API versions &lt;= 23.
     */
    private fun onPause() {
        if (videoSurfaceView is GLSurfaceView) {
            videoSurfaceView.onPause()
        }
    }

    /**
     * Called when there's a change in the desired aspect ratio of the content frame. The default
     * implementation sets the aspect ratio of the content frame to the specified value.
     *
     * @param contentFrame The content frame, or `null`.
     * @param aspectRatio  The aspect ratio to apply.
     */
    private fun onContentAspectRatioChanged(
        contentFrame: AspectRatioFrameLayout?,
        aspectRatio: Float,
    ) {
        contentFrame?.setAspectRatio(aspectRatio)
    }

    // AdsLoader.AdViewProvider implementation.
    override fun getAdViewGroup(): ViewGroup {
        return Assertions.checkStateNotNull(
            adOverlayFrameLayout, "exo_ad_overlay must be present for ad playback"
        )
    }

    override fun getAdOverlayInfos(): List<AdOverlayInfo> {
        val overlayViews: MutableList<AdOverlayInfo> = mutableListOf()
        overlayFrameLayout?.let { overlayFrameLayout ->
            overlayViews.add(
                AdOverlayInfo.Builder(overlayFrameLayout, AdOverlayInfo.PURPOSE_NOT_VISIBLE)
                    .setDetailedReason("Transparent overlay does not impact viewability")
                    .build()
            )
        }
        if (controller != null) {
            overlayViews.add(
                AdOverlayInfo.Builder(controller, AdOverlayInfo.PURPOSE_CONTROLS).build()
            )
        }
        return overlayViews
    }

    private fun useController(): Boolean {
        if (useController) {
            Assertions.checkStateNotNull(controller)
            return true
        }
        return false
    }

    private fun useArtwork(): Boolean {
        if (artworkDisplayMode != ARTWORK_DISPLAY_MODE_OFF) {
            Assertions.checkStateNotNull(artworkView)
            return true
        }
        return false
    }

    private fun toggleControllerVisibility() {
        if (!useController() || player == null || controller == null) {
            return
        }
        if (!controller.isFullyVisible) {
            maybeShowController(true)
        } else if (controllerHideOnTouch) {
            controller.hide()
        }
    }

    /**
     * Shows the playback controls, but only if forced or shown indefinitely.
     */
    private fun maybeShowController(isForced: Boolean) {
        if (isPlayingAd && controllerHideDuringAds || controller == null) {
            return
        }
        if (useController()) {
            val wasShowingIndefinitely = controller.isFullyVisible && controller.showTimeoutMs <= 0
            val shouldShowIndefinitely = shouldShowControllerIndefinitely()
            if (isForced || wasShowingIndefinitely || shouldShowIndefinitely) {
                showController(shouldShowIndefinitely)
            }
        }
    }

    private fun shouldShowControllerIndefinitely(): Boolean {
        player?.let { player ->
            val playbackState = player.playbackState
            return (controllerAutoShow
                    && (!player.isCommandAvailable(Player.COMMAND_GET_TIMELINE)
                    || !player.currentTimeline.isEmpty)
                    && (playbackState == Player.STATE_IDLE ||
                    playbackState == Player.STATE_ENDED ||
                    !Assertions.checkNotNull(player).playWhenReady))
        } ?: return true
    }

    private fun showController(showIndefinitely: Boolean) {
        if (!useController() || controller == null) {
            return
        }
        controller.setShowTimeoutMs(if (showIndefinitely) 0 else controllerShowTimeoutMs)
        controller.show()
    }

    private val isPlayingAd: Boolean
        get() = (player?.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM) == true
                && player?.isPlayingAd == true
                && player?.playWhenReady == true)

    private fun updateForCurrentTrackSelections(isNewPlayer: Boolean) {
        val player = player
        if (player == null || !player.isCommandAvailable(Player.COMMAND_GET_TRACKS)
            || player.currentTracks.isEmpty
        ) {
            if (!keepContentOnPlayerReset) {
                hideArtwork()
                closeShutter()
            }
            return
        }
        if (isNewPlayer && !keepContentOnPlayerReset) {
            // Hide any video from the previous player.
            closeShutter()
        }
        if (player.currentTracks.isTypeSelected(C.TRACK_TYPE_VIDEO)) {
            // Video enabled, so artwork must be hidden. If the shutter is closed, it will be opened
            // in onRenderedFirstFrame().
            hideArtwork()
            return
        }

        // Video disabled so the shutter must be closed.
        closeShutter()
        // Display artwork if enabled and available, else hide it.
        if (useArtwork()) {
            if (setArtworkFromMediaMetadata(player)) {
                return
            }
            if (setDrawableArtwork(defaultArtwork)) {
                return
            }
        }
        // Artwork disabled or unavailable.
        hideArtwork()
    }

    private fun setArtworkFromMediaMetadata(player: Player): Boolean {
        if (!player.isCommandAvailable(Player.COMMAND_GET_METADATA)) {
            return false
        }
        val artworkData = player.mediaMetadata.artworkData ?: return false
        val bitmap = BitmapFactory.decodeByteArray(
            artworkData, /* offset= */ 0, artworkData.size
        )
        return setDrawableArtwork(BitmapDrawable(resources, bitmap))
    }

    private fun setDrawableArtwork(drawable: Drawable?): Boolean {
        if (drawable != null) {
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight
            if (drawableWidth > 0 && drawableHeight > 0) {
                var artworkLayoutAspectRatio = drawableWidth.toFloat() / drawableHeight
                var scaleStyle = ScaleType.FIT_XY
                if (artworkDisplayMode == ARTWORK_DISPLAY_MODE_FILL) {
                    artworkLayoutAspectRatio = width.toFloat() / height
                    scaleStyle = ScaleType.CENTER_CROP
                }
                onContentAspectRatioChanged(contentFrame, artworkLayoutAspectRatio)
                artworkView?.apply {
                    setScaleType(scaleStyle)
                    setImageDrawable(drawable)
                    setVisibility(VISIBLE)
                }
                return true
            }
        }
        return false
    }

    private fun hideArtwork() {
        artworkView?.setImageResource(android.R.color.transparent) // Clears any bitmap reference.
        artworkView?.setVisibility(INVISIBLE)
    }

    private fun closeShutter() {
        shutterView?.visibility = VISIBLE
    }

    private fun updateBuffering() {
        val showBufferingSpinner = player?.playbackState == Player.STATE_BUFFERING &&
                (showBuffering == SHOW_BUFFERING_ALWAYS ||
                        showBuffering == SHOW_BUFFERING_WHEN_PLAYING &&
                        player?.playWhenReady == true)
        bufferingView?.visibility = if (showBufferingSpinner) VISIBLE else GONE
    }

    private fun updateErrorMessage() {
        errorMessageView?.apply {
            if (customErrorMessage != null) {
                text = customErrorMessage
                visibility = VISIBLE
                return
            }
            val error = player?.playerError
            if (error != null) {
                val errorMessage = errorMessageProvider?.getErrorMessage(error)?.second
                if (errorMessage != null) {
                    text = errorMessage
                    visibility = VISIBLE
                }
            } else {
                visibility = GONE
            }
        }
    }

    private fun updateContentDescription() {
        if (controller == null || !useController) {
            setContentDescription(null)
        } else if (controller.isFullyVisible) {
            setContentDescription(
                if (controllerHideOnTouch) resources.getString(R.string.exo_controls_hide)
                else null
            )
        } else {
            setContentDescription(resources.getString(R.string.exo_controls_show))
        }
    }

    private fun updateControllerVisibility() {
        if (isPlayingAd && controllerHideDuringAds) {
            hideController()
        } else {
            maybeShowController(false)
        }
    }

    private fun updateAspectRatio() {
        val videoSize = player?.videoSize ?: VideoSize.UNKNOWN
        val width = videoSize.width
        val height = videoSize.height
        val unappliedRotationDegrees = videoSize.unappliedRotationDegrees
        var videoAspectRatio: Float = if (height == 0 || width == 0) 0f
        else width * videoSize.pixelWidthHeightRatio / height

        if (videoSurfaceView is TextureView) {
            // Try to apply rotation transformation when our surface is a TextureView.
            if (videoAspectRatio > 0
                && (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270)
            ) {
                // We will apply a rotation 90/270 degree to the output texture of the TextureView.
                // In this case, the output video's width and height will be swapped.
                videoAspectRatio = 1 / videoAspectRatio
            }
            if (textureViewRotation != 0) {
                videoSurfaceView.removeOnLayoutChangeListener(componentListener)
            }
            textureViewRotation = unappliedRotationDegrees
            if (textureViewRotation != 0) {
                // The texture view's dimensions might be changed after layout step.
                // So add an OnLayoutChangeListener to apply rotation after layout step.
                videoSurfaceView.addOnLayoutChangeListener(componentListener)
            }
            applyTextureViewRotation(videoSurfaceView, textureViewRotation)
        }
        onContentAspectRatioChanged(
            contentFrame, if (surfaceViewIgnoresVideoAspectRatio) 0f else videoAspectRatio
        )
    }

    private fun isDpadKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                keyCode == KeyEvent.KEYCODE_DPAD_UP_RIGHT ||
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN_RIGHT ||
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN_LEFT ||
                keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                keyCode == KeyEvent.KEYCODE_DPAD_UP_LEFT ||
                keyCode == KeyEvent.KEYCODE_DPAD_CENTER
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
                || playerGestureDetector.onTouchEvent(event)
                || super.onTouchEvent(event)
    }

    // Implementing the deprecated PlayerControlView.VisibilityListener and
    // PlayerControlView.OnFullScreenModeChangedListener for now.
    @Suppress("deprecation")
    private inner class ComponentListener : Player.Listener, OnLayoutChangeListener,
        OnClickListener, PlayerControlView.VisibilityListener,
        PlayerControlView.OnFullScreenModeChangedListener {
        private val period: Timeline.Period = Timeline.Period()
        private var lastPeriodUidWithTracks: Any? = null

        // Player.Listener implementation
        override fun onCues(cueGroup: CueGroup) {
            subtitleView?.setCues(cueGroup.cues)
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            if (videoSize == VideoSize.UNKNOWN || player?.playbackState == Player.STATE_IDLE) {
                return
            }
            updateAspectRatio()
        }

        override fun onRenderedFirstFrame() {
            shutterView?.visibility = INVISIBLE
        }

        override fun onTracksChanged(tracks: Tracks) {
            // Suppress the update if transitioning to an unprepared period within the same window. This
            // is necessary to avoid closing the shutter when such a transition occurs. See:
            // https://github.com/google/ExoPlayer/issues/5507.
            val player = Assertions.checkNotNull(player)
            val timeline = if (player.isCommandAvailable(Player.COMMAND_GET_TIMELINE)) {
                player.currentTimeline
            } else {
                Timeline.EMPTY
            }
            if (timeline.isEmpty) {
                lastPeriodUidWithTracks = null
            } else if (player.isCommandAvailable(Player.COMMAND_GET_TRACKS)
                && !player.currentTracks.isEmpty
            ) {
                lastPeriodUidWithTracks =
                    timeline.getPeriod(player.currentPeriodIndex, period, /* setIds= */ true).uid
            } else if (lastPeriodUidWithTracks != null) {
                val lastPeriodIndexWithTracks = timeline.getIndexOfPeriod(
                    lastPeriodUidWithTracks!!
                )
                if (lastPeriodIndexWithTracks != C.INDEX_UNSET) {
                    val lastWindowIndexWithTracks =
                        timeline.getPeriod(lastPeriodIndexWithTracks, period).windowIndex
                    if (player.currentMediaItemIndex == lastWindowIndexWithTracks) {
                        // We're in the same media item. Suppress the update.
                        return
                    }
                }
                lastPeriodUidWithTracks = null
            }
            updateForCurrentTrackSelections(false)
        }

        override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
            updateBuffering()
            updateErrorMessage()
            updateControllerVisibility()
        }

        override fun onPlayWhenReadyChanged(
            playWhenReady: Boolean,
            reason: @PlayWhenReadyChangeReason Int,
        ) {
            updateBuffering()
            updateControllerVisibility()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: @DiscontinuityReason Int,
        ) {
            if (isPlayingAd && controllerHideDuringAds) {
                hideController()
            }
        }

        // OnLayoutChangeListener implementation
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int,
        ) {
            applyTextureViewRotation(view as TextureView, textureViewRotation)
        }

        // OnClickListener implementation
        override fun onClick(view: View) {
            toggleControllerVisibility()
        }

        // PlayerControlView.VisibilityListener implementation
        override fun onVisibilityChange(visibility: Int) {
            updateContentDescription()
            controllerVisibilityListener?.onVisibilityChanged(visibility)
        }

        // PlayerControlView.OnFullScreenModeChangedListener implementation
        override fun onFullScreenModeChanged(isFullScreen: Boolean) {
            fullscreenButtonClickListener?.onFullscreenButtonClick(isFullScreen)
        }
    }


    private val playerGestureDetector = PlayerGestureDetector(object : PlayerGestureListener {
        // 全屏手动滑动下拉状态栏的起始偏移位置
        private val statusBarOffset = 50

        // 全屏手动滑动右滑左侧的起始偏移位置
        private val horizontalOffset = 50

        // 只有在第一次显示SeekPreview前会限制deltaX和deltaY的比例和大小等
        // 在本次Touch已经显示过SeekPreview后，不会再限制
        private var startMovingVideoPos: Long = -1  // 负数表示还没开始移动
        private var startMovingBrightnessPos = -1f  // 负数表示还没开始移动
        private var startMovingVolumePos = -1f      // 负数表示还没开始移动
        private fun isSeekMoving(deltaX: Float, deltaY: Float, x: Float): Boolean {
            val absDeltaX = abs(deltaX)
            val absDeltaY = abs(deltaY)
            // 确保在屏幕水平边界不会触发
            return startMovingBrightnessPos < 0 && startMovingVolumePos < 0 &&
                    (startMovingVideoPos >= 0 || absDeltaX > absDeltaY) &&
                    x - deltaX > horizontalOffset.dp &&
                    x - deltaX < width - horizontalOffset.dp
        }

        private fun isBrightnessMoving(
            deltaX: Float,
            deltaY: Float,
            x: Float,
            y: Float,
        ): Boolean {
            val absDeltaX = abs(deltaX)
            val absDeltaY = abs(deltaY)
            // 确保在状态栏处不会触发
            return startMovingVideoPos < 0 && startMovingVolumePos < 0 &&
                    (startMovingBrightnessPos >= 0 ||
                            absDeltaX < absDeltaY && x - deltaX < width / 3.0f) &&
                    y - deltaY > statusBarOffset.dp
        }

        private fun isVolumeMoving(deltaX: Float, deltaY: Float, x: Float, y: Float): Boolean {
            val absDeltaX = abs(deltaX)
            val absDeltaY = abs(deltaY)
            // 确保在状态栏处不会触发
            return startMovingVideoPos < 0 && startMovingBrightnessPos < 0 &&
                    (startMovingVolumePos >= 0 ||
                            absDeltaX < absDeltaY && x - deltaX > width * 2 / 3.0f) &&
                    y - deltaY > statusBarOffset.dp
        }

        override fun onSingleMoved(deltaX: Float, deltaY: Float, x: Float, y: Float): Boolean {
            // 这里集中隐藏提示View，例如快进、亮度、声音等
            controller?.apply {
                setSeekPreviewVisibility(GONE)
                setBrightnessControlsVisibility(GONE)
                setVolumeControlsVisibility(GONE)
            }

            // seekTo
            // 在记录过 seek起始点 或者符合约束时执行seek
            if (isSeekMoving(deltaX, deltaY, x)) {
                player?.apply {
                    seekTo(startMovingVideoPos + (deltaX.dp * 13).toLong())
                    startMovingVideoPos = -1
                    return true
                }
            } else if (isBrightnessMoving(deltaX, deltaY, x, y)) {
                // Brightness
                startMovingBrightnessPos = -1f
                return true
            } else if (isVolumeMoving(deltaX, deltaY, x, y)) {
                // Volume
                startMovingVolumePos = -1f
                return true
            }
            return false
        }

        override fun onSingleMoving(deltaX: Float, deltaY: Float, x: Float, y: Float): Boolean {
            // seekTo
            // 在记录过 seek起始点 或者符合约束时更新SeekPreview
            if (isSeekMoving(deltaX, deltaY, x)) {
                if (startMovingVideoPos < 0) {
                    startMovingVideoPos = player?.currentPosition ?: 0
                }
                controller?.apply {
                    setSeekPreviewVisibility(VISIBLE)
                    return updateSeekPreview(
                        startMovingVideoPos + (deltaX.dp * 13).toLong()
                    )
                }
            } else if (isBrightnessMoving(deltaX, deltaY, x, y)) {
                // Brightness
                val activity = context.tryActivity ?: return false
                val layoutParams = activity.window.attributes
                if (startMovingBrightnessPos < 0) {
                    // 设置亮度默认值
                    if (layoutParams.screenBrightness <= 0.00f) {
                        val brightness = activity.getScreenBrightness()
                        if (brightness != null) {
                            layoutParams.screenBrightness = brightness / 255.0f
                            activity.window.setAttributes(layoutParams)
                        }
                    }
                    // 记录亮度起始值
                    startMovingBrightnessPos = layoutParams.screenBrightness
                }
                controller?.let { controller ->
                    // 设置屏幕亮度值，使用起始值作为基准，根据手势移动的距离计算新的亮度值
                    layoutParams.screenBrightness =
                        max(0.01f, min(1.0f, startMovingBrightnessPos - deltaY / height))
                    activity.window.setAttributes(layoutParams)
                    controller.setBrightnessControlsVisibility(VISIBLE)
                    controller.updateBrightnessProgress((layoutParams.screenBrightness * 100).toInt())
                }
            } else if (isVolumeMoving(deltaX, deltaY, x, y)) {
                // Volume
                val audioManager =
                    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (startMovingVolumePos < 0) {
                    startMovingVolumePos =
                        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                }
                controller?.let { controller ->
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    controller.setMaxVolume(maxVolume)
                    var minVolume = 0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        minVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
                    }
                    val desiredVolume =
                        (startMovingVolumePos - deltaY / height * 1.2f * (maxVolume - minVolume)).toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, desiredVolume, 0)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        controller.setMinVolume(audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC))
                    }
                    controller.setVolumeControlsVisibility(VISIBLE)
                    controller.updateVolumeProgress(desiredVolume)
                }
            }
            return false
        }

        override fun onZoomUpdate(detector: PlayerGestureDetector): Boolean {
            contentFrame?.apply {
                rotation = detector.rotation
                scaleX = detector.scale
                scaleY = detector.scale
                translationX = detector.translationX
                translationY = detector.translationY
            }
            return true
        }

        override fun isZoomChanged(isZoom: Boolean) {
            controller?.onZoomStateChanged(isZoom)
        }

        private var isLongPress = false
        private var beforeLongPressSpeed = 1.0f
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        override fun onLongPress() {
            player?.let { player ->
                vibrator.tickVibrate(35)
                isLongPress = true
                beforeLongPressSpeed = player.playbackParameters.speed
                player.setPlaybackSpeed(3.0f)
                controller?.setLongPressPlaybackSpeedVisibility(VISIBLE)
            }
        }

        override fun onLongPressUp(): Boolean {
            var handled = false
            if (isLongPress) {
                player?.let { player ->
                    isLongPress = false
                    player.setPlaybackSpeed(beforeLongPressSpeed)
                    controller?.setLongPressPlaybackSpeedVisibility(GONE)
                    handled = true
                }
            }
            return handled
        }
    }
    )
    private val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        private fun onDoubleTapPausePlay(): Boolean = controller?.playOrPause() == Unit

        private fun onDoubleTapBackwardForward(e: MotionEvent): Boolean {
            player?.let { player ->
                if (e.x < width / 2f) {
                    controller?.showBackwardRipple()
                    player.seekTo(player.currentPosition - 10000) // -10s.
                } else {
                    controller?.showForwardRipple()
                    player.seekTo(player.currentPosition + 10000) // +10s.
                }
                return true
            }
            return false
        }

        private fun onDoubleTapBackwardPausePlayForward(e: MotionEvent): Boolean {
            val player = this@PlayerView.player
            val controller = this@PlayerView.controller
            if (player == null) {
                return onDoubleTapPausePlay()
            }
            if (controller == null) {
                return onDoubleTapBackwardForward(e)
            }
            if (e.x <= width * 0.25f) {
                controller.showBackwardRipple()
                player.seekTo(player.currentPosition - 10000) // -10s.
            } else if (e.x >= width * 0.75f) {
                controller.showForwardRipple()
                player.seekTo(player.currentPosition + 10000) // +10s.
            } else {
                controller.playOrPause()
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            val doubleTapPreference = context.dataStore.getOrDefault(PlayerDoubleTapPreference)
            return when (doubleTapPreference) {
                PlayerDoubleTapPreference.BACKWARD_FORWARD -> onDoubleTapBackwardForward(e)
                PlayerDoubleTapPreference.BACKWARD_PAUSE_PLAY_FORWARD -> {
                    onDoubleTapBackwardPausePlayForward(e)
                }

                else -> onDoubleTapPausePlay()
            }
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            toggleControllerVisibility()
            return true
        }
    })

    @RequiresApi(34)
    private object Api34 {
        @DoNotInline
        fun setSurfaceLifecycleToFollowsAttachment(surfaceView: SurfaceView) {
            surfaceView.setSurfaceLifecycle(SurfaceView.SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT)
        }
    }

    companion object {
        /**
         * No artwork is shown.
         */
        const val ARTWORK_DISPLAY_MODE_OFF = 0

        /**
         * The artwork is fit into the player view and centered creating a letterbox style.
         */
        const val ARTWORK_DISPLAY_MODE_FIT = 1

        /**
         * The artwork covers the entire space of the player view.
         * If the aspect ratio of the image is different than the player view some areas of
         * the image are cropped.
         */
        const val ARTWORK_DISPLAY_MODE_FILL = 2

        /**
         * The buffering view is never shown.
         */
        const val SHOW_BUFFERING_NEVER = 0

        /**
         * The buffering view is shown when the player is in the [Player.STATE_BUFFERING] buffering
         * state and [Player.getPlayWhenReady] playWhenReady is `true`.
         */
        const val SHOW_BUFFERING_WHEN_PLAYING = 1

        /**
         * The buffering view is always shown when the player is in the
         * [Player.STATE_BUFFERING] buffering state.
         */
        const val SHOW_BUFFERING_ALWAYS = 2

        private const val SURFACE_TYPE_NONE = 0
        private const val SURFACE_TYPE_SURFACE_VIEW = 1
        private const val SURFACE_TYPE_TEXTURE_VIEW = 2
        private const val SURFACE_TYPE_SPHERICAL_GL_SURFACE_VIEW = 3
        private const val SURFACE_TYPE_VIDEO_DECODER_GL_SURFACE_VIEW = 4

        /**
         * Switches the view targeted by a given [Player].
         *
         * @param player        The player whose target view is being switched.
         * @param oldPlayerView The old view to detach from the player.
         * @param newPlayerView The new view to attach to the player.
         */
        fun switchTargetView(
            player: Player?,
            oldPlayerView: PlayerView?,
            newPlayerView: PlayerView?,
        ) {
            if (oldPlayerView == newPlayerView) {
                return
            }
            // We attach the new view before detaching the old one because this ordering allows the player
            // to swap directly from one surface to another, without transitioning through a state where no
            // surface is attached. This is significantly more efficient and achieves a more seamless
            // transition when using platform provided video decoders.
            newPlayerView?.player = player
            oldPlayerView?.player = null
        }

        private fun configureEditModeLogo(context: Context, resources: Resources, logo: ImageView) {
            logo.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.exo_edit_mode_logo, context.theme)
            )
            logo.setBackgroundColor(
                resources.getColor(R.color.exo_edit_mode_background_color, null)
            )
        }

        private fun setResizeModeRaw(aspectRatioFrame: AspectRatioFrameLayout, resizeMode: Int) {
            aspectRatioFrame.resizeMode = resizeMode
        }

        /**
         * Applies a texture rotation to a [TextureView].
         */
        private fun applyTextureViewRotation(textureView: TextureView, textureViewRotation: Int) {
            val transformMatrix = Matrix()
            val textureViewWidth = textureView.width.toFloat()
            val textureViewHeight = textureView.height.toFloat()
            if (textureViewWidth != 0f && textureViewHeight != 0f && textureViewRotation != 0) {
                val pivotX = textureViewWidth / 2
                val pivotY = textureViewHeight / 2
                transformMatrix.postRotate(textureViewRotation.toFloat(), pivotX, pivotY)

                // After rotation, scale the rotated texture to fit the TextureView size.
                val originalTextureRect = RectF(0f, 0f, textureViewWidth, textureViewHeight)
                val rotatedTextureRect = RectF()
                transformMatrix.mapRect(rotatedTextureRect, originalTextureRect)
                transformMatrix.postScale(
                    textureViewWidth / rotatedTextureRect.width(),
                    textureViewHeight / rotatedTextureRect.height(),
                    pivotX,
                    pivotY,
                )
            }
            textureView.setTransform(transformMatrix)
        }
    }
}
