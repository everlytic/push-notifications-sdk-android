package com.everlytic.android.pushnotificationsdk.models

import java.util.*

internal data class UnsubscribeEvent(
    val subscription_id: Int,
    val device_id: String,
    val datetime: Date = Date()
)