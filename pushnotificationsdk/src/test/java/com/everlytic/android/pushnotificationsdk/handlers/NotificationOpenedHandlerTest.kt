package com.everlytic.android.pushnotificationsdk.handlers

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.everlytic.android.pushnotificationsdk.EvIntentExtras
import com.everlytic.android.pushnotificationsdk.Mock
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.models.LaunchAppNotificationAction
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import io.mockk.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class NotificationOpenedHandlerTest {

    @Test
    fun testHandleIntentWithContext_WithLaunchAction_CompletesSuccessfully() {

        val context = getContext()
        val mockSdkRepository = mockSdkRepository()
        val mockEventRepository = mockNotificationEventRepository().apply {
            every { storeNotificationEvent(any()) } just Runs
        }
        val mockLogRepository = mockNotificationLogRepository().apply {
            every { setNotificationAsRead(any(), any()) } just Runs
        }

        val handler = spyk(
            objToCopy = NotificationOpenedHandler(
                mockSdkRepository,
                mockEventRepository,
                mockLogRepository,
                mockk(relaxed = true)
            ),
            recordPrivateCalls = true
        )

        every { handler invoke "scheduleEventUploadWorker" withArguments listOf(ofType<Context>()) } returns Unit

        val intent = spyk(Intent()).apply {
            val notificationParcelable = EvNotification(
                0,
                0,
                "test",
                "test body",
                false,
                0,
                0,
                0,
                emptyList(),
                emptyMap(),
                Date()
            )
            putExtra(EvIntentExtras.EVERLYTIC_DATA, notificationParcelable)
            putExtra(EvIntentExtras.ACTION_TYPE, LaunchAppNotificationAction.ACTION_ID)

            every { hasExtra(EvIntentExtras.EVERLYTIC_DATA) } returns true
        }

        handler.handleIntentWithContext(context, intent)

        verify { handler invoke "processIntent" withArguments listOf(context, intent) }
        verify { handler invoke "setNotificationReadFromIntent" withArguments listOf(intent) }
        verify { mockEventRepository.storeNotificationEvent(any()) }
        verify { handler invoke "scheduleEventUploadWorker" withArguments listOf(ofType<Context>()) }
        verify { context.startActivity(any()) }
    }

    @Test
    fun testHandleIntentWithContext_WithUnknownIntent_DoesNotRun() {

        val mockCtx = getContext()
        val mockSdkRepository = mockSdkRepository()
        val mockEventRepository = mockNotificationEventRepository().apply {
            every { storeNotificationEvent(any()) } just Runs
        }
        val mockLogRepository = mockNotificationLogRepository().apply {
            every { setNotificationAsRead(any(), any()) } just Runs
        }

        val handler = spyk(
            objToCopy = NotificationOpenedHandler(
                mockSdkRepository,
                mockEventRepository,
                mockLogRepository,
                mockk(relaxed = true)
            ),
            recordPrivateCalls = true
        )

        val intent = mockIntent().apply {
            every { hasExtra(EvIntentExtras.EVERLYTIC_DATA) } returns false
        }

        handler.handleIntentWithContext(mockCtx, intent)

        verify(inverse = true) { handler invoke "processIntent" withArguments listOf(mockCtx, intent) }
    }

    private fun mockIntent() = mockk<Intent>(relaxed = true)

    private fun getContext(): Context {
        return spyk(ApplicationProvider.getApplicationContext<Application>()).apply {
            every { packageManager.getLaunchIntentForPackage(any()) } returns Intent()
        }
    }

    private fun mockSdkRepository(): SdkRepository {
        return Mock.getSdkRepositoryMock()
    }

    private fun mockNotificationEventRepository(): NotificationEventRepository {
        return mockk()
    }

    private fun mockNotificationLogRepository(): NotificationLogRepository {
        return mockk()
    }

}