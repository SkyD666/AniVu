package com.skyd.anivu.ui.screen.media.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.anivu.base.mvi.MviEventListener
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.plus
import com.skyd.anivu.ext.toUri
import com.skyd.anivu.model.bean.MediaGroupBean
import com.skyd.anivu.model.bean.VideoBean
import com.skyd.anivu.ui.activity.PlayActivity
import com.skyd.anivu.ui.component.CircularProgressPlaceholder
import com.skyd.anivu.ui.component.EmptyPlaceholder
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.screen.media.CreateGroupDialog
import com.skyd.anivu.ui.screen.media.sub.openSubMediaScreen

class GroupInfo(
    val group: MediaGroupBean,
    val version: Long,   // For update, if version changed, MediaList will refresh media list
    val onCreateGroup: (MediaGroupBean) -> Unit,
    val onMoveFileToGroup: (VideoBean, MediaGroupBean) -> Unit,
)

@Composable
internal fun MediaList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    fabPadding: PaddingValues = PaddingValues(),
    path: String,
    groupInfo: GroupInfo? = null,
    viewModel: MediaListViewModel = hiltViewModel(key = path + groupInfo?.group)
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val navController = LocalNavController.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val uiState by viewModel.viewState.collectAsStateWithLifecycle()
        val dispatch = viewModel.getDispatcher(
            path,
            groupInfo?.version,
            startWith = MediaListIntent.Init(
                path = path,
                group = groupInfo?.group,
                version = groupInfo?.version,
            )
        )
        val state = rememberPullToRefreshState()
        Box(
            modifier = Modifier.pullToRefresh(
                isRefreshing = uiState.listState.loading,
                onRefresh = {
                    dispatch(MediaListIntent.Refresh(path = path, group = groupInfo?.group))
                },
                state = state
            )
        ) {
            when (val listState = uiState.listState) {
                is ListState.Failed -> Unit
                is ListState.Init -> CircularProgressPlaceholder(contentPadding = innerPadding + contentPadding)

                is ListState.Success -> {
                    if (listState.list.isEmpty()) {
                        EmptyPlaceholder(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            contentPadding = innerPadding + contentPadding
                        )
                    } else {
                        MediaList(
                            modifier = modifier,
                            list = listState.list,
                            groups = uiState.groups,
                            groupInfo = groupInfo,
                            onPlay = {
                                PlayActivity.play(
                                    context.activity,
                                    it.file.toUri(context)
                                )
                            },
                            onOpenDir = {
                                openSubMediaScreen(
                                    navController = navController,
                                    path = it.file.path
                                )
                            },
                            onRemove = { dispatch(MediaListIntent.DeleteFile(it.file)) },
                            contentPadding = innerPadding + contentPadding + fabPadding,
                        )
                    }
                }
            }

            Indicator(
                modifier = Modifier
                    .padding(contentPadding + fabPadding)
                    .align(Alignment.TopCenter),
                isRefreshing = uiState.listState.loading,
                state = state
            )
        }

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is MediaListEvent.DeleteFileResultEvent.Failed ->
                    snackbarHostState.showSnackbar(event.msg)

                is MediaListEvent.MediaListResultEvent.Failed ->
                    snackbarHostState.showSnackbar(event.msg)
            }
        }
    }
}

@Composable
private fun MediaList(
    modifier: Modifier = Modifier,
    list: List<VideoBean>,
    groups: List<MediaGroupBean>,
    groupInfo: GroupInfo?,
    onPlay: (VideoBean) -> Unit,
    onOpenDir: (VideoBean) -> Unit,
    onRemove: (VideoBean) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var openEditMediaDialog by rememberSaveable {
        mutableStateOf<VideoBean?>(null, policy = referentialEqualityPolicy())
    }
    var openCreateGroupDialog by rememberSaveable { mutableStateOf(false) }
    var createGroupDialogGroup by rememberSaveable { mutableStateOf("") }

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding + PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        columns = GridCells.Fixed(1),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(list) { item ->
            Media1Item(
                data = item,
                onPlay = onPlay,
                onOpenDir = onOpenDir,
                onRemove = onRemove,
                onLongClick = if (groupInfo == null) null else {
                    { openEditMediaDialog = it }
                },
            )
        }
    }
    if (openEditMediaDialog != null) {
        val videoBean = openEditMediaDialog!!
        EditMediaSheet(
            onDismissRequest = { openEditMediaDialog = null },
            file = videoBean.file,
            currentGroup = groupInfo!!.group,
            groups = groups,
            onDelete = {
                onRemove(videoBean)
                openEditMediaDialog = null
            },
            onGroupChange = {
                groupInfo.onMoveFileToGroup(videoBean, it)
                openEditMediaDialog = null
            },
            openCreateGroupDialog = {
                openCreateGroupDialog = true
                createGroupDialogGroup = ""
            },
        )
    }

    CreateGroupDialog(
        visible = openCreateGroupDialog,
        value = createGroupDialogGroup,
        onValueChange = { text -> createGroupDialogGroup = text },
        onCreateGroup = {
            groupInfo?.onCreateGroup?.invoke(it)
            openCreateGroupDialog = false
            createGroupDialogGroup = ""
        },
        onDismissRequest = {
            openCreateGroupDialog = false
            createGroupDialogGroup = ""
        }
    )
}