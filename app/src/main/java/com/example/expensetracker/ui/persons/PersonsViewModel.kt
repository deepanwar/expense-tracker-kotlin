package com.example.expensetracker.ui.persons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.PersonRepository
import com.example.expensetracker.model.ImportedContact
import com.example.expensetracker.model.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PersonsViewModel(
    private val personRepository: PersonRepository
) : ViewModel() {
    val persons: StateFlow<List<Person>> = personRepository.observeAllPersons()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun observePerson(id: Long): Flow<Person?> {
        return personRepository.observePersonById(id)
    }

    fun updatePerson(person: Person) {
        viewModelScope.launch {
            personRepository.updatePerson(person)
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            personRepository.deletePerson(person)
        }
    }

    fun addPersonFromImport(contact: ImportedContact) {
        viewModelScope.launch {
            personRepository.insertFromImport(contact)
        }
    }

    fun addPersonManually(name: String, phone: String?, email: String?) {
        viewModelScope.launch {
            personRepository.insertFromImport(
                ImportedContact(
                    name = name,
                    phone = phone,
                    email = email
                )
            )
        }
    }

    suspend fun findExistingForImport(
        contact: ImportedContact,
        excludePersonId: Long? = null
    ): Person? {
        val existing = personRepository.findExistingForImport(contact)
        return if (existing?.id == excludePersonId) null else existing
    }
}

class PersonsViewModelFactory(
    private val personRepository: PersonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonsViewModel::class.java)) {
            return PersonsViewModel(personRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
