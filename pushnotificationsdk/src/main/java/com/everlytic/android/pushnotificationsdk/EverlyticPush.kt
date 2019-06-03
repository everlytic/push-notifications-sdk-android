package com.everlytic.android.pushnotificationsdk

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushInvalidSDKConfigurationException
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushNotInitialisedException
import com.everlytic.android.pushnotificationsdk.models.EverlyticNotification

/**
 * Everlytic Push Notifications SDK
 * */
@Suppress("unused")
public object EverlyticPush {
    @SuppressLint("StaticFieldLeak")
    internal var instance: PushSdk? = null
    internal lateinit var sdkSettingsBag: SdkSettings.SdkSettingsBag

    var isInTestMode: Boolean = false
        private set

    /**
     * Initialises the Everlytic Push EvNotification SDK
     * @since 0.0.1-alpha
     * @param context [Application] instance
     * */
    @JvmStatic
    @Throws(EverlyticPushInvalidSDKConfigurationException::class)
    fun init(context: Context) {
        init(context, null)
    }

    /**
     * Initialises the Everlytic Push EvNotification SDK
     * @since 0.0.6-alpha
     * @param context [Application] instance
     * */
    @JvmStatic
    @Throws(EverlyticPushInvalidSDKConfigurationException::class)
    fun init(context: Context, config: String?) {
        logd("::init(); Initializing SDK")
        try {
            sdkSettingsBag = config?.let { SdkSettings.getSettings(it) } ?: SdkSettings.getSettings(context)
            instance = PushSdk(context.applicationContext, sdkSettingsBag, testMode = isInTestMode)
        } catch (e: Exception) {
            throw newInvalidSdkConfigurationException(SdkSettings.META_SDK_CONFIGURATION_STRING)
        }
    }

    /**
     * Subscribes a contact email to Everlytic Push Notifications for the current device
     * @since 0.0.1-alpha
     * @param email
     * @return [Unit]
     * */
    @JvmStatic
    @Throws(EverlyticPushNotInitialisedException::class)
    fun subscribe(email: String) {
        EverlyticPush.subscribe(email, null)
    }

    /**
     * Subscribes a contact email to Everlytic Push Notifications for the current device
     * @since 0.0.1-alpha
     * @param email
     * @param onComplete Callback with status of attempted subscription
     * @return [Unit]
     * */
    @JvmStatic
    @Throws(EverlyticPushNotInitialisedException::class)
    fun subscribe(email: String, onComplete: OnResultReceiver?) {
        logd("::subscribe(); email=$email; onComplete=$onComplete")
        instance?.let { sdk ->
            sdk.subscribeContact(email) {
                onComplete?.onResult(it)
            }
        } ?: throw newNotInitialisedException()
    }

    /**
     * Unsubscribes the current contact device from Everlytic Push Notifications
     * @throws [EverlyticPushNotInitialisedException]
     * */
    @JvmStatic
    @Throws(EverlyticPushNotInitialisedException::class)
    fun unsubscribe() {
        EverlyticPush.unsubscribe(null)
    }

    /**
     * Unsubscribes the current contact device from Everlytic Push Notifications
     * @throws [EverlyticPushNotInitialisedException]
     * */
    @JvmStatic
//    @JvmOverloads
    @Throws(EverlyticPushNotInitialisedException::class)
    fun unsubscribe(onComplete: OnResultReceiver?) {
        logd("::unsubscribe()")
        instance?.let { sdk ->
            sdk.unsubscribeCurrentContact {
                onComplete?.onResult(it)
            }

        } ?: throw newNotInitialisedException()
    }

    /**
     * Returns true if a contact has already been subscribed on the device
     *
     * @return [Boolean]
     * */
    @JvmStatic
    fun isContactSubscribed(): Boolean {
        logd("::isContactSubscribed()")
        return instance?.isContactSubscribed() ?: false
    }

    /**
     * Returns true if the Everlytic Push SDK has been initialised
     *
     * @return [Boolean]
     * */
    @JvmStatic
    fun isInitialised(): Boolean = instance != null

    /**
     * Retrieve a [List] of [EverlyticNotification] objects and return results in a callback
     *
     * @return [List]
     * */
    @JvmStatic
    fun getNotificationHistory(listener: OnNotificationHistoryResultListener) {
        logd("::getNotificationHistory()")
        return instance?.let { sdk ->
            Thread {
                sdk.getPublicNotificationHistory().let { notifications ->
                    Handler(Looper.getMainLooper()).post {
                        listener.onResult(notifications)
                    }
                }
            }.start()
        } ?: throw newNotInitialisedException()
    }

    /**
     * Set the SDK to test mode
     * @param mode
     * @return [EverlyticPush]
     * */
    @JvmStatic
    fun setInTestMode(mode: Boolean): EverlyticPush {
        isInTestMode = mode
        return this
    }

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
                Missing, empty or invalid <meta-data android:name="$metadataName"></meta-data> value in your AndroidManifest.xml file.
                Please follow the SDK setup to configure this correctly
            """.trimIndent()
        )
}