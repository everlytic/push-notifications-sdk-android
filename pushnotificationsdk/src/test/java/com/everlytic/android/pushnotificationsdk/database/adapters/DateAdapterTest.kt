package com.everlytic.android.pushnotificationsdk.database.adapters

import com.everlytic.android.pushnotificationsdk.database.adapters.DateAdapter
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFails

class DateAdapterTest {

    @Test
    fun testEncodeDate_ReturnsValidString() {
        val adapter = DateAdapter()
        val date = Date(0)

        val result = adapter.encode(date)


        assertEquals("1970-01-01T00:00:00.000Z", result)
    }

    @Test
    fun testDecodeString_StringIsValidFormat_ReturnsDate() {
        val adapter = DateAdapter()
        val dateString = "1970-01-01T00:00:00.000Z"

        val result = adapter.decode(dateString)


        assertEquals(Date(0), result)
    }

    @Test
    fun testDecodeString_StringIsInvalidFormat_DecodeFails() {
        val adapter = DateAdapter()
        val dateString = "1970-01-01 00:00:00"

        assertFails {
            adapter.decode(dateString)
        }
    }

}