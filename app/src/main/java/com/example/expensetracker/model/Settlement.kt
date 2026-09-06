package com.example.expensetracker.model

data class Settlement(
    val id: Long = 0,
    val fromPersonId: Long,
    val toPersonId: Long,
    val amountMinorUnits: Long,
    val groupId: Long? = null,
    val note: String? = null,
    val date: Long,
    val createdAt: Long = 0
)

data class SettlementDetails(
    val settlement: Settlement,
    val fromPerson: Person,
    val toPerson: Person,
    val group: Group?
)
