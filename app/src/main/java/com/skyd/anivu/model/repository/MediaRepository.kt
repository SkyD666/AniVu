package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.MediaGroupBean
import com.skyd.anivu.model.bean.MediaGroupBean.Companion.isDefaultGroup
import com.skyd.anivu.model.bean.VideoBean
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

    fun requestMedias(uriPath: String, isMediaLibRoot: Boolean): Flow<List<Any>> {
        return flow {
            val file = File(uriPath)

            val fileList = file.listFiles()
                .orEmpty()
                .map {
                    VideoBean(
                        displayName = if (it.isDirectory) {
                            parseFolderInfoJson(File(it, FOLDER_INFO_JSON_NAME))?.displayName
                        } else {
                            null
                        },
                        file = it,
                    )
                }

            if (isMediaLibRoot) {
                val groupToFiles = parseGroupJsonToMap(
                    groupJsonFile = File(file, GROUP_JSON_NAME),
                )

                // Map group name to group object
                val groupNameToObject = groupToFiles.keys
                    .map { MediaGroupBean(name = it) }
                    .associateBy { it.name }

                // Map group object to video list
                val result = mapOf(MediaGroupBean.DefaultMediaGroup to mutableListOf<VideoBean>()) +
                        groupNameToObject.values.map { it to mutableListOf() }

                // Map (fileName, isFile) to group object
                val fileNameMap = groupToFiles.values
                    .flatten()
                    .associateBy { it.fileName }
                    .mapKeys { it.key to it.value.isFile }

                fileList.forEach { videoBean ->
                    // Skip config files
                    if (GROUP_JSON_NAME == videoBean.name) return@forEach
                    val fileToGroup = fileNameMap[videoBean.name to videoBean.isFile]
                    if (fileToGroup == null) {
                        result[MediaGroupBean.DefaultMediaGroup]!! += videoBean
                    } else {
                        result[groupNameToObject[fileToGroup.groupName]]!! += videoBean
                    }
                }

                emit(result.flatMap { (group, list) -> listOf(group) + list })
            } else {
                // Skip config files
                emit(fileList.toMutableList().apply {
                    forEach {
                        if (it.name == GROUP_JSON_NAME) {
                            remove(it)
                            return@apply
                        }
                    }
                })
            }
        }.flowOn(Dispatchers.IO)
    }

    fun requestDelete(file: File): Flow<Boolean> {
        return flow {
            emit(file.deleteRecursively())
        }.flowOn(Dispatchers.IO)
    }

    fun requestCreateGroup(uriPath: String, group: MediaGroupBean): Flow<Unit> = flow {
        if (group.isDefaultGroup()) {
            emit(Unit)
            return@flow
        }
        val groupJsonFile = File(uriPath, GROUP_JSON_NAME)
        val mediaGroupJson = parseGroupJson(groupJsonFile)!!

        writeGroupToJson(
            groupJsonFile,
            mediaGroupJson.copy(allGroups = mediaGroupJson.allGroups + group.name),
        )

        emit(Unit)
    }.flowOn(Dispatchers.IO)

    fun requestDeleteGroup(uriPath: String, group: MediaGroupBean): Flow<Int> = flow {
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

    fun requestRenameGroup(
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

    fun requestChangeMediaGroup(
        uriPath: String,
        videoBean: VideoBean,
        group: MediaGroupBean,
    ): Flow<Unit> = flow {
        val groupJsonFile = File(uriPath, GROUP_JSON_NAME)
        val mediaGroupJson = parseGroupJson(groupJsonFile)!!
        val fileToGroups = mediaGroupJson.files.toSet()
        val file = fileToGroups.firstOrNull { it.fileName == videoBean.file.name }

        val newFileToGroups = (if (file != null) fileToGroups - file else fileToGroups)
            .toMutableList()
            .apply {
                if (!group.isDefaultGroup()) {
                    add(
                        FileToGroup(
                            fileName = videoBean.file.name,
                            groupName = group.name,
                            isFile = videoBean.file.isFile,
                        )
                    )
                }
            }
        writeGroupToJson(groupJsonFile, mediaGroupJson.copy(files = newFileToGroups.toList()))

        emit(Unit)
    }.flowOn(Dispatchers.IO)

    fun requestMoveFilesToGroup(
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

    private fun parseFolderInfoJson(folderJsonFile: File): FolderInfo? {
        if (!folderJsonFile.exists()) return null
        return folderJsonFile.inputStream().use { inputStream ->
            json.decodeFromStream(inputStream)
        }
    }
}