package com.everlytic.android.pushnotificationsdk

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.MapAdapter
import org.json.JSONObject
import java.net.HttpURLConnection

@Suppress("UNCHECKED_CAST")
internal fun decodeJsonMap(data: String): Map<String, String> {
    return decodeJsonMap(JSONObject(data))
}

@Suppress("UNCHECKED_CAST")
internal fun decodeJsonMap(data: JSONObject): Map<String, String> {
    return MapAdapter.fromJson(data)
}

@Suppress("UNCHECKED_CAST")
internal fun encodeJsonMap(map: Map<String, String>): JSONObject {
    return MapAdapter.toJson(map)
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

fun runOnMainThread(block: () -> Unit) {
    Handler(Looper.getMainLooper()).post(block)
}

fun runOnBackgroundThread(block: () -> Unit) {
    Thread(block).start()
}