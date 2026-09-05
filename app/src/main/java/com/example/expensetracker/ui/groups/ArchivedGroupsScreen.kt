package com.example.expensetracker.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.model.Group
import com.example.expensetracker.ui.components.ScreenLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedGroupsScreen(
    viewModel: ArchivedGroupsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var actionsGroup by remember { mutableStateOf<Group?>(null) }
    var deletingGroup by remember { mutableStateOf<Group?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.archived_groups)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    ScreenLoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.groups.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_groups_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }

                else -> {
                    GroupList(
                        groups = uiState.groups,
                        onGroupClick = { summary -> actionsGroup = summary.group },
                        contentPadding = PaddingValues(bottom = 16.dp)
                    )
                }
            }
        }
    }

    actionsGroup?.let { group ->
        ArchivedGroupActionsSheet(
            groupName = group.name,
            onDismiss = { actionsGroup = null },
            onRestore = {
                viewModel.restoreGroup(group.id)
                actionsGroup = null
            },
            onDelete = {
                actionsGroup = null
                deletingGroup = group
            }
        )
    }

    deletingGroup?.let { group ->
        AlertDialog(
            onDismissRequest = { deletingGroup = null },
            title = { Text(text = stringResource(R.string.delete_group)) },
            text = {
                Text(text = stringResource(R.string.delete_group_message, group.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGroup(group)
                        deletingGroup = null
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingGroup = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchivedGroupActionsSheet(
    groupName: String,
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

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
                text = groupName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            ActionRow(
                title = stringResource(R.string.restore_group),
                icon = Icons.Filled.Unarchive,
                onClick = onRestore
            )
            ActionRow(
                title = stringResource(R.string.delete_group),
                icon = Icons.Filled.Delete,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun ActionRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
