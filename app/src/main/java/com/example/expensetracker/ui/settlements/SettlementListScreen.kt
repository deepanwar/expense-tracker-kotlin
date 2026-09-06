package com.example.expensetracker.ui.settlements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.example.expensetracker.model.SettlementDetails
import com.example.expensetracker.ui.expenses.listDateLabel
import com.example.expensetracker.ui.expenses.personDisplayName
import com.example.expensetracker.util.Money

@Composable
fun SettlementList(
    settlements: List<SettlementDetails>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 24.dp)
) {
    if (settlements.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.no_settlements_yet),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.no_settlements_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        itemsIndexed(settlements, key = { _, item -> item.settlement.id }) { index, details ->
            SegmentedListItem(
                selected = false,
                onClick = {},
                shapes = ListItemDefaults.segmentedShapes(
                    index = index,
                    count = settlements.size
                ),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    selectedContentColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                content = {
                    Text(
                        text = stringResource(
                            R.string.settlement_paid,
                            personDisplayName(details.fromPerson),
                            personDisplayName(details.toPerson)
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                supportingContent = details.settlement.note?.let { note ->
                    { Text(text = note) }
                },
                trailingContent = {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = Money.formatPaise(details.settlement.amountMinorUnits),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = listDateLabel(details.settlement.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun SettlementHistorySection(
    settlements: List<SettlementDetails>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.settlements),
            style = MaterialTheme.typography.titleMedium
        )
        if (settlements.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.no_settlements_yet),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.no_settlements_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                settlements.forEachIndexed { index, details ->
                    SegmentedListItem(
                        selected = false,
                        onClick = {},
                        shapes = ListItemDefaults.segmentedShapes(
                            index = index,
                            count = settlements.size
                        ),
                        colors = ListItemDefaults.segmentedColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            selectedContentColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        overlineContent = {
                            Text(text = listDateLabel(details.settlement.date))
                        },
                        content = {
                            Text(
                                text = stringResource(
                                    R.string.settlement_paid,
                                    personDisplayName(details.fromPerson),
                                    personDisplayName(details.toPerson)
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        supportingContent = details.settlement.note?.let { note ->
                            { Text(text = note) }
                        },
                        trailingContent = {
                            Text(
                                text = Money.formatPaise(details.settlement.amountMinorUnits),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )
                }
            }
        }
    }
}
