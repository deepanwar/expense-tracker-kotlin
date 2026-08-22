package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "persons",
    indices = [
        Index(value = ["phone"]),
        Index(value = ["email"]),
        Index(value = ["contactId"], unique = true)
    ]
)
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val photoUri: String? = null,
    val contactId: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
