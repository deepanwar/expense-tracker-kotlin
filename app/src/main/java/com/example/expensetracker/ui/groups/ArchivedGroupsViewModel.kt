package com.example.expensetracker.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.GroupRepository
import com.example.expensetracker.model.Group
import com.example.expensetracker.model.GroupSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ArchivedGroupsUiState(
    val isLoading: Boolean = true,
    val groups: List<GroupSummary> = emptyList()
)

class ArchivedGroupsViewModel(
    private val groupRepository: GroupRepository
) : ViewModel() {
    val uiState: StateFlow<ArchivedGroupsUiState> = groupRepository.observeArchivedGroups()
        .map { groups -> ArchivedGroupsUiState(isLoading = false, groups = groups) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArchivedGroupsUiState()
        )

    fun restoreGroup(groupId: Long) {
        viewModelScope.launch {
            groupRepository.restoreGroup(groupId)
        }
    }

    fun deleteGroup(group: Group) {
        viewModelScope.launch {
            groupRepository.deleteGroup(group)
        }
    }
}

class ArchivedGroupsViewModelFactory(
    private val groupRepository: GroupRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArchivedGroupsViewModel::class.java)) {
            return ArchivedGroupsViewModel(groupRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
