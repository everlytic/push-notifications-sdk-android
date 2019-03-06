package com.everlytic.android.pushnotificationsdk

import android.util.Log

object EvLogger {

    fun d(tag: String, message: String? = null, throwable: Throwable? = null) {
        Log.d(tag, message, throwable)
    }
    fun w(tag: String, message: String? = null, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
    }

}