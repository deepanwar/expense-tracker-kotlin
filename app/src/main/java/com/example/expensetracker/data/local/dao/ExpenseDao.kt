package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.ExpenseParticipantEntity
import com.example.expensetracker.data.local.entity.ExpenseWithParticipants
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query(
        """
        SELECT * FROM expenses
        WHERE payerId = :personId
        ORDER BY date DESC, id DESC
        """
    )
    fun observeForPerson(personId: Long): Flow<List<ExpenseWithParticipants>>

    @Transaction
    @Query(
        """
        SELECT * FROM expenses
        WHERE groupId = :groupId
        ORDER BY date DESC, id DESC
        """
    )
    fun observeByGroupId(groupId: Long): Flow<List<ExpenseWithParticipants>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id")
    fun observeById(id: Long): Flow<ExpenseWithParticipants?>

    @Transaction
    @Query(
        """
        SELECT * FROM expenses
        WHERE payerId = :personId OR id IN (
            SELECT expenseId FROM expense_participants WHERE personId = :personId
        )
        ORDER BY date DESC, id DESC
        """
    )
    fun observeExpensesInvolvingPerson(personId: Long): Flow<List<ExpenseWithParticipants>>

    @Transaction
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun observeAllExpenses(): Flow<List<ExpenseWithParticipants>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertParticipants(participants: List<ExpenseParticipantEntity>)

    @Query("DELETE FROM expense_participants WHERE expenseId = :expenseId")
    suspend fun deleteParticipants(expenseId: Long)
}
