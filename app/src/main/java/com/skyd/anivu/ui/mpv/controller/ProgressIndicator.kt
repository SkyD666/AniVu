package com.skyd.anivu.ui.mpv.controller

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.skyd.anivu.ui.mpv.controller.state.PlayState

@Composable
fun ProgressIndicator(modifier: Modifier = Modifier, playState: () -> PlayState) {
    val animatedProgress by animateFloatAsState(
        targetValue = playState().run { currentPosition.toFloat() / duration },
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "playerProgressIndicatorAnimate"
    )
    Log.e("TAG", "ProgressIndicator: $animatedProgress", )
    LinearProgressIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        progress = { animatedProgress },
    )
}