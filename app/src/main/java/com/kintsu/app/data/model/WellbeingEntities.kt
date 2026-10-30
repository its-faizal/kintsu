package com.kintsu.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a logical group of applications for digital wellbeing rules
 * (e.g., Social, Video/Streaming, Games, Shopping, News, Custom).
 */
@Entity(
    tableName = "app_groups",
    indices = [
        Index("name"),
        Index("categoryType")
    ]
)
data class AppGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryType: String, // SOCIAL, STREAMING, GAMES, SHOPPING, NEWS, MESSAGING, CUSTOM
    val isPredefined: Boolean = false,
    val packageNames: String = "", // Comma-separated package names
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A block rule enforcing time limits, schedules, focus shielding, or mindful pauses.
 */
@Entity(
    tableName = "block_rules",
    indices = [
        Index("appGroupId"),
        Index("isEnabled"),
        Index("mode")
    ]
)
data class BlockRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appGroupId: Long,
    val isEnabled: Boolean = true,
    val mode: String = "DAILY_LIMIT", // DAILY_LIMIT, SCHEDULE, FOCUS, MINDFUL_PAUSE, BEDTIME, OPEN_LIMIT
    val dailyLimitMinutes: Int = 60,
    val maxOpensPerDay: Int = 0,
    val strictnessLevel: String = "GENTLE", // GENTLE, BALANCED, STRICT
    val pauseBreathingSeconds: Int = 10,
    val pauseQuestion: String = "What are you here for?",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Schedule windows for a block rule (supports overnight windows like 22:00 -> 07:00).
 */
@Entity(
    tableName = "block_schedules",
    indices = [
        Index("ruleId"),
        Index("isEnabled")
    ]
)
data class BlockScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleId: Long,
    val startMinuteOfDay: Int = 1320, // 22:00 = 10 PM
    val endMinuteOfDay: Int = 420,    // 07:00 = 7 AM (Overnight)
    val daysOfWeekMask: Int = 127,     // Bitmask 1..7 (all 7 days)
    val isEnabled: Boolean = true
)

/**
 * Digital Detox session tracking.
 */
@Entity(
    tableName = "digital_detox_sessions",
    indices = [
        Index("startTimeMillis"),
        Index("isCompleted")
    ]
)
data class DigitalDetoxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val durationMinutes: Int, // 60, 180, 480, 1440, custom
    val endTimeMillis: Long,
    val intention: String? = null,
    val allowedPackages: String = "", // Comma-separated user allowlist
    val enableDnd: Boolean = true,
    val isCompleted: Boolean = false,
    val isRepairedWithGold: Boolean = false,
    val timeReclaimedMinutes: Int = 0
)

/**
 * Quit Habit / "Break a habit" model.
 * Extends the Kintsu ethos: slips are cracks that can be repaired with gold.
 */
@Entity(
    tableName = "quit_habits",
    indices = [
        Index("name"),
        Index("cleanStreakStartMillis"),
        Index("isArchived")
    ]
)
data class QuitHabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String = "shield",
    val colorHex: String = "#B8935A",
    val cleanStreakStartMillis: Long = System.currentTimeMillis(),
    val bestStreakDays: Int = 0,
    val totalSlips: Int = 0,
    val totalRepairs: Int = 0,
    val linkedAppGroupId: Long? = null,
    val ifThenPlan: String? = null,
    val alternativeActivities: String? = null, // Newline-separated
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

/**
 * Urge log entries for quit-habit tracking and urge-surfing.
 */
@Entity(
    tableName = "urge_logs",
    indices = [
        Index("quitHabitId"),
        Index("timestampMillis")
    ]
)
data class UrgeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quitHabitId: Long,
    val timestampMillis: Long = System.currentTimeMillis(),
    val intensity: Int = 5, // 1 to 10
    val triggerTags: String = "", // Comma-separated (e.g. Boredom, Stress, Bed)
    val note: String? = null,
    val actionTaken: String = "SURFED" // SURFED, SUBSTITUTED, SLIPPED
)

/**
 * Commitment lock preventing impulsive loosening of rules.
 * Making rules stricter takes effect immediately; making them weaker takes 24h.
 */
@Entity(
    tableName = "commitment_locks",
    indices = [
        Index("targetType"),
        Index("unlocksAtMillis"),
        Index("isCancelled"),
        Index("isApplied")
    ]
)
data class CommitmentLockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetType: String, // RULE, APP_GROUP, PROTECTION, UNINSTALL_FRICTION
    val targetId: Long,
    val pendingAction: String, // DELETE, WEAKEN, DISABLE, INCREASE_LIMIT
    val pendingPayloadJson: String = "",
    val requestedAtMillis: Long = System.currentTimeMillis(),
    val unlocksAtMillis: Long = System.currentTimeMillis() + 86_400_000L, // 24 hours
    val isCancelled: Boolean = false,
    val isApplied: Boolean = false
)

/**
 * Sanctuary Shop item catalog & user unlocks.
 */
@Entity(
    tableName = "shop_inventory",
    indices = [
        Index("itemType"),
        Index("isUnlocked")
    ]
)
data class ShopItemEntity(
    @PrimaryKey
    val id: String, // e.g. "theme_twilight", "sound_singing_bowl", "vial_gold_repair", "badge_zen_master"
    val itemType: String, // THEME, SOUNDSCAPE, GOLD_VIAL, BADGE_SLOT
    val title: String,
    val description: String,
    val costCoins: Int,
    val isUnlocked: Boolean = false,
    val isEquipped: Boolean = false,
    val purchasedAtMillis: Long? = null
)

/**
 * Blocked website entry supporting exact host and wildcard subdomains (*.domain.com).
 */
@Entity(
    tableName = "blocked_websites",
    indices = [
        Index("domain"),
        Index("groupName"),
        Index("isEnabled")
    ]
)
data class BlockedWebsiteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val domain: String,
    val groupName: String = "Social",
    val mode: String = "ALWAYS_BLOCKED", // ALWAYS_BLOCKED, SCHEDULED, FOCUS_ONLY, LIMITED_ACCESS
    val matchSubdomains: Boolean = true,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Focus Lock Session extending Focus with strict app & website blocking.
 */
@Entity(
    tableName = "focus_lock_sessions",
    indices = [
        Index("isCompleted"),
        Index("startTimeMillis")
    ]
)
data class FocusLockSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "Deep Focus",
    val durationMinutes: Int = 25,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val endTimeMillis: Long = System.currentTimeMillis() + 25 * 60 * 1000L,
    val restrictedAppGroupIds: String = "", // Comma-separated group IDs, or "ALL"
    val restrictedWebsitesEnabled: Boolean = true,
    val strictnessMode: String = "COMMITTED", // FLEXIBLE, COMMITTED, DEEP_COMMITMENT
    val breaksAllowed: Boolean = true,
    val maxBreaks: Int = 2,
    val breaksTaken: Int = 0,
    val isCompleted: Boolean = false,
    val wasEarlyExit: Boolean = false
)

/**
 * Real aggregated log of distraction interventions presented to the user.
 */
@Entity(
    tableName = "distraction_interventions",
    indices = [
        Index("packageName"),
        Index("timestampMillis")
    ]
)
data class DistractionInterventionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val interventionStyle: String = "BREATHING", // BREATHING, INTENTIONAL_CHOICE, GENTLE_DELAY, REFLECTION
    val userAction: String, // RETURNED_TO_FOCUS, CONTINUED_TO_APP, HEALTHY_ALTERNATIVE
    val choiceReason: String? = null,
    val alternativeChosen: String? = null
)

/**
 * Intentional break log for temporary access allowances.
 */
@Entity(
    tableName = "intentional_breaks",
    indices = [
        Index("timestampMillis")
    ]
)
data class IntentionalBreakLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetPackageOrDomain: String,
    val durationMinutes: Int,
    val timestampMillis: Long = System.currentTimeMillis(),
    val associatedSessionId: Long? = null,
    val reason: String? = null
)

