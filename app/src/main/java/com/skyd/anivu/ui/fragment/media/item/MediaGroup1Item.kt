package com.skyd.anivu.ui.fragment.media.item

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skyd.anivu.model.bean.MediaGroupBean
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.SHAPE_CORNER_DP

@Composable
fun MediaGroup1Item(
    index: Int,
    data: MediaGroupBean,
    initExpand: (MediaGroupBean) -> Boolean = { false },
    onExpandChange: (MediaGroupBean, Boolean) -> Unit,
    isEmpty: (index: Int) -> Boolean,
    onEdit: ((MediaGroupBean) -> Unit)? = null,
) {
    var expand by remember(data) { mutableStateOf(initExpand(data)) }

    val backgroundShapeCorner: Dp by animateDpAsState(
        targetValue = if (expand && !isEmpty(index)) 0.dp else SHAPE_CORNER_DP,
        label = "background shape corner",
    )

    Row(
        modifier = Modifier
            .padding(top = 16.dp)
            .clip(
                RoundedCornerShape(
                    SHAPE_CORNER_DP,
                    SHAPE_CORNER_DP,
                    backgroundShapeCorner,
                    backgroundShapeCorner,
                )
            )
            .background(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
            .combinedClickable(
                onLongClick = if (onEdit == null) null else {
                    { onEdit(data) }
                },
                onClick = {
                    expand = !expand
                    onExpandChange(data, expand)
                },
            )
            .padding(start = 20.dp, end = 8.dp)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = data.name,
            style = MaterialTheme.typography.titleMedium,
        )

        val expandIconRotate by animateFloatAsState(
            targetValue = if (expand) 0f else 180f,
            label = "expand icon rotate",
        )

        Icon(
            modifier = Modifier
                .padding(12.dp)
                .rotate(expandIconRotate),
            imageVector = Icons.Outlined.KeyboardArrowUp,
            contentDescription = null,
        )
    }
}