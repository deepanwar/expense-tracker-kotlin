package com.example.expensetracker.ui.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.model.GroupSummary
import com.example.expensetracker.model.Person
import com.example.expensetracker.model.isCurrentUser
import com.example.expensetracker.ui.persons.PersonAvatar

@Composable
fun GroupList(
    groups: List<GroupSummary>,
    onGroupClick: (GroupSummary) -> Unit,
    modifier: Modifier = Modifier,
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
            items = groups,
            key = { _, summary -> summary.group.id }
        ) { index, summary ->
            GroupSummaryListItem(
                summary = summary,
                index = index,
                count = groups.size,
                onClick = { onGroupClick(summary) }
            )
        }
    }
}

@Composable
fun GroupSummaryListItem(
    summary: GroupSummary,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        leadingContent = {
            Text(
                text = summary.group.icon,
                fontSize = 24.sp
            )
        },
        content = {
            Text(
                text = summary.group.name,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.member_count, summary.memberCount)
            )
        },
        modifier = modifier
    )
}

@Composable
fun MemberListItem(
    person: Person,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null
) {
    ListItem(
        leadingContent = {
            PersonAvatar(person = person)
        },
        trailingContent = onMoreClick?.let { moreClick ->
            {
                IconButton(onClick = moreClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_actions)
                    )
                }
            }
        },
        modifier = if (onClick != null) {
            modifier.clickable(onClick = onClick)
        } else {
            modifier
        }
    ) {
        Text(
            text = if (person.isCurrentUser()) {
                stringResource(R.string.you)
            } else {
                person.name
            }
        )
    }
}

@Composable
fun GroupIconPicker(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.choose_group_icon))
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(GroupDefaults.ICONS) { icon ->
                    val selected = icon == selectedIcon
                    Surface(
                        onClick = { onIconSelected(icon) },
                        shape = MaterialTheme.shapes.medium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Text(
                            text = icon,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
