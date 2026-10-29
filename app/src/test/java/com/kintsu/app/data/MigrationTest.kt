package com.kintsu.app.data

import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class MigrationTest {

    @Test
    fun testMigration2To3ExecutesExpectedSql() {
        val executedStatements = mutableListOf<String>()

        val dbProxy = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedStatements.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        MIGRATION_2_3.migrate(dbProxy)

        assertTrue(executedStatements.contains("ALTER TABLE habits ADD COLUMN reminderTimes TEXT NOT NULL DEFAULT ''"))
        assertTrue(executedStatements.contains("ALTER TABLE habits ADD COLUMN frequencyTarget INTEGER NOT NULL DEFAULT 1"))
        assertTrue(executedStatements.contains("ALTER TABLE habits ADD COLUMN routineGroup TEXT DEFAULT NULL"))
        assertTrue(executedStatements.contains("ALTER TABLE habit_logs ADD COLUMN repairedAt INTEGER DEFAULT NULL"))
        assertTrue(executedStatements.contains("ALTER TABLE habit_logs ADD COLUMN isCrack INTEGER NOT NULL DEFAULT 0"))
        assertTrue(executedStatements.contains("ALTER TABLE habit_logs ADD COLUMN completionCount INTEGER NOT NULL DEFAULT 1"))
        assertTrue(executedStatements.contains("ALTER TABLE habit_logs ADD COLUMN note TEXT DEFAULT NULL"))
        assertTrue(executedStatements.contains("ALTER TABLE habit_logs ADD COLUMN mood TEXT DEFAULT NULL"))
    }

    @Test
    fun testMigration3To4CreatesV2Tables() {
        val executedStatements = mutableListOf<String>()

        val dbProxy = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedStatements.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        MIGRATION_3_4.migrate(dbProxy)

        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS focus_sessions") })
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS daily_intentions") })
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS journal_entries") })
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS weekly_reviews") })
    }

    @Test
    fun testMigration4To5AddsLinkedGoalId() {
        val executedStatements = mutableListOf<String>()

        val dbProxy = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedStatements.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        MIGRATION_4_5.migrate(dbProxy)

        assertTrue(executedStatements.contains("ALTER TABLE habits ADD COLUMN linkedGoalId INTEGER DEFAULT NULL"))
    }

    @Test
    fun testMigration5To6AddsSubtasksAndRecurrence() {
        val executedStatements = mutableListOf<String>()

        val dbProxy = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedStatements.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        MIGRATION_5_6.migrate(dbProxy)

        assertTrue(executedStatements.contains("ALTER TABLE tasks ADD COLUMN recurrenceType TEXT NOT NULL DEFAULT 'NEVER'"))
        assertTrue(executedStatements.contains("ALTER TABLE tasks ADD COLUMN recurrenceWeekdaysMask INTEGER NOT NULL DEFAULT 0"))
        assertTrue(executedStatements.contains("ALTER TABLE tasks ADD COLUMN recurrenceActive INTEGER NOT NULL DEFAULT 1"))
        assertTrue(executedStatements.contains("ALTER TABLE tasks ADD COLUMN plannedDateEpochDay INTEGER DEFAULT NULL"))
        assertTrue(executedStatements.contains("ALTER TABLE tasks ADD COLUMN plannedTimeBlock TEXT DEFAULT NULL"))
        assertTrue(executedStatements.contains("ALTER TABLE tasks ADD COLUMN planOrder INTEGER NOT NULL DEFAULT 0"))
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS subtasks") })
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS task_completions") })
    }

    @Test
    fun testMigration6To7AddsLinkedTaskIdToFocusSessions() {
        val executedStatements = mutableListOf<String>()

        val dbProxy = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedStatements.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        MIGRATION_6_7.migrate(dbProxy)

        assertTrue(executedStatements.contains("ALTER TABLE focus_sessions ADD COLUMN linkedTaskId INTEGER DEFAULT NULL"))
        assertTrue(executedStatements.contains("CREATE INDEX IF NOT EXISTS index_focus_sessions_linkedTaskId ON focus_sessions(linkedTaskId)"))
    }

    @Test
    fun testMigration8To9CreatesWellbeingAndShopTables() {
        val executedStatements = mutableListOf<String>()

        val dbProxy = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedStatements.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        MIGRATION_8_9.migrate(dbProxy)

        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS app_groups") })
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS block_rules") })
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS block_schedules") })
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS digital_detox_sessions") })
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS quit_habits") })
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS urge_logs") })
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS commitment_locks") })
        assertTrue(executedStatements.any { it.contains("CREATE TABLE IF NOT EXISTS shop_inventory") })
    }
}
