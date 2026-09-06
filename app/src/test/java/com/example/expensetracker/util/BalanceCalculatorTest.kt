package com.example.expensetracker.util

import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseDetails
import com.example.expensetracker.model.ExpenseParticipant
import com.example.expensetracker.model.Group
import com.example.expensetracker.model.Person
import com.example.expensetracker.model.Settlement
import org.junit.Assert.assertEquals
import org.junit.Test

class BalanceCalculatorTest {
    private val you = person(1, "You")
    private val rahul = person(2, "Rahul")
    private val sarah = person(3, "Sarah")
    private val john = person(4, "John")
    private val goa = Group(id = 10, name = "Goa Trip", icon = "🌴")
    private val apartment = Group(id = 11, name = "Apartment", icon = "🏠")

    @Test
    fun singlePayerSplitsIntoPairwiseDebts() {
        val dinner = expense(
            id = 1,
            amount = 240_000,
            payer = rahul,
            shares = mapOf(you to 60_000, rahul to 60_000, sarah to 60_000, john to 60_000)
        )

        assertEquals(-60_000, BalanceCalculator.calculatePersonBalance(listOf(dinner), you.id, rahul.id))
        assertEquals(0, BalanceCalculator.calculatePersonBalance(listOf(dinner), you.id, sarah.id))
        assertEquals(60_000, BalanceCalculator.calculatePersonBalance(listOf(dinner), rahul.id, you.id))
    }

    @Test
    fun multipleExpensesNetToOneBalance() {
        val dinner = expense(
            id = 1,
            amount = 240_000,
            payer = rahul,
            shares = mapOf(you to 60_000, rahul to 60_000, sarah to 60_000, john to 60_000)
        )
        val lunch = expense(
            id = 2,
            amount = 100_000,
            payer = you,
            shares = mapOf(you to 50_000, rahul to 50_000)
        )

        assertEquals(-10_000, BalanceCalculator.calculatePersonBalance(listOf(dinner, lunch), you.id, rahul.id))
    }

    @Test
    fun noExpensesIsSettled() {
        assertEquals(0, BalanceCalculator.calculatePersonBalance(emptyList(), you.id, rahul.id))
        val overall = BalanceCalculator.calculateOverallBalance(emptyList(), you.id, listOf(rahul))
        assertEquals(0, overall.totalYouOwe)
        assertEquals(0, overall.totalYouAreOwed)
        assertEquals(0, overall.netBalance)
    }

    @Test
    fun groupScopeDiffersFromOverall() {
        val goaExpense = expense(
            id = 1,
            amount = 500_000,
            payer = you,
            shares = mapOf(you to 0, rahul to 500_000),
            group = goa
        )
        val apartmentExpense = expense(
            id = 2,
            amount = 200_000,
            payer = rahul,
            shares = mapOf(you to 200_000, rahul to 0),
            group = apartment
        )
        val expenses = listOf(goaExpense, apartmentExpense)

        val overall = BalanceCalculator.calculatePersonBalance(expenses, you.id, rahul.id)
        val goaNet = BalanceCalculator.calculateGroupBalance(expenses, you.id, goa.id).netBalance
        val apartmentNet = BalanceCalculator.calculateGroupBalance(expenses, you.id, apartment.id).netBalance

        assertEquals(300_000, overall)
        assertEquals(500_000, goaNet)
        assertEquals(-200_000, apartmentNet)
    }

    @Test
    fun thirdPartyPayerDoesNotCreatePairwiseDebt() {
        val taxi = expense(
            id = 1,
            amount = 90_000,
            payer = john,
            shares = mapOf(you to 30_000, rahul to 30_000, john to 30_000)
        )

        assertEquals(0, BalanceCalculator.calculatePersonBalance(listOf(taxi), you.id, rahul.id))
        assertEquals(-30_000, BalanceCalculator.calculatePersonBalance(listOf(taxi), you.id, john.id))
    }

    @Test
    fun overallSumsNetPersonBalances() {
        val expenses = listOf(
            expense(
                id = 1,
                amount = 80_000,
                payer = you,
                shares = mapOf(you to 0, rahul to 80_000)
            ),
            expense(
                id = 2,
                amount = 30_000,
                payer = sarah,
                shares = mapOf(you to 30_000, sarah to 0)
            )
        )
        val overall = BalanceCalculator.calculateOverallBalance(expenses, you.id)

        assertEquals(30_000, overall.totalYouOwe)
        assertEquals(80_000, overall.totalYouAreOwed)
        assertEquals(50_000, overall.netBalance)
    }

    @Test
    fun expenseDeltaMatchesPairwiseContribution() {
        val dinner = expense(
            id = 1,
            description = "Dinner",
            amount = 240_000,
            payer = rahul,
            shares = mapOf(you to 60_000, rahul to 180_000)
        )
        val movie = expense(
            id = 2,
            description = "Movie",
            amount = 100_000,
            payer = you,
            shares = mapOf(you to 50_000, rahul to 50_000)
        )

        assertEquals(-60_000L, BalanceCalculator.expenseDelta(dinner, you.id, rahul.id))
        assertEquals(50_000L, BalanceCalculator.expenseDelta(movie, you.id, rahul.id))
    }

    @Test
    fun settlementReducesPairwiseDebt() {
        val dinner = expense(
            id = 1,
            amount = 100_000,
            payer = you,
            shares = mapOf(you to 50_000, rahul to 50_000)
        )
        val payment = Settlement(
            fromPersonId = rahul.id,
            toPersonId = you.id,
            amountMinorUnits = 20_000,
            date = 1
        )

        assertEquals(50_000, BalanceCalculator.calculatePersonBalance(listOf(dinner), you.id, rahul.id))
        assertEquals(
            30_000,
            BalanceCalculator.calculatePersonBalance(listOf(dinner), you.id, rahul.id, listOf(payment))
        )
    }

    @Test
    fun groupSettlementDoesNotChangeOtherGroup() {
        val goaExpense = expense(
            id = 1,
            amount = 100_000,
            payer = you,
            shares = mapOf(you to 0, rahul to 100_000),
            group = goa
        )
        val payment = Settlement(
            fromPersonId = rahul.id,
            toPersonId = you.id,
            amountMinorUnits = 40_000,
            groupId = goa.id,
            date = 1
        )

        val goaNet = BalanceCalculator.calculateGroupBalance(
            expenses = listOf(goaExpense),
            currentUserId = you.id,
            groupId = goa.id,
            settlements = listOf(payment)
        ).netBalance
        val apartmentNet = BalanceCalculator.calculateGroupBalance(
            expenses = listOf(goaExpense),
            currentUserId = you.id,
            groupId = apartment.id,
            settlements = listOf(payment)
        ).netBalance

        assertEquals(60_000, goaNet)
        assertEquals(0, apartmentNet)
    }

    private fun person(id: Long, name: String) = Person(id = id, name = name)

    private fun expense(
        id: Long,
        amount: Long,
        payer: Person,
        shares: Map<Person, Long>,
        group: Group? = null,
        description: String = "e$id"
    ): ExpenseDetails {
        return ExpenseDetails(
            expense = Expense(
                id = id,
                description = description,
                amountMinorUnits = amount,
                date = 0,
                groupId = group?.id,
                payerId = payer.id
            ),
            payer = payer,
            group = group,
            participants = shares.map { (person, share) ->
                ExpenseParticipant(personId = person.id, shareMinorUnits = share, person = person)
            }
        )
    }
}
