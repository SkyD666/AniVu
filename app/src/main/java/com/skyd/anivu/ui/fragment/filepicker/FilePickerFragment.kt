package com.skyd.anivu.ui.fragment.filepicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.getMimeType
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.showSnackbarWithLaunchedEffect
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.util.fileicon.getFileIcon
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class FilePickerFragment : BaseComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase {
        val path = arguments?.getString(PATH_KEY)
        val pickFolder = arguments?.getBoolean(PICK_FOLDER_KEY)
        val extensionName = arguments?.getString(EXTENSION_NAME_KEY)
        if (path == null || pickFolder == null || extensionName == null) {
            findMainNavController().popBackStackWithLifecycle()
        } else {
            FilePickerScreen(
                path = path,
                pickFolder = pickFolder,
                extensionName = extensionName,
            )
        }
    }
}

const val PATH_KEY = "path"
const val PICK_FOLDER_KEY = "pickFolder"
const val EXTENSION_NAME_KEY = "extensionName"
const val FILE_PICKER_NEW_PATH_KEY = "newPath"

@Composable
fun FilePickerScreen(
    path: String,
    pickFolder: Boolean = false,
    extensionName: String? = null,
    viewModel: FilePickerViewModel = hiltViewModel(),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = LocalNavController.current
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)
    val dispatch = viewModel.getDispatcher(
        startWith = FilePickerIntent.NewLocation(
            path = path,
            extensionName = extensionName,
        )
    )

    BackHandler {
        val current = File(uiState.path)
        val parent = current.parent
        if (!parent.isNullOrBlank() &&
            uiState.path != Const.INTERNAL_STORAGE &&
            uiState.path.startsWith(Const.INTERNAL_STORAGE)
        ) {
            dispatch(FilePickerIntent.NewLocation(parent))
        } else {
            navController.popBackStackWithLifecycle()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.Small,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.file_picker_screen_name)) },
                navigationIcon = {
                    AniVuIconButton(
                        onClick = { navController.popBackStackWithLifecycle() },
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(id = R.string.close),
                    )
                },
                actions = {
                    AniVuIconButton(
                        onClick = { dispatch(FilePickerIntent.NewLocation(Const.INTERNAL_STORAGE)) },
                        imageVector = Icons.Outlined.PhoneAndroid,
                        contentDescription = stringResource(id = R.string.file_picker_screen_internal_storage),
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            PathLevelIndication(
                path = uiState.path,
                onRouteTo = { dispatch(FilePickerIntent.NewLocation(it)) },
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                    end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                ),
            ) {
                (uiState.fileListState as? FileListState.Success)?.list?.forEach { file ->
                    item {
                        ListItem(
                            modifier = Modifier.clickable {
                                if (file.isDirectory) {
                                    dispatch(FilePickerIntent.NewLocation(file.path))
                                } else {
                                    if (!pickFolder) {
                                        navController.previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set(FILE_PICKER_NEW_PATH_KEY, file.absolutePath)
                                    }
                                }
                            },
                            leadingContent = {
                                Icon(
                                    painter = painterResource(
                                        id = remember(file) {
                                            getFileIcon(file.getMimeType() ?: "*/*").resourceId
                                        }
                                    ),
                                    contentDescription = null,
                                )
                            },
                            headlineContent = { Text(text = file.name) },
                        )
                    }
                }
            }

            if (pickFolder) {
                Button(
                    modifier = Modifier
                        .padding(
                            top = 12.dp,
                            bottom = 12.dp + paddingValues.calculateBottomPadding(),
                        )
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(FILE_PICKER_NEW_PATH_KEY, uiState.path)
                        navController.popBackStackWithLifecycle()
                    },
                ) {
                    Text(text = stringResource(id = R.string.file_picker_screen_pick))
                }
            }
        }

        when (val event = uiEvent) {
            is FilePickerEvent.FileListResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            null -> Unit
        }
    }
}

@Composable
private fun PathLevelIndication(path: String, onRouteTo: (String) -> Unit) {
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.canScrollForward) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val items = remember(path) {
            mutableListOf<String>().apply {
                var newPath = path
                if (newPath.startsWith(Const.INTERNAL_STORAGE)) {
                    add(Const.INTERNAL_STORAGE)
                    newPath = newPath.removePrefix(Const.INTERNAL_STORAGE)
                }
                newPath.removeSurrounding(File.separator)
                newPath.split(File.separator).forEach {
                    if (it.isNotBlank()) add(it)
                }
            }
        }

        items.forEachIndexed { index, item ->
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .clickable {
                        onRouteTo(
                            items
                                .subList(0, index + 1)
                                .joinToString(File.separator)
                        )
                    }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                text = item,
                style = MaterialTheme.typography.labelLarge,
            )
            if (index != items.size - 1) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                    contentDescription = null,
                )
            }
        }
    }
}
