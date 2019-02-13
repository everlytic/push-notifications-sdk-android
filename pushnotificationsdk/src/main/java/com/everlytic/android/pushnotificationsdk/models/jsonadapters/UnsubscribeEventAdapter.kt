package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.database.vendor.Iso8601Utils
import com.everlytic.android.pushnotificationsdk.models.UnsubscribeEvent
import org.json.JSONObject

internal object UnsubscribeEventAdapter : JSONAdapterInterface<UnsubscribeEvent> {
    override fun fromJson(json: JSONObject): UnsubscribeEvent {
        return UnsubscribeEvent(
            json.getLong("subscription_id"),
            json.getString("device_id"),
            Iso8601Utils.parse(json.getString("datetime"))
        )
    }

    override fun toJson(obj: UnsubscribeEvent): JSONObject {
        return JSONObject()
            .put("subscription_id", obj.subscription_id)
            .put("device_id", obj.device_id)
            .put("datetime", Iso8601Utils.format(obj.datetime))
    }
}