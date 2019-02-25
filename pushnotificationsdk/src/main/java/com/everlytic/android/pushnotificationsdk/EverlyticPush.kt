package com.everlytic.android.pushnotificationsdk

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.everlytic.android.pushnotificationsdk.SdkSettings.META_API_INSTALL_URL
import com.everlytic.android.pushnotificationsdk.SdkSettings.META_API_KEY_PATH
import com.everlytic.android.pushnotificationsdk.SdkSettings.META_API_USERNAME_PATH
import com.everlytic.android.pushnotificationsdk.SdkSettings.META_PUSH_PROJECT_ID
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushInvalidSDKConfigurationException
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushNotInitialisedException

/**
 * Everlytic Push Notifications SDK
 * */
object EverlyticPush {
    private const val TAG = "EverlyticPush"

    @SuppressLint("StaticFieldLeak")
    internal var instance: PushSdk? = null

    /**
     * Initialises the Everlytic Push EvNotification SDK
     * @param context [Application] instance
     * */
    @JvmStatic
    @Throws(EverlyticPushInvalidSDKConfigurationException::class)
    fun init(context: Context) {
        logd("::init(); Initializing SDK")

        val settingsBag = SdkSettings.getSettings(context)

        val (apiInstallUrl, apiUsername, apiKey, pushListId) = settingsBag

        if (apiInstallUrl.isNullOrBlank()) {
            logd("SDK API Install URL is blank")
            throw newInvalidSdkConfigurationException(META_API_INSTALL_URL)
        }

        if (apiUsername.isNullOrBlank()) {
            logd("SDK API Username URL is blank")
            throw newInvalidSdkConfigurationException(META_API_USERNAME_PATH)
        }

        if (apiKey.isNullOrBlank()) {
            logd("SDK API Key URL is blank")
            throw newInvalidSdkConfigurationException(META_API_KEY_PATH)
        }

        if (pushListId < 0) {
            logd("SDK API List Id URL is blank")
            throw newInvalidSdkConfigurationException(META_PUSH_PROJECT_ID)
        }

        instance = PushSdk(context.applicationContext, settingsBag)
    }

    /**
     * Subscribes a contact email to Everlytic Push Notifications for the current device
     * @param email
     * @return [Unit]
     * */
    @JvmStatic
    fun subscribe(email: String) {
        EverlyticPush.subscribe(email, null)
    }

    /**
     * Subscribes a contact email to Everlytic Push Notifications for the current device
     *
     * @param email
     * @param onComplete Callback with result of attempted subscription
     * @return [Unit]
     * */
    @JvmStatic
    @JvmName("subscribeWithCallback")
    @Throws(EverlyticPushNotInitialisedException::class)
    fun subscribe(email: String, onComplete: ((EvResult) -> Unit)?) {
        logd("::subscribe(); email=$email; onComplete=$onComplete")
        instance?.let { sdk ->
            sdk.subscribeContact(email) {
                onComplete?.invoke(it)
            }
        } ?: throw newNotInitialisedException()
    }

    /**
     * Unsubscribes the current contact device from Everlytic Push Notifications
     * */
    @JvmStatic
    @JvmOverloads
    @Throws(EverlyticPushNotInitialisedException::class)
    fun unsubscribe(onComplete: ((EvResult) -> Unit)? = null) {
        logd("::unsubscribe()")
        instance?.let { sdk ->
            sdk.unsubscribeCurrentContact {
                onComplete?.invoke(it)
            }

        } ?: throw newNotInitialisedException()
    }

    /**
     * Returns true if a contact has already been subscribed on the device
     *
     * @return Boolean
     * */
    @JvmStatic
    fun isContactSubscribed(): Boolean {
        logd("::isContactSubscribed()")
        return instance?.isContactSubscribed() ?: false
    }

    /**
     * Returns true if the Everlytic Push SDK has been initialised
     *
     * @return Boolean
     * */
    @JvmStatic
    fun isInitialised(): Boolean = instance != null

    private fun newNotInitialisedException() =
        EverlyticPushNotInitialisedException(
            """
                EverlyticPush has not been initialised.
                Please call EverlyticPush.init(Application) in your Application class.
            """.trimIndent()
        )

    private fun newInvalidSdkConfigurationException(metadataName: String) =
        EverlyticPushInvalidSDKConfigurationException(
            """
                Missing or empty <meta-data android:name="$metadataName"></meta-data> value in your AndroidManifest.xml file.
                Please follow the SDK setup to configure this correctly
            """.trimIndent()
        )
}