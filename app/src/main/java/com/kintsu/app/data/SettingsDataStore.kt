package com.kintsu.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kintsu.app.ui.theme.AccentOption
import com.kintsu.app.ui.theme.AppTheme
import com.kintsu.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kintsu_settings")

enum class ContentDensity {
    COMFORTABLE,
    COMPACT
}

data class KintsuSettings(
    val displayName: String = "Seeker",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appTheme: AppTheme = AppTheme.SAGE_IVORY,
    val accentOption: AccentOption = AccentOption.SAGE,
    val contentDensity: ContentDensity = ContentDensity.COMFORTABLE,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val defaultReminderTime: String = "09:00",
    val notifyOnlyIfNotDone: Boolean = true,
    val goldRepairReminderEnabled: Boolean = true,
    val goldRepairEnabled: Boolean = true,
    val weekStartMonday: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val focusEnabled: Boolean = true,
    val focusDefaultPresetMinutes: Int = 25,
    val focusBreakMinutes: Int = 5,
    val focusSoundEnabled: Boolean = true,
    val focusVibrationEnabled: Boolean = true,
    val dailyIntentionEnabled: Boolean = true,
    val dailyIntentionReminderTime: String = "08:00",
    val journalEnabled: Boolean = true,
    val journalReminderTime: String = "21:00",
    val weeklyReviewEnabled: Boolean = true,
    val weeklyReviewReminderTime: String = "18:00",
    val homeSectionOrder: String = "DAILY_INTENTION,FOCUS,KEY_NUMBERS,QUICK_ACTIONS",
    val hiddenHomeSections: String = "",
    val quickActionsOrder: String = "ADD_HABIT,ADD_TASK,START_FOCUS,JOURNAL,NEW_GOAL",
    val accountEmail: String? = null,
    val accountDisplayName: String? = null,
    val accountUserId: String? = null,
    val accountAuthToken: String? = null,
    val accountRefreshToken: String? = null,
    val syncEnabled: Boolean = false,
    val lastSyncTime: Long = 0L,
    val cloudEndpointUrl: String = "https://aspects-oops-lance-limit.trycloudflare.com",
    val cloudApiKey: String = "",
    val permissionsReviewed: Boolean = false,
    val coinsBalance: Int = 100,
    val coinsEarnedTotal: Int = 100,
    val wellbeingEnabled: Boolean = false,
    val wellbeingStrictness: String = "GENTLE",
    val protectionActive: Boolean = false,
    val protectionFailSafeTripped: Boolean = false,
    val screenTimeGoalMinutes: Int = 120,
    val deviceAdminEnabled: Boolean = false
)

class SettingsDataStore(val context: Context) {

    companion object {
        const val DEFAULT_HOME_SECTION_ORDER = "DAILY_INTENTION,FOCUS,KEY_NUMBERS,QUICK_ACTIONS"
        const val DEFAULT_HIDDEN_HOME_SECTIONS = ""
        const val DEFAULT_QUICK_ACTIONS_ORDER = "ADD_HABIT,ADD_TASK,START_FOCUS,JOURNAL,NEW_GOAL"
    }

    private object Keys {
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APP_THEME = stringPreferencesKey("app_theme")
        val ACCENT_OPTION = stringPreferencesKey("accent_option")
        val CONTENT_DENSITY = stringPreferencesKey("content_density")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DEFAULT_REMINDER_TIME = stringPreferencesKey("default_reminder_time")
        val NOTIFY_ONLY_IF_NOT_DONE = booleanPreferencesKey("notify_only_if_not_done")
        val GOLD_REPAIR_REMINDER_ENABLED = booleanPreferencesKey("gold_repair_reminder_enabled")
        val GOLD_REPAIR_ENABLED = booleanPreferencesKey("gold_repair_enabled")
        val WEEK_START_MONDAY = booleanPreferencesKey("week_start_monday")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FOCUS_ENABLED = booleanPreferencesKey("focus_enabled")
        val FOCUS_PRESET_MINUTES = intPreferencesKey("focus_preset_minutes")
        val FOCUS_BREAK_MINUTES = intPreferencesKey("focus_break_minutes")
        val FOCUS_SOUND_ENABLED = booleanPreferencesKey("focus_sound_enabled")
        val FOCUS_VIBRATION_ENABLED = booleanPreferencesKey("focus_vibration_enabled")
        val DAILY_INTENTION_ENABLED = booleanPreferencesKey("daily_intention_enabled")
        val DAILY_INTENTION_REMINDER_TIME = stringPreferencesKey("daily_intention_reminder_time")
        val JOURNAL_ENABLED = booleanPreferencesKey("journal_enabled")
        val JOURNAL_REMINDER_TIME = stringPreferencesKey("journal_reminder_time")
        val WEEKLY_REVIEW_ENABLED = booleanPreferencesKey("weekly_review_enabled")
        val WEEKLY_REVIEW_REMINDER_TIME = stringPreferencesKey("weekly_review_reminder_time")
        val HOME_SECTION_ORDER = stringPreferencesKey("home_section_order")
        val HIDDEN_HOME_SECTIONS = stringPreferencesKey("hidden_home_sections")
        val QUICK_ACTIONS_ORDER = stringPreferencesKey("quick_actions_order")
        val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
        val ACCOUNT_DISPLAY_NAME = stringPreferencesKey("account_display_name")
        val ACCOUNT_USER_ID = stringPreferencesKey("account_user_id")
        val ACCOUNT_AUTH_TOKEN = stringPreferencesKey("account_auth_token")
        val ACCOUNT_REFRESH_TOKEN = stringPreferencesKey("account_refresh_token")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val LAST_SYNC_TIME = androidx.datastore.preferences.core.longPreferencesKey("last_sync_time")
        val CLOUD_ENDPOINT_URL = stringPreferencesKey("cloud_endpoint_url")
        val CLOUD_API_KEY = stringPreferencesKey("cloud_api_key")
        val PERMISSIONS_REVIEWED = booleanPreferencesKey("permissions_reviewed")
        val COINS_BALANCE = intPreferencesKey("coins_balance")
        val COINS_EARNED_TOTAL = intPreferencesKey("coins_earned_total")
        val WELLBEING_ENABLED = booleanPreferencesKey("wellbeing_enabled")
        val WELLBEING_STRICTNESS = stringPreferencesKey("wellbeing_strictness")
        val PROTECTION_ACTIVE = booleanPreferencesKey("protection_active")
        val PROTECTION_FAILSAFE_TRIPPED = booleanPreferencesKey("protection_failsafe_tripped")
        val SCREEN_TIME_GOAL_MINUTES = intPreferencesKey("screen_time_goal_minutes")
        val DEVICE_ADMIN_ENABLED = booleanPreferencesKey("device_admin_enabled")
    }

    val settingsFlow: Flow<KintsuSettings> = context.dataStore.data.map { prefs ->
        val name = prefs[Keys.DISPLAY_NAME] ?: "Seeker"
        val themeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try { ThemeMode.valueOf(themeStr) } catch (e: Exception) { ThemeMode.SYSTEM }

        val appThemeStr = prefs[Keys.APP_THEME] ?: AppTheme.SAGE_IVORY.name
        val appTheme = try { AppTheme.valueOf(appThemeStr) } catch (e: Exception) { AppTheme.SAGE_IVORY }

        val accentStr = prefs[Keys.ACCENT_OPTION] ?: appTheme.defaultAccent.name
        val accentOption = try { AccentOption.valueOf(accentStr) } catch (e: Exception) { appTheme.defaultAccent }

        val densityStr = prefs[Keys.CONTENT_DENSITY] ?: ContentDensity.COMFORTABLE.name
        val contentDensity = try { ContentDensity.valueOf(densityStr) } catch (e: Exception) { ContentDensity.COMFORTABLE }
        val hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true
        val reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false

        val notifications = prefs[Keys.NOTIFICATIONS_ENABLED] ?: false
        val reminderTime = prefs[Keys.DEFAULT_REMINDER_TIME] ?: "09:00"
        val notifyOnlyIfNotDone = prefs[Keys.NOTIFY_ONLY_IF_NOT_DONE] ?: true
        val goldRepairReminder = prefs[Keys.GOLD_REPAIR_REMINDER_ENABLED] ?: true
        val goldRepairEnabled = prefs[Keys.GOLD_REPAIR_ENABLED] ?: true
        val weekStartMonday = prefs[Keys.WEEK_START_MONDAY] ?: true
        val onboarding = prefs[Keys.ONBOARDING_COMPLETED] ?: false

        val focusEnabled = prefs[Keys.FOCUS_ENABLED] ?: true
        val focusPreset = prefs[Keys.FOCUS_PRESET_MINUTES] ?: 25
        val focusBreak = prefs[Keys.FOCUS_BREAK_MINUTES] ?: 5
        val focusSound = prefs[Keys.FOCUS_SOUND_ENABLED] ?: true
        val focusVibration = prefs[Keys.FOCUS_VIBRATION_ENABLED] ?: true
        val intentionEnabled = prefs[Keys.DAILY_INTENTION_ENABLED] ?: true
        val intentionReminder = prefs[Keys.DAILY_INTENTION_REMINDER_TIME] ?: "08:00"
        val journalEnabled = prefs[Keys.JOURNAL_ENABLED] ?: true
        val journalReminder = prefs[Keys.JOURNAL_REMINDER_TIME] ?: "21:00"
        val reviewEnabled = prefs[Keys.WEEKLY_REVIEW_ENABLED] ?: true
        val reviewReminder = prefs[Keys.WEEKLY_REVIEW_REMINDER_TIME] ?: "18:00"

        val homeSectionOrder = prefs[Keys.HOME_SECTION_ORDER] ?: DEFAULT_HOME_SECTION_ORDER
        val hiddenHomeSections = prefs[Keys.HIDDEN_HOME_SECTIONS] ?: DEFAULT_HIDDEN_HOME_SECTIONS
        val quickActionsOrder = prefs[Keys.QUICK_ACTIONS_ORDER] ?: DEFAULT_QUICK_ACTIONS_ORDER

        val accountEmail = prefs[Keys.ACCOUNT_EMAIL]
        val accountDisplayName = prefs[Keys.ACCOUNT_DISPLAY_NAME]
        val accountUserId = prefs[Keys.ACCOUNT_USER_ID]
        val accountAuthToken = prefs[Keys.ACCOUNT_AUTH_TOKEN]
        val accountRefreshToken = prefs[Keys.ACCOUNT_REFRESH_TOKEN]
        val syncEnabled = prefs[Keys.SYNC_ENABLED] ?: false
        val lastSyncTime = prefs[Keys.LAST_SYNC_TIME] ?: 0L
        val cloudEndpointUrl = prefs[Keys.CLOUD_ENDPOINT_URL] ?: "https://aspects-oops-lance-limit.trycloudflare.com"
        val cloudApiKey = prefs[Keys.CLOUD_API_KEY] ?: ""
        val permissionsReviewed = prefs[Keys.PERMISSIONS_REVIEWED] ?: false

        KintsuSettings(
            displayName = name,
            themeMode = themeMode,
            appTheme = appTheme,
            accentOption = accentOption,
            contentDensity = contentDensity,
            hapticsEnabled = hapticsEnabled,
            reducedMotion = reducedMotion,
            notificationsEnabled = notifications,
            defaultReminderTime = reminderTime,
            notifyOnlyIfNotDone = notifyOnlyIfNotDone,
            goldRepairReminderEnabled = goldRepairReminder,
            goldRepairEnabled = goldRepairEnabled,
            weekStartMonday = weekStartMonday,
            onboardingCompleted = onboarding,
            focusEnabled = focusEnabled,
            focusDefaultPresetMinutes = focusPreset,
            focusBreakMinutes = focusBreak,
            focusSoundEnabled = focusSound,
            focusVibrationEnabled = focusVibration,
            dailyIntentionEnabled = intentionEnabled,
            dailyIntentionReminderTime = intentionReminder,
            journalEnabled = journalEnabled,
            journalReminderTime = journalReminder,
            weeklyReviewEnabled = reviewEnabled,
            weeklyReviewReminderTime = reviewReminder,
            homeSectionOrder = homeSectionOrder,
            hiddenHomeSections = hiddenHomeSections,
            quickActionsOrder = quickActionsOrder,
            accountEmail = accountEmail,
            accountDisplayName = accountDisplayName,
            accountUserId = accountUserId,
            accountAuthToken = accountAuthToken,
            accountRefreshToken = accountRefreshToken,
            syncEnabled = syncEnabled,
            lastSyncTime = lastSyncTime,
            cloudEndpointUrl = cloudEndpointUrl,
            cloudApiKey = cloudApiKey,
            permissionsReviewed = permissionsReviewed,
            coinsBalance = prefs[Keys.COINS_BALANCE] ?: 100,
            coinsEarnedTotal = prefs[Keys.COINS_EARNED_TOTAL] ?: 100,
            wellbeingEnabled = prefs[Keys.WELLBEING_ENABLED] ?: false,
            wellbeingStrictness = prefs[Keys.WELLBEING_STRICTNESS] ?: "GENTLE",
            protectionActive = prefs[Keys.PROTECTION_ACTIVE] ?: false,
            protectionFailSafeTripped = prefs[Keys.PROTECTION_FAILSAFE_TRIPPED] ?: false,
            screenTimeGoalMinutes = prefs[Keys.SCREEN_TIME_GOAL_MINUTES] ?: 120,
            deviceAdminEnabled = prefs[Keys.DEVICE_ADMIN_ENABLED] ?: false
        )
    }

    suspend fun addCoins(amount: Int) {
        if (amount <= 0) return
        context.dataStore.edit {
            val current = it[Keys.COINS_BALANCE] ?: 100
            val total = it[Keys.COINS_EARNED_TOTAL] ?: 100
            it[Keys.COINS_BALANCE] = current + amount
            it[Keys.COINS_EARNED_TOTAL] = total + amount
        }
    }

    suspend fun spendCoins(amount: Int): Boolean {
        if (amount <= 0) return true
        var success = false
        context.dataStore.edit {
            val current = it[Keys.COINS_BALANCE] ?: 100
            if (current >= amount) {
                it[Keys.COINS_BALANCE] = current - amount
                success = true
            }
        }
        return success
    }

    suspend fun setWellbeingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WELLBEING_ENABLED] = enabled }
    }

    suspend fun setWellbeingStrictness(strictness: String) {
        context.dataStore.edit { it[Keys.WELLBEING_STRICTNESS] = strictness }
    }

    suspend fun setProtectionActive(active: Boolean) {
        context.dataStore.edit { it[Keys.PROTECTION_ACTIVE] = active }
    }

    suspend fun setProtectionFailSafeTripped(tripped: Boolean) {
        context.dataStore.edit { it[Keys.PROTECTION_FAILSAFE_TRIPPED] = tripped }
    }

    suspend fun setScreenTimeGoalMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.SCREEN_TIME_GOAL_MINUTES] = minutes }
    }

    suspend fun setDeviceAdminEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DEVICE_ADMIN_ENABLED] = enabled }
    }

    suspend fun setDisplayName(name: String) {
        context.dataStore.edit { it[Keys.DISPLAY_NAME] = name.trim() }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit {
            it[Keys.APP_THEME] = theme.name
            it[Keys.ACCENT_OPTION] = theme.defaultAccent.name
        }
    }

    suspend fun setAccentOption(accent: AccentOption) {
        context.dataStore.edit { it[Keys.ACCENT_OPTION] = accent.name }
    }

    suspend fun setContentDensity(density: ContentDensity) {
        context.dataStore.edit { it[Keys.CONTENT_DENSITY] = density.name }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setReducedMotion(reduced: Boolean) {
        context.dataStore.edit { it[Keys.REDUCED_MOTION] = reduced }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setDefaultReminderTime(time: String) {
        context.dataStore.edit { it[Keys.DEFAULT_REMINDER_TIME] = time }
    }

    suspend fun setNotifyOnlyIfNotDone(onlyIfNotDone: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFY_ONLY_IF_NOT_DONE] = onlyIfNotDone }
    }

    suspend fun setGoldRepairReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GOLD_REPAIR_REMINDER_ENABLED] = enabled }
    }

    suspend fun setGoldRepairEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GOLD_REPAIR_ENABLED] = enabled }
    }

    suspend fun setWeekStartMonday(monday: Boolean) {
        context.dataStore.edit { it[Keys.WEEK_START_MONDAY] = monday }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setFocusEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FOCUS_ENABLED] = enabled }
    }

    suspend fun setFocusDefaultPresetMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.FOCUS_PRESET_MINUTES] = minutes }
    }

    suspend fun setFocusBreakMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.FOCUS_BREAK_MINUTES] = minutes }
    }

    suspend fun setFocusSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FOCUS_SOUND_ENABLED] = enabled }
    }

    suspend fun setFocusVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FOCUS_VIBRATION_ENABLED] = enabled }
    }

    suspend fun setDailyIntentionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_INTENTION_ENABLED] = enabled }
    }

    suspend fun setDailyIntentionReminderTime(time: String) {
        context.dataStore.edit { it[Keys.DAILY_INTENTION_REMINDER_TIME] = time }
    }

    suspend fun setJournalEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.JOURNAL_ENABLED] = enabled }
    }

    suspend fun setJournalReminderTime(time: String) {
        context.dataStore.edit { it[Keys.JOURNAL_REMINDER_TIME] = time }
    }

    suspend fun setWeeklyReviewEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WEEKLY_REVIEW_ENABLED] = enabled }
    }

    suspend fun setWeeklyReviewReminderTime(time: String) {
        context.dataStore.edit { it[Keys.WEEKLY_REVIEW_REMINDER_TIME] = time }
    }

    suspend fun setHomeSectionOrder(order: String) {
        context.dataStore.edit { it[Keys.HOME_SECTION_ORDER] = order }
    }

    suspend fun setHiddenHomeSections(hidden: String) {
        context.dataStore.edit { it[Keys.HIDDEN_HOME_SECTIONS] = hidden }
    }

    suspend fun setQuickActionsOrder(order: String) {
        context.dataStore.edit { it[Keys.QUICK_ACTIONS_ORDER] = order }
    }

    suspend fun resetHomePreferences() {
        context.dataStore.edit {
            it[Keys.HOME_SECTION_ORDER] = DEFAULT_HOME_SECTION_ORDER
            it[Keys.HIDDEN_HOME_SECTIONS] = DEFAULT_HIDDEN_HOME_SECTIONS
            it[Keys.QUICK_ACTIONS_ORDER] = DEFAULT_QUICK_ACTIONS_ORDER
        }
    }

    suspend fun setAccount(
        email: String?,
        displayName: String?,
        userId: String?,
        token: String?,
        refreshToken: String?
    ) {
        context.dataStore.edit {
            if (email != null) it[Keys.ACCOUNT_EMAIL] = email else it.remove(Keys.ACCOUNT_EMAIL)
            if (displayName != null) it[Keys.ACCOUNT_DISPLAY_NAME] = displayName else it.remove(Keys.ACCOUNT_DISPLAY_NAME)
            if (userId != null) it[Keys.ACCOUNT_USER_ID] = userId else it.remove(Keys.ACCOUNT_USER_ID)
            if (token != null) it[Keys.ACCOUNT_AUTH_TOKEN] = token else it.remove(Keys.ACCOUNT_AUTH_TOKEN)
            if (refreshToken != null) it[Keys.ACCOUNT_REFRESH_TOKEN] = refreshToken else it.remove(Keys.ACCOUNT_REFRESH_TOKEN)
        }
    }

    suspend fun signOutAccount() {
        context.dataStore.edit {
            it.remove(Keys.ACCOUNT_EMAIL)
            it.remove(Keys.ACCOUNT_DISPLAY_NAME)
            it.remove(Keys.ACCOUNT_USER_ID)
            it.remove(Keys.ACCOUNT_AUTH_TOKEN)
            it.remove(Keys.ACCOUNT_REFRESH_TOKEN)
            it[Keys.SYNC_ENABLED] = false
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SYNC_ENABLED] = enabled }
    }

    suspend fun setLastSyncTime(timestamp: Long) {
        context.dataStore.edit { it[Keys.LAST_SYNC_TIME] = timestamp }
    }

    suspend fun setCloudConfig(url: String, key: String) {
        context.dataStore.edit {
            it[Keys.CLOUD_ENDPOINT_URL] = url
            it[Keys.CLOUD_API_KEY] = key
        }
    }

    suspend fun setPermissionsReviewed(reviewed: Boolean) {
        context.dataStore.edit { it[Keys.PERMISSIONS_REVIEWED] = reviewed }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { it.clear() }
    }
}
