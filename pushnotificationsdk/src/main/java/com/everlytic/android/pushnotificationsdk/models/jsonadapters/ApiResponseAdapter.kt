package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.models.ApiResponse
import org.json.JSONObject

internal object ApiResponseAdapter : JSONAdapter<ApiResponse> {
    override fun toJson(obj: ApiResponse): String {
        return JSONObject()
            .put("result", obj.result)
            .put("data", obj.data)
            .toString()
    }

    override fun fromJson(json: JSONObject): ApiResponse {
        return ApiResponse(
            json.getString("result"),
            json.getJSONObject("data")
        )
    }
}