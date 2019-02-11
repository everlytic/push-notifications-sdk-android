package com.everlytic.android.pushnotificationsdk

import com.everlytic.android.pushnotificationsdk.models.jsonadapters.MapAdapter
import org.json.JSONObject

@Suppress("UNCHECKED_CAST")
internal fun decodeJsonMap(data: String): Map<String, String> {
    return MapAdapter.fromJson(JSONObject(data))
}