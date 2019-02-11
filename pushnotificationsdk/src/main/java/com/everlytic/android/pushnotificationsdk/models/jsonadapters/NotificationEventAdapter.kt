package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import org.json.JSONObject

internal object NotificationEventAdapter : JSONAdapter<NotificationEvent> {
    override fun fromJson(json: JSONObject): NotificationEvent {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toJson(obj: NotificationEvent): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}