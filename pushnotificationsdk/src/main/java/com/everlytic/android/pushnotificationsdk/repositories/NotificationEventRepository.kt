package com.everlytic.android.pushnotificationsdk.repositories

import android.content.ContentValues
import com.everlytic.android.pushnotificationsdk.database.EvDbContract
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.database.adapters.toIso8601
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent;
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_EVENT_TYPE
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_ANDROID_NOTIFICATION_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_SUBSCRIPTION_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_METADATA
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_MESSAGE_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_DEVICE_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_DATETIME
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_IS_UPLOADED
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_ID
import com.everlytic.android.pushnotificationsdk.database.adapters.asIso8601Date
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.MapAdapter
import org.json.JSONObject

internal class NotificationEventRepository(
    val database: EvDbHelper,
    private val sdkRepository: SdkRepository
) {

    val tableName = EvDbContract.NotificationEventsLogTable.TBL_NAME

    fun storeNotificationEvent(eventType: NotificationEventType, event: NotificationEvent) {

        val deviceId = sdkRepository.getDeviceId()

        val values = ContentValues().apply {
            put(COL_EVENT_TYPE, eventType.name)
            put(COL_ANDROID_NOTIFICATION_ID, event.android_notification_id)
            put(COL_SUBSCRIPTION_ID, event.subscription_id)
            put(COL_MESSAGE_ID, event.message_id)
            put(COL_DEVICE_ID, deviceId!!)
            put(COL_DATETIME, event.datetime.toIso8601())
            put(COL_IS_UPLOADED, false)
        }

        database.writableDatabase.let { db ->
            db.insert(tableName, null, values)
        }

    }

    fun getEventsForNotificationByType(
        androidNotificationId: Int,
        eventType: NotificationEventType
    ): List<NotificationEvent> {
        database.readableDatabase.let { db ->
            db.query(
                tableName,
                null,
                "$COL_ANDROID_NOTIFICATION_ID = ? AND $COL_EVENT_TYPE = ?",
                arrayOf(androidNotificationId.toString(), eventType.name),
                null,
                null,
                null
            ).use { cursor ->
                val list = mutableListOf<NotificationEvent>()

                while (cursor.moveToNext()) {
                    list += NotificationEvent(
                        cursor.getInt(cursor.getColumnIndex(COL_ANDROID_NOTIFICATION_ID)),
                        cursor.getLong(cursor.getColumnIndex(COL_SUBSCRIPTION_ID)),
                        cursor.getLong(cursor.getColumnIndex(COL_MESSAGE_ID)),
                        MapAdapter.fromJson(JSONObject(cursor.getString(cursor.getColumnIndex(COL_METADATA)))),
                        cursor.getString(cursor.getColumnIndex(COL_DATETIME)).asIso8601Date(),
                        NotificationEventType.valueOf(cursor.getString(cursor.getColumnIndex(COL_EVENT_TYPE))),
                        cursor.getLong(cursor.getColumnIndex(COL_ID))
                    )
                }

                return list
            }
        }
    }

    fun getAllPendingEventsForType(eventLog: NotificationEventType): List<NotificationEvent> {
        database.readableDatabase.let { db ->
            db.query(
                tableName,
                null,
                "$COL_EVENT_TYPE = ? AND $COL_IS_UPLOADED = ?",
                arrayOf(eventLog.name, 0.toString()),
                null,
                null,
                null
            ).use { cursor ->
                val list = mutableListOf<NotificationEvent>()

                while (cursor.moveToNext()) {
                    list += NotificationEvent(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ANDROID_NOTIFICATION_ID)),
                        cursor.getLong(cursor.getColumnIndex(COL_SUBSCRIPTION_ID)),
                        cursor.getLong(cursor.getColumnIndex(COL_MESSAGE_ID)),
                        MapAdapter.fromJson(JSONObject(cursor.getString(cursor.getColumnIndex(COL_METADATA)))),
                        cursor.getString(cursor.getColumnIndex(COL_DATETIME)).asIso8601Date(),
                        NotificationEventType.valueOf(cursor.getString(cursor.getColumnIndex(COL_EVENT_TYPE))),
                        cursor.getLong(cursor.getColumnIndex(COL_ID))
                    )
                }

                return list
            }
        }
    }

    fun updateEventIsUploaded(eventId: Long, isUploaded: Boolean) {
        val values = ContentValues().apply {
            put(EvDbContract.NotificationEventsLogTable.COL_IS_UPLOADED, isUploaded)
        }

        database.writableDatabase.let { db ->
            db.update(
                tableName,
                values,
                "$COL_ID = ?",
                arrayOf(eventId.toString())
            )
        }
    }
}