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
    val homeSectionOrder: String = "KEY_NUMBERS,HABITS,FOCUS,DAILY_INTENTION,JOURNAL,ADDICTION",
    val hiddenHomeSections: String = "",
    val quickActionsOrder: String = "JOURNAL,WEEKLY_REVIEW,DETOX",
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
    val deviceAdminEnabled: Boolean = false,
    val breathingDurationSeconds: Int = 10,
    val interventionStyle: String = "BREATHING",
    val reinterventionIntervalMinutes: Int = 10,
    val wellbeingHapticsEnabled: Boolean = true,
    val dnsProtectionEnabled: Boolean = false,
    val dailyScreenTimeBudgetMinutes: Int = 120,
    val launcherIconId: String = "icon_original",
    val widgetSkinId: String = "widget_botanical_sage",
    val focusTimerFaceId: String = "focus_face_classic_ring",
    val focusSceneId: String = "focus_scene_none",
    val focusCompletionEffectId: String = "focus_effect_soft_glow",
    val profileAvatarId: String = "avatar_botanical",
    val profileFrameId: String = "frame_classic_thin",
    val profileBackgroundId: String = "bg_none",
    val profileTitle: String = "Seeker",
    val goldRepairStyleId: String = "repair_classic_gold",
    val habitMarkerId: String = "habit_marker_classic_check",
    val habitRingId: String = "habit_ring_minimal",
    val soundPresetHabit: String = "sound_soft_chime",
    val soundPresetFocus: String = "sound_warm_resonance",
    val hapticPreset: String = "SUBTLE",
    val appLockEnabled: Boolean = false,
    val appLockType: String = "PIN",
    val appLockCode: String = "",
    val appLockBiometricEnabled: Boolean = false,
    val lastAutoBackupTime: Long = 0L,
    val shortFormBlockingEnabled: Boolean = true,
    val blockYouTubeShorts: Boolean = true,
    val blockInstagramReels: Boolean = true,
    val blockTikTok: Boolean = true,
    val blockFacebookReels: Boolean = true,
    val blockSnapchatSpotlight: Boolean = true,
    val blockWebShorts: Boolean = true,
    val maxContinuousSessionMinutes: Int = 20,
    val sessionCooldownMinutes: Int = 5,
    val appLaunchLimitEnabled: Boolean = true,
    val defaultAppMaxLaunches: Int = 10,
    val bedtimeLockEnabled: Boolean = false,
    val bedtimeStartHour: Int = 23,
    val bedtimeEndHour: Int = 6,
    val mindfulPauseOnLaunch: Boolean = true,
    val blockNsfw: Boolean = true,
    val bedtimeDaysMask: Int = 0b1111111,
    val bedtimeDndEnabled: Boolean = true,
    val notificationBatchingEnabled: Boolean = false,
    val storeAllNotifications: Boolean = true,
    val notificationHistoryDays: Int = 15,
    val notificationRecapType: String = "SUMMARY_ONLY",
    val notificationScheduleMorning: Boolean = true,
    val notificationScheduleAfternoon: Boolean = true,
    val notificationScheduleEvening: Boolean = true,
    val invincibleModeEnabled: Boolean = false,
    val invincibleWindowHour: Int = 0,
    val invincibleWindowMinute: Int = 0,
    val invincibleIncludeShorts: Boolean = true,
    val invincibleIncludeBedtime: Boolean = true,
    val protectedAccessEnabled: Boolean = false,
    val tamperProtectionEnabled: Boolean = false,
    val uninstallWindowHour: Int = 0,
    val uninstallWindowMinute: Int = 0
)

class SettingsDataStore(val context: Context) {

    companion object {
        const val DEFAULT_HOME_SECTION_ORDER = "KEY_NUMBERS,HABITS,FOCUS,DAILY_INTENTION,JOURNAL,ADDICTION"
        const val DEFAULT_HIDDEN_HOME_SECTIONS = ""
        const val DEFAULT_QUICK_ACTIONS_ORDER = "JOURNAL,WEEKLY_REVIEW,DETOX"
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
        val LAUNCHER_ICON_ID = stringPreferencesKey("launcher_icon_id")
        val WIDGET_SKIN_ID = stringPreferencesKey("widget_skin_id")
        val FOCUS_TIMER_FACE_ID = stringPreferencesKey("focus_timer_face_id")
        val FOCUS_SCENE_ID = stringPreferencesKey("focus_scene_id")
        val FOCUS_COMPLETION_EFFECT_ID = stringPreferencesKey("focus_completion_effect_id")
        val PROFILE_AVATAR_ID = stringPreferencesKey("profile_avatar_id")
        val PROFILE_FRAME_ID = stringPreferencesKey("profile_frame_id")
        val PROFILE_BACKGROUND_ID = stringPreferencesKey("profile_background_id")
        val PROFILE_TITLE = stringPreferencesKey("profile_title")
        val GOLD_REPAIR_STYLE_ID = stringPreferencesKey("gold_repair_style_id")
        val HABIT_MARKER_ID = stringPreferencesKey("habit_marker_id")
        val HABIT_RING_ID = stringPreferencesKey("habit_ring_id")
        val SOUND_PRESET_HABIT = stringPreferencesKey("sound_preset_habit")
        val SOUND_PRESET_FOCUS = stringPreferencesKey("sound_preset_focus")
        val HAPTIC_PRESET = stringPreferencesKey("haptic_preset")
        val PROTECTION_FAILSAFE_TRIPPED = booleanPreferencesKey("protection_failsafe_tripped")
        val SCREEN_TIME_GOAL_MINUTES = intPreferencesKey("screen_time_goal_minutes")
        val DEVICE_ADMIN_ENABLED = booleanPreferencesKey("device_admin_enabled")
        val BREATHING_DURATION_SECONDS = intPreferencesKey("breathing_duration_seconds")
        val INTERVENTION_STYLE = stringPreferencesKey("intervention_style")
        val REINTERVENTION_INTERVAL_MINUTES = intPreferencesKey("reintervention_interval_minutes")
        val WELLBEING_HAPTICS_ENABLED = booleanPreferencesKey("wellbeing_haptics_enabled")
        val DNS_PROTECTION_ENABLED = booleanPreferencesKey("dns_protection_enabled")
        val DAILY_SCREEN_TIME_BUDGET_MINUTES = intPreferencesKey("daily_screen_time_budget_minutes")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_TYPE = stringPreferencesKey("app_lock_type")
        val APP_LOCK_CODE = stringPreferencesKey("app_lock_code")
        val APP_LOCK_BIOMETRIC_ENABLED = booleanPreferencesKey("app_lock_biometric_enabled")
        val LAST_AUTO_BACKUP_TIME = androidx.datastore.preferences.core.longPreferencesKey("last_auto_backup_time")
        val SHORT_FORM_BLOCKING_ENABLED = booleanPreferencesKey("short_form_blocking_enabled")
        val BLOCK_YOUTUBE_SHORTS = booleanPreferencesKey("block_youtube_shorts")
        val BLOCK_INSTAGRAM_REELS = booleanPreferencesKey("block_instagram_reels")
        val BLOCK_TIKTOK = booleanPreferencesKey("block_tiktok")
        val BLOCK_FACEBOOK_REELS = booleanPreferencesKey("block_facebook_reels")
        val BLOCK_SNAPCHAT_SPOTLIGHT = booleanPreferencesKey("block_snapchat_spotlight")
        val BLOCK_WEB_SHORTS = booleanPreferencesKey("block_web_shorts")
        val MAX_CONTINUOUS_SESSION_MINUTES = intPreferencesKey("max_continuous_session_minutes")
        val SESSION_COOLDOWN_MINUTES = intPreferencesKey("session_cooldown_minutes")
        val APP_LAUNCH_LIMIT_ENABLED = booleanPreferencesKey("app_launch_limit_enabled")
        val DEFAULT_APP_MAX_LAUNCHES = intPreferencesKey("default_app_max_launches")
        val BEDTIME_LOCK_ENABLED = booleanPreferencesKey("bedtime_lock_enabled")
        val BEDTIME_START_HOUR = intPreferencesKey("bedtime_start_hour")
        val BEDTIME_END_HOUR = intPreferencesKey("bedtime_end_hour")
        val MINDFUL_PAUSE_ON_LAUNCH = booleanPreferencesKey("mindful_pause_on_launch")
        val BLOCK_NSFW = booleanPreferencesKey("block_nsfw")
        val BEDTIME_DAYS_MASK = intPreferencesKey("bedtime_days_mask")
        val BEDTIME_DND_ENABLED = booleanPreferencesKey("bedtime_dnd_enabled")
        val NOTIFICATION_BATCHING_ENABLED = booleanPreferencesKey("notification_batching_enabled")
        val STORE_ALL_NOTIFICATIONS = booleanPreferencesKey("store_all_notifications")
        val NOTIFICATION_HISTORY_DAYS = intPreferencesKey("notification_history_days")
        val NOTIFICATION_RECAP_TYPE = stringPreferencesKey("notification_recap_type")
        val NOTIFICATION_SCHEDULE_MORNING = booleanPreferencesKey("notification_schedule_morning")
        val NOTIFICATION_SCHEDULE_AFTERNOON = booleanPreferencesKey("notification_schedule_afternoon")
        val NOTIFICATION_SCHEDULE_EVENING = booleanPreferencesKey("notification_schedule_evening")
        val INVINCIBLE_MODE_ENABLED = booleanPreferencesKey("invincible_mode_enabled")
        val INVINCIBLE_WINDOW_HOUR = intPreferencesKey("invincible_window_hour")
        val INVINCIBLE_WINDOW_MINUTE = intPreferencesKey("invincible_window_minute")
        val INVINCIBLE_INCLUDE_SHORTS = booleanPreferencesKey("invincible_include_shorts")
        val INVINCIBLE_INCLUDE_BEDTIME = booleanPreferencesKey("invincible_include_bedtime")
        val PROTECTED_ACCESS_ENABLED = booleanPreferencesKey("protected_access_enabled")
        val TAMPER_PROTECTION_ENABLED = booleanPreferencesKey("tamper_protection_enabled")
        val UNINSTALL_WINDOW_HOUR = intPreferencesKey("uninstall_window_hour")
        val UNINSTALL_WINDOW_MINUTE = intPreferencesKey("uninstall_window_minute")
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
            deviceAdminEnabled = prefs[Keys.DEVICE_ADMIN_ENABLED] ?: false,
            breathingDurationSeconds = prefs[Keys.BREATHING_DURATION_SECONDS] ?: 10,
            interventionStyle = prefs[Keys.INTERVENTION_STYLE] ?: "BREATHING",
            reinterventionIntervalMinutes = prefs[Keys.REINTERVENTION_INTERVAL_MINUTES] ?: 10,
            wellbeingHapticsEnabled = prefs[Keys.WELLBEING_HAPTICS_ENABLED] ?: true,
            dnsProtectionEnabled = prefs[Keys.DNS_PROTECTION_ENABLED] ?: false,
            dailyScreenTimeBudgetMinutes = prefs[Keys.DAILY_SCREEN_TIME_BUDGET_MINUTES] ?: 120,
            launcherIconId = prefs[Keys.LAUNCHER_ICON_ID] ?: "icon_original",
            widgetSkinId = prefs[Keys.WIDGET_SKIN_ID] ?: "widget_botanical_sage",
            focusTimerFaceId = prefs[Keys.FOCUS_TIMER_FACE_ID] ?: "focus_face_classic_ring",
            focusSceneId = prefs[Keys.FOCUS_SCENE_ID] ?: "focus_scene_none",
            focusCompletionEffectId = prefs[Keys.FOCUS_COMPLETION_EFFECT_ID] ?: "focus_effect_soft_glow",
            profileAvatarId = prefs[Keys.PROFILE_AVATAR_ID] ?: "avatar_botanical",
            profileFrameId = prefs[Keys.PROFILE_FRAME_ID] ?: "frame_classic_thin",
            profileBackgroundId = prefs[Keys.PROFILE_BACKGROUND_ID] ?: "bg_none",
            profileTitle = prefs[Keys.PROFILE_TITLE] ?: "Seeker",
            goldRepairStyleId = prefs[Keys.GOLD_REPAIR_STYLE_ID] ?: "repair_classic_gold",
            habitMarkerId = prefs[Keys.HABIT_MARKER_ID] ?: "habit_marker_classic_check",
            habitRingId = prefs[Keys.HABIT_RING_ID] ?: "habit_ring_minimal",
            soundPresetHabit = prefs[Keys.SOUND_PRESET_HABIT] ?: "sound_soft_chime",
            soundPresetFocus = prefs[Keys.SOUND_PRESET_FOCUS] ?: "sound_warm_resonance",
            hapticPreset = prefs[Keys.HAPTIC_PRESET] ?: "SUBTLE",
            appLockEnabled = prefs[Keys.APP_LOCK_ENABLED] ?: false,
            appLockType = prefs[Keys.APP_LOCK_TYPE] ?: "PIN",
            appLockCode = prefs[Keys.APP_LOCK_CODE] ?: "",
            appLockBiometricEnabled = prefs[Keys.APP_LOCK_BIOMETRIC_ENABLED] ?: false,
            lastAutoBackupTime = prefs[Keys.LAST_AUTO_BACKUP_TIME] ?: 0L,
                shortFormBlockingEnabled = prefs[Keys.SHORT_FORM_BLOCKING_ENABLED] ?: true,
                blockYouTubeShorts = prefs[Keys.BLOCK_YOUTUBE_SHORTS] ?: true,
                blockInstagramReels = prefs[Keys.BLOCK_INSTAGRAM_REELS] ?: true,
                blockTikTok = prefs[Keys.BLOCK_TIKTOK] ?: true,
                blockFacebookReels = prefs[Keys.BLOCK_FACEBOOK_REELS] ?: true,
                blockSnapchatSpotlight = prefs[Keys.BLOCK_SNAPCHAT_SPOTLIGHT] ?: true,
                blockWebShorts = prefs[Keys.BLOCK_WEB_SHORTS] ?: true,
                maxContinuousSessionMinutes = prefs[Keys.MAX_CONTINUOUS_SESSION_MINUTES] ?: 20,
                sessionCooldownMinutes = prefs[Keys.SESSION_COOLDOWN_MINUTES] ?: 5,
                appLaunchLimitEnabled = prefs[Keys.APP_LAUNCH_LIMIT_ENABLED] ?: true,
                defaultAppMaxLaunches = prefs[Keys.DEFAULT_APP_MAX_LAUNCHES] ?: 10,
                bedtimeLockEnabled = prefs[Keys.BEDTIME_LOCK_ENABLED] ?: false,
                bedtimeStartHour = prefs[Keys.BEDTIME_START_HOUR] ?: 23,
                bedtimeEndHour = prefs[Keys.BEDTIME_END_HOUR] ?: 6,
                mindfulPauseOnLaunch = prefs[Keys.MINDFUL_PAUSE_ON_LAUNCH] ?: true,
                blockNsfw = prefs[Keys.BLOCK_NSFW] ?: true,
                bedtimeDaysMask = prefs[Keys.BEDTIME_DAYS_MASK] ?: 0b1111111,
                bedtimeDndEnabled = prefs[Keys.BEDTIME_DND_ENABLED] ?: true,
                notificationBatchingEnabled = prefs[Keys.NOTIFICATION_BATCHING_ENABLED] ?: false,
                storeAllNotifications = prefs[Keys.STORE_ALL_NOTIFICATIONS] ?: true,
                notificationHistoryDays = prefs[Keys.NOTIFICATION_HISTORY_DAYS] ?: 15,
                notificationRecapType = prefs[Keys.NOTIFICATION_RECAP_TYPE] ?: "SUMMARY_ONLY",
                notificationScheduleMorning = prefs[Keys.NOTIFICATION_SCHEDULE_MORNING] ?: true,
                notificationScheduleAfternoon = prefs[Keys.NOTIFICATION_SCHEDULE_AFTERNOON] ?: true,
                notificationScheduleEvening = prefs[Keys.NOTIFICATION_SCHEDULE_EVENING] ?: true,
                invincibleModeEnabled = prefs[Keys.INVINCIBLE_MODE_ENABLED] ?: false,
                invincibleWindowHour = prefs[Keys.INVINCIBLE_WINDOW_HOUR] ?: 0,
                invincibleWindowMinute = prefs[Keys.INVINCIBLE_WINDOW_MINUTE] ?: 0,
                invincibleIncludeShorts = prefs[Keys.INVINCIBLE_INCLUDE_SHORTS] ?: true,
                invincibleIncludeBedtime = prefs[Keys.INVINCIBLE_INCLUDE_BEDTIME] ?: true,
                protectedAccessEnabled = prefs[Keys.PROTECTED_ACCESS_ENABLED] ?: false,
                tamperProtectionEnabled = prefs[Keys.TAMPER_PROTECTION_ENABLED] ?: false,
                uninstallWindowHour = prefs[Keys.UNINSTALL_WINDOW_HOUR] ?: 0,
                uninstallWindowMinute = prefs[Keys.UNINSTALL_WINDOW_MINUTE] ?: 0
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

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setAppLockType(type: String) {
        context.dataStore.edit { it[Keys.APP_LOCK_TYPE] = type }
    }

    suspend fun setAppLockCode(code: String) {
        context.dataStore.edit { it[Keys.APP_LOCK_CODE] = code }
    }

    suspend fun setAppLockBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.APP_LOCK_BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setLastAutoBackupTime(timeMillis: Long) {
        context.dataStore.edit { it[Keys.LAST_AUTO_BACKUP_TIME] = timeMillis }
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

    suspend fun setBreathingDurationSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.BREATHING_DURATION_SECONDS] = seconds }
    }

    suspend fun setInterventionStyle(style: String) {
        context.dataStore.edit { it[Keys.INTERVENTION_STYLE] = style }
    }

    suspend fun setReinterventionIntervalMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.REINTERVENTION_INTERVAL_MINUTES] = minutes }
    }

    suspend fun setWellbeingHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WELLBEING_HAPTICS_ENABLED] = enabled }
    }

    suspend fun setDnsProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DNS_PROTECTION_ENABLED] = enabled }
    }

    suspend fun setDailyScreenTimeBudgetMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.DAILY_SCREEN_TIME_BUDGET_MINUTES] = minutes }
    }

    suspend fun setLauncherIconId(id: String) {
        context.dataStore.edit { it[Keys.LAUNCHER_ICON_ID] = id }
    }

    suspend fun setWidgetSkinId(id: String) {
        context.dataStore.edit { it[Keys.WIDGET_SKIN_ID] = id }
    }

    suspend fun setFocusTimerFaceId(id: String) {
        context.dataStore.edit { it[Keys.FOCUS_TIMER_FACE_ID] = id }
    }

    suspend fun setFocusSceneId(id: String) {
        context.dataStore.edit { it[Keys.FOCUS_SCENE_ID] = id }
    }

    suspend fun setFocusCompletionEffectId(id: String) {
        context.dataStore.edit { it[Keys.FOCUS_COMPLETION_EFFECT_ID] = id }
    }

    suspend fun setProfileAvatarId(id: String) {
        context.dataStore.edit { it[Keys.PROFILE_AVATAR_ID] = id }
    }

    suspend fun setProfileFrameId(id: String) {
        context.dataStore.edit { it[Keys.PROFILE_FRAME_ID] = id }
    }

    suspend fun setProfileBackgroundId(id: String) {
        context.dataStore.edit { it[Keys.PROFILE_BACKGROUND_ID] = id }
    }

    suspend fun setProfileTitle(title: String) {
        context.dataStore.edit { it[Keys.PROFILE_TITLE] = title }
    }

    suspend fun setGoldRepairStyleId(id: String) {
        context.dataStore.edit { it[Keys.GOLD_REPAIR_STYLE_ID] = id }
    }

    suspend fun setHabitMarkerId(id: String) {
        context.dataStore.edit { it[Keys.HABIT_MARKER_ID] = id }
    }

    suspend fun setHabitRingId(id: String) {
        context.dataStore.edit { it[Keys.HABIT_RING_ID] = id }
    }

    suspend fun setSoundPresetHabit(id: String) {
        context.dataStore.edit { it[Keys.SOUND_PRESET_HABIT] = id }
    }

    suspend fun setSoundPresetFocus(id: String) {
        context.dataStore.edit { it[Keys.SOUND_PRESET_FOCUS] = id }
    }

    suspend fun setHapticPreset(preset: String) {
        context.dataStore.edit { it[Keys.HAPTIC_PRESET] = preset }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun setShortFormBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHORT_FORM_BLOCKING_ENABLED] = enabled }
    }

    suspend fun setBlockShortFormPlatforms(
        yt: Boolean, ig: Boolean, tt: Boolean, fb: Boolean, snap: Boolean, web: Boolean
    ) {
        context.dataStore.edit {
            it[Keys.BLOCK_YOUTUBE_SHORTS] = yt
            it[Keys.BLOCK_INSTAGRAM_REELS] = ig
            it[Keys.BLOCK_TIKTOK] = tt
            it[Keys.BLOCK_FACEBOOK_REELS] = fb
            it[Keys.BLOCK_SNAPCHAT_SPOTLIGHT] = snap
            it[Keys.BLOCK_WEB_SHORTS] = web
        }
    }

    suspend fun setMaxContinuousSessionMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.MAX_CONTINUOUS_SESSION_MINUTES] = minutes }
    }

    suspend fun setSessionCooldownMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.SESSION_COOLDOWN_MINUTES] = minutes }
    }

    suspend fun setAppLaunchLimitEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.APP_LAUNCH_LIMIT_ENABLED] = enabled }
    }

    suspend fun setDefaultAppMaxLaunches(maxLaunches: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_APP_MAX_LAUNCHES] = maxLaunches }
    }

    suspend fun setBedtimeLock(enabled: Boolean, startHour: Int, endHour: Int) {
        context.dataStore.edit {
            it[Keys.BEDTIME_LOCK_ENABLED] = enabled
            it[Keys.BEDTIME_START_HOUR] = startHour
            it[Keys.BEDTIME_END_HOUR] = endHour
        }
    }

    suspend fun setMindfulPauseOnLaunch(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MINDFUL_PAUSE_ON_LAUNCH] = enabled }
    }

    suspend fun setBlockNsfw(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BLOCK_NSFW] = enabled }
    }

    suspend fun setBedtimeSchedule(enabled: Boolean, startHour: Int, endHour: Int, daysMask: Int, dnd: Boolean) {
        context.dataStore.edit {
            it[Keys.BEDTIME_LOCK_ENABLED] = enabled
            it[Keys.BEDTIME_START_HOUR] = startHour
            it[Keys.BEDTIME_END_HOUR] = endHour
            it[Keys.BEDTIME_DAYS_MASK] = daysMask
            it[Keys.BEDTIME_DND_ENABLED] = dnd
        }
    }

    suspend fun setNotificationBatching(
        enabled: Boolean,
        storeAll: Boolean,
        historyDays: Int,
        recapType: String,
        morning: Boolean,
        afternoon: Boolean,
        evening: Boolean
    ) {
        context.dataStore.edit {
            it[Keys.NOTIFICATION_BATCHING_ENABLED] = enabled
            it[Keys.STORE_ALL_NOTIFICATIONS] = storeAll
            it[Keys.NOTIFICATION_HISTORY_DAYS] = historyDays
            it[Keys.NOTIFICATION_RECAP_TYPE] = recapType
            it[Keys.NOTIFICATION_SCHEDULE_MORNING] = morning
            it[Keys.NOTIFICATION_SCHEDULE_AFTERNOON] = afternoon
            it[Keys.NOTIFICATION_SCHEDULE_EVENING] = evening
        }
    }

    suspend fun setInvincibleMode(
        enabled: Boolean,
        windowHour: Int,
        windowMinute: Int,
        includeShorts: Boolean,
        includeBedtime: Boolean
    ) {
        context.dataStore.edit {
            it[Keys.INVINCIBLE_MODE_ENABLED] = enabled
            it[Keys.INVINCIBLE_WINDOW_HOUR] = windowHour
            it[Keys.INVINCIBLE_WINDOW_MINUTE] = windowMinute
            it[Keys.INVINCIBLE_INCLUDE_SHORTS] = includeShorts
            it[Keys.INVINCIBLE_INCLUDE_BEDTIME] = includeBedtime
        }
    }

    suspend fun setParentalControlsProtection(
        protectedAccess: Boolean,
        tamperProtection: Boolean,
        uninstallHour: Int,
        uninstallMinute: Int
    ) {
        context.dataStore.edit {
            it[Keys.PROTECTED_ACCESS_ENABLED] = protectedAccess
            it[Keys.TAMPER_PROTECTION_ENABLED] = tamperProtection
            it[Keys.UNINSTALL_WINDOW_HOUR] = uninstallHour
            it[Keys.UNINSTALL_WINDOW_MINUTE] = uninstallMinute
        }
    }
}
