package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.getJSONObjectOrNull
import com.everlytic.android.pushnotificationsdk.models.ApiResponse
import org.json.JSONObject

internal object ApiResponseAdapter : JSONAdapterInterface<ApiResponse> {
    override fun toJson(obj: ApiResponse): JSONObject {
        return JSONObject()
            .put("status", obj.status)
            .put("data", obj.data)
    }

    override fun fromJson(json: JSONObject): ApiResponse {
        return ApiResponse(
            json.getString("status"),
            json.getJSONObjectOrNull("data") ?: JSONObject()
        )
    }


}