package com.skyd.anivu.model.worker.rsssync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skyd.anivu.model.db.dao.FeedDao
import com.skyd.anivu.model.repository.ArticleRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class RssSyncWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        val feedDao: FeedDao
        val articleRepo: ArticleRepository
    }

    private val hiltEntryPoint = EntryPointAccessors.fromApplication(
        context, WorkerEntryPoint::class.java
    )

    override suspend fun doWork(): Result {
        coroutineScope {
            runCatching {
                val requests = mutableListOf<Deferred<Unit>>()
                hiltEntryPoint.feedDao.getAllFeedUrl().forEach {
                    requests += async {
                        hiltEntryPoint.articleRepo.refreshArticleList(it)
                            .catch { it.printStackTrace() }.collect()
                    }
                }
                requests.forEach { it.await() }
            }
        }
        return Result.success()
    }

    companion object {
        const val uniqueWorkName = "rssSyncWorker"
    }
}