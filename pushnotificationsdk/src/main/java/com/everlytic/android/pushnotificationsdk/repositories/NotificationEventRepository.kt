package com.everlytic.android.pushnotificationsdk.repositories

import com.everlytic.android.pushnotificationsdk.EverlyticDb
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import java.util.*

internal class NotificationEventRepository(
    private val database: EverlyticDb,
    private val sdkRepository: SdkRepository
) {

    val queries = database.notificationEventsQueries

    fun storeNotificationEvent(eventType: NotificationEventType, event: NotificationEvent) {

        val deviceId = sdkRepository.getDeviceId()

        queries.insertEvent(
            eventType,
            event.android_notification_id,
            event.subscription_id,
            event.message_id,
            deviceId!!,
            Date(),
            false
        )

    }

    fun getEventsForNotificationByType(
        androidNotificationId: Long,
        eventType: NotificationEventType
    ) = queries.getEventsForNotificationByType(androidNotificationId, eventType).executeAsList()

    fun getAllPendingEventsForType(eventLog: NotificationEventType) =
        queries.getEventsPendingUploadByType(eventLog).executeAsList()

    fun updateEventIsUploaded(eventId: Long, isUploaded: Boolean) =
        queries.updateEventIsUploadedById(isUploaded, eventId)

}