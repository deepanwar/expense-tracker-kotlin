package com.example.expensetracker.ui.persons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.expensetracker.model.Person
import kotlin.math.abs

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
    contentPadding: PaddingValues = PaddingValues(bottom = 88.dp)
) {
    val sections = remember(persons) { groupPersonsByLetter(persons) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        sections.forEach { section ->
            item(key = "header_${section.header}") {
                PersonSectionHeader(letter = section.header)
            }

            items(
                items = section.persons,
                key = { person -> person.id }
            ) { person ->
                PersonListRow(person = person)
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
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun PersonListRow(
    person: Person,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PersonAvatar(person = person)
            Text(
                text = person.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PersonAvatar(
    person: Person,
    modifier: Modifier = Modifier
) {
    if (person.photoUri != null) {
        AsyncImage(
            model = person.photoUri,
            contentDescription = person.name,
            modifier = modifier
                .size(44.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        val initial = person.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        val backgroundColor = avatarColorForName(person.name)

        Box(
            modifier = modifier
                .size(44.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = backgroundColor
            ) {}
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

private fun groupPersonsByLetter(persons: List<Person>): List<PersonListSection> {
    return persons
        .sortedBy { it.name.lowercase() }
        .groupBy { person ->
            person.name.firstOrNull()?.uppercaseChar() ?: '#'
        }
        .toList()
        .sortedBy { (letter, _) -> letter }
        .map { (letter, sectionPersons) ->
            PersonListSection(header = letter, persons = sectionPersons)
        }
}

private fun avatarColorForName(name: String): Color {
    if (name.isBlank()) return AvatarColors.first()
    return AvatarColors[abs(name.hashCode()) % AvatarColors.size]
}
