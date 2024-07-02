package com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.local.LocalFeedListTonalElevation

open class Group1Proxy(
    val isExpand: (GroupBean) -> Boolean = { false },
    val onExpandChange: (GroupBean, Boolean) -> Unit = { _, _ -> },
    val isEmpty: (index: Int) -> Boolean,
    val onShowAllArticles: (GroupBean) -> Unit = { },
    val onEdit: ((GroupBean) -> Unit)? = null,
) : LazyGridAdapter.Proxy<GroupBean>() {
    @Composable
    override fun Draw(index: Int, data: GroupBean) {
        Group1Item(
            index = index,
            data = data,
            initExpand = isExpand,
            onExpandChange = onExpandChange,
            isEmpty = isEmpty,
            onShowAllArticles = onShowAllArticles,
            onEdit = onEdit,
        )
    }
}

val SHAPE_CORNER_DP = 26.dp

@Composable
fun Group1Item(
    index: Int,
    data: GroupBean,
    initExpand: (GroupBean) -> Boolean = { false },
    onExpandChange: (GroupBean, Boolean) -> Unit,
    isEmpty: (index: Int) -> Boolean,
    onShowAllArticles: (GroupBean) -> Unit,
    onEdit: ((GroupBean) -> Unit)? = null,
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
                onClick = { onShowAllArticles(data) },
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

        AniVuIconButton(
            onClick = {
                expand = !expand
                onExpandChange(data, expand)
            },
            imageVector = Icons.Outlined.KeyboardArrowUp,
            contentDescription = null,
            rotate = expandIconRotate,
        )
    }
}