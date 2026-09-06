package com.example.expensetracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseParticipantWithPerson(
    @Embedded val participant: ExpenseParticipantEntity,
    @Relation(
        parentColumn = "personId",
        entityColumn = "id"
    )
    val person: PersonEntity
)

data class ExpenseWithParticipants(
    @Embedded val expense: ExpenseEntity,
    @Relation(
        parentColumn = "payerId",
        entityColumn = "id"
    )
    val payer: PersonEntity,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "id"
    )
    val group: GroupEntity?,
    @Relation(
        entity = ExpenseParticipantEntity::class,
        parentColumn = "id",
        entityColumn = "expenseId"
    )
    val participants: List<ExpenseParticipantWithPerson>
)
