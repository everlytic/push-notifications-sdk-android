package com.everlytic.android.pushnotificationsdk

import android.util.Log
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.MapAdapter
import org.json.JSONObject
import java.net.HttpURLConnection
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal fun decodeJsonMap(data: String): Map<String, String> {
    return MapAdapter.fromJson(JSONObject(data))
}

internal inline fun HttpURLConnection.use(block: HttpURLConnection.() -> Unit) {
    try {
        this.block()
    } finally {
        this.disconnect()
    }
}

internal fun logd(message: String) {
    Log.d("Default tag", message)
}