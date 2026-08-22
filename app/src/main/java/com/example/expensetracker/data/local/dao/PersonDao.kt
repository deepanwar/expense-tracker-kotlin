package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.local.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE id = :id")
    fun observeById(id: Long): Flow<PersonEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(person: PersonEntity): Long

    @Update
    suspend fun update(person: PersonEntity)

    @Delete
    suspend fun delete(person: PersonEntity)

    @Query("SELECT * FROM persons WHERE phone IS NOT NULL AND phone = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): PersonEntity?

    @Query("SELECT * FROM persons WHERE email IS NOT NULL AND email = :email COLLATE NOCASE LIMIT 1")
    suspend fun findByEmail(email: String): PersonEntity?

    @Query("SELECT * FROM persons WHERE contactId IS NOT NULL AND contactId = :contactId LIMIT 1")
    suspend fun findByContactId(contactId: String): PersonEntity?

    @Query(
        """
        SELECT * FROM persons
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun searchByName(query: String): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE phone IS NOT NULL")
    suspend fun getAllWithPhone(): List<PersonEntity>
}
