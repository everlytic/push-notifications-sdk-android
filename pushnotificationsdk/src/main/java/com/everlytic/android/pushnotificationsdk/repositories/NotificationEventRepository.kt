package com.everlytic.android.pushnotificationsdk.repositories

import com.everlytic.android.pushnotificationsdk.EverlyticDb
import com.everlytic.android.pushnotificationsdk.NotificationEventsLog
import com.everlytic.android.pushnotificationsdk.database.Enums
import com.everlytic.android.pushnotificationsdk.database.Enums.NotificationEventState.*
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import java.util.*

internal class NotificationEventRepository(
    private val database: EverlyticDb,
    private val sdkRepository: SdkRepository
) {

    val queries = database.notificationEventsQueries

    fun storeNotificationEvent(eventType: Enums.NotificationEventType, event: NotificationEvent) {

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
        eventType: Enums.NotificationEventType
    ) = queries.getEventsForNotificationByType(androidNotificationId, eventType).executeAsList()

    fun getAllPendingEventsForType(eventLog: Enums.NotificationEventType) =
        queries.getEventsPendingUploadByType(eventLog).executeAsList()

    fun updateEventIsUploaded(eventId: Long, isUploaded: Boolean) =
        queries.updateEventIsUploadedById(isUploaded, eventId)

}