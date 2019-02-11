package com.everlytic.android.pushnotificationsdk.models

import android.os.Parcelable
import androidx.annotation.IntRange
import androidx.core.app.NotificationCompat
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
    val received_at: Date,
    val read_at: Date? = null,
    val dismissed_at: Date? = null
) : Parcelable

@Parcelize
data class NotificationAction(
    val actionType: NotificationAction.ActionType,
    val intentType: NotificationAction.IntentType,
    val parameters: Map<String, String>
) : Parcelable {

    enum class ActionType(val jsonKey: String) {
        DEFAULT("default"),
        PRIMARY("primary"),
        SECONDARY("secondary")
    }

    enum class IntentType {
        LAUNCH_APP,
        GOTO_URL
    }

    companion object {
        const val ACTION_PREFIX = "@"
        const val CUSTOM_PARAM_DELIMITER = "$"
    }
}