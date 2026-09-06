package com.example.expensetracker.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.model.GroupSummary
import com.example.expensetracker.model.Person
import com.example.expensetracker.model.SplitMethod
import com.example.expensetracker.ui.persons.PersonAvatar
import com.example.expensetracker.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPickerSheet(
    groups: List<GroupSummary>,
    selectedGroupId: Long?,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.select_group),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SheetChoiceRow(
                    label = stringResource(R.string.no_group),
                    selected = selectedGroupId == null,
                    onClick = { onSelect(null) }
                )
                groups.forEach { summary ->
                    SheetChoiceRow(
                        label = "${summary.group.icon} ${summary.group.name}",
                        selected = selectedGroupId == summary.group.id,
                        onClick = { onSelect(summary.group.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayerPickerSheet(
    people: List<Person>,
    selectedPersonId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.select_payer),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                people.forEach { person ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(person.id) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = person.id == selectedPersonId,
                            onClick = { onSelect(person.id) }
                        )
                        PersonAvatar(person = person)
                        Text(
                            text = personDisplayName(person),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitEditorSheet(
    people: List<Person>,
    selectedIds: Set<Long>,
    splitMethod: SplitMethod,
    shares: Map<Long, Long>,
    exactAmountTexts: Map<Long, String>,
    percentageTexts: Map<Long, String>,
    shareUnitTexts: Map<Long, String>,
    totalAmount: Long?,
    participantsError: Boolean,
    splitError: Boolean,
    onToggle: (Long) -> Unit,
    onSelectMethod: (SplitMethod) -> Unit,
    onExactAmountChange: (Long, String) -> Unit,
    onPercentageChange: (Long, String) -> Unit,
    onShareUnitChange: (Long, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val exactEntered = selectedIds.sumOf { id ->
        Money.parseRupeesToPaise(exactAmountTexts[id].orEmpty()) ?: 0L
    }
    val percentTotal = selectedIds.sumOf { id ->
        percentageTexts[id]?.trim()?.toIntOrNull() ?: 0
    }
    val shareTotal = selectedIds.sumOf { id ->
        shareUnitTexts[id]?.trim()?.toIntOrNull() ?: 0
    }
    val remaining = (totalAmount ?: 0L) - exactEntered
    val footerColor = if (splitError || participantsError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.split_expense),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            if (totalAmount != null && totalAmount > 0L) {
                Text(
                    text = Money.formatPaise(totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }
            PrimaryTabRow(selectedTabIndex = SplitMethod.entries.indexOf(splitMethod)) {
                SplitMethod.entries.forEach { method ->
                    Tab(
                        selected = splitMethod == method,
                        onClick = { onSelectMethod(method) },
                        text = { Text(text = stringResource(splitMethodTabLabel(method))) }
                    )
                }
            }
            Text(
                text = stringResource(R.string.people_selected, selectedIds.size),
                style = MaterialTheme.typography.bodyMedium,
                color = if (participantsError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                people.forEach { person ->
                    val selected = person.id in selectedIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(person.id) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = selected,
                            onCheckedChange = { onToggle(person.id) }
                        )
                        PersonAvatar(person = person)
                        Text(
                            text = personDisplayName(person),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            when (splitMethod) {
                                SplitMethod.EQUAL -> {
                                    shares[person.id]?.let { share ->
                                        Text(
                                            text = Money.formatPaise(share),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                SplitMethod.EXACT -> {
                                    SplitValueField(
                                        value = exactAmountTexts[person.id].orEmpty(),
                                        onValueChange = { onExactAmountChange(person.id, it) },
                                        suffix = "₹"
                                    )
                                }
                                SplitMethod.PERCENTAGE -> {
                                    SplitValueField(
                                        value = percentageTexts[person.id].orEmpty(),
                                        onValueChange = { onPercentageChange(person.id, it) },
                                        suffix = "%"
                                    )
                                }
                                SplitMethod.SHARES -> {
                                    SplitValueField(
                                        value = shareUnitTexts[person.id].orEmpty(),
                                        onValueChange = { onShareUnitChange(person.id, it) },
                                        suffix = ""
                                    )
                                    shares[person.id]?.let { share ->
                                        Text(
                                            text = Money.formatPaise(share),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Text(
                text = when (splitMethod) {
                    SplitMethod.EQUAL -> stringResource(R.string.split_valid)
                    SplitMethod.EXACT -> if (totalAmount != null && remaining == 0L) {
                        stringResource(R.string.split_valid)
                    } else {
                        stringResource(R.string.remaining_amount, Money.formatPaise(remaining))
                    }
                    SplitMethod.PERCENTAGE -> stringResource(R.string.total_percent, percentTotal)
                    SplitMethod.SHARES -> stringResource(R.string.total_shares, shareTotal)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = footerColor,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = stringResource(R.string.done))
            }
        }
    }
}

@Composable
private fun SplitValueField(
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (suffix == "₹") {
            Text(text = suffix, style = MaterialTheme.typography.bodyMedium)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onBackground
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(72.dp)
        )
        if (suffix == "%") {
            Text(text = suffix, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun splitMethodTabLabel(method: SplitMethod): Int {
    return when (method) {
        SplitMethod.EQUAL -> R.string.split_tab_equal
        SplitMethod.EXACT -> R.string.split_tab_exact
        SplitMethod.PERCENTAGE -> R.string.split_tab_percent
        SplitMethod.SHARES -> R.string.split_tab_shares
    }
}

@Composable
private fun SheetChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
