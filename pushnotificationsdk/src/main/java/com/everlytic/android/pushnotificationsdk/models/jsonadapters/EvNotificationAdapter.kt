package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.database.vendor.Iso8601Utils
import com.everlytic.android.pushnotificationsdk.decodeJsonMap
import com.everlytic.android.pushnotificationsdk.encodeJsonMap
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import org.json.JSONObject

object EvNotificationAdapter : JSONAdapterInterface<EvNotification> {
    override fun fromJson(json: JSONObject): EvNotification {

        val receivedAt = json.getString("received_at")
        val readAt = json.getString("read_at")
        val dismissedAt = json.getString("dismissed_at")

        return EvNotification(
            json.getLong("messageId"),
            json.getInt("androidNotificationId"),
            json.getString("title"),
            json.getString("body"),
            json.getBoolean("useSound"),
            json.getInt("color"),
            json.getInt("icon"),
            json.getInt("priority"),
            ListAdapter.fromJson(json.getJSONArray("actions"), NotificationActionAdapter),
            decodeJsonMap(json.getJSONObject("customActions")),
            Iso8601Utils.parse(receivedAt),
            Iso8601Utils.parse(readAt),
            Iso8601Utils.parse(dismissedAt)
        )
    }

    override fun toJson(obj: EvNotification): JSONObject {
        return JSONObject()
            .put("messageId", obj.messageId)
            .put("androidNotificationId", obj.androidNotificationId)
            .put("title", obj.title)
            .put("body", obj.body)
            .put("useSound", obj.useSound)
            .put("color", obj.color)
            .put("icon", obj.icon)
            .put("priority", obj.priority)
            .put("received_at", Iso8601Utils.format(obj.received_at))
            .put("read_at", Iso8601Utils.format(obj.read_at))
            .put("dismissed_at", Iso8601Utils.format(obj.dismissed_at))
            .put("customParameters", encodeJsonMap(obj.customParameters))
            .put("actions", ListAdapter.toJson(obj.actions, NotificationActionAdapter))
    }
}