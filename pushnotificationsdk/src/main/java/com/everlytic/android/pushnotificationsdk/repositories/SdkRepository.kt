package com.everlytic.android.pushnotificationsdk.repositories

import android.content.Context
import android.content.SharedPreferences
import com.everlytic.android.pushnotificationsdk.database.SharedPreferenceStore
import com.everlytic.android.pushnotificationsdk.logd
import com.everlytic.android.pushnotificationsdk.models.ApiSubscription

internal class SdkRepository(private val context: Context) {

    private fun getPreferences() : SharedPreferences {
        return SharedPreferenceStore.getInstance(context)
    }

    private fun edit(block: SharedPreferences.Editor.() -> Unit) {
        getPreferences().edit().apply(block).apply()
    }

    fun getDeviceId() : String? {
        return getPreferences().getString(DEVICE_ID, null)
    }

    fun setDeviceId(id: String) : String {
        logd("::setDeviceId($id)")
        edit {
            putString(DEVICE_ID, id)
        }

        return id
    }

    fun getSubscriptionId() : Long? {
        return getPreferences()
            .getLong(SUBSCRIPTION_ID, -1)
            .let { if (it > 0) it else null }
    }

    fun getContactId() : Long? {
        return getPreferences()
            .getLong(CONTACT_ID, -1)
            .let { if (it > 0) it else null }
    }

    fun setContactSubscription(apiSubscription: ApiSubscription) {
        logd("::setContactSubscription($apiSubscription)")
        edit {
            putLong(SUBSCRIPTION_ID, apiSubscription.pns_id.toLong())
            putLong(CONTACT_ID, apiSubscription.pns_contact_id.toLong())
        }
    }

    fun removeContactSubscription() {
        logd("::removeContactSubscription()")
        edit {
            remove(SUBSCRIPTION_ID)
            remove(CONTACT_ID)
            remove(DEVICE_ID)
        }
    }

    companion object{
        const val DEVICE_ID = "device_id"
        const val SUBSCRIPTION_ID = "subscription_id"
        const val CONTACT_ID = "contact_id"
    }
}