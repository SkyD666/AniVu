package com.skyd.anivu.ui.activity.intenthandler

import android.content.Intent
import com.skyd.downloader.notification.NotificationConst

class OpenDownloadIntentHandler(private val onHandle: () -> Unit) : IntentHandler {
    override fun handle(intent: Intent) {
        if (intent.extras?.containsKey(NotificationConst.KEY_DOWNLOAD_REQUEST_ID) == true) {
            onHandle()
        }
    }
}