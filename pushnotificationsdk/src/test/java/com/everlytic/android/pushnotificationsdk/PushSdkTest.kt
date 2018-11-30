package com.everlytic.android.pushnotificationsdk

import android.os.Build
import com.everlytic.android.pushnotificationsdk.network.EverlyticApi
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import io.mockk.*
import org.junit.Test

class PushSdkTest {

    @Test
    fun testSubscribe_WithValidEmail_ReturnsSuccess() {

        val apiInstall = "install"
        val apiUsername = "username"
        val apiKey = "api_key"
        val projectId = "123"
        val userEmail = "test@test.com"

        val mockEverlyticApi = mockk<EverlyticApi>()
        val mockHttp = mockk<EverlyticHttp>()

        val mockFirebaseIdInstance = getMockFirebaseInstanceId()

        mockkStatic("android.os.Build")

        every { Build.VERSION.RELEASE } returns "test"

        every { mockHttp.buildEverlyticApi(apiInstall, apiUsername, apiKey) } returns mockEverlyticApi
        val sdk = PushSdk(apiInstall, apiUsername, apiKey, projectId, mockHttp, mockFirebaseIdInstance)

        sdk.subscribeUser(userEmail)

        verify(exactly = 1) { mockEverlyticApi.subscribe(any()) }
    }

    private fun getMockFirebaseInstanceId(): FirebaseInstanceId {
        val mockFirebaseIdInstance = mockk<FirebaseInstanceId>()
        val mockInstanceId = spyk<Task<InstanceIdResult>>()

        every { mockFirebaseIdInstance.instanceId } returns mockInstanceId
        every { mockInstanceId.addOnSuccessListener(any()) } returns mockInstanceId
        val slot = slot<OnSuccessListener<InstanceIdResult>>()
        every { mockInstanceId.addOnSuccessListener(capture(slot)) } answers {
            val instanceId = mockk<InstanceIdResult>()
            every { instanceId.token } returns "token"
            slot.captured.onSuccess(instanceId)
            mockInstanceId
        }
        return mockFirebaseIdInstance
    }

}