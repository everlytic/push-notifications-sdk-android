package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import org.json.JSONObject

interface JSONAdapter<T> {

    fun fromJson(json: JSONObject) : T
    fun toJson(obj: T): String

}