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

        val apiInstall = "install"
        val apiUsername = "username"
        val apiKey = "api_key"
        val projectId = "123"
        val userEmail = "test@test.com"

        val mockCall = mockk<Call<ResponseBody>>()
        val mockResponse = mockk<Response<ResponseBody>>()
        val mockEverlyticApi = mockk<EverlyticApi>().apply {
            every { subscribe(ofType()) } returns mockCall
            every { subscribe(ofType()).execute() } returns mockResponse
        }
        val mockHttp = mockk<EverlyticHttp>().apply {
            every { buildEverlyticApi(apiInstall, apiUsername, apiKey) } answers { mockEverlyticApi }
        }

        val mockFirebaseIdFacade = mockk<FirebaseInstanceIdFacade>().apply {
            coEvery { getInstanceId() } returns "test_instance_id"
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
                apiInstall,
                apiUsername,
                apiKey,
                projectId,
                mockHttp,
                mockFirebaseIdFacade,
                mockSdkRepository()
            )
        )

        every { sdk.saveContactSubscriptionFromResponse(any()) } just Runs

        runBlocking { sdk.subscribeUser(userEmail) }

        verify(exactly = 1) { mockEverlyticApi.subscribe(ofType()).execute() }
    }

    @Test
    @MockK(relaxed = true)
    fun testSubscribe_RequestFails_ReturnsError() {

        val apiInstall = "install"
        val apiUsername = "username"
        val apiKey = "api_key"
        val projectId = "123"
        val userEmail = "test@test.com"

        val mockResponse = mockk<Response<ResponseBody>>().apply {
            every { code() } returns 400
            every { message() } returns "Test Exception"
        }
        val httpException = HttpException(mockResponse)

        val mockFirebaseIdFacade = mockk<FirebaseInstanceIdFacade>().apply {
            coEvery { getInstanceId() } returns "test_instance_id"
        }
        val mockEverlyticApi = mockk<EverlyticApi>().apply {
            val mockCall = mockk<Call<ResponseBody>>()
            every { subscribe(ofType()) } returns mockCall
            every { subscribe(ofType()).execute() } throws httpException
        }
        val mockHttp = mockk<EverlyticHttp>().apply {
            every { buildEverlyticApi(apiInstall, apiUsername, apiKey) } answers { mockEverlyticApi }
        }

        val sdk = spyk(
            PushSdk(
                mockk(),
                apiInstall,
                apiUsername,
                apiKey,
                projectId,
                mockHttp,
                mockFirebaseIdFacade,
                mockSdkRepository()
            )
        )

        assertFails {
            runBlocking {
                sdk.subscribeUser(userEmail)
            }
        }
    }

    private fun mockSdkRepository(): SdkRepository {
        return mockk<SdkRepository>().apply {
            every { getDeviceId() } returns "[test] device id"
            every { setDeviceId(any()) } returns "[test] generated device id"
        }
    }
}