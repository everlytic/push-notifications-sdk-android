package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.database.vendor.Iso8601Utils
import com.everlytic.android.pushnotificationsdk.decodeJsonMap
import com.everlytic.android.pushnotificationsdk.encodeJsonMap
import com.everlytic.android.pushnotificationsdk.getJSONObjectOrNull
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import org.json.JSONObject

internal object NotificationEventAdapter : JSONAdapterInterface<NotificationEvent> {
    override fun fromJson(json: JSONObject): NotificationEvent {
        return with(json.getJSONObjectOrNull("event") ?: json) {
            NotificationEvent(
                getInt("android_notification_id"),
                getLong("subscription_id"),
                getLong("message_id"),
                decodeJsonMap(getJSONObject("metadata")),
                Iso8601Utils.parse(getString("create_date")), //todo change to datetime
                type = NotificationEventType.valueOf(getString("type")),
                _id = getString("_id").toLongOrNull()
            )
        }
    }

    override fun toJson(obj: NotificationEvent): JSONObject {
        return JSONObject()
            .put("subscription_id", obj.subscription_id)
            .put("message_id", obj.message_id)
            .put("metadata", encodeJsonMap(obj.metadata))
            .put("datetime", Iso8601Utils.format(obj.datetime))
    }
}