package com.everlytic.android.pushnotificationsdk.database.adapters

import com.everlytic.android.pushnotificationsdk.database.vendor.Iso8601Utils
import java.util.*

/**
 * Convert ISO8601 string to date
 * @suppress
 * */
fun String.toDate(): Date {
    return Iso8601Utils.parse(this)
}

/**
 * @suppress
 * */
fun Date.toIso8601String(): String {
    return Iso8601Utils.format(this)
}