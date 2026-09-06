package com.example.expensetracker.ui.settlements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.model.Person
import com.example.expensetracker.ui.expenses.formatExpenseDate
import com.example.expensetracker.ui.expenses.isSameDay
import com.example.expensetracker.ui.expenses.personDisplayName
import com.example.expensetracker.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpSheet(
    currentUser: Person,
    otherPerson: Person,
    suggestedAmount: Long,
    theyPayYou: Boolean,
    groupId: Long? = null,
    onConfirm: (
        fromPersonId: Long,
        toPersonId: Long,
        amountMinorUnits: Long,
        groupId: Long?,
        note: String?,
        date: Long
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountText by remember {
        mutableStateOf(if (suggestedAmount > 0L) Money.paiseToInput(suggestedAmount) else "")
    }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    val fromPerson = if (theyPayYou) otherPerson else currentUser
    val toPerson = if (theyPayYou) currentUser else otherPerson
    val dateLabel = if (isSameDay(dateMillis)) {
        stringResource(R.string.today)
    } else {
        formatExpenseDate(dateMillis)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settle_up),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(
                    R.string.settlement_paid,
                    personDisplayName(fromPerson),
                    personDisplayName(toPerson)
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.take(16)
                    amountError = false
                },
                label = { Text(text = stringResource(R.string.amount)) },
                prefix = { Text(text = "₹") },
                isError = amountError,
                supportingText = if (amountError) {
                    { Text(text = stringResource(R.string.amount_required_payment)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { showDatePicker = true }) {
                Text(text = dateLabel)
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(200) },
                label = { Text(text = stringResource(R.string.payment_note)) },
                placeholder = { Text(text = stringResource(R.string.payment_note_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val amount = Money.parseRupeesToPaise(amountText)
                    if (amount == null || amount <= 0L) {
                        amountError = true
                        return@Button
                    }
                    onConfirm(
                        fromPerson.id,
                        toPerson.id,
                        amount,
                        groupId,
                        note.trim().ifEmpty { null },
                        dateMillis
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.record_payment))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { dateMillis = it }
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
}
