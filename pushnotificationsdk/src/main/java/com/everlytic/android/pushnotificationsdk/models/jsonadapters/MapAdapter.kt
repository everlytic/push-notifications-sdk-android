package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import org.json.JSONObject

object MapAdapter : JSONAdapter<Map<String, String>> {
    override fun fromJson(json: JSONObject): Map<String, String> {

        val map = mutableMapOf<String, String>()

        for (k in json.keys()) {
            map[k] = json.get(k).toString()
        }

        return map
    }

    override fun toJson(obj: Map<String, String>): String {
        return JSONObject().apply {
            obj.forEach { (key, value) ->
                put(key, value)
            }
        }.toString()
    }
}