package com.kintsu.app.wellbeing.service

import android.app.NotificationManager
import android.content.Context
import android.os.Build

class DndManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun isDndPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    /**
     * Captures current DND state and activates Priority Only / Alarms Only.
     * Returns the previous filter int so it can be restored exactly.
     */
    fun enableCalmMode(): Int {
        if (!isDndPermissionGranted()) return -1

        val currentFilter = notificationManager.currentInterruptionFilter
        try {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        } catch (_: Exception) {
            // Safe fallback if OEM restricts filter changes
        }
        return currentFilter
    }

    /**
     * Restores the exact previous notification interruption filter.
     */
    fun restorePreviousState(previousFilter: Int) {
        if (!isDndPermissionGranted() || previousFilter == -1) return
        try {
            notificationManager.setInterruptionFilter(previousFilter)
        } catch (_: Exception) {
            // Ignore if unable to restore
        }
    }
}
