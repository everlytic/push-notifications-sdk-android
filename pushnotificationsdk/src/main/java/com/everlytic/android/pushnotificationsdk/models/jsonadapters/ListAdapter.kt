package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import org.json.JSONArray

internal object ListAdapter {

    fun <T> fromJson(json: JSONArray, adapter: JSONAdapterInterface<T>? = null): List<T> {

        val list = mutableListOf<T>()

        for (index in 0 until json.length()){
            adapter?.let {
                val o = json.getJSONObject(index)
                list.add(adapter.fromJson(o))
            } ?: list.add(json.get(index) as T)
        }

        return list
    }

    fun <T> toJson(obj: List<T>, adapter: JSONAdapterInterface<T>? = null): JSONArray {
        return JSONArray().apply {
            adapter?.let {
                obj.forEach {
                    put(adapter.toJson(it))
                }
            } ?: obj.forEach { put(it) }
        }
    }

}