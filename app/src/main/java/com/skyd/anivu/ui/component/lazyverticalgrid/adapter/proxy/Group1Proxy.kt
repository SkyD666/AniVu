package com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.MoveUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.dialog.DeleteWarningDialog
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter

open class Group1Proxy(
    val isExpand: (GroupBean) -> Boolean = { false },
    val onExpandChange: (GroupBean, Boolean) -> Unit = { _, _ -> },
    val isEmpty: (index: Int) -> Boolean,
    val onShowAllArticles: (GroupBean) -> Unit = { },
    val onDelete: ((GroupBean) -> Unit)? = null,
    val onMoveFeedsTo: ((from: GroupBean) -> Unit)? = null,
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
            onDelete = onDelete,
            onFeedsMoveTo = onMoveFeedsTo,
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
    onDelete: ((GroupBean) -> Unit)? = null,
    onFeedsMoveTo: ((GroupBean) -> Unit)? = null,
) {
    var expand by remember(data) { mutableStateOf(initExpand(data)) }
    var expandMenu by rememberSaveable { mutableStateOf(false) }
    var openDeleteWarningDialog by rememberSaveable { mutableStateOf(false) }

    val backgroundShapeCorner: Dp by animateDpAsState(
        targetValue = if (expand && !isEmpty(index)) 0.dp else SHAPE_CORNER_DP,
        label = "background shape corner",
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .clip(
                RoundedCornerShape(
                    SHAPE_CORNER_DP,
                    SHAPE_CORNER_DP,
                    backgroundShapeCorner,
                    backgroundShapeCorner,
                )
            )
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .combinedClickable(
                onLongClick = { expandMenu = true },
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

        DropdownMenu(
            expanded = expandMenu,
            onDismissRequest = { expandMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.delete)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                },
                enabled = onDelete != null && data.groupId != GroupBean.DEFAULT_GROUP_ID,
                onClick = {
                    openDeleteWarningDialog = true
                    expandMenu = false
                },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.feed_screen_group_feeds_move_to)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.MoveUp, contentDescription = null)
                },
                enabled = onFeedsMoveTo != null,
                onClick = {
                    onFeedsMoveTo?.invoke(data)
                    expandMenu = false
                },
            )
        }
    }

    DeleteWarningDialog(
        visible = openDeleteWarningDialog,
        title = stringResource(id = R.string.feed_screen_delete_group_warning_title),
        text = stringResource(id = R.string.feed_screen_delete_group_warning, data.name),
        confirmText = stringResource(id = R.string.delete),
        onConfirm = { onDelete?.invoke(data) },
        onDismiss = { openDeleteWarningDialog = false },
        onDismissRequest = { openDeleteWarningDialog = false },
    )
}