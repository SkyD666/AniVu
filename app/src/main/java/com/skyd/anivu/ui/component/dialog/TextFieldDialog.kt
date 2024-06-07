package com.skyd.anivu.ui.component.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import com.skyd.anivu.R
import com.skyd.anivu.ui.component.AniVuTextFieldStyle
import com.skyd.anivu.ui.component.ClipboardTextField
import com.skyd.anivu.ui.component.DefaultTrailingIcon
import com.skyd.anivu.ui.local.LocalTextFieldStyle

@Composable
fun TextFieldDialog(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    readOnly: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    style: AniVuTextFieldStyle = AniVuTextFieldStyle.toEnum(LocalTextFieldStyle.current),
    icon: @Composable (() -> Unit)? = null,
    titleText: String? = null,
    value: String = "",
    placeholder: String = "",
    trailingIcon: @Composable (() -> Unit)? = DefaultTrailingIcon,
    isPassword: Boolean = false,
    errorText: String = "",
    dismissText: String = stringResource(R.string.cancel),
    confirmText: String = stringResource(R.string.ok),
    enableConfirm: (String) -> Boolean = { it.isNotBlank() },
    onValueChange: (String) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
    imeAction: ImeAction = if (maxLines == 1) ImeAction.Done else ImeAction.Default,
) {
    TextFieldDialog(
        modifier = modifier,
        visible = visible,
        readOnly = readOnly,
        maxLines = maxLines,
        style = style,
        icon = icon,
        title = if (titleText == null) null else {
            { Text(text = titleText, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        },
        value = value,
        placeholder = placeholder,
        trailingIcon = trailingIcon,
        isPassword = isPassword,
        errorText = errorText,
        dismissText = dismissText,
        confirmText = confirmText,
        enableConfirm = enableConfirm,
        onValueChange = onValueChange,
        onDismissRequest = onDismissRequest,
        onConfirm = onConfirm,
        imeAction = imeAction,
    )
}

@Composable
fun TextFieldDialog(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    readOnly: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    style: AniVuTextFieldStyle = AniVuTextFieldStyle.toEnum(LocalTextFieldStyle.current),
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    value: String = "",
    placeholder: String = "",
    trailingIcon: @Composable (() -> Unit)? = DefaultTrailingIcon,
    isPassword: Boolean = false,
    errorText: String = "",
    dismissText: String = stringResource(R.string.cancel),
    confirmText: String = stringResource(R.string.ok),
    enableConfirm: (String) -> Boolean = { it.isNotBlank() },
    onValueChange: (String) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
    imeAction: ImeAction = if (maxLines == 1) ImeAction.Done else ImeAction.Default,
) {
    val focusManager = LocalFocusManager.current

    AniVuDialog(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        icon = icon,
        title = title,
        text = {
            ClipboardTextField(
                modifier = modifier.fillMaxWidth(),
                readOnly = readOnly,
                value = value,
                maxLines = maxLines,
                style = style,
                onValueChange = onValueChange,
                placeholder = placeholder,
                trailingIcon = trailingIcon,
                isPassword = isPassword,
                errorText = errorText,
                imeAction = imeAction,
                focusManager = focusManager,
                onConfirm = onConfirm,
            )
        },
        confirmButton = {
            TextButton(
                enabled = enableConfirm(value),
                onClick = {
                    focusManager.clearFocus()
                    onConfirm(value)
                }
            ) {
                Text(
                    text = confirmText,
                    color = if (enableConfirm(value)) {
                        Color.Unspecified
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = dismissText)
            }
        },
    )
}
