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

    fun exactShares(
        totalPaise: Long,
        exactAmounts: Map<Long, Long>
    ): Map<Long, Long>? {
        if (exactAmounts.isEmpty()) return null
        if (exactAmounts.values.any { it < 0L }) return null
        if (exactAmounts.values.sum() != totalPaise) return null
        return exactAmounts
    }

    fun percentageShares(
        totalPaise: Long,
        percentages: Map<Long, Int>
    ): Map<Long, Long>? {
        if (percentages.isEmpty()) return null
        if (percentages.values.any { it < 0 }) return null
        if (percentages.values.sum() != 100) return null
        return allocateByWeight(totalPaise, percentages.mapValues { it.value.toLong() })
    }

    fun unitShares(
        totalPaise: Long,
        units: Map<Long, Int>
    ): Map<Long, Long> {
        if (units.isEmpty() || units.values.any { it < 0 }) return emptyMap()
        val positive = units.filterValues { it > 0 }
        if (positive.isEmpty()) return emptyMap()
        return allocateByWeight(totalPaise, positive.mapValues { it.value.toLong() })
    }

    fun percentagesFromAmounts(amounts: Map<Long, Long>): Map<Long, Int> {
        val total = amounts.values.sum()
        if (total <= 0L || amounts.isEmpty()) return amounts.mapValues { 0 }
        val raw = amounts.mapValues { ((it.value * 100L) / total).toInt() }
        return distributeIntRemainder(raw, 100)
    }

    fun shareUnitsFromAmounts(amounts: Map<Long, Long>): Map<Long, Int> {
        val values = amounts.values.filter { it > 0L }
        if (values.isEmpty()) return amounts.mapValues { 0 }
        val divisor = values.reduce(::gcd)
        return amounts.mapValues { (it.value / divisor).toInt() }
    }

    private fun allocateByWeight(totalPaise: Long, weights: Map<Long, Long>): Map<Long, Long> {
        val totalWeight = weights.values.sum()
        if (totalWeight <= 0L) return emptyMap()
        val bases = weights.mapValues { totalPaise * it.value / totalWeight }
        val remainder = (totalPaise - bases.values.sum()).toInt()
        return bases.entries.mapIndexed { index, entry ->
            entry.key to entry.value + if (index < remainder) 1L else 0L
        }.toMap()
    }

    private fun distributeIntRemainder(raw: Map<Long, Int>, target: Int): Map<Long, Int> {
        var leftover = target - raw.values.sum()
        if (leftover == 0) return raw
        val keys = raw.keys.toList()
        val next = raw.toMutableMap()
        var index = 0
        while (leftover != 0 && keys.isNotEmpty()) {
            val key = keys[index % keys.size]
            val step = if (leftover > 0) 1 else -1
            next[key] = next.getValue(key) + step
            leftover -= step
            index++
        }
        return next
    }

    private fun gcd(left: Long, right: Long): Long {
        var a = kotlin.math.abs(left)
        var b = kotlin.math.abs(right)
        while (b != 0L) {
            val next = a % b
            a = b
            b = next
        }
        return if (a == 0L) 1L else a
    }
}
