package com.example.expensetracker.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.GroupRepository
import com.example.expensetracker.data.repository.PersonRepository
import com.example.expensetracker.model.Person
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateGroupUiState(
    val name: String = "",
    val icon: String = GroupDefaults.DEFAULT_ICON,
    val currentUser: Person? = null,
    val selectedMemberIds: Set<Long> = emptySet(),
    val nameError: Boolean = false
)

class CreateGroupViewModel(
    private val groupRepository: GroupRepository,
    private val personRepository: PersonRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    init {
        includeCurrentUser()
    }

    fun reset() {
        _uiState.value = CreateGroupUiState()
        includeCurrentUser()
    }

    fun updateName(name: String) {
        _uiState.update { state ->
            state.copy(
                name = name.take(GroupDefaults.MAX_NAME_LENGTH),
                nameError = false
            )
        }
    }

    fun updateIcon(icon: String) {
        _uiState.update { it.copy(icon = icon) }
    }

    fun setSelectedMemberIds(memberIds: Set<Long>) {
        val currentUserId = _uiState.value.currentUser?.id
        _uiState.update { state ->
            state.copy(
                selectedMemberIds = if (currentUserId != null) {
                    memberIds + currentUserId
                } else {
                    memberIds
                }
            )
        }
    }

    fun create(onCreated: (Long) -> Unit) {
        val trimmed = _uiState.value.name.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(nameError = true) }
            return
        }

        viewModelScope.launch {
            val currentUserId = _uiState.value.currentUser?.id
            val memberIds = if (currentUserId != null) {
                _uiState.value.selectedMemberIds + currentUserId
            } else {
                _uiState.value.selectedMemberIds
            }
            val groupId = groupRepository.createGroup(
                name = trimmed,
                icon = _uiState.value.icon,
                memberIds = memberIds
            )
            onCreated(groupId)
        }
    }

    private fun includeCurrentUser() {
        viewModelScope.launch {
            val currentUser = personRepository.ensureCurrentUser()
            _uiState.update { state ->
                state.copy(
                    currentUser = currentUser,
                    selectedMemberIds = state.selectedMemberIds + currentUser.id
                )
            }
        }
    }
}

class CreateGroupViewModelFactory(
    private val groupRepository: GroupRepository,
    private val personRepository: PersonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateGroupViewModel::class.java)) {
            return CreateGroupViewModel(groupRepository, personRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
