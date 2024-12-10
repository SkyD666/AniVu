package com.skyd.anivu.ui.mpv.service

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.skyd.anivu.ui.mpv.MPVPlayer

class PlayerNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val application = context.applicationContext as Application

        when (intent.action) {
            PLAY_ACTION -> MPVPlayer.getInstance(application).cyclePause()
            CLOSE_ACTION -> {
                MPVPlayer.getInstance(application).destroy()
                context.stopService(Intent(context, PlayerService::class.java))
                context.sendBroadcast(Intent(FINISH_PLAY_ACTIVITY_ACTION))
            }
        }
    }

    companion object {
        const val PLAY_ACTION = "play"
        const val CLOSE_ACTION = "close"
        const val FINISH_PLAY_ACTIVITY_ACTION = "finishPlayActivity"

        fun createIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(action).apply {
                component = ComponentName(context, PlayerNotificationReceiver::class.java)
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
    }
}