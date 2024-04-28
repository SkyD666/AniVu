package com.skyd.anivu.ui.fragment.settings.behavior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.SwipeLeft
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.model.preference.behavior.article.ArticleSwipeLeftActionPreference
import com.skyd.anivu.model.preference.behavior.article.ArticleTapActionPreference
import com.skyd.anivu.model.preference.behavior.article.DeduplicateTitleInDescPreference
import com.skyd.anivu.model.preference.behavior.feed.HideEmptyDefaultPreference
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.BaseSettingsItem
import com.skyd.anivu.ui.component.CategorySettingsItem
import com.skyd.anivu.ui.component.SwitchSettingsItem
import com.skyd.anivu.ui.local.LocalArticleSwipeLeftAction
import com.skyd.anivu.ui.local.LocalArticleTapAction
import com.skyd.anivu.ui.local.LocalDeduplicateTitleInDesc
import com.skyd.anivu.ui.local.LocalHideEmptyDefault
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BehaviorFragment : BaseComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { BehaviorScreen() }
}

@Composable
fun BehaviorScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expandArticleTapActionMenu by rememberSaveable { mutableStateOf(false) }
    var expandArticleSwipeLeftActionMenu by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.behavior_screen_name)) },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                CategorySettingsItem(text = stringResource(id = R.string.behavior_screen_feed_screen_category))
            }
            item {
                SwitchSettingsItem(
                    imageVector = Icons.Outlined.VisibilityOff,
                    text = stringResource(id = R.string.behavior_screen_feed_screen_hide_empty_default),
                    description = stringResource(id = R.string.behavior_screen_feed_screen_hide_empty_default_description),
                    checked = LocalHideEmptyDefault.current,
                    onCheckedChange = {
                        HideEmptyDefaultPreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    }
                )
            }
            item {
                CategorySettingsItem(text = stringResource(id = R.string.behavior_screen_article_screen_category))
            }
            item {
                SwitchSettingsItem(
                    painter = painterResource(id = R.drawable.ic_ink_eraser_24),
                    text = stringResource(id = R.string.behavior_screen_article_screen_deduplicate_title_in_desc),
                    description = stringResource(id = R.string.behavior_screen_article_screen_deduplicate_title_in_desc_description),
                    checked = LocalDeduplicateTitleInDesc.current,
                    onCheckedChange = {
                        DeduplicateTitleInDescPreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.AutoMirrored.Outlined.Article),
                    text = stringResource(id = R.string.behavior_screen_article_tap_action),
                    descriptionText = ArticleTapActionPreference.toDisplayName(
                        context = context,
                        value = LocalArticleTapAction.current,
                    ),
                    dropdownMenu = {
                        ArticleTapActionMenu(
                            expanded = expandArticleTapActionMenu,
                            onDismissRequest = { expandArticleTapActionMenu = false }
                        )
                    },
                    onClick = { expandArticleTapActionMenu = true },
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(image = Icons.Outlined.SwipeLeft),
                    text = stringResource(id = R.string.behavior_screen_article_swipe_left_action),
                    descriptionText = ArticleSwipeLeftActionPreference.toDisplayName(
                        context = context,
                        value = LocalArticleSwipeLeftAction.current,
                    ),
                    dropdownMenu = {
                        ArticleSwipeLeftActionMenu(
                            expanded = expandArticleSwipeLeftActionMenu,
                            onDismissRequest = { expandArticleSwipeLeftActionMenu = false }
                        )
                    },
                    onClick = { expandArticleSwipeLeftActionMenu = true },
                )
            }
        }
    }
}

@Composable
private fun ArticleTapActionMenu(expanded: Boolean, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val articleTapAction = LocalArticleTapAction.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        ArticleTapActionPreference.values.forEach { action ->
            DropdownMenuItem(
                text = { Text(text = ArticleTapActionPreference.toDisplayName(context, action)) },
                leadingIcon = {
                    if (articleTapAction == action) {
                        Icon(imageVector = Icons.Outlined.Done, contentDescription = null)
                    }
                },
                onClick = {
                    ArticleTapActionPreference.put(
                        context = context,
                        scope = scope,
                        value = action,
                    )
                    onDismissRequest()
                },
            )
        }
    }
}

@Composable
private fun ArticleSwipeLeftActionMenu(expanded: Boolean, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val articleSwipeLeftAction = LocalArticleSwipeLeftAction.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        ArticleSwipeLeftActionPreference.values.forEach { action ->
            DropdownMenuItem(
                text = {
                    Text(text = ArticleSwipeLeftActionPreference.toDisplayName(context, action))
                },
                leadingIcon = {
                    if (articleSwipeLeftAction == action) {
                        Icon(imageVector = Icons.Outlined.Done, contentDescription = null)
                    }
                },
                onClick = {
                    ArticleSwipeLeftActionPreference.put(
                        context = context,
                        scope = scope,
                        value = action,
                    )
                    onDismissRequest()
                },
            )
        }
    }
}