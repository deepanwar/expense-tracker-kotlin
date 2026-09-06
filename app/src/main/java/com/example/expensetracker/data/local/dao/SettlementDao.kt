package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.expensetracker.data.local.entity.SettlementEntity
import com.example.expensetracker.data.local.entity.SettlementWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementDao {
    @Transaction
    @Query("SELECT * FROM settlements ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<SettlementWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM settlements
        WHERE groupId = :groupId
        ORDER BY date DESC, id DESC
        """
    )
    fun observeByGroupId(groupId: Long): Flow<List<SettlementWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM settlements
        WHERE fromPersonId = :personId OR toPersonId = :personId
        ORDER BY date DESC, id DESC
        """
    )
    fun observeInvolvingPerson(personId: Long): Flow<List<SettlementWithDetails>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(settlement: SettlementEntity): Long
}
