package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.MediaPlayHistoryBean
import com.skyd.anivu.model.db.dao.MediaPlayHistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PlayerRepository @Inject constructor(
    private val mediaPlayHistoryDao: MediaPlayHistoryDao,
) : BaseRepository() {
    fun updatePlayHistory(bean: MediaPlayHistoryBean): Flow<Unit> {
        return flow {
            mediaPlayHistoryDao.updateMediaPlayHistory(bean)
            emit(Unit)
        }.flowOn(Dispatchers.IO)
    }

    fun requestLastPlayPosition(path: String): Flow<Long> {
        return flow {
            emit(mediaPlayHistoryDao.getMediaPlayHistory(path).lastPlayPosition)
        }.flowOn(Dispatchers.IO)
    }
}