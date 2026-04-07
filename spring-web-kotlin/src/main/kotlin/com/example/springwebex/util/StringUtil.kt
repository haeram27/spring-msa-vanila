package com.example.springwebex.util

import java.util.Locale

object StringUtil {

    fun isEmpty(text: String?): Boolean {
        return text == null || text.isEmpty()
    }

    fun nonEmpty(text: String?): Boolean {
        return text != null && text.isNotEmpty()
    }

    fun toLong(value: String?, def: Long): Long {
        if (isEmpty(value)) return def
        return try {
            value!!.toLong()
        } catch (e: NumberFormatException) {
            def
        }
    }

    fun toInt(value: String?, def: Int): Int {
        if (isEmpty(value)) return def
        return try {
            value!!.toInt()
        } catch (e: NumberFormatException) {
            def
        }
    }

    fun toLowerCase(src: String?): String? {
        return src?.lowercase(Locale.getDefault())
    }

    fun toUpperCase(src: String?): String? {
        return src?.uppercase(Locale.getDefault())
    }

    fun toString(obj: Any?): String? {
        return obj?.toString()
    }
}
