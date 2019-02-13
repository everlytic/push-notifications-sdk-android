package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import org.json.JSONObject

interface JSONAdapterInterface<T> {

    fun fromJson(json: JSONObject): T
    fun toJson(obj: T): JSONObject

}