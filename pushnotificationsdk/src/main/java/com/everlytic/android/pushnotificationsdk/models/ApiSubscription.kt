package com.everlytic.android.pushnotificationsdk.models

internal data class ApiSubscription(
    val pns_id: String,
    val pns_list_id: String,
    val pns_customer_id: String,
    val pns_contact_id: String,
    val pns_device_id: String
)