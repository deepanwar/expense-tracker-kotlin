package com.example.expensetracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SettlementWithDetails(
    @Embedded val settlement: SettlementEntity,
    @Relation(
        parentColumn = "fromPersonId",
        entityColumn = "id"
    )
    val fromPerson: PersonEntity,
    @Relation(
        parentColumn = "toPersonId",
        entityColumn = "id"
    )
    val toPerson: PersonEntity,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "id"
    )
    val group: GroupEntity?
)
