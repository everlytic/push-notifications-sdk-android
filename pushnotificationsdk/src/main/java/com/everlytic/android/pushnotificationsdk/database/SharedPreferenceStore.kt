package com.everlytic.android.pushnotificationsdk.database

import android.content.Context
import android.content.SharedPreferences

object SharedPreferenceStore {
    private const val PREFERENCES_NAME = "ev_push_notifications_kv_store"

    private var instance: SharedPreferences? = null

    fun getInstance(context: Context) : SharedPreferences {
        instance?.let { return it }

        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).also {
            instance = it
        }
    }
}