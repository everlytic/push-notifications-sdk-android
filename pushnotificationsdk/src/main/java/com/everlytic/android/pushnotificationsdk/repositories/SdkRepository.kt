package com.everlytic.android.pushnotificationsdk.repositories

import android.content.Context
import android.content.SharedPreferences
import com.everlytic.android.pushnotificationsdk.database.SharedPreferenceStore
import com.everlytic.android.pushnotificationsdk.models.ApiSubscription

internal class SdkRepository(private val context: Context) {

    private fun getPreferences() : SharedPreferences {
        return SharedPreferenceStore.getInstance(context)
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

    fun getSubscriptionId() : Long? {
        return getPreferences()
            .getLong(Keys.SubscriptionId.key, -1)
            .let { if (it > 0) it else null }
    }

    fun getContactId() : Long? {
        return getPreferences()
            .getLong(Keys.ContactId.key, -1)
            .let { if (it > 0) it else null }
    }

    fun setContactSubscription(apiSubscription: ApiSubscription) {
        edit {
            putLong(Keys.SubscriptionId.key, apiSubscription.pns_id.toLong())
            putLong(Keys.ContactId.key, apiSubscription.pns_contact_id.toLong())
        }
    }

    fun removeContactSubscription() {
        edit {
            remove(Keys.SubscriptionId.key)
            remove(Keys.ContactId.key)
        }
    }

    enum class Keys(val key: String) {
        DeviceId("device_id"),
        SubscriptionId("subscription_id"),
        ContactId("contact_id"),
        ContactEmail("contact_email"),
    }
}