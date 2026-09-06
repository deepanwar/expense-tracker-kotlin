package com.example.expensetracker.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.GroupRepository
import com.example.expensetracker.data.repository.SettlementRepository
import com.example.expensetracker.model.ExpenseDetails
import com.example.expensetracker.model.Group
import com.example.expensetracker.model.GroupBalance
import com.example.expensetracker.model.GroupWithMembers
import com.example.expensetracker.model.SettlementDetails
import com.example.expensetracker.model.isCurrentUser
import com.example.expensetracker.util.BalanceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupDetailsUiState(
    val isLoading: Boolean = true,
    val groupWithMembers: GroupWithMembers? = null,
    val expenses: List<ExpenseDetails> = emptyList(),
    val settlements: List<SettlementDetails> = emptyList(),
    val groupBalance: GroupBalance? = null
)

class GroupDetailsViewModel(
    private val groupId: Long,
    private val groupRepository: GroupRepository,
    expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository
) : ViewModel() {
    val uiState: StateFlow<GroupDetailsUiState> = combine(
        groupRepository.observeGroupWithMembers(groupId),
        expenseRepository.observeByGroup(groupId),
        settlementRepository.observeByGroup(groupId)
    ) { group, expenses, settlements ->
        val currentUserId = group?.members?.firstOrNull { it.isCurrentUser() }?.id
            ?: expenses.firstNotNullOfOrNull { details ->
                when {
                    details.payer.isCurrentUser() -> details.payer.id
                    else -> details.participants.firstOrNull { it.person.isCurrentUser() }?.person?.id
                }
            }
        val groupBalance = if (group == null || currentUserId == null) {
            null
        } else {
            BalanceCalculator.calculateGroupBalance(
                expenses = expenses,
                settlements = settlements,
                currentUserId = currentUserId,
                groupId = groupId,
                extraPeople = group.members
            )
        }
        GroupDetailsUiState(
            isLoading = false,
            groupWithMembers = group,
            expenses = expenses,
            settlements = settlements,
            groupBalance = groupBalance
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupDetailsUiState()
    )

    fun recordSettlement(
        fromPersonId: Long,
        toPersonId: Long,
        amountMinorUnits: Long,
        note: String?,
        date: Long
    ) {
        viewModelScope.launch {
            settlementRepository.recordSettlement(
                fromPersonId = fromPersonId,
                toPersonId = toPersonId,
                amountMinorUnits = amountMinorUnits,
                groupId = groupId,
                note = note,
                date = date
            )
        }
    }

    fun addMembers(personIds: Collection<Long>) {
        viewModelScope.launch {
            groupRepository.addMembers(groupId, personIds)
        }
    }

    fun removeMember(personId: Long) {
        viewModelScope.launch {
            groupRepository.removeMember(groupId, personId)
        }
    }

    fun archiveGroup() {
        viewModelScope.launch {
            groupRepository.archiveGroup(groupId)
        }
    }

    fun updateGroup(group: Group) {
        viewModelScope.launch {
            groupRepository.updateGroup(group)
        }
    }
}

class GroupDetailsViewModelFactory(
    private val groupId: Long,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupDetailsViewModel::class.java)) {
            return GroupDetailsViewModel(
                groupId,
                groupRepository,
                expenseRepository,
                settlementRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
