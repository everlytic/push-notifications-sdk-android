package com.everlytic.android.pushnotificationsdk.eventreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.handlers.NotificationDismissedHandler
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
/**
 * @suppress
 * */
internal class EvNotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sdkRepository = SdkRepository(context)
        val db = EvDbHelper.getInstance(context)
        val eventsRepository = NotificationEventRepository(db, sdkRepository)
        val logRepository = NotificationLogRepository(db)
        NotificationDismissedHandler(
            sdkRepository,
            eventsRepository,
            logRepository
        ).handleIntentWithContext(context, intent)
    }
}