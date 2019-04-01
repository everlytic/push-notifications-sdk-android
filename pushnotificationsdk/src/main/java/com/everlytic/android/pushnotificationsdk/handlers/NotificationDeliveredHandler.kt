package com.everlytic.android.pushnotificationsdk.handlers

import android.content.Context
import com.everlytic.android.pushnotificationsdk.EvNotificationHandler
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import com.everlytic.android.pushnotificationsdk.workers.UploadMessageEventsService

internal class NotificationDeliveredHandler(
    val context: Context,
    private val sdkRepository: SdkRepository,
    private val notificationEventRepository: NotificationEventRepository,
    private val notificationHandler: EvNotificationHandler
) {
    fun processDeliveryEventForNotification(notification: EvNotification) {
        val event = createDeliveryEvent(notification, sdkRepository)
        notificationEventRepository.storeNotificationEvent(event)
        scheduleEventUploadWorker()
    }

    private fun scheduleEventUploadWorker() {
        UploadMessageEventsService.enqueue(context)
    }

    private fun createDeliveryEvent(notification: EvNotification, sdkRepository: SdkRepository): NotificationEvent {
        return NotificationEvent(
            notification.androidNotificationId,
            sdkRepository.getSubscriptionId() ?: -1,
            notification.messageId,
            metadata = mapOf("displayed" to notificationHandler.canDisplayNotifications().toString()),
            type = NotificationEventType.DELIVERY
        )
    }
}