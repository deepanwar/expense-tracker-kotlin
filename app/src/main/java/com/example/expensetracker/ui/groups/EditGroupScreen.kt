package com.example.expensetracker.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.model.Group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupScreen(
    group: Group,
    onBack: () -> Unit,
    onSave: (Group) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(group.id) { mutableStateOf(group.name) }
    var icon by remember(group.id) { mutableStateOf(group.icon) }
    var showIconPicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    val hasChanges = name.trim() != group.name || icon != group.icon

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.edit_group)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                onClick = { showIconPicker = true },
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = icon,
                    fontSize = 48.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }
            Text(
                text = stringResource(R.string.group_icon),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.take(GroupDefaults.MAX_NAME_LENGTH)
                    nameError = false
                },
                label = { Text(stringResource(R.string.group_name)) },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text(stringResource(R.string.group_name_required)) }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) {
                        nameError = true
                        return@Button
                    }
                    onSave(group.copy(name = trimmed, icon = icon))
                },
                enabled = hasChanges && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }

    if (showIconPicker) {
        GroupIconPicker(
            selectedIcon = icon,
            onIconSelected = { selected ->
                icon = selected
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}
