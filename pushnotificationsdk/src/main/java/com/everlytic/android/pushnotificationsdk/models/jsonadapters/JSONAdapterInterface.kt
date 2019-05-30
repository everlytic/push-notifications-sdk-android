package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import org.json.JSONObject

internal interface JSONAdapterInterface<T> {

    fun fromJson(json: JSONObject): T
    fun toJson(obj: T): JSONObject

}