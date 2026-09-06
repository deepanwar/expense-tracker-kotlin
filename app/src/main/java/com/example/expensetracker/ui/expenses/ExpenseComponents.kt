package com.example.expensetracker.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.model.ExpenseDetails
import com.example.expensetracker.model.Person
import com.example.expensetracker.model.isCurrentUser
import com.example.expensetracker.util.Money
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseList(
    expenses: List<ExpenseDetails>,
    onExpenseClick: (ExpenseDetails) -> Unit,
    modifier: Modifier = Modifier,
    showGroupName: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        bottom = 88.dp
    )
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        itemsIndexed(
            items = expenses,
            key = { _, details -> details.expense.id }
        ) { index, details ->
            ExpenseListItem(
                details = details,
                index = index,
                count = expenses.size,
                onClick = { onExpenseClick(details) },
                showGroupName = showGroupName
            )
        }
    }
}

@Composable
fun ExpenseListItem(
    details: ExpenseDetails,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showGroupName: Boolean = true
) {
    SegmentedListItem(
        selected = false,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count
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
        supportingContent = {
            Text(text = expenseSubtitle(details, showGroupName = showGroupName))
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Money.formatPaise(details.expense.amountMinorUnits),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = listDateLabel(details.expense.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun expenseSubtitle(details: ExpenseDetails, showGroupName: Boolean = true): String {
    val payerLabel = payerLabel(details.payer)
    val group = details.group
    return if (showGroupName && group != null) {
        "${group.icon} ${group.name} · $payerLabel"
    } else {
        payerLabel
    }
}

@Composable
fun payerLabel(person: Person): String {
    return if (person.isCurrentUser()) {
        stringResource(R.string.you_paid)
    } else {
        stringResource(R.string.person_paid, person.name)
    }
}

@Composable
fun personDisplayName(person: Person): String {
    return if (person.isCurrentUser()) {
        stringResource(R.string.you)
    } else {
        person.name
    }
}

fun formatExpenseDate(millis: Long): String {
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
}

@Composable
fun listDateLabel(millis: Long): String {
    return if (isSameDay(millis)) {
        stringResource(R.string.today)
    } else {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
    }
}

fun isSameDay(millis: Long, other: Long = System.currentTimeMillis()): Boolean {
    val left = Calendar.getInstance().apply { timeInMillis = millis }
    val right = Calendar.getInstance().apply { timeInMillis = other }
    return left.get(Calendar.YEAR) == right.get(Calendar.YEAR) &&
        left.get(Calendar.DAY_OF_YEAR) == right.get(Calendar.DAY_OF_YEAR)
}
