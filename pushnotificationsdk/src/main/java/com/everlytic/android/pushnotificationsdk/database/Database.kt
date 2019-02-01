package com.everlytic.android.pushnotificationsdk.database

import android.content.Context
import com.everlytic.android.pushnotificationsdk.EverlyticDb
import com.everlytic.android.pushnotificationsdk.NotificationEventsLog
import com.everlytic.android.pushnotificationsdk.NotificationLog
import com.everlytic.android.pushnotificationsdk.database.adapters.DateAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

object Database {
    private var instance: EverlyticDb? = null

    fun getInstance(context: Context): EverlyticDb {
        instance?.let { return it }

        return createDbWrapper(AndroidSqliteDriver(EverlyticDb.Schema, context, "evpush.db"))
            .also {
                instance = it
            }
    }

    fun releaseInstance() {
        instance = null
    }

    private fun createDbWrapper(driver: SqlDriver): EverlyticDb {
        return EverlyticDb(
            driver = driver,
            NotificationEventsLogAdapter = createNotificationEventsLogAdapter(),
            NotificationLogAdapter = createNotificationLogAdapter()
        )
    }

    private fun createNotificationLogAdapter(): NotificationLog.Adapter {
        return NotificationLog.Adapter(
            received_atAdapter = DateAdapter(),
            read_atAdapter = DateAdapter(),
            dismissed_atAdapter = DateAdapter()
        )
    }

    private fun createNotificationEventsLogAdapter(): NotificationEventsLog.Adapter {
        return NotificationEventsLog.Adapter(
            event_typeAdapter = EnumColumnAdapter(),
            datetimeAdapter = DateAdapter()
        )
    }
}