package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.ArticleWithEnclosureBean
import com.skyd.anivu.model.db.dao.ArticleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ReadRepository @Inject constructor(
    private val articleDao: ArticleDao,
) : BaseRepository() {
    fun requestArticleWithEnclosure(articleId: String): Flow<ArticleWithEnclosureBean?> {
        return articleDao.getArticleWithEnclosures(articleId = articleId)
            .filterNotNull()
            .flowOn(Dispatchers.IO)
    }
}