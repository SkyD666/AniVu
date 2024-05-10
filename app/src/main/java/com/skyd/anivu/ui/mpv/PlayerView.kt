package com.skyd.anivu.ui.mpv

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
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.skyd.anivu.ext.alwaysLight
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

sealed interface PlayerCommand {
    data class SetUri(val uri: Uri) : PlayerCommand
    data object Destroy : PlayerCommand
    data class Paused(val paused: Boolean) : PlayerCommand
    data object PlayOrPause : PlayerCommand
    data class SeekTo(val position: Int) : PlayerCommand
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

    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableIntStateOf(0) }
    var currentPosition by rememberSaveable { mutableIntStateOf(0) }
    var isSeeking by rememberSaveable { mutableStateOf(false) }

    val mpvObserver = remember {
        object : MPVLib.EventObserver {
            override fun eventProperty(property: String) {
            }

            override fun eventProperty(property: String, value: Long) {
                when (property) {
                    "time-pos" -> currentPosition = value.toInt()
                    "duration" -> duration = value.toInt()
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
                            when (command) {
                                is PlayerCommand.SetUri -> uri.resolveUri(context)
                                    ?.let { playFile(it) }

                                PlayerCommand.Destroy -> destroy()
                                is PlayerCommand.Paused -> paused = command.paused
                                PlayerCommand.PlayOrPause -> cyclePause()
                                is PlayerCommand.SeekTo -> seek(command.position)
                            }
                        }
                        .collect()
                }
            }
        },
        update = { view ->
        }
    )

    PlayerController(
        isPlaying = isPlaying,
        title = title,
        onBack = onBack,
        onPlayStateChanged = {
            isPlaying = !isPlaying
            commandQueue.trySend(PlayerCommand.Paused(!isPlaying))
        },
        currentPosition = currentPosition,
        duration = duration,
        isSeeking = isSeeking,
        onSeekTo = {
            isSeeking = true
            commandQueue.trySend(PlayerCommand.SeekTo(it))
        },
        onPlayOrPause = { commandQueue.trySend(PlayerCommand.PlayOrPause) },
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
                    if (!isPlaying) {
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

@Composable
private fun PlayerController(
    isPlaying: Boolean,
    title: String,
    onBack: () -> Unit,
    onPlayStateChanged: () -> Unit,
    isSeeking: Boolean,
    currentPosition: Int,
    duration: Int,
    onSeekTo: (Int) -> Unit,
    onPlayOrPause: () -> Unit,
) {
    val doubleTapPreference = LocalPlayerDoubleTap.current
    var showController by rememberSaveable { mutableStateOf(true) }
    var controllerWidth by remember { mutableStateOf(0) }

    val onDoubleTapPausePlay: () -> Unit = remember { { onPlayOrPause() } }
    val onDoubleTapBackwardForward: (Offset) -> Unit = remember {
        { offset ->
            if (offset.x < controllerWidth / 2f) {
                onSeekTo(currentPosition - 10000) // -10s.
            } else {
                onSeekTo(currentPosition + 10000) // +10s.
            }
        }
    }
    val onDoubleTapBackwardPausePlayForward: (Offset) -> Unit = remember {
        { offset ->
            if (offset.x <= controllerWidth * 0.25f) {
                onSeekTo(currentPosition - 10000) // -10s.
            } else if (offset.x >= controllerWidth * 0.75f) {
                onSeekTo(currentPosition + 10000) // +10s.
            } else {
                onDoubleTapPausePlay()
            }
        }
    }
    val onDoubleTap: (Offset) -> Unit = remember {
        { offset ->
            when (doubleTapPreference) {
                PlayerDoubleTapPreference.BACKWARD_FORWARD -> onDoubleTapBackwardForward(offset)
                PlayerDoubleTapPreference.BACKWARD_PAUSE_PLAY_FORWARD ->
                    onDoubleTapBackwardPausePlayForward(offset)

                else -> onDoubleTapPausePlay()
            }
        }
    }
    val onTap: (Offset) -> Unit = remember {
        {
            showController = !showController
        }
    }

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

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    controllerWidth = it.size.width
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            restartAutoHideControllerRunnable()
                            onDoubleTap(it)
                        },
                        onTap = {
                            cancelAutoHideControllerRunnable()
                            onTap(it)
                        }
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
                        val (topBar, bottomBar) = createRefs()

                        TopBar(
                            modifier = Modifier.constrainAs(topBar) { top.linkTo(parent.top) },
                            title = title,
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
                    }
                }
            }

        }
    }
}

@Composable
private fun TopBar(modifier: Modifier = Modifier, title: String, onBack: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xAD000000), Color.Transparent)
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
    isPlaying: Boolean,
    onPlayStateChanged: () -> Unit,
    isSeeking: Boolean,
    currentPosition: Int,
    duration: Int,
    onPositionChanged: (position: Int) -> Unit,
    onRestartAutoHideControllerRunnable: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xAD000000))
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
        val sliderInteractionSource = remember { MutableInteractionSource() }
        var sliderValue by rememberSaveable { mutableStateOf(currentPosition.toFloat()) }
        var valueIsChanging by rememberSaveable { mutableStateOf(false) }
        if (!valueIsChanging && !isSeeking && sliderValue != currentPosition.toFloat()) {
            sliderValue = currentPosition.toFloat()
        }
        Slider(
            modifier = Modifier.height(10.dp),
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
            valueRange = 0f..duration.toFloat(),
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
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(62.dp)
                    .clickable(onClick = onPlayStateChanged)
                    .padding(13.dp),
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = currentPosition.toDurationString(),
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
                text = duration.toDurationString(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
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