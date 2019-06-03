package com.everlytic.android.pushnotificationsdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository

/**
 * @suppress
 * */
class EvBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        restoreUnactionedNotifications(context)

    }

    private fun restoreUnactionedNotifications(context: Context) {
        logd("restoring notifications on boot")
        val repository = NotificationLogRepository(EvDbHelper.getInstance(context))

        val handler = EvNotificationHandler(context)

        repository.getUnactionedNotificationLogHistory()
            .forEach {
                handler.displayNotification(it)
            }
    }
}