package com.example.expensetracker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

private enum class NavDestination(
    val label: String,
    val icon: ImageVector
) {
    Expenses("Expenses", Icons.Filled.Receipt),
    Groups("Groups", Icons.Filled.Groups),
    Persons("Persons", Icons.Filled.Person),
    Settings("Settings", Icons.Filled.Settings)
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var selectedDestination by rememberSaveable { mutableStateOf(NavDestination.Expenses) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            ShortNavigationBar(
                arrangement = ShortNavigationBarArrangement.EqualWeight
            ) {
                NavDestination.entries.forEach { destination ->
                    ShortNavigationBarItem(
                        selected = selectedDestination == destination,
                        onClick = { selectedDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavContent(
            destination = selectedDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun NavContent(
    destination: NavDestination,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = destination.label,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ExpenseTrackerTheme {
        MainScreen()
    }
}
