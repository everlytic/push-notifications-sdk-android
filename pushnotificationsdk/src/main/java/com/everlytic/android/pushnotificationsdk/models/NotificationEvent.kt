package com.everlytic.android.pushnotificationsdk.models

import com.squareup.moshi.Json
import java.util.*

internal data class NotificationEvent (
    @Transient
    val android_notification_id: Long,
    val subscription_id: Long,
    val message_id: Long,
    @Json(name = "meta")
    val metadata: String = "{}",
    val datetime: Date = Date()
)