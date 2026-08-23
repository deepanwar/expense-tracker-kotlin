package com.example.expensetracker.ui.persons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.expensetracker.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.expensetracker.model.Person
import com.example.expensetracker.ui.preview.AppPreview
import kotlin.math.absoluteValue

private data class PersonListSection(
    val header: Char,
    val persons: List<Person>
)

private val AvatarColors = listOf(
    Color(0xFF00897B),
    Color(0xFF7B1FA2),
    Color(0xFFC62828),
    Color(0xFFAD1457),
    Color(0xFF1565C0),
    Color(0xFFEF6C00),
    Color(0xFF4527A0),
    Color(0xFF2E7D32)
)

@Composable
fun GroupedPersonList(
    persons: List<Person>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        bottom = 88.dp
    ),
    onPersonClick: (Person) -> Unit = {},
    onPersonMoreClick: (Person) -> Unit = {}
) {
    val sections = remember(persons) {
        groupPersonsByLetter(persons)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        items(
            items = sections,
            key = { section -> section.header }
        ) { section ->

            Column {
                PersonSectionHeader(
                    letter = section.header
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        ListItemDefaults.SegmentedGap
                    )
                ) {
                    section.persons.forEachIndexed { index, person ->

                        SegmentedListItem(
                            selected = false,
                            onClick = {
                                onPersonClick(person)
                            },
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = section.persons.size
                            ),
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                selectedContentColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            leadingContent = {
                                PersonAvatar(person)
                            },
                            trailingContent = {
                                    IconButton(
                                        onClick = { onPersonMoreClick(person) },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = stringResource(R.string.more_actions)
                                        )
                                    }
                                },
                            content = {
                                Text(
                                    text = person.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                val detail = person.phone ?: person.email
                                if (detail != null) Text(detail)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonSectionHeader(
    letter: Char,
    modifier: Modifier = Modifier
) {
    Text(
        text = letter.toString(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(
            start = 16.dp,
            top = 20.dp,
            bottom = 8.dp
        )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PersonAvatar(
    person: Person,
    modifier: Modifier = Modifier
) {
    val photoUri = person.photoUri
    val shape = MaterialShapes.Cookie12Sided.toShape()

    if (photoUri != null) {
        AsyncImage(
            model = photoUri,
            contentDescription = "${person.name} profile picture",
            modifier = modifier
                .size(48.dp)
                .clip(shape),
            contentScale = ContentScale.Crop
        )
    } else {
        val initial = person.name
            .trim()
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            .orEmpty()

        Surface(
            modifier = modifier.size(48.dp),
            shape = shape,
            color = avatarColorForName(person.name)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

private fun groupPersonsByLetter(
    persons: List<Person>
): List<PersonListSection> {
    return persons
        .sortedBy { it.name.trim().lowercase() }
        .groupBy { person ->
            person.name
                .trim()
                .firstOrNull()
                ?.uppercaseChar()
                ?: '#'
        }
        .toList()
        .sortedBy { (letter, _) -> letter }
        .map { (letter, sectionPersons) ->
            PersonListSection(
                header = letter,
                persons = sectionPersons
            )
        }
}

internal fun avatarColorForName(name: String): Color {
    if (name.isBlank()) {
        return AvatarColors.first()
    }

    return AvatarColors[
        name.hashCode().absoluteValue % AvatarColors.size
    ]
}