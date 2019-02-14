package com.everlytic.android.pushnotificationsdk

import android.annotation.SuppressLint
import android.app.Application
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
    private var application: Application? = null

    /**
     * Initialises the Everlytic Push EvNotification SDK
     * */
    @JvmStatic
    @Throws(EverlyticPushInvalidSDKConfigurationException::class)
    fun init(application: Application) {

        this.application = application

        val settingsBag = SdkSettings.getSettings(application)

        val apiInstallUrl = settingsBag.apiInstall
        val apiUsername = settingsBag.apiUsername
        val apiKey = settingsBag.apiKey
        val pushProjectId = settingsBag.listId

        if (apiInstallUrl.isNullOrBlank()) {
            throw newInvalidSdkConfigurationException(META_API_INSTALL_URL)
        }

        if (apiUsername.isNullOrBlank()) {
            throw newInvalidSdkConfigurationException(META_API_USERNAME_PATH)
        }

        if (apiKey.isNullOrBlank()) {
            throw newInvalidSdkConfigurationException(META_API_KEY_PATH)
        }

        if (pushProjectId < 0) {
            throw newInvalidSdkConfigurationException(META_PUSH_PROJECT_ID)
        }

        instance = PushSdk(application.applicationContext, settingsBag)
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
    @Throws(EverlyticPushNotInitialisedException::class)
    fun subscribe(email: String, onComplete: ((EvResult) -> Unit)?) {
        instance?.let { sdk ->
            runOnBackgroundThread {
                sdk.subscribeContact(email) {
                    runOnMainThread {
                        onComplete?.invoke(it)
                    }
                }
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
        instance?.let { sdk ->
            runOnBackgroundThread {
                sdk.unsubscribeCurrentContact {
                    runOnMainThread {
                        onComplete?.invoke(it)
                    }
                }
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