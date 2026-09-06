package com.example.expensetracker.model

data class Expense(
    val id: Long = 0,
    val description: String,
    val amountMinorUnits: Long,
    val date: Long,
    val groupId: Long? = null,
    val payerId: Long,
    val splitMethod: SplitMethod = SplitMethod.EQUAL,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

data class ExpenseParticipant(
    val personId: Long,
    val shareMinorUnits: Long,
    val person: Person
)

data class ExpenseDetails(
    val expense: Expense,
    val payer: Person,
    val group: Group?,
    val participants: List<ExpenseParticipant>
)
