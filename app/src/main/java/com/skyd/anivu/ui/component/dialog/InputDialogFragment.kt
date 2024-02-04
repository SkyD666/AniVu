package com.skyd.anivu.ui.component.dialog

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.skyd.anivu.R

@SuppressLint("InflateParams")
open class InputDialogBuilder(
    context: Context,
) : MaterialAlertDialogBuilder(context) {

    private val textField = LayoutInflater.from(context).inflate(
        R.layout.layout_input_dialog, null, false
    ) as TextInputLayout

    init {
        textField.setEndIconOnClickListener {
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
                textField.editText?.setText(text)
            }
        }
    }

    fun setPositiveButton(
        textId: Int = R.string.ok,
        listener: (dialog: DialogInterface, which: Int, text: String) -> Unit,
    ): MaterialAlertDialogBuilder {
        return super.setPositiveButton(textId) { dialog, which ->
            listener(dialog, which, textField.editText?.text.toString())
        }
    }

    override fun show(): AlertDialog {
        setView(textField)
        return super.show()
    }
}