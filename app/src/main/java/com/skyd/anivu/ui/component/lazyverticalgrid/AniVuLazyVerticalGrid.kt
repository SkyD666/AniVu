package com.skyd.anivu.ui.component.lazyverticalgrid

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.AniVuItemSpace.anivuItemSpace
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.MAX_SPAN_SIZE
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.anivuShowSpan

@Composable
fun AniVuLazyVerticalGrid(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    dataList: List<Any>,
    adapter: LazyGridAdapter,
    enableLandScape: Boolean = true,     // 是否启用横屏使用另一套布局方案
    key: ((index: Int, item: Any) -> Any)? = null
) {
    val context = LocalContext.current
    val listState = rememberLazyGridState()
    val spanIndexArray: MutableList<Int> = remember { mutableListOf() }
    val configuration = LocalConfiguration.current
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(MAX_SPAN_SIZE),
        state = listState,
        contentPadding = contentPadding
    ) {
        itemsIndexed(
            items = dataList,
            key = key,
            span = { index, item ->
                val spanIndex = maxLineSpan - maxCurrentLineSpan
                if (spanIndexArray.size > index) spanIndexArray[index] = spanIndex
                else spanIndexArray.add(spanIndex)
                GridItemSpan(
                    anivuShowSpan(
                        data = item,
                        enableLandScape = enableLandScape,
                        context = context,
                    )
                )
            }
        ) { index, item ->
            adapter.Draw(
                modifier = Modifier.anivuItemSpace(
                    item = item,
                    spanSize = anivuShowSpan(data = item, context = context),
                    spanIndex = spanIndexArray[index]
                ),
                index = index,
                data = item
            )
        }
    }
}