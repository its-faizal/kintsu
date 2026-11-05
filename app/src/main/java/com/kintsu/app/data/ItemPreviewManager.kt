package com.kintsu.app.data

import android.content.Context
import android.content.SharedPreferences
import com.kintsu.app.ui.theme.AppIconManager
import com.kintsu.app.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object ItemPreviewManager {

    private const val PREFS_NAME = "kintsu_item_previews"
    private const val KEY_USED_PREVIEWS = "used_preview_item_ids"
    private const val KEY_ACTIVE_ID = "active_preview_id"
    private const val KEY_ACTIVE_TYPE = "active_preview_type"
    private const val KEY_EXPIRES_AT = "active_preview_expires_at"
    private const val KEY_REVERT_THEME = "revert_theme"
    private const val KEY_REVERT_ICON = "revert_icon"

    const val PREVIEW_DURATION_MS = 10 * 60 * 1000L // 10 minutes

    private val _activePreviewId = MutableStateFlow<String?>(null)
    val activePreviewId: StateFlow<String?> = _activePreviewId.asStateFlow()

    private val _activePreviewType = MutableStateFlow<String?>(null)
    val activePreviewType: StateFlow<String?> = _activePreviewType.asStateFlow()

    private val _expiresAtMillis = MutableStateFlow(0L)
    val expiresAtMillis: StateFlow<Long> = _expiresAtMillis.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private var countdownJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val p = prefs ?: return

        val activeId = p.getString(KEY_ACTIVE_ID, null)
        val expiresAt = p.getLong(KEY_EXPIRES_AT, 0L)
        val now = System.currentTimeMillis()

        if (activeId != null && expiresAt > now) {
            _activePreviewId.value = activeId
            _activePreviewType.value = p.getString(KEY_ACTIVE_TYPE, null)
            _expiresAtMillis.value = expiresAt
            _remainingSeconds.value = (expiresAt - now) / 1000L
            startCountdown(context)
        } else if (activeId != null) {
            // Already expired during app shutdown
            clearActivePreview(context)
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return prefs ?: context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).also { prefs = it }
    }

    fun hasUsedPreview(context: Context, itemId: String): Boolean {
        val used = getPrefs(context).getStringSet(KEY_USED_PREVIEWS, emptySet()) ?: emptySet()
        return used.contains(itemId)
    }

    fun canPreview(context: Context, itemId: String): Boolean {
        if (_activePreviewId.value == itemId) return true
        return !hasUsedPreview(context, itemId)
    }

    fun isItemInPreview(itemId: String): Boolean {
        return _activePreviewId.value == itemId && _expiresAtMillis.value > System.currentTimeMillis()
    }

    fun startThemePreview(
        context: Context,
        themeId: String,
        themeObj: AppTheme,
        currentThemeName: String,
        onApply: (AppTheme) -> Unit
    ): Boolean {
        val p = getPrefs(context)
        if (hasUsedPreview(context, themeId) && _activePreviewId.value != themeId) {
            return false
        }

        val expiresAt = System.currentTimeMillis() + PREVIEW_DURATION_MS
        val used = (p.getStringSet(KEY_USED_PREVIEWS, emptySet()) ?: emptySet()).toMutableSet()
        used.add(themeId)

        p.edit()
            .putStringSet(KEY_USED_PREVIEWS, used)
            .putString(KEY_ACTIVE_ID, themeId)
            .putString(KEY_ACTIVE_TYPE, "THEME")
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putString(KEY_REVERT_THEME, currentThemeName)
            .apply()

        _activePreviewId.value = themeId
        _activePreviewType.value = "THEME"
        _expiresAtMillis.value = expiresAt
        _remainingSeconds.value = PREVIEW_DURATION_MS / 1000L

        onApply(themeObj)
        startCountdown(context)
        return true
    }

    fun startIconPreview(
        context: Context,
        iconId: String,
        currentIconId: String,
        onApply: (String) -> Unit
    ): Boolean {
        val p = getPrefs(context)
        if (hasUsedPreview(context, iconId) && _activePreviewId.value != iconId) {
            return false
        }

        val expiresAt = System.currentTimeMillis() + PREVIEW_DURATION_MS
        val used = (p.getStringSet(KEY_USED_PREVIEWS, emptySet()) ?: emptySet()).toMutableSet()
        used.add(iconId)

        p.edit()
            .putStringSet(KEY_USED_PREVIEWS, used)
            .putString(KEY_ACTIVE_ID, iconId)
            .putString(KEY_ACTIVE_TYPE, "ICON")
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putString(KEY_REVERT_ICON, currentIconId)
            .apply()

        _activePreviewId.value = iconId
        _activePreviewType.value = "ICON"
        _expiresAtMillis.value = expiresAt
        _remainingSeconds.value = PREVIEW_DURATION_MS / 1000L

        onApply(iconId)
        startCountdown(context)
        return true
    }

    fun startPackPreview(
        context: Context,
        packId: String,
        themeObj: AppTheme,
        iconId: String,
        currentThemeName: String,
        currentIconId: String,
        onApply: (AppTheme, String) -> Unit
    ): Boolean {
        val p = getPrefs(context)
        if (hasUsedPreview(context, packId) && _activePreviewId.value != packId) {
            return false
        }

        val expiresAt = System.currentTimeMillis() + PREVIEW_DURATION_MS
        val used = (p.getStringSet(KEY_USED_PREVIEWS, emptySet()) ?: emptySet()).toMutableSet()
        used.add(packId)

        p.edit()
            .putStringSet(KEY_USED_PREVIEWS, used)
            .putString(KEY_ACTIVE_ID, packId)
            .putString(KEY_ACTIVE_TYPE, "PACK")
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putString(KEY_REVERT_THEME, currentThemeName)
            .putString(KEY_REVERT_ICON, currentIconId)
            .apply()

        _activePreviewId.value = packId
        _activePreviewType.value = "PACK"
        _expiresAtMillis.value = expiresAt
        _remainingSeconds.value = PREVIEW_DURATION_MS / 1000L

        onApply(themeObj, iconId)
        startCountdown(context)
        return true
    }

    private fun startCountdown(context: Context) {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val remaining = (_expiresAtMillis.value - now) / 1000L
                if (remaining <= 0) {
                    _remainingSeconds.value = 0L
                    expireActivePreview(context)
                    break
                }
                _remainingSeconds.value = remaining
                delay(1000L)
            }
        }
    }

    fun expireActivePreview(
        context: Context,
        onRevertTheme: ((AppTheme) -> Unit)? = null,
        onRevertIcon: ((String) -> Unit)? = null
    ) {
        val p = getPrefs(context)
        val revertThemeName = p.getString(KEY_REVERT_THEME, null)
        val revertIconId = p.getString(KEY_REVERT_ICON, null)

        if (revertThemeName != null) {
            val restoredTheme = try {
                AppTheme.valueOf(revertThemeName)
            } catch (_: Exception) {
                AppTheme.SAGE_IVORY
            }
            onRevertTheme?.invoke(restoredTheme)
        }

        if (revertIconId != null) {
            onRevertIcon?.invoke(revertIconId)
        }

        clearActivePreview(context)
    }

    fun cancelPreview(
        context: Context,
        onRevertTheme: (AppTheme) -> Unit,
        onRevertIcon: (String) -> Unit
    ) {
        expireActivePreview(context, onRevertTheme, onRevertIcon)
    }

    private fun clearActivePreview(context: Context) {
        val p = getPrefs(context)
        p.edit()
            .remove(KEY_ACTIVE_ID)
            .remove(KEY_ACTIVE_TYPE)
            .remove(KEY_EXPIRES_AT)
            .remove(KEY_REVERT_THEME)
            .remove(KEY_REVERT_ICON)
            .apply()

        _activePreviewId.value = null
        _activePreviewType.value = null
        _expiresAtMillis.value = 0L
        _remainingSeconds.value = 0L
    }
}
