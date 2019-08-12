package com.everlytic.android.pushnotificationsdk.repositories

import android.content.ContentValues
import com.everlytic.android.pushnotificationsdk.database.EvDbContract
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.database.adapters.toIso8601String
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_EVENT_TYPE
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_ANDROID_NOTIFICATION_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_SUBSCRIPTION_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_METADATA
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_MESSAGE_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_DEVICE_ID
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_DATETIME
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationEventsLogTable.COL_ID
import com.everlytic.android.pushnotificationsdk.database.adapters.toDate
import com.everlytic.android.pushnotificationsdk.logd
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.MapAdapter
import org.json.JSONObject
import com.everlytic.android.pushnotificationsdk.database.EvDbContract.NotificationLogTable as logTable

internal class NotificationEventRepository(
    val database: EvDbHelper,
    private val sdkRepository: SdkRepository
) {

    val tableName = EvDbContract.NotificationEventsLogTable.TBL_NAME
    val logTableName = logTable.TBL_NAME

    fun storeNotificationEvent(event: NotificationEvent) {

        val deviceId = sdkRepository.getDeviceId()

        val values = ContentValues().apply {
            put(COL_EVENT_TYPE, event.type.name)
            put(COL_ANDROID_NOTIFICATION_ID, event.android_notification_id)
            put(COL_SUBSCRIPTION_ID, event.subscription_id)
            put(COL_MESSAGE_ID, event.message_id)
            put(COL_DEVICE_ID, deviceId!!)
            put(COL_DATETIME, event.datetime.toIso8601String())
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
                        cursor.getString(cursor.getColumnIndex(COL_DATETIME)).toDate(),
                        NotificationEventType.valueOf(cursor.getString(cursor.getColumnIndex(COL_EVENT_TYPE))),
                        cursor.getLong(cursor.getColumnIndex(COL_ID))
                    )
                }

                return list
            }
        }
    }

    fun getAllPendingEventsForType(eventLog: NotificationEventType): List<NotificationEvent> {
        val sql = """
                SELECT
                    ${tableName}.${COL_ID},
                    ${tableName}.${COL_ANDROID_NOTIFICATION_ID},
                    ${tableName}.${COL_SUBSCRIPTION_ID},
                    ${tableName}.${COL_MESSAGE_ID},
                    ${tableName}.${COL_DATETIME},
                    ${tableName}.${COL_EVENT_TYPE},
                    ${tableName}.${COL_METADATA},
                    ${logTableName}.${logTable.COL_RETURN_DATA}
                FROM ${tableName}
                LEFT JOIN ${logTableName} ON ${tableName}.${COL_MESSAGE_ID}=${logTableName}.${logTable.COL_MESSAGE_ID}
                WHERE $COL_EVENT_TYPE = ?
                """.trimIndent()

        database.readableDatabase.let { db ->
            db.rawQuery(
                sql,
                arrayOf(eventLog.name)
            ).use { cursor ->
                val list = mutableListOf<NotificationEvent>()

                while (cursor.moveToNext()) {

                    val meta = MapAdapter
                        .fromJson(JSONObject(cursor.getString(cursor.getColumnIndex(COL_METADATA))))
                        .toMutableMap()
                    meta["ev_return_data"] = cursor.getString(cursor.getColumnIndex(logTable.COL_RETURN_DATA))

                    list += NotificationEvent(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ANDROID_NOTIFICATION_ID)),
                        cursor.getLong(cursor.getColumnIndex(COL_SUBSCRIPTION_ID)),
                        cursor.getLong(cursor.getColumnIndex(COL_MESSAGE_ID)),
                        meta,
                        cursor.getString(cursor.getColumnIndex(COL_DATETIME)).toDate(),
                        NotificationEventType.valueOf(cursor.getString(cursor.getColumnIndex(COL_EVENT_TYPE))),
                        cursor.getLong(cursor.getColumnIndex(COL_ID))
                    )
                }

                return list
            }
        }
    }

    fun deleteNotificationEvent(eventId: Long) {
        database.writableDatabase.let { db ->
            db.delete(
                tableName,
                "$COL_ID = ?",
                arrayOf(eventId.toString())
            ).let {
                logd("Deleted event $eventId = $it")
            }
        }
    }
}