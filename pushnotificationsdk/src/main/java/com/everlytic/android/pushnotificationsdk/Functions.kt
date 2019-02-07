package com.everlytic.android.pushnotificationsdk

import com.squareup.moshi.Moshi

@Suppress("UNCHECKED_CAST")
internal fun decodeJsonMap(data: String): Map<String, String> {
    return Moshi
        .Builder()
        .build()
        .adapter(Map::class.java)
        .let {
            it.fromJson(data) as Map<String, String>
        }
}