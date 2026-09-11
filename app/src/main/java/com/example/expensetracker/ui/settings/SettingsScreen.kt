package com.example.expensetracker.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ExpenseTrackerApplication
import com.example.expensetracker.R
import com.example.expensetracker.ui.auth.AuthViewModel
import com.example.expensetracker.ui.auth.AuthViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val defaults = stringArrayResource(R.array.known_issue_items).toList()
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApplication
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(app.authRepository)
    )
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val store = remember(context) { IssueNotesStore(context) }
    var issues by remember { mutableStateOf(store.load(defaults)) }
    var draft by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    fun persist(next: List<IssueNote>) {
        issues = next
        store.save(next)
    }

    fun submitIssue() {
        val title = draft.trim()
        val editing = editingIndex
        if (title.isEmpty()) return
        val duplicate = issues.indices.any { index ->
            index != editing && issues[index].title.equals(title, ignoreCase = true)
        }
        if (duplicate) return
        if (editing != null) {
            persist(
                issues.mapIndexed { index, note ->
                    if (index == editing) note.copy(title = title) else note
                }
            )
            editingIndex = null
        } else {
            persist(issues + IssueNote(title))
        }
        draft = ""
    }

    fun startEdit(index: Int) {
        editingIndex = index
        draft = issues[index].title
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.account),
                style = MaterialTheme.typography.titleMedium
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = authState.email ?: stringResource(R.string.not_available),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    TextButton(
                        onClick = { authViewModel.signOut() },
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        Text(text = stringResource(R.string.sign_out))
                    }
                    val errorRes = authState.errorRes
                    if (errorRes != null) {
                        Text(
                            text = stringResource(errorRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.known_issues),
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = {
                    Text(
                        text = stringResource(
                            if (editingIndex != null) R.string.edit else R.string.add_issue
                        )
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = { submitIssue() },
                        enabled = draft.isNotBlank()
                    ) {
                        if (editingIndex != null) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = stringResource(R.string.save)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.add_issue)
                            )
                        }
                    }
                }
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column {
                    issues.forEachIndexed { index, issue ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    persist(
                                        issues.mapIndexed { noteIndex, note ->
                                            if (noteIndex == index) {
                                                note.copy(done = !note.done)
                                            } else {
                                                note
                                            }
                                        }
                                    )
                                }
                                .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = issue.done,
                                onCheckedChange = { checked ->
                                    persist(
                                        issues.mapIndexed { noteIndex, note ->
                                            if (noteIndex == index) {
                                                note.copy(done = checked)
                                            } else {
                                                note
                                            }
                                        }
                                    )
                                }
                            )
                            Text(
                                text = issue.title,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { startEdit(index) }) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = stringResource(R.string.edit)
                                )
                            }
                            IconButton(
                                onClick = {
                                    persist(issues.filterIndexed { noteIndex, _ -> noteIndex != index })
                                    if (editingIndex == index) {
                                        editingIndex = null
                                        draft = ""
                                    } else if (editingIndex != null && editingIndex!! > index) {
                                        editingIndex = editingIndex!! - 1
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.delete)
                                )
                            }
                        }
                        if (index < issues.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}
