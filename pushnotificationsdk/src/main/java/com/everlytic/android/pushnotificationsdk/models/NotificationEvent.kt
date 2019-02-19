package com.everlytic.android.pushnotificationsdk.models

import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import java.util.*

internal data class NotificationEvent (
    @Transient
    val android_notification_id: Int,
    val subscription_id: Long,
    val message_id: Long,
    val meta: Map<String, String> = emptyMap(),
    val datetime: Date = Date(),
    val type: NotificationEventType,
    val _id: Long? = null
)