package com.skyd.anivu.model.worker.deletearticle

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.db.dao.ArticleDao
import com.skyd.anivu.model.preference.data.autodelete.AutoDeleteArticleBeforePreference
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class DeleteArticleWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        val articleDao: ArticleDao
    }

    private val hiltEntryPoint = EntryPointAccessors.fromApplication(
        context, WorkerEntryPoint::class.java
    )

    override suspend fun doWork(): Result {
        runCatching {
            hiltEntryPoint.articleDao.deleteArticleBefore(
                System.currentTimeMillis() -
                        applicationContext.dataStore.getOrDefault(AutoDeleteArticleBeforePreference)
            )
        }.onFailure { return Result.failure() }
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "deleteArticleWorker"
    }
}