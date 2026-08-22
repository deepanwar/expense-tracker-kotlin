package com.example.expensetracker.util

import com.example.expensetracker.model.ImportedContact
import com.example.expensetracker.model.Person
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonMatcherTest {
    @Test
    fun matchesByPhoneNumber() {
        val existing = Person(id = 1, name = "Rahul Sharma", phone = "9876543210", createdAt = 1, updatedAt = 1)
        val contact = ImportedContact(name = "Rahul S", phone = "+91 98765 43210")

        val match = PersonMatcher.findExistingPerson(contact, listOf(existing))

        assertEquals(existing, match)
    }

    @Test
    fun matchesByEmail() {
        val existing = Person(id = 1, name = "Rahul Sharma", email = "rahul@example.com", createdAt = 1, updatedAt = 1)
        val contact = ImportedContact(name = "Rahul", email = "Rahul@Example.com")

        val match = PersonMatcher.findExistingPerson(contact, listOf(existing))

        assertEquals(existing, match)
    }

    @Test
    fun matchesByContactId() {
        val existing = Person(id = 1, name = "Rahul Sharma", contactId = "42", createdAt = 1, updatedAt = 1)
        val contact = ImportedContact(name = "Rahul Sharma", contactId = "42")

        val match = PersonMatcher.findExistingPerson(contact, listOf(existing))

        assertEquals(existing, match)
    }

    @Test
    fun doesNotMatchByNameAlone() {
        val existing = Person(id = 1, name = "Rahul Sharma", createdAt = 1, updatedAt = 1)
        val contact = ImportedContact(name = "Rahul Sharma")

        val match = PersonMatcher.findExistingPerson(contact, listOf(existing))

        assertNull(match)
    }
}
