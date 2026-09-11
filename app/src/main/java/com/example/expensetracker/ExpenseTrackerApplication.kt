package com.example.expensetracker

import android.app.Application
import androidx.room.Room
import com.example.expensetracker.data.local.ExpenseTrackerDatabase
import com.example.expensetracker.data.local.MIGRATION_1_2
import com.example.expensetracker.data.local.MIGRATION_2_3
import com.example.expensetracker.data.local.MIGRATION_3_4
import com.example.expensetracker.data.remote.createExpenseTrackerSupabaseClient
import com.example.expensetracker.data.remote.SupabaseCloudSync
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.GroupRepository
import com.example.expensetracker.data.repository.PersonRepository
import com.example.expensetracker.data.repository.SettlementRepository

class ExpenseTrackerApplication : Application() {
    val database: ExpenseTrackerDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            ExpenseTrackerDatabase::class.java,
            "expense_tracker.db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }

    val supabase by lazy { createExpenseTrackerSupabaseClient() }

    val authRepository: AuthRepository by lazy {
        AuthRepository(supabase)
    }

    val cloudSync: SupabaseCloudSync by lazy {
        SupabaseCloudSync(supabase)
    }

    val personRepository: PersonRepository by lazy {
        PersonRepository(database.personDao(), cloudSync)
    }

    val groupRepository: GroupRepository by lazy {
        GroupRepository(database, cloudSync)
    }

    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(database, cloudSync)
    }

    val settlementRepository: SettlementRepository by lazy {
        SettlementRepository(database, cloudSync)
    }
}
