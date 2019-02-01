package com.everlytic.android.pushnotificationsdk.repositories

import com.everlytic.android.pushnotificationsdk.EverlyticDb
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import java.util.*

class NotificationLogRepository(database: EverlyticDb) {

    private val queries = database.notificationLogQueries

    fun storeNotification(notification: EvNotification, subscriptionId: Long, contactId: Long) {
        queries.insertNotification(
            notification.messageId,
            notification.androidNotificationId,
            subscriptionId,
            contactId,
            notification.title,
            notification.body,
            notification.received_at
        )
    }

    fun setNotificationAsRead(androidNotificationId: Int, date: Date = Date()) {
        queries.updateNotificationDateStampsById(date, null, androidNotificationId)
    }

    fun setNotificationAsDismissed(androidNotificationId: Int, date: Date = Date()) {
        queries.updateNotificationDateStampsById(null, date, androidNotificationId)
    }

}