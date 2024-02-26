package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.deleteRecursivelyExclude
import com.skyd.anivu.model.db.dao.TorrentFileDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DataRepository @Inject constructor(
    private val torrentFileDao: TorrentFileDao,
) : BaseRepository() {
    fun requestClearCache(): Flow<Long> {
        return flow {
            var size: Long = 0
            Const.TEMP_TORRENT_DIR.deleteRecursivelyExclude(hook = {
                if (!it.canWrite()) return@deleteRecursivelyExclude false
                if (it.isFile) size += it.length()
                true
            })
            Const.DOWNLOADING_VIDEO_DIR.walkBottomUp().forEach {
                val relativePath = it.toRelativeString(Const.DOWNLOADING_VIDEO_DIR)
                if (relativePath.isNotBlank() && 0 == torrentFileDao.count(relativePath)) {
                    val s = it.length()
                    if (it.delete()) {
                        size += s
                    }
                }
            }
            emit(size)
        }.flowOn(Dispatchers.IO)
    }
}