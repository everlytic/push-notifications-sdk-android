package com.everlytic.android.pushnotificationsdk.network

import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.models.SubscriptionEvent
import com.everlytic.android.pushnotificationsdk.models.UnsubscribeEvent
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.JSONAdapter

internal class EverlyticApi(val http: EverlyticHttp) {

    fun subscribe(subscription: SubscriptionEvent, responseHandler: EverlyticHttp.ResponseHandler) {
        http.post(
            "push-notifications/subscriptions/subscribe",
            JSONAdapter.encodeAsString(subscription),
            responseHandler
        )
    }

    fun unsubscribe(unsubscribeEvent: UnsubscribeEvent, responseHandler: EverlyticHttp.ResponseHandler) {
        http.post(
            "push-notifications/subscriptions/unsubscribe",
            JSONAdapter.encodeAsString(unsubscribeEvent),
            responseHandler
        )
    }

    fun recordClickEvent(event: NotificationEvent, responseHandler: EverlyticHttp.ResponseHandler) {
        http.post("push-notifications/events/clicks", JSONAdapter.encodeAsString(event), responseHandler)
    }

    fun recordDeliveryEvent(event: NotificationEvent, responseHandler: EverlyticHttp.ResponseHandler) {
        http.post("push-notifications/events/deliveries", JSONAdapter.encodeAsString(event), responseHandler)
    }

    fun recordDismissEvent(event: NotificationEvent, responseHandler: EverlyticHttp.ResponseHandler) {
        http.post("push-notifications/events/dismissals", JSONAdapter.encodeAsString(event), responseHandler)
    }
}