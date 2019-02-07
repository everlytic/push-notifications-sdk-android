package com.everlytic.android.pushnotificationsdk.network

import com.everlytic.android.pushnotificationsdk.models.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface EverlyticApi {
    @POST("push-notifications/subscriptions/subscribe")
    fun subscribe(@Body subscription: SubscriptionEvent): Call<ApiSubscriptionResponse>

    @POST("push-notifications/subscriptions/unsubscribe")
    fun unsubscribe(@Body unsubscribeEvent: UnsubscribeEvent): Call<ResponseBody>

    @POST("push-notifications/events/clicks")
    fun recordClickEvent(@Body event: NotificationEvent): Call<ResponseBody>

    @POST("push-notifications/events/deliveries")
    fun recordDeliveryEvent(@Body event: NotificationEvent): Call<ResponseBody>
}