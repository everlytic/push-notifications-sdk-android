package com.everlytic.android.pushnotificationsdk.network

import com.everlytic.android.pushnotificationsdk.models.SubscriptionEvent
import com.everlytic.android.pushnotificationsdk.models.UnsubscribeEvent
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

internal interface EverlyticApi {
    @POST("push-notifications/subscriptions/subscribe")
    fun subscribe(@Body subscription: SubscriptionEvent): Call<ResponseBody>

    @POST("push-notifications/subscriptions/unsubscribe")
    fun unsubscribe(@Body unsubscribeEvent: UnsubscribeEvent): Call<ResponseBody>
}