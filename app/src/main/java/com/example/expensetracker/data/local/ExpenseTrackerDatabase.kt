package com.example.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expensetracker.data.local.dao.PersonDao
import com.example.expensetracker.data.local.entity.PersonEntity

@Database(
    entities = [PersonEntity::class],
    version = 1,
    exportSchema = true
)
abstract class ExpenseTrackerDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
}
