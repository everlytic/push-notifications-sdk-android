package com.everlytic.android.pushnotificationsdk.network

import com.everlytic.android.pushnotificationsdk.models.SubscriptionEvent
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

internal interface EverlyticApi {
    @POST("/push-notifications/subscriptions")
    fun subscribe(@Body subscription: SubscriptionEvent) : Call<ResponseBody>

    @PUT("/push-notifications/subscriptions")
    fun updateSubscription(@Body subscription: SubscriptionEvent) : Call<ResponseBody>
}