package com.example.expensetracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class GroupWithMemberEntities(
    @Embedded val group: GroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = GroupMemberEntity::class,
            parentColumn = "groupId",
            entityColumn = "personId"
        )
    )
    val members: List<PersonEntity>
)
