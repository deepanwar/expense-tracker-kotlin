package com.example.expensetracker.ui.settings

import android.content.Context

data class IssueNote(
    val title: String,
    val done: Boolean = false
)

// ponytail: SharedPreferences is enough for a scratch issue list
class IssueNotesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(defaults: List<String>): List<IssueNote> {
        if (!prefs.contains(KEY_NOTES)) {
            return defaults.map { IssueNote(it) }
        }
        val raw = prefs.getString(KEY_NOTES, "").orEmpty()
        if (raw.isEmpty()) return emptyList()
        return raw.lineSequence().mapNotNull { line ->
            val sep = line.indexOf('|')
            if (sep <= 0) return@mapNotNull null
            val title = line.substring(sep + 1).trim()
            if (title.isEmpty()) return@mapNotNull null
            IssueNote(title = title, done = line.startsWith("1"))
        }.toList()
    }

    fun save(notes: List<IssueNote>) {
        val encoded = notes.joinToString("\n") { note ->
            val flag = if (note.done) "1" else "0"
            "$flag|${note.title.replace("\n", " ").trim()}"
        }
        prefs.edit().putString(KEY_NOTES, encoded).apply()
    }

    private companion object {
        const val PREFS_NAME = "issue_notes"
        const val KEY_NOTES = "notes"
    }
}
