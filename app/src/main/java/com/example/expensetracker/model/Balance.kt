package com.example.expensetracker.model

data class PersonBalance(
    val personId: Long,
    val person: Person,
    val youOwe: Long,
    val theyOwe: Long,
    val netBalance: Long
)

data class GroupBalance(
    val groupId: Long,
    val youOwe: Long,
    val youAreOwed: Long,
    val netBalance: Long,
    val personBalances: List<PersonBalance>
)

data class OverallBalance(
    val totalYouOwe: Long,
    val totalYouAreOwed: Long,
    val netBalance: Long
)

fun ExpenseDetails.involves(personId: Long): Boolean {
    return expense.payerId == personId || participants.any { it.personId == personId }
}
