package com.everlytic.android.pushnotificationsdk.repositories

import android.content.SharedPreferences
import com.everlytic.android.pushnotificationsdk.Mock
import com.everlytic.android.pushnotificationsdk.models.ApiSubscription
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SdkRepositoryTest {

    @Before
    fun setUp() {
        Mock.EvLogger()
    }

    @Test
    fun testRemoveContactSubscription_ClearsSubscriptionData() {

        val mockEditor = mockSharedPrefsEditor()

        val mockPrefs = mockSharedPreferences(mockEditor)

        val repository = spyk(objToCopy = SdkRepository(mockk()))

        every { repository getProperty "preferences" } answers { mockPrefs }

        repository.removeContactSubscription()

        verify { mockEditor.remove(SdkRepository.CONTACT_ID) }
        verify { mockEditor.remove(SdkRepository.CONTACT_EMAIL) }
        verify { mockEditor.remove(SdkRepository.NEW_FCM_TOKEN) }
        verify { mockEditor.remove(SdkRepository.SUBSCRIPTION_ID) }
        verify { mockEditor.remove(SdkRepository.SUBSCRIPTION_DATETIME) }
        verify { mockEditor.remove(SdkRepository.NEW_FCM_TOKEN_DATETIME) }
    }

    @Test
    fun testSetContactSubscription_StoresSubscriptionData() {
        val mockEditor = mockSharedPrefsEditor()

        val mockPrefs = mockSharedPreferences(mockEditor)

        val repository = spyk(objToCopy = SdkRepository(mockk()))

        every { repository getProperty "preferences" } answers { mockPrefs }

        val subscriptionId = "1"
        val contactId = "4"
        val contactEmail = "test@example.com"

        val apiSubscription = ApiSubscription(
            subscriptionId,
            "2",
            "3",
            contactId,
            "123-456789"
        )

        repository.setContactSubscription(null, contactEmail, apiSubscription)

        verify { mockEditor.putLong(SdkRepository.SUBSCRIPTION_ID, subscriptionId.toLong()) }
        verify { mockEditor.putLong(SdkRepository.CONTACT_ID, contactId.toLong()) }
    }

    private fun mockSharedPreferences(mockEditor: SharedPreferences.Editor): SharedPreferences {
        return mockk(relaxed = true) {
            every { edit() } returns mockEditor
        }
    }

    private fun mockSharedPrefsEditor(): SharedPreferences.Editor {
        return mockk(relaxed = true) {
            every { remove(any()) } returns this
        }
    }

}