package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expensetracker.data.local.entity.GroupEntity
import com.example.expensetracker.data.local.entity.GroupWithMemberCount
import com.example.expensetracker.data.local.entity.GroupWithMemberEntities
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query(
        """
        SELECT groups.*, COUNT(group_members.personId) AS memberCount
        FROM groups
        LEFT JOIN group_members ON groups.id = group_members.groupId
        WHERE groups.archivedAt IS NULL
        GROUP BY groups.id
        ORDER BY groups.name COLLATE NOCASE ASC
        """
    )
    fun observeActiveSummaries(): Flow<List<GroupWithMemberCount>>

    @Query(
        """
        SELECT groups.*, COUNT(group_members.personId) AS memberCount
        FROM groups
        LEFT JOIN group_members ON groups.id = group_members.groupId
        WHERE groups.archivedAt IS NOT NULL
        GROUP BY groups.id
        ORDER BY groups.archivedAt DESC
        """
    )
    fun observeArchivedSummaries(): Flow<List<GroupWithMemberCount>>

    @Query("SELECT * FROM groups WHERE id = :id")
    fun observeById(id: Long): Flow<GroupEntity?>

    @Transaction
    @Query("SELECT * FROM groups WHERE id = :id")
    fun observeGroupWithMembers(id: Long): Flow<GroupWithMemberEntities?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(group: GroupEntity): Long

    @Update
    suspend fun update(group: GroupEntity)

    @Delete
    suspend fun delete(group: GroupEntity)

    @Query(
        """
        UPDATE groups
        SET archivedAt = :archivedAt, updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun setArchivedAt(id: Long, archivedAt: Long?, updatedAt: Long)
}
