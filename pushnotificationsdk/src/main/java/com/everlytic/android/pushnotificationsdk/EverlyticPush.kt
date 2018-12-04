package com.everlytic.android.pushnotificationsdk

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushInvalidSDKConfigurationException
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushNotInitialisedException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Everlytic Push Notifications SDK
 * */
object EverlyticPush {
    private const val TAG = "EverlyticPush"
    private const val META_API_USERNAME_PATH = "com.everlytic.api.API_USERNAME"
    private const val META_API_KEY_PATH = "com.everlytic.api.API_KEY"
    private const val META_PUSH_PROJECT_ID = "com.everlytic.api.PUSH_NOTIFICATIONS_PROJECT_ID"
    private const val META_API_INSTALL_URL = "com.everlytic.api.API_INSTALL_URL"

    @SuppressLint("StaticFieldLeak")
    internal var instance: PushSdk? = null
    internal var application: Application? = null

    /**
     * Initialises the Everlytic Push Notification SDK
     * */
    @JvmStatic
    @Throws(EverlyticPushInvalidSDKConfigurationException::class)
    fun init(application: Application) {

        this.application = application

        val appInfo = application
            .packageManager
            .getApplicationInfo(application.packageName, PackageManager.GET_META_DATA)

        val apiInstallUrl = appInfo.metaData.getString(META_API_INSTALL_URL)
        val apiUsername = appInfo.metaData.getString(META_API_USERNAME_PATH)
        val apiKey = appInfo.metaData.getString(META_API_KEY_PATH)
        val pushProjectId = appInfo.metaData.getInt(META_PUSH_PROJECT_ID, -1)

        if (apiInstallUrl.isNullOrBlank()) {
            throw EverlyticPushInvalidSDKConfigurationException(
                """
                    Missing or empty <meta-data android:name="$META_API_INSTALL_URL"></meta-data> value in your AndroidManifest.xml file.
                    Please follow the SDK setup to configure this correctly
                """.trimIndent()
            )
        }

        if (apiUsername.isNullOrBlank()) {
            throw EverlyticPushInvalidSDKConfigurationException(
                """
                    Missing or empty <meta-data android:name="$META_API_USERNAME_PATH"></meta-data> value in your AndroidManifest.xml file.
                    Please follow the SDK setup to configure this correctly
                """.trimIndent()
            )
        }

        if (apiKey.isNullOrBlank()) {
            throw EverlyticPushInvalidSDKConfigurationException(
                """
                    Missing or empty <meta-data android:name="$META_API_KEY_PATH"></meta-data> value in your AndroidManifest.xml file.
                    Please follow the SDK setup to configure this correctly
                """.trimIndent()
            )
        }

        if (pushProjectId < 0) {
            throw EverlyticPushInvalidSDKConfigurationException(
                """
                    Missing or empty <meta-data android:name="$META_PUSH_PROJECT_ID"></meta-data> value in your AndroidManifest.xml file.
                    Please follow the SDK setup to configure this correctly
                """.trimIndent()
            )
        }

        instance = PushSdk(application.applicationContext, apiInstallUrl, apiUsername, apiKey, "$pushProjectId")
    }

    /**
     * Subscribes a contact email to Everlytic Push Notifications
     * */
    @JvmStatic
    @Throws(EverlyticPushNotInitialisedException::class)
    fun subscribe(email: String, onComplete: ((EvResult) -> Unit)? = null) {
        instance?.let { sdk ->
            GlobalScope.launch {
                try {
                    sdk.subscribeUser(email)
                    onComplete?.invoke(EvResult(true))
                } catch (exception: Exception) {
                    onComplete?.invoke(EvResult(false, exception))
                    exception.printStackTrace()
                }
            }
        } ?: throw EverlyticPushNotInitialisedException(
            """
                EverlyticPush has not been initialised.
                Please call EverlyticPush.init(Application) before calling EverlyticPush.subscribe().
            """.trimIndent()
        )
    }

    @JvmStatic
    @Throws(EverlyticPushNotInitialisedException::class)
    fun resubscribe(email: String, onComplete: (() -> Unit)? = null) {
        instance?.let { sdk ->
            GlobalScope.launch {
                sdk.resubscribeUser(email)
            }
        } ?: throw EverlyticPushNotInitialisedException(
            """
                EverlyticPush has not been initialised.
                Please call EverlyticPush.init(Application) before calling EverlyticPush.subscribe().
            """.trimIndent()
        )
    }
}