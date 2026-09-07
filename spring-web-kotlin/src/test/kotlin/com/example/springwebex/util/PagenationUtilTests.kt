package com.example.springwebex.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class PagenationUtilTests {

    @Test
    fun testGetTotalPages() {
        assertEquals(5, PagenationUtil.getTotalPages(100, 20))
        assertEquals(6, PagenationUtil.getTotalPages(101, 20))
        assertEquals(0, PagenationUtil.getTotalPages(0, 20))
    }

    @Test
    fun testPagenationList() {
        val list = (1..100).toList()
        val result = PagenationUtil.pagenationList(list, 20, 1)
        assertEquals(20, result.size)
        assertEquals(1, result.first())
    }

    @Test
    fun testPagenationListOutOfRange() {
        val list = (1..100).toList()
        val result = PagenationUtil.pagenationList(list, 20, 10)
        assertTrue(result.isEmpty())
    }
}
