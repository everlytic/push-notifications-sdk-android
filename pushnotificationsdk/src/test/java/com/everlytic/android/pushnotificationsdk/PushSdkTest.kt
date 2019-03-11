package com.everlytic.android.pushnotificationsdk

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import com.everlytic.android.pushnotificationsdk.facades.FirebaseInstanceIdFacade
import com.everlytic.android.pushnotificationsdk.facades.TokenResult
import com.everlytic.android.pushnotificationsdk.models.ApiResponse
import com.everlytic.android.pushnotificationsdk.models.ApiSubscription
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.ApiSubscriptionAdapter
import com.everlytic.android.pushnotificationsdk.network.EverlyticApi
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.everlytic.android.pushnotificationsdk.repositories.FcmToken
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.reflect.jvm.internal.impl.utils.FunctionsKt
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PushSdkTest {

    @Before
    fun setUp() {
        Mock.BuildFacade()
        Mock.EvLogger()

        mockkStatic("com.everlytic.android.pushnotificationsdk.Functions")
        every { isDeviceOnline(any()) } returns true
    }

    @Test
    @MockK(relaxed = true)
    fun testSubscribe_RequestSucceeds_ReturnsSuccess() {


        val mockEverlyticApi = mockk<EverlyticApi>(relaxed = true) {
            val sl = slot<EverlyticHttp.ResponseHandler>()
            every { subscribe(any(), capture(sl)) } answers {

                val sub = ApiSubscription(
                    "1",
                    "2",
                    "3",
                    "4",
                    "213-456-789"
                )
                val json = JSONObject()
                    .put("subscription", ApiSubscriptionAdapter.toJson(sub))
                val apiResponse = ApiResponse("success", json)
                sl.captured.onSuccess(apiResponse)
            }
        }

        val mockContext = mockk<Context> {
            val mockResources = mockk<Resources> {
                every { getBoolean(R.bool.isTablet) } returns false
            }
            every { resources } returns mockResources
            every { registerReceiver(any(), any()) } returns Intent()
        }

        val sdk = spyk(
            PushSdk(
                mockContext,
                getSettingsBag(),
                mockEverlyticApi,
                getFirebaseInstanceIdFacade(),
                mockSdkRepository()
            )
        )

        every { sdk.saveContactSubscriptionFromResponse(any(), any()) } just Runs

        sdk.subscribeContact(USER_EMAIL) {}

        verify(exactly = 1) { mockEverlyticApi.subscribe(ofType(), ofType()) }
    }

    @Test
    @MockK(relaxed = true)
    fun testSubscribe_RequestFails_ReturnsError() {

        val mockEverlyticApi = mockk<EverlyticApi> {
            val slot = slot<EverlyticHttp.ResponseHandler>()
            every { subscribe(ofType(), capture(slot)) } answers {
                slot.captured.onFailure(400, null, null)
            }
        }

        val sdk = spyk(
            PushSdk(
                mockContext(),
                getSettingsBag(),
                mockEverlyticApi,
                getFirebaseInstanceIdFacade(),
                mockSdkRepository()
            )
        )

        assertFails {
            sdk.subscribeContact(USER_EMAIL) {}
        }
    }

    @Test
    @MockK(relaxed = true)
    fun testUnsubscribe_RequestSucceeds_ReturnsSuccess() {
        val mockSdkRepository = mockSdkRepository()

        val mockEverlyticApi = mockk<EverlyticApi> {
            val slot = slot<EverlyticHttp.ResponseHandler>()
            every { unsubscribe(ofType(), capture(slot)) } answers {
                slot.captured.onSuccess(ApiResponse("success", JSONObject()))
            }
        }

        val mockContext = mockk<Context> {
            val mockResources = mockk<Resources> {
                every { getBoolean(R.bool.isTablet) } returns false
            }
            every { resources } returns mockResources
            every { registerReceiver(any(), any()) } returns Intent()
        }

        val sdk = spyk(
            PushSdk(
                mockContext,
                getSettingsBag(),
                mockEverlyticApi,
                getFirebaseInstanceIdFacade(),
                mockSdkRepository
            )
        )

        sdk.unsubscribeCurrentContact()

        verify(exactly = 1) { mockEverlyticApi.unsubscribe(ofType(), ofType()) }
        verify(exactly = 1) { mockSdkRepository.getSubscriptionId() }
        verify(exactly = 1) { mockSdkRepository.removeContactSubscription() }
    }


    @Test
    @MockK(relaxed = true)
    fun testUnsubscribe_RequestFails_ReturnsError() {

        val mockEverlyticApi = mockk<EverlyticApi> {
            val slot = slot<EverlyticHttp.ResponseHandler>()
            every { unsubscribe(ofType(), capture(slot)) } answers {
                slot.captured.onFailure(400, null, null)
            }
        }

        val sdk = spyk(
            PushSdk(
                mockContext(),
                getSettingsBag(),
                mockEverlyticApi,
                getFirebaseInstanceIdFacade(),
                mockSdkRepository()
            )
        )

        sdk.unsubscribeCurrentContact {
            assertFalse { it.isSuccessful }
        }
    }

    @Test
    fun testIsContactSubscribed_WithSubscriptionAndContactIds_ReturnsTrue() {

        val mockSdkRepo = mockSdkRepository().apply {
            every { getSubscriptionId() } returns 1
            every { getContactId() } returns 10
            every { getNewFcmToken() } returns null
        }

        val sdk = spyk(
            PushSdk(
                mockContext(),
                getSettingsBag(),
                mockk(),
                getFirebaseInstanceIdFacade(),
                mockSdkRepo
            )
        )

        assertTrue { sdk.isContactSubscribed() }
        verify { mockSdkRepo.getSubscriptionId() }
        verify { mockSdkRepo.getContactId() }
    }

    @Test
    fun testIsContactSubscribed_NullSubscriptionId_ReturnsFalse() {

        val mockSdkRepo = mockSdkRepository().apply {
            every { getSubscriptionId() } returns null
            every { getContactId() } returns 10
        }

        val sdk = spyk(
            PushSdk(
                mockContext(),
                getSettingsBag(),
                mockk(),
                getFirebaseInstanceIdFacade(),
                mockSdkRepo
            )
        )

        assertFalse { sdk.isContactSubscribed() }
        verify { mockSdkRepo.getSubscriptionId() }
    }

    @Test
    fun testIsContactSubscribed_NullContactId_ReturnsFalse() {
        val mockSdkRepo = mockSdkRepository().apply {
            every { getSubscriptionId() } returns 1
            every { getContactId() } returns null
        }

        val sdk = spyk(
            PushSdk(
                mockContext(),
                getSettingsBag(),
                mockk(),
                getFirebaseInstanceIdFacade(),
                mockSdkRepo
            )
        )

        assertFalse { sdk.isContactSubscribed() }
        verify { mockSdkRepo.getContactId() }
    }

    private fun mockContext(): Context {
        return mockk<Context> {
            every { registerReceiver(any(), any()) } returns Intent()
        }
    }

    @Test
    fun testResubscribeIfRequired_RequiresResubscribe_ResubscribeIsSuccessful() {
        val mockSdkRepo = mockSdkRepository().apply {
            every { getHasSubscription() } returns true
            every { getSubscriptionDatetime() } returns Date(0)
            every { getNewFcmToken() } returns FcmToken("abc", Date())
            every { getContactEmail() } returns "test@example.com"
        }

        val ctx = mockk<Context>(relaxed = true) {
            every { resources.getBoolean(any()) } returns false
        }

        val api = mockk<EverlyticApi> {
            every { subscribe(any(), any()) } just Runs
        }

        val sdk = spyk(
            PushSdk(
                ctx,
                getSettingsBag(),
                api,
                getFirebaseInstanceIdFacade(),
                mockSdkRepo
            )
        )

        sdk.resubscribeIfRequired()

        verify { sdk.resubscribeUserWithToken(any(), any(), any()) }
        verify { api.subscribe(any(), any()) }
    }

    private fun getFirebaseInstanceIdFacade(): FirebaseInstanceIdFacade {
        return mockk {
            val slot = slot<(TokenResult) -> Unit>()
            every { getInstanceId(capture(slot)) } answers {
                slot.captured.invoke(
                    TokenResult(
                        true,
                        "test_instance_id"
                    )
                )
            }
        }
    }

    private fun mockSdkRepository(): SdkRepository {
        return Mock.getSdkRepositoryMock()
    }

    private fun getSettingsBag(): SdkSettings.SdkSettingsBag {
        return SdkSettings.SdkSettingsBag(
            API_INSTALL,
            API_USERNAME,
            API_KEY,
            LIST_ID
        )
    }

    companion object {
        const val API_INSTALL = "install"
        const val API_USERNAME = "username"
        const val API_KEY = "api_key"
        const val LIST_ID = 123
        const val USER_EMAIL = "test@test.com"
    }
}