package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.ExpenseTrackerDatabase
import com.example.expensetracker.data.local.entity.SettlementEntity
import com.example.expensetracker.data.local.toDomain
import com.example.expensetracker.data.remote.CloudSync
import com.example.expensetracker.data.remote.NoOpCloudSync
import com.example.expensetracker.model.SettlementDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettlementRepository(
    private val database: ExpenseTrackerDatabase,
    private val cloudSync: CloudSync = NoOpCloudSync
) {
    private val settlementDao = database.settlementDao()

    fun observeAll(): Flow<List<SettlementDetails>> {
        return settlementDao.observeAll().map { rows -> rows.map { it.toDomain() } }
    }

    fun observeByGroup(groupId: Long): Flow<List<SettlementDetails>> {
        return settlementDao.observeByGroupId(groupId).map { rows -> rows.map { it.toDomain() } }
    }

    fun observeInvolvingPerson(personId: Long): Flow<List<SettlementDetails>> {
        return settlementDao.observeInvolvingPerson(personId).map { rows -> rows.map { it.toDomain() } }
    }

    suspend fun recordSettlement(
        fromPersonId: Long,
        toPersonId: Long,
        amountMinorUnits: Long,
        groupId: Long? = null,
        note: String? = null,
        date: Long = System.currentTimeMillis()
    ): Long {
        val now = System.currentTimeMillis()
        val settlement = SettlementEntity(
            fromPersonId = fromPersonId,
            toPersonId = toPersonId,
            amountMinorUnits = amountMinorUnits,
            groupId = groupId,
            note = note?.trim()?.ifEmpty { null },
            date = date,
            createdAt = now
        )
        val id = settlementDao.insert(settlement)
        cloudSync.upsertSettlement(settlement.copy(id = id))
        return id
    }
}
