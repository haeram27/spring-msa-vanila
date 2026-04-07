package com.example.springwebex.util

object PagenationUtil {

    fun getTotalPages(totalCount: Int, pageSize: Int): Int {
        if (totalCount < 0) return 0
        if (pageSize <= 0) return 0
        return kotlin.math.ceil(totalCount.toDouble() / pageSize).toInt()
    }

    fun <T> pagenationList(list: List<T>?, pageSize: Int, pageNumber: Int): List<T> {
        if (pageSize < 1 || pageNumber < 1) return emptyList()
        if (list == null || list.isEmpty()) return emptyList()
        
        val totalCount = list.size
        val startInclusive = kotlin.math.min((pageNumber - 1) * pageSize, totalCount)
        val endExclusive = kotlin.math.min(startInclusive + pageSize, totalCount)

        return list.subList(startInclusive, endExclusive)
    }
}
