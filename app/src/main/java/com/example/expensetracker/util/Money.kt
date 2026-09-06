package com.example.expensetracker.util

import java.text.NumberFormat
import java.util.Locale

object Money {
    fun parseRupeesToPaise(input: String): Long? {
        val trimmed = input.trim().replace(",", "")
        if (trimmed.isEmpty() || trimmed.startsWith("-")) return null
        if (!trimmed.matches(Regex("""\d+(\.\d{0,2})?"""))) return null

        val parts = trimmed.split('.')
        val rupees = parts[0].toLongOrNull() ?: return null
        val paise = when {
            parts.size == 1 || parts[1].isEmpty() -> 0L
            parts[1].length == 1 -> parts[1].toLong() * 10
            else -> parts[1].take(2).toLong()
        }
        return rupees * 100 + paise
    }

    fun paiseToInput(paise: Long): String {
        val rupees = paise / 100
        val leftover = paise % 100
        return if (leftover == 0L) {
            rupees.toString()
        } else {
            "$rupees.${leftover.toString().padStart(2, '0')}"
        }
    }

    fun formatPaise(paise: Long): String {
        val format = NumberFormat.getNumberInstance(Locale.ENGLISH)
        format.minimumFractionDigits = if (paise % 100L == 0L) 0 else 2
        format.maximumFractionDigits = 2
        return "₹${format.format(paise / 100.0)}"
    }

    fun formatSignedPaise(paise: Long): String {
        val formatted = formatPaise(kotlin.math.abs(paise))
        return when {
            paise > 0 -> "+$formatted"
            paise < 0 -> "-$formatted"
            else -> formatted
        }
    }

    fun equalShares(totalPaise: Long, count: Int): List<Long> {
        if (count <= 0) return emptyList()
        val base = totalPaise / count
        val remainder = (totalPaise % count).toInt()
        return List(count) { index -> if (index < remainder) base + 1 else base }
    }

    fun orderedParticipantIds(ids: Collection<Long>, currentUserId: Long?): List<Long> {
        val distinct = ids.distinct()
        val you = currentUserId?.takeIf { it in distinct }
        val others = distinct.filter { it != you }.sorted()
        return listOfNotNull(you) + others
    }

    fun sharesFor(
        totalPaise: Long,
        participantIds: Collection<Long>,
        currentUserId: Long?
    ): Map<Long, Long> {
        val ordered = orderedParticipantIds(participantIds, currentUserId)
        return ordered.zip(equalShares(totalPaise, ordered.size)).toMap()
    }
}
