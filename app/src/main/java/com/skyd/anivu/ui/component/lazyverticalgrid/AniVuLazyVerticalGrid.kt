package com.skyd.anivu.ui.component.lazyverticalgrid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter

@Composable
fun AniVuLazyVerticalGrid(
    modifier: Modifier = Modifier,
    columns: GridCells,
    contentPadding: PaddingValues = PaddingValues(),
    dataList: Any,
    listState: LazyGridState = rememberLazyGridState(),
    adapter: LazyGridAdapter,
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    key: ((index: Int, item: Any) -> Any)? = null
) {
    val getter: (index: Int) -> Any = remember(dataList) {
        when (dataList) {
            is List<*> -> {
                { dataList[it]!! }
            }

            is LazyPagingItems<*> -> {
                { dataList[it]!! }
            }

            else -> error("dataList should be the type of List or LazyPagingItems")
        }
    }
    val count: Int = when (dataList) {
        is List<*> -> dataList.size
        is LazyPagingItems<*> -> dataList.itemCount
        else -> error("dataList should be the type of List or LazyPagingItems")
    }

    LazyVerticalGrid(
        modifier = modifier,
        columns = columns,
        state = listState,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
    ) {
        items(
            count = count,
            key = if (key == null) null else { index -> key.invoke(index, getter(index)) },
        ) { index ->
            adapter.Draw(
                index = index,
                data = getter(index)
            )
        }
    }
}