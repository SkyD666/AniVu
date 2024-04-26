package com.skyd.anivu.ui.component.dialog

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
    visible: Boolean = false,
    readOnly: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    style: AniVuTextFieldStyle = AniVuTextFieldStyle.toEnum(LocalTextFieldStyle.current),
    icon: @Composable (() -> Unit)? = null,
    title: String = "",
    value: String = "",
    placeholder: String = "",
    trailingIcon: @Composable (() -> Unit)? = DefaultTrailingIcon,
    isPassword: Boolean = false,
    errorText: String = "",
    dismissText: String = stringResource(R.string.cancel),
    confirmText: String = stringResource(R.string.ok),
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
        title = { Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        text = {
            ClipboardTextField(
                modifier = modifier,
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
                enabled = value.isNotBlank(),
                onClick = {
                    focusManager.clearFocus()
                    onConfirm(value)
                }
            ) {
                Text(
                    text = confirmText,
                    color = if (value.isNotBlank()) {
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
