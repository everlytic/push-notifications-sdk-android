package com.everlytic.android.pushnotificationsdk.models

import java.util.*

data class EverlyticNotification(
    val messageId: Long,
    val title: String,
    val body: String?,
    val icon: Int,
    val received_at: Date,
    val read_at: Date? = null,
    val dismissed_at: Date? = null
)