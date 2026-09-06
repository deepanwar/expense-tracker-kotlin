package com.example.expensetracker.data.repository

import androidx.room.withTransaction
import com.example.expensetracker.data.local.ExpenseTrackerDatabase
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.ExpenseParticipantEntity
import com.example.expensetracker.data.local.toDomain
import com.example.expensetracker.data.local.toEntity
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseDetails
import com.example.expensetracker.util.Money
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepository(
    private val database: ExpenseTrackerDatabase
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
        participantIds: Collection<Long>,
        currentUserId: Long?
    ): Long {
        val now = System.currentTimeMillis()
        val shares = Money.sharesFor(amountMinorUnits, participantIds, currentUserId)
        return database.withTransaction {
            val expenseId = expenseDao.insert(
                ExpenseEntity(
                    description = description,
                    amountMinorUnits = amountMinorUnits,
                    date = date,
                    groupId = groupId,
                    payerId = payerId,
                    createdAt = now,
                    updatedAt = now
                )
            )
            insertShares(expenseId, shares)
            expenseId
        }
    }

    suspend fun update(
        expense: Expense,
        participantIds: Collection<Long>,
        currentUserId: Long?
    ) {
        val now = System.currentTimeMillis()
        val shares = Money.sharesFor(expense.amountMinorUnits, participantIds, currentUserId)
        database.withTransaction {
            expenseDao.update(expense.copy(updatedAt = now).toEntity())
            expenseDao.deleteParticipants(expense.id)
            insertShares(expense.id, shares)
        }
    }

    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense.toEntity())
    }

    private suspend fun insertShares(expenseId: Long, shares: Map<Long, Long>) {
        if (shares.isEmpty()) return
        expenseDao.insertParticipants(
            shares.map { (personId, shareMinorUnits) ->
                ExpenseParticipantEntity(
                    expenseId = expenseId,
                    personId = personId,
                    shareMinorUnits = shareMinorUnits
                )
            }
        )
    }
}
