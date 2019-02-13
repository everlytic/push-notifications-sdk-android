package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.database.vendor.Iso8601Utils
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import org.json.JSONObject

internal object NotificationEventAdapter : JSONAdapterInterface<NotificationEvent> {
    override fun fromJson(json: JSONObject): NotificationEvent {
        return NotificationEvent(
            json.getInt("android_notification_id"),
            json.getLong("subscription_id"),
            json.getLong("message_id"),
            MapAdapter.fromJson(json.getJSONObject("meta")),
            Iso8601Utils.parse(json.getString("datetime"))
        )
    }

    override fun toJson(obj: NotificationEvent): JSONObject {
        return JSONObject()
            .put("subscription_id", obj.subscription_id)
            .put("message_id", obj.subscription_id)
            .put("meta", MapAdapter.toJson(obj.meta))
            .put("datetime", Iso8601Utils.format(obj.datetime))
    }
}