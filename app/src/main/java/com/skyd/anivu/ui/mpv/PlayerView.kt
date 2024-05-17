package com.skyd.anivu.ui.mpv

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
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
import com.skyd.anivu.ext.alwaysLight
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.ext.toPercentage
import com.skyd.anivu.ui.local.LocalPlayerShow85sButton
import com.skyd.anivu.ui.mpv.state.PlayState
import com.skyd.anivu.ui.mpv.state.SubtitleTrackDialogState
import com.skyd.anivu.ui.mpv.state.TransformState
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
    data class SetSubtitleTrack(val trackId: Int) : PlayerCommand
    data object GetSpeed : PlayerCommand
    data object LoadAllTracks : PlayerCommand
    data object GetSubtitleTrack : PlayerCommand
}

private fun MPVView.solveCommand(
    command: PlayerCommand,
    uri: () -> Uri,
    isPlayingChanged: (Boolean) -> Unit,
    onSubtitleTrack: (subtitleTrack: List<MPVView.Track>) -> Unit,
    onSubtitleTrackChanged: (Int) -> Unit,
    transformState: () -> TransformState,
    onVideoZoom: (Float) -> Unit,
    onVideoOffset: (Offset) -> Unit,
    onSpeedChanged: (Float) -> Unit,
) {
    when (command) {
        is PlayerCommand.SetUri -> uri().resolveUri(context)?.let { loadFile(it) }
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
        is PlayerCommand.VideoOffset -> offset(command.offset.x.toInt(), command.offset.y.toInt())
        PlayerCommand.GetVideoOffsetX -> videoDW?.let { dw ->
            onVideoOffset(transformState().videoOffset.copy(x = (videoPanX * dw).toFloat()))
        }

        PlayerCommand.GetVideoOffsetY -> videoDH?.let { dh ->
            onVideoOffset(transformState().videoOffset.copy(y = (videoPanY * dh).toFloat()))
        }

        is PlayerCommand.SetSpeed -> playbackSpeed = command.speed.toDouble()
        PlayerCommand.GetSpeed -> onSpeedChanged(playbackSpeed.toFloat())
        PlayerCommand.LoadAllTracks -> loadTracks()
        PlayerCommand.GetSubtitleTrack -> onSubtitleTrack(subtitleTrack)
        is PlayerCommand.SetSubtitleTrack -> {
            sid = command.trackId
            onSubtitleTrackChanged(command.trackId)
        }
    }
}

@Composable
fun PlayerView(
    uri: Uri,
    onBack: () -> Unit,
    configDir: String = Const.MPV_CONFIG_DIR.path,
    cacheDir: String = Const.MPV_CACHE_DIR.path,
    fontDir: String = Const.MPV_FONT_DIR.path,
) {
    val commandQueue = remember { Channel<PlayerCommand>(capacity = UNLIMITED) }
    val scope = rememberCoroutineScope()

    var mediaLoaded by rememberSaveable { mutableStateOf(false) }
    var subtitleTrackDialogState by remember { mutableStateOf(SubtitleTrackDialogState.initial) }
    var playState by remember { mutableStateOf(PlayState.initial) }
    var transformState by remember { mutableStateOf(TransformState.initial) }

    val mpvObserver = remember {
        object : MPVLib.EventObserver {
            override fun eventProperty(property: String) {
                when (property) {
                    "video-zoom" -> commandQueue.trySend(PlayerCommand.GetZoom)
                    "video-pan-x" -> commandQueue.trySend(PlayerCommand.GetVideoOffsetX)
                    "video-pan-y" -> commandQueue.trySend(PlayerCommand.GetVideoOffsetY)
                    "speed" -> commandQueue.trySend(PlayerCommand.GetSpeed)
                    "track-list" -> commandQueue.trySend(PlayerCommand.LoadAllTracks)
                }
            }

            override fun eventProperty(property: String, value: Long) {
                when (property) {
                    "time-pos" -> playState = playState.copy(currentPosition = value.toInt())
                    "duration" -> playState = playState.copy(duration = value.toInt())
                    "video-rotate" -> transformState =
                        transformState.copy(videoRotate = value.toFloat())
                }
            }

            override fun eventProperty(property: String, value: Boolean) {
                when (property) {
                    "pause" -> playState = playState.copy(isPlaying = !value)
                }
            }

            override fun eventProperty(property: String, value: String) {
                when (property) {
                    "media-title" -> playState = playState.copy(title = value)
                }
            }

            override fun event(eventId: Int) {
                when (eventId) {
                    MPVLib.mpvEventId.MPV_EVENT_SEEK -> playState =
                        playState.copy(isSeeking = false)

                    MPVLib.mpvEventId.MPV_EVENT_END_FILE -> {
                        mediaLoaded = false
                        playState = playState.copy(isPlaying = false)
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
                initialize(
                    configDir = configDir,
                    cacheDir = cacheDir,
                    fontDir = fontDir,
                )
                addObserver(mpvObserver)
                scope.launch(Dispatchers.Main.immediate) {
                    commandQueue
                        .consumeAsFlow()
                        .startWith(PlayerCommand.SetUri(uri))
                        .onEach { command ->
                            solveCommand(
                                command = command,
                                uri = { uri },
                                isPlayingChanged = {
                                    playState = playState.copy(isPlaying = it)
                                },
                                onSubtitleTrack = {
                                    subtitleTrackDialogState = subtitleTrackDialogState.copy(
                                        show = true,
                                        currentSubtitleTrack = subtitleTrack.find { it.trackId == sid }!!,
                                        subtitleTrack = subtitleTrack,
                                    )
                                },
                                onSubtitleTrackChanged = { newTrackId ->
                                    subtitleTrackDialogState = subtitleTrackDialogState.copy(
                                        currentSubtitleTrack = subtitleTrack.find { it.trackId == newTrackId }!!,
                                    )
                                },
                                transformState = { transformState },
                                onVideoZoom = {
                                    transformState = transformState.copy(videoZoom = it)
                                },
                                onVideoOffset = {
                                    transformState = transformState.copy(videoOffset = it)
                                },
                                onSpeedChanged = { playState = playState.copy(speed = it) },
                            )
                        }
                        .collect()
                }

            }
        },
    )

    PlayerController(
        enabled = { mediaLoaded },
        onBack = onBack,
        onPlayStateChanged = { commandQueue.trySend(PlayerCommand.Paused(playState.isPlaying)) },
        playState = { playState },
        onSeekTo = {
            playState = playState.copy(isSeeking = true)
            commandQueue.trySend(PlayerCommand.SeekTo(it))
        },
        onPlayOrPause = { commandQueue.trySend(PlayerCommand.PlayOrPause) },
        onSpeedChanged = { commandQueue.trySend(PlayerCommand.SetSpeed(it)) },
        subtitleTrackDialogState = { subtitleTrackDialogState },
        onDismissSubtitleTrackDialog = {
            subtitleTrackDialogState = subtitleTrackDialogState.copy(show = false)
        },
        onRequestSubtitleTrack = { commandQueue.trySend(PlayerCommand.GetSubtitleTrack) },
        onSubtitleTrackChanged = { commandQueue.trySend(PlayerCommand.SetSubtitleTrack(it.trackId)) },
        transformState = { transformState },
        onVideoRotate = { commandQueue.trySend(PlayerCommand.Rotate(it.toInt())) },
        onVideoZoom = { commandQueue.trySend(PlayerCommand.Zoom(it)) },
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
                    needPlayWhenResume = playState.isPlaying
                    if (playState.isPlaying) {
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
internal val ControllerLabelGray = Color(0x70000000)

@Composable
private fun PlayerController(
    enabled: () -> Boolean,
    onBack: () -> Unit,
    onPlayStateChanged: () -> Unit,
    playState: () -> PlayState,
    onSeekTo: (position: Int) -> Unit,
    onPlayOrPause: () -> Unit,
    onSpeedChanged: (Float) -> Unit,
    subtitleTrackDialogState: () -> SubtitleTrackDialogState,
    onDismissSubtitleTrackDialog: () -> Unit,
    onRequestSubtitleTrack: () -> Unit,
    onSubtitleTrackChanged: (MPVView.Track) -> Unit,
    transformState: () -> TransformState,
    onVideoRotate: (Float) -> Unit,
    onVideoZoom: (Float) -> Unit,
    onVideoOffset: (Offset) -> Unit,
) {
    var showController by rememberSaveable { mutableStateOf(true) }
    var controllerWidth by remember { mutableIntStateOf(0) }
    var controllerHeight by remember { mutableIntStateOf(0) }
    var controllerLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

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

    var showForwardRipple by remember { mutableStateOf(false) }
    var forwardRippleStartControllerOffset by remember { mutableStateOf(Offset.Zero) }
    var showBackwardRipple by remember { mutableStateOf(false) }
    var backwardRippleStartControllerOffset by remember { mutableStateOf(Offset.Zero) }

    var isLongPressing by remember { mutableStateOf(false) }

    LaunchedEffect(subtitleTrackDialogState()) {
        if (subtitleTrackDialogState().show) cancelAutoHideControllerRunnable()
        else restartAutoHideControllerRunnable()
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    controllerWidth = it.size.width
                    controllerHeight = it.size.height
                    controllerLayoutCoordinates = it
                }
                .detectPressGestures(
                    controllerWidth = { controllerWidth },
                    playState = playState,
                    onSeekTo = onSeekTo,
                    onPlayOrPause = onPlayOrPause,
                    showController = { showController },
                    onShowControllerChanged = { showController = it },
                    onSpeedChanged = onSpeedChanged,
                    isLongPressing = { isLongPressing },
                    isLongPressingChanged = { isLongPressing = it },
                    onShowForwardRipple = {
                        forwardRippleStartControllerOffset = it
                        showForwardRipple = true
                    },
                    onShowBackwardRipple = {
                        backwardRippleStartControllerOffset = it
                        showBackwardRipple = true
                    },
                    cancelAutoHideControllerRunnable = cancelAutoHideControllerRunnable,
                    restartAutoHideControllerRunnable = restartAutoHideControllerRunnable,
                )
                .detectControllerGestures(
                    enabled = enabled,
                    controllerWidth = { controllerWidth },
                    controllerHeight = { controllerHeight },
                    onShowBrightness = { showBrightnessPreview = it },
                    onBrightnessRangeChanged = { brightnessRange = it },
                    onBrightnessChanged = { brightnessValue = it },
                    onShowVolume = { showVolumePreview = it },
                    onVolumeRangeChanged = { volumeRange = it },
                    onVolumeChanged = { volumeValue = it },
                    playState = playState,
                    onSeekTo = onSeekTo,
                    onShowSeekTimePreview = { showSeekTimePreview = it },
                    onTimePreviewChanged = { seekTimePreview = it },
                    transformState = transformState,
                    onVideoRotate = onVideoRotate,
                    onVideoZoom = onVideoZoom,
                    onVideoOffset = onVideoOffset,
                    cancelAutoHideControllerRunnable = cancelAutoHideControllerRunnable,
                    restartAutoHideControllerRunnable = restartAutoHideControllerRunnable,
                )
        ) {
            // Forward ripple
            AnimatedVisibility(
                visible = showForwardRipple,
                modifier = Modifier.align(Alignment.CenterEnd),
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessHigh)),
                exit = fadeOut(),
            ) {
                ForwardRipple(
                    direct = ForwardRippleDirect.Forward,
                    text = "+10s",
                    icon = Icons.Rounded.FastForward,
                    controllerWidth = { controllerWidth },
                    parentLayoutCoordinates = controllerLayoutCoordinates,
                    rippleStartControllerOffset = forwardRippleStartControllerOffset,
                    onHideRipple = { showForwardRipple = false },
                )
            }
            // Backward ripple
            AnimatedVisibility(
                visible = showBackwardRipple,
                modifier = Modifier.align(Alignment.CenterStart),
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessHigh)),
                exit = fadeOut(),
            ) {
                ForwardRipple(
                    direct = ForwardRippleDirect.Backward,
                    text = "-10s",
                    icon = Icons.Rounded.FastRewind,
                    controllerWidth = { controllerWidth },
                    parentLayoutCoordinates = controllerLayoutCoordinates,
                    rippleStartControllerOffset = backwardRippleStartControllerOffset,
                    onHideRipple = { showBackwardRipple = false },
                )
            }
            // Auto hide box
            AutoHiddenBox(
                enabled = enabled,
                show = { showController },
                onBack = onBack,
                onPlayStateChanged = onPlayStateChanged,
                playState = playState,
                onSeekTo = onSeekTo,
                onSubtitleTrackClick = onRequestSubtitleTrack,
                onRestartAutoHideControllerRunnable = restartAutoHideControllerRunnable,
                transformState = transformState,
                onVideoRotate = onVideoRotate,
                onVideoZoom = onVideoZoom,
                onVideoOffset = onVideoOffset,
            )

            // Seek time preview
            if (showSeekTimePreview) {
                SeekTimePreview(
                    value = { seekTimePreview },
                    duration = { playState().duration },
                )
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
                LongPressSpeedPreview(speed = { playState().speed })
            }

            SubtitleTrackDialog(
                onDismissRequest = onDismissSubtitleTrackDialog,
                subtitleTrackDialogState = subtitleTrackDialogState,
                onSubtitleTrackChanged = onSubtitleTrackChanged,
            )
        }
    }
}

@Composable
private fun AutoHiddenBox(
    enabled: () -> Boolean,
    show: () -> Boolean,
    onBack: () -> Unit,
    onPlayStateChanged: () -> Unit,
    playState: () -> PlayState,
    onSeekTo: (position: Int) -> Unit,
    onSubtitleTrackClick: () -> Unit,
    onRestartAutoHideControllerRunnable: () -> Unit,
    transformState: () -> TransformState,
    onVideoRotate: (Float) -> Unit,
    onVideoZoom: (Float) -> Unit,
    onVideoOffset: (Offset) -> Unit,
) {
    Box {
        AnimatedVisibility(
            visible = show(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (topBar, bottomBar, forward85s, resetTransform) = createRefs()

                TopBar(
                    modifier = Modifier.constrainAs(topBar) { top.linkTo(parent.top) },
                    title = playState().title,
                    onBack = onBack,
                )
                BottomBar(
                    modifier = Modifier.constrainAs(bottomBar) { bottom.linkTo(parent.bottom) },
                    onPlayStateChanged = onPlayStateChanged,
                    playState = playState,
                    onSeekTo = onSeekTo,
                    onSubtitleTrackClick = onSubtitleTrackClick,
                    onRestartAutoHideControllerRunnable = onRestartAutoHideControllerRunnable,
                )

                // +85s button
                if (LocalPlayerShow85sButton.current) {
                    Forward85s(
                        modifier = Modifier
                            .constrainAs(forward85s) {
                                bottom.linkTo(bottomBar.top)
                                end.linkTo(parent.end)
                            }
                            .padding(end = 50.dp),
                        onClick = {
                            with(playState()) { onSeekTo(currentPosition + 85) }
                        },
                    )
                }

                // Reset transform
                if (transformState().run {
                        videoZoom != 1f || videoRotate != 0f || videoOffset != Offset.Zero
                    }
                ) {
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
            fontSize = TextUnit(18f, TextUnitType.Sp),
            color = Color.White,
        )
        Text(
            modifier = Modifier.padding(horizontal = 6.dp),
            text = "/",
            style = MaterialTheme.typography.labelLarge,
            fontSize = TextUnit(18f, TextUnitType.Sp),
            color = Color.White,
        )
        Text(
            text = duration().toDurationString(),
            style = MaterialTheme.typography.labelLarge,
            fontSize = TextUnit(18f, TextUnitType.Sp),
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
        val percentValue = (value() - start) / length
        Icon(modifier = Modifier.size(30.dp), imageVector = icon, contentDescription = null)
        LinearProgressIndicator(
            progress = { percentValue },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(100.dp),
            drawStopIndicator = null,
        )
        Text(
            modifier = Modifier.animateContentSize(),
            text = percentValue.toPercentage(format = "%.0f%%"),
            style = MaterialTheme.typography.labelLarge,
            fontSize = TextUnit(18f, TextUnitType.Sp),
            color = Color.White,
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
        val percentValue = (value() - start).toFloat() / length
        Icon(modifier = Modifier.size(30.dp), imageVector = icon, contentDescription = null)
        LinearProgressIndicator(
            progress = { percentValue },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(100.dp),
            drawStopIndicator = null,
        )
        Text(
            modifier = Modifier.animateContentSize(),
            text = percentValue.toPercentage(format = "%.0f%%"),
            style = MaterialTheme.typography.labelLarge,
            fontSize = TextUnit(18f, TextUnitType.Sp),
            color = Color.White,
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
        Icon(
            modifier = Modifier.size(30.dp),
            imageVector = Icons.Rounded.FastForward,
            contentDescription = stringResource(id = R.string.player_long_press_playback_speed),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${speed()}x",
            style = MaterialTheme.typography.labelLarge,
            fontSize = TextUnit(18f, TextUnitType.Sp),
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
        fontSize = TextUnit(16f, TextUnitType.Sp),
        color = Color.White,
    )
}

@Composable
private fun Forward85s(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color = ControllerLabelGray)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        text = stringResource(id = R.string.player_forward_85s),
        style = MaterialTheme.typography.labelLarge,
        fontSize = TextUnit(18f, TextUnitType.Sp),
        color = Color.White,
    )
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
            .padding(horizontal = 6.dp, vertical = 3.dp),
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
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(3.dp))
    }
}

@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    onPlayStateChanged: () -> Unit,
    playState: () -> PlayState,
    onSeekTo: (position: Int) -> Unit,
    onSubtitleTrackClick: () -> Unit,
    onRestartAutoHideControllerRunnable: () -> Unit,
) {
    val playPositionStateValue = playState()

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
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val sliderInteractionSource = remember { MutableInteractionSource() }
            var sliderValue by rememberSaveable {
                mutableFloatStateOf(playPositionStateValue.currentPosition.toFloat())
            }
            var valueIsChanging by rememberSaveable { mutableStateOf(false) }
            if (!valueIsChanging && !playPositionStateValue.isSeeking &&
                sliderValue != playPositionStateValue.currentPosition.toFloat()
            ) {
                sliderValue = playPositionStateValue.currentPosition.toFloat()
            }
            Text(
                text = playPositionStateValue.currentPosition.toDurationString(),
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
                    onSeekTo(sliderValue.toInt())
                    valueIsChanging = false
                },
                valueRange = 0f..playPositionStateValue.duration.toFloat(),
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
                text = playPositionStateValue.duration.toDurationString(),
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
                imageVector = if (playPositionStateValue.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(if (playPositionStateValue.isPlaying) R.string.pause else R.string.play),
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(45.dp)
                    .clickable(onClick = onSubtitleTrackClick)
                    .padding(9.dp),
                imageVector = Icons.Rounded.ClosedCaption,
                contentDescription = stringResource(R.string.player_subtitle_track),
            )
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