package com.example.expensetracker.data.remote

import android.util.Log
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.ExpenseParticipantEntity
import com.example.expensetracker.data.local.entity.GroupMemberEntity
import com.example.expensetracker.data.local.entity.SettlementEntity
import com.example.expensetracker.model.Group
import com.example.expensetracker.model.Person
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

interface CloudSync {
    suspend fun upsertPerson(person: Person)
    suspend fun deletePerson(id: Long)
    suspend fun upsertGroup(group: Group)
    suspend fun deleteGroup(id: Long)
    suspend fun upsertGroupMembers(members: List<GroupMemberEntity>)
    suspend fun deleteGroupMember(groupId: Long, personId: Long)
    suspend fun upsertExpense(expense: ExpenseEntity)
    suspend fun replaceExpenseParticipants(
        expenseId: Long,
        participants: List<ExpenseParticipantEntity>
    )
    suspend fun deleteExpense(id: Long)
    suspend fun upsertSettlement(settlement: SettlementEntity)
}

object NoOpCloudSync : CloudSync {
    override suspend fun upsertPerson(person: Person) = Unit
    override suspend fun deletePerson(id: Long) = Unit
    override suspend fun upsertGroup(group: Group) = Unit
    override suspend fun deleteGroup(id: Long) = Unit
    override suspend fun upsertGroupMembers(members: List<GroupMemberEntity>) = Unit
    override suspend fun deleteGroupMember(groupId: Long, personId: Long) = Unit
    override suspend fun upsertExpense(expense: ExpenseEntity) = Unit
    override suspend fun replaceExpenseParticipants(
        expenseId: Long,
        participants: List<ExpenseParticipantEntity>
    ) = Unit
    override suspend fun deleteExpense(id: Long) = Unit
    override suspend fun upsertSettlement(settlement: SettlementEntity) = Unit
}

class SupabaseCloudSync(
    private val client: SupabaseClient
) : CloudSync {
    override suspend fun upsertPerson(person: Person) {
        runCloud("upsertPerson") {
            client.from(TABLE_PERSONS).upsert(person.toRemote()) {
                onConflict = "id"
            }
        }
    }

    override suspend fun deletePerson(id: Long) {
        runCloud("deletePerson") {
            client.from(TABLE_PERSONS).delete {
                filter { eq("id", id) }
            }
        }
    }

    override suspend fun upsertGroup(group: Group) {
        runCloud("upsertGroup") {
            client.from(TABLE_GROUPS).upsert(group.toRemote()) {
                onConflict = "id"
            }
        }
    }

    override suspend fun deleteGroup(id: Long) {
        runCloud("deleteGroup") {
            client.from(TABLE_GROUPS).delete {
                filter { eq("id", id) }
            }
        }
    }

    override suspend fun upsertGroupMembers(members: List<GroupMemberEntity>) {
        if (members.isEmpty()) return
        runCloud("upsertGroupMembers") {
            client.from(TABLE_GROUP_MEMBERS).upsert(members.map { it.toRemote() }) {
                onConflict = "groupId,personId"
            }
        }
    }

    override suspend fun deleteGroupMember(groupId: Long, personId: Long) {
        runCloud("deleteGroupMember") {
            client.from(TABLE_GROUP_MEMBERS).delete {
                filter {
                    eq("groupId", groupId)
                    eq("personId", personId)
                }
            }
        }
    }

    override suspend fun upsertExpense(expense: ExpenseEntity) {
        runCloud("upsertExpense") {
            client.from(TABLE_EXPENSES).upsert(expense.toRemote()) {
                onConflict = "id"
            }
        }
    }

    override suspend fun replaceExpenseParticipants(
        expenseId: Long,
        participants: List<ExpenseParticipantEntity>
    ) {
        runCloud("replaceExpenseParticipants") {
            client.from(TABLE_EXPENSE_PARTICIPANTS).delete {
                filter { eq("expenseId", expenseId) }
            }
            if (participants.isNotEmpty()) {
                client.from(TABLE_EXPENSE_PARTICIPANTS).upsert(participants.map { it.toRemote() }) {
                    onConflict = "expenseId,personId"
                }
            }
        }
    }

    override suspend fun deleteExpense(id: Long) {
        runCloud("deleteExpense") {
            client.from(TABLE_EXPENSES).delete {
                filter { eq("id", id) }
            }
        }
    }

    override suspend fun upsertSettlement(settlement: SettlementEntity) {
        runCloud("upsertSettlement") {
            client.from(TABLE_SETTLEMENTS).upsert(settlement.toRemote()) {
                onConflict = "id"
            }
        }
    }

    private suspend fun runCloud(action: String, block: suspend () -> Unit) {
        if (client.auth.currentSessionOrNull() == null) return
        try {
            block()
        } catch (error: Exception) {
            Log.w(TAG, action, error)
        }
    }

    companion object {
        private const val TAG = "CloudSync"
        private const val TABLE_PERSONS = "persons"
        private const val TABLE_GROUPS = "groups"
        private const val TABLE_GROUP_MEMBERS = "group_members"
        private const val TABLE_EXPENSES = "expenses"
        private const val TABLE_EXPENSE_PARTICIPANTS = "expense_participants"
        private const val TABLE_SETTLEMENTS = "settlements"
    }
}
