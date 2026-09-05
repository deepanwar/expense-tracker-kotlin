package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.dao.PersonDao
import com.example.expensetracker.data.local.toDomain
import com.example.expensetracker.data.local.toEntity
import com.example.expensetracker.model.CURRENT_USER_CONTACT_ID
import com.example.expensetracker.model.ImportedContact
import com.example.expensetracker.model.Person
import com.example.expensetracker.model.isCurrentUser
import com.example.expensetracker.util.PersonMatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PersonRepository(
    private val personDao: PersonDao
) {
    fun observeAllPersons(): Flow<List<Person>> {
        return personDao.observeAll().map { persons -> persons.map { it.toDomain() } }
    }

    fun observeOtherPersons(): Flow<List<Person>> {
        return observeAllPersons().map { persons -> persons.filter { !it.isCurrentUser() } }
    }

    fun observeCurrentUser(): Flow<Person?> {
        return observeAllPersons().map { persons -> persons.firstOrNull { it.isCurrentUser() } }
    }

    suspend fun ensureCurrentUser(): Person {
        findByContactId(CURRENT_USER_CONTACT_ID)?.let { return it }
        val now = System.currentTimeMillis()
        val id = insertPerson(
            Person(
                name = "You",
                contactId = CURRENT_USER_CONTACT_ID,
                createdAt = now,
                updatedAt = now
            )
        )
        return Person(
            id = id,
            name = "You",
            contactId = CURRENT_USER_CONTACT_ID,
            createdAt = now,
            updatedAt = now
        )
    }

    fun observePersonById(id: Long): Flow<Person?> {
        return personDao.observeById(id).map { entity -> entity?.toDomain() }
    }

    fun searchPersonsByName(query: String): Flow<List<Person>> {
        return personDao.searchByName(query).map { persons -> persons.map { it.toDomain() } }
    }

    suspend fun insertPerson(person: Person): Long {
        val now = System.currentTimeMillis()
        val normalized = person.copy(
            phone = person.phone?.let(PersonMatcher::normalizePhoneForStorage),
            email = person.email?.let(PersonMatcher::normalizeEmailForStorage),
            createdAt = if (person.createdAt > 0) person.createdAt else now,
            updatedAt = now
        )
        return personDao.insert(normalized.toEntity())
    }

    suspend fun updatePerson(person: Person) {
        val now = System.currentTimeMillis()
        val normalized = person.copy(
            phone = person.phone?.let(PersonMatcher::normalizePhoneForStorage),
            email = person.email?.let(PersonMatcher::normalizeEmailForStorage),
            updatedAt = now
        )
        personDao.update(normalized.toEntity())
    }

    suspend fun deletePerson(person: Person) {
        personDao.delete(person.toEntity())
    }

    suspend fun findByPhone(phone: String): Person? {
        val normalized = PersonMatcher.normalizePhoneForStorage(phone)
        return personDao.findByPhone(normalized)?.toDomain()
    }

    suspend fun findByEmail(email: String): Person? {
        val normalized = PersonMatcher.normalizeEmailForStorage(email)
        return personDao.findByEmail(normalized)?.toDomain()
    }

    suspend fun findByContactId(contactId: String): Person? {
        return personDao.findByContactId(contactId)?.toDomain()
    }

    suspend fun findExistingForImport(contact: ImportedContact): Person? {
        contact.contactId?.let { findByContactId(it) }?.let { return it }

        contact.email?.let { email ->
            findByEmail(email)?.let { return it }
        }

        contact.phone?.let { phone ->
            findByPhone(phone)?.let { return it }

            val normalizedPhone = PersonMatcher.normalizePhone(phone)
            personDao.getAllWithPhone().firstOrNull { entity ->
                PersonMatcher.normalizePhone(entity.phone!!) == normalizedPhone
            }?.let { return it.toDomain() }
        }

        return null
    }

    suspend fun insertFromImport(contact: ImportedContact): Long {
        val now = System.currentTimeMillis()
        return insertPerson(
            Person(
                name = contact.name.trim(),
                phone = contact.phone,
                email = contact.email,
                photoUri = contact.photoUri,
                contactId = contact.contactId,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
