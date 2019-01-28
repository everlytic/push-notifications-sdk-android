package com.everlytic.android.pushnotificationsdk.handlers

import android.content.Context
import android.content.Intent
import com.everlytic.android.pushnotificationsdk.EvIntentExtras
import com.everlytic.android.pushnotificationsdk.database.Database
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import com.everlytic.android.pushnotificationsdk.workers.EvWorkManager
import com.everlytic.android.pushnotificationsdk.workers.UploadMessageEventsWorker

internal class NotificationOpenedHandler(
    private val sdkRepository: SdkRepository,
    private val repository: NotificationEventRepository
) {

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

        val event = createNotificationEvent(intent, sdkRepository)

        repository.storeNotificationEvent(NotificationEventType.CLICK, event)

        scheduleEventUploadWorker()

        startLauncherActivityInContext(context)
    }

    private fun startLauncherActivityInContext(context: Context) {
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT and Intent.FLAG_ACTIVITY_NEW_TASK
        }?.let {
            context.startActivity(it)
        }
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

    private fun scheduleEventUploadWorker() {
        EvWorkManager.scheduleOneTimeWorker<UploadMessageEventsWorker>()
    }
}