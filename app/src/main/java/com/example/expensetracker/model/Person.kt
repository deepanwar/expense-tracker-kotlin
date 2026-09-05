package com.example.expensetracker.model

data class Person(
    val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val photoUri: String? = null,
    val contactId: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

const val CURRENT_USER_CONTACT_ID = "current_user"

fun Person.isCurrentUser(): Boolean = contactId == CURRENT_USER_CONTACT_ID

data class ImportedContact(
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val photoUri: String? = null,
    val contactId: String? = null
)
