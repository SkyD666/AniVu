package com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
    private val onRemove: ((FeedBean) -> Unit)? = null,
    private val onEdit: ((FeedBean) -> Unit)? = null,
) : LazyGridAdapter.Proxy<FeedViewBean>() {
    @Composable
    override fun Draw(index: Int, data: FeedViewBean) {
        Feed1Item(data = data, visible = visible, onRemove = onRemove, onEdit = onEdit)
    }
}

@Composable
fun Feed1Item(
    data: FeedViewBean,
    visible: (groupId: String) -> Boolean,
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
        Column(
            modifier = Modifier
                .combinedClickable(
                    onLongClick = if (onRemove != null && onEdit != null) {
                        { expandMenu = true }
                    } else null,
                    onClick = {
                        navController.navigate(R.id.action_to_article_fragment, Bundle().apply {
                            putStringArrayList(ArticleFragment.FEED_URLS_KEY, arrayListOf(feed.url))
                        })
                    },
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeedIcon(modifier = Modifier.padding(3.dp), data = feed, size = 24.dp)
                val title = rememberSaveable(feed.title, feed.nickname) {
                    feed.nickname.orEmpty().ifBlank { feed.title?.readable().orEmpty() }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val feedCount = data.articleCount
                if (feedCount > 0) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
            Spacer(modifier = Modifier.height(6.dp))
            val description =
                rememberSaveable(feed.description) { feed.description?.readable().orEmpty() }
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.padding(top = 2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
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