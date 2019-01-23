package com.everlytic.android.pushnotificationsdk.database

abstract class Types {
    enum class NotificationEvent {
        DELIVERY,
        CLICK,
        DISMISS,
        BOUNCE,
        SOFT_BOUNCE,
        UNKNOWN
    }
}