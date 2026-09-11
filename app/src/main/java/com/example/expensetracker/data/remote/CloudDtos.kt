package com.example.expensetracker.data.remote

import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.ExpenseParticipantEntity
import com.example.expensetracker.data.local.entity.GroupMemberEntity
import com.example.expensetracker.data.local.entity.SettlementEntity
import com.example.expensetracker.model.Group
import com.example.expensetracker.model.Person
import kotlinx.serialization.Serializable

@Serializable
data class PersonRemote(
    val id: Long,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val photoUri: String? = null,
    val contactId: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class GroupRemote(
    val id: Long,
    val name: String,
    val icon: String,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long? = null
)

@Serializable
data class GroupMemberRemote(
    val groupId: Long,
    val personId: Long,
    val joinedAt: Long
)

@Serializable
data class ExpenseRemote(
    val id: Long,
    val description: String,
    val amountMinorUnits: Long,
    val date: Long,
    val groupId: Long? = null,
    val payerId: Long,
    val splitMethod: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class ExpenseParticipantRemote(
    val expenseId: Long,
    val personId: Long,
    val shareMinorUnits: Long
)

@Serializable
data class SettlementRemote(
    val id: Long,
    val fromPersonId: Long,
    val toPersonId: Long,
    val amountMinorUnits: Long,
    val groupId: Long? = null,
    val note: String? = null,
    val date: Long,
    val createdAt: Long
)

internal fun Person.toRemote(): PersonRemote {
    return PersonRemote(
        id = id,
        name = name,
        phone = phone,
        email = email,
        photoUri = photoUri,
        contactId = contactId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

internal fun Group.toRemote(): GroupRemote {
    return GroupRemote(
        id = id,
        name = name,
        icon = icon,
        createdAt = createdAt,
        updatedAt = updatedAt,
        archivedAt = archivedAt
    )
}

internal fun GroupMemberEntity.toRemote(): GroupMemberRemote {
    return GroupMemberRemote(
        groupId = groupId,
        personId = personId,
        joinedAt = joinedAt
    )
}

internal fun ExpenseEntity.toRemote(): ExpenseRemote {
    return ExpenseRemote(
        id = id,
        description = description,
        amountMinorUnits = amountMinorUnits,
        date = date,
        groupId = groupId,
        payerId = payerId,
        splitMethod = splitMethod,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

internal fun ExpenseParticipantEntity.toRemote(): ExpenseParticipantRemote {
    return ExpenseParticipantRemote(
        expenseId = expenseId,
        personId = personId,
        shareMinorUnits = shareMinorUnits
    )
}

internal fun SettlementEntity.toRemote(): SettlementRemote {
    return SettlementRemote(
        id = id,
        fromPersonId = fromPersonId,
        toPersonId = toPersonId,
        amountMinorUnits = amountMinorUnits,
        groupId = groupId,
        note = note,
        date = date,
        createdAt = createdAt
    )
}
