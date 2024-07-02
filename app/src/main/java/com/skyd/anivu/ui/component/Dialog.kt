package com.skyd.anivu.ui.component

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skyd.anivu.R


fun Fragment.showMessageDialog(
    title: CharSequence = getString(R.string.warning),
    message: CharSequence? = null,
    @DrawableRes icon: Int = 0,
    cancelable: Boolean = true,
    negativeText: String = resources.getString(R.string.cancel),
    positiveText: String = resources.getString(R.string.ok),
    onCancel: ((dialog: DialogInterface) -> Unit)? = null,
    onNegative: ((dialog: DialogInterface, which: Int) -> Unit)? = { dialog, _ -> dialog.dismiss() },
    onPositive: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
): AlertDialog? =
    requireActivity().showMessageDialog(
        onPositive = onPositive,
        onNegative = onNegative,
        message = message,
        icon = icon,
        title = title,
        cancelable = cancelable,
        onCancel = onCancel,
        positiveText = positiveText,
        negativeText = negativeText
    )

fun Activity.showMessageDialog(
    title: CharSequence = getString(R.string.warning),
    message: CharSequence? = null,
    @DrawableRes icon: Int = 0,
    cancelable: Boolean = true,
    negativeText: String = getString(R.string.cancel),
    positiveText: String = getString(R.string.ok),
    onCancel: ((dialog: DialogInterface) -> Unit)? = null,
    onNegative: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
    onPositive: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
): AlertDialog? {
    return MaterialAlertDialogBuilder(
        this,
//        R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
    )
        .setTitle(title)
        .setMessage(message)
        .apply { onPositive?.let { setPositiveButton(positiveText, it) } }
        .apply { onNegative?.let { setNegativeButton(negativeText, it) } }
        .setCancelable(cancelable)
        .setIcon(icon)
        .setOnCancelListener { onCancel?.invoke(it) }
        .run {
            if (!isFinishing) show() else null
        }
}

fun Activity.showListDialog(
    title: CharSequence? = null,
    items: List<CharSequence>? = null,
    checkedItem: Int = -1,
    onItemClickListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
    icon: Drawable? = null,
    cancelable: Boolean = true,
    negativeText: String = getString(R.string.cancel),
    neutralText: String? = null,
    positiveText: String = getString(R.string.ok),
    onNegative: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
    onNeutral: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
    onPositive: ((dialog: DialogInterface, which: Int, itemIndex: Int) -> Unit)? = null,
): AlertDialog? {
    var itemIndex = checkedItem
    var positionButton: Button? = null
    return MaterialAlertDialogBuilder(
        this,
//        R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
    )
        .setTitle(title)
        .setSingleChoiceItems(items?.toTypedArray(), checkedItem) { dialog, which ->
            itemIndex = which
            positionButton?.isEnabled = which != -1
            onItemClickListener?.invoke(dialog, which)
        }
        .apply {
            onPositive?.let {
                setPositiveButton(positiveText) { dialog, which ->
                    onPositive.invoke(dialog, which, itemIndex)
                }
            }
        }
        .apply { onNegative?.let { setNegativeButton(negativeText, it) } }
        .apply {
            if (onNeutral != null && neutralText != null) {
                setNeutralButton(neutralText, onNeutral)
            }
        }
        .setCancelable(cancelable)
        .setIcon(icon)
        .run {
            if (!isFinishing) {
                show().apply {
                    positionButton = getButton(AlertDialog.BUTTON_POSITIVE)
                    positionButton?.isEnabled = itemIndex != -1
                }
            } else null
        }
}

private var waitingDialog: AlertDialog? = null
fun dismissWaitingDialog() {
    waitingDialog?.dismiss()
}

fun Activity.showWaitingDialog(
    message: CharSequence? = null,
    cancelable: Boolean = true,
    negativeText: String = getString(R.string.cancel),
    positiveText: String = getString(R.string.ok),
    onNegative: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
    onPositive: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
): AlertDialog? {
    waitingDialog = MaterialAlertDialogBuilder(this)
        .setMessage(message)
        .setView(ProgressBar(this))
        .apply { onPositive?.let { setPositiveButton(positiveText, it) } }
        .apply { onNegative?.let { setNegativeButton(negativeText, it) } }
        .setCancelable(cancelable)
        .setOnDismissListener { waitingDialog = null }
        .run { if (!isFinishing) show() else null }
    return waitingDialog
}

/**
 * 修复theme.xml里面设置windowTranslucentStatus=true时，Dialog无法上弹的问题
 */
private fun Window.fixWindowTranslucentStatus() {
    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
}

private fun autoShowKeyboard(context: Context, v: View) {
    v.post {
        v.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
    }
}