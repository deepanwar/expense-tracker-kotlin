package com.example.expensetracker.data.local

import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.ExpenseWithParticipants
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseDetails
import com.example.expensetracker.model.ExpenseParticipant

internal fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        id = id,
        description = description,
        amountMinorUnits = amountMinorUnits,
        date = date,
        groupId = groupId,
        payerId = payerId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

internal fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        description = description,
        amountMinorUnits = amountMinorUnits,
        date = date,
        groupId = groupId,
        payerId = payerId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

internal fun ExpenseWithParticipants.toDomain(): ExpenseDetails {
    return ExpenseDetails(
        expense = expense.toDomain(),
        payer = payer.toDomain(),
        group = group?.toDomain(),
        participants = participants.map { row ->
            ExpenseParticipant(
                personId = row.participant.personId,
                shareMinorUnits = row.participant.shareMinorUnits,
                person = row.person.toDomain()
            )
        }
    )
}
