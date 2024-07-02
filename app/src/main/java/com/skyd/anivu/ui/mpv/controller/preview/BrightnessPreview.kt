package com.skyd.anivu.ui.mpv.controller.preview

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.skyd.anivu.ext.toPercentage
import com.skyd.anivu.ui.mpv.controller.ControllerLabelGray

@Composable
internal fun BoxScope.BrightnessPreview(
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