package com.everlytic.android.pushnotificationsdk.models

internal data class ApiSubscriptionResponse(
    val result: String,
    val data: ApiSubscriptionHolder
)

internal data class ApiSubscriptionHolder(val subscription: ApiSubscription)

internal data class ApiSubscription(
    val pns_id: String,
    val pns_project_id: String,
    val pns_customer_id: String,
    val pns_contact_id: String,
    val pns_device_id: String
)