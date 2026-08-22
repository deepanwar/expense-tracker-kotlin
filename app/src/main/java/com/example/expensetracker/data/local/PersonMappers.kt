package com.example.expensetracker.data.local

import com.example.expensetracker.data.local.entity.PersonEntity
import com.example.expensetracker.model.Person

internal fun PersonEntity.toDomain(): Person {
    return Person(
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

internal fun Person.toEntity(): PersonEntity {
    return PersonEntity(
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
