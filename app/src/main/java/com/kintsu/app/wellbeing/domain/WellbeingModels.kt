package com.kintsu.app.wellbeing.domain

enum class StrictnessLevel {
    GENTLE,    // Dismissible with one tap (logged)
    BALANCED,  // Dismissible after short pause and choosing reason
    STRICT     // Slow 60s emergency exit with confirmation phrase required
}

enum class InterventionStyle {
    BREATHING,          // Animated breathing prompt
    INTENTIONAL_CHOICE, // Intentional question with choices (Why are you opening?)
    GENTLE_DELAY,       // Simple countdown bar with no distracting bounce
    REFLECTION          // "Is this how you want to spend the next few minutes?"
}

enum class FocusLockStrictness {
    FLEXIBLE,        // Normal pause / end
    COMMITTED,       // Confirmed duration, confirmation flow to exit
    DEEP_COMMITMENT  // Friction reflection period before early exit
}

enum class WebsiteBlockMode {
    ALWAYS_BLOCKED,
    SCHEDULED,
    FOCUS_ONLY,
    LIMITED_ACCESS
}

enum class ProtectionState {
    ACTIVE,
    PAUSED,
    NO_RESTRICTIONS,
    SETUP_REQUIRED
}

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val categoryName: String = "Other",
    val usageMinutesToday: Long = 0,
    val isSelected: Boolean = false,
    val activeRuleMode: String = "ALLOW"
)

data class DistractionMetricSummary(
    val screenTimeTodayMinutes: Long = 0,
    val remainingBudgetMinutes: Long? = null,
    val distractionsInterruptedToday: Int = 0,
    val activeFocusLockTitle: String? = null,
    val activeFocusLockRemainingMinutes: Long? = null
)


enum class BlockMode {
    DAILY_LIMIT,    // Per-app or per-group daily time limit
    SCHEDULE,       // Time windows on chosen weekdays (supports overnight)
    FOCUS,          // Blocked automatically during active Focus session
    MINDFUL_PAUSE,  // Short breathing animation before opening app
    BEDTIME,        // Nightly wind-down schedule with optional DND
    OPEN_LIMIT      // Cap on number of app opens per day
}

enum class PredefinedAppCategory {
    SOCIAL,
    STREAMING,
    GAMES,
    SHOPPING,
    NEWS,
    MESSAGING,
    CUSTOM
}

/**
 * Hardcoded Protected Essentials:
 * CAN NEVER BE BLOCKED UNDER ANY CIRCUMSTANCES.
 */
object ProtectedApps {
    val SYSTEM_CRITICAL_PACKAGES = setOf(
        // Phone / Dialer / Emergency
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.phone",
        "com.android.server.telecom",

        // SMS / Messaging
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.samsung.android.messaging",

        // System Settings
        "com.android.settings",

        // Clock & Alarms
        "com.google.android.deskclock",
        "com.android.deskclock",
        "com.sec.android.app.clockpackage",

        // System UI & Home Launchers
        "com.android.systemui",
        "com.google.android.apps.nexuslauncher",
        "com.sec.android.app.launcher",
        "com.nothing.launcher",

        // Kintsu Self
        "com.kintsu.app",
        "com.kintsu.app.debug"
    )

    fun isProtected(packageName: String): Boolean {
        if (packageName.isBlank()) return true
        if (SYSTEM_CRITICAL_PACKAGES.contains(packageName.trim())) return true
        if (packageName.startsWith("com.android.inputmethod") || packageName.contains("keyboard") || packageName.contains("gboard")) {
            return true // Keyboards are always protected
        }
        return false
    }
}
