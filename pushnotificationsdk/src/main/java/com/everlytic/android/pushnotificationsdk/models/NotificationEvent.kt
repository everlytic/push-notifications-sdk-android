package com.everlytic.android.pushnotificationsdk.models

import java.util.*

internal data class NotificationEvent (
    @Transient
    val android_notification_id: Int,
    val subscription_id: Long,
    val message_id: Long,
    val meta: Map<String, String> = emptyMap(),
    val datetime: Date = Date()
)