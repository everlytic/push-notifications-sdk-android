package com.everlytic.android.pushnotificationsdk

import com.everlytic.android.pushnotificationsdk.facades.BuildFacade
import com.everlytic.android.pushnotificationsdk.facades.FirebaseInstanceIdFacade
import com.everlytic.android.pushnotificationsdk.network.EverlyticApi
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import io.mockk.*
import io.mockk.impl.annotations.MockK
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Call

class PushSdkTest {

    @Before
    fun setUp() {
        mockkObject(BuildFacade)

        every { BuildFacade.getPlatformVersion() } returns "[test] pversion"
        every { BuildFacade.getDeviceManufacturer() } returns "[test] manufacturer"
        every { BuildFacade.getDeviceModel() } returns "[test] model"
    }

    @Test
    @MockK(relaxed = true)
    fun testSubscribe_WithValidEmail_ReturnsSuccess() {

        val apiInstall = "install"
        val apiUsername = "username"
        val apiKey = "api_key"
        val projectId = "123"
        val userEmail = "test@test.com"

        val mockEverlyticApi = mockk<EverlyticApi>()
        val mockCall = mockk<Call<ResponseBody>>()
        val mockHttp = mockk<EverlyticHttp>()

        val mockFirebaseIdFacade = mockk<FirebaseInstanceIdFacade>()

        coEvery { mockFirebaseIdFacade.getInstanceId() } returns "test_instance_id"

        every { mockHttp.buildEverlyticApi(apiInstall, apiUsername, apiKey) } answers { mockEverlyticApi }

        every { mockEverlyticApi.subscribe(ofType()) } returns mockCall

        val sdk = PushSdk(apiInstall, apiUsername, apiKey, projectId, mockHttp, mockFirebaseIdFacade)

        sdk.subscribeUser(userEmail)

        verify(exactly = 1) { mockEverlyticApi.subscribe(ofType()) }
    }
}