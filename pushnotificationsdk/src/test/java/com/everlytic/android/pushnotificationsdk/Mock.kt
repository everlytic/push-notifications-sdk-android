package com.everlytic.android.pushnotificationsdk

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
        every { FirebaseInstanceIdFacade.getDefaultInstance() } returns mockFirebaseFacade
    }

    fun EvLogger() {
        mockkObject(EvLogger)

        every { EvLogger.d(any(), any(), any()) } returns Unit
        every { EvLogger.w(any(), any(), any()) } returns Unit
    }

    fun SdkSettings() {
        mockkObject(SdkSettings)
        every { SdkSettings.getSettings(ofType()) } returns SdkSettings.SdkSettingsBag(
            "install_id",
            "api_username",
            "api_key",
            0
        )
    }

    fun getSdkRepositoryMock(): SdkRepository {
        return mockk {
            every { getDeviceId() } returns "[test] device id"
            every { setDeviceId(any()) } returns "[test] generated device id"
            every { getSubscriptionId() } returns 5
            every { getContactId() } returns 10
            every { removeContactSubscription() } just Runs
        }
    }

}