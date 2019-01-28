package com.everlytic.android.pushnotificationsdk

import com.everlytic.android.pushnotificationsdk.facades.BuildFacade
import com.everlytic.android.pushnotificationsdk.facades.FirebaseInstanceIdFacade
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject

object Mock {

    fun BuildFacade() {
        mockkObject(BuildFacade)

        every { BuildFacade.getPlatformVersion() } returns "[test] pversion"
        every { BuildFacade.getDeviceManufacturer() } returns "[test] manufacturer"
        every { BuildFacade.getDeviceModel() } returns "[test] model"
    }

    fun FirebaseInstanceIdFacade() {
        mockkObject(FirebaseInstanceIdFacade)
        val mockFirebaseFacade = mockk<FirebaseInstanceIdFacade>()

        coEvery { mockFirebaseFacade.getInstanceId() } returns "test token"
        every { FirebaseInstanceIdFacade.getDefaultInstance() } returns mockFirebaseFacade
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

}