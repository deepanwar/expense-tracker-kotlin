package com.example.expensetracker.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.GroupRepository
import com.example.expensetracker.data.repository.PersonRepository
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.Group
import com.example.expensetracker.model.GroupSummary
import com.example.expensetracker.model.GroupWithMembers
import com.example.expensetracker.model.Person
import com.example.expensetracker.util.Money
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ExpenseFormMode {
    data class Add(val groupId: Long? = null) : ExpenseFormMode
    data class View(val expenseId: Long) : ExpenseFormMode
    data class Edit(val expenseId: Long) : ExpenseFormMode
}

data class AddEditExpenseUiState(
    val isLoading: Boolean = true,
    val isReadOnly: Boolean = false,
    val groupLocked: Boolean = false,
    val description: String = "",
    val amountText: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val groupId: Long? = null,
    val payerId: Long? = null,
    val selectedParticipantIds: Set<Long> = emptySet(),
    val currentUser: Person? = null,
    val persons: List<Person> = emptyList(),
    val groups: List<GroupSummary> = emptyList(),
    val selectedGroup: GroupWithMembers? = null,
    val lockedGroup: Group? = null,
    val existingExpense: Expense? = null,
    val descriptionError: Boolean = false,
    val amountError: Boolean = false,
    val participantsError: Boolean = false
)

class AddEditExpenseViewModel(
    private val mode: ExpenseFormMode,
    private val expenseRepository: ExpenseRepository,
    private val personRepository: PersonRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AddEditExpenseUiState(isReadOnly = mode is ExpenseFormMode.View)
    )
    val uiState: StateFlow<AddEditExpenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value.take(200), descriptionError = false) }
    }

    fun updateAmount(value: String) {
        _uiState.update { it.copy(amountText = value.take(16), amountError = false) }
    }

    fun updateDate(millis: Long) {
        _uiState.update { it.copy(dateMillis = millis) }
    }

    fun selectGroup(groupId: Long?) {
        viewModelScope.launch {
            applyGroupSelection(groupId, resetParticipants = true)
        }
    }

    fun selectPayer(personId: Long) {
        _uiState.update { it.copy(payerId = personId) }
    }

    fun toggleParticipant(personId: Long) {
        _uiState.update { state ->
            val next = if (personId in state.selectedParticipantIds) {
                state.selectedParticipantIds - personId
            } else {
                state.selectedParticipantIds + personId
            }
            state.copy(selectedParticipantIds = next, participantsError = false)
        }
    }

    fun save(onSaved: (Long) -> Unit) {
        val state = _uiState.value
        val description = state.description.trim()
        val amount = Money.parseRupeesToPaise(state.amountText)
        val payerId = state.payerId
        val participants = availableParticipantIds(state)
            .intersect(state.selectedParticipantIds)

        var hasError = false
        if (description.isEmpty()) {
            _uiState.update { it.copy(descriptionError = true) }
            hasError = true
        }
        if (amount == null || amount <= 0L) {
            _uiState.update { it.copy(amountError = true) }
            hasError = true
        }
        if (participants.isEmpty()) {
            _uiState.update { it.copy(participantsError = true) }
            hasError = true
        }
        if (hasError || payerId == null || amount == null) return

        viewModelScope.launch {
            val expenseId = when (val current = mode) {
                is ExpenseFormMode.Add -> expenseRepository.create(
                    description = description,
                    amountMinorUnits = amount,
                    date = state.dateMillis,
                    groupId = state.groupId,
                    payerId = payerId,
                    participantIds = participants,
                    currentUserId = state.currentUser?.id
                )
                is ExpenseFormMode.Edit -> {
                    val existing = state.existingExpense ?: return@launch
                    expenseRepository.update(
                        expense = existing.copy(
                            description = description,
                            amountMinorUnits = amount,
                            date = state.dateMillis,
                            groupId = state.groupId,
                            payerId = payerId
                        ),
                        participantIds = participants,
                        currentUserId = state.currentUser?.id
                    )
                    current.expenseId
                }
                is ExpenseFormMode.View -> return@launch
            }
            onSaved(expenseId)
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val expense = _uiState.value.existingExpense ?: return
        viewModelScope.launch {
            expenseRepository.delete(expense)
            onDeleted()
        }
    }

    private suspend fun load() {
        val currentUser = personRepository.ensureCurrentUser()
        val persons = personRepository.observeAllPersons().first()
        val groups = groupRepository.observeActiveGroups().first()

        when (val current = mode) {
            is ExpenseFormMode.Add -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = currentUser,
                        persons = persons,
                        groups = groups,
                        groupLocked = current.groupId != null,
                        payerId = currentUser.id,
                        selectedParticipantIds = setOf(currentUser.id)
                    )
                }
                if (current.groupId != null) {
                    applyGroupSelection(current.groupId, resetParticipants = true)
                }
            }

            is ExpenseFormMode.View,
            is ExpenseFormMode.Edit -> {
                val expenseId = when (current) {
                    is ExpenseFormMode.View -> current.expenseId
                    is ExpenseFormMode.Edit -> current.expenseId
                    is ExpenseFormMode.Add -> return
                }
                val details = expenseRepository.observeById(expenseId).first() ?: return
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = currentUser,
                        persons = persons,
                        groups = groups,
                        existingExpense = details.expense,
                        description = details.expense.description,
                        amountText = Money.paiseToInput(details.expense.amountMinorUnits),
                        dateMillis = details.expense.date,
                        groupId = details.expense.groupId,
                        lockedGroup = details.group,
                        payerId = details.expense.payerId,
                        selectedParticipantIds = details.participants.map { row -> row.personId }.toSet()
                    )
                }
                applyGroupSelection(details.expense.groupId, resetParticipants = false)
            }
        }
    }

    private suspend fun applyGroupSelection(groupId: Long?, resetParticipants: Boolean) {
        val group = groupId?.let { groupRepository.observeGroupWithMembers(it).first() }
        _uiState.update { state ->
            val poolIds = participantPool(state.copy(groupId = groupId, selectedGroup = group))
                .map { it.id }
                .toSet()
            val payerId = state.payerId?.takeIf { it in poolIds }
                ?: state.currentUser?.id?.takeIf { it in poolIds }
                ?: poolIds.firstOrNull()
            val selected = when {
                !resetParticipants -> state.selectedParticipantIds.intersect(poolIds).ifEmpty {
                    defaultParticipantIds(group, state.currentUser)
                }
                else -> defaultParticipantIds(group, state.currentUser)
            }
            state.copy(
                groupId = groupId,
                selectedGroup = group,
                lockedGroup = group?.group ?: state.lockedGroup,
                payerId = payerId,
                selectedParticipantIds = selected
            )
        }
    }

    companion object {
        fun participantPool(state: AddEditExpenseUiState): List<Person> {
            val members = state.selectedGroup?.members.orEmpty()
            val source = if (state.groupId != null) members else state.persons
            val you = state.currentUser
            val others = source.filter { it.id != you?.id }.sortedBy { it.name.lowercase() }
            return listOfNotNull(you) + others
        }

        fun defaultParticipantIds(group: GroupWithMembers?, currentUser: Person?): Set<Long> {
            return if (group != null) {
                group.members.map { it.id }.toSet() + setOfNotNull(currentUser?.id)
            } else {
                setOfNotNull(currentUser?.id)
            }
        }

        fun availableParticipantIds(state: AddEditExpenseUiState): Set<Long> {
            return participantPool(state).map { it.id }.toSet()
        }
    }
}

class AddEditExpenseViewModelFactory(
    private val mode: ExpenseFormMode,
    private val expenseRepository: ExpenseRepository,
    private val personRepository: PersonRepository,
    private val groupRepository: GroupRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditExpenseViewModel::class.java)) {
            return AddEditExpenseViewModel(
                mode,
                expenseRepository,
                personRepository,
                groupRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

fun ExpenseFormMode.viewModelKey(): String {
    return when (this) {
        is ExpenseFormMode.Add -> "expense-add-${groupId}"
        is ExpenseFormMode.View -> "expense-view-$expenseId"
        is ExpenseFormMode.Edit -> "expense-edit-$expenseId"
    }
}
