package com.everlytic.android.pushnotificationsdk

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushInvalidSDKConfigurationException
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushNotInitialisedException
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MokkIoEverlyticPushTest {

    @Test
    fun testInit_WithInvalidManifestSettings_ReturnsError() {

        val mockApp = mockk<Application> {

            val mPackageManager = mockk<PackageManager> {

                val applicationInfo = mockk<ApplicationInfo> {

                    metaData = mockk {
                        every { getString(eq("com.everlytic.api.API_USERNAME")) } returns null
                        every { getString(eq("com.everlytic.api.API_KEY")) } returns null
                        every { getString(eq("com.everlytic.api.PUSH_NOTIFICATIONS_PROJECT_ID")) } returns null
                    }
                }

                every { getApplicationInfo(any(), any()) } returns applicationInfo
            }

            every { packageManager } returns mPackageManager
            every { packageName } returns "test_package"
        }

        assertFailsWith<EverlyticPushInvalidSDKConfigurationException> {
            EverlyticPush.init(mockApp)
        }
    }

    @Test
    fun testInit_WithApplication_CreatesInitialisedSDK() {
        mockApplication().let { app ->
            EverlyticPush.init(app)
            verify { app.packageManager }
        }

    }

    @Test
    fun testSubscribe_WithValidContactEmail_IsSuccessful() {
        EverlyticPush.init(mockApplication())
        EverlyticPush.subscribe("test@test.com")
    }

    @Test
    fun testSubscribe_WithInvalidContactEmail_ReturnsError() {
        EverlyticPush.init(mockApplication())
        EverlyticPush.subscribe("test@")
    }

    @Test
    fun testSubscribe_EverlyticPushNotInitialised_ReturnsError() {

        val everlyticPush = spyk<EverlyticPush.Companion> {
            this.instance = null
        }

        assertFailsWith<EverlyticPushNotInitialisedException> {
            everlyticPush.subscribe("test@test.com")
        }
    }

    @Test
    fun testResubscribe_WithValidContactEmail_IsSuccessful() {
        EverlyticPush.init(mockApplication())
        EverlyticPush.resubscribe("test@test.com")
    }

    @Test
    fun testResubscribe_WithInvalidContactEmail_ReturnsError() {
        EverlyticPush.init(mockApplication())
        EverlyticPush.resubscribe("test@")
    }

    @Test
    fun testResubscribe_EverlyticPushNotInitialised_ReturnsError() {
        val everlyticPush = spyk<EverlyticPush.Companion> {
            this.instance = null
        }

        assertFailsWith<EverlyticPushNotInitialisedException> {
            everlyticPush.subscribe("test@test.com")
        }
    }

    private fun mockApplicationInfo() = mockk<ApplicationInfo> {
        metaData = mockk {
            every { getString(eq("com.everlytic.api.API_USERNAME")) } returns "api_username"
            every { getString(eq("com.everlytic.api.API_KEY")) } returns "api_key"
            every { getString(eq("com.everlytic.api.PUSH_NOTIFICATIONS_PROJECT_ID")) } returns "push_project_id"
        }
    }

    private fun mockPackageManager() = mockk<PackageManager> {
        val applicationInfo = mockApplicationInfo()

        every { getApplicationInfo(any(), eq(PackageManager.GET_META_DATA)) } returns applicationInfo
    }

    private fun mockApplication() = mockk<Application> {
        val pm = mockPackageManager()

        every { packageManager } returns pm
        every { packageName } returns "packageName"
    }

}