package com.everlytic.android.pushnotificationsdk.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.everlytic.android.pushnotificationsdk.logd
/**
 * @suppress
 * */
class EvDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        EvDbContract.getCreateStatements().forEach { db.execSQL(it) }
    }

    @Suppress("UNUSED_CHANGED_VALUE")
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        logd("::onUpgrade()")
        EvDbContract.getMigrations().let { migrations ->
            for (version in oldVersion until newVersion) {
                migrations[version]?.forEachIndexed { _, sql ->
                    logd("::onUpgrade() executing sql: $sql")
                    db.execSQL(sql)
                }
            }
        }
    }

    companion object {
        const val DB_NAME = "evpush.db"
        const val DB_VERSION = 3

        var instance: EvDbHelper? = null

        @Synchronized
        fun getInstance(context: Context): EvDbHelper {
            return instance ?: EvDbHelper(context.applicationContext).also { instance = it }
        }
    }

}