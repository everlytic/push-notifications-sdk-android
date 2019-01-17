package com.everlytic.android.pushnotificationsdk

import android.content.Context
import android.content.res.Resources
import com.everlytic.android.pushnotificationsdk.facades.FirebaseInstanceIdFacade
import com.everlytic.android.pushnotificationsdk.network.EverlyticApi
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import kotlin.test.assertFails

class PushSdkTest {

    @Before
    fun setUp() {
        Mock.BuildFacade()
    }

    @Test
    @MockK(relaxed = true)
    fun testSubscribe_RequestSucceeds_ReturnsSuccess() {

        val mockEverlyticApi = mockk<EverlyticApi>().apply {
            val mockCall = mockk<Call<ResponseBody>>()
            val mockResponse = mockk<Response<ResponseBody>>()
            every { subscribe(ofType()) } returns mockCall
            every { subscribe(ofType()).execute() } returns mockResponse
        }
        val mockHttp = mockk<EverlyticHttp>().apply {
            every { buildEverlyticApi(API_INSTALL, API_USERNAME, API_KEY) } answers { mockEverlyticApi }
        }

        val mockContext = mockk<Context>().apply {
            val mockResources = mockk<Resources>().apply {
                every { getBoolean(R.bool.isTablet) } returns false
            }
            every { resources } returns mockResources
        }

        val sdk = spyk(
            PushSdk(
                mockContext,
                API_INSTALL,
                API_USERNAME,
                API_KEY,
                PROJECT_ID,
                mockHttp,
                getFirebaseInstanceIdFacade(),
                mockSdkRepository()
            )
        )

        every { sdk.saveContactSubscriptionFromResponse(any()) } just Runs

        runBlocking { sdk.subscribeContact(USER_EMAIL) }

        verify(exactly = 1) { mockEverlyticApi.subscribe(ofType()).execute() }
    }

    @Test
    @MockK(relaxed = true)
    fun testSubscribe_RequestFails_ReturnsError() {

        val mockResponse = mockk<Response<ResponseBody>>().apply {
            every { code() } returns 400
            every { message() } returns "Test Exception"
        }
        val httpException = HttpException(mockResponse)

        val mockEverlyticApi = mockk<EverlyticApi>().apply {
            val mockCall = mockk<Call<ResponseBody>>()
            every { subscribe(ofType()) } returns mockCall
            every { subscribe(ofType()).execute() } throws httpException
        }
        val mockHttp = mockk<EverlyticHttp>().apply {
            every { buildEverlyticApi(API_INSTALL, API_USERNAME, API_KEY) } answers { mockEverlyticApi }
        }

        val sdk = spyk(
            PushSdk(
                mockk(),
                API_INSTALL,
                API_USERNAME,
                API_KEY,
                PROJECT_ID,
                mockHttp,
                getFirebaseInstanceIdFacade(),
                mockSdkRepository()
            )
        )

        assertFails {
            runBlocking {
                sdk.subscribeContact(USER_EMAIL)
            }
        }
    }

    @Test
    @MockK(relaxed = true)
    fun testUnsubscribe_RequestSucceeds_ReturnsSuccess() {
        val mockSdkRepository = mockSdkRepository()

        val mockEverlyticApi = mockk<EverlyticApi>().apply {
            val mockCall = mockk<Call<ResponseBody>>()
            val mockResponse = mockk<Response<ResponseBody>>()
            every { unsubscribe(ofType()) } returns mockCall
            every { unsubscribe(ofType()).execute() } returns mockResponse
        }
        val mockHttp = mockk<EverlyticHttp>().apply {
            every { buildEverlyticApi(API_INSTALL, API_USERNAME, API_KEY) } answers { mockEverlyticApi }
        }

        val mockContext = mockk<Context>().apply {
            val mockResources = mockk<Resources>().apply {
                every { getBoolean(R.bool.isTablet) } returns false
            }
            every { resources } returns mockResources
        }

        val sdk = spyk(
            PushSdk(
                mockContext,
                API_INSTALL,
                API_USERNAME,
                API_KEY,
                PROJECT_ID,
                mockHttp,
                getFirebaseInstanceIdFacade(),
                mockSdkRepository
            )
        )

        runBlocking { sdk.unsubscribeCurrentContact() }

        verify(exactly = 1) { mockEverlyticApi.unsubscribe(ofType()).execute() }
        verify(exactly = 1) { mockSdkRepository.getSubscriptionId() }
        verify(exactly = 1) { mockSdkRepository.removeContactSubscription() }
    }

    @Test
    @MockK(relaxed = true)
    fun testUnsubscribe_RequestFails_ReturnsError() {
        val mockResponse = mockk<Response<ResponseBody>>().apply {
            every { code() } returns 400
            every { message() } returns "Test Exception"
        }

        val mockEverlyticApi = mockk<EverlyticApi>().apply {
            val httpException = HttpException(mockResponse)
            val mockCall = mockk<Call<ResponseBody>>()
            every { subscribe(ofType()) } returns mockCall
            every { subscribe(ofType()).execute() } throws httpException
        }
        val mockHttp = mockk<EverlyticHttp>().apply {
            every { buildEverlyticApi(API_INSTALL, API_USERNAME, API_KEY) } answers { mockEverlyticApi }
        }

        val sdk = spyk(
            PushSdk(
                mockk(),
                API_INSTALL,
                API_USERNAME,
                API_KEY,
                PROJECT_ID,
                mockHttp,
                getFirebaseInstanceIdFacade(),
                mockSdkRepository()
            )
        )

        assertFails {
            runBlocking {
                sdk.unsubscribeCurrentContact()
            }
        }
    }

    private fun getFirebaseInstanceIdFacade(): FirebaseInstanceIdFacade {
        return mockk<FirebaseInstanceIdFacade>().apply {
            coEvery { getInstanceId() } returns "test_instance_id"
        }
    }

    private fun mockSdkRepository(): SdkRepository {
        return mockk<SdkRepository>().apply {
            every { getDeviceId() } returns "[test] device id"
            every { setDeviceId(any()) } returns "[test] generated device id"
            every { getSubscriptionId() } returns 5
            every { removeContactSubscription() } just Runs
        }
    }

    companion object {
        const val API_INSTALL = "install"
        const val API_USERNAME = "username"
        const val API_KEY = "api_key"
        const val PROJECT_ID = "123"
        const val USER_EMAIL = "test@test.com"
    }
}