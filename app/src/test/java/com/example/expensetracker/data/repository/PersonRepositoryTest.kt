package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.dao.PersonDao
import com.example.expensetracker.data.local.entity.PersonEntity
import com.example.expensetracker.model.ImportedContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PersonRepositoryTest {
    @Test
    fun findExistingForImportMatchesByContactId() = runTest {
        val dao = FakePersonDao(
            persons = listOf(
                PersonEntity(
                    id = 1,
                    name = "Rahul Sharma",
                    contactId = "42",
                    createdAt = 1,
                    updatedAt = 1
                )
            )
        )
        val repository = PersonRepository(dao)

        val match = repository.findExistingForImport(
            ImportedContact(name = "Rahul", contactId = "42")
        )

        assertNotNull(match)
        assertEquals(1L, match?.id)
    }

    @Test
    fun findExistingForImportDoesNotMatchByNameOnly() = runTest {
        val dao = FakePersonDao(
            persons = listOf(
                PersonEntity(
                    id = 1,
                    name = "Rahul Sharma",
                    createdAt = 1,
                    updatedAt = 1
                )
            )
        )
        val repository = PersonRepository(dao)

        val match = repository.findExistingForImport(
            ImportedContact(name = "Rahul Sharma")
        )

        assertNull(match)
    }
}

private class FakePersonDao(
    private val persons: List<PersonEntity>
) : PersonDao {
    override fun observeAll(): Flow<List<PersonEntity>> = flowOf(persons)

    override fun observeById(id: Long): Flow<PersonEntity?> =
        flowOf(persons.firstOrNull { it.id == id })

    override suspend fun insert(person: PersonEntity): Long = person.id

    override suspend fun update(person: PersonEntity) = Unit

    override suspend fun delete(person: PersonEntity) = Unit

    override suspend fun findByPhone(phone: String): PersonEntity? =
        persons.firstOrNull { it.phone == phone }

    override suspend fun findByEmail(email: String): PersonEntity? =
        persons.firstOrNull { it.email.equals(email, ignoreCase = true) }

    override suspend fun findByContactId(contactId: String): PersonEntity? =
        persons.firstOrNull { it.contactId == contactId }

    override fun searchByName(query: String): Flow<List<PersonEntity>> =
        flowOf(persons.filter { it.name.contains(query, ignoreCase = true) })

    override suspend fun getAllWithPhone(): List<PersonEntity> =
        persons.filter { it.phone != null }
}
