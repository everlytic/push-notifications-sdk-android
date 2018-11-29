package com.everlytic.android.pushnotificationsdk

import android.app.Application
import android.content.pm.PackageManager
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushInvalidSDKConfigurationException
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushNotInitialisedException

/**
 * Everlytic Push Notifications SDK
 * */
public class EverlyticPush private constructor(apiUsername: String, apiKey: String, pushProjectId: String) {

    companion object {

        private const val TAG = "EverlyticPush"
        private const val META_API_USERNAME_PATH = "com.everlytic.api.API_USERNAME"
        private const val META_API_KEY_PATH = "com.everlytic.api.API_KEY"
        private const val META_PUSH_PROJECT_ID = "com.everlytic.api.PUSH_NOTIFICATIONS_PROJECT_ID"

        var instance: EverlyticPush? = null

        /**
         * Initialises the Everlytic Push Notification SDK
         * */
        @JvmStatic
        public fun init(application: Application) {
            val appInfo = application
                .packageManager
                .getApplicationInfo(application.packageName, PackageManager.GET_META_DATA)

            val apiUsername = appInfo.metaData.getString(META_API_USERNAME_PATH)
            val apiKey = appInfo.metaData.getString(META_API_KEY_PATH)
            val pushProjectId = appInfo.metaData.getString(META_PUSH_PROJECT_ID)

            if (apiUsername.isNullOrBlank()) {
                throw EverlyticPushInvalidSDKConfigurationException(
                    """
                        Missing or empty <meta-data android:name="$META_API_USERNAME_PATH"></meta-data>  value in your AndroidManifest.xml file.
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

            if (pushProjectId.isNullOrBlank()) {
                throw EverlyticPushInvalidSDKConfigurationException(
                    """
                        Missing or empty <meta-data android:name="$META_PUSH_PROJECT_ID"></meta-data> value in your AndroidManifest.xml file.
                        Please follow the SDK setup to configure this correctly
                    """.trimIndent()
                )
            }

            instance = EverlyticPush(apiUsername, apiKey, pushProjectId)
        }

        /**
         * Subscribes a contact email to Everlytic Push Notifications
         * */
        @JvmStatic
        public fun subscribe(email: String) {
            if (instance == null) {
                throw EverlyticPushNotInitialisedException(
                    """
                        EverlyticPush has not been initialised.
                        Please call EverlyticPush.init(Application) before calling EverlyticPush.subscribe().
                    """.trimIndent()
                )
            }
        }

        public fun resubscribe(email: String) {
            if (instance == null) {
                throw EverlyticPushNotInitialisedException(
                    """
                        EverlyticPush has not been initialised.
                        Please call EverlyticPush.init(Application) before calling EverlyticPush.resubscribe().
                    """.trimIndent()
                )
            }
        }
    }

}