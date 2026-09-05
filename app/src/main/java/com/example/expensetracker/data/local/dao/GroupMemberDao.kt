package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expensetracker.data.local.entity.GroupMemberEntity

@Dao
interface GroupMemberDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(members: List<GroupMemberEntity>)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND personId = :personId")
    suspend fun deleteMember(groupId: Long, personId: Long)

    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteAllForGroup(groupId: Long)
}
