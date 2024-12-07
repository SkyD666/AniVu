package com.skyd.anivu.model.repository

import androidx.collection.LruCache
import androidx.compose.ui.util.fastFirstOrNull
import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.ext.validateFileName
import com.skyd.anivu.model.bean.MediaBean
import com.skyd.anivu.model.bean.MediaGroupBean
import com.skyd.anivu.model.bean.MediaGroupBean.Companion.isDefaultGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val json: Json,
) : BaseRepository() {

    companion object {
        private const val FOLDER_INFO_JSON_NAME = "info.json"
        private const val OLD_MEDIA_LIB_JSON_NAME = "group.json"
        private const val MEDIA_LIB_JSON_NAME = "MediaLib.json"

        private val mediaLibJsons = LruCache<String, MediaLibJson>(maxSize = 5)
    }

    private fun parseMediaLibJson(mediaLibRootJsonFile: File): MediaLibJson? {
        if (!mediaLibRootJsonFile.exists()) {
            File(mediaLibRootJsonFile.parentFile, OLD_MEDIA_LIB_JSON_NAME).apply {
                if (this.exists()) renameTo(mediaLibRootJsonFile)
            }
        }
        if (!mediaLibRootJsonFile.exists()) return null
        return mediaLibRootJsonFile.inputStream().use { inputStream ->
            json.decodeFromStream<MediaLibJson>(inputStream)
        }.apply {
            files.removeIf {
                !File(mediaLibRootJsonFile.parentFile, it.fileName).exists() ||
                        it.fileName.equals(FOLDER_INFO_JSON_NAME, true) ||
                        it.fileName.equals(MEDIA_LIB_JSON_NAME, true)
            }
        }
    }

    private fun getOrReadMediaLibJson(path: String): MediaLibJson {
        return mediaLibJsons[path] ?: run {
            val jsonFile = File(path, MEDIA_LIB_JSON_NAME)
            parseMediaLibJson(jsonFile)?.also {
                mediaLibJsons.put(path, it)
            } ?: MediaLibJson(files = mutableListOf()).apply { mediaLibJsons.put(path, this) }
        }
    }

    // Format groups
    private fun formatMediaLibJson(old: MediaLibJson): MediaLibJson {
        val allGroups = (old.files.map { it.groupName } + old.allGroups)
            .distinct().filterNotNull().toMutableList()
        return MediaLibJson(
            allGroups = allGroups,
            files = old.files,
        )
    }

    private fun writeMediaLibJson(path: String, data: MediaLibJson) {
        File(path, MEDIA_LIB_JSON_NAME).outputStream().use { outputStream ->
            json.encodeToStream(formatMediaLibJson(data), outputStream)
        }
    }

    private fun MutableList<FileJson>.appendFiles(
        files: List<File>, fileJsonBuild: (File) -> FileJson = {
            FileJson(
                fileName = it.name,
                groupName = null,
                isFile = it.isFile,
                displayName = null,
            )
        }
    ) = apply {
        files.forEach { file ->
            if (file.name.equals(FOLDER_INFO_JSON_NAME, true) ||
                file.name.equals(MEDIA_LIB_JSON_NAME, true)
            ) {
                return@forEach
            }
            if (firstOrNull { it.fileName == file.name } == null) {
                add(fileJsonBuild(file))
            }
        }
    }

    fun requestGroups(path: String): Flow<List<MediaGroupBean>> {
        return flow {
            val allGroups = getOrReadMediaLibJson(path).allGroups
            emit(listOf(MediaGroupBean.DefaultMediaGroup) +
                    allGroups.map { MediaGroupBean(name = it) }.sortedBy { it.name })
        }
    }

    fun requestFiles(path: String, group: MediaGroupBean?): Flow<List<MediaBean>> {
        return flow {
            val fileJsons = getOrReadMediaLibJson(path).files.appendFiles(
                File(path).listFiles().orEmpty().toMutableList().filter { it.exists() }
            )
            val videoList = (if (group == null) fileJsons else {
                val groupName = if (group.isDefaultGroup()) null else group.name
                fileJsons.filter { it.groupName == groupName }
            }).mapNotNull {
                val file = File(path, it.fileName)
                if (file.exists()) {
                    MediaBean(
                        displayName = it.displayName,
                        file = file,
                    )
                } else null
            }

            emit(
                videoList.toMutableList().apply {
                    fastFirstOrNull { it.name.equals(FOLDER_INFO_JSON_NAME, true) }
                        ?.let { remove(it) }
                    fastFirstOrNull { it.name.equals(MEDIA_LIB_JSON_NAME, true) }
                        ?.let { remove(it) }
                }
            )
        }
    }

    fun deleteFile(file: File): Flow<Boolean> {
        return flow {
            val path = file.parentFile!!.path
            val mediaLibJson = getOrReadMediaLibJson(path).apply {
                files.removeIf { it.fileName == file.name }
            }
            writeMediaLibJson(path = path, mediaLibJson)
            emit(file.deleteRecursively())
        }.flowOn(Dispatchers.IO)
    }

    fun renameFile(file: File, newName: String): Flow<File?> {
        return flow {
            val path = file.parentFile!!.path
            val mediaLibJson = getOrReadMediaLibJson(path)
            val newFile = File(file.parentFile, newName.validateFileName())
            if (file.renameTo(newFile)) {
                mediaLibJson.files.firstOrNull { it.fileName == file.name }?.fileName = newName
                writeMediaLibJson(path = path, mediaLibJson)
                emit(newFile)
            } else {
                emit(null)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun setFileDisplayName(mediaBean: MediaBean, displayName: String?): Flow<MediaBean> {
        return flow {
            val path = mediaBean.file.parentFile!!.path
            val mediaLibJson = getOrReadMediaLibJson(path = path)
            mediaLibJson.files.firstOrNull {
                it.fileName == mediaBean.file.name
            }?.displayName = if (displayName.isNullOrBlank()) null else displayName
            writeMediaLibJson(path = path, mediaLibJson)

            emit(mediaBean.copy(displayName = displayName))
        }.flowOn(Dispatchers.IO)
    }

    fun createGroup(path: String, group: MediaGroupBean): Flow<Unit> = flow {
        if (group.isDefaultGroup()) {
            emit(Unit)
            return@flow
        }
        val mediaLibJson = getOrReadMediaLibJson(path = path)
        mediaLibJson.allGroups.add(group.name)
        writeMediaLibJson(path = path, mediaLibJson)

        emit(Unit)
    }.flowOn(Dispatchers.IO)

    fun deleteGroup(path: String, group: MediaGroupBean): Flow<Unit> = flow {
        if (group.isDefaultGroup()) {
            emit(Unit)
            return@flow
        }
        val mediaLibJson = getOrReadMediaLibJson(path = path)
        mediaLibJson.files.forEach {
            if (it.groupName == group.name) {
                it.groupName = null
            }
        }
        mediaLibJson.allGroups.remove(group.name)
        writeMediaLibJson(path = path, mediaLibJson)

        emit(Unit)
    }.flowOn(Dispatchers.IO)

    fun renameGroup(
        path: String,
        group: MediaGroupBean,
        newName: String,
    ): Flow<MediaGroupBean> = flow {
        if (group.isDefaultGroup()) {
            emit(MediaGroupBean.DefaultMediaGroup)
            return@flow
        }
        val mediaLibJson = getOrReadMediaLibJson(path = path)
        mediaLibJson.files.forEach {
            if (it.groupName == group.name) {
                it.groupName = newName
            }
        }
        val index = mediaLibJson.allGroups.indexOf(group.name)
        if (index >= 0) {
            mediaLibJson.allGroups[index] = newName
        }
        writeMediaLibJson(path = path, mediaLibJson)

        emit(MediaGroupBean(name = newName))
    }.flowOn(Dispatchers.IO)

    fun changeMediaGroup(
        path: String,
        mediaBean: MediaBean,
        group: MediaGroupBean,
    ): Flow<Unit> = flow {
        val mediaLibJson = getOrReadMediaLibJson(path = path)
        val index = mediaLibJson.files.indexOfFirst { it.fileName == mediaBean.file.name }
        if (index >= 0) {
            mediaLibJson.files[index].groupName =
                if (group.isDefaultGroup()) null else group.name
        } else {
            if (!group.isDefaultGroup()) {
                mediaLibJson.files.add(
                    FileJson(
                        fileName = mediaBean.file.name,
                        groupName = group.name,
                        isFile = mediaBean.file.isFile,
                        displayName = mediaBean.displayName,
                    )
                )
            }
        }
        writeMediaLibJson(path = path, mediaLibJson)

        emit(Unit)
    }.flowOn(Dispatchers.IO)

    fun moveFilesToGroup(
        path: String,
        from: MediaGroupBean,
        to: MediaGroupBean
    ): Flow<Unit> = flow {
        val mediaLibJson = getOrReadMediaLibJson(path = path)
        if (from.isDefaultGroup()) {
            if (to.isDefaultGroup()) {
                emit(Unit)
                return@flow
            } else {
                mediaLibJson.files.appendFiles(
                    files = File(path).listFiles().orEmpty().toList(),
                    fileJsonBuild = {
                        FileJson(
                            fileName = it.name,
                            groupName = to.name,
                            isFile = it.isFile,
                            displayName = null,
                        )
                    }
                )
                mediaLibJson.files.forEach {
                    if (it.groupName == null) it.groupName = to.name
                }
            }
        } else {
            mediaLibJson.files.forEach {
                if (it.groupName == from.name) {
                    it.groupName = if (to.isDefaultGroup()) null else to.name
                }
            }
        }
        writeMediaLibJson(path = path, mediaLibJson)

        emit(Unit)
    }.flowOn(Dispatchers.IO)

    @Serializable
    data class MediaLibJson(
        @SerialName("allGroups")
        @EncodeDefault
        val allGroups: MutableList<String> = mutableListOf(),
        @SerialName("files")
        val files: MutableList<FileJson>,
    )

    @Serializable
    data class FileJson(
        @SerialName("fileName")
        var fileName: String,
        @SerialName("groupName")
        var groupName: String? = null,
        @SerialName("isFile")
        var isFile: Boolean = false,
        @SerialName("displayName")
        var displayName: String? = null,
    )

    @Serializable
    data class FolderInfo(
        @SerialName("displayName")
        val displayName: String? = null,
    )
}