package com.example.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.local.dao.ExpenseDao
import com.example.expensetracker.data.local.dao.GroupDao
import com.example.expensetracker.data.local.dao.GroupMemberDao
import com.example.expensetracker.data.local.dao.PersonDao
import com.example.expensetracker.data.local.dao.SettlementDao
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.ExpenseParticipantEntity
import com.example.expensetracker.data.local.entity.GroupEntity
import com.example.expensetracker.data.local.entity.GroupMemberEntity
import com.example.expensetracker.data.local.entity.PersonEntity
import com.example.expensetracker.data.local.entity.SettlementEntity

@Database(
    entities = [
        PersonEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        ExpenseEntity::class,
        ExpenseParticipantEntity::class,
        SettlementEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class ExpenseTrackerDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun settlementDao(): SettlementDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `groups` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `icon` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `archivedAt` INTEGER
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `group_members` (
                `groupId` INTEGER NOT NULL,
                `personId` INTEGER NOT NULL,
                `joinedAt` INTEGER NOT NULL,
                PRIMARY KEY(`groupId`, `personId`),
                FOREIGN KEY(`groupId`) REFERENCES `groups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`personId`) REFERENCES `persons`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_group_members_groupId` ON `group_members` (`groupId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_group_members_personId` ON `group_members` (`personId`)"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `expenses` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `description` TEXT NOT NULL,
                `amountMinorUnits` INTEGER NOT NULL,
                `date` INTEGER NOT NULL,
                `groupId` INTEGER,
                `payerId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                FOREIGN KEY(`groupId`) REFERENCES `groups`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(`payerId`) REFERENCES `persons`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_groupId` ON `expenses` (`groupId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_payerId` ON `expenses` (`payerId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_date` ON `expenses` (`date`)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `expense_participants` (
                `expenseId` INTEGER NOT NULL,
                `personId` INTEGER NOT NULL,
                `shareMinorUnits` INTEGER NOT NULL,
                PRIMARY KEY(`expenseId`, `personId`),
                FOREIGN KEY(`expenseId`) REFERENCES `expenses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`personId`) REFERENCES `persons`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_expense_participants_expenseId` ON `expense_participants` (`expenseId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_expense_participants_personId` ON `expense_participants` (`personId`)"
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `expenses` ADD COLUMN `splitMethod` TEXT NOT NULL DEFAULT 'EQUAL'"
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `settlements` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `fromPersonId` INTEGER NOT NULL,
                `toPersonId` INTEGER NOT NULL,
                `amountMinorUnits` INTEGER NOT NULL,
                `groupId` INTEGER,
                `note` TEXT,
                `date` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`fromPersonId`) REFERENCES `persons`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`toPersonId`) REFERENCES `persons`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`groupId`) REFERENCES `groups`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_settlements_fromPersonId` ON `settlements` (`fromPersonId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_settlements_toPersonId` ON `settlements` (`toPersonId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_settlements_groupId` ON `settlements` (`groupId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_settlements_date` ON `settlements` (`date`)")
    }
}
