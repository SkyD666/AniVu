package com.skyd.anivu.ui.mpv.controller

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.view.WindowInsetsControllerCompat
import com.skyd.anivu.ui.component.rememberSystemUiController
import com.skyd.anivu.ui.local.LocalPlayerShow85sButton
import com.skyd.anivu.ui.local.LocalPlayerShowScreenshotButton
import com.skyd.anivu.ui.mpv.controller.bar.BottomBar
import com.skyd.anivu.ui.mpv.controller.bar.BottomBarCallback
import com.skyd.anivu.ui.mpv.controller.bar.TopBar
import com.skyd.anivu.ui.mpv.controller.button.Forward85s
import com.skyd.anivu.ui.mpv.controller.button.ResetTransform
import com.skyd.anivu.ui.mpv.controller.button.Screenshot
import com.skyd.anivu.ui.mpv.controller.dialog.AudioTrackDialog
import com.skyd.anivu.ui.mpv.controller.dialog.SpeedDialog
import com.skyd.anivu.ui.mpv.controller.dialog.SubtitleTrackDialog
import com.skyd.anivu.ui.mpv.controller.preview.BrightnessPreview
import com.skyd.anivu.ui.mpv.controller.preview.LongPressSpeedPreview
import com.skyd.anivu.ui.mpv.controller.preview.SeekTimePreview
import com.skyd.anivu.ui.mpv.controller.preview.VolumePreview
import com.skyd.anivu.ui.mpv.controller.state.PlayState
import com.skyd.anivu.ui.mpv.controller.state.PlayStateCallback
import com.skyd.anivu.ui.mpv.controller.state.TransformState
import com.skyd.anivu.ui.mpv.controller.state.TransformStateCallback
import com.skyd.anivu.ui.mpv.controller.state.dialog.DialogCallback
import com.skyd.anivu.ui.mpv.controller.state.dialog.DialogState
import com.skyd.anivu.ui.mpv.controller.state.dialog.OnDismissDialog
import kotlinx.coroutines.delay


internal val ControllerBarGray = Color(0xAD000000)
internal val ControllerLabelGray = Color(0x70000000)

@Composable
internal fun PlayerController(
    enabled: () -> Boolean,
    onBack: () -> Unit,
    playState: () -> PlayState,
    playStateCallback: PlayStateCallback,
    bottomBarCallback: BottomBarCallback,
    dialogState: DialogState,
    dialogCallback: DialogCallback,
    onDismissDialog: OnDismissDialog,
    transformState: () -> TransformState,
    transformStateCallback: TransformStateCallback,
    onScreenshot: () -> Unit,
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

    LaunchedEffect(dialogState.subtitleTrackDialogState()) {
        if (dialogState.subtitleTrackDialogState().show) cancelAutoHideControllerRunnable()
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
                // detectControllerGestures should be called before detectPressGestures
                // to avoid responding to swipes when long pressing
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
                    playStateCallback = playStateCallback,
                    onShowSeekTimePreview = { showSeekTimePreview = it },
                    onTimePreviewChanged = { seekTimePreview = it },
                    transformState = transformState,
                    transformStateCallback = transformStateCallback,
                    cancelAutoHideControllerRunnable = cancelAutoHideControllerRunnable,
                    restartAutoHideControllerRunnable = restartAutoHideControllerRunnable,
                )
                .detectPressGestures(
                    controllerWidth = { controllerWidth },
                    playState = playState,
                    playStateCallback = playStateCallback,
                    showController = { showController },
                    onShowControllerChanged = { showController = it },
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
                playState = playState,
                playStateCallback = playStateCallback,
                bottomBarCallback = bottomBarCallback,
                transformState = transformState,
                transformStateCallback = transformStateCallback,
                onScreenshot = onScreenshot,
                onRestartAutoHideControllerRunnable = restartAutoHideControllerRunnable,
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

            SpeedDialog(
                onDismissRequest = onDismissDialog.onDismissSpeedDialog,
                speedDialogState = dialogState.speedDialogState,
                speedDialogCallback = dialogCallback.speedDialogCallback,
            )
            AudioTrackDialog(
                onDismissRequest = onDismissDialog.onDismissAudioTrackDialog,
                audioTrackDialogState = dialogState.audioTrackDialogState,
                audioTrackDialogCallback = dialogCallback.audioTrackDialogCallback,
            )
            SubtitleTrackDialog(
                onDismissRequest = onDismissDialog.onDismissSubtitleTrackDialog,
                subtitleTrackDialogState = dialogState.subtitleTrackDialogState,
                subtitleTrackDialogCallback = dialogCallback.subtitleTrackDialogCallback,
            )

            val systemUiController = rememberSystemUiController()
            LaunchedEffect(
                dialogState.subtitleTrackDialogState().show,
                dialogState.audioTrackDialogState().show,
            ) {
                delay(200)
                systemUiController.isSystemBarsVisible = false
                systemUiController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}

@Composable
private fun AutoHiddenBox(
    enabled: () -> Boolean,
    show: () -> Boolean,
    onBack: () -> Unit,
    playState: () -> PlayState,
    playStateCallback: PlayStateCallback,
    bottomBarCallback: BottomBarCallback,
    transformState: () -> TransformState,
    transformStateCallback: TransformStateCallback,
    onScreenshot: () -> Unit,
    onRestartAutoHideControllerRunnable: () -> Unit,
) {
    Box {
        AnimatedVisibility(
            visible = show(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (topBar, bottomBar, screenshot, forward85s, resetTransform) = createRefs()

                TopBar(
                    modifier = Modifier.constrainAs(topBar) { top.linkTo(parent.top) },
                    title = playState().title,
                    onBack = onBack,
                )
                BottomBar(
                    modifier = Modifier.constrainAs(bottomBar) { bottom.linkTo(parent.bottom) },
                    playStateCallback = playStateCallback,
                    playState = playState,
                    bottomBarCallback = bottomBarCallback,
                    onRestartAutoHideControllerRunnable = onRestartAutoHideControllerRunnable,
                )

                if (LocalPlayerShowScreenshotButton.current) {
                    Screenshot(
                        modifier = Modifier
                            .constrainAs(screenshot) {
                                bottom.linkTo(parent.bottom)
                                top.linkTo(parent.top)
                                end.linkTo(parent.end)
                            }
                            .padding(end = 20.dp),
                        onClick = onScreenshot,
                    )
                }

                // +85s button
                if (LocalPlayerShow85sButton.current) {
                    Forward85s(
                        modifier = Modifier
                            .constrainAs(forward85s) {
                                bottom.linkTo(bottomBar.top)
                                end.linkTo(parent.end)
                            }
                            .padding(end = 20.dp),
                        onClick = {
                            with(playState()) { playStateCallback.onSeekTo(currentPosition + 85) }
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
                            with(transformStateCallback) {
                                onVideoOffset(Offset.Zero)
                                onVideoZoom(1f)
                                onVideoRotate(0f)
                            }
                        }
                    )
                }
            }
        }
    }
}