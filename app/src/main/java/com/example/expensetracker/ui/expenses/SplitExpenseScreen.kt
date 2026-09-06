package com.example.expensetracker.ui.expenses

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.model.Person
import com.example.expensetracker.model.SplitMethod
import com.example.expensetracker.ui.persons.PersonAvatar
import com.example.expensetracker.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitExpenseScreen(
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
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDone)
    val lastSelectedId = people.lastOrNull { it.id in selectedIds }?.id

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.split_expense)) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onDone) {
                        Text(text = stringResource(R.string.done))
                    }
                },
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
                .imePadding()
        ) {
            if (totalAmount != null && totalAmount > 0L) {
                Text(
                    text = Money.formatPaise(totalAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
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
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                people.forEach { person ->
                    key(person.id) {
                        val selected = person.id in selectedIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = { onToggle(person.id) },
                                modifier = Modifier.focusProperties { canFocus = false }
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
                                            suffix = "₹",
                                            isLastField = person.id == lastSelectedId
                                        )
                                    }
                                    SplitMethod.PERCENTAGE -> {
                                        SplitValueField(
                                            value = percentageTexts[person.id].orEmpty(),
                                            onValueChange = { onPercentageChange(person.id, it) },
                                            suffix = "%",
                                            isLastField = person.id == lastSelectedId
                                        )
                                    }
                                    SplitMethod.SHARES -> {
                                        SplitValueField(
                                            value = shareUnitTexts[person.id].orEmpty(),
                                            onValueChange = { onShareUnitChange(person.id, it) },
                                            suffix = "",
                                            isLastField = person.id == lastSelectedId
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
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun SplitValueField(
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    isLastField: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardOptions = remember(isLastField) {
        KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = if (isLastField) ImeAction.Done else ImeAction.Next
        )
    }
    val keyboardActions = remember(isLastField, focusManager) {
        KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
            onPrevious = { focusManager.moveFocus(FocusDirection.Previous) },
            onDone = { focusManager.clearFocus() }
        )
    }
    val outline = MaterialTheme.colorScheme.outline
    Row(
        modifier = modifier.pointerInput(focusRequester) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    if (event.changes.any { it.changedToDown() }) {
                        // ponytail: focus on DOWN so IME StopInput+StartInput same frame
                        focusRequester.requestFocus()
                    }
                }
            }
        },
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
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .focusRequester(focusRequester)
                .width(88.dp),
            decorationBox = { inner ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        inner()
                    }
                    HorizontalDivider(color = outline)
                }
            }
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
