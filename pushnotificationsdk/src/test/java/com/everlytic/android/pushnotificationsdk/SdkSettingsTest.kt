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
class SdkSettingsTest {

    @Test
    fun testSdkSettings_ValidBase64ConfigString_DecodesSettings() {
        mockkObject(SdkSettings)

        every { SdkSettings invoke "getConfigurationString" withArguments listOf(ofType<Context>()) } answers {
            "dT1hZG1pbmlzdHJhdG9yO2s9VTNhVHFmY1E2djFIOWlSZG5jVFhjMzZQYTJEOEZ3bmFfOTk5O2k9aHR0cDovL2xvY2FsLmV2ZXJseXRpYy5jb207bD0x"
        }

        val settings = SdkSettings.getSettings(ApplicationProvider.getApplicationContext<Application>())

        assertEquals("administrator", settings.apiUsername)
        assertEquals("U3aTqfcQ6v1H9iRdncTXc36Pa2D8Fwna_999", settings.apiKey)
        assertEquals("http://local.everlytic.com", settings.apiInstall)
        assertEquals(1, settings.listId)
    }

    @Test
    fun testSdkSettings_InvalidBase64ConfigString_DecodesSettings() {
        mockkObject(SdkSettings)

        every { SdkSettings invoke "getConfigurationString" withArguments listOf(ofType<Context>()) } answers {
            "bad"
        }

        assertFails {
            SdkSettings.getSettings(ApplicationProvider.getApplicationContext<Application>())
        }
    }

}