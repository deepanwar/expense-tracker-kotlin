package com.example.expensetracker.ui.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.model.Person
import com.example.expensetracker.ui.components.SimpleSearchBar
import com.example.expensetracker.ui.persons.PersonAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMembersScreen(
    persons: List<Person>,
    initiallySelectedIds: Set<Long>,
    currentMemberIds: Set<Long> = emptySet(),
    autoSelectPersonId: Long? = null,
    onConfirm: (Set<Long>) -> Unit,
    onBack: () -> Unit,
    onCreatePerson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchTextFieldState = rememberTextFieldState()
    var selectedIds by remember(initiallySelectedIds) {
        mutableStateOf(initiallySelectedIds)
    }

    LaunchedEffect(autoSelectPersonId) {
        autoSelectPersonId?.let { selectedIds = selectedIds + it }
    }
    val availablePersons = remember(persons, currentMemberIds) {
        persons.filter { it.id !in currentMemberIds }
    }
    val filteredPersons = remember(availablePersons, searchTextFieldState.text) {
        availablePersons.filterByName(searchTextFieldState.text.toString())
    }
    val selectedPersons = remember(availablePersons, selectedIds) {
        availablePersons.filter { it.id in selectedIds }
    }
    val unselectedPersons = remember(filteredPersons, selectedIds) {
        filteredPersons.filter { it.id !in selectedIds }
    }
    val currentMembers = remember(persons, currentMemberIds) {
        persons.filter { it.id in currentMemberIds }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.add_members)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        bottomBar = {
            Button(
                onClick = { onConfirm(selectedIds) },
                enabled = selectedIds.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = stringResource(R.string.add))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SimpleSearchBar(
                textFieldState = searchTextFieldState,
                placeholder = stringResource(R.string.search_persons),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (currentMembers.isNotEmpty()) {
                    item {
                        SectionHeader(text = stringResource(R.string.current_members))
                    }
                    items(currentMembers, key = { "current-${it.id}" }) { person ->
                        MemberListItem(person = person)
                    }
                }

                if (selectedPersons.isNotEmpty()) {
                    item {
                        SectionHeader(
                            text = stringResource(R.string.members_selected, selectedPersons.size)
                        )
                    }
                    items(selectedPersons, key = { "selected-${it.id}" }) { person ->
                        SelectablePersonRow(
                            person = person,
                            selected = true,
                            onToggle = { selectedIds = selectedIds - person.id }
                        )
                    }
                }

                item {
                    SectionHeader(text = stringResource(R.string.people))
                }
                items(unselectedPersons, key = { it.id }) { person ->
                    SelectablePersonRow(
                        person = person,
                        selected = false,
                        onToggle = { selectedIds = selectedIds + person.id }
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    TextButton(
                        onClick = onCreatePerson,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(text = stringResource(R.string.create_new_person))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SelectablePersonRow(
    person: Person,
    selected: Boolean,
    onToggle: () -> Unit
) {
    ListItem(
        leadingContent = { PersonAvatar(person = person) },
        trailingContent = {
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
        },
        modifier = Modifier.clickable(onClick = onToggle)
    ) {
        Text(text = person.name)
    }
}

private fun List<Person>.filterByName(query: String): List<Person> {
    val needle = query.trim()
    if (needle.isEmpty()) return this
    return filter { it.name.contains(needle, ignoreCase = true) }
}
