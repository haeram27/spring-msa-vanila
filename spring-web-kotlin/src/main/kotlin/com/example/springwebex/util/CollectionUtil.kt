package com.example.springwebex.util

import java.util.*

object CollectionUtil {

    fun <E> nonEmpty(collection: Collection<E>?): Boolean {
        return collection != null && collection.isNotEmpty()
    }

    fun <E> isEmpty(col: Collection<E>?): Boolean {
        return col == null || col.isEmpty()
    }

    fun <E> isEmpty(array: Array<E>?): Boolean {
        return array == null || array.isEmpty()
    }

    fun isEmpty(array: ByteArray?): Boolean {
        return array == null || array.isEmpty()
    }

    fun <E> wrap(list: List<E>?): List<E> {
        return list ?: Collections.emptyList()
    }

    fun <K, V> wrap(map: Map<K, V>?): Map<K, V> {
        return map ?: Collections.emptyMap()
    }

    fun <T> getValueOrAlternative(map: Map<T, T?>, vararg keys: T): T? {
        var value: T? = null
        for (key in keys) {
            value = map[key]
            if (value != null) {
                break
            }
        }
        return value
    }
}
