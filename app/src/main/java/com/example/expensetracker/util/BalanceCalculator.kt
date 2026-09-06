package com.example.expensetracker.util

import com.example.expensetracker.model.ExpenseDetails
import com.example.expensetracker.model.GroupBalance
import com.example.expensetracker.model.OverallBalance
import com.example.expensetracker.model.Person
import com.example.expensetracker.model.PersonBalance
import com.example.expensetracker.model.Settlement
import com.example.expensetracker.model.SettlementDetails
import com.example.expensetracker.model.involves

object BalanceCalculator {
    fun calculatePersonBalance(
        expenses: List<ExpenseDetails>,
        currentUserId: Long,
        otherPersonId: Long,
        settlements: List<Settlement> = emptyList()
    ): Long {
        var theyOweYou = 0L
        var youOweThem = 0L
        for (details in expenses) {
            if (!details.involves(currentUserId) || !details.involves(otherPersonId)) continue
            when (details.expense.payerId) {
                currentUserId -> theyOweYou += shareOf(details, otherPersonId)
                otherPersonId -> youOweThem += shareOf(details, currentUserId)
            }
        }
        for (settlement in settlements) {
            when {
                settlement.fromPersonId == otherPersonId && settlement.toPersonId == currentUserId -> {
                    theyOweYou -= settlement.amountMinorUnits
                }
                settlement.fromPersonId == currentUserId && settlement.toPersonId == otherPersonId -> {
                    youOweThem -= settlement.amountMinorUnits
                }
            }
        }
        return theyOweYou - youOweThem
    }

    fun calculatePersonBalance(
        expenses: List<ExpenseDetails>,
        settlements: List<SettlementDetails>,
        currentUserId: Long,
        otherPersonId: Long
    ): Long {
        return calculatePersonBalance(
            expenses,
            currentUserId,
            otherPersonId,
            settlements.map { it.settlement }
        )
    }

    fun calculateAllPersonBalances(
        expenses: List<ExpenseDetails>,
        currentUserId: Long,
        extraPeople: List<Person> = emptyList(),
        settlements: List<Settlement> = emptyList()
    ): List<PersonBalance> {
        return peopleFrom(expenses, extraPeople)
            .values
            .filter { it.id != currentUserId }
            .map { person ->
                toPersonBalance(
                    person = person,
                    net = calculatePersonBalance(expenses, currentUserId, person.id, settlements)
                )
            }
    }

    fun calculateAllPersonBalances(
        expenses: List<ExpenseDetails>,
        settlements: List<SettlementDetails>,
        currentUserId: Long,
        extraPeople: List<Person> = emptyList()
    ): List<PersonBalance> {
        return calculateAllPersonBalances(
            expenses = expenses,
            currentUserId = currentUserId,
            extraPeople = extraPeople,
            settlements = settlements.map { it.settlement }
        )
    }

    fun calculateGroupBalance(
        expenses: List<ExpenseDetails>,
        currentUserId: Long,
        groupId: Long,
        extraPeople: List<Person> = emptyList(),
        settlements: List<Settlement> = emptyList()
    ): GroupBalance {
        val groupExpenses = expenses.filter { it.expense.groupId == groupId }
        val groupSettlements = settlements.filter { it.groupId == groupId }
        val personBalances = calculateAllPersonBalances(
            expenses = groupExpenses,
            currentUserId = currentUserId,
            extraPeople = extraPeople,
            settlements = groupSettlements
        )
        return toGroupBalance(groupId, personBalances)
    }

    fun calculateGroupBalance(
        expenses: List<ExpenseDetails>,
        settlements: List<SettlementDetails>,
        currentUserId: Long,
        groupId: Long,
        extraPeople: List<Person> = emptyList()
    ): GroupBalance {
        return calculateGroupBalance(
            expenses = expenses,
            currentUserId = currentUserId,
            groupId = groupId,
            extraPeople = extraPeople,
            settlements = settlements.map { it.settlement }
        )
    }

    fun calculateOverallBalance(
        expenses: List<ExpenseDetails>,
        currentUserId: Long,
        extraPeople: List<Person> = emptyList(),
        settlements: List<Settlement> = emptyList()
    ): OverallBalance {
        return calculateOverallBalance(
            calculateAllPersonBalances(expenses, currentUserId, extraPeople, settlements)
        )
    }

    fun calculateOverallBalance(
        expenses: List<ExpenseDetails>,
        settlements: List<SettlementDetails>,
        currentUserId: Long,
        extraPeople: List<Person> = emptyList()
    ): OverallBalance {
        return calculateOverallBalance(
            expenses,
            currentUserId,
            extraPeople,
            settlements.map { it.settlement }
        )
    }

    fun calculateOverallBalance(personBalances: List<PersonBalance>): OverallBalance {
        val totalYouOwe = personBalances.sumOf { it.youOwe }
        val totalYouAreOwed = personBalances.sumOf { it.theyOwe }
        return OverallBalance(
            totalYouOwe = totalYouOwe,
            totalYouAreOwed = totalYouAreOwed,
            netBalance = totalYouAreOwed - totalYouOwe
        )
    }

    fun toPersonBalance(person: Person, net: Long): PersonBalance {
        return PersonBalance(
            personId = person.id,
            person = person,
            youOwe = if (net < 0) -net else 0L,
            theyOwe = if (net > 0) net else 0L,
            netBalance = net
        )
    }

    fun toGroupBalance(groupId: Long, personBalances: List<PersonBalance>): GroupBalance {
        val youOwe = personBalances.sumOf { it.youOwe }
        val youAreOwed = personBalances.sumOf { it.theyOwe }
        return GroupBalance(
            groupId = groupId,
            youOwe = youOwe,
            youAreOwed = youAreOwed,
            netBalance = youAreOwed - youOwe,
            personBalances = personBalances
        )
    }

    fun paidAndShare(expenses: List<ExpenseDetails>, personId: Long): Pair<Long, Long> {
        var paid = 0L
        var share = 0L
        for (details in expenses) {
            if (details.expense.payerId == personId) {
                paid += details.expense.amountMinorUnits
            }
            share += shareOf(details, personId)
        }
        return paid to share
    }

    fun expenseDelta(
        details: ExpenseDetails,
        currentUserId: Long,
        otherPersonId: Long
    ): Long? {
        if (!details.involves(currentUserId) || !details.involves(otherPersonId)) return null
        return when (details.expense.payerId) {
            currentUserId -> shareOf(details, otherPersonId)
            otherPersonId -> -shareOf(details, currentUserId)
            else -> 0L
        }
    }

    private fun shareOf(details: ExpenseDetails, personId: Long): Long {
        return details.participants.firstOrNull { it.personId == personId }?.shareMinorUnits ?: 0L
    }

    private fun peopleFrom(
        expenses: List<ExpenseDetails>,
        extraPeople: List<Person>
    ): Map<Long, Person> {
        val people = linkedMapOf<Long, Person>()
        extraPeople.forEach { people[it.id] = it }
        expenses.forEach { details ->
            people[details.payer.id] = details.payer
            details.participants.forEach { participant ->
                people[participant.person.id] = participant.person
            }
        }
        return people
    }
}
