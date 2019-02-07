package com.everlytic.android.pushnotificationsdk

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class FunctionsTest {

    @Test
    fun testDecodeJsonMap_WithJsonString_ReturnsValidMapObject() {

        val testString = "{\"object1\":\"object1 value\", \"object2\":\"object2 value\"}"
        val expected = mapOf("object1" to "object1 value", "object2" to "object2 value")

        val actual = decodeJsonMap(testString)

        assertEquals(expected, actual)
    }

    @Test
    fun testDecodeJsonMap_WithMalformedString_ThrowsException() {
        val testString = "{\"object1\":}"

        assertFails { decodeJsonMap(testString) }
    }

}