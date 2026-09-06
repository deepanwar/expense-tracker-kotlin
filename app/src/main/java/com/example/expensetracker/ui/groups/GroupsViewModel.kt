package com.example.expensetracker.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.GroupRepository
import com.example.expensetracker.data.repository.PersonRepository
import com.example.expensetracker.model.Group
import com.example.expensetracker.model.GroupBalance
import com.example.expensetracker.model.GroupSummary
import com.example.expensetracker.util.BalanceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupsUiState(
    val isLoading: Boolean = true,
    val groups: List<GroupSummary> = emptyList(),
    val groupBalances: Map<Long, GroupBalance> = emptyMap()
)

class GroupsViewModel(
    private val groupRepository: GroupRepository,
    expenseRepository: ExpenseRepository,
    personRepository: PersonRepository
) : ViewModel() {
    val uiState: StateFlow<GroupsUiState> = combine(
        groupRepository.observeActiveGroups(),
        expenseRepository.observeAllExpenses(),
        personRepository.observeCurrentUser()
    ) { groups, expenses, user ->
        val balances = if (user == null) {
            emptyMap()
        } else {
            groups.associate { summary ->
                summary.group.id to BalanceCalculator.calculateGroupBalance(
                    expenses = expenses,
                    currentUserId = user.id,
                    groupId = summary.group.id
                )
            }
        }
        GroupsUiState(
            isLoading = false,
            groups = groups,
            groupBalances = balances
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupsUiState()
    )

    fun archiveGroup(groupId: Long) {
        viewModelScope.launch {
            groupRepository.archiveGroup(groupId)
        }
    }

    fun deleteGroup(group: Group) {
        viewModelScope.launch {
            groupRepository.deleteGroup(group)
        }
    }
}

class GroupsViewModelFactory(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val personRepository: PersonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupsViewModel::class.java)) {
            return GroupsViewModel(groupRepository, expenseRepository, personRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
