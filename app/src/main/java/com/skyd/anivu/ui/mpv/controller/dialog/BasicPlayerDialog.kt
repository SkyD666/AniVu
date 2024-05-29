package com.skyd.anivu.ui.mpv.controller.dialog

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import com.skyd.anivu.ui.component.rememberSystemUiController

@Composable
internal fun BasicPlayerDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        rememberSystemUiController().apply {
            isSystemBarsVisible = false
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        Surface(
            modifier = Modifier,
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            content = content,
        )
    }
}