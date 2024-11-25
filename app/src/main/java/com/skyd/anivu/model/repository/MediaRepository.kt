package com.skyd.anivu.model.repository

import androidx.compose.ui.util.fastFirstOrNull
import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.MediaGroupBean
import com.skyd.anivu.model.bean.MediaGroupBean.Companion.isDefaultGroup
import com.skyd.anivu.model.bean.MediaBean
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
        const val FOLDER_INFO_JSON_NAME = "info.json"
        const val GROUP_JSON_NAME = "group.json"
    }

    fun requestGroups(uriPath: String): Flow<List<MediaGroupBean>> {
        return flow {
            val groupJsonFile = File(uriPath, GROUP_JSON_NAME)
            val mediaGroupJson = parseGroupJson(groupJsonFile)
            val allGroups = mediaGroupJson?.allGroups.orEmpty()
            emit(listOf(MediaGroupBean.DefaultMediaGroup) +
                    allGroups.map { MediaGroupBean(name = it) }.sortedBy { it.name })
        }.flowOn(Dispatchers.IO)
    }

    fun requestFiles(uriPath: String, group: MediaGroupBean?): Flow<List<MediaBean>> {
        return flow {
            val groupJsonFile = File(uriPath, GROUP_JSON_NAME)
            val mediaGroupJson = parseGroupJson(groupJsonFile)
            val filesInGroup = mediaGroupJson?.files.orEmpty()
            val allFiles =
                File(uriPath).listFiles().orEmpty().toMutableList().filter { it.exists() }
            val videoList = if (group == null) {
                allFiles.map {
                    MediaBean(
                        displayName = null,     // TODO
                        file = it,
                    )
                }
            } else {
                if (group.isDefaultGroup()) {
                    allFiles.filter { file ->
                        filesInGroup.fastFirstOrNull { it.fileName == file.name } == null
                    }.map { file ->
                        MediaBean(
                            displayName = null,     // TODO
                            file = file,
                        )
                    }
                } else {
                    filesInGroup.filter { it.groupName == group.name }.mapNotNull {
                        val file = File(uriPath, it.fileName)
                        if (file.exists()) {
                            MediaBean(
                                displayName = null,     // TODO
                                file = file,
                            )
                        } else null
                    }
                }

            }
            emit(videoList.toMutableList().apply {
                fastFirstOrNull { it.name.equals(FOLDER_INFO_JSON_NAME, true) }?.let { remove(it) }
                fastFirstOrNull { it.name.equals(GROUP_JSON_NAME, true) }?.let { remove(it) }
            })
        }.flowOn(Dispatchers.IO)
    }

    fun deleteFile(file: File): Flow<Boolean> {
        return flow {
            emit(file.deleteRecursively())
        }.flowOn(Dispatchers.IO)
    }

    fun createGroup(uriPath: String, group: MediaGroupBean): Flow<Unit> = flow {
        if (group.isDefaultGroup()) {
            emit(Unit)
            return@flow
        }
        val groupJsonFile = File(uriPath, GROUP_JSON_NAME)
        val mediaGroupJson = parseGroupJson(groupJsonFile) ?: MediaGroupJson(files = emptyList())

        writeGroupToJson(
            groupJsonFile,
            mediaGroupJson.copy(allGroups = mediaGroupJson.allGroups + group.name),
        )

        emit(Unit)
    }.flowOn(Dispatchers.IO)

    fun deleteGroup(uriPath: String, group: MediaGroupBean): Flow<Int> = flow {
        if (group.isDefaultGroup()) {
            emit(0)
            return@flow
        }
        val groupJsonFile = File(uriPath, GROUP_JSON_NAME)
        val mediaGroupJson = parseGroupJson(groupJsonFile)!!
        val fileToGroups = mediaGroupJson.files.toSet()
        val newFileToGroups = fileToGroups.filter { it.groupName != group.name }.toSet()

        writeGroupToJson(
            groupJsonFile,
            mediaGroupJson.copy(
                allGroups = mediaGroupJson.allGroups - group.name,
                files = newFileToGroups.toList()
            ),
        )

        (fileToGroups - newFileToGroups).forEach {
            File(uriPath, it.fileName).deleteRecursively()
        }

        emit(newFileToGroups.count() - fileToGroups.count())
    }.flowOn(Dispatchers.IO)

    fun renameGroup(
        uriPath: String,
        group: MediaGroupBean,
        newName: String,
    ): Flow<MediaGroupBean> = flow {
        if (group.isDefaultGroup()) {
            emit(MediaGroupBean.DefaultMediaGroup)
            return@flow
        }
        val groupJsonFile = File(uriPath, GROUP_JSON_NAME)
        val mediaGroupJson = parseGroupJson(groupJsonFile)!!

        writeGroupToJson(
            groupJsonFile,
            mediaGroupJson.copy(
                allGroups = mediaGroupJson.allGroups.toMutableList().apply {
                    val index = indexOf(group.name)
                    if (index != -1 && index < size) {
                        set(index, newName)
                    }
                },
                files = mediaGroupJson.files.map {
                    if (it.groupName == group.name) {
                        it.copy(groupName = newName)
                    } else {
                        it
                    }
                }
            ),
        )

        emit(MediaGroupBean(name = newName))
    }.flowOn(Dispatchers.IO)

    fun changeMediaGroup(
        uriPath: String,
        mediaBean: MediaBean,
        group: MediaGroupBean,
    ): Flow<Unit> = flow {
        val groupJsonFile = File(uriPath, GROUP_JSON_NAME)
        val mediaGroupJson = parseGroupJson(groupJsonFile)!!
        val fileToGroups = mediaGroupJson.files.toSet()
        val file = fileToGroups.firstOrNull { it.fileName == mediaBean.file.name }

        val newFileToGroups = (if (file != null) fileToGroups - file else fileToGroups)
            .toMutableList()
            .apply {
                if (!group.isDefaultGroup()) {
                    add(
                        FileToGroup(
                            fileName = mediaBean.file.name,
                            groupName = group.name,
                            isFile = mediaBean.file.isFile,
                        )
                    )
                }
            }
        writeGroupToJson(groupJsonFile, mediaGroupJson.copy(files = newFileToGroups.toList()))

        emit(Unit)
    }.flowOn(Dispatchers.IO)

    fun moveFilesToGroup(
        path: String,
        from: MediaGroupBean,
        to: MediaGroupBean
    ): Flow<Unit> = flow {
        val groupJsonFile = File(path, GROUP_JSON_NAME)
        val mediaGroupJson = parseGroupJson(groupJsonFile)!!
        val fileToGroups = mediaGroupJson.files.toSet()
        val newFileToGroups: List<FileToGroup> = if (from.isDefaultGroup()) {
            if (to.isDefaultGroup()) {
                fileToGroups.toList()
            } else {
                fileToGroups.toList() + File(path).listFiles().orEmpty().filter { f ->
                    // null means the current file f is not grouped
                    fileToGroups.firstOrNull {
                        f.isFile == it.isFile && f.name == it.fileName
                    } == null
                }.map { FileToGroup(it.name, to.name, isFile = it.isFile) }
            }
        } else {
            if (to.isDefaultGroup()) {
                fileToGroups.toList() - fileToGroups.filter { it.groupName == from.name }.toSet()
            } else {
                fileToGroups.map {
                    if (it.groupName == from.name) {
                        it.copy(groupName = to.name)
                    } else {
                        it
                    }
                }
            }
        }

        writeGroupToJson(groupJsonFile, mediaGroupJson.copy(files = newFileToGroups.toList()))

        emit(Unit)
    }.flowOn(Dispatchers.IO)

    @Serializable
    data class MediaGroupJson(
        @SerialName("allGroups")
        @EncodeDefault
        val allGroups: List<String> = listOf(),
        @SerialName("files")
        val files: List<FileToGroup>,
    )

    @Serializable
    data class FileToGroup(
        @SerialName("fileName")
        val fileName: String,
        @SerialName("groupName")
        val groupName: String,
        @SerialName("isFile")
        val isFile: Boolean = false,
    )

    @Serializable
    data class FolderInfo(
        @SerialName("displayName")
        val displayName: String? = null,
    )

    // Format groups
    private fun formatMediaGroupJson(old: MediaGroupJson): MediaGroupJson {
        val allGroups = (old.files.map { it.groupName } + old.allGroups).distinct()
        return MediaGroupJson(
            allGroups = allGroups,
            files = old.files,
        )
    }

    private fun parseGroupJson(groupJsonFile: File): MediaGroupJson? {
        if (!groupJsonFile.exists()) return null
        return groupJsonFile.inputStream().use { inputStream ->
            json.decodeFromStream<MediaGroupJson>(inputStream)
        }
    }

    private fun parseGroupJsonToMap(groupJsonFile: File): Map<String, List<FileToGroup>> {
        val mediaGroupJson = parseGroupJson(groupJsonFile) ?: return emptyMap()
        return mediaGroupJson.files.groupBy { it.groupName }.toMutableMap().apply {
            mediaGroupJson.allGroups.forEach {
                if (!containsKey(it)) {
                    put(it, emptyList())
                }
            }
        }.toSortedMap()
    }

    private fun writeGroupToJson(groupJsonFile: File, data: MediaGroupJson) {
        groupJsonFile.outputStream().use { outputStream ->
            json.encodeToStream(formatMediaGroupJson(data), outputStream)
        }
    }
}