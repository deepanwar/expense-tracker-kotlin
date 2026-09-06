package com.example.expensetracker.ui.expenses

import androidx.activity.compose.BackHandler
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ExpenseTrackerApplication
import com.example.expensetracker.R
import com.example.expensetracker.model.ExpenseDetails
import com.example.expensetracker.ui.components.ScreenLoadingIndicator
import com.example.expensetracker.ui.components.SimpleSearchBar
import kotlinx.coroutines.launch

private sealed interface ExpensesRoute {
    data object List : ExpensesRoute
    data class Form(val mode: ExpenseFormMode) : ExpensesRoute
}

@Composable
fun ExpensesScreen(
    addExpenseRequestCount: Int,
    modifier: Modifier = Modifier,
    onDetailViewChanged: (Boolean) -> Unit = {},
    onGroupClick: (Long) -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApplication
    val viewModel: ExpensesViewModel = viewModel(
        factory = ExpensesViewModelFactory(app.expenseRepository, app.personRepository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var route by remember { mutableStateOf<ExpensesRoute>(ExpensesRoute.List) }
    var lastHandledAddRequestCount by remember { mutableIntStateOf(addExpenseRequestCount) }

    LaunchedEffect(addExpenseRequestCount) {
        if (addExpenseRequestCount > lastHandledAddRequestCount) {
            lastHandledAddRequestCount = addExpenseRequestCount
            route = ExpensesRoute.Form(ExpenseFormMode.Add())
        }
    }

    LaunchedEffect(route) {
        onDetailViewChanged(route !is ExpensesRoute.List)
    }

    BackHandler(enabled = route !is ExpensesRoute.List) {
        route = when (val current = route) {
            ExpensesRoute.List -> current
            is ExpensesRoute.Form -> when (val mode = current.mode) {
                is ExpenseFormMode.Edit -> ExpensesRoute.Form(ExpenseFormMode.View(mode.expenseId))
                is ExpenseFormMode.Add,
                is ExpenseFormMode.View -> ExpensesRoute.List
            }
        }
    }

    when (val current = route) {
        ExpensesRoute.List -> {
            ExpensesListPane(
                uiState = uiState,
                onExpenseClick = { details ->
                    route = ExpensesRoute.Form(ExpenseFormMode.View(details.expense.id))
                },
                modifier = modifier
            )
        }

        is ExpensesRoute.Form -> {
            ExpenseFormRoute(
                mode = current.mode,
                onBack = {
                    route = when (val mode = current.mode) {
                        is ExpenseFormMode.Edit -> ExpensesRoute.Form(ExpenseFormMode.View(mode.expenseId))
                        is ExpenseFormMode.Add,
                        is ExpenseFormMode.View -> ExpensesRoute.List
                    }
                },
                onSaved = { expenseId ->
                    route = ExpensesRoute.Form(ExpenseFormMode.View(expenseId))
                },
                onEdit = {
                    val expenseId = when (val mode = current.mode) {
                        is ExpenseFormMode.View -> mode.expenseId
                        is ExpenseFormMode.Edit -> mode.expenseId
                        is ExpenseFormMode.Add -> return@ExpenseFormRoute
                    }
                    route = ExpensesRoute.Form(ExpenseFormMode.Edit(expenseId))
                },
                onDeleted = { route = ExpensesRoute.List },
                onGroupClick = onGroupClick,
                modifier = modifier
            )
        }
    }
}

@Composable
fun ExpenseFormRoute(
    mode: ExpenseFormMode,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    onGroupClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApplication
    val viewModel: AddEditExpenseViewModel = viewModel(
        key = mode.viewModelKey(),
        factory = AddEditExpenseViewModelFactory(
            mode = mode,
            expenseRepository = app.expenseRepository,
            personRepository = app.personRepository,
            groupRepository = app.groupRepository
        )
    )
    AddEditExpenseScreen(
        viewModel = viewModel,
        mode = mode,
        onBack = onBack,
        onSaved = onSaved,
        onEdit = onEdit,
        onDeleted = onDeleted,
        onGroupClick = onGroupClick,
        modifier = modifier
    )
}

@Composable
private fun ExpensesListPane(
    uiState: ExpensesUiState,
    onExpenseClick: (ExpenseDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchTextFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val filteredExpenses = remember(uiState.expenses, searchTextFieldState.text) {
        uiState.expenses.filterByQuery(searchTextFieldState.text.toString())
    }

    Column(modifier = modifier.fillMaxSize()) {
        SimpleSearchBar(
            textFieldState = searchTextFieldState,
            searchBarState = searchBarState,
            placeholder = stringResource(R.string.search_expenses),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ExpenseListContent(
                isLoading = uiState.isLoading,
                expenses = uiState.expenses,
                filteredExpenses = filteredExpenses,
                onExpenseClick = { details ->
                    scope.launch {
                        searchBarState.animateToCollapsed()
                        onExpenseClick(details)
                    }
                }
            )
        }

        ExpenseListContent(
            isLoading = uiState.isLoading,
            expenses = uiState.expenses,
            filteredExpenses = filteredExpenses,
            onExpenseClick = onExpenseClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExpenseListContent(
    isLoading: Boolean,
    expenses: List<ExpenseDetails>,
    filteredExpenses: List<ExpenseDetails>,
    onExpenseClick: (ExpenseDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                ScreenLoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }

            expenses.isEmpty() -> {
                EmptyExpensesState(modifier = Modifier.align(Alignment.Center))
            }

            filteredExpenses.isEmpty() -> {
                EmptyExpenseSearchState(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                ExpenseList(
                    expenses = filteredExpenses,
                    onExpenseClick = onExpenseClick
                )
            }
        }
    }
}

@Composable
private fun EmptyExpensesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.no_expenses_yet),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.no_expenses_list_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyExpenseSearchState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.no_expenses_found),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.no_expenses_found_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun List<ExpenseDetails>.filterByQuery(query: String): List<ExpenseDetails> {
    val needle = query.trim()
    if (needle.isEmpty()) return this
    return filter { details ->
        details.expense.description.contains(needle, ignoreCase = true) ||
            details.group?.name?.contains(needle, ignoreCase = true) == true ||
            details.payer.name.contains(needle, ignoreCase = true) ||
            details.participants.any { it.person.name.contains(needle, ignoreCase = true) }
    }
}
