package com.kintsu.app.domain

/**
 * Configuration constants for the Kintsugi Gold Repair feature.
 */
object GoldRepairConfig {
    /**
     * Default time window in hours to repair a missed habit day with gold.
     */
    const val REPAIR_WINDOW_HOURS: Long = 48

    /**
     * Maximum number of gold repairs allowed per habit per calendar week.
     */
    const val MAX_REPAIRS_PER_HABIT_PER_WEEK: Int = 1
}
