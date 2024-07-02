package com.skyd.anivu.ui.component.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.skyd.anivu.R

@Composable
fun DeleteWarningDialog(
    visible: Boolean,
    title: String = stringResource(R.string.warning),
    text: String? = null,
    confirmText: String = stringResource(R.string.ok),
    dismissText: String = stringResource(R.string.cancel),
    onDismissRequest: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AniVuDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        icon = { Icon(imageVector = Icons.Outlined.Warning, contentDescription = null) },
        title = { Text(text = title) },
        text = if (text == null) null else {
            { Text(text = text) }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } },
    )
}