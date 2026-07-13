package com.sagar.notifylight

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotifLightService : NotificationListenerService() {
    private lateinit var flashHelper: FlashHelper

    override fun onCreate() {
        super.onCreate()
        flashHelper = FlashHelper(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (flashHelper.hasFrontFlash()) {
            flashHelper.blink()
        }
    }
}
