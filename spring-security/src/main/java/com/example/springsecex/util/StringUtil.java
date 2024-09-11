package com.example.springsecex.util;

import java.util.Locale;
import java.util.Objects;

public final class StringUtil {

    private StringUtil() {
    }

    public static boolean isEmpty(String text) {
        return Objects.isNull(text) || text.isEmpty();
    }

    public static boolean nonEmpty(String text) {
        return Objects.nonNull(text) && !text.isEmpty();
    }

    public static long toLong(String value, long def) {
        if (isEmpty(value)) {
            return def;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static int toInt(String value, int def) {
        if (isEmpty(value)) {
            return def;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static String toLowerCase(String src) {
        return null == src ? null : src.toLowerCase(Locale.getDefault());
    }

    public static String toUpperCase(String src) {
        return null == src ? null : src.toUpperCase(Locale.getDefault());
    }

    public static String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }
}
