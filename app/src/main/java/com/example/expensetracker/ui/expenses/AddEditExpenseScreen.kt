package com.example.expensetracker.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.ui.components.ScreenLoadingIndicator
import com.example.expensetracker.model.SplitMethod
import com.example.expensetracker.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    viewModel: AddEditExpenseViewModel,
    mode: ExpenseFormMode,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var showPayerPicker by remember { mutableStateOf(false) }
    var showSplitEditor by remember { mutableStateOf(false) }
    val amountFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            amountFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val title = when (mode) {
        is ExpenseFormMode.Edit -> stringResource(R.string.edit_expense)
        else -> stringResource(R.string.add_expense)
    }
    val participantPool = remember(uiState) {
        AddEditExpenseViewModel.participantPool(uiState)
    }
    val selectedAmount = Money.parseRupeesToPaise(uiState.amountText)
    val shares = remember(uiState) {
        AddEditExpenseViewModel.displayShares(uiState)
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
    val paidByText = buildAnnotatedString {
        append(stringResource(R.string.paid_by))
        append(" ")
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        ) {
            append(selectedPayer?.let { personDisplayName(it) }.orEmpty())
        }
    }
    val splitText = buildAnnotatedString {
        append(stringResource(splitMethodLabel(uiState.splitMethod)))
        append(" · ")
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.SemiBold,
                color = if (uiState.participantsError || uiState.splitError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )
        ) {
            append(
                pluralStringResource(
                    R.plurals.people_count,
                    uiState.selectedParticipantIds.size,
                    uiState.selectedParticipantIds.size
                )
            )
        }
    }
    val groupClickable = !uiState.groupLocked

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        if (mode is ExpenseFormMode.Add) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.save(onSaved) },
                        enabled = !uiState.isLoading,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(36.dp)
                    ) {
                        Text(text = stringResource(R.string.save))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.amount),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val amountStyle = MaterialTheme.typography.displayMedium.copy(
                        textAlign = TextAlign.Center,
                        color = if (uiState.amountError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        }
                    )
                    val textMeasurer = rememberTextMeasurer()
                    val density = LocalDensity.current
                    val amountWidth = remember(uiState.amountText, amountStyle, density) {
                        val measured = textMeasurer.measure(
                            text = uiState.amountText.ifEmpty { "0" },
                            style = amountStyle,
                            maxLines = 1,
                            softWrap = false
                        )
                        with(density) { measured.size.width.toDp() + 2.dp }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CurrencyRupee,
                            contentDescription = stringResource(R.string.amount),
                            modifier = Modifier.size(36.dp),
                            tint = if (uiState.amountText.isEmpty()) {
                                MaterialTheme.colorScheme.outline
                            } else {
                                amountStyle.color
                            }
                        )
                        BasicTextField(
                            value = uiState.amountText,
                            onValueChange = viewModel::updateAmount,
                            singleLine = true,
                            textStyle = amountStyle,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .width(amountWidth.coerceAtLeast(28.dp))
                                .height(IntrinsicSize.Min)
                                .focusRequester(amountFocusRequester),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.Center) {
                                    if (uiState.amountText.isEmpty()) {
                                        Text(
                                            text = "0",
                                            style = amountStyle,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                    if (uiState.amountError) {
                        Text(
                            text = stringResource(R.string.amount_required),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                ExpenseFormCard {
                    ExpenseFormRow(
                        icon = Icons.Filled.ReceiptLong,
                        onClick = null
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.description),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.descriptionError) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            BasicTextField(
                                value = uiState.description,
                                onValueChange = viewModel::updateDescription,
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (uiState.description.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.what_was_it_for),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            if (uiState.descriptionError) {
                                Text(
                                    text = stringResource(R.string.description_required),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.background)
                    ExpenseFormRow(
                        icon = Icons.Filled.CalendarToday,
                        onClick = { showDatePicker = true },
                        showChevron = true
                    ) {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.background)
                    ExpenseFormRow(
                        icon = Icons.Filled.Groups,
                        onClick = if (groupClickable) {
                            { showGroupPicker = true }
                        } else {
                            null
                        },
                        showChevron = groupClickable
                    ) {
                        Text(
                            text = selectedGroupLabel,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.background)
                    ExpenseFormRow(
                        icon = Icons.Filled.AccountBalanceWallet,
                        onClick = { showPayerPicker = true },
                        showChevron = true
                    ) {
                        Text(
                            text = paidByText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ExpenseFormCard {
                    ExpenseFormRow(
                        icon = Icons.Filled.CallSplit,
                        onClick = { showSplitEditor = true },
                        trailing = {
                            Text(
                                text = stringResource(R.string.edit),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        Column {
                            Text(
                                text = splitText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (uiState.participantsError) {
                                Text(
                                    text = stringResource(R.string.participants_required),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if (uiState.splitError) {
                                Text(
                                    text = stringResource(splitValidationMessage(uiState.splitMethod)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
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
        GroupPickerSheet(
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
        PayerPickerSheet(
            people = participantPool,
            selectedPersonId = uiState.payerId,
            onSelect = { personId ->
                viewModel.selectPayer(personId)
                showPayerPicker = false
            },
            onDismiss = { showPayerPicker = false }
        )
    }

    if (showSplitEditor) {
        SplitEditorSheet(
            people = participantPool,
            selectedIds = uiState.selectedParticipantIds,
            splitMethod = uiState.splitMethod,
            shares = shares,
            exactAmountTexts = uiState.exactAmountTexts,
            percentageTexts = uiState.percentageTexts,
            shareUnitTexts = uiState.shareUnitTexts,
            totalAmount = selectedAmount,
            participantsError = uiState.participantsError,
            splitError = uiState.splitError,
            onToggle = viewModel::toggleParticipant,
            onSelectMethod = viewModel::selectSplitMethod,
            onExactAmountChange = viewModel::updateExactAmount,
            onPercentageChange = viewModel::updatePercentage,
            onShareUnitChange = viewModel::updateShareUnit,
            onDismiss = { showSplitEditor = false }
        )
    }
}

@Composable
private fun ExpenseFormCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(content = { content() })
    }
}

@Composable
private fun ExpenseFormRow(
    icon: ImageVector,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    showChevron: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
        when {
            trailing != null -> trailing()
            showChevron -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun splitMethodLabel(method: SplitMethod): Int {
    return when (method) {
        SplitMethod.EQUAL -> R.string.split_equally
        SplitMethod.EXACT -> R.string.split_exactly
        SplitMethod.PERCENTAGE -> R.string.split_by_percentage
        SplitMethod.SHARES -> R.string.split_by_shares
    }
}

fun splitValidationMessage(method: SplitMethod): Int {
    return when (method) {
        SplitMethod.EQUAL,
        SplitMethod.SHARES -> R.string.split_invalid
        SplitMethod.EXACT -> R.string.split_must_equal_amount
        SplitMethod.PERCENTAGE -> R.string.split_must_equal_100
    }
}
