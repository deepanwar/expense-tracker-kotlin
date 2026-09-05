package com.example.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.local.dao.GroupDao
import com.example.expensetracker.data.local.dao.GroupMemberDao
import com.example.expensetracker.data.local.dao.PersonDao
import com.example.expensetracker.data.local.entity.GroupEntity
import com.example.expensetracker.data.local.entity.GroupMemberEntity
import com.example.expensetracker.data.local.entity.PersonEntity

@Database(
    entities = [
        PersonEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class ExpenseTrackerDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
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
