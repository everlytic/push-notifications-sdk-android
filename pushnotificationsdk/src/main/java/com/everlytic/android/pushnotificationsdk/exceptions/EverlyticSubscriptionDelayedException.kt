package com.everlytic.android.pushnotificationsdk.exceptions

class EverlyticSubscriptionDelayedException(message: String? = null)
    : Exception(message ?: "Contact Subscription has been delayed until a network connection is available")