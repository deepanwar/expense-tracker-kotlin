package com.example.expensetracker

import android.app.Application
import androidx.room.Room
import com.example.expensetracker.data.local.ExpenseTrackerDatabase
import com.example.expensetracker.data.local.MIGRATION_1_2
import com.example.expensetracker.data.repository.GroupRepository
import com.example.expensetracker.data.repository.PersonRepository

class ExpenseTrackerApplication : Application() {
    val database: ExpenseTrackerDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            ExpenseTrackerDatabase::class.java,
            "expense_tracker.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    val personRepository: PersonRepository by lazy {
        PersonRepository(database.personDao())
    }

    val groupRepository: GroupRepository by lazy {
        GroupRepository(database)
    }
}
