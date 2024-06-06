package com.skyd.anivu.ext

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun SnackbarHostState.showSnackbar(
    scope: CoroutineScope,
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = true,
    duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
) {
    scope.launch {
        showSnackbar(
            message = message,
            actionLabel = actionLabel,
            withDismissAction = withDismissAction,
            duration = duration,
        )
    }
}

@Composable
fun SnackbarHostState.showSnackbarWithLaunchedEffect(
    message: String,
    key1: Any? = null,
    key2: Any? = null,
    key3: Any? = this,
    actionLabel: String? = null,
    withDismissAction: Boolean = true,
    duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
): SnackbarHostState {
    LaunchedEffect(key1, key2, key3) {
        showSnackbar(
            message = message,
            actionLabel = actionLabel,
            withDismissAction = withDismissAction,
            duration = duration,
        )
    }
    return this
}