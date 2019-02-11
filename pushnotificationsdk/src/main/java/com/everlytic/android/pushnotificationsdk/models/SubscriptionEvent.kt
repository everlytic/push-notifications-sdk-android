package com.everlytic.android.pushnotificationsdk.models

import com.everlytic.android.pushnotificationsdk.facades.BuildFacade
import java.util.*

internal data class ContactData(
    val email: String,
    val push_token: String
)

internal data class PlatformData(
    val type: String = "android",
    val version: String = BuildFacade.getPlatformVersion()
)

internal data class DeviceData(
    val id: String,
    val manufacturer: String = BuildFacade.getDeviceManufacturer(),
    val model: String = BuildFacade.getDeviceModel(),
    val type: String
)

internal data class SubscriptionEvent (
    val list_id: String,
    val contact: ContactData,
    val metadata: Map<String, String> = emptyMap(),
    val platform: PlatformData = PlatformData(),
    val device: DeviceData,
    val datetime: Date = Date()
)