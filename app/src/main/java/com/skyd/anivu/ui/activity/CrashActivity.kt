package com.skyd.anivu.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skyd.anivu.R
import com.skyd.anivu.config.Const.GITHUB_NEW_ISSUE_URL
import com.skyd.anivu.ext.getAppVersionCode
import com.skyd.anivu.ext.getAppVersionName
import com.skyd.anivu.ext.openBrowser
import com.skyd.anivu.ext.sp
import com.skyd.anivu.ui.component.showToast
import kotlin.system.exitProcess


/**
 * CrashActivity, do not extends BaseActivity
 */
class CrashActivity : AppCompatActivity() {
    companion object {
        const val CRASH_INFO = "crashInfo"

        fun start(context: Context, crashInfo: String) {
            val intent = Intent(context, CrashActivity::class.java)
            intent.putExtra(CRASH_INFO, crashInfo)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private fun copyToClipboard(crashInfo: String?) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Exception trace stack", crashInfo))
    }

    private fun exitApp() {
        finish()
        Process.killProcess(Process.myPid())
        exitProcess(1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val crashInfo = intent.getStringExtra(CRASH_INFO)

        val message = buildString {
            append("VersionName: ").append(getAppVersionName()).append("\n")
            append("VersionCode: ").append(getAppVersionCode()).append("\n")
            append("Brand: ").append(Build.BRAND).append("\n")
            append("Model: ").append(Build.MODEL).append("\n")
            append("SDK Version: ").append(Build.VERSION.SDK_INT).append("\n")
            append("ABI: ").append(Build.SUPPORTED_ABIS.firstOrNull().orEmpty()).append("\n\n")
            append("Crash Info: \n")
            append(crashInfo)
        }
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_error_24)
            .setTitle(getString(R.string.crashed))
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.submit_an_issue_on_github)) { _, _ ->
                copyToClipboard(message)
                Uri.parse(GITHUB_NEW_ISSUE_URL).openBrowser(this)
                exitApp()
            }
            .setNegativeButton(getString(R.string.close)) { _, _ ->
                exitApp()
            }
            .setNeutralButton(getString(android.R.string.copy)) { _, _ ->
                copyToClipboard(message)
                getString(R.string.copied).showToast()
                exitApp()
            }
            .show()
            .apply {
                window?.decorView?.findViewById<TextView>(android.R.id.message)?.apply {
                    setTextIsSelectable(true)
                    textSize = 3.5f.sp
                }
            }
        setFinishOnTouchOutside(false)
    }
}