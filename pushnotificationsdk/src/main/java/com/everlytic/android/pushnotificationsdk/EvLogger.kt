package com.everlytic.android.pushnotificationsdk

import android.util.Log

/**
 * @suppress
 * */
internal object EvLogger {

    var logLevel = Log.ERROR

    fun d(tag: String, message: String? = null, throwable: Throwable? = null) {
        if (logLevel == Log.DEBUG || BuildConfig.DEBUG) {
            Log.d(tag, message, throwable)
        }
    }

    fun i(tag: String, message: String? = null, throwable: Throwable? = null) {
        if (logLevel == Log.INFO || BuildConfig.DEBUG) {
            Log.i(tag, message, throwable)
        }
    }

    fun w(tag: String, message: String? = null, throwable: Throwable? = null) {
        if (logLevel == Log.WARN || BuildConfig.DEBUG) {
            Log.w(tag, message, throwable)
        }
    }

    fun e(tag: String, message: String? = null, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }

}