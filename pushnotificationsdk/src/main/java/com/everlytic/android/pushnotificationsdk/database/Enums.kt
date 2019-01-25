package com.everlytic.android.pushnotificationsdk.database

object Enums {
    enum class NotificationEventType {
        DELIVERY,
        CLICK,
        DISMISS,
        BOUNCE,
        SOFT_BOUNCE,
        UNKNOWN
    }

    enum class NotificationEventState {
        PENDING,
        RESERVED,
        UPLOADED,
    }
}