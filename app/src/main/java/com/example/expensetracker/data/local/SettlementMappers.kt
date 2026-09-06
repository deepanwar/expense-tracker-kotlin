package com.example.expensetracker.data.local

import com.example.expensetracker.data.local.entity.SettlementEntity
import com.example.expensetracker.data.local.entity.SettlementWithDetails
import com.example.expensetracker.model.Settlement
import com.example.expensetracker.model.SettlementDetails

internal fun SettlementEntity.toDomain(): Settlement {
    return Settlement(
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

internal fun SettlementWithDetails.toDomain(): SettlementDetails {
    return SettlementDetails(
        settlement = settlement.toDomain(),
        fromPerson = fromPerson.toDomain(),
        toPerson = toPerson.toDomain(),
        group = group?.toDomain()
    )
}
