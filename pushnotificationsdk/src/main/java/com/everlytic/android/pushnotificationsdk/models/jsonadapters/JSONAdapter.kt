package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.models.*
import org.json.JSONObject
import kotlin.reflect.KClass

internal object JSONAdapter {

//    inline fun <reified T> getAdapterInstance(): JSONAdapterInterface<T> {
//        return when (T) {
//            is ApiResponse -> ApiResponseAdapter
//        }
//    }

    fun <T> encode(obj: T): JSONObject {
        return when (obj) {
            is ApiResponse -> ApiResponseAdapter.toJson(obj)
            is ApiSubscription -> ApiSubscriptionAdapter.toJson(obj)
            is EvNotification -> EvNotificationAdapter.toJson(obj)
            is NotificationEvent -> NotificationEventAdapter.toJson(obj)
            is SubscriptionEvent -> SubscriptionEventAdapter.toJson(obj)
            is UnsubscribeEvent -> UnsubscribeEventAdapter.toJson(obj)
            else -> throw NotImplementedError("JSONAdapterInterface not implemented for object type")
        }
    }
    fun <T> encodeAsString(obj: T): String {
        return encode(obj).toString()
    }

    fun <T> decodeAs(clazz: Class<T>, value: JSONObject): T {
        return when (clazz) {
            ApiResponse::class.java -> ApiResponseAdapter.fromJson(value)
            ApiSubscription::class.java -> ApiSubscriptionAdapter.fromJson(value)
            EvNotification::class.java -> EvNotificationAdapter.fromJson(value)
            NotificationEvent::class.java -> NotificationEventAdapter.fromJson(value)
            SubscriptionEvent::class.java -> SubscriptionEventAdapter.fromJson(value)
            UnsubscribeEvent::class.java -> UnsubscribeEventAdapter.fromJson(value)
            else -> throw NotImplementedError("JSONAdapterInterface not implemented for object type")
        } as T
    }

}