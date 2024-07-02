package com.skyd.anivu.ui.component.lazyverticalgrid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter

@Composable
fun AniVuLazyVerticalGrid(
    modifier: Modifier = Modifier,
    columns: GridCells,
    contentPadding: PaddingValues = PaddingValues(),
    count: () -> Int,
    data: (index: Int) -> Any,
    listState: LazyGridState = rememberLazyGridState(),
    adapter: LazyGridAdapter,
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    key: ((index: Int, item: Any) -> Any)? = null
) {
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
            count = count(),
            key = if (key == null) null else { index -> key.invoke(index, data(index)) },
        ) { index ->
            adapter.Draw(
                index = index,
                data = data(index)
            )
        }
    }
}

@Composable
fun AniVuLazyVerticalGrid(
    modifier: Modifier = Modifier,
    columns: GridCells,
    contentPadding: PaddingValues = PaddingValues(),
    dataList: LazyPagingItems<*>,
    listState: LazyGridState = rememberLazyGridState(),
    adapter: LazyGridAdapter,
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    key: ((index: Int, item: Any) -> Any)? = null
) {
    AniVuLazyVerticalGrid(
        modifier = modifier,
        columns = columns,
        listState = listState,
        contentPadding = contentPadding,
        count = { dataList.itemCount },
        data = { dataList[it]!! },
        adapter = adapter,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        key = key,
    )
}

@Composable
fun AniVuLazyVerticalGrid(
    modifier: Modifier = Modifier,
    columns: GridCells,
    contentPadding: PaddingValues = PaddingValues(),
    dataList: Collection<Any>,
    listState: LazyGridState = rememberLazyGridState(),
    adapter: LazyGridAdapter,
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    key: ((index: Int, item: Any) -> Any)? = null
) {
    AniVuLazyVerticalGrid(
        modifier = modifier,
        columns = columns,
        listState = listState,
        contentPadding = contentPadding,
        count = { dataList.size },
        data = { dataList.elementAt(it) },
        adapter = adapter,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        key = key,
    )
}