package com.everlytic.android.pushnotificationsdk

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.annotation.DrawableRes
import android.support.annotation.IntRange
import android.util.Log
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushInvalidSDKConfigurationException
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushNotInitialisedException
import com.everlytic.android.pushnotificationsdk.models.EverlyticNotification
import java.lang.IllegalArgumentException

/**
 * Everlytic Push Notifications SDK
 * */
@Suppress("unused")
public object EverlyticPush {
    @SuppressLint("StaticFieldLeak")
    internal var instance: PushSdk? = null
    internal var sdkSettingsBag: SdkConfiguration.SdkConfigBag? = null

    var isInTestMode: Boolean = false
        private set

    internal var throwExceptions: Boolean = false

    internal var logLevel = Log.WARN

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
            logd("config string=$config")
            sdkSettingsBag = if (config != null) {
                logd("Using provided config string")
                SdkConfiguration.getConfigurationBag(config)
            } else {
                logd("Using config from manifest")
                SdkConfiguration.getConfigurationBag(context)
            }
            logd("Creating SDK Instance...")
            instance = PushSdk(context.applicationContext, sdkSettingsBag!!, testMode = isInTestMode)
        } catch (e: IllegalArgumentException) {
            loge("Encountered an init error", e)
            throw newInvalidSdkConfigurationException(SdkConfiguration.META_SDK_CONFIGURATION_STRING)
        } catch (e: Exception) {
            e.handle {
                loge("Encountered an init error", it)
            }
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
            sdk.subscribeContact(null, email) {
                onComplete?.onResult(it)
            }
        } ?: throw newNotInitialisedException()
    }

    @JvmStatic
    fun subscribeWithUniqueId(uniqueId: String) {
        EverlyticPush.subscribeWithUniqueId(uniqueId, null, null)
    }

    @JvmStatic
    fun subscribeWithUniqueId(uniqueId: String, onComplete: OnResultReceiver?) {
        EverlyticPush.subscribeWithUniqueId(uniqueId, null, onComplete)
    }

    @JvmStatic
    fun subscribeWithUniqueId(uniqueId: String, email: String?, onComplete: OnResultReceiver?) {
        logd("::subscribeWithUniqueId(); uniqueId=$uniqueId email=$email; onComplete=$onComplete")
        instance?.let { sdk ->
            sdk.subscribeContact(uniqueId, email) {
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
     * Retrieve the number of notifications in the device history
     * @since 0.0.8-alpha
     * @return [Int]
     * */
    @JvmStatic
    fun getNotificationHistoryCount(): Int {
        logd("::getNotificationHistoryCount()")
        return instance?.getNotificationHistoryCount() ?: throw newNotInitialisedException()
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

    /**
     * Set if the SDK should prefer to throw exceptions or handle them by logging.
     *
     * This does not affect the throwing of exceptions in the case of SDK misconfiguration
     * @param throws
     * @return [EverlyticPush]
     * */
    @JvmStatic
    fun setThrowExceptions(throws: Boolean): EverlyticPush {
        throwExceptions = throws
        return this
    }

    /**
     * Set the logging level of the SDK. Defaults to [Log.WARN]
     *
     * @param level The log level, as specified by the [Log] class
     * @return [EverlyticPush]
     * */
    @JvmStatic
    fun setLogLevel(@IntRange(from = 2, to = 7) level: Int): EverlyticPush {
        EvLogger.logLevel = level
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
