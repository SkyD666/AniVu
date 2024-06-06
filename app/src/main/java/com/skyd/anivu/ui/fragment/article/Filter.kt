package com.skyd.anivu.ui.fragment.article

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Drafts
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.Markunread
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R


@Composable
internal fun FilterRow(
    modifier: Modifier = Modifier,
    onFilterFavorite: (Boolean?) -> Unit,
    onFilterRead: (Boolean?) -> Unit,
) {
    var favoriteFilterValue by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var readFilterValue by rememberSaveable { mutableStateOf<Boolean?>(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AnimatedVisibility(
            visible = favoriteFilterValue != null || readFilterValue != null,
            enter = expandHorizontally(),
            exit = shrinkHorizontally(),
        ) {
            FilterSetting(
                filterCount = {
                    listOfNotNull(favoriteFilterValue, readFilterValue).size
                },
                onClearAllFilters = {
                    onFilterFavorite(null)
                    favoriteFilterValue = null
                    onFilterRead(null)
                    readFilterValue = null
                },
            )
        }
        FavoriteFilter(
            current = favoriteFilterValue,
            onFilterFavorite = {
                onFilterFavorite(it)
                favoriteFilterValue = it
            }
        )
        ReadFilter(
            current = readFilterValue,
            onFilterRead = {
                onFilterRead(it)
                readFilterValue = it
            },
        )
    }
}

@Composable
internal fun FilterSetting(
    filterCount: () -> Int,
    onClearAllFilters: () -> Unit,
) {
    var expandMenu by rememberSaveable { mutableStateOf(false) }
    Box {
        AssistChip(
            onClick = { expandMenu = !expandMenu },
            label = {
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(
                        modifier = Modifier.animateContentSize(),
                        text = filterCount().toString(),
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = stringResource(id = R.string.article_screen_filter_setting),
                )
            },
        )
        DropdownMenu(
            expanded = expandMenu,
            onDismissRequest = { expandMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.article_screen_filter_clear_all_filter)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.ClearAll,
                        contentDescription = null
                    )
                },
                onClick = {
                    onClearAllFilters()
                    expandMenu = false
                },
            )
        }
    }
}

@Composable
internal fun FavoriteFilter(
    current: Boolean?,
    onFilterFavorite: (Boolean?) -> Unit,
) {
    val context = LocalContext.current
    val items = remember {
        mapOf(
            null to Pair(
                context.getString(R.string.article_screen_filter_all),
                Icons.Outlined.FavoriteBorder,
            ),
            true to Pair(
                context.getString(R.string.article_screen_filter_favorite),
                Icons.Outlined.Favorite,
            ),
            false to Pair(
                context.getString(R.string.article_screen_filter_unfavorite),
                Icons.Outlined.FavoriteBorder,
            ),
        )
    }
    FavoriteReadFilter(
        current = current,
        items = items,
        onFilter = onFilterFavorite,
    )
}

@Composable
internal fun ReadFilter(
    current: Boolean?,
    onFilterRead: (Boolean?) -> Unit,
) {
    val context = LocalContext.current
    val items = remember {
        mapOf(
            null to Pair(
                context.getString(R.string.article_screen_filter_all),
                Icons.Outlined.Markunread,
            ),
            true to Pair(
                context.getString(R.string.article_screen_filter_read),
                Icons.Outlined.Drafts,
            ),
            false to Pair(
                context.getString(R.string.article_screen_filter_unread),
                Icons.Outlined.MarkEmailUnread,
            ),
        )
    }
    FavoriteReadFilter(
        current = current,
        items = items,
        onFilter = onFilterRead,
    )
}

@Composable
private fun FavoriteReadFilter(
    current: Boolean?,
    items: Map<Boolean?, Pair<String, ImageVector>>,
    onFilter: (Boolean?) -> Unit,
) {
    var expandMenu by rememberSaveable { mutableStateOf(false) }

    Box {
        FilterChip(
            onClick = { expandMenu = !expandMenu },
            label = {
                Text(
                    modifier = Modifier.animateContentSize(),
                    text = items[current]!!.first,
                )
            },
            selected = current != null,
            leadingIcon = {
                Icon(
                    imageVector = items[current]!!.second,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = if (expandMenu) Icons.Default.ArrowDropUp
                    else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        )
        DropdownMenu(
            expanded = expandMenu,
            onDismissRequest = { expandMenu = false },
        ) {
            items.forEach { (t, u) ->
                DropdownMenuItem(
                    text = { Text(text = u.first) },
                    leadingIcon = { Icon(imageVector = u.second, contentDescription = null) },
                    onClick = {
                        onFilter(t)
                        expandMenu = false
                    },
                )
            }
        }
    }
}