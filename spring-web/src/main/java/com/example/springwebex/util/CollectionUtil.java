package com.example.springwebex.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class CollectionUtil {

    private CollectionUtil() {
        // do nothing
    }

    public static <E> boolean nonEmpty(Collection<E> collection) {
        return Objects.nonNull(collection) && !collection.isEmpty();
    }

    public static <E> boolean nonEmpty(E[] array) {
        return Objects.nonNull(array) && array.length > 0;
    }

    public static <E> boolean isEmpty(Collection<E> col) {
        return null == col || col.isEmpty();
    }

    public static <E> boolean isEmpty(E[] array) {
        return null == array || 0 == array.length;
    }

    public static boolean isEmpty(byte[] array) {
        return null == array || 0 == array.length;
    }

    public static boolean isEmpty(char[] array) {
        return null == array || 0 == array.length;
    }

    public static boolean isEmpty(int[] array) {
        return null == array || 0 == array.length;
    }

    public static boolean isEmpty(long[] array) {
        return null == array || 0 == array.length;
    }

    public static boolean isEmpty(float[] array) {
        return null == array || 0 == array.length;
    }

    public static boolean isEmpty(double[] array) {
        return null == array || 0 == array.length;
    }

    public static <E> List<E> wrap(List<E> list) {
        return null != list ? list : Collections.emptyList();
    }

    public static <K, V> Map<K, V> wrap(Map<K, V> map) {
        return null != map ? map : Collections.emptyMap();
    }

    public static <E> Set<E> wrap(Set<E> set) {
        return null != set ? set : Collections.emptySet();
    }

    public static byte[] wrap(byte[] array) {
        return null != array ? array : new byte[0];
    }

    public static char[] wrap(char[] array) {
        return null != array ? array : new char[0];
    }

    public static int[] wrap(int[] array) {
        return null != array ? array : new int[0];
    }

    public static long[] wrap(long[] array) {
        return null != array ? array : new long[0];
    }

    public static float[] wrap(float[] array) {
        return null != array ? array : new float[0];
    }

    public static <E> E[] wrap(E[] array, Class<E[]> clazz) {
        return null != array ? array : clazz.cast(new Object[0]);
    }

    public static <K, V> V getValueOrAlternative(Map<K, V> map, K... keys) {
        V value = null;

        for (K key : keys) {
            value = map.get(key);
            if (null != value) {
                break;
            }
        }
        return value;

    }

    public static <K, V> V getValueOrAlternative(Map<K, V> map, K key, V alternative) {
        return map.containsKey(key) ? map.get(key) : alternative;
    }

    public static <T> T getValueOrAlternatice(T value, T... alternatives) {
        T ret = value;

        if (null == ret) {
            for (T alter : alternatives) {
                if (null != alter) {
                    ret = alter;
                    break;
                }
            }
        }
        return ret;
    }

    public static <T> Optional<T> getLastOne(T[] array) {
        return array.length > 0 ? Optional.of(array[array.length - 1]) : Optional.empty();
    }

    public static <T> Optional<T> getLastOne(List<T> list) {
        return list.size() > 0 ? Optional.of(list.get(list.size() - 1)) : Optional.empty();
    }
}
