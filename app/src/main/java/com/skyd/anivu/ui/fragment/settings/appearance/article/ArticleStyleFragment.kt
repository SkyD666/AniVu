package com.skyd.anivu.ui.fragment.settings.appearance.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Tonality
import androidx.compose.material.icons.outlined.WidthNormal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.model.preference.appearance.article.ArticleItemMinWidthPreference
import com.skyd.anivu.model.preference.appearance.article.ArticleItemTonalElevationPreference
import com.skyd.anivu.model.preference.appearance.article.ArticleListTonalElevationPreference
import com.skyd.anivu.model.preference.appearance.article.ArticleTopBarTonalElevationPreference
import com.skyd.anivu.model.preference.appearance.article.ShowArticlePullRefreshPreference
import com.skyd.anivu.model.preference.appearance.article.ShowArticleTopBarRefreshPreference
import com.skyd.anivu.model.preference.appearance.feed.TonalElevationPreferenceUtil
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.BaseSettingsItem
import com.skyd.anivu.ui.component.CategorySettingsItem
import com.skyd.anivu.ui.component.SwitchSettingsItem
import com.skyd.anivu.ui.component.dialog.SliderDialog
import com.skyd.anivu.ui.fragment.settings.appearance.feed.TonalElevationDialog
import com.skyd.anivu.ui.local.LocalArticleItemMinWidth
import com.skyd.anivu.ui.local.LocalArticleItemTonalElevation
import com.skyd.anivu.ui.local.LocalArticleListTonalElevation
import com.skyd.anivu.ui.local.LocalArticleTopBarTonalElevation
import com.skyd.anivu.ui.local.LocalShowArticlePullRefresh
import com.skyd.anivu.ui.local.LocalShowArticleTopBarRefresh
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ArticleStyleFragment : BaseComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { ArticleStyleScreen() }
}

@Composable
fun ArticleStyleScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.article_style_screen_name)) },
            )
        }
    ) { paddingValues ->
        var openTopBarTonalElevationDialog by rememberSaveable { mutableStateOf(false) }
        var openArticleListTonalElevationDialog by rememberSaveable { mutableStateOf(false) }
        var openArticleItemTonalElevationDialog by rememberSaveable { mutableStateOf(false) }
        var openArticleItemMinWidthDialog by rememberSaveable { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                CategorySettingsItem(text = stringResource(id = R.string.article_style_screen_top_bar_category))
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.Tonality),
                    text = stringResource(id = R.string.tonal_elevation),
                    descriptionText = TonalElevationPreferenceUtil.toDisplay(
                        LocalArticleTopBarTonalElevation.current
                    ),
                    onClick = { openTopBarTonalElevationDialog = true }
                )
            }
            item {
                SwitchSettingsItem(
                    imageVector = Icons.Outlined.Refresh,
                    text = stringResource(id = R.string.article_style_screen_top_bar_refresh),
                    description = stringResource(id = R.string.article_style_screen_top_bar_refresh_description),
                    checked = LocalShowArticleTopBarRefresh.current,
                    onCheckedChange = {
                        ShowArticleTopBarRefreshPreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    }
                )
            }
            item {
                CategorySettingsItem(text = stringResource(id = R.string.article_style_screen_article_list_category))
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.Tonality),
                    text = stringResource(id = R.string.tonal_elevation),
                    descriptionText = TonalElevationPreferenceUtil.toDisplay(
                        LocalArticleListTonalElevation.current
                    ),
                    onClick = { openArticleListTonalElevationDialog = true }
                )
            }
            item {
                SwitchSettingsItem(
                    imageVector = Icons.Outlined.Refresh,
                    text = stringResource(id = R.string.article_style_screen_pull_refresh),
                    description = stringResource(id = R.string.article_style_screen_pull_refresh_description),
                    checked = LocalShowArticlePullRefresh.current,
                    onCheckedChange = {
                        ShowArticlePullRefreshPreference.put(
                            context = context,
                            scope = scope,
                            value = it,
                        )
                    }
                )
            }
            item {
                CategorySettingsItem(text = stringResource(id = R.string.article_style_screen_article_item_category))
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.Tonality),
                    text = stringResource(id = R.string.tonal_elevation),
                    descriptionText = TonalElevationPreferenceUtil.toDisplay(
                        LocalArticleItemTonalElevation.current
                    ),
                    onClick = { openArticleItemTonalElevationDialog = true }
                )
            }
            item {
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.WidthNormal),
                    text = stringResource(id = R.string.min_width_dp),
                    descriptionText = "${LocalArticleItemMinWidth.current} dp",
                    onClick = { openArticleItemMinWidthDialog = true }
                )
            }
        }

        if (openTopBarTonalElevationDialog) {
            TonalElevationDialog(
                onDismissRequest = { openTopBarTonalElevationDialog = false },
                initValue = LocalArticleTopBarTonalElevation.current,
                defaultValue = { ArticleTopBarTonalElevationPreference.default },
                onConfirm = {
                    ArticleTopBarTonalElevationPreference.put(
                        context = context,
                        scope = scope,
                        value = it,
                    )
                    openTopBarTonalElevationDialog = false
                }
            )
        }
        if (openArticleListTonalElevationDialog) {
            TonalElevationDialog(
                onDismissRequest = { openArticleListTonalElevationDialog = false },
                initValue = LocalArticleListTonalElevation.current,
                defaultValue = { ArticleListTonalElevationPreference.default },
                onConfirm = {
                    ArticleListTonalElevationPreference.put(
                        context = context,
                        scope = scope,
                        value = it,
                    )
                    openArticleListTonalElevationDialog = false
                }
            )
        }
        if (openArticleItemTonalElevationDialog) {
            TonalElevationDialog(
                onDismissRequest = { openArticleItemTonalElevationDialog = false },
                initValue = LocalArticleItemTonalElevation.current,
                defaultValue = { ArticleItemTonalElevationPreference.default },
                onConfirm = {
                    ArticleItemTonalElevationPreference.put(
                        context = context,
                        scope = scope,
                        value = it,
                    )
                    openArticleItemTonalElevationDialog = false
                }
            )
        }
        if (openArticleItemMinWidthDialog) {
            ItemMinWidthDialog(
                onDismissRequest = { openArticleItemMinWidthDialog = false },
                initValue = LocalArticleItemMinWidth.current,
                defaultValue = { ArticleItemMinWidthPreference.default },
                onConfirm = {
                    ArticleItemMinWidthPreference.put(
                        context = context,
                        scope = scope,
                        value = it,
                    )
                    openArticleItemMinWidthDialog = false
                }
            )
        }
    }
}


@Composable
internal fun ItemMinWidthDialog(
    onDismissRequest: () -> Unit,
    initValue: Float,
    defaultValue: () -> Float,
    onConfirm: (Float) -> Unit,
) {
    var value by rememberSaveable { mutableFloatStateOf(initValue) }

    SliderDialog(
        onDismissRequest = onDismissRequest,
        value = value,
        onValueChange = { value = it },
        valueRange = 200f..600f,
        valueLabel = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .animateContentSize(),
                    text = "$value dp",
                    style = MaterialTheme.typography.titleMedium,
                )
                AniVuIconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = { value = defaultValue() },
                    imageVector = Icons.Outlined.Restore,
                )
            }
        },
        icon = { Icon(imageVector = Icons.Outlined.WidthNormal, contentDescription = null) },
        title = { Text(text = stringResource(id = R.string.min_width_dp)) },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    )
}