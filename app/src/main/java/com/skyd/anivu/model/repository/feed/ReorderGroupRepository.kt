package com.skyd.anivu.model.repository.feed

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.ext.calculateHashMapInitialCapacity
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.model.bean.GroupVo
import com.skyd.anivu.model.bean.GroupWithFeedBean
import com.skyd.anivu.model.db.dao.GroupDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ReorderGroupRepository @Inject constructor(
    private val groupDao: GroupDao,
) : BaseRepository() {
    private fun sortGroups(groupList: List<GroupBean>): List<GroupBean> {
        val groupsMap = groupList.associateBy { it.groupId }
        val hasPreviousGroups = LinkedHashSet<GroupBean>(
            calculateHashMapInitialCapacity(groupList.size)
        )
        groupList.forEach { group ->
            val nextGroupId = group.nextGroupId
            if (nextGroupId != null) {
                hasPreviousGroups.add(groupsMap[nextGroupId]!!)
            }
        }
        val noPreviousGroups = (groupList - hasPreviousGroups).sortedBy { it.name }
        val sortedList = LinkedHashSet<GroupBean>(
            calculateHashMapInitialCapacity(groupList.size)
        )
        noPreviousGroups.forEach { group ->
            var currentGroup: GroupBean? = group
            var currentGroupId: String? = group.groupId
            while (currentGroupId != null) {
                sortedList.add(currentGroup!!)
                if (currentGroupId == currentGroup.nextGroupId) break
                currentGroupId = currentGroup.nextGroupId
                currentGroup = if (currentGroupId != null) groupsMap[currentGroupId] else null
            }
        }
        val result = sortedList.toList()
        return result
    }

    fun sortGroupWithFeed(
        groupList: List<GroupWithFeedBean>
    ): List<GroupWithFeedBean> {
        val groupsMap = groupList.associateBy { it.group.groupId }
        val hasPreviousGroups = LinkedHashSet<GroupWithFeedBean>(
            calculateHashMapInitialCapacity(groupList.size)
        )
        groupList.forEach { group ->
            val nextGroupId = group.group.nextGroupId
            if (nextGroupId != null) {
                hasPreviousGroups.add(groupsMap[nextGroupId]!!)
            }
        }
        val noPreviousGroups = (groupList - hasPreviousGroups).sortedBy { it.group.name }
        val sortedList = LinkedHashSet<GroupWithFeedBean>(
            calculateHashMapInitialCapacity(groupList.size)
        )
        noPreviousGroups.forEach { group ->
            var currentGroup: GroupWithFeedBean? = group
            var currentGroupId: String? = group.group.groupId
            while (currentGroupId != null) {
                sortedList.add(currentGroup!!)
                if (currentGroupId == currentGroup.group.nextGroupId) break
                currentGroupId = currentGroup.group.nextGroupId
                currentGroup = if (currentGroupId != null) groupsMap[currentGroupId] else null
            }
        }
        val result = sortedList.toList()
        return result
    }

    fun requestGroupList(): Flow<List<GroupVo>> {
        return flow {
            val result = sortGroups(groupDao.getGroups().first())
            result.forEachIndexed { index, groupBean ->
                if (index == 0 && groupBean.previousGroupId == null && groupBean.nextGroupId != null) {
                    return@forEachIndexed
                }
                if (index == result.size - 1 && groupBean.previousGroupId != null && groupBean.nextGroupId == null) {
                    return@forEachIndexed
                }
                // Create link pointer if it is not exist
                if (groupBean.previousGroupId == null || groupBean.nextGroupId == null) {
                    groupDao.reorderGroup(
                        groupId = groupBean.groupId,
                        newPreviousGroupId = result.getOrNull(index - 1)?.groupId,
                        newNextGroupId = result.getOrNull(index + 1)?.groupId,
                    )
                }
            }
            emit(result.map { it.toVo() })
        }.flowOn(Dispatchers.IO)
    }

    fun requestReorderGroup(
        movedGroupId: String,
        newPreviousGroupId: String? = null,
        newNextGroupId: String? = null,
    ): Flow<Boolean> {
        return flow {
            emit(
                groupDao.reorderGroup(
                    groupId = movedGroupId,
                    newPreviousGroupId = newPreviousGroupId,
                    newNextGroupId = newNextGroupId,
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    fun requestResetGroupOrder(): Flow<List<GroupVo>> {
        return flow {
            groupDao.resetGroupOrder()
            emit(requestGroupList().first())
        }.flowOn(Dispatchers.IO)
    }
}