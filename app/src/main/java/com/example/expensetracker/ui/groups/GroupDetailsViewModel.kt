package com.example.expensetracker.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.GroupRepository
import com.example.expensetracker.model.Group
import com.example.expensetracker.model.GroupWithMembers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupDetailsUiState(
    val isLoading: Boolean = true,
    val groupWithMembers: GroupWithMembers? = null
)

class GroupDetailsViewModel(
    private val groupId: Long,
    private val groupRepository: GroupRepository
) : ViewModel() {
    val uiState: StateFlow<GroupDetailsUiState> = groupRepository.observeGroupWithMembers(groupId)
        .map { group -> GroupDetailsUiState(isLoading = false, groupWithMembers = group) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GroupDetailsUiState()
        )

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
    private val groupRepository: GroupRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupDetailsViewModel::class.java)) {
            return GroupDetailsViewModel(groupId, groupRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
