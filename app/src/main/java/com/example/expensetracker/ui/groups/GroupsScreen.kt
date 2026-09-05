package com.example.expensetracker.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ExpenseTrackerApplication
import com.example.expensetracker.R
import com.example.expensetracker.data.repository.GroupRepository
import com.example.expensetracker.model.GroupSummary
import com.example.expensetracker.model.ImportedContact
import com.example.expensetracker.model.Person
import com.example.expensetracker.ui.components.ScreenLoadingIndicator
import com.example.expensetracker.ui.components.SimpleSearchBar
import com.example.expensetracker.ui.persons.ManualPersonSheet
import com.example.expensetracker.ui.persons.PersonsViewModel
import com.example.expensetracker.ui.persons.PersonsViewModelFactory
import kotlinx.coroutines.launch

private sealed interface GroupsRoute {
    data object List : GroupsRoute
    data object Create : GroupsRoute
    data object Archived : GroupsRoute
    data class Details(val groupId: Long) : GroupsRoute
    data class Edit(val groupId: Long) : GroupsRoute
    data class AddMembers(val groupId: Long?) : GroupsRoute
}

@Composable
fun GroupsScreen(
    addGroupRequestCount: Int,
    modifier: Modifier = Modifier,
    onDetailViewChanged: (Boolean) -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApplication
    val groupsViewModel: GroupsViewModel = viewModel(
        factory = GroupsViewModelFactory(app.groupRepository)
    )
    val createViewModel: CreateGroupViewModel = viewModel(
        factory = CreateGroupViewModelFactory(app.groupRepository, app.personRepository)
    )
    val personsViewModel: PersonsViewModel = viewModel(
        factory = PersonsViewModelFactory(app.personRepository)
    )
    val archivedViewModel: ArchivedGroupsViewModel = viewModel(
        factory = ArchivedGroupsViewModelFactory(app.groupRepository)
    )

    val groupsUiState by groupsViewModel.uiState.collectAsStateWithLifecycle()
    val personsUiState by personsViewModel.uiState.collectAsStateWithLifecycle()
    val persons = personsUiState.persons
    val currentUser by personsViewModel.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var route by remember { mutableStateOf<GroupsRoute>(GroupsRoute.List) }
    var showCreatePerson by remember { mutableStateOf(false) }
    var newlyCreatedPersonId by remember { mutableStateOf<Long?>(null) }
    var lastHandledAddRequestCount by remember { mutableIntStateOf(addGroupRequestCount) }

    LaunchedEffect(addGroupRequestCount) {
        if (addGroupRequestCount > lastHandledAddRequestCount) {
            lastHandledAddRequestCount = addGroupRequestCount
            createViewModel.reset()
            route = GroupsRoute.Create
        }
    }

    LaunchedEffect(route) {
        onDetailViewChanged(route !is GroupsRoute.List)
    }

    when (val current = route) {
        GroupsRoute.List -> {
            GroupsListPane(
                uiState = groupsUiState,
                onGroupClick = { route = GroupsRoute.Details(it) },
                onArchivedClick = { route = GroupsRoute.Archived },
                modifier = modifier
            )
        }

        GroupsRoute.Create -> {
            CreateGroupScreen(
                viewModel = createViewModel,
                persons = persons,
                onBack = { route = GroupsRoute.List },
                onAddMembers = { route = GroupsRoute.AddMembers(groupId = null) },
                onCreated = { groupId -> route = GroupsRoute.Details(groupId) },
                modifier = modifier
            )
        }

        GroupsRoute.Archived -> {
            ArchivedGroupsScreen(
                viewModel = archivedViewModel,
                onBack = { route = GroupsRoute.List },
                modifier = modifier
            )
        }

        is GroupsRoute.Details -> {
            GroupDetailsRoute(
                groupId = current.groupId,
                groupRepository = app.groupRepository,
                currentUser = currentUser,
                onBack = { route = GroupsRoute.List },
                onEdit = { route = GroupsRoute.Edit(current.groupId) },
                onAddMember = { route = GroupsRoute.AddMembers(current.groupId) },
                onArchived = { route = GroupsRoute.List },
                modifier = modifier
            )
        }

        is GroupsRoute.Edit -> {
            EditGroupRoute(
                groupId = current.groupId,
                groupRepository = app.groupRepository,
                onBack = { route = GroupsRoute.Details(current.groupId) },
                modifier = modifier
            )
        }

        is GroupsRoute.AddMembers -> {
            val createState by createViewModel.uiState.collectAsStateWithLifecycle()

            if (current.groupId == null) {
                AddMembersScreen(
                    persons = persons,
                    initiallySelectedIds = createState.selectedMemberIds
                        .minus(setOfNotNull(createState.currentUser?.id)),
                    currentMemberIds = setOfNotNull(createState.currentUser?.id),
                    autoSelectPersonId = newlyCreatedPersonId,
                    onConfirm = { selected ->
                        createViewModel.setSelectedMemberIds(selected)
                        route = GroupsRoute.Create
                    },
                    onBack = { route = GroupsRoute.Create },
                    onCreatePerson = { showCreatePerson = true },
                    modifier = modifier
                )
            } else {
                AddMembersToGroupRoute(
                    groupId = current.groupId,
                    groupRepository = app.groupRepository,
                    persons = persons,
                    currentUser = currentUser,
                    autoSelectPersonId = newlyCreatedPersonId,
                    onConfirm = { route = GroupsRoute.Details(current.groupId) },
                    onBack = { route = GroupsRoute.Details(current.groupId) },
                    onCreatePerson = { showCreatePerson = true },
                    modifier = modifier
                )
            }
        }
    }

    if (showCreatePerson) {
        ManualPersonSheet(
            onDismiss = { showCreatePerson = false },
            onSave = { name, phone, email ->
                scope.launch {
                    val id = personsViewModel.addPersonReturningId(
                        ImportedContact(name = name, phone = phone, email = email)
                    )
                    newlyCreatedPersonId = id
                    showCreatePerson = false
                }
            }
        )
    }
}

@Composable
private fun GroupsListPane(
    uiState: GroupsUiState,
    onGroupClick: (Long) -> Unit,
    onArchivedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchTextFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val filteredGroups = remember(uiState.groups, searchTextFieldState.text) {
        uiState.groups.filterByQuery(searchTextFieldState.text.toString())
    }

    Column(modifier = modifier.fillMaxSize()) {
        SimpleSearchBar(
            textFieldState = searchTextFieldState,
            searchBarState = searchBarState,
            placeholder = stringResource(R.string.search_groups),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            GroupListContent(
                isLoading = uiState.isLoading,
                groups = uiState.groups,
                filteredGroups = filteredGroups,
                onGroupClick = { groupId ->
                    scope.launch {
                        searchBarState.animateToCollapsed()
                        onGroupClick(groupId)
                    }
                }
            )
        }

        GroupListContent(
            isLoading = uiState.isLoading,
            groups = uiState.groups,
            filteredGroups = filteredGroups,
            onGroupClick = onGroupClick,
            onArchivedClick = onArchivedClick,
            showHeader = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GroupListContent(
    isLoading: Boolean,
    groups: List<GroupSummary>,
    filteredGroups: List<GroupSummary>,
    onGroupClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onArchivedClick: (() -> Unit)? = null,
    showHeader: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        if (showHeader) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (groups.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.my_groups),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.more_actions)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.archived_groups)) },
                            onClick = {
                                showMenu = false
                                onArchivedClick?.invoke()
                            }
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    ScreenLoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }

                groups.isEmpty() -> {
                    EmptyGroupsState(modifier = Modifier.align(Alignment.Center))
                }

                filteredGroups.isEmpty() -> {
                    EmptyGroupSearchState(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    GroupList(
                        groups = filteredGroups,
                        onGroupClick = { summary -> onGroupClick(summary.group.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGroupsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "👥", style = MaterialTheme.typography.displaySmall)
        Text(
            text = stringResource(R.string.no_groups_yet),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.no_groups_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyGroupSearchState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.no_groups_found),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.no_groups_found_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GroupDetailsRoute(
    groupId: Long,
    groupRepository: GroupRepository,
    currentUser: Person?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddMember: () -> Unit,
    onArchived: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GroupDetailsViewModel = viewModel(
        key = "group-$groupId",
        factory = GroupDetailsViewModelFactory(groupId, groupRepository)
    )
    GroupDetailsScreen(
        viewModel = viewModel,
        currentUser = currentUser,
        onBack = onBack,
        onEdit = onEdit,
        onAddMember = onAddMember,
        onArchived = onArchived,
        modifier = modifier
    )
}

@Composable
private fun EditGroupRoute(
    groupId: Long,
    groupRepository: GroupRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GroupDetailsViewModel = viewModel(
        key = "group-$groupId",
        factory = GroupDetailsViewModelFactory(groupId, groupRepository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val group = uiState.groupWithMembers?.group

    if (group == null) {
        if (!uiState.isLoading) {
            LaunchedEffect(Unit) { onBack() }
        }
        return
    }

    EditGroupScreen(
        group = group,
        onBack = onBack,
        onSave = { updated ->
            viewModel.updateGroup(updated)
            onBack()
        },
        modifier = modifier
    )
}

@Composable
private fun AddMembersToGroupRoute(
    groupId: Long,
    groupRepository: GroupRepository,
    persons: List<Person>,
    currentUser: Person?,
    autoSelectPersonId: Long?,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    onCreatePerson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GroupDetailsViewModel = viewModel(
        key = "group-$groupId",
        factory = GroupDetailsViewModelFactory(groupId, groupRepository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentMemberIds = uiState.groupWithMembers?.members
        ?.map { it.id }
        ?.toSet()
        .orEmpty()
        .plus(setOfNotNull(currentUser?.id))

    AddMembersScreen(
        persons = persons,
        initiallySelectedIds = emptySet(),
        currentMemberIds = currentMemberIds,
        autoSelectPersonId = autoSelectPersonId,
        onConfirm = { selected ->
            viewModel.addMembers(selected)
            onConfirm()
        },
        onBack = onBack,
        onCreatePerson = onCreatePerson,
        modifier = modifier
    )
}

private fun List<GroupSummary>.filterByQuery(query: String): List<GroupSummary> {
    val needle = query.trim()
    if (needle.isEmpty()) return this
    return filter { it.group.name.contains(needle, ignoreCase = true) }
}
