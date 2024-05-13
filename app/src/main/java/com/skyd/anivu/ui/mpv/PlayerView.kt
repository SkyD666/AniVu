package com.skyd.anivu.ui.mpv

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.materialkolor.ktx.toColor
import com.materialkolor.ktx.toHct
import com.skyd.anivu.R
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.alwaysLight
import com.skyd.anivu.ext.detectDoubleFingerTransformGestures
import com.skyd.anivu.ext.getScreenBrightness
import com.skyd.anivu.ext.snapshotStateOffsetSaver
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.preference.player.PlayerDoubleTapPreference
import com.skyd.anivu.ui.local.LocalPlayerDoubleTap
import `is`.xyz.mpv.MPVLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow

sealed interface PlayerCommand {
    data class SetUri(val uri: Uri) : PlayerCommand
    data object Destroy : PlayerCommand
    data class Paused(val paused: Boolean) : PlayerCommand
    data object GetPaused : PlayerCommand
    data object PlayOrPause : PlayerCommand
    data class SeekTo(val position: Int) : PlayerCommand
    data class Rotate(val rotate: Int) : PlayerCommand
    data class Zoom(val zoom: Float) : PlayerCommand
    data object GetZoom : PlayerCommand
    data class VideoOffset(val offset: Offset) : PlayerCommand
    data object GetVideoOffsetX : PlayerCommand
    data object GetVideoOffsetY : PlayerCommand
    data class SetSpeed(val speed: Float) : PlayerCommand
    data object GetSpeed : PlayerCommand
}

private fun MPVView.solveCommand(
    command: PlayerCommand,
    uri: () -> Uri,
    isPlayingChanged: (Boolean) -> Unit,
    onVideoZoom: (Float) -> Unit,
    videoOffset: () -> Offset,
    onVideoOffset: (Offset) -> Unit,
    onSpeedChanged: (Float) -> Unit,
) {
    when (command) {
        is PlayerCommand.SetUri -> uri().resolveUri(context)
            ?.let { loadFile(it) }

        PlayerCommand.Destroy -> destroy()
        is PlayerCommand.Paused -> {
            if (!command.paused) {
                if (keepOpen && eofReached) {
                    seek(0)
                } else if (isIdling) {
                    uri().resolveUri(context)?.let { loadFile(it) }
                }
            }
            paused = command.paused
        }

        PlayerCommand.GetPaused -> isPlayingChanged(!paused)
        PlayerCommand.PlayOrPause -> cyclePause()
        is PlayerCommand.SeekTo -> seek(command.position.coerceIn(0..(duration ?: 0)))
        is PlayerCommand.Rotate -> rotate(command.rotate)
        is PlayerCommand.Zoom -> zoom(command.zoom)
        PlayerCommand.GetZoom -> onVideoZoom(2.0.pow(videoZoom).toFloat())
        is PlayerCommand.VideoOffset -> offset(
            command.offset.x.toInt(), command.offset.y.toInt()
        )

        PlayerCommand.GetVideoOffsetX -> videoDW?.let { dw ->
            onVideoOffset(videoOffset().copy(x = (videoPanX * dw).toFloat()))
        }

        PlayerCommand.GetVideoOffsetY -> videoDH?.let { dh ->
            onVideoOffset(videoOffset().copy(y = (videoPanY * dh).toFloat()))
        }

        is PlayerCommand.SetSpeed -> playbackSpeed =
            command.speed.toDouble()

        PlayerCommand.GetSpeed -> onSpeedChanged(playbackSpeed.toFloat())
    }
}

@Composable
fun PlayerView(
    uri: Uri,
    onBack: () -> Unit,
    configDir: String = Const.MPV_CONFIG_DIR.path,
    cacheDir: String = Const.MPV_CACHE_DIR.path,
) {
    val commandQueue = remember { Channel<PlayerCommand>(capacity = UNLIMITED) }
    val scope = rememberCoroutineScope()

    var mediaLoaded by rememberSaveable { mutableStateOf(false) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableIntStateOf(0) }
    var currentPosition by rememberSaveable { mutableIntStateOf(0) }
    var isSeeking by rememberSaveable { mutableStateOf(false) }
    var speed by rememberSaveable { mutableFloatStateOf(1f) }
    var videoRotate by rememberSaveable { mutableIntStateOf(0) }
    var videoZoom by rememberSaveable { mutableFloatStateOf(1f) }
    var videoOffset by rememberSaveable(saver = snapshotStateOffsetSaver()) { mutableStateOf(Offset.Zero) }

    val mpvObserver = remember {
        object : MPVLib.EventObserver {
            override fun eventProperty(property: String) {
                when (property) {
                    "video-zoom" -> commandQueue.trySend(PlayerCommand.GetZoom)
                    "video-pan-x" -> commandQueue.trySend(PlayerCommand.GetVideoOffsetX)
                    "video-pan-y" -> commandQueue.trySend(PlayerCommand.GetVideoOffsetY)
                    "speed" -> commandQueue.trySend(PlayerCommand.GetSpeed)
                }
            }

            override fun eventProperty(property: String, value: Long) {
                when (property) {
                    "time-pos" -> currentPosition = value.toInt()
                    "duration" -> duration = value.toInt()
                    "video-rotate" -> videoRotate = value.toInt()
                }
            }

            override fun eventProperty(property: String, value: Boolean) {
                when (property) {
                    "pause" -> isPlaying = !value
                }
            }

            override fun eventProperty(property: String, value: String) {
                when (property) {
                    "media-title" -> title = value
                }
            }

            override fun event(eventId: Int) {
                when (eventId) {
                    MPVLib.mpvEventId.MPV_EVENT_SEEK -> isSeeking = false
                    MPVLib.mpvEventId.MPV_EVENT_END_FILE -> {
                        mediaLoaded = false
                        isPlaying = false
                    }

                    MPVLib.mpvEventId.MPV_EVENT_FILE_LOADED,
                    MPVLib.mpvEventId.MPV_EVENT_PLAYBACK_RESTART -> {
                        mediaLoaded = true
                        commandQueue.trySend(PlayerCommand.GetPaused)
                    }
                }
            }

            override fun efEvent(err: String?) {
            }
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MPVView(context, null).apply {
                initialize(configDir, cacheDir)
                addObserver(mpvObserver)
                scope.launch(Dispatchers.Main.immediate) {
                    commandQueue
                        .consumeAsFlow()
                        .startWith(PlayerCommand.SetUri(uri))
                        .onEach { command ->
                            solveCommand(
                                command = command,
                                uri = { uri },
                                isPlayingChanged = { isPlaying = it },
                                onVideoZoom = { videoZoom = it },
                                videoOffset = { videoOffset },
                                onVideoOffset = { videoOffset = it },
                                onSpeedChanged = { speed = it },
                            )
                        }
                        .collect()
                }

            }
        },
    )

    PlayerController(
        enabled = { mediaLoaded },
        isPlaying = { isPlaying },
        title = { title },
        onBack = onBack,
        onPlayStateChanged = { commandQueue.trySend(PlayerCommand.Paused(isPlaying)) },
        currentPosition = { currentPosition },
        duration = { duration },
        isSeeking = { isSeeking },
        onSeekTo = {
            isSeeking = true
            commandQueue.trySend(PlayerCommand.SeekTo(it))
        },
        onPlayOrPause = { commandQueue.trySend(PlayerCommand.PlayOrPause) },
        speed = { speed },
        onSpeedChanged = { commandQueue.trySend(PlayerCommand.SetSpeed(it)) },
        videoRotate = { videoRotate.toFloat() },
        videoZoom = { videoZoom },
        onVideoRotate = {
            commandQueue.trySend(PlayerCommand.Rotate(it.toInt()))
        },
        onVideoZoom = {
            commandQueue.trySend(PlayerCommand.Zoom(it))
        },
        videoOffset = { videoOffset },
        onVideoOffset = { commandQueue.trySend(PlayerCommand.VideoOffset(it)) }
    )

    var needPlayWhenResume by rememberSaveable { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (needPlayWhenResume) {
                        commandQueue.trySend(PlayerCommand.Paused(false))
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    needPlayWhenResume = isPlaying
                    if (isPlaying) {
                        commandQueue.trySend(PlayerCommand.Paused(true))
                    }
                }

                Lifecycle.Event.ON_DESTROY -> {
                    commandQueue.trySend(PlayerCommand.Destroy)
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

private val ControllerBarGray = Color(0xAD000000)
private val ControllerLabelGray = Color(0x70000000)

@Composable
private fun PlayerController(
    enabled: () -> Boolean,
    isPlaying: () -> Boolean,
    title: () -> String,
    onBack: () -> Unit,
    onPlayStateChanged: () -> Unit,
    isSeeking: () -> Boolean,
    currentPosition: () -> Int,
    duration: () -> Int,
    onSeekTo: (Int) -> Unit,
    onPlayOrPause: () -> Unit,
    speed: () -> Float,
    onSpeedChanged: (Float) -> Unit,
    videoRotate: () -> Float,
    onVideoRotate: (Float) -> Unit,
    videoZoom: () -> Float,
    onVideoZoom: (Float) -> Unit,
    videoOffset: () -> Offset,
    onVideoOffset: (Offset) -> Unit,
) {
    var showController by rememberSaveable { mutableStateOf(true) }
    var controllerWidth by remember { mutableStateOf(0) }
    var controllerHeight by remember { mutableStateOf(0) }

    val view = LocalView.current
    val autoHideControllerRunnable = remember { Runnable { showController = false } }
    val cancelAutoHideControllerRunnable = { view.removeCallbacks(autoHideControllerRunnable) }
    val restartAutoHideControllerRunnable = {
        cancelAutoHideControllerRunnable()
        if (showController) {
            view.postDelayed(autoHideControllerRunnable, 5000)
        }
    }
    LaunchedEffect(showController) { restartAutoHideControllerRunnable() }

    var showSeekTimePreview by remember { mutableStateOf(false) }
    var seekTimePreview by remember { mutableIntStateOf(0) }

    var showBrightnessPreview by remember { mutableStateOf(false) }
    var brightnessValue by remember { mutableFloatStateOf(0f) }
    var brightnessRange by remember { mutableStateOf(0f..0f) }

    var showVolumePreview by remember { mutableStateOf(false) }
    var volumeValue by remember { mutableIntStateOf(0) }
    var volumeRange by remember { mutableStateOf(0..0) }

    var isLongPressing by remember { mutableStateOf(false) }
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    controllerWidth = it.size.width
                    controllerHeight = it.size.height
                }
                .detectPressGestures(
                    controllerWidth = { controllerWidth },
                    currentPosition = currentPosition,
                    onSeekTo = onSeekTo,
                    onPlayOrPause = onPlayOrPause,
                    speed = speed,
                    onSpeedChanged = onSpeedChanged,
                    showController = { showController },
                    onShowControllerChanged = { showController = it },
                    isLongPressing = { isLongPressing },
                    isLongPressingChanged = { isLongPressing = it },
                    cancelAutoHideControllerRunnable = cancelAutoHideControllerRunnable,
                    restartAutoHideControllerRunnable = restartAutoHideControllerRunnable,
                )
                .run {
                    if (isLongPressing) this
                    else detectControllerGestures(
                        enabled = enabled,
                        controllerWidth = { controllerWidth },
                        controllerHeight = { controllerHeight },
                        onShowBrightness = { showBrightnessPreview = it },
                        onBrightnessRangeChanged = { brightnessRange = it },
                        onBrightnessChanged = { brightnessValue = it },
                        onShowVolume = { showVolumePreview = it },
                        onVolumeRangeChanged = { volumeRange = it },
                        onVolumeChanged = { volumeValue = it },
                        currentPosition = currentPosition,
                        onShowSeekTimePreview = { showSeekTimePreview = it },
                        onSeekTo = onSeekTo,
                        onTimePreviewChanged = { seekTimePreview = it },
                        videoRotate = videoRotate,
                        onVideoRotate = onVideoRotate,
                        videoZoom = videoZoom,
                        onVideoZoom = onVideoZoom,
                        videoOffset = videoOffset,
                        onVideoOffset = onVideoOffset,
                        cancelAutoHideControllerRunnable = cancelAutoHideControllerRunnable,
                        restartAutoHideControllerRunnable = restartAutoHideControllerRunnable,
                    )
                }
        ) {
            // Auto hide box
            Box {
                AnimatedVisibility(
                    visible = showController,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (topBar, bottomBar, resetTransform) = createRefs()

                        TopBar(
                            modifier = Modifier.constrainAs(topBar) { top.linkTo(parent.top) },
                            title = title(),
                            onBack = onBack,
                        )
                        BottomBar(
                            modifier = Modifier.constrainAs(bottomBar) { bottom.linkTo(parent.bottom) },
                            isPlaying = isPlaying,
                            onPlayStateChanged = onPlayStateChanged,
                            isSeeking = isSeeking,
                            currentPosition = currentPosition,
                            duration = duration,
                            onPositionChanged = onSeekTo,
                            onRestartAutoHideControllerRunnable = restartAutoHideControllerRunnable
                        )

                        // Reset transform
                        if (videoZoom() != 1f || videoRotate() != 0f) {
                            ResetTransform(
                                modifier = Modifier.constrainAs(resetTransform) {
                                    bottom.linkTo(bottomBar.top)
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                    verticalBias = 1f
                                },
                                enabled = enabled,
                                onClick = {
                                    onVideoOffset(Offset.Zero)
                                    onVideoZoom(1f)
                                    onVideoRotate(0f)
                                }
                            )
                        }
                    }
                }
            }

            // Seek time preview
            if (showSeekTimePreview) {
                SeekTimePreview(value = { seekTimePreview }, duration = duration)
            }
            // Brightness preview
            if (showBrightnessPreview) {
                BrightnessPreview(value = { brightnessValue }, range = { brightnessRange })
            }
            // Volume preview
            if (showVolumePreview) {
                VolumePreview(value = { volumeValue }, range = { volumeRange })
            }
            // Long press speed preview
            if (isLongPressing) {
                LongPressSpeedPreview(speed = speed)
            }
        }
    }
}

@Composable
private fun BoxScope.SeekTimePreview(
    value: () -> Int,
    duration: () -> Int,
) {
    Row(
        modifier = Modifier
            .align(Alignment.Center)
            .clip(RoundedCornerShape(6.dp))
            .background(color = ControllerLabelGray)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = value()
                .coerceIn(0..duration())
                .toDurationString(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
        Text(
            modifier = Modifier.padding(horizontal = 3.dp),
            text = "/",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
        Text(
            text = duration().toDurationString(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}

@Composable
private fun BoxScope.BrightnessPreview(
    value: () -> Float,
    range: () -> ClosedFloatingPointRange<Float>,
) {
    Row(
        modifier = Modifier
            .align(Alignment.Center)
            .clip(RoundedCornerShape(6.dp))
            .background(color = ControllerLabelGray)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .width(120.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val start = range().start
        val endInclusive = range().endInclusive
        val length = endInclusive - start
        val icon = when (value()) {
            in start..start + length / 3 -> Icons.Rounded.BrightnessLow
            in start + length * 2 / 3..endInclusive -> Icons.Rounded.BrightnessHigh
            else -> Icons.Rounded.BrightnessMedium
        }
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(10.dp))
        LinearProgressIndicator(
            progress = { (value() - start) / length },
            modifier = Modifier.weight(1f),
            drawStopIndicator = null,
        )
    }
}

@Composable
private fun BoxScope.VolumePreview(
    value: () -> Int,
    range: () -> IntRange,
) {
    Row(
        modifier = Modifier
            .align(Alignment.Center)
            .clip(RoundedCornerShape(6.dp))
            .background(color = ControllerLabelGray)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .width(120.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val start = range().first
        val endInclusive = range().last
        val length = endInclusive - start
        val v = value()
        val icon = when {
            v <= start -> Icons.AutoMirrored.Rounded.VolumeMute
            v in start..start + length / 2 -> Icons.AutoMirrored.Rounded.VolumeDown
            else -> Icons.AutoMirrored.Rounded.VolumeUp
        }
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(10.dp))
        LinearProgressIndicator(
            progress = { (value() - start).toFloat() / length },
            modifier = Modifier.weight(1f),
            drawStopIndicator = null,
        )
    }
}

@Composable
private fun BoxScope.LongPressSpeedPreview(speed: () -> Float) {
    Row(
        modifier = Modifier
            .align(BiasAlignment(0f, -0.6f))
            .clip(RoundedCornerShape(6.dp))
            .background(color = ControllerLabelGray)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = Icons.Rounded.FastForward, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${speed()}x",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}

@Composable
private fun ResetTransform(
    modifier: Modifier = Modifier,
    enabled: () -> Boolean,
    onClick: () -> Unit,
) {
    Text(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color = ControllerLabelGray)
            .clickable(enabled = enabled(), onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        text = stringResource(id = R.string.player_reset_zoom),
        style = MaterialTheme.typography.labelLarge,
        color = Color.White,
    )
}

private val inStatusBarArea: PointerInputScope.(y: Float) -> Boolean = { y ->
    y / density <= 60
}

@Composable
private fun Modifier.detectPressGestures(
    controllerWidth: () -> Int,
    currentPosition: () -> Int,
    onSeekTo: (Int) -> Unit,
    onPlayOrPause: () -> Unit,
    speed: () -> Float,
    onSpeedChanged: (Float) -> Unit,
    showController: () -> Boolean,
    onShowControllerChanged: (Boolean) -> Unit,
    isLongPressing: () -> Boolean,
    isLongPressingChanged: (Boolean) -> Unit,
    cancelAutoHideControllerRunnable: () -> Boolean,
    restartAutoHideControllerRunnable: () -> Unit,
): Modifier {
    var beforeLongPressingSpeed by remember { mutableStateOf(speed()) }

    val playerDoubleTap = LocalPlayerDoubleTap.current
    val onDoubleTapPausePlay: () -> Unit = remember { { onPlayOrPause() } }

    val onDoubleTapBackwardForward: (Offset) -> Unit = { offset ->
        if (offset.x < controllerWidth() / 2f) {
            onSeekTo(currentPosition() - 10) // -10s.
        } else {
            onSeekTo(currentPosition() + 10) // +10s.
        }
    }
    val onDoubleTapBackwardPausePlayForward: (Offset) -> Unit = { offset ->
        if (offset.x <= controllerWidth() * 0.25f) {
            onSeekTo(currentPosition() - 10) // -10s.
        } else if (offset.x >= controllerWidth() * 0.75f) {
            onSeekTo(currentPosition() + 10) // +10s.
        } else {
            onDoubleTapPausePlay()
        }
    }

    val onDoubleTap: (Offset) -> Unit = { offset ->
        when (playerDoubleTap) {
            PlayerDoubleTapPreference.BACKWARD_FORWARD -> onDoubleTapBackwardForward(offset)
            PlayerDoubleTapPreference.BACKWARD_PAUSE_PLAY_FORWARD ->
                onDoubleTapBackwardPausePlayForward(offset)

            else -> onDoubleTapPausePlay()
        }
    }

    return pointerInput(playerDoubleTap, speed(), isLongPressing()) {
        detectTapGestures(
            onLongPress = {
                beforeLongPressingSpeed = speed()
                isLongPressingChanged(true)
                onSpeedChanged(3f)
            },
            onDoubleTap = {
                restartAutoHideControllerRunnable()
                onDoubleTap(it)
            },
            onPress = {
                tryAwaitRelease()
                isLongPressingChanged(false)
                onSpeedChanged(beforeLongPressingSpeed)
            },
            onTap = {
                cancelAutoHideControllerRunnable()
                onShowControllerChanged(!showController())
            }
        )
    }
}

@Composable
private fun Modifier.detectControllerGestures(
    enabled: () -> Boolean,
    controllerWidth: () -> Int,
    controllerHeight: () -> Int,
    onShowBrightness: (Boolean) -> Unit,
    onBrightnessRangeChanged: (ClosedFloatingPointRange<Float>) -> Unit,
    onBrightnessChanged: (Float) -> Unit,
    onShowVolume: (Boolean) -> Unit,
    onVolumeRangeChanged: (IntRange) -> Unit,
    onVolumeChanged: (Int) -> Unit,
    currentPosition: () -> Int,
    onShowSeekTimePreview: (Boolean) -> Unit,
    onTimePreviewChanged: (Int) -> Unit,
    onSeekTo: (Int) -> Unit,
    videoRotate: () -> Float,
    onVideoRotate: (Float) -> Unit,
    videoZoom: () -> Float,
    onVideoZoom: (Float) -> Unit,
    videoOffset: () -> Offset,
    onVideoOffset: (Offset) -> Unit,
    cancelAutoHideControllerRunnable: () -> Boolean,
    restartAutoHideControllerRunnable: () -> Unit,
): Modifier {
    if (!enabled()) {
        onShowBrightness(false)
        onShowVolume(false)
        onShowSeekTimePreview(false)
        restartAutoHideControllerRunnable()
        return this
    }

    var pointerStartX by rememberSaveable { mutableFloatStateOf(0f) }
    var pointerStartY by rememberSaveable { mutableFloatStateOf(0f) }

    val context = LocalContext.current
    val activity = remember(context) { context.activity }
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val minVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)

    var startBrightness by rememberSaveable { mutableFloatStateOf(0f) }
    var startVolume by rememberSaveable { mutableIntStateOf(0) }

    var seekTimePreviewStartPosition by remember { mutableIntStateOf(0) }
    var seekTimePreviewPositionDelta by remember { mutableIntStateOf(0) }

    return pointerInput(Unit) {
        detectDoubleFingerTransformGestures(
            onVerticalDragStart = onVerticalDragStart@{
                cancelAutoHideControllerRunnable()
                pointerStartX = it.x
                pointerStartY = it.y
                if (inStatusBarArea(it.y)) return@onVerticalDragStart
                when (pointerStartX) {
                    in 0f..controllerWidth() / 3f -> {
                        onBrightnessRangeChanged(0.01f..1f)
                        startBrightness = activity.window.attributes.apply {
                            if (screenBrightness <= 0.00f) {
                                val brightness = activity.getScreenBrightness()
                                if (brightness != null) {
                                    screenBrightness = brightness / 255.0f
                                    activity.window.setAttributes(this)
                                }
                            }
                        }.screenBrightness
                        onBrightnessChanged(startBrightness)
                        onShowBrightness(true)
                    }

                    in controllerWidth() * 2 / 3f..controllerWidth().toFloat() -> {
                        onVolumeRangeChanged(minVolume..maxVolume)
                        startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        onVolumeChanged(startVolume)
                        onShowVolume(true)
                    }
                }
            },
            onVerticalDragEnd = {
                restartAutoHideControllerRunnable()
                onShowBrightness(false)
                onShowVolume(false)
            },
            onVerticalDragCancel = {
                restartAutoHideControllerRunnable()
                onShowBrightness(false)
                onShowVolume(false)
            },
            onVerticalDrag = onVerticalDrag@{ change, _ ->
                val deltaY = change.position.y - pointerStartY
                if (inStatusBarArea(pointerStartY) || abs(deltaY) < 50) return@onVerticalDrag

                when (pointerStartX) {
                    in 0f..controllerWidth() / 3f -> {
                        val layoutParams = activity.window.attributes
                        layoutParams.screenBrightness =
                            (startBrightness - deltaY / controllerHeight())
                                .coerceIn(0.01f..1f)
                        activity.window.setAttributes(layoutParams)
                        onBrightnessChanged(layoutParams.screenBrightness)
                    }

                    in controllerWidth() * 2 / 3f..controllerWidth().toFloat() -> {
                        val desiredVolume =
                            (startVolume - deltaY / controllerHeight() * 1.2f * (maxVolume - minVolume)).toInt()
                        onVolumeChanged(desiredVolume)
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, desiredVolume, 0)
                    }
                }
            },
            onHorizontalDragStart = onHorizontalDragStart@{
                cancelAutoHideControllerRunnable()
                pointerStartX = it.x
                pointerStartY = it.y
                if (inStatusBarArea(it.y)) return@onHorizontalDragStart
                seekTimePreviewStartPosition = currentPosition()
                seekTimePreviewPositionDelta = 0
                onShowSeekTimePreview(true)
            },
            onHorizontalDragEnd = onHorizontalDragEnd@{
                onShowSeekTimePreview(false)
                restartAutoHideControllerRunnable()
                if (inStatusBarArea(pointerStartY)) return@onHorizontalDragEnd
                onSeekTo(seekTimePreviewStartPosition + seekTimePreviewPositionDelta)
            },
            onHorizontalDragCancel = {
                onShowSeekTimePreview(false)
                restartAutoHideControllerRunnable()
            },
            onHorizontalDrag = onHorizontalDrag@{ change, _ ->
                if (inStatusBarArea(pointerStartY)) return@onHorizontalDrag
                seekTimePreviewPositionDelta =
                    ((change.position.x - pointerStartX) / density / 8).toInt()
                onTimePreviewChanged(seekTimePreviewStartPosition + seekTimePreviewPositionDelta)
            },
            onGesture = onGesture@{ _: Offset, pan: Offset, zoom: Float, rotation: Float ->
                onVideoOffset(videoOffset() + pan)
                onVideoRotate(videoRotate() + rotation)
                onVideoZoom(videoZoom() * zoom)
            }
        )
    }
}

@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ControllerBarGray, Color.Transparent)
                )
            )
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                )
            )
            .padding(bottom = 30.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .size(56.dp)
                .clickable(onClick = onBack)
                .padding(15.dp),
            imageVector = Icons.Outlined.ArrowBackIosNew,
            contentDescription = stringResource(id = R.string.back),
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            modifier = Modifier
                .weight(1f)
                .basicMarquee(),
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(3.dp))
    }
}

@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    isPlaying: () -> Boolean,
    onPlayStateChanged: () -> Unit,
    isSeeking: () -> Boolean,
    currentPosition: () -> Int,
    duration: () -> Int,
    onPositionChanged: (position: Int) -> Unit,
    onRestartAutoHideControllerRunnable: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, ControllerBarGray)
                )
            )
            .windowInsetsPadding(
                WindowInsets.displayCutout.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .padding(top = 30.dp)
            .padding(horizontal = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val sliderInteractionSource = remember { MutableInteractionSource() }
            var sliderValue by rememberSaveable { mutableStateOf(currentPosition().toFloat()) }
            var valueIsChanging by rememberSaveable { mutableStateOf(false) }
            if (!valueIsChanging && !isSeeking() && sliderValue != currentPosition().toFloat()) {
                sliderValue = currentPosition().toFloat()
            }
            Text(
                text = currentPosition().toDurationString(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
            Slider(
                modifier = Modifier
                    .padding(6.dp)
                    .height(10.dp)
                    .weight(1f),
                value = sliderValue,
                onValueChange = {
                    valueIsChanging = true
                    onRestartAutoHideControllerRunnable()
                    sliderValue = it
                },
                onValueChangeFinished = {
                    onPositionChanged(sliderValue.toInt())
                    valueIsChanging = false
                },
                valueRange = 0f..duration().toFloat(),
                interactionSource = sliderInteractionSource,
                thumb = {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Spacer(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(CircleShape)
                                .size(14.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary
                                        .alwaysLight(true)
                                        .toHct()
                                        .withTone(90.0)
                                        .toColor()
                                )
                        )
                    }
                },
                track = {
                    Spacer(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(SliderDefaults.colors().activeTrackColor)
                    )
                },
            )
            Text(
                text = duration().toDurationString(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(52.dp)
                    .clickable(onClick = onPlayStateChanged)
                    .padding(9.dp),
                imageVector = if (isPlaying()) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(if (isPlaying()) R.string.pause else R.string.play),
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

fun Int.toDurationString(sign: Boolean = false): String {
    if (sign) return (if (this >= 0) "+" else "-") + abs(this).toDurationString()

    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60
    return if (hours == 0) "%02d:%02d".format(minutes, seconds)
    else "%d:%02d:%02d".format(hours, minutes, seconds)
}