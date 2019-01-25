package com.everlytic.android.pushnotificationsdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.everlytic.android.pushnotificationsdk.handlers.NotificationOpenedHandler

internal class EvNotificationClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationOpenedHandler.handleIntentWithContext(context, intent)
    }
}