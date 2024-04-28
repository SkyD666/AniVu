package com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImportContacts
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.skyd.anivu.R
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.readable
import com.skyd.anivu.ext.toDateTimeString
import com.skyd.anivu.model.bean.ArticleWithEnclosureBean
import com.skyd.anivu.model.bean.ArticleWithFeed
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.preference.behavior.article.ArticleSwipeLeftActionPreference
import com.skyd.anivu.model.preference.behavior.article.ArticleTapActionPreference
import com.skyd.anivu.ui.component.AniVuImage
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.fragment.read.EnclosureBottomSheet
import com.skyd.anivu.ui.fragment.read.ReadFragment
import com.skyd.anivu.ui.local.LocalArticleSwipeLeftAction
import com.skyd.anivu.ui.local.LocalArticleTapAction
import com.skyd.anivu.ui.local.LocalDeduplicateTitleInDesc
import com.skyd.anivu.ui.local.LocalNavController

class Article1Proxy : LazyGridAdapter.Proxy<ArticleWithFeed>() {
    @Composable
    override fun Draw(index: Int, data: ArticleWithFeed) {
        Article1Item(data = data)
    }
}

@Composable
fun Article1Item(
    data: ArticleWithFeed,
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val articleTapAction = LocalArticleTapAction.current
    val articleSwipeLeftAction = LocalArticleSwipeLeftAction.current
    val articleWithEnclosure = data.articleWithEnclosure
    val article = articleWithEnclosure.article
    var expandMenu by rememberSaveable { mutableStateOf(false) }

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                swipeLeftAction(
                    articleSwipeLeftAction,
                    context,
                    navController,
                    articleWithEnclosure,
                )
            }
            false
        },
        positionalThreshold = { it * 0.15f },
    )
    var isSwipeToDismissActive by remember(data) { mutableStateOf(false) }

    LaunchedEffect(swipeToDismissBoxState.progress > 0.15f) {
        isSwipeToDismissActive = swipeToDismissBoxState.progress > 0.15f &&
                swipeToDismissBoxState.targetValue != SwipeToDismissBoxValue.Settled
    }

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        backgroundContent = {
            SwipeBackgroundContent(
                direction = swipeToDismissBoxState.dismissDirection,
                isActive = isSwipeToDismissActive,
                articleSwipeLeftAction = articleSwipeLeftAction,
                context = context,
            )
        },
        enableDismissFromStartToEnd = false,
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .run { if (article.image.isNullOrBlank()) this else height(IntrinsicSize.Max) }
                .combinedClickable(
                    onLongClick = { expandMenu = true },
                    onClick = {
                        tapAction(
                            articleTapAction,
                            context,
                            navController,
                            articleWithEnclosure,
                        )
                    },
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FeedIcon(data = data.feed)
                val feedName = data.feed.nickname.orEmpty().ifBlank { data.feed.title.orEmpty() }
                if (feedName.isNotBlank()) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .weight(1f),
                        text = feedName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val date = article.date?.toDateTimeString(context = context)
                if (!date.isNullOrBlank()) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    val title = article.title?.readable().orEmpty()
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val author = article.author
                    if (!author.isNullOrBlank()) {
                        Text(
                            text = author,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    val description = article.description?.readable()?.let { desc ->
                        if (LocalDeduplicateTitleInDesc.current) desc.replace(title, "") else desc
                    }
                    if (!description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
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
                            text = { Text(text = stringResource(id = R.string.article_screen_read)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.ImportContacts,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                navigateToReadScreen(
                                    navController = navController,
                                    data = articleWithEnclosure,
                                )
                                expandMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.bottom_sheet_enclosure_title)) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_home_storage_24),
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                showEnclosureBottomSheet(
                                    context = context,
                                    data = articleWithEnclosure
                                )
                                expandMenu = false
                            },
                        )
                    }
                }
                if (!article.image.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(15.dp))
                    OutlinedCard(
                        modifier = Modifier
                            .width(90.dp)
                            .fillMaxHeight(),
                    ) {
                        AniVuImage(
                            modifier = Modifier.fillMaxSize(),
                            model = article.image,
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeedIcon(modifier: Modifier = Modifier, data: FeedBean, size: Dp = 22.dp) {
    val icon = data.icon
    if (icon.isNullOrBlank()) {
        Box(
            modifier = modifier
                .size(size)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = data.title?.first().toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    } else {
        AniVuImage(
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            model = icon,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun SwipeBackgroundContent(
    direction: SwipeToDismissBoxValue,
    isActive: Boolean,
    articleSwipeLeftAction: String,
    context: Context,
) {
    val containerColor = MaterialTheme.colorScheme.background
    val containerColorElevated = MaterialTheme.colorScheme.tertiaryContainer
    val backgroundColor = remember { Animatable(containerColor) }

    LaunchedEffect(isActive) {
        backgroundColor.animateTo(if (isActive) containerColorElevated else containerColor)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(backgroundColor.value)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        val painter = when (direction) {
            SwipeToDismissBoxValue.EndToStart -> when (articleSwipeLeftAction) {
                ArticleSwipeLeftActionPreference.READ -> {
                    rememberVectorPainter(image = Icons.Outlined.ImportContacts)
                }

                ArticleSwipeLeftActionPreference.SHOW_ENCLOSURES -> {
                    painterResource(id = R.drawable.ic_home_storage_24)
                }

                else -> rememberVectorPainter(image = Icons.Outlined.ImportContacts)
            }

            SwipeToDismissBoxValue.StartToEnd -> null
            SwipeToDismissBoxValue.Settled -> null
        }
        val contentDescription = when (direction) {
            SwipeToDismissBoxValue.EndToStart -> ArticleSwipeLeftActionPreference
                .toDisplayName(context, articleSwipeLeftAction)

            SwipeToDismissBoxValue.StartToEnd -> null
            SwipeToDismissBoxValue.Settled -> null
        }

        if (painter != null) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

private fun swipeLeftAction(
    articleSwipeLeftAction: String,
    context: Context,
    navController: NavController,
    data: ArticleWithEnclosureBean,
) {
    when (articleSwipeLeftAction) {
        ArticleSwipeLeftActionPreference.READ -> {
            navigateToReadScreen(navController = navController, data = data)
        }

        ArticleSwipeLeftActionPreference.SHOW_ENCLOSURES -> {
            showEnclosureBottomSheet(context = context, data = data)
        }
    }
}

private fun tapAction(
    articleTapAction: String,
    context: Context,
    navController: NavController,
    data: ArticleWithEnclosureBean,
) {
    when (articleTapAction) {
        ArticleTapActionPreference.READ -> {
            navigateToReadScreen(navController = navController, data = data)
        }

        ArticleTapActionPreference.SHOW_ENCLOSURES -> {
            showEnclosureBottomSheet(context = context, data = data)
        }
    }
}

private fun navigateToReadScreen(navController: NavController, data: ArticleWithEnclosureBean) {
    val bundle = Bundle().apply {
        putString(ReadFragment.ARTICLE_ID_KEY, data.article.articleId)
    }
    navController.navigate(R.id.action_to_read_fragment, bundle)
}

private fun showEnclosureBottomSheet(context: Context, data: ArticleWithEnclosureBean) {
    EnclosureBottomSheet().apply {
        show(
            (context.activity as FragmentActivity).supportFragmentManager,
            EnclosureBottomSheet.TAG,
        )
        updateData(ReadFragment.getEnclosuresList(context, data))
    }
}