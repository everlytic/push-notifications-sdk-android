package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.database.vendor.Iso8601Utils
import com.everlytic.android.pushnotificationsdk.models.ContactData
import com.everlytic.android.pushnotificationsdk.models.DeviceData
import com.everlytic.android.pushnotificationsdk.models.PlatformData
import com.everlytic.android.pushnotificationsdk.models.SubscriptionEvent
import org.json.JSONObject

internal object SubscriptionEventAdapter : JSONAdapterInterface<SubscriptionEvent> {
    override fun fromJson(json: JSONObject): SubscriptionEvent {
        return SubscriptionEvent(
            json.getString("list_id"),
            ContactDataAdapter.fromJson(json.getJSONObject("contact")),
            MapAdapter.fromJson(json.getJSONObject("metadata")),
            PlatformDataAdapter.fromJson(json.getJSONObject("platform")),
            DeviceDataAdapter.fromJson(json.getJSONObject("device")),
            Iso8601Utils.parse(json.getString("datetime"))
        )
    }

    override fun toJson(obj: SubscriptionEvent): JSONObject {
        return JSONObject()
            .put("list_id", obj.list_id)
            .put("contact", ContactDataAdapter.toJson(obj.contact))
            .put("platform", PlatformDataAdapter.toJson(obj.platform))
            .put("device", DeviceDataAdapter.toJson(obj.device))
            .put("metadata", MapAdapter.toJson(obj.metadata))
            .put("datetime", Iso8601Utils.format(obj.datetime))
    }

    object ContactDataAdapter : JSONAdapterInterface<ContactData> {
        override fun fromJson(json: JSONObject): ContactData {
            return ContactData(
                json.getString("email"),
                json.getString("push_token")
            )
        }

        override fun toJson(obj: ContactData): JSONObject {
            return JSONObject()
                .put("email", obj.email)
                .put("push_token", obj.push_token)
        }

    }

    object PlatformDataAdapter : JSONAdapterInterface<PlatformData> {
        override fun fromJson(json: JSONObject): PlatformData {
            return PlatformData(
                json.getString("type"),
                json.getString("version")
            )
        }

        override fun toJson(obj: PlatformData): JSONObject {
            return JSONObject()
                .put("type", obj.type)
                .put("version", obj.version)
        }

    }

    object DeviceDataAdapter : JSONAdapterInterface<DeviceData> {
        override fun fromJson(json: JSONObject): DeviceData {
            return DeviceData(
                json.getString("id"),
                json.getString("manufacturer"),
                json.getString("model"),
                json.getString("type")
            )
        }

        override fun toJson(obj: DeviceData): JSONObject {
            return JSONObject()
                .put("id", obj.id)
                .put("manufacturer", obj.manufacturer)
                .put("model", obj.model)
                .put("type", obj.type)
        }

    }
}

