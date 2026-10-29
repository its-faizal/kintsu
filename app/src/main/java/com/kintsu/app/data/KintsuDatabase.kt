package com.kintsu.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kintsu.app.data.dao.DailyIntentionDao
import com.kintsu.app.data.dao.FocusDao
import com.kintsu.app.data.dao.GoalDao
import com.kintsu.app.data.dao.HabitDao
import com.kintsu.app.data.dao.JournalDao
import com.kintsu.app.data.dao.SubtaskDao
import com.kintsu.app.data.dao.TaskCompletionDao
import com.kintsu.app.data.dao.TaskDao
import com.kintsu.app.data.dao.WeeklyReviewDao
import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.SubtaskEntity
import com.kintsu.app.data.model.TaskCompletionEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.WeeklyReviewEntity
import com.kintsu.app.sync.dao.SyncDao
import com.kintsu.app.sync.model.SyncQueueEntity
import com.kintsu.app.sync.model.SyncRegistryEntity

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habits ADD COLUMN reminderTimes TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE habits ADD COLUMN frequencyTarget INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE habits ADD COLUMN routineGroup TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE habit_logs ADD COLUMN repairedAt INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE habit_logs ADD COLUMN isCrack INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE habit_logs ADD COLUMN completionCount INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE habit_logs ADD COLUMN note TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE habit_logs ADD COLUMN mood TEXT DEFAULT NULL")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS focus_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                dateEpochDay INTEGER NOT NULL,
                durationMinutes INTEGER NOT NULL,
                linkedHabitId INTEGER,
                isCompleted INTEGER NOT NULL,
                startTimeMillis INTEGER NOT NULL,
                endTimeMillis INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS daily_intentions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                dateEpochDay INTEGER NOT NULL,
                text TEXT NOT NULL,
                isCompleted INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS journal_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                dateEpochDay INTEGER NOT NULL,
                content TEXT NOT NULL,
                mood TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS weekly_reviews (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                weekStartEpochDay INTEGER NOT NULL,
                weekEndEpochDay INTEGER NOT NULL,
                completionRate REAL NOT NULL,
                bestStreak INTEGER NOT NULL,
                totalCracks INTEGER NOT NULL,
                totalRepairs INTEGER NOT NULL,
                focusMinutes INTEGER NOT NULL,
                mostConsistentHabitName TEXT,
                leastConsistentHabitName TEXT,
                goalsProgressSummary TEXT NOT NULL,
                reflection1 TEXT NOT NULL,
                reflection2 TEXT NOT NULL,
                reflection3 TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habits ADD COLUMN linkedGoalId INTEGER DEFAULT NULL")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN recurrenceType TEXT NOT NULL DEFAULT 'NEVER'")
        db.execSQL("ALTER TABLE tasks ADD COLUMN recurrenceWeekdaysMask INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE tasks ADD COLUMN recurrenceActive INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE tasks ADD COLUMN plannedDateEpochDay INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE tasks ADD COLUMN plannedTimeBlock TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE tasks ADD COLUMN planOrder INTEGER NOT NULL DEFAULT 0")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS subtasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                taskId INTEGER NOT NULL,
                title TEXT NOT NULL,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                orderIndex INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(taskId) REFERENCES tasks(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_taskId ON subtasks(taskId)")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS task_completions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                taskId INTEGER NOT NULL,
                epochDay INTEGER NOT NULL,
                completedAt INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_task_completions_taskId ON task_completions(taskId)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE focus_sessions ADD COLUMN linkedTaskId INTEGER DEFAULT NULL")
        } catch (_: Exception) {
            // Column may already exist
        }
        db.execSQL("CREATE INDEX IF NOT EXISTS index_focus_sessions_linkedTaskId ON focus_sessions(linkedTaskId)")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS sync_registry (
                localTableName TEXT NOT NULL,
                localRecordId INTEGER NOT NULL,
                syncUuid TEXT NOT NULL,
                updatedAt INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (localTableName, localRecordId)
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_sync_registry_syncUuid ON sync_registry(syncUuid)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_registry_localTableName ON sync_registry(localTableName)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS sync_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                syncUuid TEXT NOT NULL,
                tableName TEXT NOT NULL,
                operation TEXT NOT NULL,
                payloadJson TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                retryCount INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_queue_syncUuid ON sync_queue(syncUuid)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_queue_tableName ON sync_queue(tableName)")
    }
}

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        TaskEntity::class,
        GoalEntity::class,
        FocusSessionEntity::class,
        DailyIntentionEntity::class,
        JournalEntryEntity::class,
        WeeklyReviewEntity::class,
        SubtaskEntity::class,
        TaskCompletionEntity::class,
        SyncRegistryEntity::class,
        SyncQueueEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class KintsuDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun taskDao(): TaskDao
    abstract fun goalDao(): GoalDao
    abstract fun focusDao(): FocusDao
    abstract fun dailyIntentionDao(): DailyIntentionDao
    abstract fun journalDao(): JournalDao
    abstract fun weeklyReviewDao(): WeeklyReviewDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    abstract fun syncDao(): SyncDao

    companion object {
        @Volatile
        private var INSTANCE: KintsuDatabase? = null

        fun getDatabase(context: Context): KintsuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KintsuDatabase::class.java,
                    "kintsu_v1_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
