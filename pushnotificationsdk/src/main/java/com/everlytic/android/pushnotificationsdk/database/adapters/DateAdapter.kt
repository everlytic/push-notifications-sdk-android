package com.everlytic.android.pushnotificationsdk.database.adapters

import com.everlytic.android.pushnotificationsdk.database.vendor.Iso8601Utils
import java.util.*


fun String.asIso8601Date(): Date {
    return Iso8601Utils.parse(this)
}

fun Date.toIso8601(): String {
    return Iso8601Utils.format(this)
}