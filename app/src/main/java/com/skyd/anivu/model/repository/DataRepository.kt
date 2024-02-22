package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.deleteRecursivelyExclude
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DataRepository @Inject constructor() : BaseRepository() {
    fun requestClearCache(): Flow<Long> {
        return flow {
            var size: Long = 0
            Const.TEMP_TORRENT_DIR.deleteRecursivelyExclude(hook = {
                if (!it.canWrite()) return@deleteRecursivelyExclude false
                if (it.isFile) size += it.length()
                true
            })
            emit(size)
        }.flowOn(Dispatchers.IO)
    }
}