package com.skyd.anivu.ui.mpv.controller.bar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ui.mpv.controller.ControllerBarGray
import com.skyd.anivu.ui.mpv.controller.state.PlayState
import com.skyd.anivu.ui.mpv.controller.state.PlayStateCallback
import kotlin.math.abs


@Immutable
data class BottomBarCallback(
    val onSpeedClick: () -> Unit,
    val onAudioTrackClick: () -> Unit,
    val onSubtitleTrackClick: () -> Unit,
)

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    playState: () -> PlayState,
    playStateCallback: PlayStateCallback,
    bottomBarCallback: BottomBarCallback,
    onRestartAutoHideControllerRunnable: () -> Unit,
) {
    val playStateValue = playState()

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
                mutableFloatStateOf(playStateValue.currentPosition.toFloat())
            }
            var valueIsChanging by rememberSaveable { mutableStateOf(false) }
            if (!valueIsChanging && !playStateValue.isSeeking &&
                sliderValue != playStateValue.currentPosition.toFloat()
            ) {
                sliderValue = playStateValue.currentPosition.toFloat()
            }
            Text(
                text = playStateValue.currentPosition.toDurationString(),
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
                    playStateCallback.onSeekTo(sliderValue.toInt())
                    valueIsChanging = false
                },
                colors = SliderDefaults.colors(),
                interactionSource = sliderInteractionSource,
                thumb = {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Thumb(interactionSource = sliderInteractionSource)
                    }
                },
                track = { sliderState ->
                    Track(
                        sliderState = sliderState,
                        bufferDurationValue = playStateValue.bufferDuration.toFloat()
                    )
                },
                valueRange = 0f..playStateValue.duration.toFloat(),
            )
            Text(
                text = playStateValue.duration.toDurationString(),
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
                    .size(50.dp)
                    .clickable(onClick = playStateCallback.onPlayStateChanged)
                    .padding(7.dp),
                imageVector = if (playStateValue.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(if (playStateValue.isPlaying) R.string.pause else R.string.play),
            )

            Spacer(modifier = Modifier.weight(1f))

            // Speed button
            Text(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .clip(CircleShape)
                    .height(45.dp)
                    .clickable(onClick = bottomBarCallback.onSpeedClick)
                    .padding(9.dp)
                    .animateContentSize()
                    // For vertical centering
                    .wrapContentHeight(),
                text = "${playStateValue.speed}x",
                style = MaterialTheme.typography.labelLarge,
                fontSize = TextUnit(16f, TextUnitType.Sp),
                textAlign = TextAlign.Center,
                color = Color.White,
            )
            // Audio track button
            BarIconButton(
                onClick = bottomBarCallback.onAudioTrackClick,
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = stringResource(R.string.player_audio_track),
            )
            // Audio track button
            BarIconButton(
                onClick = bottomBarCallback.onSubtitleTrackClick,
                imageVector = Icons.Rounded.ClosedCaption,
                contentDescription = stringResource(R.string.player_subtitle_track),
            )
        }
    }
}

fun Int.toDurationString(sign: Boolean = false, splitter: String = ":"): String {
    if (sign) return (if (this >= 0) "+" else "-") + abs(this).toDurationString()

    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60
    return if (hours == 0) "%02d$splitter%02d".format(minutes, seconds)
    else "%d$splitter%02d$splitter%02d".format(hours, minutes, seconds)
}