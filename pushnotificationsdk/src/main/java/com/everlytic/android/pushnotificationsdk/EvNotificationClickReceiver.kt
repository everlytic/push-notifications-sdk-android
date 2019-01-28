package com.everlytic.android.pushnotificationsdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.everlytic.android.pushnotificationsdk.database.Database
import com.everlytic.android.pushnotificationsdk.handlers.NotificationOpenedHandler
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository

internal class EvNotificationClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sdkRepository = SdkRepository(context)
        val repository = NotificationEventRepository(Database.getInstance(context), sdkRepository)
        NotificationOpenedHandler(sdkRepository, repository).handleIntentWithContext(context, intent)
    }
}