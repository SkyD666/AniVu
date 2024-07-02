package com.skyd.anivu.ui.mpv.controller.bar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
internal fun BarIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
) {
    Icon(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .clip(CircleShape)
            .size(45.dp)
            .clickable(onClick = onClick)
            .padding(9.dp),
        imageVector = imageVector,
        contentDescription = contentDescription,
    )
}