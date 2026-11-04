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
import com.kintsu.app.data.dao.WellbeingDao
import com.kintsu.app.data.model.AchievementEntity
import com.kintsu.app.data.model.AppGroupEntity
import com.kintsu.app.data.model.BlockedWebsiteEntity
import com.kintsu.app.data.model.BlockRuleEntity
import com.kintsu.app.data.model.BlockScheduleEntity
import com.kintsu.app.data.model.CommitmentLockEntity
import com.kintsu.app.data.model.DailyIntentionEntity
import com.kintsu.app.data.model.DigitalDetoxEntity
import com.kintsu.app.data.model.DistractionInterventionLogEntity
import com.kintsu.app.data.model.FocusLockSessionEntity
import com.kintsu.app.data.model.FocusSessionEntity
import com.kintsu.app.data.model.GoalEntity
import com.kintsu.app.data.model.HabitEntity
import com.kintsu.app.data.model.HabitLogEntity
import com.kintsu.app.data.model.IntentionalBreakLogEntity
import com.kintsu.app.data.model.JournalCollectionEntity
import com.kintsu.app.data.model.JournalEntryEntity
import com.kintsu.app.data.model.JournalTemplateEntity
import com.kintsu.app.data.model.PersonalRewardEntity
import com.kintsu.app.data.model.QuitHabitEntity
import com.kintsu.app.data.model.ShopItemEntity
import com.kintsu.app.data.model.StylePresetEntity
import com.kintsu.app.data.model.SubtaskEntity
import com.kintsu.app.data.model.TaskCompletionEntity
import com.kintsu.app.data.model.TaskEntity
import com.kintsu.app.data.model.UrgeLogEntity
import com.kintsu.app.data.model.WalletTransactionEntity
import com.kintsu.app.data.model.WeeklyReviewEntity
import com.kintsu.app.sync.dao.SyncDao
import com.kintsu.app.sync.model.SyncQueueEntity
import com.kintsu.app.sync.model.SyncRegistryEntity

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        fun addColumnSafe(sql: String) {
            try { db.execSQL(sql) } catch (_: Exception) {}
        }
        addColumnSafe("ALTER TABLE shop_inventory ADD COLUMN requiredStreakDays INTEGER NOT NULL DEFAULT 0")
        addColumnSafe("ALTER TABLE wallet_transactions ADD COLUMN previousHash TEXT NOT NULL DEFAULT 'GENESIS'")
        addColumnSafe("ALTER TABLE wallet_transactions ADD COLUMN transactionHash TEXT NOT NULL DEFAULT ''")
        addColumnSafe("ALTER TABLE wallet_transactions ADD COLUMN signature TEXT NOT NULL DEFAULT ''")
        addColumnSafe("ALTER TABLE wallet_transactions ADD COLUMN multiplier REAL NOT NULL DEFAULT 1.0")
        addColumnSafe("ALTER TABLE wallet_transactions ADD COLUMN isDeduction INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        fun addColumnSafe(sql: String) {
            try { db.execSQL(sql) } catch (_: Exception) {}
        }
        addColumnSafe("ALTER TABLE wallet_transactions ADD COLUMN previousHash TEXT NOT NULL DEFAULT 'GENESIS'")
        addColumnSafe("ALTER TABLE wallet_transactions ADD COLUMN transactionHash TEXT NOT NULL DEFAULT ''")
        addColumnSafe("ALTER TABLE wallet_transactions ADD COLUMN signature TEXT NOT NULL DEFAULT ''")
        addColumnSafe("ALTER TABLE wallet_transactions ADD COLUMN multiplier REAL NOT NULL DEFAULT 1.0")
        addColumnSafe("ALTER TABLE wallet_transactions ADD COLUMN isDeduction INTEGER NOT NULL DEFAULT 0")
        addColumnSafe("ALTER TABLE shop_inventory ADD COLUMN requiredStreakDays INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        fun addColumnSafe(sql: String) {
            try { db.execSQL(sql) } catch (_: Exception) {}
        }
        addColumnSafe("ALTER TABLE tasks ADD COLUMN category TEXT DEFAULT NULL")
        addColumnSafe("ALTER TABLE tasks ADD COLUMN notes TEXT DEFAULT NULL")
        addColumnSafe("ALTER TABLE tasks ADD COLUMN estimatedMinutes INTEGER DEFAULT NULL")
        addColumnSafe("ALTER TABLE goals ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
        addColumnSafe("ALTER TABLE goals ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
        addColumnSafe("ALTER TABLE goals ADD COLUMN notes TEXT DEFAULT NULL")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        fun addColumnSafe(sql: String) {
            try { db.execSQL(sql) } catch (_: Exception) {}
        }
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN title TEXT DEFAULT NULL")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN moodNote TEXT DEFAULT NULL")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN emotions TEXT NOT NULL DEFAULT ''")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN photoUris TEXT NOT NULL DEFAULT ''")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN collectionId INTEGER NOT NULL DEFAULT 1")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN linkedHabitIds TEXT NOT NULL DEFAULT ''")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN linkedTaskIds TEXT NOT NULL DEFAULT ''")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN linkedGoalIds TEXT NOT NULL DEFAULT ''")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN linkedFocusSessionId INTEGER DEFAULT NULL")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN isPrivate INTEGER NOT NULL DEFAULT 0")
        addColumnSafe("ALTER TABLE journal_entries ADD COLUMN promptUsed TEXT DEFAULT NULL")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS journal_collections (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                colorHex TEXT NOT NULL DEFAULT '#7F9A83',
                iconName TEXT NOT NULL DEFAULT 'book',
                isPrivate INTEGER NOT NULL DEFAULT 0,
                isArchived INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS journal_templates (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                templateContent TEXT NOT NULL,
                category TEXT NOT NULL,
                isCustom INTEGER NOT NULL DEFAULT 0,
                isFavorite INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        val now = System.currentTimeMillis()
        db.execSQL("INSERT OR IGNORE INTO journal_collections (id, name, colorHex, iconName, isPrivate, isArchived, createdAt) VALUES (1, 'Personal Journal', '#7F9A83', 'book', 0, 0, $now)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_collectionId ON journal_entries(collectionId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_dateEpochDay ON journal_entries(dateEpochDay)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_isFavorite ON journal_entries(isFavorite)")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS achievements (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                pointsAwarded INTEGER NOT NULL,
                badgeIcon TEXT NOT NULL,
                isEarned INTEGER NOT NULL DEFAULT 0,
                earnedAtMillis INTEGER,
                category TEXT NOT NULL DEFAULT 'MILESTONE'
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_achievements_category ON achievements(category)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_achievements_isEarned ON achievements(isEarned)")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE shop_inventory ADD COLUMN category TEXT NOT NULL DEFAULT 'THEMES'")
        db.execSQL("ALTER TABLE shop_inventory ADD COLUMN previewAssetId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE shop_inventory ADD COLUMN bundleId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE shop_inventory ADD COLUMN collectionName TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE shop_inventory ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE shop_inventory ADD COLUMN tag TEXT NOT NULL DEFAULT ''")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS wallet_transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                amount INTEGER NOT NULL,
                type TEXT NOT NULL,
                reason TEXT NOT NULL,
                sourceActivity TEXT NOT NULL,
                sourceEventId TEXT NOT NULL,
                timestampMillis INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_wallet_transactions_timestampMillis ON wallet_transactions(timestampMillis)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_wallet_transactions_sourceEventId ON wallet_transactions(sourceEventId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_wallet_transactions_sourceActivity ON wallet_transactions(sourceActivity)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS personal_rewards (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                costCoins INTEGER NOT NULL DEFAULT 50,
                category TEXT NOT NULL DEFAULT 'Leisure',
                iconName TEXT NOT NULL DEFAULT 'Star',
                isArchived INTEGER NOT NULL DEFAULT 0,
                redemptionsCount INTEGER NOT NULL DEFAULT 0,
                lastRedeemedAtMillis INTEGER,
                createdAtMillis INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_personal_rewards_category ON personal_rewards(category)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_personal_rewards_isArchived ON personal_rewards(isArchived)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS style_presets (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                themeName TEXT NOT NULL,
                accentName TEXT NOT NULL,
                iconId TEXT NOT NULL,
                widgetSkinId TEXT NOT NULL,
                focusFaceId TEXT NOT NULL,
                createdAtMillis INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_style_presets_name ON style_presets(name)")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS blocked_websites (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                domain TEXT NOT NULL,
                groupName TEXT NOT NULL DEFAULT 'Social',
                mode TEXT NOT NULL DEFAULT 'ALWAYS_BLOCKED',
                matchSubdomains INTEGER NOT NULL DEFAULT 1,
                isEnabled INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_blocked_websites_domain ON blocked_websites(domain)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_blocked_websites_groupName ON blocked_websites(groupName)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_blocked_websites_isEnabled ON blocked_websites(isEnabled)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS focus_lock_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL DEFAULT 'Deep Focus',
                durationMinutes INTEGER NOT NULL DEFAULT 25,
                startTimeMillis INTEGER NOT NULL DEFAULT 0,
                endTimeMillis INTEGER NOT NULL DEFAULT 0,
                restrictedAppGroupIds TEXT NOT NULL DEFAULT '',
                restrictedWebsitesEnabled INTEGER NOT NULL DEFAULT 1,
                strictnessMode TEXT NOT NULL DEFAULT 'COMMITTED',
                breaksAllowed INTEGER NOT NULL DEFAULT 1,
                maxBreaks INTEGER NOT NULL DEFAULT 2,
                breaksTaken INTEGER NOT NULL DEFAULT 0,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                wasEarlyExit INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_focus_lock_sessions_isCompleted ON focus_lock_sessions(isCompleted)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_focus_lock_sessions_startTimeMillis ON focus_lock_sessions(startTimeMillis)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS distraction_interventions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                packageName TEXT NOT NULL,
                timestampMillis INTEGER NOT NULL DEFAULT 0,
                interventionStyle TEXT NOT NULL DEFAULT 'BREATHING',
                userAction TEXT NOT NULL,
                choiceReason TEXT,
                alternativeChosen TEXT
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_distraction_interventions_packageName ON distraction_interventions(packageName)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_distraction_interventions_timestampMillis ON distraction_interventions(timestampMillis)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS intentional_breaks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                targetPackageOrDomain TEXT NOT NULL,
                durationMinutes INTEGER NOT NULL,
                timestampMillis INTEGER NOT NULL DEFAULT 0,
                associatedSessionId INTEGER,
                reason TEXT
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_intentional_breaks_timestampMillis ON intentional_breaks(timestampMillis)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS app_groups (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                categoryType TEXT NOT NULL,
                isPredefined INTEGER NOT NULL DEFAULT 0,
                packageNames TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_app_groups_name ON app_groups(name)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_app_groups_categoryType ON app_groups(categoryType)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS block_rules (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                appGroupId INTEGER NOT NULL,
                isEnabled INTEGER NOT NULL DEFAULT 1,
                mode TEXT NOT NULL DEFAULT 'DAILY_LIMIT',
                dailyLimitMinutes INTEGER NOT NULL DEFAULT 60,
                maxOpensPerDay INTEGER NOT NULL DEFAULT 0,
                strictnessLevel TEXT NOT NULL DEFAULT 'GENTLE',
                pauseBreathingSeconds INTEGER NOT NULL DEFAULT 10,
                pauseQuestion TEXT NOT NULL DEFAULT 'What are you here for?',
                createdAt INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_block_rules_appGroupId ON block_rules(appGroupId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_block_rules_isEnabled ON block_rules(isEnabled)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_block_rules_mode ON block_rules(mode)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS block_schedules (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ruleId INTEGER NOT NULL,
                startMinuteOfDay INTEGER NOT NULL DEFAULT 1320,
                endMinuteOfDay INTEGER NOT NULL DEFAULT 420,
                daysOfWeekMask INTEGER NOT NULL DEFAULT 127,
                isEnabled INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_block_schedules_ruleId ON block_schedules(ruleId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_block_schedules_isEnabled ON block_schedules(isEnabled)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS digital_detox_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                startTimeMillis INTEGER NOT NULL,
                durationMinutes INTEGER NOT NULL,
                endTimeMillis INTEGER NOT NULL,
                intention TEXT,
                allowedPackages TEXT NOT NULL DEFAULT '',
                enableDnd INTEGER NOT NULL DEFAULT 1,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                isRepairedWithGold INTEGER NOT NULL DEFAULT 0,
                timeReclaimedMinutes INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_digital_detox_sessions_startTimeMillis ON digital_detox_sessions(startTimeMillis)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_digital_detox_sessions_isCompleted ON digital_detox_sessions(isCompleted)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS quit_habits (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                iconName TEXT NOT NULL DEFAULT 'shield',
                colorHex TEXT NOT NULL DEFAULT '#B8935A',
                cleanStreakStartMillis INTEGER NOT NULL,
                bestStreakDays INTEGER NOT NULL DEFAULT 0,
                totalSlips INTEGER NOT NULL DEFAULT 0,
                totalRepairs INTEGER NOT NULL DEFAULT 0,
                linkedAppGroupId INTEGER,
                ifThenPlan TEXT,
                alternativeActivities TEXT,
                emergencyContactName TEXT,
                emergencyContactPhone TEXT,
                createdAt INTEGER NOT NULL,
                isArchived INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quit_habits_name ON quit_habits(name)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quit_habits_cleanStreakStartMillis ON quit_habits(cleanStreakStartMillis)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quit_habits_isArchived ON quit_habits(isArchived)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS urge_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                quitHabitId INTEGER NOT NULL,
                timestampMillis INTEGER NOT NULL,
                intensity INTEGER NOT NULL DEFAULT 5,
                triggerTags TEXT NOT NULL DEFAULT '',
                note TEXT,
                actionTaken TEXT NOT NULL DEFAULT 'SURFED'
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_urge_logs_quitHabitId ON urge_logs(quitHabitId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_urge_logs_timestampMillis ON urge_logs(timestampMillis)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS commitment_locks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                targetType TEXT NOT NULL,
                targetId INTEGER NOT NULL,
                pendingAction TEXT NOT NULL,
                pendingPayloadJson TEXT NOT NULL DEFAULT '',
                requestedAtMillis INTEGER NOT NULL,
                unlocksAtMillis INTEGER NOT NULL,
                isCancelled INTEGER NOT NULL DEFAULT 0,
                isApplied INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_commitment_locks_targetType ON commitment_locks(targetType)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_commitment_locks_unlocksAtMillis ON commitment_locks(unlocksAtMillis)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_commitment_locks_isCancelled ON commitment_locks(isCancelled)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_commitment_locks_isApplied ON commitment_locks(isApplied)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS shop_inventory (
                id TEXT PRIMARY KEY NOT NULL,
                itemType TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                costCoins INTEGER NOT NULL,
                isUnlocked INTEGER NOT NULL DEFAULT 0,
                isEquipped INTEGER NOT NULL DEFAULT 0,
                purchasedAtMillis INTEGER DEFAULT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_shop_inventory_itemType ON shop_inventory(itemType)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_shop_inventory_isUnlocked ON shop_inventory(isUnlocked)")
    }
}

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
        SyncQueueEntity::class,
        AppGroupEntity::class,
        BlockRuleEntity::class,
        BlockScheduleEntity::class,
        DigitalDetoxEntity::class,
        QuitHabitEntity::class,
        UrgeLogEntity::class,
        CommitmentLockEntity::class,
        ShopItemEntity::class,
        BlockedWebsiteEntity::class,
        FocusLockSessionEntity::class,
        DistractionInterventionLogEntity::class,
        IntentionalBreakLogEntity::class,
        WalletTransactionEntity::class,
        PersonalRewardEntity::class,
        StylePresetEntity::class,
        AchievementEntity::class,
        JournalCollectionEntity::class,
        JournalTemplateEntity::class
    ],
    version = 16,
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
    abstract fun wellbeingDao(): WellbeingDao

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
                    .addMigrations(
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16
                    )
                    .build()
                val appContext = context.applicationContext
                instance.invalidationTracker.addObserver(
                    object : androidx.room.InvalidationTracker.Observer(
                        arrayOf("habits", "habit_logs", "tasks", "task_completions", "goals", "focus_sessions", "daily_intentions", "journal_entries")
                    ) {
                        override fun onInvalidated(tables: Set<String>) {
                            com.kintsu.app.widget.KintsuWidgetUpdater.triggerUpdate(appContext)
                        }
                    }
                )
                INSTANCE = instance
                instance
            }
        }
    }
}
