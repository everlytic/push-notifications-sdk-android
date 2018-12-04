package com.everlytic.android.pushnotificationsdk

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushInvalidSDKConfigurationException
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushNotInitialisedException
import io.mockk.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EverlyticPushTest {

    @Before
    fun setUp() {
        Mock.BuildFacade()
        Mock.FirebaseInstanceIdFacade();
    }

    @Test
    fun testInit_WithInvalidManifestSettings_ReturnsError() {

        val mockApp = mockk<Application> {

            val mPackageManager = mockk<PackageManager> {

                val applicationInfo = mockk<ApplicationInfo> {

                    metaData = mockk {
                        every { getString(eq("com.everlytic.api.API_INSTALL_URL")) } returns null
                        every { getString(eq("com.everlytic.api.API_USERNAME")) } returns null
                        every { getString(eq("com.everlytic.api.API_KEY")) } returns null
                        every { getInt(eq("com.everlytic.api.PUSH_NOTIFICATIONS_PROJECT_ID"), any()) } returns -1
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
            mockkConstructor(PushSdk::class)
            EverlyticPush.init(app)
            verify { app.packageManager }
        }
    }

    @Test
    fun testSubscribe_EverlyticPushNotInitialised_ReturnsError() {

        val everlyticPush = spyk<EverlyticPush> {
            instance = null
        }

        assertFailsWith<EverlyticPushNotInitialisedException> {
            everlyticPush.subscribe("test@test.com")
        }
    }

    @Test
    fun testSubscribe_WithSdkInitialised_IsSuccessful() {
        initialiseEverlyticPush()

        EverlyticPush.subscribe("test@test.com")

        coVerify(exactly = 1) { EverlyticPush.instance!!.subscribeUser(any()) }
    }

    @Test
    fun testSubscribe_WithOnCompleteCallback_ReturnsSuccess() {
        initialiseEverlyticPush()

        EverlyticPush.subscribe("test@test.com") {
            assertTrue { it.isSuccessful }
        }

        coVerify(exactly = 1) { EverlyticPush.instance!!.subscribeUser(any()) }
    }

    @Test
    fun testResubscribe_WithContactEmail_IsSuccessful() {
        initialiseEverlyticPush()

        EverlyticPush.resubscribe("test@test.com")

        coVerify { EverlyticPush.instance!!.resubscribeUser(any()) }
    }

    @Test
    fun testResubscribe_EverlyticPushNotInitialised_ReturnsError() {
        val everlyticPush = spyk<EverlyticPush> {
            this.instance = null
        }

        assertFailsWith<EverlyticPushNotInitialisedException> {
            everlyticPush.resubscribe("test@test.com")
        }
    }

    private fun initialiseEverlyticPush() {
        val mockPushSdk = mockPushSdk()
        spyk(EverlyticPush).instance = mockPushSdk
    }

    private fun mockApplicationInfo() = mockk<ApplicationInfo> {
        metaData = mockk {
            every { getString(eq("com.everlytic.api.API_INSTALL_URL")) } returns "install_id"
            every { getString(eq("com.everlytic.api.API_USERNAME")) } returns "api_username"
            every { getString(eq("com.everlytic.api.API_KEY")) } returns "api_key"
            every { getInt(eq("com.everlytic.api.PUSH_NOTIFICATIONS_PROJECT_ID"), any()) } returns 0
        }
    }

    private fun mockPackageManager() = mockk<PackageManager> {
        val applicationInfo = mockApplicationInfo()

        every { getApplicationInfo(any(), eq(PackageManager.GET_META_DATA)) } returns applicationInfo
    }

    private fun mockApplication() = mockk<Application> {
        val pm = mockPackageManager()
        val ctx = mockk<Context>().apply {
            val sharedPreferences = mockk<SharedPreferences>().apply {
                every { getString(any(), any()) } answers { "[val for] ${args.first()}" }
            }
            every { getSharedPreferences(any(), any()) } returns sharedPreferences
        }

        every { packageManager } returns pm
        every { packageName } returns "packageName"
        every { applicationContext } returns ctx
    }

    private fun mockPushSdk(): PushSdk {
        mockkConstructor(PushSdk::class)
        val mockPushSdk = mockk<PushSdk>()
        coEvery { mockPushSdk.subscribeUser(any()) } just Runs
        coEvery { mockPushSdk.resubscribeUser(any()) } just Runs
        return mockPushSdk
    }

}