package com.everlytic.android.pushnotificationsdk.database.adapters

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFails

class DateAdapterTest {

    @Test
    fun testEncodeDate_ReturnsValidString() {
        val date = Date(0)

        val result = date.toIso8601String()

        assertEquals("1970-01-01T00:00:00.000Z", result)
    }

    @Test
    fun testDecodeString_StringIsValidFormat_ReturnsDate() {
        val dateString = "1970-01-01T00:00:00.000Z"

        val result = dateString.toDate()

        assertEquals(Date(0), result)
    }

    @Test
    fun testDecodeString_StringIsInvalidFormat_DecodeFails() {
        val dateString = "1970-01-01 00:00:00"

        assertFails {
            dateString.toDate()
        }
    }

}