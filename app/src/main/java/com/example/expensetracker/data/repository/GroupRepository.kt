package com.example.expensetracker.data.repository

import androidx.room.withTransaction
import com.example.expensetracker.data.local.ExpenseTrackerDatabase
import com.example.expensetracker.data.local.entity.GroupEntity
import com.example.expensetracker.data.local.entity.GroupMemberEntity
import com.example.expensetracker.data.local.toDomain
import com.example.expensetracker.data.local.toEntity
import com.example.expensetracker.data.local.toSummary
import com.example.expensetracker.model.Group
import com.example.expensetracker.model.GroupSummary
import com.example.expensetracker.model.GroupWithMembers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GroupRepository(
    private val database: ExpenseTrackerDatabase
) {
    private val groupDao = database.groupDao()
    private val groupMemberDao = database.groupMemberDao()

    fun observeActiveGroups(): Flow<List<GroupSummary>> {
        return groupDao.observeActiveSummaries().map { groups -> groups.map { it.toSummary() } }
    }

    fun observeArchivedGroups(): Flow<List<GroupSummary>> {
        return groupDao.observeArchivedSummaries().map { groups -> groups.map { it.toSummary() } }
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
        return database.withTransaction {
            val groupId = groupDao.insert(
                GroupEntity(
                    name = name,
                    icon = icon,
                    createdAt = now,
                    updatedAt = now,
                    archivedAt = null
                )
            )
            if (memberIds.isNotEmpty()) {
                groupMemberDao.insertAll(
                    memberIds.distinct().map { personId ->
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
    }

    suspend fun updateGroup(group: Group) {
        val now = System.currentTimeMillis()
        groupDao.update(group.copy(updatedAt = now).toEntity())
    }

    suspend fun archiveGroup(groupId: Long) {
        val now = System.currentTimeMillis()
        groupDao.setArchivedAt(id = groupId, archivedAt = now, updatedAt = now)
    }

    suspend fun restoreGroup(groupId: Long) {
        val now = System.currentTimeMillis()
        groupDao.setArchivedAt(id = groupId, archivedAt = null, updatedAt = now)
    }

    suspend fun deleteGroup(group: Group) {
        groupDao.delete(group.toEntity())
    }

    suspend fun addMembers(groupId: Long, personIds: Collection<Long>) {
        if (personIds.isEmpty()) return
        val now = System.currentTimeMillis()
        groupMemberDao.insertAll(
            personIds.distinct().map { personId ->
                GroupMemberEntity(
                    groupId = groupId,
                    personId = personId,
                    joinedAt = now
                )
            }
        )
    }

    suspend fun removeMember(groupId: Long, personId: Long) {
        groupMemberDao.deleteMember(groupId = groupId, personId = personId)
    }
}
