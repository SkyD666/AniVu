package com.skyd.anivu.ui.fragment.filepicker

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.showSnackbarWithLaunchedEffect
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.local.LocalNavController
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.Serializable


@AndroidEntryPoint
class FilePickerFragment : BaseComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase {
        val callback = arguments?.getSerializable(CALLBACK_KEY) as? FilePickerCallback
        val path = arguments?.getString(PATH_KEY)
        val pickFolder = arguments?.getBoolean(PICK_FOLDER_KEY)
        val extensionName = arguments?.getString(EXTENSION_NAME_KEY)
        if (callback == null || path == null || pickFolder == null || extensionName == null) {
            findMainNavController().popBackStackWithLifecycle()
        } else {
            FilePickerScreen(
                path = path,
                pickFolder = pickFolder,
                extensionName = extensionName,
                onFilePicked = { callback.onFilePicked(it) }
            )
        }
    }
}

const val PATH_KEY = "path"
const val CALLBACK_KEY = "callback"
const val PICK_FOLDER_KEY = "pickFolder"
const val EXTENSION_NAME_KEY = "extensionName"


fun interface FilePickerCallback : Serializable {
    fun onFilePicked(file: File)
}

@Composable
fun FilePickerScreen(
    path: String,
    pickFolder: Boolean = false,
    extensionName: String? = null,
    onFilePicked: (File) -> Unit,
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
            uiState.path != Environment.getExternalStorageDirectory().absolutePath &&
            uiState.path.startsWith(Environment.getExternalStorageDirectory().absolutePath)
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
                        onClick = { dispatch(FilePickerIntent.NewLocation(Environment.getExternalStorageDirectory().absolutePath)) },
                        imageVector = Icons.Outlined.PhoneAndroid,
                        contentDescription = stringResource(id = R.string.file_picker_screen_internal_storage),
                    )
                }
            )
        }
    ) { paddingValues ->
        Column {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                    end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                    top = paddingValues.calculateTopPadding(),
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
                                        onFilePicked(file)
                                    }
                                }
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = if (file.isDirectory) Icons.Outlined.Folder
                                    else Icons.AutoMirrored.Outlined.InsertDriveFile,
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
                        onFilePicked(File(uiState.path))
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
