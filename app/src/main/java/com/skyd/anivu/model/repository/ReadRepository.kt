package com.skyd.anivu.model.repository

import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.skyd.anivu.appContext
import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.ext.imageLoaderBuilder
import com.skyd.anivu.ext.savePictureToMediaStore
import com.skyd.anivu.ext.validateFileName
import com.skyd.anivu.model.bean.ArticleWithEnclosureBean
import com.skyd.anivu.model.db.dao.ArticleDao
import com.skyd.anivu.util.image.ImageFormatChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.random.Random

class ReadRepository @Inject constructor(
    private val articleDao: ArticleDao,
) : BaseRepository() {
    fun requestArticleWithEnclosure(articleId: String): Flow<ArticleWithEnclosureBean?> {
        return articleDao.getArticleWithEnclosures(articleId = articleId)
            .filterNotNull()
            .flowOn(Dispatchers.IO)
    }

    fun downloadImage(url: String, title: String?): Flow<Unit> {
        return flow {
            val request = ImageRequest.Builder(appContext)
                .data(url)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            val imageLoader = appContext.imageLoaderBuilder().build()
            when (val result = imageLoader.execute(request)) {
                is ErrorResult -> throw result.throwable
                is SuccessResult -> {
                    imageLoader.diskCache!!.openSnapshot(url).use { snapshot ->
                        val imageFile = snapshot!!.data.toFile()
                        val format = imageFile.inputStream().use { ImageFormatChecker.check(it) }
                        imageFile.savePictureToMediaStore(
                            context = appContext,
                            mimetype = format.toMimeType(),
                            fileName = (title.orEmpty().ifEmpty {
                                url.substringAfterLast('/')
                            } + "_" + Random.nextInt()).validateFileName() + format.toString(),
                            autoDelete = false,
                        )
                    }
                }
            }
            emit(Unit)
        }.flowOn(Dispatchers.IO)
    }
}