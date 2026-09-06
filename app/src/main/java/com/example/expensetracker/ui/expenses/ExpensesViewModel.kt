package com.example.expensetracker.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.PersonRepository
import com.example.expensetracker.model.ExpenseDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExpensesUiState(
    val isLoading: Boolean = true,
    val expenses: List<ExpenseDetails> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesViewModel(
    expenseRepository: ExpenseRepository,
    personRepository: PersonRepository
) : ViewModel() {
    val uiState: StateFlow<ExpensesUiState> = personRepository.observeCurrentUser()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                expenseRepository.observeForPerson(user.id)
            }
        }
        .map { expenses -> ExpensesUiState(isLoading = false, expenses = expenses) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExpensesUiState()
        )

    init {
        viewModelScope.launch {
            personRepository.ensureCurrentUser()
        }
    }
}

class ExpensesViewModelFactory(
    private val expenseRepository: ExpenseRepository,
    private val personRepository: PersonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpensesViewModel::class.java)) {
            return ExpensesViewModel(expenseRepository, personRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
