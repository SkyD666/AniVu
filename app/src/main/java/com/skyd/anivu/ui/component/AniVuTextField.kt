package com.skyd.anivu.ui.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.skyd.anivu.R

@Composable
fun AniVuTextField(
    value: String,
    label: String = "",
    readOnly: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isPassword: Boolean = false,
    placeholder: String = "",
    errorMessage: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    val clipboardManager = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TextField(
        modifier = Modifier.focusRequester(focusRequester),
        maxLines = maxLines,
        enabled = !readOnly,
        value = value,
        label = if (label.isBlank()) null else {
            { Text(label) }
        },
        onValueChange = {
            if (!readOnly) onValueChange(it)
        },
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else visualTransformation,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        isError = errorMessage.isNotEmpty(),
        singleLine = maxLines == 1,
        trailingIcon = {
            if (value.isNotEmpty()) {
                AniVuIconButton(
                    imageVector = if (isPassword) {
                        if (showPassword) Icons.Rounded.Visibility
                        else Icons.Rounded.VisibilityOff
                    } else Icons.Rounded.Close,
                    contentDescription = if (isPassword) {
                        if (showPassword) stringResource(R.string.password_visibility_off)
                        else stringResource(R.string.password_visibility_on)
                    } else stringResource(R.string.clear_input_text),
                    onClick = {
                        if (isPassword) {
                            showPassword = !showPassword
                        } else if (!readOnly) {
                            onValueChange("")
                        }
                    }
                )
            } else {
                AniVuIconButton(
                    imageVector = Icons.Rounded.ContentPaste,
                    contentDescription = stringResource(R.string.paste),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { onValueChange(clipboardManager.getText()?.text.orEmpty()) }
                )
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}
