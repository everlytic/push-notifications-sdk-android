package com.everlytic.android.pushnotificationsdk.models

import com.everlytic.android.pushnotificationsdk.facades.BuildFacade
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
internal data class ContactData(
    val email: String,
    val push_token: String
)

@JsonClass(generateAdapter = true)
internal data class PlatformData(
    val type: String = "android",
    val version: String = BuildFacade.getPlatformVersion()
)

@JsonClass(generateAdapter = true)
internal data class DeviceData(
    val id: String,
    val manufacturer: String = BuildFacade.getDeviceManufacturer(),
    val model: String = BuildFacade.getDeviceModel(),
    val type: String
)

@JsonClass(generateAdapter = true)
internal data class SubscriptionEvent (
    val push_configuration_id: String,
    val contact: ContactData,
    val metadata: Map<String, String> = emptyMap(),
    val platform: PlatformData = PlatformData(),
    val device: DeviceData,
    val datetime: Date = Date()
)