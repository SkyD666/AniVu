package com.skyd.anivu.ui.mpv.controller.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ui.mpv.controller.ControllerLabelGray


@Composable
internal fun Screenshot(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Icon(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color = ControllerLabelGray)
            .clickable(onClick = onClick)
            .padding(10.dp),
        imageVector = Icons.Rounded.CameraAlt,
        contentDescription = stringResource(id = R.string.player_screenshot),
        tint = Color.White,
    )
}