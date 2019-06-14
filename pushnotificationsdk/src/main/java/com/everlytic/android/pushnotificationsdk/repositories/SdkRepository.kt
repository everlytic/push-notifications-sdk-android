package com.everlytic.android.pushnotificationsdk.repositories

import android.content.Context
import android.content.SharedPreferences
import com.everlytic.android.pushnotificationsdk.database.SharedPreferenceStore
import com.everlytic.android.pushnotificationsdk.database.adapters.toDate
import com.everlytic.android.pushnotificationsdk.database.adapters.toIso8601String
import com.everlytic.android.pushnotificationsdk.getHash
import com.everlytic.android.pushnotificationsdk.logd
import com.everlytic.android.pushnotificationsdk.models.ApiSubscription
import java.util.*

internal data class FcmToken(val token: String, val datetime: Date)

internal class SdkRepository(private val context: Context) {

    private val preferences: SharedPreferences
        get() {
            return SharedPreferenceStore.getInstance(context)
        }

    fun getHasSubscription(): Boolean {
        return getSubscriptionId() != null || getContactEmail() != null
    }

    fun getSubscriptionId(): Long? {
        return preferences
            .getLong(SUBSCRIPTION_ID, -1)
            .let { if (it > 0) it else null }
    }

    fun getSubscriptionDatetime(): Date? {
        return preferences
            .getString(SUBSCRIPTION_DATETIME, null)
            .let { if (it.isNullOrBlank()) null else it.toDate() }
    }

    fun getContactId(): Long? {
        return preferences
            .getLong(CONTACT_ID, -1)
            .let { if (it > 0) it else null }
    }

    fun getContactEmail(): String? {
        return preferences.getString(CONTACT_EMAIL, null)
    }

    fun getDeviceId(): String? {
        return preferences.getString(DEVICE_ID, null)
    }

    private fun edit(block: SharedPreferences.Editor.() -> Unit) {
        preferences.edit().apply(block).apply()
    }

    fun setDeviceId(id: String): String {
        logd("::setDeviceId() id=$id")
        edit {
            putString(DEVICE_ID, id)
        }

        return id
    }

    fun setContactSubscription(email: String, apiSubscription: ApiSubscription) {
        logd("::setContactSubscription($apiSubscription)")
        edit {
            putString(CONTACT_EMAIL, email)
            putLong(SUBSCRIPTION_ID, apiSubscription.pns_id.toLong())
            putLong(CONTACT_ID, apiSubscription.pns_contact_id.toLong())
            putString(SUBSCRIPTION_DATETIME, Date().toIso8601String())
        }
    }

    fun removeContactSubscription() {
        logd("::removeContactSubscription()")
        edit {
            remove(NEW_FCM_TOKEN_DATETIME)
            remove(SUBSCRIPTION_DATETIME)
            remove(FCM_TOKEN_HASH)
            remove(SUBSCRIPTION_ID)
            remove(CONTACT_EMAIL)
            remove(NEW_FCM_TOKEN)
            remove(CONTACT_ID)
        }
    }

    fun setNewFcmToken(token: String) {
        logd("::setNewFcmToken() token=$token")
        edit {
            putString(NEW_FCM_TOKEN, token)
            putString(FCM_TOKEN_HASH, token.getHash())
            putString(NEW_FCM_TOKEN_DATETIME, Date().toIso8601String())
        }
    }

    fun setContactEmail(email: String) {
        logd("::setContactEmail() email=$email")
        edit {
            putString(CONTACT_EMAIL, email)
        }
    }

    fun removeNewFcmToken() {
        logd("::removeNewFcmToken()")
        edit {
            remove(NEW_FCM_TOKEN)
            remove(NEW_FCM_TOKEN_DATETIME)
        }
    }

    fun getNewFcmToken(): FcmToken? {
        val token = preferences.getString(NEW_FCM_TOKEN, null)
        val datetime = preferences.getString(NEW_FCM_TOKEN_DATETIME, null)

        return if (!token.isNullOrBlank() && !datetime.isNullOrBlank()) {
            FcmToken(token, datetime.toDate())
        } else {
            null
        }
    }

    fun setFcmTokenHash(hash: String) {
        logd("::setFcmTokenHash() hash=$hash")
        edit {
            putString(FCM_TOKEN_HASH, hash)
        }
    }

    fun getFcmTokenHash(): String? {
        return preferences
            .getString(FCM_TOKEN_HASH, null)
    }

    companion object {
        const val DEVICE_ID = "device_id"
        const val SUBSCRIPTION_ID = "subscription_id"
        const val CONTACT_EMAIL = "contact_email"
        const val CONTACT_ID = "contact_id"
        const val SUBSCRIPTION_DATETIME = "subscription_datetime"
        const val FCM_TOKEN_HASH = "fcm_token_hash"
        const val NEW_FCM_TOKEN = "new_fcm_token"
        const val NEW_FCM_TOKEN_DATETIME = "new_fcm_token_datetime"
    }
}