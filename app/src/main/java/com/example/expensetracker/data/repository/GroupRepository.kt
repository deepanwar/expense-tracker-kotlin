package com.example.expensetracker.data.repository

import androidx.room.withTransaction
import com.example.expensetracker.data.local.ExpenseTrackerDatabase
import com.example.expensetracker.data.local.entity.GroupEntity
import com.example.expensetracker.data.local.entity.GroupMemberEntity
import com.example.expensetracker.data.local.toDomain
import com.example.expensetracker.data.local.toEntity
import com.example.expensetracker.data.local.toSummary
import com.example.expensetracker.data.remote.CloudSync
import com.example.expensetracker.data.remote.NoOpCloudSync
import com.example.expensetracker.model.Group
import com.example.expensetracker.model.GroupSummary
import com.example.expensetracker.model.GroupWithMembers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GroupRepository(
    private val database: ExpenseTrackerDatabase,
    private val cloudSync: CloudSync = NoOpCloudSync
) {
    private val groupDao = database.groupDao()
    private val groupMemberDao = database.groupMemberDao()

    fun observeActiveGroups(): Flow<List<GroupSummary>> {
        return groupDao.observeActiveSummaries().map { groups -> groups.map { it.toSummary() } }
    }

    fun observeArchivedGroups(): Flow<List<GroupSummary>> {
        return groupDao.observeArchivedSummaries().map { groups -> groups.map { it.toSummary() } }
    }

    fun observeCommonGroups(personId: Long): Flow<List<GroupSummary>> {
        return groupDao.observeCommonGroups(personId)
            .map { groups -> groups.map { it.toSummary() } }
    }

    fun observeGroupWithMembers(id: Long): Flow<GroupWithMembers?> {
        return groupDao.observeGroupWithMembers(id).map { entity -> entity?.toDomain() }
    }

    suspend fun createGroup(
        name: String,
        icon: String,
        memberIds: Collection<Long>
    ): Long {
        val now = System.currentTimeMillis()
        val members = memberIds.distinct()
        val groupId = database.withTransaction {
            val groupId = groupDao.insert(
                GroupEntity(
                    name = name,
                    icon = icon,
                    createdAt = now,
                    updatedAt = now,
                    archivedAt = null
                )
            )
            if (members.isNotEmpty()) {
                groupMemberDao.insertAll(
                    members.map { personId ->
                        GroupMemberEntity(
                            groupId = groupId,
                            personId = personId,
                            joinedAt = now
                        )
                    }
                )
            }
            groupId
        }
        cloudSync.upsertGroup(
            Group(
                id = groupId,
                name = name,
                icon = icon,
                createdAt = now,
                updatedAt = now,
                archivedAt = null
            )
        )
        cloudSync.upsertGroupMembers(
            members.map { personId ->
                GroupMemberEntity(
                    groupId = groupId,
                    personId = personId,
                    joinedAt = now
                )
            }
        )
        return groupId
    }

    suspend fun updateGroup(group: Group) {
        val now = System.currentTimeMillis()
        val updated = group.copy(updatedAt = now)
        groupDao.update(updated.toEntity())
        cloudSync.upsertGroup(updated)
    }

    suspend fun archiveGroup(groupId: Long) {
        val now = System.currentTimeMillis()
        groupDao.setArchivedAt(id = groupId, archivedAt = now, updatedAt = now)
        groupDao.getById(groupId)?.toDomain()?.let { cloudSync.upsertGroup(it) }
    }

    suspend fun restoreGroup(groupId: Long) {
        val now = System.currentTimeMillis()
        groupDao.setArchivedAt(id = groupId, archivedAt = null, updatedAt = now)
        groupDao.getById(groupId)?.toDomain()?.let { cloudSync.upsertGroup(it) }
    }

    suspend fun deleteGroup(group: Group) {
        groupDao.delete(group.toEntity())
        cloudSync.deleteGroup(group.id)
    }

    suspend fun addMembers(groupId: Long, personIds: Collection<Long>) {
        if (personIds.isEmpty()) return
        val now = System.currentTimeMillis()
        val members = personIds.distinct().map { personId ->
            GroupMemberEntity(
                groupId = groupId,
                personId = personId,
                joinedAt = now
            )
        }
        groupMemberDao.insertAll(members)
        cloudSync.upsertGroupMembers(members)
    }

    suspend fun removeMember(groupId: Long, personId: Long) {
        groupMemberDao.deleteMember(groupId = groupId, personId = personId)
        cloudSync.deleteGroupMember(groupId, personId)
    }
}
