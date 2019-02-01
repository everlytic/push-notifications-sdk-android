package com.everlytic.android.pushnotificationsdk.models

import com.squareup.moshi.Json
import java.util.*

internal data class NotificationEvent (
    @Transient
    val android_notification_id: Int,
    val subscription_id: Long,
    val message_id: Long,
    val meta: String = "{}",
    @field:Json(name = "create_date")
    val datetime: Date = Date()
)