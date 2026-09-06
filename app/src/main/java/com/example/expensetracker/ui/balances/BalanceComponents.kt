package com.example.expensetracker.ui.balances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.model.GroupBalance
import com.example.expensetracker.model.OverallBalance
import com.example.expensetracker.model.PersonBalance
import com.example.expensetracker.util.Money

@Composable
fun balanceColor(netBalance: Long): Color {
    return when {
        netBalance > 0 -> MaterialTheme.colorScheme.primary
        netBalance < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun personBalanceHeadline(balance: PersonBalance): String {
    return when {
        balance.netBalance > 0 -> stringResource(
            R.string.person_owes_you,
            balance.person.name,
            Money.formatPaise(balance.theyOwe)
        )
        balance.netBalance < 0 -> stringResource(
            R.string.you_owe_amount,
            Money.formatPaise(balance.youOwe)
        )
        else -> stringResource(R.string.settled)
    }
}

@Composable
fun groupNetLabel(balance: GroupBalance): String {
    return when {
        balance.netBalance > 0 -> stringResource(
            R.string.you_are_owed_amount,
            Money.formatPaise(balance.youAreOwed)
        )
        balance.netBalance < 0 -> stringResource(
            R.string.you_owe_amount,
            Money.formatPaise(balance.youOwe)
        )
        else -> stringResource(R.string.settled)
    }
}

@Composable
fun OverallBalanceCard(
    balance: OverallBalance,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BalanceAmountRow(
                label = stringResource(R.string.you_are_owed),
                amount = balance.totalYouAreOwed,
                color = balanceColor(if (balance.totalYouAreOwed > 0) 1 else 0)
            )
            BalanceAmountRow(
                label = stringResource(R.string.you_owe),
                amount = balance.totalYouOwe,
                color = balanceColor(if (balance.totalYouOwe > 0) -1 else 0)
            )
            BalanceAmountRow(
                label = stringResource(R.string.net_balance),
                amount = balance.netBalance,
                color = balanceColor(balance.netBalance),
                signed = true
            )
        }
    }
}

@Composable
fun BalanceAmountRow(
    label: String,
    amount: Long,
    color: Color,
    modifier: Modifier = Modifier,
    signed: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (signed) Money.formatSignedPaise(amount) else Money.formatPaise(amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
