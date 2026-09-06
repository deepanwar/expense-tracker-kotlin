package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settlements",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromPersonId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["toPersonId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("fromPersonId"),
        Index("toPersonId"),
        Index("groupId"),
        Index("date")
    ]
)
data class SettlementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromPersonId: Long,
    val toPersonId: Long,
    val amountMinorUnits: Long,
    val groupId: Long? = null,
    val note: String? = null,
    val date: Long,
    val createdAt: Long
)
