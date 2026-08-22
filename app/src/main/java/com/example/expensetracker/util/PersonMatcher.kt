package com.example.expensetracker.util

import com.example.expensetracker.model.ImportedContact
import com.example.expensetracker.model.Person

object PersonMatcher {
    fun findExistingPerson(contact: ImportedContact, existingPeople: List<Person>): Person? {
        val normalizedPhone = contact.phone?.let(::normalizePhone)
        val normalizedEmail = contact.email?.let(::normalizeEmail)

        if (normalizedPhone == null && normalizedEmail == null && contact.contactId == null) {
            return null
        }

        contact.contactId?.let { contactId ->
            existingPeople.firstOrNull { it.contactId == contactId }?.let { return it }
        }

        return existingPeople.firstOrNull { person ->
            val personPhone = person.phone?.let(::normalizePhone)
            val personEmail = person.email?.let(::normalizeEmail)

            (normalizedPhone != null && personPhone == normalizedPhone) ||
                (normalizedEmail != null && personEmail == normalizedEmail)
        }
    }

    fun normalizePhone(phone: String): String {
        return phone.filter { it.isDigit() }.takeLast(10)
    }

    fun normalizePhoneForStorage(phone: String): String {
        return normalizePhone(phone)
    }

    fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }

    fun normalizeEmailForStorage(email: String): String {
        return normalizeEmail(email)
    }
}
