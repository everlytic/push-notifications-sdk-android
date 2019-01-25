package com.everlytic.android.pushnotificationsdk.handlers

import android.content.Context
import android.content.Intent
import com.everlytic.android.pushnotificationsdk.EvIntentExtras
import com.everlytic.android.pushnotificationsdk.database.Database
import com.everlytic.android.pushnotificationsdk.database.Enums
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import com.everlytic.android.pushnotificationsdk.workers.EvWorkManager
import com.everlytic.android.pushnotificationsdk.workers.UploadMessageEventsWorker

internal object NotificationOpenedHandler {

    fun handleIntentWithContext(context: Context, intent: Intent) {
        if (!intent.isEverlyticEventIntent()) {
            return
        }

        processIntent(context, intent)
    }

    private fun Intent.isEverlyticEventIntent(): Boolean {
        return this.hasExtra(EvIntentExtras.EVERLYTIC_DATA) || this.hasExtra(EvIntentExtras.ANDROID_NOTIFICATION_ID)
    }

    private fun processIntent(context: Context, intent: Intent) {
        val sdkRepository = SdkRepository(context)
        val repository = NotificationEventRepository(Database.getInstance(context), sdkRepository)

        val event = createNotificationEvent(intent, sdkRepository)

        repository.storeNotificationEvent(Enums.NotificationEventType.CLICK, event)

        EvWorkManager.scheduleOneTimeWorker<UploadMessageEventsWorker>()
    }

    private fun createNotificationEvent(
        intent: Intent,
        sdkRepository: SdkRepository
    ): NotificationEvent {
        val notification = intent.getParcelableExtra<EvNotification>(EvIntentExtras.EVERLYTIC_DATA)

        return NotificationEvent(
            notification.androidNotificationId,
            sdkRepository.getSubscriptionId()?.toLong() ?: -1,
            notification.messageId
        )
    }
}