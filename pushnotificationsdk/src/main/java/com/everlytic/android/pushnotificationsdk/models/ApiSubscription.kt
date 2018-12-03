package com.everlytic.android.pushnotificationsdk.models

data class ApiSubscription(
    val pns_id : Int,
    val pns_configuration_id : Int,
    val pns_customer_id : Int,
    val pns_contact_id : Int,
    val pns_device_id : String
)