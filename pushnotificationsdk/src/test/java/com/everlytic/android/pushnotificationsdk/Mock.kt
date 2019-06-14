package com.everlytic.android.pushnotificationsdk

import android.content.Context
import com.everlytic.android.pushnotificationsdk.facades.BuildFacade
import com.everlytic.android.pushnotificationsdk.facades.FirebaseInstanceIdFacade
import com.everlytic.android.pushnotificationsdk.facades.TokenResult
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import io.mockk.*

internal object Mock {

    fun BuildFacade() {
        mockkObject(BuildFacade)

        every { BuildFacade.getPlatformVersion() } returns "[test] pversion"
        every { BuildFacade.getDeviceManufacturer() } returns "[test] manufacturer"
        every { BuildFacade.getDeviceModel() } returns "[test] model"
    }

    fun FirebaseInstanceIdFacade() {
        mockkObject(FirebaseInstanceIdFacade)
        val mockFirebaseFacade = mockk<FirebaseInstanceIdFacade>()

        val slot = slot<(TokenResult) -> Unit>()
        every { mockFirebaseFacade.getInstanceId(capture(slot)) } answers {
            slot.captured.invoke(
                TokenResult(
                    true,
                    "test token"
                )
            )
        }
        every { FirebaseInstanceIdFacade.getDefaultInstance(ofType()) } returns mockFirebaseFacade
    }

    fun EvLogger() {
        mockkObject(EvLogger)

        every { EvLogger.d(any(), any(), any()) } returns Unit
        every { EvLogger.w(any(), any(), any()) } returns Unit
    }

    fun SdkSettings() {
        mockkObject(SdkConfiguration)
        val sdkSettings = SdkConfiguration.SdkConfigBag(
            "install_id",
            "push_project_uuid"
        )
        every { SdkConfiguration.getConfigurationBag(ofType<Context>()) } returns sdkSettings
        every { SdkConfiguration.getConfigurationBag(ofType<String>()) } returns sdkSettings
    }

    fun getSdkRepositoryMock(): SdkRepository {
        return mockk {
            every { getDeviceId() } returns "[test] device id"
            every { setDeviceId(any()) } returns "[test] generated device id"
            every { getSubscriptionId() } returns 5
            every { getContactId() } returns 10
            every { removeContactSubscription() } just Runs
            every { getHasSubscription() } returns true
            every { getNewFcmToken() } returns null
            every { getFcmTokenHash() } answers { "test token".getHash() }
        }
    }

}