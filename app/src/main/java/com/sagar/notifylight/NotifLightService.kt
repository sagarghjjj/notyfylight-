package com.sagar.notifylight

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotifLightService : NotificationListenerService() {
    private lateinit var flashHelper: FlashHelper
    private var lastKey: String? = null
    private var lastTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        flashHelper = FlashHelper(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Ignore notifications from our own app (in case toggling the flash
        // ever creates a system notification that could loop back to us)
        if (sbn.packageName == packageName) return
        if (sbn.packageName == "com.android.systemui") return

        val now = System.currentTimeMillis()

        // Ignore repeats of the exact same notification within 4 seconds
        if (sbn.key == lastKey && (now - lastTime) < 4000) {
            return
        }

        lastKey = sbn.key
        lastTime = now

        if (flashHelper.hasFrontFlash()) {
            flashHelper.blink()
        }
    }
}
