package com.everlytic.android.pushnotificationsdk.repositories

import android.content.ContentValues
import com.everlytic.android.pushnotificationsdk.database.EvDbContract
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.database.adapters.toIso8601
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import java.util.*

class NotificationLogRepository(val database: EvDbHelper) {
    private val tableName = EvDbContract.NotificationLogTable.TBL_NAME

    fun storeNotification(notification: EvNotification, subscriptionId: Long, contactId: Long) {

        val insert = ContentValues().apply {
            put(EvDbContract.NotificationLogTable.COL_MESSAGE_ID, notification.messageId)
            put(EvDbContract.NotificationLogTable.COL_ANDROID_NOTIFICAITON_ID, notification.androidNotificationId)
            put(EvDbContract.NotificationLogTable.COL_SUBSCRIPTION_ID, subscriptionId)
            put(EvDbContract.NotificationLogTable.COL_CONTACT_ID, contactId)
            put(EvDbContract.NotificationLogTable.COL_TITLE, notification.title)
            put(EvDbContract.NotificationLogTable.COL_BODY, notification.body)
            put(EvDbContract.NotificationLogTable.COL_RECEIVED_AT, notification.received_at.toIso8601())
        }

        database.writableDatabase.let { db ->
            db.insert(tableName, null, insert)
        }
    }

    fun setNotificationAsRead(androidNotificationId: Int, date: Date = Date()) {

        val update = ContentValues().apply {
            put(EvDbContract.NotificationLogTable.COL_READ_AT, date.toIso8601())
        }

        database.writableDatabase.let { db ->
            db.update(
                tableName,
                update,
                "${EvDbContract.NotificationLogTable.COL_ANDROID_NOTIFICAITON_ID} = ?",
                arrayOf(androidNotificationId.toString())
            )
        }
    }

    fun setNotificationAsDismissed(androidNotificationId: Int, date: Date = Date()) {
        val update = ContentValues().apply {
            put(EvDbContract.NotificationLogTable.COL_DISMISSED_AT, date.toIso8601())
        }

        database.writableDatabase.let { db ->
            db.update(
                tableName,
                update,
                "${EvDbContract.NotificationLogTable.COL_ANDROID_NOTIFICAITON_ID} = ?",
                arrayOf(androidNotificationId.toString())
            )
        }
    }
}