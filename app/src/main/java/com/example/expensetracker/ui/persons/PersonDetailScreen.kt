package com.example.expensetracker.ui.persons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.model.Person
import com.example.expensetracker.model.involves
import com.example.expensetracker.ui.balances.BalanceAmountRow
import com.example.expensetracker.ui.balances.balanceColor
import com.example.expensetracker.ui.balances.personBalanceHeadline
import com.example.expensetracker.ui.groups.GroupSummaryListItem
import com.example.expensetracker.ui.settlements.SettleUpSheet
import com.example.expensetracker.ui.settlements.SettlementHistorySection
import com.example.expensetracker.util.BalanceCalculator
import com.example.expensetracker.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personId: Long,
    viewModel: PersonsViewModel,
    onBack: () -> Unit,
    onEdit: (Person) -> Unit,
    onDelete: (Person) -> Unit,
    onGroupClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val person by viewModel.observePerson(personId).collectAsStateWithLifecycle(initialValue = null)
    val commonGroups by viewModel.observeCommonGroups(personId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val balance = uiState.personBalances[personId]
    val personExpenses = remember(uiState.expenses, personId, currentUser?.id) {
        val userId = currentUser?.id ?: return@remember emptyList()
        uiState.expenses.filter { it.involves(personId) && it.involves(userId) }
    }
    val personSettlements = remember(uiState.settlements, personId, currentUser?.id) {
        val userId = currentUser?.id ?: return@remember emptyList()
        uiState.settlements.filter { details ->
            val from = details.settlement.fromPersonId
            val to = details.settlement.toPersonId
            (from == personId && to == userId) || (from == userId && to == personId)
        }
    }
    var showSettleUp by remember { mutableStateOf(false) }
    val (youPaid, yourShare) = remember(personExpenses, currentUser?.id) {
        val userId = currentUser?.id ?: return@remember 0L to 0L
        BalanceCalculator.paidAndShare(personExpenses, userId)
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    if (person == null) {
        return
    }

    val currentPerson = person!!

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(text = currentPerson.name)
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
                    IconButton(onClick = { onEdit(currentPerson) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }
                    IconButton(onClick = { onDelete(currentPerson) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete)
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
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 16.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        PersonAvatar(
                            person = currentPerson,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (balance == null) {
                                stringResource(R.string.settled)
                            } else {
                                personBalanceHeadline(balance)
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = balanceColor(balance?.netBalance ?: 0L)
                        )
                        BalanceAmountRow(
                            label = stringResource(R.string.you_paid),
                            amount = youPaid,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        BalanceAmountRow(
                            label = stringResource(R.string.your_share),
                            amount = yourShare,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        BalanceAmountRow(
                            label = stringResource(R.string.balance),
                            amount = balance?.netBalance ?: 0L,
                            color = balanceColor(balance?.netBalance ?: 0L),
                            signed = true
                        )
                        if (balance != null && balance.netBalance != 0L && currentUser != null) {
                            TextButton(onClick = { showSettleUp = true }) {
                                Text(text = stringResource(R.string.settle_up))
                            }
                        }
                    }
                }

                item {
                    PersonDetailField(
                        label = stringResource(R.string.phone),
                        value = currentPerson.phone ?: stringResource(R.string.not_available)
                    )
                }

                item {
                    PersonDetailField(
                        label = stringResource(R.string.email),
                        value = currentPerson.email ?: stringResource(R.string.not_available)
                    )
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.shared_groups),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (commonGroups.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.no_shared_groups),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(
                                    ListItemDefaults.SegmentedGap
                                )
                            ) {
                                commonGroups.forEachIndexed { index, summary ->
                                    GroupSummaryListItem(
                                        summary = summary,
                                        index = index,
                                        count = commonGroups.size,
                                        onClick = { onGroupClick(summary.group.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.expenses),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (personExpenses.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.no_expenses_yet),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = stringResource(R.string.no_expenses_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            val userId = currentUser?.id
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(
                                    ListItemDefaults.SegmentedGap
                                )
                            ) {
                                personExpenses.forEachIndexed { index, details ->
                                    val delta = if (userId == null) {
                                        0L
                                    } else {
                                        BalanceCalculator.expenseDelta(details, userId, personId) ?: 0L
                                    }
                                    SegmentedListItem(
                                        selected = false,
                                        onClick = {},
                                        shapes = ListItemDefaults.segmentedShapes(
                                            index = index,
                                            count = personExpenses.size
                                        ),
                                        colors = ListItemDefaults.segmentedColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            selectedContentColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                        ),
                                        content = {
                                            Text(
                                                text = details.expense.description,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        trailingContent = {
                                            Text(
                                                text = Money.formatSignedPaise(delta),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = balanceColor(delta)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    SettlementHistorySection(settlements = personSettlements)
                }
            }
        }
    )

    val user = currentUser
    if (showSettleUp && user != null && balance != null) {
        SettleUpSheet(
            currentUser = user,
            otherPerson = currentPerson,
            suggestedAmount = kotlin.math.abs(balance.netBalance),
            theyPayYou = balance.netBalance > 0L,
            onConfirm = { fromPersonId, toPersonId, amount, groupId, note, date ->
                viewModel.recordSettlement(
                    fromPersonId = fromPersonId,
                    toPersonId = toPersonId,
                    amountMinorUnits = amount,
                    groupId = groupId,
                    note = note,
                    date = date
                )
            },
            onDismiss = { showSettleUp = false }
        )
    }
}

@Composable
private fun PersonDetailField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
