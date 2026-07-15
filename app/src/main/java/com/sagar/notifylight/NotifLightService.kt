package com.sagar.notifylight

import android.app.Notification
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
        if (sbn.packageName == packageName) return
        if (sbn.packageName == "com.android.systemui") return

        val isCall = sbn.notification.category == Notification.CATEGORY_CALL

        // Block ongoing notifications UNLESS it's a phone call
        if (sbn.isOngoing && !isCall) return

        val now = System.currentTimeMillis()
        val last = lastTriggerByPackage[sbn.packageName] ?: 0L

        // Calls repeat every ~5s while ringing; other apps rate-limited too
        val cooldown = if (isCall) 4000L else 5000L
        if (now - last < cooldown) return

        lastTriggerByPackage[sbn.packageName] = now

        if (flashHelper.hasFrontFlash()) {
            flashHelper.blink()
        }
    }
}
