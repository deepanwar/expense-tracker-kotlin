package com.example.expensetracker.data.repository

import androidx.room.withTransaction
import com.example.expensetracker.data.local.ExpenseTrackerDatabase
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.ExpenseParticipantEntity
import com.example.expensetracker.data.local.toDomain
import com.example.expensetracker.data.local.toEntity
import com.example.expensetracker.data.remote.CloudSync
import com.example.expensetracker.data.remote.NoOpCloudSync
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseDetails
import com.example.expensetracker.model.SplitMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepository(
    private val database: ExpenseTrackerDatabase,
    private val cloudSync: CloudSync = NoOpCloudSync
) {
    private val expenseDao = database.expenseDao()

    fun observeForPerson(personId: Long): Flow<List<ExpenseDetails>> {
        return expenseDao.observeForPerson(personId).map { rows -> rows.map { it.toDomain() } }
    }

    fun observeByGroup(groupId: Long): Flow<List<ExpenseDetails>> {
        return expenseDao.observeByGroupId(groupId).map { rows -> rows.map { it.toDomain() } }
    }

    fun observeById(id: Long): Flow<ExpenseDetails?> {
        return expenseDao.observeById(id).map { entity -> entity?.toDomain() }
    }

    fun observeExpensesInvolvingPerson(personId: Long): Flow<List<ExpenseDetails>> {
        return expenseDao.observeExpensesInvolvingPerson(personId).map { rows -> rows.map { it.toDomain() } }
    }

    fun observeAllExpenses(): Flow<List<ExpenseDetails>> {
        return expenseDao.observeAllExpenses().map { rows -> rows.map { it.toDomain() } }
    }

    suspend fun create(
        description: String,
        amountMinorUnits: Long,
        date: Long,
        groupId: Long?,
        payerId: Long,
        splitMethod: SplitMethod,
        shares: Map<Long, Long>
    ): Long {
        val now = System.currentTimeMillis()
        val expense = ExpenseEntity(
            description = description,
            amountMinorUnits = amountMinorUnits,
            date = date,
            groupId = groupId,
            payerId = payerId,
            splitMethod = splitMethod.name,
            createdAt = now,
            updatedAt = now
        )
        val expenseId = database.withTransaction {
            val expenseId = expenseDao.insert(expense)
            insertShares(expenseId, shares)
            expenseId
        }
        val participants = shareEntities(expenseId, shares)
        cloudSync.upsertExpense(expense.copy(id = expenseId))
        cloudSync.replaceExpenseParticipants(expenseId, participants)
        return expenseId
    }

    suspend fun update(
        expense: Expense,
        shares: Map<Long, Long>
    ) {
        val now = System.currentTimeMillis()
        val updated = expense.copy(updatedAt = now)
        database.withTransaction {
            expenseDao.update(updated.toEntity())
            expenseDao.deleteParticipants(expense.id)
            insertShares(expense.id, shares)
        }
        cloudSync.upsertExpense(updated.toEntity())
        cloudSync.replaceExpenseParticipants(expense.id, shareEntities(expense.id, shares))
    }

    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense.toEntity())
        cloudSync.deleteExpense(expense.id)
    }

    private suspend fun insertShares(expenseId: Long, shares: Map<Long, Long>) {
        val participants = shareEntities(expenseId, shares)
        if (participants.isEmpty()) return
        expenseDao.insertParticipants(participants)
    }

    private fun shareEntities(
        expenseId: Long,
        shares: Map<Long, Long>
    ): List<ExpenseParticipantEntity> {
        return shares.map { (personId, shareMinorUnits) ->
            ExpenseParticipantEntity(
                expenseId = expenseId,
                personId = personId,
                shareMinorUnits = shareMinorUnits
            )
        }
    }
}
