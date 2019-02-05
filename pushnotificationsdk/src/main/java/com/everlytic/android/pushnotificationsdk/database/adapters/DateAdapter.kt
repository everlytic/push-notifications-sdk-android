package com.everlytic.android.pushnotificationsdk.database.adapters

import com.everlytic.android.pushnotificationsdk.database.vendor.Iso8601Utils
import com.squareup.sqldelight.ColumnAdapter
import java.util.*

class DateAdapter : ColumnAdapter<Date, String> {
    override fun decode(databaseValue: String): Date {
        return Iso8601Utils.parse(databaseValue)
    }

    override fun encode(value: Date): String {
        return Iso8601Utils.format(value)
    }
}