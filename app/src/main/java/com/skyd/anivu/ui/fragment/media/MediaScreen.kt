package com.skyd.anivu.ui.fragment.media

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.anivu.R
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.isCompact
import com.skyd.anivu.ext.showSnackbarWithLaunchedEffect
import com.skyd.anivu.model.bean.MediaGroupBean
import com.skyd.anivu.model.preference.data.medialib.MediaLibLocationPreference
import com.skyd.anivu.ui.activity.PlayActivity
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.dialog.TextFieldDialog
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import com.skyd.anivu.ui.fragment.filepicker.ListenToFilePicker
import com.skyd.anivu.ui.fragment.filepicker.navigateToFilePicker
import com.skyd.anivu.ui.fragment.media.list.GroupInfo
import com.skyd.anivu.ui.fragment.media.list.MediaList
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.local.LocalWindowSizeClass
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.min

const val MEDIA_SCREEN_ROUTE = "mediaScreen"

@Composable
fun MediaScreen(path: String, viewModel: MediaViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = LocalNavController.current
    val context = LocalContext.current
    val windowSizeClass = LocalWindowSizeClass.current
    val scope = rememberCoroutineScope()

    var fabHeight by remember { mutableStateOf(0.dp) }
    var fabWidth by remember { mutableStateOf(0.dp) }

    val dispatch = viewModel.getDispatcher(key1 = path, startWith = MediaIntent.Init(path = path))
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)

    val pagerState = rememberPagerState(pageCount = { uiState.groups.size })
    var openEditGroupDialog by rememberSaveable { mutableStateOf<MediaGroupBean?>(value = null) }

    ListenToFilePicker { result ->
        if (result.pickFolder) {
            MediaLibLocationPreference.put(context, this, result.result)
        } else {
            PlayActivity.play(
                context.activity,
                File(result.result).toUri(),
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.Small,
                title = { Text(text = stringResource(R.string.media_screen_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    navigationIconContentColor = TopAppBarDefaults.topAppBarColors().actionIconContentColor
                ),
                navigationIcon = {},
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets.safeDrawing.run {
                    var sides = WindowInsetsSides.Top + WindowInsetsSides.Right
                    if (windowSizeClass.isCompact) sides += WindowInsetsSides.Left
                    only(sides)
                },
                actions = {
                    AniVuIconButton(
                        onClick = {
                            navigateToFilePicker(navController = navController, path = path)
                        },
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = stringResource(id = R.string.data_screen_media_lib_location),
                    )
                    AniVuIconButton(
                        onClick = { dispatch(MediaIntent.Refresh(path)) },
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = stringResource(id = R.string.refresh),
                    )
                    AniVuIconButton(
                        onClick = { navController.navigate(R.id.action_to_download_fragment) },
                        imageVector = Icons.Outlined.Download,
                        contentDescription = stringResource(R.string.download_fragment_name),
                    )
                }
            )
        },
        floatingActionButton = {
            val density = LocalDensity.current
            Column(
                modifier = Modifier.onSizeChanged {
                    with(density) {
                        fabWidth = it.width.toDp() + 16.dp
                        fabHeight = it.height.toDp() + 16.dp
                    }
                },
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        navigateToFilePicker(
                            navController = navController,
                            path = path,
                            pickFolder = false,
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileOpen,
                        contentDescription = stringResource(id = R.string.open_file),
                    )
                }
                AniVuFloatingActionButton(
                    onClick = {
                        openEditGroupDialog = uiState.groups[pagerState.currentPage].first
                    },
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing.run {
            val leftPadding = windowSizeClass.isCompact
            val bottomPadding = !windowSizeClass.isCompact
            var sides = WindowInsetsSides.Top + WindowInsetsSides.Right
            if (leftPadding) sides += WindowInsetsSides.Left
            if (bottomPadding) sides += WindowInsetsSides.Bottom
            only(sides)
        },
    ) { innerPadding ->
        var openCreateGroupDialog by rememberSaveable { mutableStateOf(false) }
        var createGroupDialogGroup by rememberSaveable { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState.groups.isNotEmpty()) {
                PrimaryScrollableTabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = min(uiState.groups.size - 1, pagerState.currentPage),
                    edgePadding = 0.dp,
                    divider = {},
                ) {
                    uiState.groups.forEachIndexed { index, group ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = {
                                Text(
                                    modifier = Modifier
                                        .widthIn(max = 220.dp)
                                        .basicMarquee(),
                                    text = group.first.name,
                                    maxLines = 1,
                                )
                            },
                        )
                    }
                }
                HorizontalDivider()

                HorizontalPager(state = pagerState) { index ->
                    MediaList(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        contentPadding = PaddingValues(bottom = fabHeight),
                        path = path,
                        groupInfo = GroupInfo(
                            group = uiState.groups[index].first,
                            version = uiState.groups[index].second,
                            onCreateGroup = { dispatch(MediaIntent.CreateGroup(path, it)) },
                            onMoveFileToGroup = { video, newGroup ->
                                dispatch(MediaIntent.ChangeMediaGroup(path, video, newGroup))
                            },
                        ),
                    )
                }
            }
        }

        if (openEditGroupDialog != null) {
            EditMediaGroupSheet(
                onDismissRequest = { openEditGroupDialog = null },
                group = openEditGroupDialog!!,
                groups = remember(uiState.groups) { uiState.groups.map { it.first } },
                onDelete = {
                    dispatch(MediaIntent.DeleteGroup(path, it))
                    openEditGroupDialog = null
                },
                onNameChange = {
                    dispatch(MediaIntent.RenameGroup(path, openEditGroupDialog!!, it))
                },
                onMoveTo = {
                    dispatch(MediaIntent.MoveFilesToGroup(path, openEditGroupDialog!!, it))
                },
                openCreateGroupDialog = { openCreateGroupDialog = true },
            )
        }

        CreateGroupDialog(
            visible = openCreateGroupDialog,
            value = createGroupDialogGroup,
            onValueChange = { text -> createGroupDialogGroup = text },
            onCreateGroup = {
                dispatch(MediaIntent.CreateGroup(path, it))
                openCreateGroupDialog = false
                createGroupDialogGroup = ""
            },
            onDismissRequest = {
                openCreateGroupDialog = false
                createGroupDialogGroup = ""
            }
        )

        when (val event = uiEvent) {
            is MediaEvent.CreateGroupResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = event.msg, key1 = event
                )

            is MediaEvent.DeleteGroupResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = event.msg,
                    key1 = event
                )

            is MediaEvent.EditGroupResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = event.msg, key1 = event
                )

            is MediaEvent.MoveFilesToGroupResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = event.msg, key1 = event
                )

            is MediaEvent.ChangeFileGroupResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = event.msg, key1 = event
                )

            is MediaEvent.EditGroupResultEvent.Success -> LaunchedEffect(event) {
                dispatch(MediaIntent.Refresh(path = path))
                if (openEditGroupDialog != null) openEditGroupDialog = event.group
            }

            is MediaEvent.CreateGroupResultEvent.Success,
            is MediaEvent.DeleteGroupResultEvent.Success,
            is MediaEvent.ChangeFileGroupResultEvent.Success,
            is MediaEvent.MoveFilesToGroupResultEvent.Success -> LaunchedEffect(event) {
                dispatch(MediaIntent.Refresh(path = path))
            }

            null -> Unit
        }
    }

    WaitingDialog(visible = uiState.loadingDialog)
}

@Composable
internal fun CreateGroupDialog(
    visible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onCreateGroup: (MediaGroupBean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    TextFieldDialog(
        visible = visible,
        icon = { Icon(imageVector = Icons.Outlined.Workspaces, contentDescription = null) },
        titleText = stringResource(id = R.string.media_screen_add_group),
        placeholder = stringResource(id = R.string.media_group),
        maxLines = 1,
        value = value,
        onValueChange = onValueChange,
        onConfirm = { text -> onCreateGroup(MediaGroupBean(name = text)) },
        onDismissRequest = onDismissRequest,
    )
}