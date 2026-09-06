package com.example.expensetracker.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.model.GroupSummary
import com.example.expensetracker.model.Person
import com.example.expensetracker.ui.components.ScreenLoadingIndicator
import com.example.expensetracker.ui.persons.PersonAvatar
import com.example.expensetracker.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    viewModel: AddEditExpenseViewModel,
    mode: ExpenseFormMode,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    onGroupClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var showPayerPicker by remember { mutableStateOf(false) }

    val title = when (mode) {
        is ExpenseFormMode.Add -> stringResource(R.string.add_expense)
        is ExpenseFormMode.Edit -> stringResource(R.string.edit_expense)
        is ExpenseFormMode.View -> stringResource(R.string.expense)
    }
    val participantPool = remember(uiState) {
        AddEditExpenseViewModel.participantPool(uiState)
    }
    val selectedAmount = Money.parseRupeesToPaise(uiState.amountText)
    val shares = remember(selectedAmount, uiState.selectedParticipantIds, uiState.currentUser?.id) {
        val amount = selectedAmount
        if (amount == null || amount <= 0L || uiState.selectedParticipantIds.isEmpty()) {
            emptyMap()
        } else {
            Money.sharesFor(amount, uiState.selectedParticipantIds, uiState.currentUser?.id)
        }
    }
    val selectedGroupLabel = when {
        uiState.groupId == null -> stringResource(R.string.no_group)
        uiState.selectedGroup != null -> {
            "${uiState.selectedGroup!!.group.icon} ${uiState.selectedGroup!!.group.name}"
        }
        uiState.lockedGroup != null -> {
            "${uiState.lockedGroup!!.icon} ${uiState.lockedGroup!!.name}"
        }
        else -> stringResource(R.string.no_group)
    }
    val selectedPayer = participantPool.firstOrNull { it.id == uiState.payerId }
    val dateLabel = if (isSameDay(uiState.dateMillis)) {
        stringResource(R.string.today)
    } else {
        formatExpenseDate(uiState.dateMillis)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (uiState.isReadOnly) {
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
                                    text = { Text(stringResource(R.string.edit_expense)) },
                                    onClick = {
                                        showMenu = false
                                        onEdit()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete_expense)) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    } else if (mode is ExpenseFormMode.Add) {
                        IconButton(
                            onClick = { viewModel.save(onSaved) },
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = stringResource(R.string.done)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                ScreenLoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text(stringResource(R.string.what_was_it_for)) },
                    isError = uiState.descriptionError,
                    supportingText = if (uiState.descriptionError) {
                        { Text(stringResource(R.string.description_required)) }
                    } else {
                        null
                    },
                    enabled = !uiState.isReadOnly,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.amountText,
                    onValueChange = viewModel::updateAmount,
                    label = { Text(stringResource(R.string.amount)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.CurrencyRupee,
                            contentDescription = stringResource(R.string.amount)
                        )
                    },
                    isError = uiState.amountError,
                    supportingText = if (uiState.amountError) {
                        { Text(stringResource(R.string.amount_required)) }
                    } else {
                        null
                    },
                    enabled = !uiState.isReadOnly,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                SelectableField(
                    value = dateLabel,
                    label = stringResource(R.string.date),
                    enabled = !uiState.isReadOnly,
                    onClick = { showDatePicker = true }
                )

                val groupClickable = !uiState.isReadOnly && !uiState.groupLocked
                SelectableField(
                    value = selectedGroupLabel,
                    label = stringResource(R.string.group),
                    enabled = groupClickable || (uiState.isReadOnly && uiState.groupId != null),
                    onClick = {
                        if (uiState.isReadOnly) {
                            uiState.groupId?.let(onGroupClick)
                        } else if (groupClickable) {
                            showGroupPicker = true
                        }
                    }
                )

                SelectableField(
                    value = selectedPayer?.let { personDisplayName(it) }.orEmpty(),
                    label = stringResource(R.string.paid_by),
                    enabled = !uiState.isReadOnly,
                    onClick = { showPayerPicker = true }
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.who_shared),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(
                            R.string.people_selected,
                            uiState.selectedParticipantIds.size
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.participantsError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    participantPool.forEach { person ->
                        ParticipantRow(
                            person = person,
                            selected = person.id in uiState.selectedParticipantIds,
                            shareLabel = shares[person.id]?.let(Money::formatPaise),
                            enabled = !uiState.isReadOnly,
                            onToggle = { viewModel.toggleParticipant(person.id) }
                        )
                    }
                }

                if (shares.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.split_equally),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (mode is ExpenseFormMode.Edit) {
                    Button(
                        onClick = { viewModel.save(onSaved) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.save))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(viewModel::updateDate)
                        showDatePicker = false
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showGroupPicker) {
        GroupPickerDialog(
            groups = uiState.groups,
            selectedGroupId = uiState.groupId,
            onSelect = { groupId ->
                viewModel.selectGroup(groupId)
                showGroupPicker = false
            },
            onDismiss = { showGroupPicker = false }
        )
    }

    if (showPayerPicker) {
        PayerPickerDialog(
            people = participantPool,
            selectedPersonId = uiState.payerId,
            onSelect = { personId ->
                viewModel.selectPayer(personId)
                showPayerPicker = false
            },
            onDismiss = { showPayerPicker = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(R.string.delete_expense)) },
            text = { Text(text = stringResource(R.string.delete_expense_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete(onDeleted)
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SelectableField(
    value: String,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled, onClick = onClick)
        )
    }
}

@Composable
private fun ParticipantRow(
    person: Person,
    selected: Boolean,
    shareLabel: String?,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { if (enabled) onToggle() },
            enabled = enabled
        )
        PersonAvatar(person = person)
        Text(
            text = personDisplayName(person),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (selected && shareLabel != null) {
            Text(
                text = shareLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GroupPickerDialog(
    groups: List<GroupSummary>,
    selectedGroupId: Long?,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.select_group)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                GroupChoiceRow(
                    label = stringResource(R.string.no_group),
                    selected = selectedGroupId == null,
                    onClick = { onSelect(null) }
                )
                groups.forEach { summary ->
                    GroupChoiceRow(
                        label = "${summary.group.icon} ${summary.group.name}",
                        selected = selectedGroupId == summary.group.id,
                        onClick = { onSelect(summary.group.id) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun GroupChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PayerPickerDialog(
    people: List<Person>,
    selectedPersonId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.select_payer)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                people.forEach { person ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(person.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = person.id == selectedPersonId,
                            onClick = { onSelect(person.id) }
                        )
                        Text(
                            text = personDisplayName(person),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
