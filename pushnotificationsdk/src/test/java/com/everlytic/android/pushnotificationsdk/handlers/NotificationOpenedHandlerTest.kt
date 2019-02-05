package com.everlytic.android.pushnotificationsdk.handlers

import android.content.Context
import android.content.Intent
import com.everlytic.android.pushnotificationsdk.EvIntentExtras
import com.everlytic.android.pushnotificationsdk.Mock
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import io.mockk.*
import org.junit.Test
import java.util.*

class NotificationOpenedHandlerTest {

    @Test
    fun testHandleIntentWithContext_WithCorrectIntent_CompletesSuccessfully() {

        val mockCtx = mockContext()
        val mockSdkRepository = mockSdkRepository()
        val mockEventRepository = mockNotificationEventRepository().apply {
            every { storeNotificationEvent(any(), any()) } just Runs
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

        every { handler invokeNoArgs "scheduleEventUploadWorker" } returns Unit

        val intent = mockIntent().apply {
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
                Date()
            )
            every { hasExtra(EvIntentExtras.EVERLYTIC_DATA) } returns true
            every { getParcelableExtra<EvNotification>(EvIntentExtras.EVERLYTIC_DATA) } returns notificationParcelable
        }

        handler.handleIntentWithContext(mockCtx, intent)

        verify { handler invoke "processIntent" withArguments listOf(mockCtx, intent) }
        verify { handler invoke "setNotificationReadFromIntent" withArguments listOf(intent) }
        verify { mockEventRepository.storeNotificationEvent(any(), any()) }
        verify { handler invokeNoArgs "scheduleEventUploadWorker" }
        verify { mockCtx.startActivity(any()) }
    }

    @Test
    fun testHandleIntentWithContext_WithUnknownIntent_DoesNotRun() {

        val mockCtx = mockContext()
        val mockSdkRepository = mockSdkRepository()
        val mockEventRepository = mockNotificationEventRepository().apply {
            every { storeNotificationEvent(any(), any()) } just Runs
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

    private fun mockContext(): Context {
        return mockk<Context>(relaxed = true).apply {
            val intent = mockIntent()
            every { packageManager.getLaunchIntentForPackage(any()) } returns intent
            every { startActivity(any()) } just Runs
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