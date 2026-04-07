package com.example.springwebex.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CollectionUtilTests {

    @Test
    fun testNonEmpty() {
        val list = listOf(1, 2, 3)
        assertTrue(CollectionUtil.nonEmpty(list))
    }

    @Test
    fun testIsEmpty() {
        val list: List<Int>? = null
        assertTrue(CollectionUtil.isEmpty(list))
    }

    @Test
    fun testWrapList() {
        val wrapped = CollectionUtil.wrap(null as List<Int>?)
        assertTrue(wrapped.isEmpty())
    }

    @Test
    fun testWrapMap() {
        val wrapped = CollectionUtil.wrap(null as Map<String, String>?)
        assertTrue(wrapped.isEmpty())
    }
}
