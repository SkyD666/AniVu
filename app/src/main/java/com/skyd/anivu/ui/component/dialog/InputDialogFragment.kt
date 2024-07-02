package com.skyd.anivu.ui.component.dialog

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.text.Editable
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.skyd.anivu.R
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.dp
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.showSoftKeyboard
import com.skyd.anivu.model.preference.appearance.TextFieldStylePreference
import com.skyd.anivu.ui.component.AniVuTextFieldStyle

@SuppressLint("InflateParams")
open class InputDialogBuilder(
    context: Context,
) : MaterialAlertDialogBuilder(context) {

    private val textField = TextInputLayout(
        context, null,
        if (context.dataStore.getOrDefault(TextFieldStylePreference) == AniVuTextFieldStyle.Normal.value) {
            com.google.android.material.R.attr.textInputFilledStyle
        } else {
            com.google.android.material.R.attr.textInputOutlinedStyle
        }
    ).apply {
        addView(TextInputEditText(this.context))
    }

    init {
        with(textField) {
            setPadding(20.dp, 20.dp, 20.dp, 0.dp)
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            setEndIconDrawable(R.drawable.ic_content_paste_24)
            setEndIconContentDescription(R.string.paste)
            setEndIconOnClickListener {
                val clipboard = getSystemService(context, ClipboardManager::class.java)
                val text = clipboard?.primaryClip?.let { primaryClip ->
                    if (primaryClip.itemCount > 0) {
                        // note: text may be null, ensure this is null-safe
                        primaryClip.getItemAt(0)?.coerceToText(context)
                    } else {
                        null
                    }
                }
                if (!text.isNullOrBlank()) {
                    editText?.setText(text)
                }
            }
        }
    }

    fun addTextChangedListener(
        beforeTextChanged: (
            text: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) -> Unit = { _, _, _, _ -> },
        onTextChanged: (
            text: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) -> Unit = { _, _, _, _ -> },
        afterTextChanged: (text: Editable?) -> Unit = {}
    ): InputDialogBuilder {
        textField.editText?.addTextChangedListener(
            beforeTextChanged,
            onTextChanged,
            afterTextChanged,
        )
        return this
    }

    fun setPositiveButton(
        textId: Int = R.string.ok,
        listener: (dialog: DialogInterface, which: Int, text: String) -> Unit,
    ): InputDialogBuilder {
        return super.setPositiveButton(textId) { dialog, which ->
            listener(dialog, which, textField.editText?.text.toString())
        } as InputDialogBuilder
    }

    fun setPositiveButton(
        text: CharSequence?,
        listener: (dialog: DialogInterface, which: Int, text: String) -> Unit,
    ): InputDialogBuilder {
        return super.setPositiveButton(text) { dialog, which ->
            listener(dialog, which, textField.editText?.text.toString())
        } as InputDialogBuilder
    }

    fun setInitInputText(text: String): InputDialogBuilder {
        textField.editText?.apply {
            setText(text)
            setSelection(text.length)
        }
        return this
    }

    fun setHint(text: String): InputDialogBuilder {
        textField.hint = text
        return this
    }

    override fun show(): AlertDialog {
        setView(textField)
        return super.show().apply {
            textField.editText?.post {
                textField.editText?.showSoftKeyboard(window ?: return@post)
            }
        }
    }
}