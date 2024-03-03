package com.skyd.anivu.model.worker.rsssync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.model.preference.rss.RssSyncFrequencyPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

private val coroutineScope = CoroutineScope(Dispatchers.IO)

fun listenerRssSyncFrequency(context: Context) {
    coroutineScope.launch {
        context.dataStore.data.map {
            it[RssSyncFrequencyPreference.key] ?: RssSyncFrequencyPreference.default
        }.distinctUntilChanged().combine(
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow(RssSyncWorker.uniqueWorkName)
                .distinctUntilChanged()
        ) { rssSyncFrequency, workInfos ->
            val workInfo = workInfos.firstOrNull()
            if (rssSyncFrequency == RssSyncFrequencyPreference.MANUAL) {
                stopRssSyncWorker(context)
            } else {
                if (workInfo == null) {
                    startRssSyncWorker(context, rssSyncFrequency)
                } else if (workInfo.periodicityInfo?.repeatIntervalMillis != rssSyncFrequency) {
                    updateRssSyncWorker(context, rssSyncFrequency, workInfo.id)
                }
            }
        }.collect()
    }
}

fun updateRssSyncWorker(context: Context, rssSyncFrequency: Long, id: UUID) {
    WorkManager.getInstance(context).updateWork(
        getRssSyncWorkRequest(rssSyncFrequency, id)
    )
}

fun startRssSyncWorker(context: Context, rssSyncFrequency: Long) {
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        RssSyncWorker.uniqueWorkName,
        ExistingPeriodicWorkPolicy.KEEP,
        getRssSyncWorkRequest(rssSyncFrequency),
    )
}

fun stopRssSyncWorker(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(RssSyncWorker.uniqueWorkName)
}

fun getRssSyncWorkRequest(rssSyncFrequency: Long, id: UUID? = null): PeriodicWorkRequest {
    val builder = PeriodicWorkRequestBuilder<RssSyncWorker>(
        rssSyncFrequency, TimeUnit.MILLISECONDS
    )
    if (id != null) {
        builder.setId(id)
    }
    return builder.build()
}