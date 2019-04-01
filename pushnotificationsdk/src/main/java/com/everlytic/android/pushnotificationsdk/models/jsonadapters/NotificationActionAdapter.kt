package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import android.net.Uri
import com.everlytic.android.pushnotificationsdk.models.GoToUrlNotificationAction
import com.everlytic.android.pushnotificationsdk.models.LaunchAppNotificationAction
import com.everlytic.android.pushnotificationsdk.models.NotificationAction
import org.json.JSONObject

object NotificationActionAdapter : JSONAdapterInterface<NotificationAction> {
    override fun fromJson(json: JSONObject): NotificationAction {
        val type = json.getString("_type")

        return when (type) {
            LaunchAppNotificationAction.ACTION_ID -> decodeLaunchAppNotificationAction(json)
            GoToUrlNotificationAction.ACTION_ID -> decodeGoToUrlNotificationAction(json)
            else -> LaunchAppNotificationAction(NotificationAction.Action.DEFAULT, "")
        }
    }

    override fun toJson(obj: NotificationAction): JSONObject {
        return JSONObject().apply {
            put(
                "_type",
                when (obj) {
                    is LaunchAppNotificationAction -> LaunchAppNotificationAction.ACTION_ID
                    is GoToUrlNotificationAction -> GoToUrlNotificationAction.ACTION_ID
                }
            )
            put("data", getEncodedAction(obj))
        }
    }

    private fun getEncodedAction(action: NotificationAction): JSONObject {
        return when (action) {
            is LaunchAppNotificationAction -> encodeLaunchAppNotificationAction(action)
            is GoToUrlNotificationAction -> encodeGoToUrlNotificationAction(action)
        }
    }

    private fun encodeLaunchAppNotificationAction(action: LaunchAppNotificationAction): JSONObject {
        return JSONObject().apply {
            put("title", action.actionTitle)
            put("type", action.action.jsonKeyName)
        }
    }

    private fun encodeGoToUrlNotificationAction(action: GoToUrlNotificationAction): JSONObject {
        return JSONObject().apply {
            put("title", action.actionTitle)
            put("type", action.action.jsonKeyName)
            put("url", action.uri.toString())
        }
    }

    private fun decodeLaunchAppNotificationAction(json: JSONObject): LaunchAppNotificationAction {
        val action = NotificationAction.Action.getValue(json.getString("type"))
        val title = json.getString("title")

        return LaunchAppNotificationAction(action, title)
    }

    private fun decodeGoToUrlNotificationAction(json: JSONObject): GoToUrlNotificationAction {
        val action = NotificationAction.Action.getValue(json.getString("type"))
        val title = json.getString("title")
        val url = Uri.parse(json.getString("url"))

        return GoToUrlNotificationAction(action, title, url)
    }

}