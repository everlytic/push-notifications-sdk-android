package com.everlytic.android.pushnotificationsdk.models

import android.os.Parcelable
import androidx.annotation.IntRange
import androidx.core.app.NotificationCompat
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EvNotification(
    @Json(name = "message_id")
    val messageId: Long,
    val androidNotificationId: Long,
    val title: String,
    val body: String,
    @Json(name = "use_sound")
    val useSound: Boolean,
    val color: Int,
    val icon: Int,
    @IntRange(from = NotificationCompat.PRIORITY_MIN.toLong(), to = NotificationCompat.PRIORITY_HIGH.toLong())
    val priority: Int,
    val actions: List<NotificationAction>
) : Parcelable

@Parcelize
data class NotificationAction(
    val actionType: NotificationAction.ActionType,
    val intentType: NotificationAction.IntentType,
    val parameters: Map<String, String>
) : Parcelable {

    enum class ActionType {
        DEFAULT,
        PRIMARY,
        SECONDARY
    }

    enum class IntentType {
        LAUNCH_APP,
        GOTO_URL
    }
}