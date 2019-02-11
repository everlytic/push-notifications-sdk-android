package com.everlytic.android.pushnotificationsdk.models.jsonadapters

object JsonDeserializer {

    inline fun <reified T> deserializeAs(json: String): T? {
        return when (T::class) {
            else -> null
        }
    }
}