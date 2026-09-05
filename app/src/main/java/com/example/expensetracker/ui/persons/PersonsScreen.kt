package com.example.expensetracker.ui.persons

import androidx.activity.compose.BackHandler
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ExpenseTrackerApplication
import com.example.expensetracker.R
import com.example.expensetracker.model.ImportedContact
import com.example.expensetracker.model.Person
import com.example.expensetracker.ui.components.ScreenLoadingIndicator
import com.example.expensetracker.ui.components.SimpleSearchBar
import com.example.expensetracker.util.ContactReader
import kotlinx.coroutines.launch

private sealed class PersonAddStep {

    data object Options : PersonAddStep()

    data object Manual : PersonAddStep()

    data class EditImported(
        val contact: ImportedContact,
        val allowDuplicate: Boolean = false
    ) : PersonAddStep()

    data class Duplicate(
        val contact: ImportedContact,
        val existing: Person
    ) : PersonAddStep()
}

@Composable
fun PersonsScreen(
    addPersonRequestCount: Int,
    modifier: Modifier = Modifier,
    onDetailViewChanged: (Boolean) -> Unit = {},
    onGroupClick: (Long) -> Unit = {},
    viewModel: PersonsViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as ExpenseTrackerApplication).let { app ->
            PersonsViewModelFactory(app.personRepository, app.groupRepository)
        }
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val persons = uiState.persons
    val searchTextFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val filteredPersons = remember(persons, searchTextFieldState.text) {
        persons.filterByQuery(searchTextFieldState.text.toString())
    }
    var addStep by remember { mutableStateOf<PersonAddStep?>(null) }
    var lastHandledAddRequestCount by remember { mutableIntStateOf(addPersonRequestCount) }
    var selectedPersonId by remember { mutableStateOf<Long?>(null) }
    var actionsPerson by remember { mutableStateOf<Person?>(null) }
    var editingPerson by remember { mutableStateOf<Person?>(null) }
    var deletingPerson by remember { mutableStateOf<Person?>(null) }

    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        val contact = ContactReader.readContact(context, uri) ?: return@rememberLauncherForActivityResult
        addStep = PersonAddStep.EditImported(contact)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickContactLauncher.launch(null)
        }
    }

    LaunchedEffect(addPersonRequestCount) {
        if (addPersonRequestCount > lastHandledAddRequestCount) {
            lastHandledAddRequestCount = addPersonRequestCount
            addStep = PersonAddStep.Options
        }
    }

    LaunchedEffect(selectedPersonId) {
        onDetailViewChanged(selectedPersonId != null)
    }

    BackHandler(enabled = selectedPersonId != null) {
        selectedPersonId = null
    }

    fun dismissFlow() {
        addStep = null
    }

    fun startContactImport() {
        addStep = null
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickContactLauncher.launch(null)
            }
            else -> permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    fun handlePersonSave(
        contact: ImportedContact,
        allowDuplicate: Boolean = false
    ) {
        scope.launch {
            if (!allowDuplicate) {
                val existing = viewModel.findExistingForImport(contact)
                if (existing != null) {
                    addStep = PersonAddStep.Duplicate(contact, existing)
                    return@launch
                }
            }
            viewModel.addPersonFromImport(contact)
            dismissFlow()
        }
    }

    fun handlePersonUpdate(
        person: Person,
        name: String,
        phone: String?,
        email: String?
    ) {
        scope.launch {
            val contact = ImportedContact(
                name = name,
                phone = phone,
                email = email,
                contactId = person.contactId
            )
            val existing = viewModel.findExistingForImport(
                contact = contact,
                excludePersonId = person.id
            )
            if (existing != null) {
                editingPerson = null
                addStep = PersonAddStep.Duplicate(contact, existing)
                return@launch
            }

            viewModel.updatePerson(
                person.copy(
                    name = name,
                    phone = phone,
                    email = email
                )
            )
            editingPerson = null
        }
    }

    fun confirmDelete(person: Person) {
        viewModel.deletePerson(person)
        deletingPerson = null
        actionsPerson = null
        if (selectedPersonId == person.id) {
            selectedPersonId = null
        }
    }

    val selectedPersonIdValue = selectedPersonId
    if (selectedPersonIdValue != null) {
        PersonDetailScreen(
            personId = selectedPersonIdValue,
            viewModel = viewModel,
            modifier = modifier,
            onBack = { selectedPersonId = null },
            onEdit = { person ->
                editingPerson = person
            },
            onDelete = { person ->
                deletingPerson = person
            },
            onGroupClick = onGroupClick
        )
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            SimpleSearchBar(
                textFieldState = searchTextFieldState,
                searchBarState = searchBarState,
                placeholder = stringResource(R.string.search_persons),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                PersonListContent(
                    isLoading = uiState.isLoading,
                    persons = persons,
                    filteredPersons = filteredPersons,
                    onPersonClick = { person ->
                        scope.launch {
                            searchBarState.animateToCollapsed()
                            selectedPersonId = person.id
                        }
                    },
                    onPersonMoreClick = { person ->
                        scope.launch {
                            searchBarState.animateToCollapsed()
                            actionsPerson = person
                        }
                    }
                )
            }

            PersonListContent(
                isLoading = uiState.isLoading,
                persons = persons,
                filteredPersons = filteredPersons,
                onPersonClick = { person ->
                    selectedPersonId = person.id
                },
                onPersonMoreClick = { person ->
                    actionsPerson = person
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    when (val step = addStep) {
        PersonAddStep.Options -> AddPersonOptionsSheet(
            onDismiss = ::dismissFlow,
            onImportFromContacts = ::startContactImport,
            onCreateManually = { addStep = PersonAddStep.Manual }
        )

        PersonAddStep.Manual -> ManualPersonSheet(
            onDismiss = ::dismissFlow,
            onSave = { name, phone, email ->
                handlePersonSave(ImportedContact(name = name, phone = phone, email = email))
            }
        )

        is PersonAddStep.EditImported -> ManualPersonSheet(
            onDismiss = ::dismissFlow,
            initialName = step.contact.name,
            initialPhone = step.contact.phone.orEmpty(),
            initialEmail = step.contact.email.orEmpty(),
            title = stringResource(R.string.review_imported_contact),
            onSave = { name, phone, email ->
                handlePersonSave(
                    contact = step.contact.copy(
                        name = name,
                        phone = phone,
                        email = email
                    ),
                    allowDuplicate = step.allowDuplicate
                )
            }
        )

        is PersonAddStep.Duplicate -> DuplicateMatchSheet(
            contact = step.contact,
            existingPersonName = step.existing.name,
            onDismiss = ::dismissFlow,
            onUseExisting = ::dismissFlow,
            onCreateNew = {
                addStep = PersonAddStep.EditImported(
                    contact = step.contact,
                    allowDuplicate = true
                )
            }
        )

        null -> Unit
    }

    actionsPerson?.let { person ->
        PersonActionsSheet(
            person = person,
            onDismiss = { actionsPerson = null },
            onView = {
                actionsPerson = null
                selectedPersonId = person.id
            },
            onEdit = {
                actionsPerson = null
                editingPerson = person
            },
            onDelete = {
                actionsPerson = null
                deletingPerson = person
            }
        )
    }

    editingPerson?.let { person ->
        ManualPersonSheet(
            onDismiss = { editingPerson = null },
            initialName = person.name,
            initialPhone = person.phone.orEmpty(),
            initialEmail = person.email.orEmpty(),
            title = stringResource(R.string.edit_person),
            onSave = { name, phone, email ->
                handlePersonUpdate(person, name, phone, email)
            }
        )
    }

    deletingPerson?.let { person ->
        DeletePersonDialog(
            personName = person.name,
            onDismiss = { deletingPerson = null },
            onConfirm = { confirmDelete(person) }
        )
    }
}

@Composable
private fun PersonListContent(
    isLoading: Boolean,
    persons: List<Person>,
    filteredPersons: List<Person>,
    onPersonClick: (Person) -> Unit,
    onPersonMoreClick: (Person) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                ScreenLoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }

            persons.isEmpty() -> {
                EmptyPersonsState(modifier = Modifier.align(Alignment.Center))
            }

            filteredPersons.isEmpty() -> {
                EmptySearchState(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                GroupedPersonList(
                    persons = filteredPersons,
                    onPersonClick = onPersonClick,
                    onPersonMoreClick = onPersonMoreClick
                )
            }
        }
    }
}

@Composable
private fun EmptySearchState(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.no_matching_persons),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(32.dp)
    )
}

@Composable
private fun EmptyPersonsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.no_persons_yet),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.no_persons_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun List<Person>.filterByQuery(query: String): List<Person> {
    val needle = query.trim()
    if (needle.isEmpty()) return this
    return filter { person ->
        person.name.contains(needle, ignoreCase = true) ||
            person.phone?.contains(needle, ignoreCase = true) == true ||
            person.email?.contains(needle, ignoreCase = true) == true
    }
}
