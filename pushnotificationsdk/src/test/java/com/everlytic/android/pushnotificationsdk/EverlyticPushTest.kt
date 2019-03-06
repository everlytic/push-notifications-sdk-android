package com.everlytic.android.pushnotificationsdk

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushInvalidSDKConfigurationException
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticPushNotInitialisedException
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EverlyticPushTest {

    @Before
    fun setUp() {
        Mock.BuildFacade()
        Mock.FirebaseInstanceIdFacade()
        Mock.SdkSettings()
        Mock.EvLogger()
    }

    @After
    fun tearDown() {
        clearConstructorMockk(PushSdk::class)
        unmockkAll()
    }

    @Test
    fun testInit_WithInvalidManifestSettings_ReturnsError() {

        val mockApp = mockk<Application> {

            val mockApplicationInfo = mockk<ApplicationInfo>()

            every { SdkSettings.getSettings(ofType()) } returns SdkSettings.SdkSettingsBag(null, null, null, -1)

            every { applicationInfo } returns mockApplicationInfo
        }

        assertFailsWith<EverlyticPushInvalidSDKConfigurationException> {
            EverlyticPush.init(mockApp)
        }

        Mock.SdkSettings()
    }

    @Test
    fun testInit_WithApplication_CreatesInitialisedSDK() {
        mockApplication().let { app ->
            mockkConstructor(PushSdk::class)
            mockkConstructor(SdkRepository::class)

            every { anyConstructed<SdkRepository>().getNewFcmToken() } returns null

            EverlyticPush.init(app)
            verify { SdkSettings.getSettings(ofType()) }
            clearConstructorMockk(PushSdk::class)
            clearConstructorMockk(SdkRepository::class)
        }
    }

    @Test
    fun testSubscribe_EverlyticPushNotInitialised_ReturnsError() {

        val everlyticPush = spyk<EverlyticPush> {
            instance = null
        }

        assertFailsWith<EverlyticPushNotInitialisedException> {
            everlyticPush.subscribe("test@test.com", null)
        }
    }

    @Test
    fun testSubscribe_WithSdkInitialised_IsSuccessful() {
        initialiseEverlyticPush()

        EverlyticPush.subscribe("test@test.com", null)

        verify(exactly = 1) { EverlyticPush.instance!!.subscribeContact(any(), any()) }
    }

    @Test
    fun testSubscribe_WithOnCompleteCallback_ReturnsSuccess() {
        initialiseEverlyticPush()

        EverlyticPush.subscribe("test@test.com", OnResultReceiver {
            assertTrue { it.isSuccessful }
        })

        verify(exactly = 1) { EverlyticPush.instance!!.subscribeContact(any(), any()) }
    }

    @Test
    fun testUnsubscribe_EverlyticPushNotInitialised_ReturnsError() {
        val everlyticPush = spyk<EverlyticPush> {
            this.instance = null
        }

        assertFailsWith<EverlyticPushNotInitialisedException> {
            everlyticPush.unsubscribe(null)
        }
    }

    @Test
    fun testUnsubscribe_WithOnCompleteCallback_IsSuccessful() {
        initialiseEverlyticPush().apply {
            val slot = slot<(EvResult) -> Unit>()
            every { unsubscribeCurrentContact(capture(slot)) } answers { slot.captured.invoke(EvResult(true)) }
        }

        EverlyticPush.unsubscribe( OnResultReceiver {
            assertTrue { it.isSuccessful }
        })

        verify { EverlyticPush.instance!!.unsubscribeCurrentContact(any()) }
    }

    @Test
    fun testIsContactSubscribed_WithSubscribedContact_ReturnsTrue() {
        val mockPushSdk = mockPushSdk().apply {
            every { isContactSubscribed() } returns true
        }

        val everlyticPush = spyk<EverlyticPush> {
            this.instance = mockPushSdk
        }

        assertTrue { everlyticPush.isContactSubscribed() }
        verify { mockPushSdk.isContactSubscribed() }
    }

    @Test
    fun testIsContactSubscribed_NoContact_ReturnsFalse() {
        val mockPushSdk = mockPushSdk().apply {
            every { isContactSubscribed() } returns false
        }

        val everlyticPush = spyk<EverlyticPush> {
            this.instance = mockPushSdk
        }

        assertFalse { everlyticPush.isContactSubscribed() }
        verify { mockPushSdk.isContactSubscribed() }
    }

    private fun initialiseEverlyticPush(): PushSdk {
        val mockPushSdk = mockPushSdk()
        spyk(EverlyticPush).instance = mockPushSdk
        return mockPushSdk
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
        val appInfo = mockApplicationInfo()
        val ctx = mockk<Context>().apply {
            val sharedPreferences = mockk<SharedPreferences>().apply {
                every { getString(any(), any()) } answers { "[val for] ${args.first()}" }
                every { getLong(any(), any()) } answers { 1 }
            }
            every { getSharedPreferences(any(), any()) } returns sharedPreferences
        }

        every { applicationInfo } returns appInfo
        every { packageManager } returns pm
        every { packageName } returns "packageName"
        every { applicationContext } returns ctx
    }

    private fun mockPushSdk(): PushSdk {
        mockkConstructor(PushSdk::class)
        return mockk {
            every { subscribeContact(any(), any()) } just Runs
            every { unsubscribeCurrentContact() } just Runs
        }
    }

}