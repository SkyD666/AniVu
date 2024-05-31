package com.skyd.anivu.ui.mpv

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.ui.component.rememberSystemUiController
import com.skyd.anivu.ui.mpv.controller.PlayerController
import com.skyd.anivu.ui.mpv.controller.bar.BottomBarCallback
import com.skyd.anivu.ui.mpv.controller.state.PlayState
import com.skyd.anivu.ui.mpv.controller.state.PlayStateCallback
import com.skyd.anivu.ui.mpv.controller.state.TransformState
import com.skyd.anivu.ui.mpv.controller.state.TransformStateCallback
import com.skyd.anivu.ui.mpv.controller.state.dialog.DialogCallback
import com.skyd.anivu.ui.mpv.controller.state.dialog.DialogState
import com.skyd.anivu.ui.mpv.controller.state.dialog.OnDismissDialog
import com.skyd.anivu.ui.mpv.controller.state.dialog.SpeedDialogCallback
import com.skyd.anivu.ui.mpv.controller.state.dialog.SpeedDialogState
import com.skyd.anivu.ui.mpv.controller.state.dialog.track.AudioTrackDialogCallback
import com.skyd.anivu.ui.mpv.controller.state.dialog.track.AudioTrackDialogState
import com.skyd.anivu.ui.mpv.controller.state.dialog.track.SubtitleTrackDialogCallback
import com.skyd.anivu.ui.mpv.controller.state.dialog.track.SubtitleTrackDialogState
import `is`.xyz.mpv.MPVLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.pow


private fun MPVView.solveCommand(
    command: PlayerCommand,
    uri: () -> Uri,
    isPlayingChanged: (Boolean) -> Unit,
    onSubtitleTrack: (subtitleTrack: List<MPVView.Track>) -> Unit,
    onSubtitleTrackChanged: (Int) -> Unit,
    onAudioTrack: (subtitleTrack: List<MPVView.Track>) -> Unit,
    onAudioTrackChanged: (Int) -> Unit,
    transformState: () -> TransformState,
    onVideoZoom: (Float) -> Unit,
    onVideoOffset: (Offset) -> Unit,
    onSpeedChanged: (Float) -> Unit,
    onSaveScreenshot: (File) -> Unit,
    onCacheBufferStateChanged: (Float) -> Unit,
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
        PlayerCommand.GetAudioTrack -> onAudioTrack(audioTrack)
        is PlayerCommand.SetSubtitleTrack -> {
            sid = command.trackId
            onSubtitleTrackChanged(command.trackId)
        }

        is PlayerCommand.SetAudioTrack -> {
            aid = command.trackId
            onAudioTrackChanged(command.trackId)
        }

        PlayerCommand.Screenshot -> screenshot(onSaveScreenshot = onSaveScreenshot)
        is PlayerCommand.AddSubtitle -> {
            addSubtitle(command.filePath)
            onSubtitleTrack(subtitleTrack)
        }

        is PlayerCommand.AddAudio -> {
            addAudio(command.filePath)
            onAudioTrack(audioTrack)
        }

        PlayerCommand.GetBuffer -> onCacheBufferStateChanged(demuxerCacheDuration.toFloat())
    }
}

@Composable
fun PlayerView(
    uri: Uri,
    onBack: () -> Unit,
    onSaveScreenshot: (File) -> Unit,
    configDir: String = Const.MPV_CONFIG_DIR.path,
    cacheDir: String = Const.MPV_CACHE_DIR.path,
    fontDir: String = Const.MPV_FONT_DIR.path,
    onPlayerChanged: (MPVView?) -> Unit,
) {
    val systemUiController = rememberSystemUiController().apply {
        isSystemBarsVisible = false
        systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    val commandQueue = remember { Channel<PlayerCommand>(capacity = UNLIMITED) }
    val scope = rememberCoroutineScope()

    var mediaLoaded by rememberSaveable { mutableStateOf(false) }

    var playState by remember { mutableStateOf(PlayState.initial) }
    var transformState by remember { mutableStateOf(TransformState.initial) }

    var subtitleTrackDialogState by remember { mutableStateOf(SubtitleTrackDialogState.initial) }
    var audioTrackDialogState by remember { mutableStateOf(AudioTrackDialogState.initial) }
    var speedDialogState by remember { mutableStateOf(SpeedDialogState.initial) }
    LaunchedEffect(playState.speed) {
        speedDialogState = speedDialogState.copy(currentSpeed = playState.speed)
    }
    val dialogState by remember {
        mutableStateOf(
            DialogState(
                speedDialogState = { speedDialogState },
                audioTrackDialogState = { audioTrackDialogState },
                subtitleTrackDialogState = { subtitleTrackDialogState },
            )
        )
    }

    val playStateCallback = remember {
        PlayStateCallback(
            onPlayStateChanged = { commandQueue.trySend(PlayerCommand.Paused(playState.isPlaying)) },
            onPlayOrPause = { commandQueue.trySend(PlayerCommand.PlayOrPause) },
            onSeekTo = {
                playState = playState.copy(isSeeking = true)
                commandQueue.trySend(PlayerCommand.SeekTo(it))
            },
            onSpeedChanged = { commandQueue.trySend(PlayerCommand.SetSpeed(it)) },
        )
    }
    val transformStateCallback = remember {
        TransformStateCallback(
            onVideoRotate = { commandQueue.trySend(PlayerCommand.Rotate(it.toInt())) },
            onVideoZoom = { commandQueue.trySend(PlayerCommand.Zoom(it)) },
            onVideoOffset = { commandQueue.trySend(PlayerCommand.VideoOffset(it)) },
        )
    }
    val dialogCallback = remember {
        DialogCallback(
            speedDialogCallback = SpeedDialogCallback(
                onSpeedChanged = { commandQueue.trySend(PlayerCommand.SetSpeed(it)) },
            ),
            audioTrackDialogCallback = AudioTrackDialogCallback(
                onAudioTrackChanged = { commandQueue.trySend(PlayerCommand.SetAudioTrack(it.trackId)) },
                onAddAudioTrack = { commandQueue.trySend(PlayerCommand.AddAudio(it)) },
            ),
            subtitleTrackDialogCallback = SubtitleTrackDialogCallback(
                onSubtitleTrackChanged = { commandQueue.trySend(PlayerCommand.SetSubtitleTrack(it.trackId)) },
                onAddSubtitle = { commandQueue.trySend(PlayerCommand.AddSubtitle(it)) },
            )
        )
    }
    val bottomBarCallback = remember {
        BottomBarCallback(
            onSpeedClick = { speedDialogState = speedDialogState.copy(show = true) },
            onAudioTrackClick = { commandQueue.trySend(PlayerCommand.GetAudioTrack) },
            onSubtitleTrackClick = { commandQueue.trySend(PlayerCommand.GetSubtitleTrack) },
        )
    }

    val mpvObserver = remember {
        object : MPVLib.EventObserver {
            override fun eventProperty(property: String) {
                when (property) {
                    "video-zoom" -> commandQueue.trySend(PlayerCommand.GetZoom)
                    "video-pan-x" -> commandQueue.trySend(PlayerCommand.GetVideoOffsetX)
                    "video-pan-y" -> commandQueue.trySend(PlayerCommand.GetVideoOffsetY)
                    "speed" -> commandQueue.trySend(PlayerCommand.GetSpeed)
                    "track-list" -> commandQueue.trySend(PlayerCommand.LoadAllTracks)
                    "demuxer-cache-duration" -> commandQueue.trySend(PlayerCommand.GetBuffer)
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
                                onAudioTrack = {
                                    audioTrackDialogState = audioTrackDialogState.copy(
                                        show = true,
                                        currentAudioTrack = audioTrack.find { it.trackId == aid }!!,
                                        audioTrack = audioTrack,
                                    )
                                },
                                onAudioTrackChanged = { newTrackId ->
                                    audioTrackDialogState = audioTrackDialogState.copy(
                                        currentAudioTrack = audioTrack.find { it.trackId == newTrackId }!!,
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
                                onSaveScreenshot = onSaveScreenshot,
                                onCacheBufferStateChanged = {
                                    playState = playState.copy(bufferDuration = it.toInt())
                                }
                            )
                        }
                        .collect()
                }
                doOnAttach { onPlayerChanged(this) }
                doOnDetach { onPlayerChanged(null) }
            }
        },
    )

    PlayerController(
        enabled = { mediaLoaded },
        onBack = onBack,
        playState = { playState },
        playStateCallback = playStateCallback,
        bottomBarCallback = bottomBarCallback,
        dialogState = dialogState,
        dialogCallback = dialogCallback,
        onDismissDialog = remember {
            OnDismissDialog(
                onDismissSpeedDialog = {
                    speedDialogState = speedDialogState.copy(show = false)
                },
                onDismissAudioTrackDialog = {
                    audioTrackDialogState = audioTrackDialogState.copy(show = false)
                },
                onDismissSubtitleTrackDialog = {
                    subtitleTrackDialogState = subtitleTrackDialogState.copy(show = false)
                },
            )
        },
        transformState = { transformState },
        transformStateCallback = transformStateCallback,
        onScreenshot = { commandQueue.trySend(PlayerCommand.Screenshot) },
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
                    systemUiController.isSystemBarsVisible = false
                    systemUiController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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