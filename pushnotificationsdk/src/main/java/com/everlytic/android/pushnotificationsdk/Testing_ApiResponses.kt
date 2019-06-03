package com.everlytic.android.pushnotificationsdk

import com.everlytic.android.pushnotificationsdk.models.ApiResponse
import com.everlytic.android.pushnotificationsdk.models.ApiSubscription
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.JSONAdapter
import org.json.JSONObject
import java.security.SecureRandom

internal object Testing_ApiResponses {

    private fun randId() = SecureRandom().nextInt(10000).toString()

    fun subscribeSuccess(deviceId: String): ApiResponse {
        val sub = ApiSubscription(
            randId(),
            randId(),
            randId(),
            randId(),
            deviceId
        )
        return ApiResponse("success", JSONAdapter.encode(sub))
    }

    fun unsubscribeSuccess(): ApiResponse {
        return ApiResponse("success", JSONObject())
    }

}