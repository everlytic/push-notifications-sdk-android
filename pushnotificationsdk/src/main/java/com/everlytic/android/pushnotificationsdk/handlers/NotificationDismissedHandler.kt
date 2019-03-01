package com.everlytic.android.pushnotificationsdk.handlers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.everlytic.android.pushnotificationsdk.EvIntentExtras
import com.everlytic.android.pushnotificationsdk.EvNotificationHandler
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.isEverlyticEventIntent
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import com.everlytic.android.pushnotificationsdk.workers.JobIntentService
import com.everlytic.android.pushnotificationsdk.workers.UploadMessageEventsService

internal class NotificationDismissedHandler(
    private val sdkRepository: SdkRepository,
    private val notificationEventRepository: NotificationEventRepository,
    private val notificationLogRepository: NotificationLogRepository
) {

    private val eventType = NotificationEventType.DISMISS

    fun handleIntentWithContext(context: Context, intent: Intent) {
        if (!intent.isEverlyticEventIntent()) {
            return
        }

        processIntent(context, intent)
        setNotificationDismissedFromIntent(intent)
    }

    private fun processIntent(context: Context, intent: Intent) {
        val event = createNotificationEvent(intent, sdkRepository)
        notificationEventRepository.storeNotificationEvent(event)
        scheduleEventUploadWorker(context)
    }

    private fun setNotificationDismissedFromIntent(intent: Intent) {
        val androidNotificationId =
            intent.getParcelableExtra<EvNotification>(EvIntentExtras.EVERLYTIC_DATA).androidNotificationId

        notificationLogRepository.setNotificationAsDismissed(androidNotificationId)
    }

    private fun createNotificationEvent(
        intent: Intent,
        sdkRepository: SdkRepository
    ): NotificationEvent {
        val notification = intent.getParcelableExtra<EvNotification>(EvIntentExtras.EVERLYTIC_DATA)

        return NotificationEvent(
            notification.androidNotificationId,
            sdkRepository.getSubscriptionId() ?: -1,
            notification.messageId,
            type = eventType
        )
    }

    private fun scheduleEventUploadWorker(context: Context) {
        val componentName = ComponentName(context, UploadMessageEventsService::class.java)
        JobIntentService.enqueueWork(context, componentName, UploadMessageEventsService.JOB_SERVICE_ID, Intent())
    }
}