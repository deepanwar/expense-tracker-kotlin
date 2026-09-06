package com.example.expensetracker.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.PersonRepository
import com.example.expensetracker.model.ExpenseDetails
import com.example.expensetracker.model.OverallBalance
import com.example.expensetracker.util.BalanceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExpensesUiState(
    val isLoading: Boolean = true,
    val expenses: List<ExpenseDetails> = emptyList(),
    val overallBalance: OverallBalance = OverallBalance(0, 0, 0)
)

class ExpensesViewModel(
    expenseRepository: ExpenseRepository,
    personRepository: PersonRepository
) : ViewModel() {
    val uiState: StateFlow<ExpensesUiState> = combine(
        expenseRepository.observeAllExpenses(),
        personRepository.observeCurrentUser()
    ) { expenses, user ->
        val listExpenses = if (user == null) {
            emptyList()
        } else {
            expenses.filter { it.expense.payerId == user.id }
        }
        val overall = if (user == null) {
            OverallBalance(0, 0, 0)
        } else {
            BalanceCalculator.calculateOverallBalance(expenses, user.id)
        }
        ExpensesUiState(
            isLoading = false,
            expenses = listExpenses,
            overallBalance = overall
        )
    }.stateIn(
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
