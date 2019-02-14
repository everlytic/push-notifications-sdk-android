package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import org.json.JSONObject

object MapAdapter {
    fun fromJson(json: JSONObject): Map<String, String> {

        val map = mutableMapOf<String, String>()

        for (k in json.keys()) {
            map[k] = json.get(k).toString()
        }

        return map
    }

    fun toJson(obj: Map<String, String>): JSONObject {
        return JSONObject().apply {
            obj.forEach { (key, value) ->
                put(key, value)
            }
        }
    }
}