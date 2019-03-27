package com.everlytic.android.pushnotificationsdk.models

import android.net.Uri
import android.os.Parcelable
import android.support.annotation.IntRange
import android.support.v4.app.NotificationCompat
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class EvNotification(
    val messageId: Long,
    val androidNotificationId: Int,
    val title: String,
    val body: String?,
    val useSound: Boolean,
    val color: Int,
    val icon: Int,
    @IntRange(from = NotificationCompat.PRIORITY_MIN.toLong(), to = NotificationCompat.PRIORITY_HIGH.toLong())
    val priority: Int,
    val actions: List<NotificationAction>,
    val customParameters: Map<String, String>,
    val received_at: Date,
    val read_at: Date? = null,
    val dismissed_at: Date? = null
) : Parcelable {
    companion object {
        const val ACTION_PREFIX = "@"
        const val CUSTOM_PARAM_DELIMITER = "$"
    }
}

sealed class NotificationAction(val action: Action, val actionTitle: String) : Parcelable {
    enum class Action(val jsonKeyName: String) {
        DEFAULT("default"),
        PRIMARY("primary"),
        SECONDARY("secondary");

        companion object {
            fun getValue(value: String): Action {
                return values().first { it.jsonKeyName == value }
            }
        }
    }

    companion object {
        const val ACTION_ID_DELIMITER = "="
    }
}

@Parcelize
data class LaunchAppNotificationAction(
    private val _action: NotificationAction.Action,
    private val title: String
) : NotificationAction(_action, title) {
    companion object {
        const val ACTION_ID = "launch"
    }
}

@Parcelize
data class GoToUrlNotificationAction(
    private val _action: Action,
    private val title: String,
    val uri: Uri
) : NotificationAction(_action, title) {
    companion object {
        const val ACTION_ID = "url"
    }
}