package com.kintsu.app.wellbeing.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KintsuNotificationListenerService : NotificationListenerService() {

    companion object {
        private val _totalNotificationsToday = MutableStateFlow(0)
        val totalNotificationsToday: StateFlow<Int> = _totalNotificationsToday.asStateFlow()

        private val _batchedAppsCount = MutableStateFlow(0)
        val batchedAppsCount: StateFlow<Int> = _batchedAppsCount.asStateFlow()

        var isServiceConnected: Boolean = false
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceConnected = true
        try {
            val active = activeNotifications
            _totalNotificationsToday.value = active.size
            _batchedAppsCount.value = active.map { it.packageName }.toSet().size
        } catch (_: Exception) {}
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceConnected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return
        if (sbn.packageName == packageName) return
        _totalNotificationsToday.value++
        try {
            _batchedAppsCount.value = activeNotifications.map { it.packageName }.toSet().size
        } catch (_: Exception) {}
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        try {
            _batchedAppsCount.value = activeNotifications.map { it.packageName }.toSet().size
        } catch (_: Exception) {}
    }
}
