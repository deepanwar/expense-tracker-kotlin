package com.example.expensetracker.ui.persons

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.expensetracker.util.ContactReader
import kotlinx.coroutines.launch

private sealed class PersonAddStep {
    data object Options : PersonAddStep()
    data object Manual : PersonAddStep()
    data class Duplicate(val contact: ImportedContact, val existing: Person) : PersonAddStep()
    data class Confirm(val contact: ImportedContact) : PersonAddStep()
}

@Composable
fun PersonsScreen(
    addPersonRequestCount: Int,
    modifier: Modifier = Modifier,
    viewModel: PersonsViewModel = viewModel(
        factory = PersonsViewModelFactory(
            (LocalContext.current.applicationContext as ExpenseTrackerApplication).personRepository
        )
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val persons by viewModel.persons.collectAsStateWithLifecycle()
    var addStep by remember { mutableStateOf<PersonAddStep?>(null) }

    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        val contact = ContactReader.readContact(context, uri) ?: return@rememberLauncherForActivityResult
        scope.launch {
            val existing = viewModel.findExistingForImport(contact)
            addStep = if (existing != null) {
                PersonAddStep.Duplicate(contact, existing)
            } else {
                PersonAddStep.Confirm(contact)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickContactLauncher.launch(null)
        }
    }

    LaunchedEffect(addPersonRequestCount) {
        if (addPersonRequestCount > 0) {
            addStep = PersonAddStep.Options
        }
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

    fun finishImport(contact: ImportedContact) {
        viewModel.addPersonFromImport(contact)
        dismissFlow()
    }

    fun handleManualSave(name: String, phone: String?, email: String?) {
        val contact = ImportedContact(name = name, phone = phone, email = email)
        scope.launch {
            val existing = viewModel.findExistingForImport(contact)
            if (existing != null) {
                addStep = PersonAddStep.Duplicate(contact, existing)
            } else {
                viewModel.addPersonManually(name, phone, email)
                dismissFlow()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (persons.isEmpty()) {
            EmptyPersonsState(modifier = Modifier.align(Alignment.Center))
        } else {
            GroupedPersonList(persons = persons)
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
            onSave = ::handleManualSave
        )

        is PersonAddStep.Duplicate -> DuplicateMatchSheet(
            contact = step.contact,
            existingPersonName = step.existing.name,
            onDismiss = ::dismissFlow,
            onUseExisting = ::dismissFlow,
            onCreateNew = { addStep = PersonAddStep.Confirm(step.contact) }
        )

        is PersonAddStep.Confirm -> ImportConfirmationSheet(
            contact = step.contact,
            onDismiss = ::dismissFlow,
            onConfirm = { finishImport(step.contact) }
        )

        null -> Unit
    }
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
