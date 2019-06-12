package com.everlytic.android.pushnotificationsdk

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFails

@RunWith(AndroidJUnit4::class)
class SdkConfigurationTest {

    @Test
    fun testSdkSettings_ValidBase64ConfigString_DecodesSettings() {
        mockkObject(SdkConfiguration)

        val configString = "cD1hNjc1Zjk0MC1iN2Y1LTQ5MTctOGU4Mi00NTU5Y2Q0MTM0MWI7aT1odHRwOi8vbG9jYWwuZXZlcmx5dGljLmNvbQ=="

        val settings = SdkConfiguration.getConfigurationBag(configString)

        assertEquals("a675f940-b7f5-4917-8e82-4559cd41341b", settings.pushProjectUuid)
        assertEquals("http://local.everlytic.com", settings.apiInstall)
    }

    @Test
    fun testSdkSettings_InvalidBase64ConfigString_ThrowsError() {
        mockkObject(SdkConfiguration)

        every { SdkConfiguration invoke "getConfigurationString" withArguments listOf(ofType<Context>()) } answers {
            "bad"
        }

        assertFails {
            SdkConfiguration.getConfigurationBag(ApplicationProvider.getApplicationContext<Application>())
        }
    }

}