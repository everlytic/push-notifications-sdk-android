package com.everlytic.android.pushnotificationsdk.models

import android.os.Bundle
import com.everlytic.android.pushnotificationsdk.facades.BuildFacade
import com.squareup.moshi.Json
import java.util.*

data class ContactData(
    val email: String,
    val push_token: String
)

data class PlatformData(
    val type: String = "android",
    val version: String = BuildFacade.getPlatformVersion()
)

data class DeviceData(
    val id: String,
    val manufacturer: String = BuildFacade.getDeviceManufacturer(),
    val model: String = BuildFacade.getDeviceModel()
)

data class SubscriptionEvent (
    val deviceId: String,
    val push_configuration_id: String,
    val contact: ContactData,
    val metadata: Bundle? = null,
    val platform: PlatformData = PlatformData(),
    val device: DeviceData = DeviceData(deviceId),
    val datetime: Date = Date()
)