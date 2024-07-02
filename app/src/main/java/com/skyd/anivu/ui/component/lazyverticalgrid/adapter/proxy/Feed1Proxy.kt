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
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
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
    private val selected: (FeedBean) -> Boolean = { false },
    private val isEnded: (index: Int) -> Boolean = { false },
    private val inGroup: () -> Boolean = { false },
    private val onClick: ((FeedBean) -> Unit)? = null,
    private val onEdit: ((FeedBean) -> Unit)? = null,
) : LazyGridAdapter.Proxy<FeedViewBean>() {
    @Composable
    override fun Draw(index: Int, data: FeedViewBean) {
        Feed1Item(
            index = index,
            data = data,
            visible = visible,
            selected = selected,
            isEnded = isEnded,
            inGroup = inGroup,
            onClick = onClick,
            onEdit = onEdit,
        )
    }
}

@Composable
fun Feed1Item(
    index: Int,
    data: FeedViewBean,
    visible: (groupId: String) -> Boolean,
    selected: (FeedBean) -> Boolean,
    inGroup: () -> Boolean,
    onClick: ((FeedBean) -> Unit)? = null,
    isEnded: (index: Int) -> Boolean,
    onEdit: ((FeedBean) -> Unit)? = null,
) {
    val navController = LocalNavController.current
    val feed = data.feed

    AnimatedVisibility(
        visible = visible(feed.groupId ?: GroupBean.DEFAULT_GROUP_ID),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val isEnd = isEnded(index)
        Row(
            modifier = Modifier
                .clip(
                    if (inGroup()) {
                        if (isEnd) RoundedCornerShape(0.dp, 0.dp, SHAPE_CORNER_DP, SHAPE_CORNER_DP)
                        else RectangleShape
                    } else {
                        RoundedCornerShape(12.dp)
                    }
                )
                .background(
                    MaterialTheme.colorScheme.secondary.copy(
                        alpha = if (selected(feed)) 0.15f else 0.1f
                    )
                )
                .combinedClickable(
                    onLongClick = if (onEdit != null) {
                        { onEdit(feed) }
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
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = if (inGroup() && isEnd) 6.dp else 0.dp)
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
                val description = rememberSaveable(feed.customDescription, feed.description) {
                    if (feed.customDescription == null) {
                        feed.description?.readable().orEmpty()
                    } else {
                        feed.customDescription.readable()
                    }
                }
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}