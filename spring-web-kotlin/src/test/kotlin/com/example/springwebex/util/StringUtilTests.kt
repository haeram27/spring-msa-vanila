package com.example.springwebex.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class StringUtilTests {

    @Test
    fun testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null))
        assertTrue(StringUtil.isEmpty(""))
        assertFalse(StringUtil.isEmpty("text"))
    }

    @Test
    fun testNonEmpty() {
        assertFalse(StringUtil.nonEmpty(null))
        assertFalse(StringUtil.nonEmpty(""))
        assertTrue(StringUtil.nonEmpty("text"))
    }

    @Test
    fun testToLong() {
        assertEquals(123L, StringUtil.toLong("123", 0L))
        assertEquals(0L, StringUtil.toLong("invalid", 0L))
    }

    @Test
    fun testToInt() {
        assertEquals(123, StringUtil.toInt("123", 0))
        assertEquals(0, StringUtil.toInt("invalid", 0))
    }
}
