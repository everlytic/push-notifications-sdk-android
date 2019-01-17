package com.everlytic.android.pushnotificationsdk.repositories

import android.content.Context
import android.content.SharedPreferences
import com.everlytic.android.pushnotificationsdk.models.ApiSubscription
import java.util.*

internal class SdkRepository(val context: Context) {

    private fun getPreferences() : SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private fun edit(block: SharedPreferences.Editor.() -> Unit) {
        getPreferences().edit().apply(block).apply()
    }

    fun getDeviceId() : String? {
        return getPreferences().getString(Keys.DeviceId.key, null)
    }

    fun setDeviceId(id: String) : String {
        edit {
            putString(Keys.DeviceId.key, id)
        }

        return id
    }

    fun getSubscriptionId() : Int? {
        return getPreferences()
            .getInt(Keys.SubscriptionId.key, -1)
            .let { if (it > 0) it else null }
    }

    fun setContactSubscription(apiSubscription: ApiSubscription) {
        edit {
            putInt(Keys.SubscriptionId.key, apiSubscription.pns_id)
            putInt(Keys.ContactId.key, apiSubscription.pns_contact_id)
        }
    }

    fun removeContactSubscription() {
        edit {
            remove(Keys.SubscriptionId.key)
            remove(Keys.ContactId.key)
        }
    }

    companion object {
        const val PREFERENCES_NAME = "ev_push_notifications_kv_store"
    }

    enum class Keys(val key: String) {
        DeviceId("device_id"),
        SubscriptionId("subscription_id"),
        ContactId("contact_id"),
        ContactEmail("contact_email"),
    }
}