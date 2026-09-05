package com.example.expensetracker.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.model.Person
import com.example.expensetracker.model.isCurrentUser
import com.example.expensetracker.ui.components.ScreenLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    viewModel: GroupDetailsViewModel,
    currentUser: Person?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddMember: () -> Unit,
    onArchived: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var memberToRemove by remember { mutableStateOf<Person?>(null) }
    var viewedPerson by remember { mutableStateOf<Person?>(null) }

    val groupWithMembers = uiState.groupWithMembers
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    LaunchedEffect(uiState.isLoading, groupWithMembers) {
        if (!uiState.isLoading && groupWithMembers == null) {
            onBack()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = buildString {
                            groupWithMembers?.let { append("${it.group.icon} ${it.group.name}") }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
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
                            text = { Text(stringResource(R.string.edit_group)) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.archive_group)) },
                            onClick = {
                                showMenu = false
                                showArchiveDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading || groupWithMembers == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    ScreenLoadingIndicator()
                }
            }

            else -> {
                val members = remember(groupWithMembers.members, currentUser) {
                    groupWithMembers.members.withCurrentUserFirst(currentUser)
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.members_with_count, members.size),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                    items(members, key = { it.id }) { person ->
                        MemberListItem(
                            person = person,
                            onClick = { viewedPerson = person },
                            onMoreClick = if (person.isCurrentUser()) {
                                null
                            } else {
                                { memberToRemove = person }
                            }
                        )
                    }
                    item {
                        TextButton(
                            onClick = onAddMember,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.add_member))
                        }
                    }
                }
            }
        }
    }

    if (showArchiveDialog && groupWithMembers != null) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = {
                Text(text = stringResource(R.string.archive_group_title, groupWithMembers.group.name))
            },
            text = {
                Text(text = stringResource(R.string.archive_group_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.archiveGroup()
                        showArchiveDialog = false
                        onArchived()
                    }
                ) {
                    Text(text = stringResource(R.string.archive))
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    memberToRemove?.let { person ->
        AlertDialog(
            onDismissRequest = { memberToRemove = null },
            title = {
                Text(text = stringResource(R.string.remove_member, person.name))
            },
            text = {
                Text(text = stringResource(R.string.remove_member_message, person.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeMember(person.id)
                        memberToRemove = null
                    }
                ) {
                    Text(text = stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToRemove = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    viewedPerson?.let { person ->
        AlertDialog(
            onDismissRequest = { viewedPerson = null },
            title = {
                Text(
                    text = if (person.isCurrentUser()) {
                        stringResource(R.string.you)
                    } else {
                        person.name
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = person.phone ?: stringResource(R.string.not_available))
                    Text(text = person.email ?: stringResource(R.string.not_available))
                }
            },
            confirmButton = {
                TextButton(onClick = { viewedPerson = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

private fun List<Person>.withCurrentUserFirst(currentUser: Person?): List<Person> {
    val others = filter { !it.isCurrentUser() }
    val you = firstOrNull { it.isCurrentUser() } ?: currentUser
    return listOfNotNull(you) + others
}
