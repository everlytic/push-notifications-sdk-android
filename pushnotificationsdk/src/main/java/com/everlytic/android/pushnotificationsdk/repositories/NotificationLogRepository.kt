package com.everlytic.android.pushnotificationsdk.repositories

import android.content.ContentValues
import com.everlytic.android.pushnotificationsdk.database.EvDbContract
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_ACTIONS
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_ANDROID_NOTIFICATION_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_BODY
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_CONTACT_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_CUSTOM_PARAMS
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_DISMISSED_AT
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_MESSAGE_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_READ_AT
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_RECEIVED_AT
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_SUBSCRIPTION_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.COL_TITLE
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable.TBL_NAME
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.database.adapters.toDate
import com.everlytic.android.pushnotificationsdk.database.adapters.toIso8601String
import com.everlytic.android.pushnotificationsdk.decodeJsonMap
import com.everlytic.android.pushnotificationsdk.encodeJsonMap
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.models.EverlyticNotification
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.ListAdapter
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.NotificationActionAdapter
import org.json.JSONArray
import java.util.*

internal class NotificationLogRepository(private val database: EvDbHelper) {
    private val tableName = EvDbContract.NotificationLogTable.TBL_NAME

    fun storeNotification(notification: EvNotification, subscriptionId: Long, contactId: Long) {
        val insert = ContentValues().apply {
            put(COL_MESSAGE_ID, notification.messageId)
            put(COL_ANDROID_NOTIFICATION_ID, notification.androidNotificationId)
            put(COL_SUBSCRIPTION_ID, subscriptionId)
            put(COL_CONTACT_ID, contactId)
            put(COL_TITLE, notification.title)
            put(COL_BODY, notification.body)
            put(COL_RECEIVED_AT, notification.received_at.toIso8601String())

            val actionListing = ListAdapter.toJson(notification.actions, NotificationActionAdapter)
            val customParametersListing = encodeJsonMap(notification.customParameters)

            put(COL_ACTIONS, actionListing.toString())
            put(COL_CUSTOM_PARAMS, customParametersListing.toString())
        }

        database.writableDatabase.let { db ->
            db.insert(tableName, null, insert)
        }
    }

    fun setNotificationAsRead(androidNotificationId: Int, date: Date = Date()) {

        val update = ContentValues().apply {
            put(EvDbContract.NotificationLogTable.COL_READ_AT, date.toIso8601String())
        }

        database.writableDatabase.let { db ->
            db.update(
                tableName,
                update,
                "${COL_ANDROID_NOTIFICATION_ID} = ?",
                arrayOf(androidNotificationId.toString())
            )
        }
    }

    fun setNotificationAsDismissed(androidNotificationId: Int, date: Date? = Date()) {
        val update = ContentValues().apply {
            put(COL_DISMISSED_AT, date?.toIso8601String())
        }

        database.writableDatabase.let { db ->
            db.update(
                tableName,
                update,
                "${COL_ANDROID_NOTIFICATION_ID} = ?",
                arrayOf(androidNotificationId.toString())
            )
        }
    }

    fun getPublicNotificationLogHistory(): List<EverlyticNotification> {

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
                    val customParams = decodeJsonMap(cursor.getString(cursor.getColumnIndex(COL_CUSTOM_PARAMS)))

                    list += EverlyticNotification(
                        cursor.getLong(cursor.getColumnIndex(COL_MESSAGE_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                        cursor.getString(cursor.getColumnIndex(COL_BODY)),
                        0 /*cursor.getInt(cursor.getColumnIndex(COL_))*/,
                        cursor.getString(cursor.getColumnIndex(COL_RECEIVED_AT)).toDate(),
                        cursor.getString(cursor.getColumnIndex(COL_READ_AT))?.toDate(),
                        custom_attributes = customParams
                    )
                }
            }
        }

        return list
    }

    fun getNotificationHistoryCount(): Int {
        return database.readableDatabase.let { db ->
            db.rawQuery("SELECT count(`$COL_ID`) FROM $TBL_NAME", null)
                .use {
                    if (it.moveToNext()) {
                        it.getInt(0)
                    } else 0
                }
        }
    }

    fun getUnactionedNotificationLogHistory(): List<EvNotification> {

        val list = mutableListOf<EvNotification>()

        database.readableDatabase.let { db ->
            db.query(
                tableName,
                null,
                "${COL_READ_AT} IS NULL AND ${COL_DISMISSED_AT} IS NULL",
                null,
                null,
                null,
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val actionArray = JSONArray(cursor.getString(cursor.getColumnIndex(COL_ACTIONS)))
                    val actions = ListAdapter.fromJson(actionArray, NotificationActionAdapter)
                    val customParams = decodeJsonMap(cursor.getString(cursor.getColumnIndex(COL_CUSTOM_PARAMS)))

                    list += EvNotification(
                        cursor.getLong(cursor.getColumnIndex(COL_MESSAGE_ID)),
                        cursor.getInt(cursor.getColumnIndex(COL_ANDROID_NOTIFICATION_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                        cursor.getString(cursor.getColumnIndex(COL_BODY)),
                        true,
                        0,
                        0,
                        0,
                        actions,
                        customParams,
                        cursor.getString(cursor.getColumnIndex(COL_RECEIVED_AT)).toDate(),
                        cursor.getString(cursor.getColumnIndex(COL_READ_AT))?.toDate(),
                        cursor.getString(cursor.getColumnIndex(COL_DISMISSED_AT))?.toDate()
                    )
                }
            }
        }

        return list
    }

    fun clearNotificationLogHistory(): Int {
        return database.writableDatabase.delete(TBL_NAME, null, null)
    }
}