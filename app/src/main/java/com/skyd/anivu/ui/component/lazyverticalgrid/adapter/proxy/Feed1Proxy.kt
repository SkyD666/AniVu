package com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ext.readable
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.fragment.article.ArticleFragment
import com.skyd.anivu.ui.local.LocalNavController

class Feed1Proxy(
    private val visible: (groupId: String) -> Boolean = { true },
    private val onRemove: ((FeedBean) -> Unit)? = null,
    private val onEdit: ((FeedBean) -> Unit)? = null,
) : LazyGridAdapter.Proxy<FeedBean>() {
    @Composable
    override fun Draw(index: Int, data: FeedBean) {
        Feed1Item(data = data, visible = visible, onRemove = onRemove, onEdit = onEdit)
    }
}

@Composable
fun Feed1Item(
    data: FeedBean,
    visible: (groupId: String) -> Boolean,
    onRemove: ((FeedBean) -> Unit)? = null,
    onEdit: ((FeedBean) -> Unit)? = null,
) {
    val navController = LocalNavController.current
    var expandMenu by rememberSaveable { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visible(data.groupId ?: GroupBean.DEFAULT_GROUP_ID),
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
                            putString(ArticleFragment.FEED_URL_KEY, data.url)
                        })
                    },
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val title = rememberSaveable(data.title) { data.title?.readable().orEmpty() }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val description =
                rememberSaveable(data.description) { data.description?.readable().orEmpty() }
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.padding(top = 2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
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
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    },
                    onClick = {
                        onRemove?.invoke(data)
                        expandMenu = false
                    },
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.edit)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                    },
                    onClick = {
                        onEdit?.invoke(data)
                        expandMenu = false
                    },
                )
            }
        }
    }
}