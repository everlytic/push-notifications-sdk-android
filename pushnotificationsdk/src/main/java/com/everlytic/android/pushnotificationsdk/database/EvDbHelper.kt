package com.everlytic.android.pushnotificationsdk.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.everlytic.android.pushnotificationsdk.logd

class EvDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        SQL_TABLES_CREATE.forEach { db.execSQL(it) }
        SQL_INDEXES_CREATE.forEach { db.execSQL(it) }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        logd("::onUpgrade()")
    }

    companion object {
        const val DB_NAME = "evpush.db"
        const val DB_VERSION = 1

        val SQL_TABLES_CREATE = arrayOf(
            EvDbContract.NotificationLogTable.CREATE_STATEMENT,
            EvDbContract.NotificationEventsLogTable.CREATE_STATEMENT
        )

        val SQL_INDEXES_CREATE = arrayOf(
            EvDbContract.NotificationEventsLogTable.INDEX_CREATE_EVENT_TYPE,
            EvDbContract.NotificationEventsLogTable.INDEX_CREATE_IS_UPLOADED_EVENT_TYPE
        )

        var instance: EvDbHelper? = null

        @Synchronized
        fun getInstance(context: Context) : EvDbHelper {
            return instance ?: EvDbHelper(context.applicationContext).also { instance = it }
        }
    }

}