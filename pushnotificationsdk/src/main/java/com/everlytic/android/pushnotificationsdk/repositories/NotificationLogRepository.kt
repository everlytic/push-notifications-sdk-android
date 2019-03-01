package com.everlytic.android.pushnotificationsdk.repositories

import android.content.ContentValues
import com.everlytic.android.pushnotificationsdk.database.EvDbContract
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.database.adapters.toIso8601
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.models.EverlyticNotification
import java.util.*
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_MESSAGE_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_ANDROID_NOTIFICAITON_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_SUBSCRIPTION_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_CONTACT_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_TITLE
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_BODY
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_DISMISSED_AT
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_RECEIVED_AT
import com.everlytic.android.pushnotificationsdk.database.adapters.asIso8601Date

class NotificationLogRepository(private val database: EvDbHelper) {
    private val tableName = EvDbContract.NotificationLogTable.TBL_NAME

    fun storeNotification(notification: EvNotification, subscriptionId: Long, contactId: Long) {

        val insert = ContentValues().apply {
            put(COL_MESSAGE_ID, notification.messageId)
            put(COL_ANDROID_NOTIFICAITON_ID, notification.androidNotificationId)
            put(COL_SUBSCRIPTION_ID, subscriptionId)
            put(COL_CONTACT_ID, contactId)
            put(COL_TITLE, notification.title)
            put(COL_BODY, notification.body)
            put(COL_RECEIVED_AT, notification.received_at.toIso8601())
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
                "${COL_ANDROID_NOTIFICAITON_ID} = ?",
                arrayOf(androidNotificationId.toString())
            )
        }
    }

    fun setNotificationAsDismissed(androidNotificationId: Int, date: Date? = Date()) {
        val update = ContentValues().apply {
            put(COL_DISMISSED_AT, date?.toIso8601())
        }

        database.writableDatabase.let { db ->
            db.update(
                tableName,
                update,
                "${COL_ANDROID_NOTIFICAITON_ID} = ?",
                arrayOf(androidNotificationId.toString())
            )
        }
    }

    fun getNotificationLogHistory() : List<EverlyticNotification> {

        val list = mutableListOf<EverlyticNotification>()

        database.readableDatabase.let { db ->
            db.query(
                tableName,
                null,
                null,
                null,
                null,
                null,
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    list += EverlyticNotification(
                        cursor.getLong(cursor.getColumnIndex(COL_MESSAGE_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_MESSAGE_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_MESSAGE_ID)),
                        cursor.getInt(cursor.getColumnIndex(COL_MESSAGE_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_MESSAGE_ID)).asIso8601Date(),
                        cursor.getString(cursor.getColumnIndex(COL_MESSAGE_ID)).asIso8601Date()
                    )
                }
            }
        }

        return list
    }
}