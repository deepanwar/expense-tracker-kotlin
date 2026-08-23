package com.example.expensetracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ui.persons.PersonsScreen
import com.example.expensetracker.ui.preview.AppPreview
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import kotlinx.coroutines.delay

private enum class NavDestination(
    val label: String,
    val icon: ImageVector,
    val fabLabel: String? = null
) {
    Expenses("Expenses", Icons.Filled.Receipt, "Add expense"),
    Groups("Groups", Icons.Filled.Groups, "Add group"),
    Persons("Persons", Icons.Filled.Person, "Add person"),
    Settings("Settings", Icons.Filled.Settings);

    val showFab: Boolean get() = fabLabel != null
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var selectedDestination by rememberSaveable { mutableStateOf(NavDestination.Expenses) }
    var personAddRequestCount by remember { mutableIntStateOf(0) }
    var isPersonDetailView by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            AnimatedExtendedFab(
                destination = selectedDestination,
                visible = selectedDestination.showFab && !isPersonDetailView,

                onClick = {
                    if (selectedDestination == NavDestination.Persons) {
                        personAddRequestCount++
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
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
            personAddRequestCount = personAddRequestCount,
            onPersonDetailViewChanged = { isDetailView ->
                isPersonDetailView = isDetailView
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun AnimatedExtendedFab(
    destination: NavDestination,
    visible: Boolean,
    onClick: () -> Unit
) {
    val fabLabel = destination.fabLabel ?: return
    var expanded by rememberSaveable(destination) { mutableStateOf(true) }

    // LaunchedEffect(destination) {
    //     expanded = false
    //     delay(100)
    //     expanded = true
    // }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        ),
        exit = fadeOut() + scaleOut(
            targetScale = 0.8f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
    ) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            expanded = expanded,
            modifier = Modifier.height(56.dp),
            shape = FloatingActionButtonDefaults.mediumShape,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = fabLabel
                )
            },
            text = {
                Text(
                    text = fabLabel,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        )
    }
}

@Composable
private fun NavContent(
    destination: NavDestination,
    personAddRequestCount: Int,
    onPersonDetailViewChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    when (destination) {
        NavDestination.Persons -> PersonsScreen(
            addPersonRequestCount = personAddRequestCount,
            onDetailViewChanged = onPersonDetailViewChanged,
            modifier = modifier
        )

        else -> Box(
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
}

@AppPreview
@Composable
fun MainScreenPreview() {
    ExpenseTrackerTheme {
        MainScreen()
    }
}
