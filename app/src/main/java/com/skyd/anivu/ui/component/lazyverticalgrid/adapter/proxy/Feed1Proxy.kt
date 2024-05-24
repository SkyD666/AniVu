package com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ext.readable
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.FeedViewBean
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.fragment.article.ArticleFragment
import com.skyd.anivu.ui.local.LocalNavController

class Feed1Proxy(
    private val visible: (groupId: String) -> Boolean = { true },
    private val isEnded: (index: Int) -> Boolean = { false },
    private val useCardLayout: () -> Boolean = { false },
    private val onClick: ((FeedBean) -> Unit)? = null,
    private val onRemove: ((FeedBean) -> Unit)? = null,
    private val onEdit: ((FeedBean) -> Unit)? = null,
) : LazyGridAdapter.Proxy<FeedViewBean>() {
    @Composable
    override fun Draw(index: Int, data: FeedViewBean) {
        Feed1Item(
            index = index,
            data = data,
            visible = visible,
            isEnded = isEnded,
            useCardLayout = useCardLayout,
            onClick = onClick,
            onRemove = onRemove,
            onEdit = onEdit,
        )
    }
}

@Composable
fun Feed1Item(
    index: Int,
    data: FeedViewBean,
    visible: (groupId: String) -> Boolean,
    useCardLayout: () -> Boolean,
    onClick: ((FeedBean) -> Unit)? = null,
    isEnded: (index: Int) -> Boolean,
    onRemove: ((FeedBean) -> Unit)? = null,
    onEdit: ((FeedBean) -> Unit)? = null,
) {
    val navController = LocalNavController.current
    var expandMenu by rememberSaveable { mutableStateOf(false) }
    val feed = data.feed

    AnimatedVisibility(
        visible = visible(feed.groupId ?: GroupBean.DEFAULT_GROUP_ID),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val isEnd = isEnded(index)
        Row(
            modifier = Modifier
                .padding(horizontal = if (useCardLayout()) 16.dp else 0.dp)
                .clip(
                    if (useCardLayout() && isEnd) {
                        RoundedCornerShape(0.dp, 0.dp, SHAPE_CORNER_DP, SHAPE_CORNER_DP)
                    } else RectangleShape
                )
                .run {
                    if (useCardLayout()) background(color = MaterialTheme.colorScheme.surfaceContainer)
                    else this
                }
                .combinedClickable(
                    onLongClick = if (onRemove != null && onEdit != null) {
                        { expandMenu = true }
                    } else null,
                    onClick = {
                        if (onClick == null) {
                            navController.navigate(R.id.action_to_article_fragment, Bundle().apply {
                                putStringArrayList(
                                    ArticleFragment.FEED_URLS_KEY,
                                    arrayListOf(feed.url)
                                )
                            })
                        } else onClick(feed)
                    },
                )
                .padding(horizontal = if (useCardLayout()) 20.dp else 16.dp, vertical = 10.dp)
                .padding(bottom = if (useCardLayout() && isEnd) 6.dp else 0.dp)
        ) {
            FeedIcon(modifier = Modifier.padding(vertical = 3.dp), data = feed, size = 36.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val title = rememberSaveable(feed.title, feed.nickname) {
                        feed.nickname.orEmpty().ifBlank { feed.title?.readable().orEmpty() }
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val feedCount = data.articleCount
                    if (feedCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.outline,
                            content = {
                                Text(
                                    text = feedCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                        )
                    }
                }
                val description =
                    rememberSaveable(feed.description) { feed.description?.readable().orEmpty() }
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                DropdownMenu(
                    expanded = expandMenu,
                    onDismissRequest = { expandMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.remove)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                        },
                        onClick = {
                            onRemove?.invoke(feed)
                            expandMenu = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.edit)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
                        },
                        onClick = {
                            onEdit?.invoke(feed)
                            expandMenu = false
                        },
                    )
                }
            }
        }
    }
}