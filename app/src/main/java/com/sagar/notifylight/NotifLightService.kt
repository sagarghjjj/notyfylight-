package com.sagar.notifylight

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotifLightService : NotificationListenerService() {
    private lateinit var flashHelper: FlashHelper
    private val lastTriggerByPackage = HashMap<String, Long>()

    override fun onCreate() {
        super.onCreate()
        flashHelper = FlashHelper(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Ignore our own app and system UI
        if (sbn.packageName == packageName) return
        if (sbn.packageName == "com.android.systemui") return

        // Ignore ongoing/persistent notifications (Termux status bar,
        // music players, downloads, active calls, foreground services, etc.)
        if (sbn.isOngoing) return

        val now = System.currentTimeMillis()
        val last = lastTriggerByPackage[sbn.packageName] ?: 0L

        // Only allow one blink per app every 5 seconds, no matter how many
        // notifications that app posts in that window
        if (now - last < 5000) return

        lastTriggerByPackage[sbn.packageName] = now

        if (flashHelper.hasFrontFlash()) {
            flashHelper.blink()
        }
    }
}
